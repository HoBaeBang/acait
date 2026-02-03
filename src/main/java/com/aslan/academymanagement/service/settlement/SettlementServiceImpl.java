package com.aslan.academymanagement.service.settlement;

import com.aslan.academymanagement.domain.*;
import com.aslan.academymanagement.domain.enums.AttendanceStatus;
import com.aslan.academymanagement.domain.enums.Role;
import com.aslan.academymanagement.domain.enums.SettlementStatus;
import com.aslan.academymanagement.domain.enums.StudentStatus;
import com.aslan.academymanagement.dto.SettlementDetailResponse;
import com.aslan.academymanagement.dto.SettlementForecastResponse;
import com.aslan.academymanagement.dto.SettlementResponse;
import com.aslan.academymanagement.repository.LectureRecordRepository;
import com.aslan.academymanagement.repository.LectureRepository;
import com.aslan.academymanagement.repository.LectureStudentRepository;
import com.aslan.academymanagement.repository.SettlementRepository;
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
                AttendanceStatus.MAKEUP
        );

        // 1. 정산 대상 수업 기록 조회
        List<LectureRecord> records;
        if (targetInstructor != null) {
            // 특정 강사의 기록만 조회 (Repository 메서드 추가 필요하지만, 일단 전체 조회 후 필터링하거나 쿼리 추가)
            // 성능을 위해 쿼리 추가가 좋지만, 여기서는 기존 메서드 활용 후 필터링
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

            settlement.updateTotalAmount(totalAmount);
            settlementRepository.save(settlement);

            log.info("✅ 정산 완료: 강사={}, 금액={}", instructor.getName(), totalAmount);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> getMonthlySettlements(Member admin, String yearMonth) {
        if (admin.getRole() != Role.ROLE_OWNER) {
            throw new IllegalArgumentException("정산 현황 조회 권한이 없습니다.");
        }

        Academy academy = admin.getAcademy();
        return settlementRepository.findAllByAcademyAndYearMonth(academy, yearMonth).stream()
                .map(SettlementResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> getMySettlements(Member instructor, String yearMonth) {
        return settlementRepository.findAllByInstructorAndYearMonth(instructor, yearMonth).stream()
                .map(SettlementResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportSettlementToExcel(Member admin, String yearMonth) {
        List<SettlementResponse> settlements = getMonthlySettlements(admin, yearMonth);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("정산 내역");

            Row headerRow = sheet.createRow(0);
            String[] columns = {"강사명", "정산월", "세전 총액", "세금(3.3%)", "실지급액", "상태"};
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
                AttendanceStatus.MAKEUP
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
            return buildForecastResponse(confirmedAmount, BigDecimal.ZERO);
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

        return buildForecastResponse(confirmedAmount, expectedAmount);
    }

    private SettlementForecastResponse buildForecastResponse(BigDecimal confirmed, BigDecimal expected) {
        BigDecimal total = confirmed.add(expected);
        
        // 세금 계산 (3.3%)
        BigDecimal taxRate = new BigDecimal("0.033");
        BigDecimal tax = total.multiply(taxRate).setScale(0, java.math.RoundingMode.FLOOR);
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
