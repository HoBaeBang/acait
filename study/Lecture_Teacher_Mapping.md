# 강의(Lecture)와 강사(Member) 연관관계 매핑 학습

> **작성일:** 2024.01.06  
> **프로젝트:** Academy Management  
> **주제:** 엔티티 간의 관계 설정과 인증 정보 활용

---

## 1. 왜 연관관계 매핑이 필요한가?

기존 `Lecture` 엔티티는 강의 제목, 과목, 시간표 정보만 가지고 있었습니다. 하지만 실제 학원 시스템에서는 **"누가 이 강의를 가르치는가?"**가 매우 중요합니다.

### 1.1 문제점
*   로그인한 강사가 **자신의 강의만 조회**할 수 없음 (모든 강의가 다 보임)
*   강의 생성 시 **작성자 정보**가 누락됨
*   데이터베이스 관점에서 데이터의 **소유권(Ownership)**이 불분명함

### 1.2 해결책: N:1 연관관계
한 명의 강사(`Member`)는 여러 개의 강의(`Lecture`)를 개설할 수 있습니다. 따라서 **강의(N) : 강사(1)** 관계가 성립합니다.

---

## 2. 구현 내용 상세

### 2.1 Domain 계층 (`Lecture.java`)

```java
@Entity
public class Lecture {
    // ... 기존 필드들

    // ✅ 강사 정보 추가 (N:1 관계)
    // fetch = FetchType.LAZY: 강의 조회 시 강사 정보를 당장 쓰지 않는다면 굳이 조회하지 않음 (성능 최적화)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id") // 외래키 컬럼명 지정
    private Member teacher;

    // ✅ 강사 설정 메서드 (Setter 대신 의미 있는 메서드 사용 권장)
    public void setTeacher(Member teacher) {
        this.teacher = teacher;
    }
}
```

### 2.2 Service 계층 (`LectureServiceImpl.java`)

서비스 메서드의 시그니처를 변경하여 **강사 정보(`Member`)**를 파라미터로 받도록 수정했습니다.

```java
@Override
public LectureResponse createLecture(Member teacher, LectureRequest req) {
    // 1. DTO -> Entity 변환
    Lecture lecture = req.toLecture();
    
    // 2. 강사 정보 주입 (핵심!)
    lecture.setTeacher(teacher);

    // 3. 스케줄 추가 및 저장
    // ...
    return LectureResponse.from(saved);
}
```

### 2.3 Controller 계층 (`LectureController.java`)

가장 중요한 변화가 일어난 곳입니다. **Spring Security**가 제공하는 인증 정보를 활용하여 현재 로그인한 사용자를 식별합니다.

```java
@PostMapping
public ResponseEntity<LectureResponse> createLecture(
        // ✅ @AuthenticationPrincipal: 현재 인증된 사용자 정보(UserDetails)를 주입받음
        @AuthenticationPrincipal UserDetails userDetails, 
        @RequestBody LectureRequest lectureRequest) {
    
    // 1. 토큰(JWT)에 담겨있던 이메일(username) 추출
    String email = userDetails.getUsername();
    
    // 2. 이메일로 실제 Member 엔티티 조회
    Member teacher = memberRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

    // 3. 서비스에 강사 정보 전달
    LectureResponse lecture = lectureService.createLecture(teacher, lectureRequest);
    
    return ResponseEntity.ok(lecture);
}
```

---

## 3. 핵심 학습 포인트

### 3.1 `@AuthenticationPrincipal`의 마법
Spring Security는 요청이 들어오면 `JwtAuthenticationFilter`에서 토큰을 검사하고, 유효하다면 `SecurityContext`에 인증 객체(`Authentication`)를 저장합니다.

컨트롤러에서 `@AuthenticationPrincipal`을 사용하면, `SecurityContext`에 저장된 `Principal` 객체(우리의 경우 `UserDetails`)를 바로 꺼내 쓸 수 있습니다.

> **흐름:**
> Request Header (Token) -> JwtFilter -> SecurityContext -> Controller (@AuthenticationPrincipal)

### 3.2 왜 Controller에서 Member를 조회했나?
서비스 계층(`LectureService`)은 **순수한 비즈니스 로직**만 담당하는 것이 좋습니다.
"현재 로그인한 유저가 누구인지"를 파악하는 것은 **웹 계층(Controller)**의 역할이거나, 혹은 별도의 **보안 유틸리티**가 담당해야 합니다.

이번 구현에서는 Controller에서 유저를 식별(`findByEmail`)하고, Service에는 완성된 `Member` 객체를 넘겨주어 역할 분리를 시도했습니다.

### 3.3 `FetchType.LAZY` (지연 로딩)
`Lecture` 엔티티에서 `Member`를 참조할 때 `FetchType.LAZY`를 사용했습니다.

*   **EAGER (즉시 로딩):** 강의를 조회할 때마다 강사 정보도 무조건 같이 조회 (JOIN 쿼리 발생)
*   **LAZY (지연 로딩):** 강의만 조회하고, `lecture.getTeacher().getName()` 처럼 실제로 강사 정보가 필요할 때 쿼리 발생

실무에서는 **무조건 LAZY**를 기본으로 사용하고, 필요할 때만 `Fetch Join` 등으로 성능을 최적화하는 것이 정석입니다.

---

## 4. 다음 단계 (Next Step)

이제 강의에 "주인"이 생겼습니다. 다음으로는 **"내 강의만 조회하기"** 기능을 구현하여 데이터의 보안성을 높여야 합니다.

1.  `LectureRepository`에 `findAllByTeacher(Member teacher)` 메서드 추가
2.  `LectureService`에 `retrieveMyLectures(Member teacher)` 메서드 추가
3.  API 엔드포인트 분리 또는 파라미터 처리
