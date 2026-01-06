# OAuth 2.0 완벽 가이드 (with Spring Security)

> **작성일:** 2024.01.06  
> **프로젝트:** Academy Management  
> **작성자:** 시니어 개발자 (AI Assistant)

---

## 1. OAuth 2.0이란?

**OAuth (Open Authorization)**는 인터넷 사용자들이 비밀번호를 제공하지 않고, 다른 웹사이트 상의 자신들의 정보에 대해 웹사이트나 애플리케이션의 접근 권한을 부여할 수 있는 공통적인 수단으로서 사용되는 개방형 표준입니다.

### 💡 쉬운 비유: "호텔 카드키"
- **상황:** 당신(사용자)이 호텔(구글)에 투숙합니다.
- **문제:** 청소부(우리 앱)가 당신 방을 청소해야 하는데, 당신의 **마스터키(비밀번호)**를 줄 수는 없습니다.
- **해결:** 프론트 데스크(인증 서버)에서 청소부에게 **"청소 시간 동안만 문을 열 수 있는 임시 카드키(Access Token)"**를 발급해줍니다.
- **결과:** 청소부는 마스터키 없이도 방에 들어갈 수 있고, 당신의 비밀번호는 안전합니다.

---

## 2. 핵심 용어 정리

| 용어 | 설명 | 우리 프로젝트 예시 |
| :--- | :--- | :--- |
| **Resource Owner** | 정보의 주인 (사용자) | 학원 관리 시스템에 가입하려는 사람 |
| **Client** | 정보를 사용하려는 애플리케이션 | Academy Management (Spring Boot 서버) |
| **Authorization Server** | 권한을 부여해주는 서버 | Google 로그인 서버 |
| **Resource Server** | 실제 정보를 가지고 있는 서버 | Google API 서버 (프로필 정보 등) |
| **Access Token** | 자원에 접근할 수 있는 열쇠 | 구글이 발급해준 토큰 (유효기간 있음) |
| **Refresh Token** | Access Token이 만료되면 재발급받기 위한 토큰 | (선택적) 장기 로그인 유지용 |

---

## 3. 동작 원리 (Authorization Code Grant Type)

가장 많이 사용되고 보안성이 높은 **"권한 부여 코드 승인 방식"**의 흐름입니다.

1.  **사용자:** "구글로 로그인하기" 버튼 클릭
2.  **Client(우리 앱):** 사용자를 구글 로그인 페이지로 이동시킴 (`Redirect`)
3.  **사용자:** 구글 ID/PW 입력 및 정보 제공 동의
4.  **Google:** 로그인이 성공하면, 미리 등록된 우리 앱의 주소(`Redirect URI`)로 **"인증 코드(Code)"**를 전달
5.  **Client(우리 앱):** 받은 "인증 코드"를 들고 구글 서버에 가서 **"Access Token 주세요"** 요청 (백엔드끼리 통신)
6.  **Google:** 코드가 유효하면 **"Access Token"** 발급
7.  **Client(우리 앱):** Access Token으로 사용자 정보(이메일, 이름 등) 조회 후 **회원가입/로그인 처리**

---

## 4. 실전 구현 (Spring Security 6.x)

### 4.1. 의존성 추가 (`build.gradle`)
Spring Boot 3.x 이상에서는 `spring-boot-starter-oauth2-client` 하나면 충분합니다.

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
}
```

### 4.2. 설정 파일 (`application.yml`)
구글 클라우드 콘솔에서 발급받은 ID와 Secret을 등록합니다.

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_CLIENT_ID
            client-secret: YOUR_CLIENT_SECRET
            scope: profile, email
```

### 4.3. Member 엔티티 설계 (Role 분리 전략)
로그인 계정(`Member`)과 비즈니스 역할(`Student`, `Teacher`)을 분리하여 확장성을 확보했습니다.

```java
@Entity
public class Member {
    @Id @GeneratedValue
    private Long id;

    private String email;  // 로그인 ID
    private String name;
    private String picture;

    @Enumerated(EnumType.STRING)
    private Role role;     // GUEST, STUDENT, TEACHER, ADMIN

    private String provider;   // google
    private String providerId; // 구글의 고유 식별자 (sub)
    
    // ... update 메서드 등
}
```

### 4.4. 핵심 로직: `CustomOAuth2UserService`
구글에서 받은 정보를 우리 DB에 저장하는 핵심 서비스입니다.

```java
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 구글에서 사용자 정보 가져오기
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 2. 정보 추출 (구글, 카카오, 네이버 분기 처리 가능)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 3. DB 저장 또는 업데이트 (가입/로그인)
        Member member = saveOrUpdate(attributes);

        // 4. 세션에 저장할 사용자 객체 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRoleKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }
}
```

