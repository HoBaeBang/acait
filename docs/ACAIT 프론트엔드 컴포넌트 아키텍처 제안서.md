---

# ACAIT 프론트엔드 컴포넌트 아키텍처 제안서

**문서 버전:** v1.0
**대상:** 프론트엔드 개발팀
**목표:** 재사용성을 높이고, 복잡한 비즈니스 로직(시간 충돌, 정산 상태)을 효과적으로 격리하는 컴포넌트 구조 설계.

---

## 1. 프로젝트 디렉토리 구조 (추천)

엔터프라이즈급 관리자 화면이므로 **기능(Feature) 단위**로 폴더를 구조화하는 것을 추천합니다.

```text
src/
├── api/                # API 호출 함수 모음 (Axios/Fetch Wrapper)
├── components/         # 공통 UI 컴포넌트 (Button, Modal, Input...)
├── features/           # 비즈니스 로직이 포함된 핵심 기능 모듈
│   ├── auth/           # 로그인, 회원가입
│   ├── schedule/       # 캘린더, 일정 관리, 수업 기록
│   ├── students/       # 학생 관리 CRUD
│   ├── settlement/     # 정산 대시보드, 테이블
│   └── lectures/       # 강의 개설 관리
├── layouts/            # 레이아웃 (Sidebar, Header)
├── pages/              # 라우팅 페이지 (Container 역할)
└── stores/             # 전역 상태 관리 (Zustand/Redux/Pinia)

```

---

## 2. 핵심 기능별 컴포넌트 상세 설계

### 2.1. 일정 및 캘린더 (Schedule Feature)

가장 복잡한 화면입니다. '판서 수업'과 '개별 진도'의 렌더링 방식이 다르므로 컴포넌트 분리가 필수입니다.

* **`WeeklyCalendarContainer`** (Smart Component)
* **역할:** API(`GET /schedules/weekly`) 호출, 날짜 변경 상태 관리.
* **하위:**
* `CalendarHeader`: 이번 주 날짜 네비게이션 (< 2026.01.2주차 >), 필터(강사별 보기).
* `TimeGrid`: 월~일 / 13:00~22:00 그리드 레이아웃.
* **`LectureEventBlock`**: 개별 일정 렌더링 컴포넌트 (조건부 렌더링).





#### 🔍 상세: `LectureEventBlock` 내부 분기

이 컴포넌트는 `props.type`에 따라 다른 UI를 그립니다.

1. **`BoardEventItem` (판서 수업용)**
* **Style:** 배경색이 꽉 찬 블록 (`backgroundColor: #FFD700`).
* **Content:** `강의명` 중앙 정렬.
* **Interaction:** 클릭 시 `LectureDetailModal` 오픈.


2. **`IndividualEventItem` (개별 진도용)**
* **Style:** 테두리(Border)만 있고 배경은 투명하거나 연함.
* **Content:** `학생 이름` + `시간(14:00~16:00)`.
* **Overlap:** CSS `z-index`와 `left/width` 값을 계산하여 겹치는 일정을 시각화.



* **`DailyLogModal`** (Form)
* **역할:** 수업 후 출석 체크 및 일지 작성.
* **Props:** `lectureId`, `studentId`, `defaultTime`.
* **하위:**
* `AttendanceRadioGroup`: 출석/지각/결석 선택.
* `TimeInput`: 실제 시작/종료 시간 입력 (개별 진도일 경우 노출).
* `BookSearchInput`: 교재 검색 모달 트리거.
* **`ConflictWarningDialog`**: 저장 시 409 에러가 뜨면 노출되는 "시간 충돌 경고창".





---

### 2.2. 정산 및 대시보드 (Settlement Feature)

데이터의 '수정 가능/불가능' 상태 제어가 핵심입니다.

* **`SettlementDashboardPage`** (Container)
* **역할:** `GET /settlements/dashboard` 데이터 페칭.
* **하위:**
* `DashboardSummaryCard`: [이번 달 예상 수익 | 정산 완료 금액] 표시.
* `SettlementFilter`: 년/월 선택, 강사 선택(원장용).
* `SettlementActionToolbar`: [마감 처리] 버튼, [엑셀 다운로드] 버튼.




