# CORS (Cross-Origin Resource Sharing) 완벽 가이드

> **작성일:** 2024.01.06  
> **프로젝트:** Academy Management  
> **주제:** 프론트엔드(React)와 백엔드(Spring Boot) 연동 시 발생하는 CORS 문제와 Preflight Request

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

## 2. Preflight Request (예비 요청)란?

CORS 에러를 이해하려면 **Preflight Request**를 반드시 알아야 합니다.

### 2.1 동작 원리
브라우저는 보안을 위해 요청을 두 번 보냅니다.

1.  **예비 요청 (Preflight Request):**
    *   **HTTP Method:** `OPTIONS`
    *   **내용:** "나 지금 `POST`로 요청 보낼 건데, 헤더에 `Authorization`도 있어. 이거 받아줄 거야?"
    *   **목적:** 서버가 CORS를 허용하는지 간보기 위함.

2.  **본 요청 (Actual Request):**
    *   **HTTP Method:** `GET`, `POST`, `PUT` 등
    *   **내용:** 실제 데이터 요청
    *   **조건:** 예비 요청에서 서버가 "OK, 허용할게(200 OK)"라고 응답했을 때만 전송됨.

### 2.2 왜 OPTIONS 메서드를 쓰는가?
`OPTIONS`는 리소스가 지원하는 메서드 종류를 물어보는 HTTP 표준 메서드입니다. 데이터에 영향을 주지 않으면서 서버의 설정을 확인할 수 있어 Preflight 용도로 사용됩니다.

---

## 3. 발생했던 문제 상황

### 3.1 에러 메시지
```
Access to XMLHttpRequest at '...' from origin '...' has been blocked by CORS policy: 
Response to preflight request doesn't pass access control check: 
No 'Access-Control-Allow-Origin' header is present on the requested resource.
```

### 3.2 원인 분석
우리는 API 보안을 강화하기 위해 `SecurityConfig`에서 모든 API 요청(`/api/v1/**`)에 대해 인증(`authenticated()`)을 요구했습니다.

1.  브라우저가 `OPTIONS /api/v1/lecture/events` (Preflight) 요청을 보냄.
2.  이 요청에는 **토큰(Authorization 헤더)이 없음** (브라우저가 자동으로 보내는 거라 토큰을 안 넣음).
3.  Spring Security가 "어? 인증 안 된 요청이네? **403 Forbidden!**" 하고 막아버림.
4.  브라우저는 "서버가 허용 안 하네"라고 판단하고 CORS 에러 발생.

---

## 4. 해결 방법

### 4.1 SecurityConfig 수정 (Best Practice)

Spring Security에게 **"OPTIONS 메서드로 오는 요청(Preflight)은 인증 검사하지 말고 그냥 통과시켜줘"**라고 설정해야 합니다.

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            // ✅ Preflight Request (OPTIONS)는 무조건 허용!
            .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
            
            // 나머지 API는 인증 필요
            .requestMatchers("/api/v1/**").authenticated()
            .anyRequest().authenticated()
        )
        // ...
}
```

### 4.2 CorsConfigurationSource 설정 (필수)

Preflight 요청이 통과되더라도, 서버가 응답 헤더에 "허용한다"는 내용을 담아줘야 합니다.

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // 1. 허용할 프론트엔드 주소 (와일드카드 * 대신 명시적으로 적는 것이 보안상 좋음)
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
    
    // 2. 허용할 HTTP 메서드
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    
    // 3. 허용할 헤더 (Authorization 등)
    configuration.setAllowedHeaders(Arrays.asList("*"));
    
    // 4. 자격 증명 허용 (쿠키, Authorization 헤더 등)
    configuration.setAllowCredentials(true);
    
    // 5. Preflight 응답 캐시 시간 (1시간 동안은 다시 안 물어봄)
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

---

## 5. 결론

1.  **CORS 에러**는 브라우저가 우리를 보호하기 위해 내는 에러다.
2.  브라우저는 실제 요청 전에 **`OPTIONS` 메서드**로 간을 본다 (**Preflight**).
3.  Spring Security를 쓸 때는 **`OPTIONS` 요청을 `permitAll()`로 열어줘야** CORS 설정이 먹힌다.
4.  `WebMvcConfig`보다는 **`SecurityConfig`에서 CORS를 통합 관리**하는 것이 좋다. (Security 필터가 더 앞단에 있기 때문)