### 4.5. SecurityConfig 설정
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService) // 우리가 만든 서비스 등록
            )
        );
    return http.build();
}
```

---

## 5. 프론트엔드 연동 시나리오 (Advanced)

### Q. 프론트엔드(React)가 생기면 어떻게 하나요?
현재 구현은 **"백엔드 주도(Server-Side)"** 방식입니다. 프론트엔드가 생기면 **JWT(Json Web Token)**를 활용해야 합니다.

1.  **프론트엔드:** `<a href="http://api.server.com/oauth2/authorization/google">로그인</a>` 링크 제공
2.  **백엔드:** 로그인 처리 완료 후 `OAuth2SuccessHandler` 실행
3.  **백엔드:** `SuccessHandler`에서 JWT 생성 후 프론트엔드 URL로 리다이렉트
    *   `response.sendRedirect("http://localhost:3000/oauth/callback?token=" + jwt);`
4.  **프론트엔드:** URL 파라미터에서 토큰을 꺼내 저장하고 API 요청 시 사용

---

## 6. 트러블슈팅 & 팁

1.  **`redirect_uri_mismatch` 에러:** 구글 콘솔에 등록한 주소와 실제 요청 주소가 토씨 하나라도 다르면 발생합니다. (끝에 `/` 유무도 중요)
2.  **`401 Unauthorized`:** 시큐리티 설정에서 `.anyRequest().authenticated()`로 막혀있는데 토큰 없이 요청하면 발생합니다. 개발 중에는 `.permitAll()`을 적절히 활용하세요.
3.  **DB 설계:** `Member` 테이블에 모든 정보를 넣지 마세요. `Student`, `Teacher` 테이블을 따로 만들고 `Member`와 1:1로 연결하는 것이 유지보수에 훨씬 좋습니다.

---

## 7. 심화 학습: 다양한 식별자(ID)의 세계

OAuth 2.0과 본인인증 서비스를 연동하다 보면 다양한 ID 값들을 마주하게 됩니다. 우리 프로젝트에서는 `sub`만 사용하지만, 실무를 위해 알아두면 좋습니다.

### 7.1. SUB (Subject) - "이 서비스에서의 너"
*   **정의:** OAuth 2.0 표준(OIDC)에서 정의한 **사용자 식별자**입니다.
*   **특징:** 서비스마다 값이 다릅니다. (구글의 `sub` ≠ 카카오의 `id`)
*   **우리 프로젝트:** `Member` 엔티티의 `providerId` 컬럼에 저장되는 값입니다.
*   **용도:** "이 구글 계정으로 로그인한 사람이 우리 DB의 몇 번 회원인가?" 식별용.

### 7.2. CI (Connecting Information) - "온라인 주민등록번호"
*   **정의:** **연계 정보**. 방통위/KISA에서 정의한 88바이트 암호화 문자열입니다.
*   **특징:** **대한민국 국민이라면 어느 사이트를 가도 똑같은 값**을 가집니다. (카카오, 네이버, 11번가 모두 동일)
*   **용도:**
    *   **중복 가입 방지:** "어? 너 카카오로 가입했었네? 네이버로 또 가입하지 마."
    *   **실명 인증:** 금융, 결제 서비스에서 필수입니다.
*   **주의:** 구글 같은 해외 서비스는 CI를 주지 않습니다. 카카오/네이버 등 국내 서비스에서만 제공합니다.

### 7.3. DI (Duplication Information) - "중복 가입 확인 정보"
*   **정의:** 해당 웹사이트(CP) 내에서의 중복 가입을 확인하기 위한 값입니다.
*   **특징:** 사이트마다 값이 다릅니다. (A 사이트의 DI ≠ B 사이트의 DI)
*   **용도:** 요즘은 CI가 워낙 강력해서 잘 쓰이지 않지만, 단순 중복 가입 체크용으로 사용됩니다.

### 7.4. UUID (Universally Unique Identifier)
*   **정의:** 범용 고유 식별자 (예: `550e8400-e29b...`)
*   **용도:** DB의 PK(Primary Key)로 사용하거나, 임시 토큰(이메일 인증 링크 등) 생성 시 사용합니다.

### ✅ 결론: 우리 프로젝트는?
우리는 **`sub` (providerId)**만 있으면 충분합니다. 하지만 나중에 "카카오 계정과 네이버 계정을 통합해주세요"라는 요구사항이 생긴다면, 그때 **`CI`**를 도입해야 합니다.
