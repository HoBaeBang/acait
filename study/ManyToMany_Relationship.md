# 다대다(N:M) 관계 해결 전략: 중간 엔티티 활용

> **작성일:** 2024.01.06  
> **프로젝트:** Academy Management  
> **주제:** 강의(Lecture)와 학생(Student)의 다대다 관계를 중간 엔티티(LectureStudent)로 해결하는 방법

---

## 1. 다대다(N:M) 관계란?

현실 세계에서 **"한 학생은 여러 강의를 들을 수 있고, 한 강의에는 여러 학생이 있을 수 있다"**는 상황을 말합니다.
관계형 데이터베이스(RDB)는 정규화된 테이블 2개만으로는 다대다 관계를 표현할 수 없습니다.

### 1.1 왜 `@ManyToMany`를 쓰지 않는가?
JPA는 `@ManyToMany` 어노테이션을 제공하여 자동으로 중간 테이블을 만들어주지만, 실무에서는 **절대 사용하지 않는 것**을 권장합니다.

1.  **추가 정보 저장 불가:** 단순히 연결만 할 뿐, "언제 등록했는지(`registeredAt`)", "성적은 몇 점인지(`grade`)" 같은 추가 정보를 중간 테이블에 넣을 수 없습니다.
2.  **쿼리 제어 불가:** JPA가 알아서 만드는 숨겨진 쿼리가 많아 성능 최적화가 어렵습니다.

---

## 2. 해결책: 연결 엔티티(Connection Entity) 승격

다대다 관계를 **일대다(1:N) + 다대일(N:1)** 관계로 풀어냅니다.
이를 위해 **`LectureStudent`**라는 중간 엔티티를 명시적으로 생성합니다.

### 2.1 구조 변화
*   **Before:** Lecture <-> Student (N:M)
*   **After:** Lecture <-(1:N)- **LectureStudent** -(N:1)-> Student

### 2.2 `LectureStudent` 엔티티 설계

```java
@Entity
@Table(name = "lecture_students",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_lecture_student",
                        columnNames = {"lecture_id", "student_id"} // 중복 등록 방지 (복합 유니크 키)
                )
        }
)
public class LectureStudent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 대리키 (Surrogate Key) 사용 권장

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @CreatedDate
    private LocalDateTime registeredAt; // 추가 정보 (등록일)
}
```

---

## 3. 비즈니스 로직 구현 (Service Layer)

### 3.1 학생 등록 (`registerStudent`)
1.  **권한 체크:** 요청한 강사가 해당 강의의 주인인지 확인합니다.
2.  **중복 체크:** 이미 등록된 학생인지 확인합니다 (`existsByLectureAndStudent`).
3.  **저장:** `LectureStudent` 객체를 생성하여 저장합니다.

```java
public void registerStudent(Member teacher, Long lectureId, Long studentId) {
    // ... (강의, 학생 조회 및 권한 체크)

    if (lectureStudentRepository.existsByLectureAndStudent(lecture, student)) {
        throw new IllegalStateException("이미 등록된 학생입니다.");
    }

    LectureStudent lectureStudent = LectureStudent.builder()
            .lecture(lecture)
            .student(student)
            .build();

    lectureStudentRepository.save(lectureStudent);
}
```

### 3.2 수강생 목록 조회 (`getStudentsByLecture`)
중간 엔티티를 통해 학생 정보를 가져옵니다.

```java
public List<StudentResponse> getStudentsByLecture(Member teacher, Long lectureId) {
    // ... (강의 조회 및 권한 체크)

    return lectureStudentRepository.findAllByLecture(lecture).stream()
            .map(ls -> StudentResponse.from(ls.getStudent())) // LectureStudent -> Student -> DTO
            .collect(Collectors.toList());
}
```

---

## 4. 핵심 학습 포인트

### 4.1 복합키 vs 대리키
중간 테이블의 PK(Primary Key)를 정하는 두 가지 전략이 있습니다.

1.  **복합키(Composite Key):** `lecture_id` + `student_id`를 묶어서 PK로 사용. (`@EmbeddedId` 필요)
2.  **대리키(Surrogate Key):** 독립적인 `id` (Long)를 PK로 사용하고, 외래키들은 유니크 제약조건으로 관리.

**우리의 선택:** **대리키 전략** (JPA에서 훨씬 다루기 쉽고, 나중에 비즈니스 요구사항이 바뀌어도 유연함)

### 4.2 유니크 제약조건 (`@UniqueConstraint`)
DB 레벨에서 중복 데이터(같은 강의에 같은 학생이 두 번 등록됨)를 막기 위해 `uniqueConstraints`를 설정했습니다. 애플리케이션 로직(`existsBy...`)으로도 막지만, DB에서 한 번 더 막아주는 것이 안전합니다.

---

## 5. 결론

이제 강사는 자신의 강의에 학생들을 자유롭게 배정하고 관리할 수 있습니다.
이 패턴(중간 엔티티 활용)은 **장바구니(상품-유저), 좋아요(게시글-유저), 태그(게시글-태그)** 등 수많은 곳에서 응용되므로 꼭 익혀두어야 합니다.
