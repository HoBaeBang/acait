# CORS (Cross-Origin Resource Sharing) 이슈 해결

> **작성일:** 2024.01.06  
> **프로젝트:** Academy Management  
> **주제:** 프론트엔드(React)와 백엔드(Spring Boot) 연동 시 발생하는 CORS 문제 해결

---

## 1. CORS란 무엇인가?

**CORS (Cross-Origin Resource Sharing)**는 웹 브라우저가 보안을 위해 실행하는 정책입니다.
기본적으로 브라우저는 **SOP (Same-Origin Policy)** 정책을 따르는데, 이는 "같은 출처(Origin)에서만 리소스를 공유할 수 있다"는 원칙입니다.

### 1.1 Origin(출처)이란?
URL의 `Protocol`, `Host`, `Port` 세 가지가 모두 같아야 같은 출처입니다.

*   **백엔드:** `http://localhost:8080`
*   **프론트엔드:** `http://localhost:5173`

위 두 주소는 **포트 번호(8080 vs 5173)**가 다르므로 **다른 출처(Cross-Origin)**입니다.
따라서 브라우저는 프론트엔드에서 백엔드로 요청을 보낼 때 보안상 이유로 차단합니다.

---

## 2. 해결 방법

백엔드 서버에서 "이 출처(`http://localhost:5173`)는 안전하니까 허용해줘"라고 브라우저에게 알려줘야 합니다.
Spring Boot에서는 두 가지 계층에서 설정을 해줘야 완벽하게 동작합니다.

### 2.1 WebMvcConfig 설정 (Spring MVC 계층)
일반적인 Controller 요청에 대한 CORS를 허용합니다.

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

### 2.2 SecurityConfig 설정 (Spring Security 계층)
Spring Security 필터 체인에서 발생하는 요청(로그인, 인증 등)에 대한 CORS를 허용합니다.
**Security가 MVC보다 앞단에 있기 때문에, 여기서 막히면 MVC 설정은 소용이 없습니다.**

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 적용
        // ... 기타 설정
}

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

---

## 3. 주의사항

1.  **`allowCredentials(true)`:** 쿠키나 인증 헤더를 포함한 요청을 허용하려면 필수입니다. 이때 `allowedOrigins`에 와일드카드(`*`)를 사용할 수 없고, 구체적인 주소를 명시해야 합니다.
2.  **Preflight Request:** 브라우저는 실제 요청(POST, PUT 등)을 보내기 전에 `OPTIONS` 메서드로 "보내도 되니?"라고 먼저 물어봅니다. 이를 Preflight라고 하며, 서버는 이에 대해 200 OK 응답을 줘야 합니다. 위 설정들이 이를 자동으로 처리해줍니다.

---

## 4. 결론

프론트엔드 개발자가 "CORS 오류나요!"라고 하면, **"내 서버가 너를 신뢰하지 않아서 문을 안 열어주는구나"**라고 이해하면 됩니다.
위와 같이 설정을 추가하여 프론트엔드 주소(`localhost:5173`)를 신뢰할 수 있는 출처로 등록해주었습니다.
