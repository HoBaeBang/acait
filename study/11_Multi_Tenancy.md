# 멀티 테넌트 (Multi-Tenancy) 아키텍처 이해하기

> **작성일:** 2026-01-16  
> **프로젝트:** Academy Management  
> **주제:** SaaS(Software as a Service)의 핵심, 데이터 격리 전략

---

## 1. 멀티 테넌트란 무엇인가?

**멀티 테넌트(Multi-Tenancy)**는 하나의 소프트웨어 인스턴스로 여러 사용자 그룹(Tenant)에게 서비스를 제공하는 아키텍처입니다.

### 🏢 쉬운 비유: "아파트 vs 단독주택"
*   **싱글 테넌트 (단독주택):** 고객마다 서버와 DB를 따로 구축해줍니다. (비싸고 관리 힘듦)
*   **멀티 테넌트 (아파트):** 하나의 거대한 건물(서버/DB) 안에 여러 가구(학원)가 입주해서 삽니다.
    *   **핵심:** 옆집 사람이 우리 집 안방에 들어오면 안 됩니다! (**데이터 격리**)

### 1.1 우리 프로젝트에서의 적용
*   **Tenant:** 각각의 **학원(Academy)**
*   **User:** 학원에 소속된 **강사(Member)**, **학생(Student)**
*   **목표:** A학원 강사는 A학원의 학생과 강의만 볼 수 있어야 하고, B학원 데이터는 절대 볼 수 없어야 합니다.

---

## 2. 멀티 테넌트 구현 전략

데이터베이스 수준에서 데이터를 격리하는 방법은 크게 3가지가 있습니다.

### 2.1 Database 분리 (Database per Tenant)
*   학원마다 아예 다른 DB 서버를 씁니다.
*   **장점:** 완벽한 격리, 보안성 최고.
*   **단점:** 비용이 매우 비쌈. 학원이 1000개면 DB도 1000개 필요.

### 2.2 Schema 분리 (Schema per Tenant)
*   하나의 DB 서버 안에서 스키마(Schema)만 분리합니다.
*   **장점:** 적당한 비용과 격리 수준.
*   **단점:** 여전히 관리가 복잡함.

### 2.3 Row 분리 (Shared Database, Shared Schema) - ✅ 우리의 선택
*   모든 학원 데이터가 **하나의 테이블**에 섞여 있습니다.
*   대신 모든 테이블에 **`academy_id` (Tenant ID)** 컬럼을 추가하여 구분합니다.
*   **장점:** 비용이 가장 저렴하고 확장이 쉬움. SaaS 스타트업의 표준.
*   **단점:** 개발자가 실수로 `WHERE academy_id = ?` 조건을 빼먹으면 **대형 보안 사고(데이터 유출)** 발생.

---

## 3. 구현 내용 상세

우리는 **Row 분리 전략**을 채택하여 다음과 같이 구현했습니다.

### 3.1 `Academy` 엔티티 (Tenant)
시스템의 최상위 개념인 '학원'을 정의했습니다.

```java
@Entity
public class Academy {
    @Id @GeneratedValue
    @Column(name = "academy_id")
    private Long id;

    private String name;
    private String inviteCode; // 강사 초대용 코드
}
```

### 3.2 모든 엔티티에 소속 명시
`Member`, `Student`, `Lecture` 등 모든 핵심 엔티티에 `Academy`와의 연관관계를 추가했습니다.

```java
@Entity
public class Student {
    // ...
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", nullable = false)
    private Academy academy; // 이 학생이 어느 학원 소속인지 명시
}
```

### 3.3 회원가입 프로세스 변경
데이터의 주인이 될 '학원'이 먼저 존재해야 하므로, 가입 절차를 분리했습니다.

1.  **원장 가입:** 가입 시 새로운 `Academy`를 생성합니다.
2.  **강사 가입:** 원장이 알려준 `inviteCode`를 입력하여 기존 `Academy`에 소속됩니다.

---

## 4. 개발 시 주의사항 (보안)

멀티 테넌트 환경에서 개발할 때 가장 중요한 것은 **"데이터 유출 방지"**입니다.

### 🚨 절대 금지: `findAll()`
```java
// ❌ 위험! 모든 학원의 학생이 다 조회됨
studentRepository.findAll();
```

### ✅ 올바른 방법: `findAllByAcademy()`
항상 현재 로그인한 사용자의 `academy` 정보를 조건으로 걸어야 합니다.

```java
// ⭕ 안전! 내 학원의 학생만 조회됨
Member currentMember = ...;
studentRepository.findAllByAcademy(currentMember.getAcademy());
```

### 🛡️ 더 안전하게 하려면? (심화)
매번 `where` 조건을 넣는 것은 실수하기 쉽습니다. 이를 방지하기 위해 **Hibernate Filter**나 **AOP**를 사용하여 자동으로 조건을 붙이는 기술을 도입할 수도 있습니다. (추후 고도화 과제)

---

## 5. 결론

멀티 테넌트 아키텍처 도입으로 우리 시스템은 **단일 학원용 프로그램**에서 **수천 개의 학원을 동시에 수용할 수 있는 SaaS 플랫폼**으로 진화했습니다.
이제부터 모든 비즈니스 로직을 짤 때는 항상 **"이 데이터가 어느 학원 것인가?"**를 먼저 생각해야 합니다.
