# 세션(Session)에서 JWT(Json Web Token)로의 전환기

> **작성일:** 2024.01.06  
> **프로젝트:** Academy Management  
> **주제:** API 서버로의 진화를 위한 인증 체계 개편

---

## 1. 왜 JWT로 전환했는가?

초기에는 **Thymeleaf(SSR)** 기반의 웹 애플리케이션이었기 때문에 **세션(Session)** 방식이 가장 적합했습니다. 하지만 프로젝트의 확장성을 고려하여 다음과 같은 이유로 **JWT**로 전환을 결정했습니다.

1.  **프론트엔드 분리:** 향후 React, Vue, 모바일 앱(iOS/Android) 등 다양한 클라이언트와 통신하기 위해서는 **Stateless(무상태)**한 API 서버가 되어야 합니다.
2.  **확장성 (Scalability):** 세션은 서버 메모리에 저장되므로 서버를 여러 대(Scale-out)로 늘릴 때 세션 동기화 문제(Redis 필요 등)가 발생하지만, JWT는 토큰 자체에 정보가 있어 서버 확장이 자유롭습니다.

---

## 2. JWT(Json Web Token)란 무엇인가?

**JWT**는 유저를 인증하고 식별하기 위한 **토큰(Token) 기반 인증 방식**의 표준(RFC 7519)입니다. 쉽게 말해, **"사용자의 신분증을 암호화해서 문자열로 만든 것"**입니다.

### 2.1. JWT의 생김새
JWT는 `.`(점)을 구분자로 하여 세 부분으로 나뉩니다.
> **aaaaaa.bbbbbb.cccccc**

### 2.2. 상세 구조

#### ① Header (헤더)
토큰의 타입(JWT)과 해싱 알고리즘(HS512 등) 정보가 들어갑니다.
```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```

#### ② Payload (페이로드)
실제 **데이터(Claim)**가 담기는 곳입니다. 사용자의 ID, 권한, 만료 시간 등을 담습니다.
*   **주의:** 이곳은 암호화되지 않고 단순히 인코딩(Base64)만 되므로, **비밀번호 같은 민감한 정보는 절대 넣으면 안 됩니다.**
```json
{
  "sub": "user@gmail.com",  // 사용자 식별자 (Subject)
  "auth": "ROLE_USER",      // 권한
  "iat": 1704546000,        // 토큰 발급 시간 (Issued At)
  "exp": 1704549600         // 토큰 만료 시간 (Expiration Time)
}
```

#### ③ Signature (서명)
**JWT의 핵심 보안 장치**입니다.
Header와 Payload를 합친 뒤, 서버만 알고 있는 **비밀키(Secret Key)**로 암호화(Hashing)한 값입니다.
*   누군가 Payload의 데이터를 조작(예: 권한을 ADMIN으로 변경)하더라도, 비밀키를 모르면 올바른 서명을 만들어낼 수 없으므로 서버는 이를 즉시 알아채고 거부합니다.

---

## 3. 구현 아키텍처 및 핵심 컴포넌트

우리는 `jjwt` 라이브러리를 사용하여 다음과 같은 구조를 구축했습니다.

### 3.1. `JwtTokenProvider` (토큰 공장)
JWT를 생성하고, 검증하고, 파싱하는 핵심 유틸리티 클래스입니다.

*   **`createToken()`:** 사용자 인증 정보(`Authentication`)를 받아 암호화된 토큰 문자열을 생성합니다.
*   **`getAuthentication()`:** 토큰을 복호화하여 사용자 정보(ID, 권한)를 꺼냅니다.
*   **`validateToken()`:** 토큰이 위조되었거나 만료되지 않았는지 검사합니다.

### 3.2. `JwtAuthenticationFilter` (출입국 심사대)
모든 API 요청의 앞단에서 동작하는 필터입니다.

*   **역할:** HTTP 헤더(`Authorization: Bearer ...`)를 검사합니다.
*   **로직:**
    1.  헤더에서 토큰 추출
    2.  `validateToken()`으로 유효성 검사
    3.  유효하면 `SecurityContextHolder`에 인증 정보 저장 (통과)
    4.  유효하지 않으면 그냥 통과 (뒤쪽의 시큐리티가 알아서 막음)

### 3.3. `OAuth2SuccessHandler` (토큰 발급처)
OAuth2 로그인 성공 직후 실행되는 핸들러입니다.

*   **기존(세션):** 그냥 메인 페이지(`/`)로 리다이렉트하면 끝 (세션 자동 생성됨).
*   **변경(JWT):**
    1.  `JwtTokenProvider`를 이용해 **Access Token** 생성.
    2.  생성된 토큰을 클라이언트에게 전달 (현재는 `/login-success?token=...`로 리다이렉트).

### 3.4. `SecurityConfig` (보안 정책)
가장 중요한 설정 변경이 일어난 곳입니다.

```java
http
    // 1. 세션 끄기 (가장 중요!)
    .sessionManagement(session -> session
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    )
    // 2. JWT 필터 끼워 넣기
    .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), 
                     UsernamePasswordAuthenticationFilter.class)
```

---

## 4. 동작 흐름 (Authentication Flow)

### 4.1. 로그인 과정
1.  **User:** `GET /oauth2/authorization/google` 요청
2.  **Google:** 로그인 및 정보 제공 동의
3.  **Server:** `CustomOAuth2UserService`가 DB에 회원 정보 저장/업데이트
4.  **Server:** `OAuth2SuccessHandler` 실행 -> **JWT 생성**
5.  **Server:** `http://localhost:8080/login-success?token=eyJhbG...` 로 리다이렉트
6.  **User:** URL에 있는 토큰을 복사해서 저장 (Local Storage 등)

### 4.2. API 요청 과정
1.  **User:** `GET /api/v1/lectures` 요청 시 헤더에 토큰 포함
    *   `Authorization: Bearer eyJhbG...`
2.  **Server:** `JwtAuthenticationFilter`가 토큰 검증
3.  **Server:** 검증 성공 시 `SecurityContext`에 인증 정보 세팅
4.  **Server:** `LectureController` 실행 및 JSON 응답

---

## 5. 트러블슈팅 (Trouble Shooting)

### 🚨 `WeakKeyException` (HS512 키 길이 문제)
*   **증상:** 서버 시작 시 또는 토큰 생성 시 에러 발생.
    *   `The signing key's size is 448 bits which is not secure enough for the HS512 algorithm.`
*   **원인:** `application.yml`에 설정한 `jwt.secret` 키 값이 너무 짧아서 발생. HS512 알고리즘은 최소 512비트(64바이트) 이상의 키를 요구함.
*   **해결:** 비밀키 문자열을 아주 길게(100자 이상) 늘려서 해결.

---

## 6. 결론
이제 우리 서버는 **완전한 Stateless API 서버**가 되었습니다.
더 이상 HTML을 렌더링하지 않으며, 오직 JSON 데이터만 주고받습니다.
이로써 React, Vue, iOS, Android 등 어떤 클라이언트와도 자유롭게 통신할 수 있는 기반이 마련되었습니다.
