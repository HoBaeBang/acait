# API 보안 강화 (SecurityConfig 설정)

> **작성일:** 2024.01.06  
> **프로젝트:** Academy Management  
> **주제:** 인증되지 않은 사용자의 API 접근 차단

---

## 1. 왜 보안 설정이 필요한가?

기존 설정에서는 개발 편의를 위해 `/api/v1/**` 경로를 `.permitAll()`로 열어두었습니다.
이는 **로그인하지 않은 사용자(해커 포함)도 API를 호출하여 데이터를 조작하거나 조회할 수 있음**을 의미합니다.

특히 `LectureController`에서 `@AuthenticationPrincipal`을 사용하게 되면서, 인증되지 않은 요청이 들어오면 `userDetails`가 `null`이 되어 `NullPointerException`이 발생할 위험도 생겼습니다.

---

## 2. 변경 내용

### 2.1 SecurityConfig 수정

```java
.authorizeHttpRequests(auth -> auth
    // 1. 정적 리소스 및 로그인 페이지: 누구나 접근 가능
    .requestMatchers("/", "/css/**", "/images/**", "/js/**", "/h2-console/**", "/profile", "/login-success").permitAll()
    
    // 2. Swagger UI: 문서 확인을 위해 허용
    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

    // 3. API 요청: 인증된 사용자만 접근 가능 (핵심!)
    .requestMatchers("/api/v1/**").authenticated()
    
    // 4. 나머지 모든 요청: 인증 필요
    .anyRequest().authenticated()
)
```

### 2.2 동작 방식 변화

| 요청 | 기존 (permitAll) | 변경 후 (authenticated) |
| :--- | :--- | :--- |
| `GET /api/v1/lecture` (토큰 없음) | 200 OK (또는 500 에러) | **403 Forbidden** |
| `GET /api/v1/lecture` (유효 토큰) | 200 OK | 200 OK |
| `GET /swagger-ui/index.html` | 200 OK | 200 OK |

---

## 3. 학습 포인트

### 3.1 `authenticated()` vs `permitAll()`
*   `authenticated()`: 로그인한 사용자(SecurityContext에 인증 정보가 있는 경우)만 통과시킵니다.
*   `permitAll()`: 로그인 여부와 상관없이 무조건 통과시킵니다.

### 3.2 Swagger UI 예외 처리
API 문서를 보는 Swagger UI(`/swagger-ui/**`, `/v3/api-docs/**`)는 개발자가 편하게 봐야 하므로 `permitAll()`로 열어두는 것이 일반적입니다. (운영 환경에서는 막기도 합니다.)

### 3.3 403 Forbidden 에러
이제 프론트엔드에서 토큰 없이 API를 호출하면 **403 Forbidden** 에러를 받게 됩니다.
이는 "너 누군지 모르니까(또는 권한 없으니까) 안 돼!"라는 뜻입니다. 프론트엔드는 이 에러를 받으면 로그인 페이지로 리다이렉트 시키는 등의 처리를 해야 합니다.

---

## 4. 결론

이제 우리 서버는 **"문단속"**이 되었습니다.
열쇠(JWT)를 가진 사람만 API라는 방에 들어올 수 있습니다.
이로써 데이터 무결성을 지키고, 애플리케이션의 안정성을 높였습니다.
