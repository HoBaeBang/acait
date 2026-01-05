package com.aslan.academymanagement.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 로깅 Aspect
 *
 * ========================================
 * Spring AOP (Aspect-Oriented Programming) 설명
 * ========================================
 * AOP는 "관점 지향 프로그래밍"으로, 횡단 관심사(Cross-Cutting Concerns)를
 * 모듈화하여 코드 중복을 제거하고 비즈니스 로직에 집중할 수 있게 합니다.
 *
 * 횡단 관심사란?
 * - 로깅, 트랜잭션, 보안, 성능 모니터링 등
 * - 여러 클래스/메서드에서 공통으로 필요한 기능
 * - 비즈니스 로직과는 별개로 필요한 부가 기능
 *
 * AOP 주요 용어:
 * 1. Aspect: 횡단 관심사를 모듈화한 것 (이 클래스)
 * 2. Join Point: Aspect가 적용될 수 있는 지점 (메서드 실행, 예외 발생 등)
 * 3. Pointcut: 어떤 Join Point에 Aspect를 적용할지 정의 (@Around의 표현식)
 * 4. Advice: 실제로 실행되는 코드 (logExecutionTime 메서드)
 * 5. Weaving: Aspect를 실제 코드에 적용하는 과정
 *
 * Advice 종류:
 * - @Before: 메서드 실행 전
 * - @After: 메서드 실행 후 (성공/실패 무관)
 * - @AfterReturning: 메서드 정상 종료 후
 * - @AfterThrowing: 예외 발생 후
 * - @Around: 메서드 실행 전/후 모두 제어 (가장 강력) ✅
 *
 * @Aspect: 이 클래스가 Aspect임을 Spring에게 알림
 * @Component: Spring Bean으로 등록
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /**
     * @Loggable 어노테이션이 붙은 메서드의 실행 시간을 로깅하는 Around Advice
     *
     * Pointcut 표현식:
     * - @annotation(com.aslan.academymanagement.annotation.Loggable)
     * - @Loggable 어노테이션이 붙은 모든 메서드가 대상
     *
     * @param joinPoint 실행되는 메서드의 정보를 담고 있는 객체
     * @return 원본 메서드의 실행 결과
     * @throws Throwable 원본 메서드에서 발생한 예외
     */
    @Around("@annotation(com.aslan.academymanagement.annotation.Loggable)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        // JoinPoint에서 실행되는 메서드의 정보 추출
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("📝 [{}] {} 실행 시작", className, methodName);
        long startTime = System.currentTimeMillis();

        Object result = null;
        try {
            // 원본 메서드 실행 - 이 부분이 실제 비즈니스 로직!
            // proceed()를 호출하지 않으면 원본 메서드가 실행되지 않습니다.
            result = joinPoint.proceed();

            // 실행 시간 계산 및 로깅
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("✅ [{}] {} 실행 완료 ({}ms)", className, methodName, executionTime);

            return result;

        } catch (Exception e) {
            // 예외 발생 시 로깅
            log.error("❌ [{}] {} 실행 실패: {}", className, methodName, e.getMessage());
            throw e; // 예외를 다시 던져서 호출자가 처리할 수 있게 함
        }
    }
}
