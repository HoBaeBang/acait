package com.aslan.academymanagement.aspect;

import com.aslan.academymanagement.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 성능 모니터링 Aspect
 *
 * ========================================
 * AOP - Around Advice 실습
 * ========================================
 *
 * 이 Aspect는 @Monitored 어노테이션이 붙은 메서드의 성능을 모니터링합니다.
 *
 * Around Advice의 특징:
 * - 메서드 실행 전/후를 모두 제어할 수 있음
 * - ProceedingJoinPoint.proceed()로 원본 메서드 실행
 * - 메서드 실행을 완전히 제어 가능 (실행 전 중단, 결과 변경 등)
 * - 반환값 수정, 예외 처리 등 자유로운 제어 가능
 *
 * AOP의 실무 활용:
 * - 성능 모니터링 (이 클래스)
 * - 트랜잭션 관리 (@Transactional)
 * - 캐싱 (@Cacheable)
 * - 보안 검사 (@PreAuthorize)
 * - 로깅 (LoggingAspect)
 *
 * DI 활용:
 * - NotificationService를 주입받아 성능 저하 시 알림 전송
 * - Aspect에서도 DI를 사용할 수 있음!
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PerformanceAspect {

    // DI - Aspect에서도 다른 Bean을 주입받을 수 있습니다!
    private final NotificationService notificationService;

    /**
     * @Monitored 어노테이션이 붙은 메서드의 성능을 모니터링
     *
     * 3초 이상 소요되면 성능 저하로 간주하고 알림을 전송합니다.
     *
     * @param joinPoint 실행되는 메서드의 정보
     * @return 원본 메서드의 실행 결과
     * @throws Throwable 원본 메서드에서 발생한 예외
     */
    @Around("@annotation(com.aslan.academymanagement.annotation.Monitored)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();

        // 원본 메서드 실행
        Object result = joinPoint.proceed();

        // 실행 시간 측정
        long executionTime = System.currentTimeMillis() - startTime;

        // 성능 저하 감지 (3초 이상)
        if (executionTime > 3000) {
            log.warn("🐌 성능 저하 감지! 메서드: {} ({}ms)", methodName, executionTime);

            // 성능 저하 시 선생님에게 알림 전송
            notificationService.notifyTeacher(
                    String.format("⚠️ 성능 저하: %s 메서드가 %dms 소요됨", methodName, executionTime)
            );
        } else {
            log.debug("⚡ 성능 측정: {} ({}ms)", methodName, executionTime);
        }

        // 원본 메서드의 결과를 그대로 반환
        return result;
    }
}
