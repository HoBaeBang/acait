# JPA (Java Persistence API) 학습 가이드

## 목차
1. [JPA란 무엇인가?](#1-jpa란-무엇인가)
2. [엔티티 매핑](#2-엔티티-매핑)
3. [연관관계 매핑](#3-연관관계-매핑)
4. [영속성 컨텍스트](#4-영속성-컨텍스트)
5. [지연 로딩과 즉시 로딩](#5-지연-로딩과-즉시-로딩)
6. [Cascade와 고아 객체](#6-cascade와-고아-객체)
7. [실전 팁과 주의사항](#7-실전-팁과-주의사항)

---

## 1. JPA란 무엇인가?

### 1.1 개념
JPA(Java Persistence API)는 자바 ORM(Object-Relational Mapping) 표준 기술입니다.
- 객체와 관계형 데이터베이스를 매핑해주는 기술
- SQL을 직접 작성하지 않고도 데이터베이스 작업 수행 가능
- Hibernate, EclipseLink 등이 JPA의 구현체

### 1.2 장점
- **생산성**: SQL 작성 없이 객체 중심 개발
- **유지보수**: 필드 추가/삭제 시 SQL 수정 불필요
- **패러다임 불일치 해결**: 객체지향과 관계형 데이터베이스 간 차이 해소
- **성능 최적화**: 1차 캐시, 지연 로딩 등
- **데이터 접근 추상화**: 특정 DB에 종속되지 않음

---

## 2. 엔티티 매핑

### 2.1 기본 어노테이션

```java
@Entity                                    // JPA가 관리할 엔티티 클래스
@Table(name = "lectures")                  // 매핑할 테이블 이름 (생략 시 클래스명)
@Getter @Setter                            // Lombok
@NoArgsConstructor                         // 기본 생성자 (JPA 필수)
@AllArgsConstructor
@Builder
public class Lecture {

    @Id                                    // 기본키 매핑
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 자동 증가
    private Long id;

    @Column(nullable = false, length = 100)  // 컬럼 매핑
    private String title;
}
```

### 2.2 기본키 생성 전략

| 전략 | 설명 | 사용 예시 |
|-----|------|----------|
| `IDENTITY` | DB의 AUTO_INCREMENT 사용 | MySQL, PostgreSQL |
| `SEQUENCE` | DB 시퀀스 사용 | Oracle, PostgreSQL |
| `TABLE` | 키 생성용 테이블 사용 | 모든 DB (성능 이슈) |
| `AUTO` | DB에 따라 자동 선택 | 기본값 |

**프로젝트에서의 사용:**
```java
// Lecture.java:24-26
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```
- MySQL을 사용하므로 IDENTITY 전략이 적합

### 2.3 Column 매핑

```java
@Column(
    name = "lecture_title",      // DB 컬럼명
    nullable = false,            // NOT NULL 제약조건
    unique = true,               // UNIQUE 제약조건
    length = 200,                // VARCHAR 길이
    columnDefinition = "TEXT"    // 컬럼 정의 직접 지정
)
private String title;
```

### 2.4 Enum 매핑

```java
// Lecture.java:31-37
@Enumerated(EnumType.STRING)    // ORDINAL(숫자) 대신 STRING 사용 권장
@Column(nullable = false)
private LectureType lectureType;

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private Subject subject;
```

**주의:** `EnumType.ORDINAL`은 순서가 바뀌면 데이터 오류 발생 가능!

### 2.5 날짜/시간 매핑

```java
// LectureSchedule.java:27-33
@Enumerated(EnumType.STRING)
private DayOfWeek dayOfWeek;     // Java 8+ java.time 패키지 사용 가능

private LocalTime startTime;     // @Column 어노테이션 생략 가능
private LocalTime endTime;

// Lecture.java:44-49 (JPA Auditing)
@CreatedDate
@Column(updatable = false)
private LocalDateTime createdAt;

@LastModifiedDate
private LocalDateTime updatedAt;
```

**JPA Auditing 활성화:**
```java
@Entity
@EntityListeners(AuditingEntityListener.class)  // Lecture.java:21
public class Lecture { ... }

// Application.java에 추가
@EnableJpaAuditing
```

---

## 3. 연관관계 매핑

### 3.1 연관관계의 종류

| 연관관계 | 어노테이션 | 예시 |
|---------|-----------|------|
| 다대일 (N:1) | `@ManyToOne` | 여러 스케줄 → 하나의 강의 |
| 일대다 (1:N) | `@OneToMany` | 하나의 강의 → 여러 스케줄 |
| 일대일 (1:1) | `@OneToOne` | 회원 ↔ 회원 상세정보 |
| 다대다 (N:M) | `@ManyToMany` | 학생 ↔ 강의 (중간 테이블 권장) |

### 3.2 단방향 vs 양방향

#### 단방향 연관관계
```java
@Entity
public class LectureSchedule {
    @ManyToOne
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;  // 스케줄 → 강의만 참조
}
```

**장점:**
- 구조가 단순하고 관리가 쉬움
- 한쪽 방향으로만 조회하므로 코드가 명확함
- 순환 참조 문제 없음

**언제 사용하나?**
- 한쪽에서만 참조가 필요한 경우
  - 예: 주문(Order) → 회원(Member) - 주문에서 회원 정보만 필요
  - 예: 댓글(Comment) → 게시글(Post) - 댓글에서 게시글 정보만 필요
- 객체 그래프 탐색이 단방향으로만 필요한 경우
- 복잡도를 줄이고 싶을 때

**실무 사용 예시:**
```java
// 결제 정보는 주문을 참조하지만, 주문에서 결제 정보를 조회할 일이 거의 없음
@Entity
public class Payment {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;  // 단방향만으로 충분
}
```

#### 양방향 연관관계 (프로젝트에서 사용)
```java
// Lecture.java:40-42 (일대다 쪽)
@OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
@Builder.Default
private List<LectureSchedule> schedules = new ArrayList<>();

// LectureSchedule.java:21-23 (다대일 쪽 - 연관관계 주인)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "lecture_id", nullable = false)
private Lecture lecture;
```

**장점:**
- 양쪽에서 자유롭게 객체 그래프 탐색 가능
- 비즈니스 로직 구현이 편리함

**단점:**
- 관리 포인트가 두 배 (양쪽 모두 신경 써야 함)
- 순환 참조 문제 주의 필요 (JSON 직렬화, toString 등)
- 코드 복잡도 증가

**언제 사용하나?**
- 양쪽에서 모두 조회가 빈번한 경우
  - 예: 강의(Lecture) ↔ 스케줄(Schedule) - 강의의 스케줄 조회, 스케줄의 강의 조회 모두 필요
  - 예: 게시글(Post) ↔ 댓글(Comment) - 게시글의 댓글 목록, 댓글의 게시글 모두 자주 조회
  - 예: 팀(Team) ↔ 회원(Member) - 팀의 회원 목록, 회원의 팀 정보 모두 필요
- 부모-자식 관계에서 부모가 자식을 관리할 때
  - cascade, orphanRemoval 사용 시 유용
- 컬렉션 조회가 필요한 경우

**실무 사용 예시:**
```java
// 게시글-댓글: 게시글에서 댓글 목록 조회, 댓글에서 게시글 조회 모두 필요
@Entity
public class Post {
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);
    }
}

@Entity
public class Comment {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;
}
```

#### 실무 판단 기준

| 상황 | 권장 | 이유 |
|-----|------|------|
| 한쪽에서만 조회 | 단방향 | 단순하고 관리 쉬움 |
| 양쪽에서 조회 필요 | 양방향 | 편의성 증가 |
| 부모가 자식 생명주기 관리 | 양방향 + cascade | 데이터 일관성 유지 |
| 다른 엔티티와 공유되는 객체 | 단방향 | orphanRemoval 위험 방지 |
| 조회만 하는 경우 | 단방향 | 복잡도 감소 |

**실무 팁:**
1. **기본은 단방향으로 시작** - 필요할 때만 양방향으로 변경
2. **JPQL로 해결 가능하면 단방향** - 쿼리로 조회 가능하면 굳이 양방향 불필요
   ```java
   // 양방향 없이도 JPQL로 조회 가능
   @Query("SELECT s FROM Schedule s WHERE s.lecture.id = :lectureId")
   List<Schedule> findByLectureId(@Param("lectureId") Long lectureId);
   ```
3. **성능보다는 비즈니스 로직 우선** - 조회 빈도가 높으면 양방향 고려
4. **API 응답에는 DTO 사용** - 양방향 관계의 순환 참조 문제 방지

### 3.3 연관관계의 주인

**핵심 개념:**
- 양방향 연관관계에서 **외래키를 관리하는 쪽**이 주인
- `mappedBy`가 **없는** 쪽이 주인 (보통 `@ManyToOne` 쪽)
- 주인이 아닌 쪽은 읽기만 가능

**프로젝트 예시:**
```java
// LectureSchedule이 주인 (외래키 lecture_id 보유)
@ManyToOne
@JoinColumn(name = "lecture_id")  // 외래키 컬럼명 지정
private Lecture lecture;

// Lecture은 주인이 아님 (mappedBy 사용)
@OneToMany(mappedBy = "lecture")  // 주인의 필드명 지정
private List<LectureSchedule> schedules;
```

### 3.4 연관관계 편의 메서드

양방향 관계에서는 **양쪽을 모두 설정**해야 합니다!

```java
// 잘못된 사용
LectureSchedule schedule = new LectureSchedule();
schedule.setLecture(lecture);
lecture.getSchedules().add(schedule);  // 두 곳에서 설정 필요

// 올바른 사용 - 편의 메서드 (Lecture.java:52-55)
public void addSchedule(LectureSchedule schedule) {
    this.schedules.add(schedule);
    schedule.setLecture(this);  // 양방향 관계 자동 설정
}

// 사용
lecture.addSchedule(schedule);  // 한 번만 호출
```

### 3.5 실무 사례: 프로젝트에서 양방향 관계를 선택한 이유

이 프로젝트(academy-management)에서 **Lecture(강의) ↔ LectureSchedule(스케줄)** 관계를 양방향으로 설계한 이유를 분석해봅시다.

#### 프로젝트 코드 구조

```java
// Lecture.java (부모 엔티티)
@Entity
@Table(name = "lectures")
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private LectureType lectureType;

    @Enumerated(EnumType.STRING)
    private Subject subject;

    // 👉 양방향 관계 설정
    @OneToMany(mappedBy = "lecture",
               cascade = CascadeType.ALL,      // 핵심!
               orphanRemoval = true)            // 핵심!
    @Builder.Default
    private List<LectureSchedule> schedules = new ArrayList<>();

    // 연관관계 편의 메서드
    public void addSchedule(LectureSchedule schedule) {
        this.schedules.add(schedule);
        schedule.setLecture(this);
    }
}

// LectureSchedule.java (자식 엔티티)
@Entity
@Table(name = "lecture_schedules")
public class LectureSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 👉 연관관계 주인
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private LocalTime startTime;
    private LocalTime endTime;
}
```

---

#### 양방향 관계를 선택한 5가지 이유

#### 1. 부모-자식 생명주기 완전 의존 관계

**스케줄은 강의에 완전히 종속**되어 있습니다.

```java
// ✅ 강의 삭제 시 스케줄도 자동 삭제 (cascade = CascadeType.ALL)
@Transactional
public void deleteLecture(Long lectureId) {
    Lecture lecture = lectureRepository.findById(lectureId).orElseThrow();
    lectureRepository.delete(lecture);
    // 👉 연관된 모든 LectureSchedule이 자동으로 삭제됨!
}

// ✅ 스케줄 제거 시 DB에서도 삭제 (orphanRemoval = true)
@Transactional
public void removeSchedule(Long lectureId, int scheduleIndex) {
    Lecture lecture = lectureRepository.findById(lectureId).orElseThrow();
    lecture.getSchedules().remove(scheduleIndex);
    // 👉 컬렉션에서 제거만 해도 DB에서 자동 삭제됨!
}
```

**만약 단방향이었다면:**
```java
// ❌ 수동으로 처리해야 함
@Transactional
public void deleteLecture(Long lectureId) {
    // 1. 먼저 스케줄 삭제
    scheduleRepository.deleteAllByLectureId(lectureId);

    // 2. 그 다음 강의 삭제
    lectureRepository.deleteById(lectureId);

    // 순서를 틀리면 외래키 제약조건 위반!
    // 코드가 복잡하고 실수하기 쉬움
}
```

#### 2. 양방향 조회가 모두 필요

**강의 → 스케줄 조회 (필수)**
```java
// "수학 강의는 언제 진행되나요?"
@Transactional(readOnly = true)
public LectureDetailDTO getLectureDetail(Long lectureId) {
    Lecture lecture = lectureRepository.findById(lectureId).orElseThrow();

    // 👉 강의에서 스케줄 목록 조회
    List<ScheduleDTO> scheduleDTOs = lecture.getSchedules().stream()
        .map(schedule -> new ScheduleDTO(
            schedule.getDayOfWeek(),
            schedule.getStartTime(),
            schedule.getEndTime()
        ))
        .collect(Collectors.toList());

    return new LectureDetailDTO(lecture.getTitle(), scheduleDTOs);
    // 결과: "수학 강의 - 월/수/금 14:00-16:00"
}
```

**스케줄 → 강의 조회 (필수)**
```java
// "월요일 14시 수업은 무슨 과목인가요?"
@Transactional(readOnly = true)
public String getScheduleInfo(Long scheduleId) {
    LectureSchedule schedule = scheduleRepository.findById(scheduleId)
        .orElseThrow();

    // 👉 스케줄에서 강의 정보 조회
    Lecture lecture = schedule.getLecture();

    return String.format("%s %s - %s 강의",
        schedule.getDayOfWeek(),
        schedule.getStartTime(),
        lecture.getTitle()
    );
    // 결과: "월요일 14:00 - 고등수학 강의"
}
```

#### 3. 편의 메서드로 데이터 일관성 보장

```java
// Lecture.java:52-55
public void addSchedule(LectureSchedule schedule) {
    this.schedules.add(schedule);
    schedule.setLecture(this);  // 양방향 자동 동기화
}

// 실제 사용
@Transactional
public LectureResponse createLectureWithSchedules(LectureCreateRequest req) {
    Lecture lecture = Lecture.builder()
        .title(req.getTitle())
        .lectureType(req.getLectureType())
        .subject(req.getSubject())
        .build();

    // 스케줄 추가
    LectureSchedule monday = LectureSchedule.builder()
        .dayOfWeek(DayOfWeek.MONDAY)
        .startTime(LocalTime.of(14, 0))
        .endTime(LocalTime.of(16, 0))
        .build();

    // 👉 한 번 호출로 양방향 설정 완료!
    lecture.addSchedule(monday);

    // 👉 cascade로 lecture와 schedule 모두 저장
    Lecture saved = lectureRepository.save(lecture);

    return LectureResponse.from(saved);
}
```

**만약 편의 메서드 없이 수동으로 했다면:**
```java
// ⚠️ 실수하기 쉬운 코드
lecture.getSchedules().add(monday);  // 한쪽만 설정
monday.setLecture(lecture);          // 다른 쪽도 설정

// 만약 한쪽을 빠뜨리면 동기화 문제 발생!
```

#### 4. 고아 객체 자동 제거로 데이터 정합성 유지

```java
// orphanRemoval = true 덕분에 가능
@Transactional
public void updateLectureSchedules(Long lectureId, List<ScheduleDTO> newSchedules) {
    Lecture lecture = lectureRepository.findById(lectureId).orElseThrow();

    // 👉 기존 스케줄 전체 제거 - DB에서도 자동 삭제됨!
    lecture.getSchedules().clear();

    // 새로운 스케줄 추가
    newSchedules.forEach(dto -> {
        LectureSchedule schedule = dto.toEntity();
        lecture.addSchedule(schedule);
    });

    // 변경 감지로 자동 UPDATE
    // orphanRemoval로 제거된 스케줄은 자동 DELETE
}
```

**만약 단방향이었다면:**
```java
// ❌ 복잡한 수동 처리 필요
@Transactional
public void updateLectureSchedules(Long lectureId, List<ScheduleDTO> newSchedules) {
    // 1. 기존 스케줄 조회
    List<LectureSchedule> oldSchedules = scheduleRepository.findByLectureId(lectureId);

    // 2. 수동 삭제
    scheduleRepository.deleteAll(oldSchedules);

    // 3. 새로운 스케줄 생성 및 저장
    List<LectureSchedule> newEntities = newSchedules.stream()
        .map(dto -> {
            LectureSchedule schedule = dto.toEntity();
            schedule.setLecture(lecture);  // 수동 설정
            return schedule;
        })
        .collect(Collectors.toList());

    scheduleRepository.saveAll(newEntities);
}
```

#### 5. 코드 간결성과 유지보수성

**양방향 관계:**
```java
// ✅ 간결하고 직관적
Lecture lecture = lectureRepository.findById(1L).orElseThrow();
int scheduleCount = lecture.getSchedules().size();

for (LectureSchedule schedule : lecture.getSchedules()) {
    System.out.println(schedule.getDayOfWeek());
}
```

**단방향이었다면:**
```java
// ❌ 추가 Repository 메서드 필요
Lecture lecture = lectureRepository.findById(1L).orElseThrow();

// 별도 Repository 메서드 작성 필요
List<LectureSchedule> schedules = scheduleRepository.findByLectureId(1L);
int scheduleCount = schedules.size();

for (LectureSchedule schedule : schedules) {
    System.out.println(schedule.getDayOfWeek());
}
```

---

#### 실무 판단 체크리스트

프로젝트에서 다음 항목들을 체크한 결과 양방향이 적합했습니다:

| 체크 항목 | 이 프로젝트 | 양방향 필요 여부 |
|---------|-----------|----------------|
| 자식이 부모에 완전히 종속? | ✅ 스케줄은 강의에 종속 | 양방향 (cascade) |
| 부모에서 자식 조회 빈번? | ✅ 강의 상세 조회 시 스케줄 필요 | 양방향 |
| 자식에서 부모 조회 빈번? | ✅ 스케줄에서 강의 정보 필요 | 양방향 |
| 부모 삭제 시 자식도 삭제? | ✅ 강의 삭제 시 스케줄도 삭제 | 양방향 (cascade) |
| 컬렉션 관리 필요? | ✅ 스케줄 목록 관리 | 양방향 |
| 자식이 다른 부모와 공유? | ❌ 스케줄은 단일 강의 소속 | orphanRemoval 가능 |

---

#### 핵심 정리

**이 프로젝트에서 양방향 관계를 선택한 결정적 이유:**

1. **생명주기 의존**: 스케줄은 강의에 완전히 종속 → `cascade`, `orphanRemoval` 필수
2. **양방향 조회**: 강의→스케줄, 스케줄→강의 모두 빈번
3. **편의성**: `addSchedule()` 메서드로 양방향 동기화 자동화
4. **데이터 정합성**: 고아 레코드 자동 제거로 DB 정합성 유지
5. **코드 간결성**: 별도 Repository 메서드 불필요, 코드 가독성 향상

**결론**: 부모-자식 관계에서 **생명주기를 함께 관리**해야 한다면 **양방향 + cascade + orphanRemoval** 조합이 최선의 선택입니다!

---

### 3.6 다대다(N:M) 관계와 실무 적용

#### @ManyToMany는 왜 실무에서 사용하지 않을까?

```java
// 이론적으로는 간단해 보이는 @ManyToMany
@Entity
public class Student {
    @Id @GeneratedValue
    private Long id;
    private String name;

    @ManyToMany
    @JoinTable(name = "student_lecture",
               joinColumns = @JoinColumn(name = "student_id"),
               inverseJoinColumns = @JoinColumn(name = "lecture_id"))
    private List<Lecture> lectures = new ArrayList<>();
}

@Entity
public class Lecture {
    @Id @GeneratedValue
    private Long id;
    private String title;

    @ManyToMany(mappedBy = "lectures")
    private List<Student> students = new ArrayList<>();
}
```

**@ManyToMany의 치명적인 문제점:**

#### 1. 중간 테이블에 추가 컬럼을 넣을 수 없음
```sql
-- 실제로 필요한 테이블 구조
CREATE TABLE student_lecture (
    id BIGINT PRIMARY KEY,
    student_id BIGINT,
    lecture_id BIGINT,
    enrolled_at DATETIME,      -- 수강 신청 일시
    status VARCHAR(20),         -- 수강 상태 (수강중, 완료, 취소)
    grade VARCHAR(2),           -- 성적
    attendance_rate DECIMAL,    -- 출석률
    created_at DATETIME,
    updated_at DATETIME
);

-- @ManyToMany로는 student_id와 lecture_id만 관리 가능!
-- 나머지 필드들을 저장할 방법이 없음
```

#### 2. 중간 테이블을 직접 제어할 수 없음
- JPA가 자동으로 생성한 테이블은 쿼리 조작이 어려움
- 복잡한 조회 조건 추가 불가능
- 감사(Audit) 정보 추가 불가능

#### 3. 예상치 못한 쿼리 발생
```java
student.getLectures().add(lecture);
// 내부적으로 복잡한 쿼리 발생
// 중간 테이블 조회 → 삭제 → 재생성
```

---

#### 실무 해결책: 중간 엔티티를 직접 생성

다대다 관계를 **일대다 + 다대일** 두 개로 풀어서 구현

**Before (안티패턴):**
```
Student ←─────┐
              ├─ @ManyToMany
Lecture ←─────┘
```

**After (실무 패턴):**
```
Student ─(1:N)─→ Enrollment ←─(N:1)─ Lecture
```

#### 실무 예시 1: 수강 신청 시스템

```java
// 학생 엔티티
@Entity
public class Student {
    @Id @GeneratedValue
    private Long id;

    private String name;

    // 학생 → 수강신청 (1:N)
    @OneToMany(mappedBy = "student")
    private List<Enrollment> enrollments = new ArrayList<>();

    // 비즈니스 메서드
    public void enrollLecture(Lecture lecture) {
        Enrollment enrollment = Enrollment.builder()
            .student(this)
            .lecture(lecture)
            .status(EnrollmentStatus.ACTIVE)
            .enrolledAt(LocalDateTime.now())
            .build();

        enrollments.add(enrollment);
        lecture.getEnrollments().add(enrollment);
    }
}

// 강의 엔티티
@Entity
public class Lecture {
    @Id @GeneratedValue
    private Long id;

    private String title;

    // 강의 → 수강신청 (1:N)
    @OneToMany(mappedBy = "lecture")
    private List<Enrollment> enrollments = new ArrayList<>();

    // 비즈니스 메서드
    public int getCurrentStudentCount() {
        return (int) enrollments.stream()
            .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
            .count();
    }
}

// 중간 엔티티 (핵심!)
@Entity
@Table(name = "enrollments")
public class Enrollment {
    @Id @GeneratedValue
    private Long id;

    // 수강신청 → 학생 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // 수강신청 → 강의 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    // 추가 필드들 (이게 핵심!)
    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;  // ACTIVE, COMPLETED, CANCELLED

    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;

    private String grade;  // A+, A, B+, ...
    private Integer attendanceRate;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 비즈니스 로직
    public void cancel() {
        if (this.status == EnrollmentStatus.COMPLETED) {
            throw new IllegalStateException("완료된 수강은 취소할 수 없습니다.");
        }
        this.status = EnrollmentStatus.CANCELLED;
    }

    public void complete(String grade) {
        this.status = EnrollmentStatus.COMPLETED;
        this.grade = grade;
        this.completedAt = LocalDateTime.now();
    }
}

public enum EnrollmentStatus {
    ACTIVE,      // 수강중
    COMPLETED,   // 완료
    CANCELLED    // 취소
}
```

#### 실무 예시 2: 상품-카테고리 관계

```java
// 상품과 카테고리는 다대다 관계이지만, 중간 엔티티로 풀어냄
@Entity
public class Product {
    @Id @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = "product")
    private List<ProductCategory> productCategories = new ArrayList<>();
}

@Entity
public class Category {
    @Id @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = "category")
    private List<ProductCategory> productCategories = new ArrayList<>();
}

// 중간 엔티티
@Entity
@Table(name = "product_categories")
public class ProductCategory {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // 추가 필드
    private Boolean isPrimary;  // 대표 카테고리 여부
    private Integer displayOrder;  // 카테고리 내 상품 표시 순서

    @CreatedDate
    private LocalDateTime createdAt;
}
```

#### 실무 조회 예시

```java
// EnrollmentRepository.java
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // 학생의 수강 목록 조회 (강의 정보 포함)
    @Query("SELECT e FROM Enrollment e " +
           "JOIN FETCH e.lecture " +
           "WHERE e.student.id = :studentId " +
           "AND e.status = :status")
    List<Enrollment> findByStudentIdAndStatus(
        @Param("studentId") Long studentId,
        @Param("status") EnrollmentStatus status);

    // 강의의 수강생 목록 조회 (학생 정보 포함)
    @Query("SELECT e FROM Enrollment e " +
           "JOIN FETCH e.student " +
           "WHERE e.lecture.id = :lectureId " +
           "AND e.status = 'ACTIVE'")
    List<Enrollment> findActiveLectureStudents(@Param("lectureId") Long lectureId);

    // 학생이 특정 강의를 수강 중인지 확인
    boolean existsByStudentIdAndLectureIdAndStatus(
        Long studentId, Long lectureId, EnrollmentStatus status);

    // 성적별 수강생 통계
    @Query("SELECT e.grade, COUNT(e) FROM Enrollment e " +
           "WHERE e.lecture.id = :lectureId " +
           "AND e.status = 'COMPLETED' " +
           "GROUP BY e.grade")
    List<Object[]> countByGrade(@Param("lectureId") Long lectureId);
}
```

#### 실무 사용 흐름

```java
// EnrollmentService.java
@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final LectureRepository lectureRepository;

    // 수강 신청
    public EnrollmentDTO enroll(Long studentId, Long lectureId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new EntityNotFoundException("학생 없음"));

        Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(() -> new EntityNotFoundException("강의 없음"));

        // 중복 수강 체크
        if (enrollmentRepository.existsByStudentIdAndLectureIdAndStatus(
                studentId, lectureId, EnrollmentStatus.ACTIVE)) {
            throw new IllegalStateException("이미 수강 중인 강의입니다.");
        }

        // 중간 엔티티 생성
        Enrollment enrollment = Enrollment.builder()
            .student(student)
            .lecture(lecture)
            .status(EnrollmentStatus.ACTIVE)
            .enrolledAt(LocalDateTime.now())
            .build();

        Enrollment saved = enrollmentRepository.save(enrollment);
        return EnrollmentDTO.from(saved);
    }

    // 수강 취소
    public void cancel(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new EntityNotFoundException("수강 신청 없음"));

        enrollment.cancel();  // 엔티티 내부 비즈니스 로직
        // 변경 감지로 자동 UPDATE
    }

    // 성적 입력
    public void completeWithGrade(Long enrollmentId, String grade) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new EntityNotFoundException("수강 신청 없음"));

        enrollment.complete(grade);
        // 변경 감지로 자동 UPDATE
    }
}
```

#### 다대다 관계 설계 가이드

| 상황 | 사용 | 이유 |
|-----|------|------|
| 프로토타입, 간단한 예제 | `@ManyToMany` | 빠른 구현 |
| 실무 프로젝트 | 중간 엔티티 | 확장성, 유지보수 |
| 중간 테이블에 추가 정보 필요 | 중간 엔티티 필수 | @ManyToMany 불가능 |
| 복잡한 조회 조건 필요 | 중간 엔티티 | 쿼리 작성 용이 |
| 중간 테이블 이력 관리 | 중간 엔티티 | Audit 기능 추가 가능 |

**핵심 정리:**
1. **@ManyToMany는 실무에서 거의 사용하지 않음**
2. **중간 엔티티를 만들어서 1:N + N:1 관계로 풀어냄**
3. **중간 엔티티에 비즈니스 로직과 추가 필드 포함**
4. **확장성과 유지보수성이 훨씬 좋음**

---

## 4. 영속성 컨텍스트

### 4.1 개념
영속성 컨텍스트는 **엔티티를 영구 저장하는 환경**입니다.
- 1차 캐시
- 동일성 보장
- 트랜잭션을 지원하는 쓰기 지연
- 변경 감지(Dirty Checking)
- 지연 로딩

### 4.2 엔티티의 생명주기

```
비영속(new/transient)
    ↓ em.persist()
영속(managed) ← em.find(), JPQL
    ↓ em.detach()
준영속(detached)
    ↓ em.remove()
삭제(removed)
```

### 4.3 1차 캐시와 동일성 보장

```java
// 같은 트랜잭션 내에서
Lecture lecture1 = em.find(Lecture.class, 1L);  // DB 조회
Lecture lecture2 = em.find(Lecture.class, 1L);  // 1차 캐시에서 조회 (SQL 안나감)

lecture1 == lecture2  // true (같은 인스턴스)
```

### 4.4 변경 감지 (Dirty Checking)

```java
@Transactional
public void updateLecture(Long id, String newTitle) {
    Lecture lecture = lectureRepository.findById(id).orElseThrow();
    lecture.setTitle(newTitle);  // 변경만 하면 자동으로 UPDATE 쿼리 실행
    // lectureRepository.save() 호출 불필요!
}
```

### 4.5 플러시(Flush)

영속성 컨텍스트의 변경 내용을 DB에 반영하는 것

```java
em.flush();           // 수동 플러시
// 또는
@Modifying(flushAutomatically = true)
```

**플러시 발생 시점:**
1. `em.flush()` 직접 호출
2. 트랜잭션 커밋 시 자동
3. JPQL 쿼리 실행 직전 자동

---

## 5. 지연 로딩과 즉시 로딩

### 5.1 즉시 로딩 (EAGER)

```java
@ManyToOne(fetch = FetchType.EAGER)  // 기본값
private Lecture lecture;

// 스케줄 조회 시 강의도 함께 조회 (JOIN 사용)
LectureSchedule schedule = em.find(LectureSchedule.class, 1L);
// SELECT * FROM lecture_schedules s
// LEFT JOIN lectures l ON s.lecture_id = l.id
```

**단점:**
- 불필요한 데이터까지 조회
- N+1 문제 발생 가능

### 5.2 지연 로딩 (LAZY) - 권장

```java
// LectureSchedule.java:21
@ManyToOne(fetch = FetchType.LAZY)  // 프록시 객체 반환
private Lecture lecture;

LectureSchedule schedule = em.find(LectureSchedule.class, 1L);
// SELECT * FROM lecture_schedules WHERE id = 1

String lectureTitle = schedule.getLecture().getTitle();  // 이 시점에 Lecture 조회
// SELECT * FROM lectures WHERE id = ?
```

### 5.3 N+1 문제와 해결

**문제 발생:**
```java
// 스케줄 10개 조회
List<LectureSchedule> schedules = scheduleRepository.findAll();
// SELECT * FROM lecture_schedules (1번)

for (LectureSchedule schedule : schedules) {
    schedule.getLecture().getTitle();
    // SELECT * FROM lectures WHERE id = ? (N번, 총 10번)
}
// 총 11번의 쿼리 발생!
```

**해결 방법 1: Fetch Join**
```java
@Query("SELECT s FROM LectureSchedule s JOIN FETCH s.lecture")
List<LectureSchedule> findAllWithLecture();
// SELECT * FROM lecture_schedules s
// INNER JOIN lectures l ON s.lecture_id = l.id (1번만!)
```

**해결 방법 2: @EntityGraph**
```java
@EntityGraph(attributePaths = {"lecture"})
List<LectureSchedule> findAll();
```

**해결 방법 3: Batch Size**
```java
@BatchSize(size = 100)  // 또는 application.yml에 설정
@OneToMany(mappedBy = "lecture")
private List<LectureSchedule> schedules;

// IN 쿼리로 한 번에 조회
// SELECT * FROM lectures WHERE id IN (?, ?, ?, ...)
```

### 5.4 기본 Fetch 전략

| 연관관계 | 기본값 | 권장 |
|---------|--------|------|
| `@ManyToOne` | EAGER | LAZY |
| `@OneToOne` | EAGER | LAZY |
| `@OneToMany` | LAZY | LAZY |
| `@ManyToMany` | LAZY | LAZY |

**결론: 모든 연관관계에 LAZY 사용 권장!**

---

## 6. Cascade와 고아 객체

### 6.1 Cascade (영속성 전이)

부모 엔티티의 영속 상태 변화를 자식 엔티티에 전파

```java
// Lecture.java:40
@OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL)
private List<LectureSchedule> schedules;
```

#### Cascade 타입

| 타입 | 설명 |
|-----|------|
| `PERSIST` | 부모 저장 시 자식도 저장 |
| `REMOVE` | 부모 삭제 시 자식도 삭제 |
| `MERGE` | 부모 병합 시 자식도 병합 |
| `REFRESH` | 부모 새로고침 시 자식도 새로고침 |
| `DETACH` | 부모 분리 시 자식도 분리 |
| `ALL` | 위 모든 것 |

**사용 예시:**
```java
Lecture lecture = new Lecture();
lecture.setTitle("수학 강의");

LectureSchedule schedule = new LectureSchedule();
schedule.setDayOfWeek(DayOfWeek.MONDAY);
lecture.addSchedule(schedule);

lectureRepository.save(lecture);
// Lecture와 LectureSchedule 모두 저장됨 (cascade = PERSIST)

lectureRepository.delete(lecture);
// Lecture와 LectureSchedule 모두 삭제됨 (cascade = REMOVE)
```

### 6.2 고아 객체 (orphanRemoval)

```java
// Lecture.java:40
@OneToMany(mappedBy = "lecture",
           cascade = CascadeType.ALL,
           orphanRemoval = true)  // 고아 객체 자동 삭제
private List<LectureSchedule> schedules;
```

**동작:**
```java
Lecture lecture = lectureRepository.findById(1L).orElseThrow();
lecture.getSchedules().remove(0);  // 컬렉션에서 제거
// DELETE FROM lecture_schedules WHERE id = ? (자동 삭제)
```

### 6.3 Cascade vs orphanRemoval 차이

```java
// cascade = CascadeType.REMOVE
lectureRepository.delete(lecture);  // 부모 삭제 시 자식도 삭제

// orphanRemoval = true
lecture.getSchedules().clear();  // 컬렉션에서 제거만 해도 삭제
```

**주의사항:**
- `orphanRemoval = true`는 참조가 제거된 엔티티를 **자동 삭제**
- 다른 곳에서도 참조하는 엔티티에는 사용 금지!
- 단일 소유자일 때만 사용 (예: Lecture가 Schedule의 유일한 소유자)

---

## 7. 실전 팁과 주의사항

### 7.1 양방향 연관관계 주의사항

#### 문제 1: JSON 직렬화 시 무한 순환 참조
```java
@Entity
public class Lecture {
    @OneToMany(mappedBy = "lecture")
    private List<LectureSchedule> schedules;  // → schedules에 lecture가 있고...
}

@Entity
public class LectureSchedule {
    @ManyToOne
    private Lecture lecture;  // → lecture에 schedules가 있고...
}
// 무한 루프 발생!
```

**해결 방법:**
```java
// 1. @JsonIgnore 사용
@OneToMany(mappedBy = "lecture")
@JsonIgnore
private List<LectureSchedule> schedules;

// 2. @JsonManagedReference / @JsonBackReference
@OneToMany(mappedBy = "lecture")
@JsonManagedReference
private List<LectureSchedule> schedules;

@ManyToOne
@JsonBackReference
private Lecture lecture;

// 3. DTO 변환 (가장 권장)
public class LectureResponseDTO {
    private Long id;
    private String title;
    private List<ScheduleDTO> schedules;
}
```

#### 문제 2: toString() 무한 루프
```java
// Lombok 사용 시
@ToString(exclude = "schedules")  // 양방향 관계 필드 제외
public class Lecture { ... }
```

### 7.2 컬렉션 초기화

```java
// Lecture.java:42
@OneToMany(mappedBy = "lecture")
@Builder.Default  // 빌더 패턴 사용 시 필수
private List<LectureSchedule> schedules = new ArrayList<>();
// null이 아닌 빈 리스트로 초기화 → NPE 방지
```

**왜 필요한가?**
```java
// 초기화 안 하면
Lecture lecture = new Lecture();
lecture.getSchedules().add(schedule);  // NullPointerException!

// 초기화 하면
Lecture lecture = new Lecture();
lecture.getSchedules().add(schedule);  // OK
```

### 7.3 양방향 연관관계 설정 시 주의

```java
// 잘못된 예
LectureSchedule schedule = new LectureSchedule();
schedule.setLecture(lecture);
// lecture.schedules에는 추가 안 됨 → 동기화 문제

// 올바른 예 - 편의 메서드 사용
lecture.addSchedule(schedule);
```

### 7.4 엔티티 직접 반환 금지

```java
// 나쁜 예
@GetMapping("/lectures/{id}")
public Lecture getLecture(@PathVariable Long id) {
    return lectureRepository.findById(id).orElseThrow();
    // 엔티티 구조가 API 스펙이 되어버림
    // 순환 참조 위험
    // 엔티티 변경 시 API 스펙도 변경
}

// 좋은 예
@GetMapping("/lectures/{id}")
public LectureResponseDTO getLecture(@PathVariable Long id) {
    Lecture lecture = lectureRepository.findById(id).orElseThrow();
    return LectureResponseDTO.from(lecture);
    // DTO로 변환하여 반환
}
```

### 7.5 equals()와 hashCode() 구현

```java
@Entity
public class Lecture {
    @Id
    @GeneratedValue
    private Long id;

    // Set, Map 사용 시 필요
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lecture)) return false;
        Lecture lecture = (Lecture) o;
        return id != null && id.equals(lecture.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
```

### 7.6 @Transactional 필수 상황

```java
// 1. 지연 로딩 사용 시
@Transactional(readOnly = true)
public LectureDTO getLecture(Long id) {
    Lecture lecture = lectureRepository.findById(id).orElseThrow();
    lecture.getSchedules().size();  // 지연 로딩 초기화
    return LectureDTO.from(lecture);
}

// 2. 변경 감지 사용 시
@Transactional
public void updateLecture(Long id, String newTitle) {
    Lecture lecture = lectureRepository.findById(id).orElseThrow();
    lecture.setTitle(newTitle);  // 트랜잭션 커밋 시 자동 UPDATE
}
```

### 7.7 Bulk 연산 주의

```java
@Modifying
@Query("UPDATE Lecture l SET l.title = :title WHERE l.id = :id")
int updateTitle(@Param("id") Long id, @Param("title") String title);
// 영속성 컨텍스트를 거치지 않음!

// 해결: 벌크 연산 후 영속성 컨텍스트 초기화
@Modifying(clearAutomatically = true)
```

---

## 8. 프로젝트 코드 분석

### 8.1 Lecture 엔티티
```java
@Entity
@Table(name = "lectures")
@EntityListeners(AuditingEntityListener.class)
public class Lecture {
    // ✅ IDENTITY 전략 (MySQL)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Enum은 STRING으로
    @Enumerated(EnumType.STRING)
    private LectureType lectureType;

    // ✅ 양방향 관계 + cascade + orphanRemoval
    @OneToMany(mappedBy = "lecture",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    @Builder.Default
    private List<LectureSchedule> schedules = new ArrayList<>();

    // ✅ JPA Auditing
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ✅ 연관관계 편의 메서드
    public void addSchedule(LectureSchedule schedule) {
        this.schedules.add(schedule);
        schedule.setLecture(this);
    }
}
```

### 8.2 LectureSchedule 엔티티
```java
@Entity
@Table(name = "lecture_schedules")
public class LectureSchedule {
    // ✅ LAZY 로딩 (권장)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    // ✅ Java 8 time 패키지 활용
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private LocalTime startTime;
    private LocalTime endTime;

    // ✅ 비즈니스 로직 포함
    public boolean isValidTime() {
        return startTime.isBefore(endTime);
    }
}
```

### 8.3 개선 제안

#### 1. DTO 변환 유틸리티 추가
```java
public class LectureResponseDTO {
    private Long id;
    private String title;
    private LectureType lectureType;
    private Subject subject;
    private List<ScheduleDTO> schedules;

    public static LectureResponseDTO from(Lecture lecture) {
        return LectureResponseDTO.builder()
            .id(lecture.getId())
            .title(lecture.getTitle())
            .lectureType(lecture.getLectureType())
            .subject(lecture.getSubject())
            .schedules(lecture.getSchedules().stream()
                .map(ScheduleDTO::from)
                .collect(Collectors.toList()))
            .build();
    }
}
```

#### 2. 삭제 시 확인 로직 추가
```java
// LectureService.java
@Transactional
public void deleteLecture(Long id) {
    Lecture lecture = lectureRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("강의를 찾을 수 없습니다."));

    // cascade로 인해 모든 스케줄이 삭제됨을 확인
    if (!lecture.getSchedules().isEmpty()) {
        log.warn("강의 삭제 시 {} 개의 스케줄도 함께 삭제됩니다.",
                 lecture.getSchedules().size());
    }

    lectureRepository.delete(lecture);
}
```

#### 3. 조회 최적화
```java
// LectureRepository.java
public interface LectureRepository extends JpaRepository<Lecture, Long> {

    // Fetch Join으로 N+1 문제 해결
    @Query("SELECT DISTINCT l FROM Lecture l LEFT JOIN FETCH l.schedules WHERE l.id = :id")
    Optional<Lecture> findByIdWithSchedules(@Param("id") Long id);

    // 목록 조회 시
    @Query("SELECT DISTINCT l FROM Lecture l LEFT JOIN FETCH l.schedules")
    List<Lecture> findAllWithSchedules();
}
```

---

## 9. 학습 체크리스트

### 기초
- [ ] JPA의 개념과 장점 이해
- [ ] 엔티티 매핑 어노테이션 사용
- [ ] 기본키 생성 전략 이해
- [ ] Column 매핑 옵션 활용
- [ ] Enum 매핑 (STRING vs ORDINAL)

### 연관관계
- [ ] 단방향 vs 양방향 이해
- [ ] 연관관계의 주인 개념
- [ ] @ManyToOne, @OneToMany 사용
- [ ] mappedBy 사용법
- [ ] 연관관계 편의 메서드 작성

### 영속성 컨텍스트
- [ ] 영속성 컨텍스트 개념 이해
- [ ] 엔티티 생명주기 이해
- [ ] 1차 캐시와 동일성 보장
- [ ] 변경 감지 (Dirty Checking)
- [ ] 플러시 동작 방식

### 성능 최적화
- [ ] LAZY vs EAGER 차이
- [ ] N+1 문제 인식 및 해결
- [ ] Fetch Join 사용
- [ ] @EntityGraph 사용
- [ ] @BatchSize 활용

### 고급
- [ ] Cascade 타입별 동작
- [ ] orphanRemoval 이해
- [ ] @Transactional 활용
- [ ] DTO 변환 패턴
- [ ] Bulk 연산 주의사항

---

## 10. 참고 자료

### 공식 문서
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Hibernate Documentation](https://hibernate.org/orm/documentation/)

### 추천 도서
- 자바 ORM 표준 JPA 프로그래밍 (김영한)
- Spring Boot와 AWS로 혼자 구현하는 웹 서비스

### 온라인 강의
- 인프런: 자바 ORM 표준 JPA 프로그래밍 - 기본편
- 인프런: 실전! 스프링 부트와 JPA 활용

---

## 마치며

JPA는 강력하지만 올바르게 사용하지 않으면 성능 이슈가 발생할 수 있습니다.
특히 다음 사항을 항상 염두에 두세요:

1. **모든 연관관계는 LAZY 로딩으로!**
2. **N+1 문제 주의하고 Fetch Join 활용**
3. **엔티티는 API에 직접 노출하지 말고 DTO 사용**
4. **양방향 관계는 정말 필요한 경우에만**
5. **@Transactional 적절히 사용**

이 문서에서 다룬 개념들을 실제 프로젝트에 적용하며 학습하시길 바랍니다!
