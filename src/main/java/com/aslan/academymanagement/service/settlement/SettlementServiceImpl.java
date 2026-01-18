package com.aslan.academymanagement.service.settlement;

import com.aslan.academymanagement.domain.*;
import com.aslan.academymanagement.domain.enums.AttendanceStatus;
import com.aslan.academymanagement.domain.enums.Role;
import com.aslan.academymanagement.domain.enums.SettlementStatus;
import com.aslan.academymanagement.domain.enums.StudentStatus;
import com.aslan.academymanagement.dto.SettlementResponse;
import com.aslan.academymanagement.repository.LectureRecordRepository;
import com.aslan.academymanagement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public void calculateMonthlySettlement(String yearMonth) {
        log.info("💰 {} 월 정산 계산 시작", yearMonth);

        // 1. 날짜 범위 계산 (해당 월의 1일 ~ 말일)
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        // 2. 정산 대상 상태 정의 (출석, 지각, 보강 완료)
        List<AttendanceStatus> targetStatuses = List.of(
                AttendanceStatus.ATTENDED,
                AttendanceStatus.LATE,
                AttendanceStatus.MAKEUP
        );

        // 3. 해당 기간의 모든 유효한 수업 기록 조회
        List<LectureRecord> records = lectureRecordRepository.findSettlementTargets(startDate, endDate, targetStatuses);

        // 4. 강사별로 그룹화
        Map<Member, List<LectureRecord>> recordsByInstructor = records.stream()
                .collect(Collectors.groupingBy(record -> record.getLecture().getTeacher()));

        // 5. 강사별 정산 금액 계산 및 저장
        for (Map.Entry<Member, List<LectureRecord>> entry : recordsByInstructor.entrySet()) {
            Member instructor = entry.getKey();
            List<LectureRecord> instructorRecords = entry.getValue();
            Academy academy = instructor.getAcademy();

            BigDecimal totalAmount = BigDecimal.ZERO;

            for (LectureRecord record : instructorRecords) {
                // 퇴원일 체크: 수업일이 퇴원일 이후라면 정산 제외
                Student student = record.getStudent();
                if (student.getStatus() == StudentStatus.DISCHARGED &&
                        student.getDischargeDate() != null &&
                        record.getDate().isAfter(student.getDischargeDate())) {
                    continue; // 제외
                }

                // 금액 합산 (강의 기본 단가)
                BigDecimal price = record.getLecture().getDefaultPrice();
                if (price != null) {
                    totalAmount = totalAmount.add(price);
                }
            }

            // 6. Settlement 엔티티 생성 또는 업데이트
            Settlement settlement = settlementRepository.findByInstructorAndYearMonth(instructor, yearMonth)
                    .orElse(Settlement.builder()
                            .academy(academy)
                            .instructor(instructor)
                            .yearMonth(yearMonth)
                            .status(SettlementStatus.OPEN)
                            .build());

            // 이미 마감된 정산은 건드리지 않음
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
        // 권한 체크: 원장(OWNER)만 조회 가능
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
    public List<SettlementResponse> getMySettlements(Member instructor) {
        // 본인의 모든 정산 내역 조회 (최신순 정렬 필요하지만 일단 전체)
        // Repository에 findAllByInstructor 추가 필요
        return List.of(); // 임시 반환
    }
}