* **`SettlementTable`** (Data Grid)
* **역할:** 정산 상세 내역 리스팅.
* **Props:** `data`, `status` ('OPEN' | 'CLOSED').
* **Columns:** 날짜, 강의명, 학생명, 출결, **단가(수정가능)**, **금액(계산됨)**.
* **Logic:**
* `status === 'CLOSED'`이면 모든 Input은 `disabled` (회색 처리).
* `status === 'OPEN'`이면 단가 셀 클릭 시 `<EditableCell>`로 전환.





---

### 2.3. 학생 및 강의 관리 (CRUD Feature)

반복되는 CRUD 패턴을 모듈화합니다.

* **`StudentListTable`**
* **기능:** 검색, 페이지네이션, 행 클릭 시 상세 이동.


* **`StudentFormModal`**
* **입력 필드:** 이름, 학교, 학년, **학부모 연락처(Validation 필수)**.


* **`LectureManageCard`**
* **기능:** 강의 정보 표시 및 수강생 관리.
* **하위:**
* `StudentAssignmentList`: 현재 수강 중인 학생 목록 + [제거] 버튼.
* `AddStudentModal`: 수강생 검색 및 추가.





---

## 3. 공통 UI 라이브러리 (Common Components)

개발 속도를 높이기 위해 미리 만들어야 할 "Dumb Components"입니다.

| 컴포넌트명 | 설명 | 비고 |
| --- | --- | --- |
| **`BaseModal`** | 헤더, 바디, 푸터가 있는 팝업 껍데기 | `isOpen`, `onClose` props 사용 |
| **`SearchableSelect`** | 검색이 가능한 드롭다운 | 학생 검색, 교재 검색용 |
| **`Badge`** | 상태 표시 태그 | 출석(초록), 결석(빨강), 마감(회색) |
| **`Toast`** | 우측 상단 알림 메시지 | "저장되었습니다", "오류가 발생했습니다" |
| **`LoadingSpinner`** | 데이터 로딩 중 표시 | Suspense fallback 용도 |

---

## 4. 상태 관리 전략 (State Management)

복잡도를 낮추기 위해 **서버 상태**와 **클라이언트 상태**를 분리합니다.

### 4.1. Server State (React Query / TanStack Query 권장)

캘린더 데이터나 정산 데이터는 서버 데이터이므로 캐싱과 동기화가 중요합니다.

* `useWeeklySchedule(startDate, endDate)`: 캘린더 데이터 캐싱.
* `useSettlementStats(year, month)`: 정산 대시보드 데이터.
* **장점:** `refetchOnWindowFocus` 기능을 켜두면, PC를 켜놓고 다른 데 다녀와도 자동으로 최신 일정으로 갱신됩니다.

### 4.2. Client State (Zustand / Recoil)

* `useAuthStore`: 로그인한 사용자 정보 (`accessToken`, `role`, `userName`).
* `useToastStore`: 전역 알림 메시지 큐.

---

## 5. 외부 라이브러리 추천 (Tech Stack)

프론트엔드 개발 시 생산성을 높여줄 라이브러리 조합입니다.

1. **UI Framework:**
* **Tailwind CSS** (빠른 스타일링) 또는 **MUI / Ant Design** (빠른 어드민 구축).
* *추천:* 디자인 커스텀이 많다면 Tailwind, 기본 디자인으로 충분하면 MUI.


2. **Calendar:**
* **FullCalendar** (기능 강력, 유료 옵션 확인 필요) 또는 **React-Big-Calendar** (무료, 커스텀 용이).
* *ACAIT 추천:* 개별 진도의 중복 표현이 중요하므로 **React-Big-Calendar**를 커스텀하거나, **직접 `Grid`로 구현**하는 것을 추천 (생각보다 어렵지 않음).


3. **Form:**
* **React Hook Form**: 렌더링 최적화 및 Validation 관리에 필수.


4. **Date Util:**
* **Day.js**: Moment.js보다 가볍고 사용법이 유사함.


---
