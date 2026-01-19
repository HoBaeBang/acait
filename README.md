# 🏫 ACAIT (Academy Management SaaS)

> **학원 운영의 모든 것, ACAIT 하나로 해결하세요.**  
> 멀티 테넌트 아키텍처 기반의 학원 관리 SaaS 플랫폼입니다.

---

## 🌟 프로젝트 소개

**ACAIT**는 학원 운영에 필요한 핵심 기능(회원, 강의, 학생, 출결, 정산)을 제공하는 SaaS 플랫폼입니다.  
하나의 시스템으로 수많은 학원을 동시에 수용할 수 있는 **멀티 테넌트(Multi-Tenancy)** 아키텍처를 채택하여, 데이터 격리와 확장성을 모두 확보했습니다.

### 💡 핵심 가치
- **SaaS (Software as a Service):** 설치 없이 웹에서 바로 사용 가능
- **Multi-Tenancy:** 학원별 완벽한 데이터 격리 및 독립적인 운영 환경 제공
- **All-in-One:** 학생 관리부터 정산/세금 처리까지 한 번에 해결

---

## 🚀 주요 기능

### 1. 회원 및 권한 관리 (Auth & RBAC)
- **OAuth2 로그인:** 구글 계정으로 간편 가입 및 로그인
- **역할 기반 접근 제어 (RBAC):**
    - `OWNER` (원장): 학원 생성, 강사 승인, 정산 관리, 설정
    - `INSTRUCTOR` (강사): 강의 개설, 학생 관리, 출결/일지 작성
    - `SUPER_ADMIN` (개발자): 시스템 운영 및 인원 제한 관리

### 2. 멀티 테넌트 (Multi-Tenancy)
- **학원 생성:** 원장 가입 시 고유한 학원 공간 생성
- **강사 초대:** 초대 코드(`inviteCode`)를 통한 강사 합류 시스템
- **데이터 격리:** 모든 데이터(학생, 강의, 정산 등)는 소속 학원 내에서만 접근 가능

### 3. 강의 및 시간표 관리 (LMS)
- **강의 개설:** 과목, 수강료, 기간 설정
- **시간표 관리:** 요일/시간별 반복 일정 자동 생성
- **캘린더 연동:** FullCalendar와 연동하여 월별/주별 일정 시각화

### 4. 수업 기록 및 출결 (Attendance)
- **출결 관리:** 출석, 지각, 결석, 보강 필요(`REQ_MAKEUP`) 상태 관리
- **보강 시스템:** 결석한 수업에 대한 보강 일정 잡기 및 연결(`Link`)
- **수업 일지:** 매 수업마다 진도 및 특이사항 기록

### 5. 정산 및 세금 처리 (Settlement)
- **자동 정산:** 매월 말일 수업 기록을 집계하여 강사료 계산
- **세금 처리:** 3.3% 원천징수 자동 계산 및 실지급액 산출
- **퇴원 처리:** 학생 퇴원일(`dischargeDate`) 이후 수업 자동 제외
- **엑셀 다운로드:** 정산 내역을 엑셀 파일로 다운로드

### 6. 교재 관리 (Material)
- **하이브리드 검색:** 공용 교재 데이터베이스 + 학원 전용 교재 검색
- **교재 등록:** 우리 학원만의 전용 교재 등록 기능

---

## 🛠️ 기술 스택 (Tech Stack)

### Backend
- **Language:** Java 17
- **Framework:** Spring Boot 3.2.1
- **Database:** H2 (In-Memory) -> MySQL (Production)
- **ORM:** Spring Data JPA (Hibernate)
- **Security:** Spring Security, OAuth2 Client, JWT
- **Build Tool:** Gradle
- **Docs:** SpringDoc OpenAPI (Swagger UI)
- **Utils:** Apache POI (Excel), JavaMailSender (Email)

### Architecture
- **Layered Architecture:** Controller - Service - Repository
- **Multi-Tenancy:** Row Isolation Strategy (`academy_id` column)
- **AOP:** Logging, Performance Monitoring

---

## 🏃‍♂️ 실행 방법 (Getting Started)

### 1. 사전 준비
- Java 17 이상 설치
- Google Cloud Console에서 OAuth2 Client ID/Secret 발급
- Gmail 앱 비밀번호 발급 (알림 발송용)

### 2. 설정 파일 생성
`src/main/resources/application-secret.yml` 파일을 생성하고 다음 내용을 입력하세요.

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: "YOUR_GOOGLE_CLIENT_ID"
            client-secret: "YOUR_GOOGLE_CLIENT_SECRET"
  mail:
    username: "YOUR_GMAIL_ADDRESS"
    password: "YOUR_GMAIL_APP_PASSWORD"

jwt:
  secret: "YOUR_JWT_SECRET_KEY_MUST_BE_VERY_LONG_AND_SECURE"
```

### 3. 실행
```bash
./gradlew bootRun
```

### 4. 접속
- **API 서버:** `http://localhost:8080`
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **H2 Console:** `http://localhost:8080/h2-console`

---

## 📚 API 문서 (Swagger)

서버 실행 후 `http://localhost:8080/swagger-ui/index.html`에 접속하면 전체 API 명세를 확인하고 테스트할 수 있습니다.

- **인증:** 우측 상단 `Authorize` 버튼 클릭 -> `Bearer {토큰}` 입력
- **주요 API:**
    - `Auth`: 회원가입, 로그인
    - `Academy`: 학원 관리
    - `Lecture`: 강의 및 시간표
    - `Student`: 학생 관리
    - `Record`: 수업 기록 및 출결
    - `Settlement`: 정산 및 엑셀

---

## 📂 프로젝트 구조

```
src/main/java/com/aslan/academymanagement/
├── aspect/          # AOP (로깅, 성능 등)
├── config/          # 설정 (Security, Swagger, Web 등)
├── controller/      # API 컨트롤러
├── domain/          # JPA 엔티티
├── dto/             # 데이터 전송 객체
├── repository/      # DB 접근 계층
├── service/         # 비즈니스 로직
└── AcademyManagementApplication.java
```

---

## 📝 License

This project is licensed under the MIT License.
