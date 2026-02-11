package com.aslan.academymanagement.service.tuition;

import com.aslan.academymanagement.domain.*;
import com.aslan.academymanagement.domain.enums.AttendanceStatus;
import com.aslan.academymanagement.dto.StudentBalanceResponse;
import com.aslan.academymanagement.dto.TuitionPaymentRequest;
import com.aslan.academymanagement.repository.LectureRecordRepository;
import com.aslan.academymanagement.repository.StudentBalanceRepository;
import com.aslan.academymanagement.repository.StudentRepository;
import com.aslan.academymanagement.repository.TuitionPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TuitionServiceImpl implements TuitionService {

    private final TuitionPaymentRepository tuitionPaymentRepository;
    private final StudentBalanceRepository studentBalanceRepository;
    private final StudentRepository studentRepository;
    private final LectureRecordRepository lectureRecordRepository;

    @Override
    public void createPayment(Member manager, Long studentId, TuitionPaymentRequest request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학생이 없습니다."));

        if (!student.getAcademy().getId().equals(manager.getAcademy().getId())) {
            throw new IllegalArgumentException("다른 학원의 학생에게 수납할 수 없습니다.");
        }

        TuitionPayment payment = TuitionPayment.builder()
                .student(student)
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate())
                .yearMonth(request.getYearMonth())
                .memo(request.getMemo())
                .build();

        tuitionPaymentRepository.save(payment);
        
        // 수납 후 잔액 재계산
        calculateBalance(studentId, request.getYearMonth());
    }

    @Override
    @Transactional(readOnly = true)
    public StudentBalanceResponse getBalance(Member manager, Long studentId, String yearMonth) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학생이 없습니다."));

        if (!student.getAcademy().getId().equals(manager.getAcademy().getId())) {
            throw new IllegalArgumentException("다른 학원의 학생 정보를 조회할 수 없습니다.");
        }

        StudentBalance balance = studentBalanceRepository.findByStudentAndYearMonth(student, yearMonth)
                .orElse(StudentBalance.builder()
                        .student(student)
                        .yearMonth(yearMonth)
                        .paidAmount(BigDecimal.ZERO)
                        .usedAmount(BigDecimal.ZERO)
                        .carryOverAmount(BigDecimal.ZERO)
                        .currentBalance(BigDecimal.ZERO)
                        .build());

        return StudentBalanceResponse.from(balance);
    }

    @Override
    public void calculateBalance(Long studentId, String yearMonth) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학생이 없습니다."));

        YearMonth currentYm = YearMonth.parse(yearMonth);
        YearMonth prevYm = currentYm.minusMonths(1);

        // 1. 전월 이월금 조회
        BigDecimal carryOverAmount = BigDecimal.ZERO;
        StudentBalance prevBalance = studentBalanceRepository.findByStudentAndYearMonth(student, prevYm.toString())
                .orElse(null);
        
        if (prevBalance != null) {
            carryOverAmount = prevBalance.getCurrentBalance();
        }

        // 2. 당월 납부액 합계
        List<TuitionPayment> payments = tuitionPaymentRepository.findAllByStudentAndYearMonth(student, yearMonth);
        BigDecimal paidAmount = payments.stream()
                .map(TuitionPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 당월 사용액 계산 (수업료 차감)
        LocalDate startDate = currentYm.atDay(1);
        LocalDate endDate = currentYm.atEndOfMonth();
        
        List<AttendanceStatus> targetStatuses = List.of(
                AttendanceStatus.ATTENDED,
                AttendanceStatus.LATE,
                AttendanceStatus.MAKEUP,
                AttendanceStatus.ABSENT
        );

        List<LectureRecord> records = lectureRecordRepository.findByStudentAndDateBetweenAndStatusIn(
                student, startDate, endDate, targetStatuses
        );

        BigDecimal usedAmount = BigDecimal.ZERO;
        for (LectureRecord record : records) {
            BigDecimal price = record.getLecture().getDefaultPrice();
            if (price != null) {
                usedAmount = usedAmount.add(price);
            }
        }

        // 4. 잔액 저장
        StudentBalance balance = studentBalanceRepository.findByStudentAndYearMonth(student, yearMonth)
                .orElse(StudentBalance.builder()
                        .student(student)
                        .yearMonth(yearMonth)
                        .paidAmount(BigDecimal.ZERO)
                        .usedAmount(BigDecimal.ZERO)
                        .carryOverAmount(BigDecimal.ZERO)
                        .currentBalance(BigDecimal.ZERO)
                        .build());

        balance.updateAmounts(paidAmount, usedAmount, carryOverAmount);
        studentBalanceRepository.save(balance);
        
        log.info("💰 잔액 갱신: 학생={}, 월={}, 이월={}, 납부={}, 사용={}, 잔액={}",
                student.getName(), yearMonth, carryOverAmount, paidAmount, usedAmount, balance.getCurrentBalance());
    }
}
