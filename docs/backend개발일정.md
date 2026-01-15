# 📅 ACAIT 백엔드 개발 일정 (v5.0 반영)

> **작성일:** 2026-01-14  
> **기준 문서:** [요구사항명세서_v5.md](./요구사항명세서_v5.md)  
> **작성자:** Backend Team

---

## 1. 현재 진행 상황 (As-Is)

*   **회원/인증:** OAuth2 로그인, 가입 신청/승인 프로세스 구현 완료.
*   **강의/학생:** 기본 CRUD 및 N:M 매핑(`LectureStudent`) 구현 완료.
*   **수업 기록:** `LectureRecord` 엔티티 및 기본 저장 API 구현 완료.
*   **보안:** JWT 인증 및 API 보안 설정 완료.

**⚠️ 주요 변경 필요 사항 (Gap Analysis):**
1.  **멀티 테넌트 미적용:** 현재는 `academy_id` 개념이 없어 모든 데이터가 섞여 있음.
2.  **보강/퇴원 로직 부재:** 단순 출석/결석만 존재하며, 보강 연결이나 퇴원 처리가 안 됨.
3.  **시간표 정책 단순함:** 반복 일정의 예외 처리(이번 주만 변경) 로직이 없음.

---

## 2. 개발 로드맵 (To-Be)

### 🚀 [Phase 1] 멀티 테넌트 아키텍처 전환 (D-Day ~ D+2)
> **목표:** 모든 데이터에 소속 학원(`academy_id`)을 부여하여 데이터 격리 구현.

*   **Task 1.1: `Academy` 엔티티 생성** (완료 ✅)
    *   `name`, `invite_code`, `max_members` 필드 포함.
*   **Task 1.2: 모든 엔티티에 `academy_id` 추가 및 연관관계 설정** (완료 ✅)
    *   `Member`, `Student`, `Lecture`, `LectureRecord`, `Settlement` 등.
    *   JPA `@PrePersist` 또는 별도 리스너를 통해 `academy_id` 자동 주입 고려.
*   **Task 1.3: 회원가입 프로세스 수정** (완료 ✅)
    *   최초 가입 시: "새 학원 생성" vs "기존 학원 합류(초대코드)" 선택 로직 추가.

### 🧩 [Phase 2] 핵심 비즈니스 로직 고도화 (D+3 ~ D+5)
> **목표:** 현업에서 필수적인 예외 상황(보강, 퇴원, 일정 변경) 처리.

*   **Task 2.1: 학생 퇴원 관리** (완료 ✅)
    *   `Student` 엔티티에 `status` (ATTENDING/DISCHARGED), `discharge_date` 추가.
    *   `DELETE` API를 물리 삭제에서 논리 삭제(상태 변경)로 수정.
*   **Task 2.2: 보강 시스템 구현** (완료 ✅)
    *   `LectureRecord`에 `attendance_status` (REQ_MAKEUP, MAKEUP) 및 `linked_record_id` 추가.
    *   보강 수업 등록 시 원본 결석 기록 검증 로직 구현.
*   **Task 2.3: 시간표 변경 정책 (Instance vs Series)** (완료 ✅)
    *   `PUT /schedules/{id}` API에서 `scope` 파라미터 처리.
    *   `INSTANCE`: `LectureRecord`만 생성/수정.
    *   `SERIES`: `Schedule` 엔티티 수정 (과거 기록 보존).

### 💰 [Phase 3] 정산 및 세금 처리 (D+6 ~ D+7)
> **목표:** 정확한 강사료 계산 및 세금 공제.

*   **Task 3.1: 정산 엔티티 및 로직 구현** (진행 예정 🚧)
    *   `Settlement` 엔티티에 `tax_amount`, `real_amount` 추가.
    *   3.3% 공제 계산 로직 구현 (`Total * 0.033`).
*   **Task 3.2: 정산 대시보드 API** (진행 예정 🚧)
    *   월별, 강사별 정산 현황 조회.
    *   퇴원일 이후 수업 제외 로직 적용.

### 🛡️ [Phase 4] 운영 및 관리 기능 (D+8)
> **목표:** 서비스 운영을 위한 제어 장치 마련.

*   **Task 4.1: 인원 제한 체크**
    *   강사 승인 시 `Academy.max_members` 체크 로직 추가.
*   **Task 4.2: 슈퍼 어드민 기능**
    *   학원별 인원 제한 상향 조정 API (`PUT /admin/academies/{id}/limit`).

---

## 3. 상세 일정표 (Gantt Chart)

| 구분 | 작업 내용 | 예상 소요 | 담당자 | 비고 |
| :--- | :--- | :--- | :--- | :--- |
| **Phase 1** | Academy 엔티티 및 멀티 테넌트 적용 | 2일 | Backend | **DB 스키마 대공사** |
| | 회원가입(학원 생성/합류) 로직 수정 | 1일 | Backend | |
| **Phase 2** | 학생 퇴원(논리 삭제) 구현 | 0.5일 | Backend | |
| | 보강(Makeup) 로직 및 연결 구현 | 1.5일 | Backend | 난이도 높음 |
| | 시간표 변경 정책(Scope) 구현 | 1일 | Backend | 난이도 높음 |
| **Phase 3** | 정산 및 세금 계산 로직 | 1일 | Backend | |
| **Phase 4** | 인원 제한 및 슈퍼 어드민 | 0.5일 | Backend | |
| **Test** | 전체 통합 테스트 및 버그 수정 | 1일 | All | |

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

---

## 5. 내일 진행할 작업 (To-Do)

### 2026-01-16 (Day 3)
*   **[Phase 3] 정산 및 세금 처리 시작**
    *   **정산 엔티티 구현:** `Settlement` 엔티티 생성 (`taxAmount`, `realAmount` 포함).
    *   **정산 로직 구현:** 3.3% 세금 공제 계산 및 월별 정산 데이터 생성 로직 구현.
    *   **정산 대시보드 API:** 강사별/월별 정산 현황 조회 API 구현.
