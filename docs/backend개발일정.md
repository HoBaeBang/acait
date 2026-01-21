# 📅 ACAIT 백엔드 개발 일정 (v5.0 반영)

> **작성일:** 2026-01-21  
> **기준 문서:** [요구사항명세서_v5.md](./요구사항명세서_v5.md)  
> **작성자:** Backend Team

---

## 1. 현재 진행 상황 (As-Is)

*   **회원/인증:** OAuth2 로그인, 가입 신청/승인 프로세스 구현 완료.
*   **강의/학생:** 기본 CRUD 및 N:M 매핑(`LectureStudent`) 구현 완료.
*   **수업 기록:** `LectureRecord` 엔티티 및 기본 저장 API 구현 완료.
*   **보안:** JWT 인증 및 API 보안 설정 완료.
*   **정산:** `Settlement` 엔티티 및 기초 조회 API 구현 완료.
*   **운영:** 학원 생성 API 및 인원 제한 체크 로직 구현 완료.
*   **교재/엑셀/알림:** 긴급 추가된 기능 구현 완료.
*   **통합 테스트:** 주요 시나리오 검증 및 버그 수정 완료.
*   **문서화:** Swagger 설정 최신화 완료.

**⚠️ 주요 변경 필요 사항 (Gap Analysis):**
*   현재 모든 주요 기능 구현 및 테스트가 완료되었습니다.
*   배포 준비 및 최종 점검 단계입니다.

---

## 2. 개발 로드맵 (To-Be)

### 🚀 [Phase 1] 멀티 테넌트 아키텍처 전환 (완료 ✅)
> **목표:** 모든 데이터에 소속 학원(`academy_id`)을 부여하여 데이터 격리 구현.

*   **Task 1.1: `Academy` 엔티티 생성** (완료 ✅)
*   **Task 1.2: 모든 엔티티에 `academy_id` 추가 및 연관관계 설정** (완료 ✅)
*   **Task 1.3: 회원가입 프로세스 수정** (완료 ✅)

### 🧩 [Phase 2] 핵심 비즈니스 로직 고도화 (완료 ✅)
> **목표:** 현업에서 필수적인 예외 상황(보강, 퇴원, 일정 변경) 처리.

*   **Task 2.1: 학생 퇴원 관리** (완료 ✅)
*   **Task 2.2: 보강 시스템 구현** (완료 ✅)
*   **Task 2.3: 시간표 변경 정책 (Instance vs Series)** (완료 ✅)

### 💰 [Phase 3] 정산 및 세금 처리 (완료 ✅)
> **목표:** 정확한 강사료 계산 및 세금 공제.

*   **Task 3.1: 정산 엔티티 및 로직 구현** (완료 ✅)
    *   `Settlement` 엔티티 생성 및 3.3% 세금 계산 로직 구현.
    *   `SettlementRepository` 및 `Service` 뼈대 생성.
*   **Task 3.2: 정산 대시보드 API** (완료 ✅)
    *   `GET /api/v1/settlements/dashboard` 구현.
*   **Task 3.3: 실제 정산 계산 로직 구현** (완료 ✅)
    *   `LectureRecord` 데이터를 집계하여 `Settlement` 데이터 생성.

### 🛡️ [Phase 4] 운영 및 관리 기능 (완료 ✅)
> **목표:** 서비스 운영을 위한 제어 장치 마련.

*   **Task 4.1: 인원 제한 체크** (완료 ✅)
    *   강사 승인 시 `Academy.max_members` 체크 로직 추가.
*   **Task 4.2: 슈퍼 어드민 기능** (완료 ✅)
    *   학원별 인원 제한 상향 조정 API (`PUT /admin/academies/{id}/limit`).

### 📚 [Phase 5] 교재 및 부가 기능 (완료 ✅)
> **목표:** 프론트엔드 3주차 작업 지원 및 상용화 수준 기능 구현.

*   **Task 5.1: 교재(Material) 관리** (완료 ✅)
    *   `Material` 엔티티 생성 (공용/전용 구분).
    *   교재 검색 및 등록 API 구현.
*   **Task 5.2: 엑셀 다운로드** (완료 ✅)
    *   정산 내역 엑셀 파일 생성 API 구현.
*   **Task 5.3: 알림 시스템 실연동** (완료 ✅)
    *   SendGrid/Solapi 연동 (Gmail SMTP로 구현).

---

## 3. 상세 일정표 (Gantt Chart)

| 구분 | 작업 내용 | 예상 소요 | 담당자 | 비고 |
| :--- | :--- | :--- | :--- | :--- |
| **Phase 1** | Academy 엔티티 및 멀티 테넌트 적용 | 2일 | Backend | **완료** |
| | 회원가입(학원 생성/합류) 로직 수정 | 1일 | Backend | **완료** |
| **Phase 2** | 학생 퇴원(논리 삭제) 구현 | 0.5일 | Backend | **완료** |
| | 보강(Makeup) 로직 및 연결 구현 | 1.5일 | Backend | **완료** |
| | 시간표 변경 정책(Scope) 구현 | 1일 | Backend | **완료** |
| **Phase 3** | 정산 엔티티 및 조회 API | 0.5일 | Backend | **완료** |
| | 정산 계산 로직 (집계) | 1일 | Backend | **완료** |
| **Phase 4** | 인원 제한 체크 | 0.5일 | Backend | **완료** |
| | 슈퍼 어드민 기능 | 0.5일 | Backend | **완료** |
| **Phase 5** | 교재 관리 API | 1일 | Backend | **완료** |
| | 엑셀 다운로드 | 0.5일 | Backend | **완료** |
| **Test** | 전체 통합 테스트 및 버그 수정 | 1일 | All | **완료** |

---

## 4. 작업 히스토리 (History)

