# OAuth2 로그인 및 회원가입 프로세스 변경

> **작성일:** 2024.01.06  
> **프로젝트:** Academy Management  
> **주제:** 자동 회원가입 제거 및 승인 대기 프로세스 구현

---

## 1. 변경 배경

기존 시스템은 구글 로그인 시 자동으로 DB에 회원을 생성(`saveOrUpdate`)하고 바로 JWT를 발급했습니다.
하지만 새로운 요구사항은 다음과 같습니다:

1.  **추가 정보 입력:** 전화번호, 연락용 이메일 등 구글에서 주지 않는 정보를 받아야 함.
2.  **승인 절차:** 가입 신청 후 원장(Admin)의 승인이 있어야 서비스 이용 가능.

따라서 **"로그인 성공 핸들러(`OAuth2SuccessHandler`)"**에서 회원의 상태를 체크하고 적절한 페이지로 보내주는 로직이 필요해졌습니다.

---

## 2. 변경된 프로세스 흐름

### 2.1 전체 흐름도

```mermaid
graph TD
    A[구글 로그인 시도] --> B{DB에 회원 정보가 있는가?}
    B -- No (미가입) --> C[회원가입 페이지로 리다이렉트]
    C --> D[추가 정보 입력 후 가입 신청]
    D --> E[상태: PENDING]
    
    B -- Yes (가입됨) --> F{회원 상태(Status) 확인}
    F -- PENDING --> G[승인 대기 페이지로 리다이렉트]
    F -- REJECTED --> H[거절 안내 페이지로 리다이렉트]
    F -- ACTIVE --> I[JWT 발급 및 로그인 성공]
```

### 2.2 상세 구현 로직 (`OAuth2SuccessHandler`)

```java
@Override
public void onAuthenticationSuccess(...) {
    // 1. 구글 이메일로 DB 조회
    Optional<Member> memberOptional = memberRepository.findByGoogleEmail(email);

    // Case 1: 미가입 회원
    if (memberOptional.isEmpty()) {
        // 프론트엔드 회원가입 페이지(/register)로 이동
        // 편의를 위해 이메일과 이름을 쿼리 파라미터로 전달
        redirect("/register?googleEmail=...&name=...");
        return;
    }

    Member member = memberOptional.get();

    // Case 2: 승인 대기 중
    if (member.getStatus() == MemberStatus.PENDING) {
        redirect("/pending");
        return;
    }

    // Case 3: 정상 회원 (ACTIVE)
    // JWT 토큰 생성 후 로그인 성공 페이지(/login-success)로 이동
    String token = jwtTokenProvider.createToken(authentication);
    redirect("/login-success?token=" + token);
}
```

---

## 3. 프론트엔드 연동 가이드

프론트엔드 개발자는 다음 4가지 라우트를 처리해야 합니다.

1.  `/register`: 회원가입 폼 페이지. URL 파라미터로 넘어온 `googleEmail`, `name`을 input에 미리 채워주면 좋습니다.
2.  `/pending`: "원장님 승인 대기 중입니다" 안내 페이지.
3.  `/rejected`: "가입 승인이 거절되었습니다" 안내 페이지.
4.  `/login-success`: 기존과 동일. URL 파라미터의 `token`을 저장하고 메인으로 이동.

---

## 4. 결론

이제 우리 시스템은 **"선 가입 신청, 후 승인"**이라는 보안 절차를 갖추게 되었습니다.
아무나 들어와서 데이터를 볼 수 없으며, 원장님이 승인한 강사만 시스템에 접근할 수 있습니다.
