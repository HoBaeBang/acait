package com.aslan.academymanagement.aspect;

import com.aslan.academymanagement.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * 출석 체크 Aspect
 *
 * ========================================
 * AOP - Before/AfterReturning Advice 실습
 * ========================================
 *
 * 이 Aspect는 @AttendanceRequired 어노테이션이 붙은 메서드에서
 * 출석 체크 전/후에 부가 기능을 수행합니다.
 *
 * @Before vs @Around:
 * - @Before: 메서드 실행 전에만 동작, proceed() 불필요
 * - 원본 메서드의 실행을 막을 수 없음
 * - 간단한 전처리에 적합 (로깅, 검증 등)
 *
 * @AfterReturning:
 * - 메서드가 정상적으로 종료된 후에 동작
 * - 예외가 발생하면 실행되지 않음
 * - 후처리 작업에 적합
 *
 * Pointcut 표현식:
 * - @annotation(): 특정 어노테이션이 붙은 메서드
 * - execution(): 메서드 시그니처 패턴 매칭
 * - within(): 특정 패키지/클래스 내의 모든 메서드
 * - bean(): 특정 Bean의 모든 메서드
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AttendanceCheckAspect {

    private final NotificationService notificationService;

    /**
     * 출석 체크 메서드 실행 전에 지각 여부를 확인
     *
     * @Before Advice 특징:
     * - 메서드 실행 전에 자동으로 실행
     * - JoinPoint로 메서드의 인자에 접근 가능
     * - 원본 메서드의 실행을 막을 수 없음 (proceed() 없음)
     *
     * @param joinPoint 실행되는 메서드의 정보
     */
    @Before("@annotation(com.aslan.academymanagement.annotation.AttendanceRequired)")
    public void beforeAttendanceCheck(JoinPoint joinPoint) {
        LocalTime now = LocalTime.now();
        // JoinPoint.getArgs()로 메서드의 파라미터에 접근
        // 첫 번째 인자가 studentId입니다.
        String studentId = (String) joinPoint.getArgs()[0];

        log.info("⏰ 출석 체크 시작: {} at {}", studentId, now);

        // 9시 이후 출석은 지각으로 처리
        if (now.isAfter(LocalTime.of(9, 0))) {
            log.warn("⏰ 지각 감지: {} ({})", studentId, now);
            notificationService.notifyTeacher(
                    String.format("⚠️ %s 학생이 %s에 출석했습니다 (지각)", studentId, now)
            );
        }
    }

    /**
     * 출석 체크 메서드가 정상적으로 완료된 후 실행
     *
     * @AfterReturning Advice 특징:
     * - 메서드가 정상적으로 종료된 후에만 실행
     * - 예외가 발생하면 실행되지 않음
     * - returning 속성으로 반환값에 접근 가능 (여기서는 사용하지 않음)
     *
     * @param joinPoint 실행된 메서드의 정보
     */
    @AfterReturning("@annotation(com.aslan.academymanagement.annotation.AttendanceRequired)")
    public void afterAttendanceCheck(JoinPoint joinPoint) {
        String studentId = (String) joinPoint.getArgs()[0];
        log.info("✅ 출석 체크 완료: {}", studentId);
    }
}
