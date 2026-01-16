# 2026-01-16 트러블 슈팅: CORS, 인증, 그리고 DB 무결성

> **작성자:** Backend Team  
> **관련 이슈:** CORS Error, Login Flow, Data Integrity

---

## 1. CORS & Preflight Request 이슈

### 🚨 문제 상황
프론트엔드에서 API를 호출할 때마다 다음과 같은 CORS 에러가 발생함.
```
Access to XMLHttpRequest at '...' from origin '...' has been blocked by CORS policy: 
Response to preflight request doesn't pass access control check: ...
```

### 🔍 원인 분석
1.  브라우저는 실제 요청 전에 `OPTIONS` 메서드로 **Preflight Request**를 보냄.
2.  `SecurityConfig`에서 모든 API 요청(`/api/v1/**`)에 대해 `.authenticated()`(인증 필요)를 걸어둠.
3.  Preflight 요청은 토큰(Authorization 헤더) 없이 보내지므로, Spring Security가 **403 Forbidden**으로 막아버림.
4.  브라우저는 이를 CORS 거부로 인식함.

### 🛠️ 해결 방법
`SecurityConfig`에서 `OPTIONS` 메서드에 대한 요청은 인증 없이 통과시키도록 설정함.

```java
.authorizeHttpRequests(auth -> auth
    // Preflight Request (OPTIONS) 허용
    .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
    // ...
)
```

---

## 2. 회원가입 후 로그인 불가 (UX 이슈)

### 🚨 문제 상황
원장(OWNER)이 회원가입을 완료(`POST /signup`)해도 로그인이 되지 않음.
사용자가 다시 "구글 로그인" 버튼을 눌러야만 토큰을 발급받을 수 있어 불편함.

### 🔍 원인 분석
1.  `/signup` API는 단순히 DB에 회원을 저장하고 "가입 완료" 메시지만 반환함.
2.  JWT 토큰을 발급해주는 로직이 없었음.

### 🛠️ 해결 방법
`AuthController`에서 가입 성공 시(특히 원장인 경우), 즉시 JWT 토큰을 생성하여 응답 바디에 포함시킴.

```java
// AuthController.java
if (status == MemberStatus.ACTIVE) {
    String token = jwtTokenProvider.createToken(authentication);
    return ResponseEntity.ok(new SignupResponse("가입 완료", token));
}
```

---

## 3. 슈퍼 계정 생성 시 DB 에러

### 🚨 문제 상황
슈퍼 관리자(`aslanhobae@gmail.com`)로 로그인 시 `DataIntegrityViolationException` 발생.
`NULL not allowed for column "ACADEMY_ID"`

### 🔍 원인 분석
1.  멀티 테넌트 적용으로 `Member` 엔티티에 `academy_id`가 필수(`nullable = false`)가 됨.
2.  `CustomOAuth2UserService`의 슈퍼 계정 생성 로직에서 `academy` 정보를 넣어주지 않음.

### 🛠️ 해결 방법
슈퍼 계정 생성 시, 기본 학원("ACAIT 본사")을 조회하거나 없으면 생성하여 주입하도록 로직 수정.

```java
private Member createSuperAdmin(...) {
    Academy academy = academyRepository.findAll().stream().findFirst()
            .orElseGet(() -> academyRepository.save(new Academy("ACAIT 본사")));
    
    return Member.builder().academy(academy)...build();
}
```

---

## 4. Role 명칭 불일치

### 🚨 문제 상황
프론트엔드는 `ROLE_OWNER`를 보내는데, 백엔드는 `ROLE_ADMIN`을 기대하여 **400 Bad Request** 발생.

### 🔍 원인 분석
기획서 버전 차이로 인한 커뮤니케이션 미스.
*   백엔드: 기획서 v3.1 기준 (`ROLE_ADMIN`)
*   프론트엔드: 기획서 v5.0 기준 (`ROLE_OWNER`)

### 🛠️ 해결 방법
최신 기획서(v5.0)를 기준으로 백엔드의 `Role` Enum과 관련 로직을 모두 `ROLE_OWNER`로 수정함.