### 2026-01-14 (Day 1)
*   **[Phase 1] 멀티 테넌트 아키텍처 전환 완료**
    *   `Academy` 엔티티 및 리포지토리 생성.
    *   `Member`, `Student`, `Lecture`, `LectureRecord` 엔티티에 `academy_id` 연관관계 추가.
    *   `SignupRequest` DTO 수정 (`academyName`, `inviteCode` 추가).
    *   `AuthController` 수정: 원장 가입 시 학원 생성, 강사 가입 시 초대 코드로 학원 합류 로직 구현.
    *   `Student` 엔티티에 퇴원 관리 필드(`status`, `dischargeDate`) 추가.

### 2026-01-15 (Day 2)
*   **[Phase 2] 핵심 비즈니스 로직 고도화 완료**
    *   **학생 퇴원 관리:** `DELETE` API를 논리 삭제(`DISCHARGED`)로 변경 및 퇴원일 기록 구현.
    *   **보강 시스템 구현:** `LectureRecord`에 보강 상태(`REQ_MAKEUP`, `MAKEUP`) 및 연결(`linkedRecord`) 추가.
    *   **시간표 변경 정책:** `ScheduleUpdateRequest` DTO 생성 및 `scope`(`INSTANCE`, `SERIES`)에 따른 분기 처리 로직 구현.

### 2026-01-16 (Day 3)
*   **[Phase 3] 정산 및 세금 처리 기초 구현 완료**
    *   **정산 엔티티:** `Settlement` 엔티티 생성 (3.3% 세금 계산 로직 포함).
    *   **조회 API:** `GET /api/v1/settlements/dashboard` 구현.
    *   **슈퍼 계정:** `CustomOAuth2UserService`에 슈퍼 관리자(`aslanhobae@gmail.com`) 자동 생성 로직 추가.
    *   **트러블슈팅:** 회원가입 시 토큰 미발급 문제 해결 (`SignupResponse`에 토큰 포함).

### 2026-01-17 (Day 4)
*   **[Phase 4] 운영 및 관리 기능 일부 구현**
    *   **학원 생성 API:** `POST /api/v1/academies` 구현 (원장 가입 시 사용).
    *   **인원 제한 체크:** 강사 승인 시 `max_members` 체크 로직 추가 (`PLAN_LIMIT` 에러 반환).
    *   **API 경로 수정:** `AdminController` 경로를 `/api/v1`으로 변경하여 프론트엔드와 일치시킴.
    *   **로깅 필터 개선:** Request/Response Header 정보까지 로깅하도록 수정.
    *   **Role 명칭 변경:** `ROLE_ADMIN` -> `ROLE_OWNER`로 변경 (기획서 v5.0 반영).

### 2026-01-18 (Day 5)
*   **[Phase 3] 정산 계산 로직 구현 (핵심)**
    *   `calculateMonthlySettlement` 메서드 구현: `LectureRecord` 데이터를 집계하여 `Settlement` 데이터 생성.
    *   퇴원일 이후 수업 제외 로직 적용.
*   **[Phase 4] 슈퍼 어드민 기능 구현**
    *   학원별 인원 제한 상향 조정 API (`PUT /admin/academies/{id}/limit`).
*   **[Bug Fix] 강의 생성 및 조회 오류 수정**
    *   강의 생성 시 `academy_id` 누락 문제 해결.
    *   캘린더 조회 시 `Subject` NPE 문제 해결.

### 2026-01-19 (Day 6)
*   **[Phase 5] 교재 및 부가 기능 구현 (긴급)**
    *   **교재 관리:** `Material` 엔티티 및 검색/등록 API 구현.
    *   **엑셀 다운로드:** 정산 내역 엑셀 파일 생성 API 구현 (Apache POI).
    *   **알림 연동:** Gmail SMTP를 이용한 이메일 발송 로직 구현.
*   **[Bug Fix] FullCalendar 연동 이슈 해결**
    *   `LectureEventDto`에 `id` 필드 추가 및 기간별 조회 로직 개선.
*   **[Refactor] 로깅 및 설정 개선**
    *   Swagger 설정 수정 및 로깅 필터 가독성 개선.

### 2026-01-20 (Day 7)
*   **[Test] 통합 테스트 및 안정화 완료**
    *   **강의 기간 설정:** `Lecture` 엔티티에 `startDate`, `endDate` 추가 및 캘린더 조회 로직 고도화.
    *   **정산 상세 조회:** `GET /api/v1/settlements/{id}/details` API 구현.
    *   **학생 수강 정보:** `GET /api/v1/students/{id}/lectures` API 구현.
    *   **버그 수정:** `DataIntegrityViolationException` 등 주요 버그 해결.

### 2026-01-21 (Day 8)
*   **[Doc] API 문서화 마무리**
    *   Swagger 어노테이션 추가 (`@Operation`, `@Parameter` 등)
    *   인증 실패 처리(`JwtAuthenticationEntryPoint`) 및 Swagger JWT 인증 버튼 활성화.
*   **[Refactor] 미사용 코드 삭제**
    *   `DebugController`, `AttendanceCheckAspect` 등 삭제.
*   **[Config] DB 전환 (H2 -> MySQL)**
    *   `build.gradle` 및 `application.yml` 수정.

---

## 5. 내일 진행할 작업 (To-Do)

### 2026-01-22 (Day 9)
*   **[Deploy] 배포 준비**
    *   `Dockerfile` 및 배포 스크립트(`deploy.sh`) 작성.
    *   운영 환경용 `application-prod.yml` 설정.
*   **[Final] 최종 코드 리뷰 및 QA**
    *   전체 코드 리뷰 및 개선점 논의.
    *   프론트엔드 팀과 최종 연동 테스트.
