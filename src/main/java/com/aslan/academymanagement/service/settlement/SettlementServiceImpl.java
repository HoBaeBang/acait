package com.aslan.academymanagement.service.settlement;

import com.aslan.academymanagement.domain.*;
import com.aslan.academymanagement.domain.enums.*;
import com.aslan.academymanagement.dto.SettlementDetailResponse;
import com.aslan.academymanagement.dto.SettlementForecastResponse;
import com.aslan.academymanagement.dto.SettlementResponse;
import com.aslan.academymanagement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final LectureRecordRepository lectureRecordRepository;
    private final LectureStudentRepository lectureStudentRepository;
    private final LectureRepository lectureRepository;
    private final DeductionItemRepository deductionItemRepository;
    private final StudentBalanceRepository studentBalanceRepository; // 추가

    @Override
    public void calculateMonthlySettlement(String yearMonth) {
        log.info("💰 {} 월 정산 계산 시작 (전체)", yearMonth);
        // 기존 로직 (전체 정산)
        calculateSettlementInternal(yearMonth, null);
    }

    @Override
    public void calculateMySettlement(Member instructor, String yearMonth) {
        log.info("💰 {} 월 정산 계산 시작 (강사: {})", yearMonth, instructor.getName());
        // 강사 본인 정산
        calculateSettlementInternal(yearMonth, instructor);
    }

    // 공통 정산 로직
    private void calculateSettlementInternal(String yearMonth, Member targetInstructor) {
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<AttendanceStatus> targetStatuses = List.of(
                AttendanceStatus.ATTENDED,
                AttendanceStatus.LATE,
                AttendanceStatus.MAKEUP,
                AttendanceStatus.ABSENT
        );

        // 1. 정산 대상 수업 기록 조회
        List<LectureRecord> records;
        if (targetInstructor != null) {
            records = lectureRecordRepository.findSettlementTargets(startDate, endDate, targetStatuses).stream()
                    .filter(r -> r.getLecture().getTeacher().getId().equals(targetInstructor.getId()))
                    .collect(Collectors.toList());
        } else {
            records = lectureRecordRepository.findSettlementTargets(startDate, endDate, targetStatuses);
        }

        // 2. 강사별 그룹화
        Map<Member, List<LectureRecord>> recordsByInstructor = records.stream()
                .collect(Collectors.groupingBy(record -> record.getLecture().getTeacher()));

        // 3. 계산 및 저장
        for (Map.Entry<Member, List<LectureRecord>> entry : recordsByInstructor.entrySet()) {
            Member instructor = entry.getKey();
            List<LectureRecord> instructorRecords = entry.getValue();
            Academy academy = instructor.getAcademy();

            BigDecimal totalAmount = BigDecimal.ZERO;

            for (LectureRecord record : instructorRecords) {
                Student student = record.getStudent();
                if (student.getStatus() == StudentStatus.DISCHARGED &&
                        student.getDischargeDate() != null &&
                        record.getDate().isAfter(student.getDischargeDate())) {
                    continue;
                }

                // [수강료 이월 로직 적용]
                // 학생의 잔액이 충분한지 확인하고 정산 금액에 반영해야 함.
                // 하지만 현재 구조상 강사 정산은 '수업 횟수 * 단가'로 계산되고 있음.
                // 수강료 납부 여부와 관계없이 강사에게 정산해주는 구조라면 기존 로직 유지.
                // 만약 '납부된 수강료 한도 내에서' 정산해주는 구조라면 로직 변경 필요.
                // 요구사항 문서에는 "강사 정산은 usedAmount (실제 수업 진행 금액)를 기준으로 계산"이라고 되어 있음.
                // usedAmount는 이미 수업 횟수 * 단가로 계산된 금액임.
                // 즉, 학생이 돈을 냈든 안 냈든 수업을 했으면 강사에게 정산해주는 것이 일반적임 (학원 부담).
                // 다만, StudentBalance를 업데이트하는 로직은 TuitionService에서 수행하므로 여기서는 강사 정산금만 계산하면 됨.
                
                // 따라서 기존 로직 유지 (수업 횟수 기반 정산)
                BigDecimal price = record.getLecture().getDefaultPrice();
                if (price != null) {
                    totalAmount = totalAmount.add(price);
                }
            }

            Settlement settlement = settlementRepository.findByInstructorAndYearMonth(instructor, yearMonth)
                    .orElse(Settlement.builder()
                            .academy(academy)
                            .instructor(instructor)
                            .yearMonth(yearMonth)
                            .status(SettlementStatus.OPEN)
                            .build());

            if (settlement.getStatus() == SettlementStatus.CLOSED) {
                log.warn("⚠️ 이미 마감된 정산입니다. (강사: {}, 월: {})", instructor.getName(), yearMonth);
                continue;
            }

            // 공제 항목 적용
            List<DeductionItem> deductionItems = deductionItemRepository.findAllByAcademy(academy);
            BigDecimal taxAmount = calculateTax(totalAmount, deductionItems);

            settlement.updateTotalAmount(totalAmount);
            settlementRepository.save(settlement);

            log.info("✅ 정산 완료: 강사={}, 금액={}, 공제액(예상)={}", instructor.getName(), totalAmount, taxAmount);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> getMonthlySettlements(Member admin, String yearMonth) {
        if (admin.getRole() != Role.ROLE_OWNER) {
            throw new IllegalArgumentException("정산 현황 조회 권한이 없습니다.");
        }

        Academy academy = admin.getAcademy();
        List<Settlement> settlements = settlementRepository.findAllByAcademyAndYearMonth(academy, yearMonth);
        List<DeductionItem> deductionItems = deductionItemRepository.findAllByAcademy(academy);
        
        return settlements.stream()
                .map(settlement -> {
                    BigDecimal totalAmount = settlement.getTotalAmount();
                    BigDecimal taxAmount = calculateTax(totalAmount, deductionItems);
                    BigDecimal realAmount = totalAmount.subtract(taxAmount);
                    
                    return SettlementResponse.builder()
                            .settlementId(settlement.getId())
                            .instructorName(settlement.getInstructor().getName())
                            .yearMonth(settlement.getYearMonth())
                            .totalAmount(totalAmount)
                            .taxAmount(taxAmount)
                            .realAmount(realAmount)
                            .status(settlement.getStatus())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> getMySettlements(Member instructor, String yearMonth) {
        List<Settlement> settlements = settlementRepository.findAllByInstructorAndYearMonth(instructor, yearMonth);
        List<DeductionItem> deductionItems = deductionItemRepository.findAllByAcademy(instructor.getAcademy());

        return settlements.stream()
                .map(settlement -> {
                    BigDecimal totalAmount = settlement.getTotalAmount();
                    BigDecimal taxAmount = calculateTax(totalAmount, deductionItems);
                    BigDecimal realAmount = totalAmount.subtract(taxAmount);
                    
                    return SettlementResponse.builder()
                            .settlementId(settlement.getId())
                            .instructorName(settlement.getInstructor().getName())
                            .yearMonth(settlement.getYearMonth())
                            .totalAmount(totalAmount)
                            .taxAmount(taxAmount)
                            .realAmount(realAmount)
                            .status(settlement.getStatus())
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    private BigDecimal calculateTax(BigDecimal totalAmount, List<DeductionItem> deductionItems) {
        if (deductionItems.isEmpty()) {
            return totalAmount.multiply(new BigDecimal("0.033")).setScale(0, java.math.RoundingMode.FLOOR);
        }
        
        BigDecimal taxAmount = BigDecimal.ZERO;
        for (DeductionItem item : deductionItems) {
            BigDecimal deduction = BigDecimal.ZERO;
            if (item.getType() == DeductionType.PERCENT) {
                BigDecimal rate = item.getValue().divide(BigDecimal.valueOf(100));
                deduction = totalAmount.multiply(rate).setScale(0, java.math.RoundingMode.FLOOR);
            } else if (item.getType() == DeductionType.FIXED_AMOUNT) {
                deduction = item.getValue();
            }
            taxAmount = taxAmount.add(deduction);
        }
        return taxAmount;
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportSettlementToExcel(Member admin, String yearMonth) {
        List<SettlementResponse> settlements = getMonthlySettlements(admin, yearMonth);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("정산 내역");

            Row headerRow = sheet.createRow(0);
            String[] columns = {"강사명", "정산월", "세전 총액", "공제액", "실지급액", "상태"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }

            int rowIdx = 1;
            for (SettlementResponse settlement : settlements) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(settlement.getInstructorName());
                row.createCell(1).setCellValue(settlement.getYearMonth());
                row.createCell(2).setCellValue(settlement.getTotalAmount().doubleValue());
                row.createCell(3).setCellValue(settlement.getTaxAmount().doubleValue());
                row.createCell(4).setCellValue(settlement.getRealAmount().doubleValue());
                row.createCell(5).setCellValue(settlement.getStatus().getDescription());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("엑셀 파일 생성 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementDetailResponse> getSettlementDetails(Member member, Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("해당 정산 정보가 없습니다."));

        if (member.getRole() == Role.ROLE_OWNER) {
            if (!settlement.getAcademy().getId().equals(member.getAcademy().getId())) {
                throw new IllegalArgumentException("다른 학원의 정산 정보는 조회할 수 없습니다.");
            }
        } else if (member.getRole() == Role.ROLE_INSTRUCTOR) {
            if (!settlement.getInstructor().getId().equals(member.getId())) {
                throw new IllegalArgumentException("본인의 정산 정보만 조회할 수 있습니다.");
            }
        } else {
            throw new IllegalArgumentException("정산 상세 조회 권한이 없습니다.");
        }

        YearMonth ym = YearMonth.parse(settlement.getYearMonth());
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<AttendanceStatus> targetStatuses = List.of(
                AttendanceStatus.ATTENDED,
                AttendanceStatus.LATE,
                AttendanceStatus.MAKEUP,
                AttendanceStatus.ABSENT
        );

        List<LectureRecord> records = lectureRecordRepository.findByInstructorAndDateBetweenAndStatusIn(
                settlement.getInstructor(), startDate, endDate, targetStatuses
        );

        return records.stream()
                .map(SettlementDetailResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SettlementForecastResponse getSettlementForecast(Member instructor, String yearMonth) {
        // 1. 확정 금액
        BigDecimal confirmedAmount = settlementRepository.findByInstructorAndYearMonth(instructor, yearMonth)
                .map(Settlement::getTotalAmount)
                .orElse(BigDecimal.ZERO);

        // 2. 예정 금액 계산
        BigDecimal expectedAmount = BigDecimal.ZERO;
        
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate today = LocalDate.now();
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        if (endDate.isBefore(today)) {
            return buildForecastResponse(instructor, confirmedAmount, BigDecimal.ZERO);
        }

        LocalDate forecastStart = today.plusDays(1);
        if (forecastStart.isBefore(startDate)) {
            forecastStart = startDate;
        }

        List<Lecture> lectures = lectureRepository.findAllByTeacher(instructor);

        for (Lecture lecture : lectures) {
            if (!lecture.getIsActive()) continue;
            
            long studentCount = lectureStudentRepository.countByLecture(lecture);
            if (studentCount == 0) continue;

            long classCount = 0;
            for (LocalDate date = forecastStart; !date.isAfter(endDate); date = date.plusDays(1)) {
                if (date.isBefore(lecture.getStartDate()) || date.isAfter(lecture.getEndDate())) continue;

                for (Schedule schedule : lecture.getSchedules()) {
                    if (schedule.getDayOfWeek() == date.getDayOfWeek()) {
                        classCount++;
                        break;
                    }
                }
            }

            BigDecimal lectureExpected = lecture.getDefaultPrice()
                    .multiply(BigDecimal.valueOf(classCount))
                    .multiply(BigDecimal.valueOf(studentCount));
            
            expectedAmount = expectedAmount.add(lectureExpected);
        }

        return buildForecastResponse(instructor, confirmedAmount, expectedAmount);
    }

    private SettlementForecastResponse buildForecastResponse(Member instructor, BigDecimal confirmed, BigDecimal expected) {
        BigDecimal total = confirmed.add(expected);
        
        // 공제 항목 적용
        List<DeductionItem> deductionItems = deductionItemRepository.findAllByAcademy(instructor.getAcademy());
        BigDecimal tax = calculateTax(total, deductionItems);
        BigDecimal real = total.subtract(tax);

        return SettlementForecastResponse.builder()
                .confirmedAmount(confirmed)
                .expectedAmount(expected)
                .totalAmount(total)
                .taxAmount(tax)
                .realAmount(real)
                .build();
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}
