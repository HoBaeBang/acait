package com.aslan.academymanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(OpenAPI) 설정 클래스
 *
 * Spring의 @Configuration 어노테이션을 통해 설정 클래스임을 명시하고,
 * API 문서화를 위한 OpenAPI 설정을 제공합니다.
 *
 * 접근 URL: http://localhost:8080/swagger-ui/index.html
 */
@Configuration
public class SwaggerConfig {

    /**
     * OpenAPI 3.0 설정을 위한 Bean 생성
     *
     * Spring IoC 컨테이너가 이 메서드를 호출하여 OpenAPI 객체를 생성하고,
     * 애플리케이션 컨텍스트에 등록합니다.
     *
     * @return OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("학원 학생 관리 시스템 API")
                        .description("Spring IoC/DI/AOP 학습 프로젝트 - 초등부/중등부 학생 관리 시스템")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Academy Management Team")
                                .email("admin@academy.com")));
    }
}
