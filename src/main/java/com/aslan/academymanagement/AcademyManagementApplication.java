package com.aslan.academymanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 학원 학생 관리 시스템 메인 애플리케이션 클래스
 *
 * ========================================
 * Spring IoC (Inversion of Control) 설명
 * ========================================
 * IoC는 "제어의 역전"을 의미합니다.
 * 전통적인 프로그래밍에서는 개발자가 직접 객체를 생성하고 관리했지만,
 * Spring에서는 IoC 컨테이너가 객체의 생명주기를 관리합니다.
 *
 * @SpringBootApplication 어노테이션은 다음을 포함합니다:
 * 1. @Configuration: 이 클래스가 설정 클래스임을 명시
 * 2. @EnableAutoConfiguration: Spring Boot의 자동 설정 활성화
 * 3. @ComponentScan: 패키지 내의 @Component, @Service, @Controller 등을 자동으로 스캔하여 Bean으로 등록
 *
 * Spring IoC 컨테이너의 역할:
 * - Bean(객체) 생성 및 관리
 * - Bean 간의 의존성 주입 (Dependency Injection)
 * - Bean의 생명주기 관리 (생성 → 초기화 → 사용 → 소멸)
 *
 * IoC의 장점:
 * - 객체 간의 결합도 감소
 * - 코드의 재사용성 향상
 * - 테스트 용이성 증대
 * - 코드 유지보수 편의성
 */
@SpringBootApplication
@EnableJpaAuditing
public class AcademyManagementApplication {

    public static void main(String[] args) {
        // SpringApplication.run()은 Spring IoC 컨테이너를 시작하고
        // 모든 Bean을 생성하여 애플리케이션 컨텍스트에 등록합니다.
        SpringApplication.run(AcademyManagementApplication.class, args);
    }

}
