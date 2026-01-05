# 🎓 스프링 핵심 원리 학습 프로젝트

학원 학생 관리 시스템을 통해 스프링의 핵심 원리를 학습하는 예제 프로젝트입니다.

## 📚 프로젝트 소개

이 프로젝트는 **스프링 핵심 원리**를 실전 예제로 이해하기 위해 설계되었습니다. 초등부와 중등부를 관리하는 학원 시스템을 구현하면서 다음의 스프링 핵심 개념들을 학습할 수 있습니다:

- IoC (Inversion of Control) 컨테이너
- DI (Dependency Injection) 의존성 주입
- Bean 생명주기 관리
- AOP (Aspect-Oriented Programming)
- Profile 기반 환경 설정
- Spring Data JPA
- 트랜잭션 관리
- Validation

## 🎯 학습 목표

1. **스프링 컨테이너와 IoC** 이해하기
2. **의존성 주입의 3가지 방법** 비교하기
3. **인터페이스 기반 설계**와 다형성 활용하기
4. **AOP**로 횡단 관심사 분리하기
5. **Profile**로 환경별 설정 관리하기
6. **트랜잭션과 영속성** 이해하기

---

## 📖 학습 순서

### 🔰 1단계: 스프링 부트 시작점 이해하기

**학습 파일**: `src/main/java/com/aslan/academymanagement/AcademyManagementApplication.java`

**핵심 개념**:
- `@SpringBootApplication` 애노테이션의 역할
- 스프링 IoC 컨테이너 시작
- 컴포넌트 스캔 범위

**학습 포인트**:
```java
@SpringBootApplication  // = @Configuration + @EnableAutoConfiguration + @ComponentScan
@EnableJpaAuditing      // JPA Auditing 활성화
public class AcademyManagementApplication {
    public static void main(String[] args) {
        // 여기서 스프링 IoC 컨테이너가 시작됩니다
        SpringApplication.run(AcademyManagementApplication.class, args);
    }
}
```

#### 💡 심화: IoC (제어의 역전)란?

**전통적인 방식 (개발자가 직접 제어)**:
```java
public class StudentController {
    private StudentService service;

    public StudentController() {
        // 개발자가 직접 객체 생성 및 관리
        this.service = new ElementaryStudentService();
    }
}
```

문제점:
- Controller가 구체적인 구현체(`ElementaryStudentService`)에 강하게 결합됨
- 중등부로 변경하려면 코드를 직접 수정해야 함
- 테스트할 때 Mock 객체로 교체하기 어려움

**Spring 방식 (스프링이 제어)**:
```java
@RestController
@RequiredArgsConstructor
public class StudentController {
    private final StudentManagementService service;  // 인터페이스에 의존

    // 생성자는 Lombok이 자동 생성
    // 스프링이 자동으로 적절한 구현체를 찾아서 주입해줌
}
```

#### 🔍 Spring이 Bean을 생성하고 관리하는 과정

1. **애플리케이션 시작**
   ```java
   SpringApplication.run(AcademyManagementApplication.class, args);
   ```

2. **Component Scan 실행**
   - `@SpringBootApplication`에 포함된 `@ComponentScan`이 동작
   - `com.aslan.academymanagement` 패키지 전체를 스캔
   - `@Component`, `@Service`, `@Repository`, `@Controller` 등을 찾음

3. **Bean Definition 생성**
   - 찾은 클래스들의 메타데이터를 수집
   - 어떤 Bean이 어떤 의존성을 필요로 하는지 분석

4. **의존성 그래프 구성**
   ```
   StudentController → StudentManagementService (인터페이스)
                           ↓
                    ElementaryStudentService (Profile: elementary)
                           ↓
                    StudentRepository
   ```

5. **Bean 인스턴스화 및 주입**
   - 의존성이 없는 Bean부터 차례로 생성
   - 생성자에 필요한 Bean을 찾아서 주입
   - IoC 컨테이너에 등록

#### ❓ 왜 `new`로 객체를 만들지 않는가?

스프링이 관리하는 Bean을 사용하는 이유:
1. **싱글톤 관리**: 애플리케이션 전체에서 하나의 인스턴스만 사용 (메모리 효율)
2. **의존성 자동 주입**: 필요한 객체를 자동으로 연결
3. **생명주기 관리**: 초기화(`@PostConstruct`), 소멸(`@PreDestroy`) 자동 처리
4. **AOP 적용**: `@Transactional`, `@Loggable` 등이 동작하려면 스프링이 관리해야 함
5. **테스트 용이성**: Mock 객체로 쉽게 교체 가능

---

### 🔰 2단계: 의존성 주입(DI) 이해하기

**학습 파일**: `src/main/java/com/aslan/academymanagement/controller/StudentController.java`

**핵심 개념**:
- 생성자 주입 (Constructor Injection) - **권장 방식**
- `@RequiredArgsConstructor` (Lombok)
- 인터페이스에 의존하는 설계

**학습 포인트**:
```java
@RestController
@RequiredArgsConstructor  // final 필드에 대한 생성자 자동 생성
public class StudentController {
    // 인터페이스에 의존 (구체 클래스가 아닌)
    private final StudentManagementService studentManagementService;

    // 스프링이 자동으로 적절한 구현체를 주입해줍니다
}
```

#### 💡 심화: 의존성 주입 3가지 방식 비교

##### ① 생성자 주입 (권장 ⭐)
```java
@RestController
public class StudentController {
    private final StudentManagementService service;

    // 생성자가 하나일 때는 @Autowired 생략 가능
    public StudentController(StudentManagementService service) {
        this.service = service;
    }
}
```

**장점**:
- **불변성(Immutability)**: `final` 키워드 사용 가능
- **필수 의존성 보장**: 객체 생성 시점에 모든 의존성이 주입됨
- **순환 참조 방지**: 컴파일 시점에 순환 참조 감지
- **테스트 용이**: `new`로 객체 생성 시에도 의존성 주입 가능

##### ② 필드 주입 (권장하지 않음 ❌)
```java
@RestController
public class StudentController {
    @Autowired
    private StudentManagementService service;  // final 불가능
}
```

**단점**:
- `final` 사용 불가능 → 불변성 보장 안 됨
- 테스트할 때 리플렉션 사용해야 함
- 순환 참조 감지가 런타임에 발생

##### ③ Setter 주입 (선택적 의존성에만 사용)
```java
@RestController
public class StudentController {
    private StudentManagementService service;

    @Autowired
    public void setService(StudentManagementService service) {
        this.service = service;
    }
}
```

#### 🔍 @RequiredArgsConstructor의 마법

Lombok이 컴파일 시점에 다음 코드를 자동 생성:

```java
// 개발자가 작성한 코드
@RestController
@RequiredArgsConstructor
public class StudentController {
    private final StudentManagementService service;
}

// ↓ Lombok이 생성한 코드 (바이트코드로)

@RestController
public class StudentController {
    private final StudentManagementService service;

    public StudentController(StudentManagementService service) {
        this.service = service;
    }
}
```

**실험해보기**:
1. `@RequiredArgsConstructor`를 제거하고 직접 생성자 작성해보기
2. 필드 주입(`@Autowired private ...`)과 비교해보기

---

### 🔰 3단계: 인터페이스 기반 설계와 다형성

**학습 파일**:
- `src/main/java/com/aslan/academymanagement/service/StudentManagementService.java` (인터페이스)
- `src/main/java/com/aslan/academymanagement/service/ElementaryStudentService.java`
- `src/main/java/com/aslan/academymanagement/service/MiddleStudentService.java`

**핵심 개념**:
- 인터페이스로 계약 정의
- 다양한 구현체 제공
- OCP (Open-Closed Principle) - 확장에는 열려있고, 변경에는 닫혀있다

**학습 포인트**:
```java
// 인터페이스 정의
public interface StudentManagementService {
    Student registerStudent(StudentRequest request);
    List<Student> getTopStudents(int limit);
}

// 초등부 구현
@Service
@Profile("elementary")
public class ElementaryStudentService implements StudentManagementService {
    // 초등부 특화 로직: 출석률 80% 이상 검증
}

// 중등부 구현
@Service
@Profile("middle")
public class MiddleStudentService implements StudentManagementService {
    // 중등부 특화 로직: 평균 점수 90점 이상 검증
}
```

#### 💡 심화: OCP (개방-폐쇄 원칙) 실전 적용

**개방-폐쇄 원칙**: 확장에는 열려있고, 변경에는 닫혀있어야 한다.

##### 나쁜 설계 (인터페이스 없이)
```java
@RestController
public class StudentController {
    private final ElementaryStudentService elementaryService;
    private final MiddleStudentService middleService;

    @PostMapping
    public Student register(@RequestBody StudentRequest request) {
        if (request.getDivision() == Division.ELEMENTARY) {
            return elementaryService.register(request);
        } else if (request.getDivision() == Division.MIDDLE) {
            return middleService.register(request);
        }
        // 고등부 추가 시 → Controller 코드 수정 필요! (OCP 위반)
        else if (request.getDivision() == Division.HIGH) {
            return highService.register(request);  // 새로운 필드 추가
        }
    }
}
```

##### 좋은 설계 (인터페이스 기반)
```java
@RestController
@RequiredArgsConstructor
public class StudentController {
    // 인터페이스에 의존
    private final StudentManagementService service;

    @PostMapping
    public Student register(@RequestBody StudentRequest request) {
        // 구현체가 무엇이든 동일한 코드
        return service.register(request);
    }
}

// 새로운 고등부 서비스 추가
@Service
@Profile("high")  // ← 이것만 추가하면 됨!
public class HighStudentService implements StudentManagementService {
    @Override
    public Student registerStudent(StudentRequest request) {
        // 고등부 특화 로직: 내신 등급 검증 등
    }
}
```

**고등부 추가 시 변경사항**:
1. `HighStudentService.java` 생성 (새 파일 추가)
2. `application-high.yml` 생성 (새 설정 파일)
3. `Division` enum에 `HIGH` 추가

**기존 코드는 전혀 수정하지 않음** → OCP 원칙 준수!

#### 🔍 Profile에 따른 Bean 선택 메커니즘

```yaml
# application.yml
spring:
  profiles:
    active: elementary
```

Spring 동작 과정:
1. `active: elementary` 읽음
2. Component Scan 시 `@Profile` 확인
3. `@Profile("elementary")`인 Bean만 활성화
4. `StudentManagementService` 타입의 Bean 검색 → `ElementaryStudentService` 발견
5. Controller에 주입

Profile 변경 시:
```yaml
active: middle  # 변경
```
→ `MiddleStudentService`가 주입됨 (코드 수정 없이!)

**이해해야 할 것**:
- Controller는 어떤 구현체를 사용하는지 알 필요가 없다
- Profile에 따라 다른 구현체가 주입된다
- 새로운 부서(고등부)를 추가할 때 기존 코드 수정이 최소화된다

---

### 🔰 4단계: Profile을 활용한 환경별 설정

**학습 파일**:
- `src/main/resources/application.yml`
- `src/main/resources/application-elementary.yml`
- `src/main/resources/application-middle.yml`

**핵심 개념**:
- `@Profile` 애노테이션
- 환경별 Bean 활성화
- 설정 파일 분리

**학습 포인트**:
```yaml
# application.yml
spring:
  profiles:
    active: elementary  # 여기를 middle로 변경하면 전체 동작이 바뀝니다!
```

**실험해보기**:
1. Profile을 `middle`로 변경 후 재시작
2. `/debug/profiles` 엔드포인트로 활성 프로필 확인
3. `/debug/beans` 엔드포인트로 주입된 Bean 확인
4. `POST /api/students`로 학생 등록 시 검증 로직이 달라지는지 확인

---

### 🔰 5단계: Configuration과 Bean 수동 등록

**학습 파일**: `src/main/java/com/aslan/academymanagement/config/SwaggerConfig.java`

**핵심 개념**:
- `@Configuration` 클래스
- `@Bean` 메서드로 수동 Bean 등록
- 외부 라이브러리 Bean 등록

**학습 포인트**:
```java
@Configuration  // 설정 클래스임을 표시
public class SwaggerConfig {

    @Bean  // 이 메서드의 리턴 객체를 스프링 컨테이너에 등록
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("학원 학생 관리 시스템 API")
                .version("v1.0.0"));
    }
}
```

**이해해야 할 것**:
- `@Component`, `@Service` 등으로 자동 등록 vs `@Bean`으로 수동 등록
- 언제 수동 등록을 사용하는가? (외부 라이브러리, 복잡한 초기화 로직)

---

### 🔰 6단계: AOP (Aspect-Oriented Programming)

**학습 파일**:
- `src/main/java/com/aslan/academymanagement/aspect/LoggingAspect.java` (기본 AOP)
- `src/main/java/com/aslan/academymanagement/aspect/PerformanceAspect.java` (DI 활용)
- `src/main/java/com/aslan/academymanagement/aspect/AttendanceCheckAspect.java` (다양한 Advice)
- `src/main/java/com/aslan/academymanagement/annotation/` (커스텀 애노테이션)

**핵심 개념**:
- 횡단 관심사(Cross-cutting Concerns) 분리
- Aspect, Advice, Pointcut
- 커스텀 애노테이션 활용

#### 💡 심화: 횡단 관심사(Cross-cutting Concerns)란?

**핵심 관심사**: 비즈니스 로직
```java
public Student registerStudent(StudentRequest request) {
    Student student = repository.save(...);
    return student;
}
```

**횡단 관심사**: 여러 메서드에서 공통으로 필요한 부가 기능
- 로깅
- 성능 측정
- 트랜잭션 관리
- 보안 검사
- 캐싱

##### AOP 없이 구현하면?

```java
public Student registerStudent(StudentRequest request) {
    // 로깅 (횡단 관심사)
    log.info("메서드 시작: registerStudent");
    long startTime = System.currentTimeMillis();

    // 보안 검사 (횡단 관심사)
    if (!securityService.hasPermission()) {
        throw new SecurityException();
    }

    // 트랜잭션 시작 (횡단 관심사)
    Transaction tx = beginTransaction();

    try {
        // ===== 진짜 비즈니스 로직 (핵심 관심사) =====
        Student student = repository.save(...);
        // ========================================

        // 트랜잭션 커밋 (횡단 관심사)
        tx.commit();

        // 성능 측정 (횡단 관심사)
        long endTime = System.currentTimeMillis();
        log.info("실행 시간: {}ms", endTime - startTime);

        return student;
    } catch (Exception e) {
        // 트랜잭션 롤백 (횡단 관심사)
        tx.rollback();
        throw e;
    }
}
```

**문제점**:
- 비즈니스 로직이 부가 기능에 묻혀버림
- 모든 메서드에 동일한 코드 중복
- 유지보수 어려움 (로깅 형식 변경 시 모든 메서드 수정)

##### AOP로 해결

```java
// Service - 비즈니스 로직만 집중
@Transactional  // AOP: 트랜잭션 관리
@Loggable       // AOP: 로깅 및 성능 측정
public Student registerStudent(StudentRequest request) {
    Student student = repository.save(...);
    return student;  // 깔끔!
}

// Aspect - 부가 기능 분리
@Aspect
@Component
public class LoggingAspect {
    @Around("@annotation(Loggable)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("메서드 시작: {}", joinPoint.getSignature().getName());
        long startTime = System.currentTimeMillis();

        Object result = joinPoint.proceed();  // 실제 메서드 실행

        long endTime = System.currentTimeMillis();
        log.info("실행 시간: {}ms", endTime - startTime);
        return result;
    }
}
```

#### 🔍 AOP 동작 원리 (프록시 패턴)

Spring은 런타임에 **프록시 객체**를 생성:

```java
// 실제로 주입되는 것은 프록시
StudentManagementService service = applicationContext.getBean(...);

// service는 다음과 같은 프록시 객체:
class StudentServiceProxy implements StudentManagementService {
    private StudentManagementService target;  // 실제 객체
    private LoggingAspect loggingAspect;

    public Student registerStudent(StudentRequest request) {
        // @Around Advice 실행
        return loggingAspect.logExecutionTime(() -> {
            // 실제 메서드 호출
            return target.registerStudent(request);
        });
    }
}
```

**실행 흐름**:
```
Controller.registerStudent()
    ↓
프록시.registerStudent()  ← Controller가 호출하는 것은 프록시
    ↓
LoggingAspect.logExecutionTime() 시작
    ↓
실제객체.registerStudent()  ← joinPoint.proceed()
    ↓
LoggingAspect.logExecutionTime() 종료
    ↓
Controller로 리턴
```

**학습 포인트**:

#### 6-1. 로깅 Aspect (`@Around`)
```java
@Aspect
@Component
public class LoggingAspect {

    @Around("@annotation(com.aslan.academymanagement.annotation.Loggable)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        Object result = joinPoint.proceed();  // 실제 메서드 실행

        long endTime = System.currentTimeMillis();
        // 로깅 로직
        return result;
    }
}
```

#### 6-2. 성능 모니터링 Aspect (DI 활용)
```java
@Aspect
@Component
@RequiredArgsConstructor  // Aspect도 DI를 받을 수 있다!
public class PerformanceAspect {
    private final NotificationService notificationService;

    @Around("@annotation(com.aslan.academymanagement.annotation.Monitored)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        // 성능 측정 및 알림 서비스 활용
    }
}
```

#### 6-3. 출석 체크 Aspect (`@Before`, `@AfterReturning`)
```java
@Aspect
@Component
public class AttendanceCheckAspect {

    @Before("@annotation(com.aslan.academymanagement.annotation.AttendanceRequired)")
    public void checkAttendance(JoinPoint joinPoint) {
        // 메서드 실행 전 출석 확인
    }

    @AfterReturning(value = "...", returning = "result")
    public void recordAttendance(JoinPoint joinPoint, Object result) {
        // 메서드 실행 후 출석 기록
    }
}
```

#### 📝 Advice 종류별 실행 시점

```java
@Aspect
@Component
public class ExampleAspect {

    @Before("execution(* com.aslan..*(..))")
    public void before(JoinPoint jp) {
        // 메서드 실행 전
    }

    @After("execution(* com.aslan..*(..))")
    public void after(JoinPoint jp) {
        // 메서드 실행 후 (예외 발생해도 실행)
    }

    @AfterReturning(value = "execution(* com.aslan..*(..))", returning = "result")
    public void afterReturning(JoinPoint jp, Object result) {
        // 메서드 정상 완료 후
    }

    @AfterThrowing(value = "execution(* com.aslan..*(..))", throwing = "ex")
    public void afterThrowing(JoinPoint jp, Exception ex) {
        // 예외 발생 시
    }

    @Around("execution(* com.aslan..*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        // 전 처리
        Object result = pjp.proceed();  // 메서드 실행 (생략 가능!)
        // 후 처리
        return result;
    }
}
```

**실행 순서**:
```
@Around 시작
  ↓
@Before
  ↓
실제 메서드 실행
  ↓
@AfterReturning (정상 완료 시) 또는 @AfterThrowing (예외 발생 시)
  ↓
@After
  ↓
@Around 종료
```

**실험해보기**:
1. Service 메서드에 `@Loggable` 추가 후 로그 확인
2. `@Monitored` 추가 후 성능 측정 확인
3. `/api/students/{studentId}/attendance` 엔드포인트 호출 시 AOP 동작 확인
4. Aspect의 `log.info()` 출력을 통해 AOP가 언제 실행되는지 관찰

---

### 🔰 7단계: Repository와 Spring Data JPA

**학습 파일**: `src/main/java/com/aslan/academymanagement/repository/StudentRepository.java`

**핵심 개념**:
- `JpaRepository` 인터페이스 상속
- 메서드 이름 기반 쿼리 자동 생성
- `@Query`로 커스텀 쿼리 작성
- Spring Data JPA의 마법

**학습 포인트**:
```java
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // 메서드 이름만으로 쿼리 자동 생성!
    Optional<Student> findByStudentId(String studentId);
    List<Student> findByDivision(Division division);

    // 복잡한 쿼리는 @Query 사용
    @Query("SELECT s FROM Student s WHERE s.division = :division AND s.averageScore >= :minScore")
    List<Student> findHighAchievers(@Param("division") Division division, @Param("minScore") Double minScore);
}
```

#### 💡 심화: 인터페이스만으로 어떻게 동작하는가?

```java
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentId(String studentId);
}
```

**의문**: "구현체를 안 만들었는데 어떻게 동작하지?"

**Spring Data JPA의 동작**:

1. **애플리케이션 시작 시**
   - `JpaRepository`를 상속한 인터페이스 감지
   - **런타임에 프록시 구현체 자동 생성**

2. **생성되는 프록시 코드 (개념적)**:
```java
class StudentRepositoryImpl implements StudentRepository {
    private EntityManager em;

    @Override
    public Optional<Student> findByStudentId(String studentId) {
        // 메서드 이름 파싱: findBy + StudentId
        // → WHERE student_id = :studentId

        String jpql = "SELECT s FROM Student s WHERE s.studentId = :studentId";
        TypedQuery<Student> query = em.createQuery(jpql, Student.class);
        query.setParameter("studentId", studentId);

        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Student save(Student entity) {
        if (entity.getId() == null) {
            em.persist(entity);  // INSERT
            return entity;
        } else {
            return em.merge(entity);  // UPDATE
        }
    }

    // 기타 메서드들도 자동 구현...
}
```

#### 📝 메서드 이름 파싱 규칙

| 메서드 이름 | 생성되는 JPQL |
|------------|--------------|
| `findByName(String name)` | `WHERE name = ?` |
| `findByNameAndGrade(String name, Grade grade)` | `WHERE name = ? AND grade = ?` |
| `findByAverageScoreGreaterThan(Double score)` | `WHERE averageScore > ?` |
| `findByDivisionOrderByAverageScoreDesc(Division div)` | `WHERE division = ? ORDER BY averageScore DESC` |
| `countByGrade(Grade grade)` | `SELECT COUNT(*) WHERE grade = ?` |
| `existsByStudentId(String studentId)` | `SELECT COUNT(*) > 0 WHERE studentId = ?` |

#### 📝 @Query 사용 시기

메서드 이름이 너무 길어지거나 복잡한 조건일 때:

```java
// 메서드 이름으로는 복잡함
@Query("SELECT s FROM Student s WHERE s.division = :division AND s.averageScore >= :minScore ORDER BY s.averageScore DESC")
List<Student> findHighAchievers(@Param("division") Division division, @Param("minScore") Double minScore);

// 네이티브 SQL 사용 (특정 DB 기능 활용)
@Query(value = "SELECT * FROM students WHERE YEAR(created_at) = :year", nativeQuery = true)
List<Student> findByYear(@Param("year") int year);
```

**이해해야 할 것**:
- 인터페이스만 정의했는데 어떻게 동작하는가?
- 스프링이 런타임에 구현체를 동적으로 생성한다 (프록시 패턴)
- 메서드 이름 규칙 (`findBy`, `countBy`, `existsBy` 등)

---

### 🔰 8단계: Entity와 JPA 매핑

**학습 파일**: `src/main/java/com/aslan/academymanagement/domain/Student.java`

**핵심 개념**:
- `@Entity`와 테이블 매핑
- 기본키 전략 (`@Id`, `@GeneratedValue`)
- Auditing (`@CreatedDate`, `@LastModifiedDate`)
- Enum 타입 매핑

**학습 포인트**:
```java
@Entity
@Table(name = "students")
@EntityListeners(AuditingEntityListener.class)  // 자동으로 생성/수정 시간 기록
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String studentId;

    @Enumerated(EnumType.STRING)
    private Division division;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

#### 💡 심화: Entity Auditing 자동화

```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Student {
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**동작 과정**:

1. **JPA 이벤트 리스너 등록**
   - `@EntityListeners(AuditingEntityListener.class)` 설정

2. **JPA Lifecycle Callback**
   ```
   @PrePersist  → INSERT 직전 호출
   @PreUpdate   → UPDATE 직전 호출
   @PostLoad    → SELECT 직후 호출
   등...
   ```

3. **Auditing 리스너 동작** (개념적):
```java
// Spring이 제공하는 AuditingEntityListener (개념적)
public class AuditingEntityListener {

    @PrePersist
    public void setCreatedDate(Object entity) {
        // @CreatedDate 필드 찾기
        Field createdDateField = findField(entity, CreatedDate.class);
        // 현재 시간 설정
        createdDateField.set(entity, LocalDateTime.now());
    }

    @PreUpdate
    public void setLastModifiedDate(Object entity) {
        // @LastModifiedDate 필드 찾기
        Field lastModifiedField = findField(entity, LastModifiedDate.class);
        // 현재 시간 설정
        lastModifiedField.set(entity, LocalDateTime.now());
    }
}
```

**실행 흐름**:
```
repository.save(student)
    ↓
@PrePersist 이벤트 발생
    ↓
AuditingEntityListener.setCreatedDate() 실행
    ↓
student.createdAt = LocalDateTime.now() 자동 설정
    ↓
실제 INSERT 쿼리 실행
```

**중요**: 이 기능이 작동하려면 `AcademyManagementApplication`에 `@EnableJpaAuditing` 필요!

**이해해야 할 것**:
- `@Entity`는 어떻게 테이블과 매핑되는가?
- Auditing 기능으로 생성/수정 시간 자동 관리
- Enum을 `STRING`으로 저장하는 이유 (순서 변경에 안전)

---

### 🔰 9단계: 트랜잭션 관리

**학습 파일**: `ElementaryStudentService.java`, `MiddleStudentService.java`

**핵심 개념**:
- `@Transactional` 애노테이션
- 트랜잭션 전파(Propagation)
- 읽기 전용 최적화 (`readOnly = true`)
- 롤백 처리

**학습 포인트**:
```java
@Service
public class ElementaryStudentService implements StudentManagementService {

    @Transactional  // 쓰기 작업: 트랜잭션 필요
    @Loggable
    public Student registerStudent(StudentRequest request) {
        // 여러 DB 작업이 하나의 트랜잭션으로 묶임
        // 중간에 예외 발생 시 모두 롤백
        Student student = repository.save(student);
        return student;
    }

    @Transactional(readOnly = true)  // 읽기 전용: 성능 최적화
    public Student getStudent(String studentId) {
        return repository.findByStudentId(studentId)
            .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다"));
    }
}
```

#### 💡 심화: @Transactional의 내부 동작

```java
@Service
public class ElementaryStudentService {

    @Transactional
    public Student registerStudent(StudentRequest request) {
        Student student = repository.save(student);  // INSERT
        // 예외 발생 시 롤백!
        return student;
    }
}
```

**Spring이 생성하는 프록시**:
```java
class ElementaryStudentServiceProxy {
    private ElementaryStudentService target;
    private TransactionManager txManager;

    public Student registerStudent(StudentRequest request) {
        TransactionStatus tx = txManager.getTransaction();
        try {
            Student result = target.registerStudent(request);
            txManager.commit(tx);  // 정상 완료 시 커밋
            return result;
        } catch (RuntimeException e) {
            txManager.rollback(tx);  // 런타임 예외 발생 시 롤백
            throw e;
        }
    }
}
```

#### 🔍 트랜잭션이 필요한 이유

**시나리오**: 학생 등록 + 출석 기록 추가

```java
// 트랜잭션 없이
public Student registerStudent(StudentRequest request) {
    Student student = studentRepository.save(student);  // 1. DB 저장 성공

    attendanceRepository.save(attendance);  // 2. 여기서 예외 발생!
    // → 학생은 DB에 저장되었지만 출석 기록은 없는 불일치 상태!
}

// 트랜잭션 사용
@Transactional
public Student registerStudent(StudentRequest request) {
    Student student = studentRepository.save(student);  // 1. 저장 (임시)

    attendanceRepository.save(attendance);  // 2. 예외 발생!
    // → 둘 다 롤백되어 DB 일관성 유지!
}
```

#### 📝 readOnly = true의 성능 최적화

```java
@Transactional(readOnly = true)
public Student getStudent(String studentId) {
    return repository.findByStudentId(studentId)
        .orElseThrow();
}
```

**최적화 효과**:
1. **Flush 모드 변경**: 변경 감지(Dirty Checking) 비활성화
2. **DB 최적화**: 읽기 전용 트랜잭션으로 DB에 힌트 제공
3. **Slave DB 라우팅**: Master/Slave 구조에서 Slave로 라우팅 가능

**실험해보기**:
1. Service 메서드 중간에 예외를 던져보고 롤백 확인
2. `@Transactional` 제거 후 동작 차이 확인

---

### 🔰 10단계: Validation과 DTO

**학습 파일**:
- `src/main/java/com/aslan/academymanagement/dto/StudentRequest.java`
- `src/main/java/com/aslan/academymanagement/dto/StudentResponse.java`

**핵심 개념**:
- DTO (Data Transfer Object) 패턴
- Jakarta Validation
- Entity와 DTO 분리 이유

**학습 포인트**:
```java
@Data
public class StudentRequest {

    @NotBlank(message = "학생 번호는 필수입니다")
    @Pattern(regexp = "^(ES|MS)\\d{3}$", message = "학생 번호 형식: ES001 또는 MS001")
    private String studentId;

    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 50, message = "이름은 2-50자 이내여야 합니다")
    private String name;

    @NotNull(message = "학년은 필수입니다")
    private Grade grade;

    @Min(value = 0, message = "점수는 0 이상이어야 합니다")
    @Max(value = 100, message = "점수는 100 이하여야 합니다")
    private Double averageScore;
}
```

**Controller에서 사용**:
```java
@PostMapping
public ResponseEntity<StudentResponse> registerStudent(@Valid @RequestBody StudentRequest request) {
    // @Valid가 자동으로 검증 수행
}
```

**이해해야 할 것**:
- Entity를 직접 Controller에 노출하지 않는 이유
- 선언적 검증의 장점
- 계층 간 데이터 변환

---

### 🔰 11단계: 디버그 기능으로 스프링 내부 확인하기

**학습 파일**: `src/main/java/com/aslan/academymanagement/controller/DebugController.java`

**핵심 개념**:
- `ApplicationContext`로 Bean 조회
- 런타임에 어떤 Bean이 등록되었는지 확인
- Profile 정보 확인

**학습 포인트**:
```java
@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {
    private final ApplicationContext applicationContext;

    @GetMapping("/beans")
    public Map<String, Object> getBeanInfo() {
        // 등록된 모든 Bean 정보 조회
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        // StudentManagementService의 실제 구현체 확인
        StudentManagementService service = applicationContext.getBean(StudentManagementService.class);
    }
}
```

**실험해보기**:
1. 서버 시작 후 `http://localhost:8080/debug/beans` 접속
2. `studentManagementServiceImpl` 항목에서 어떤 클래스가 주입되었는지 확인
3. Profile을 변경한 후 다시 확인하여 다른 구현체가 주입되는지 확인

---

## 🚀 실습 가이드

### 1️⃣ 프로젝트 실행

```bash
# 초등부 프로필로 실행 (기본)
./gradlew bootRun

# 중등부 프로필로 실행
./gradlew bootRun --args='--spring.profiles.active=middle'
```

### 2️⃣ H2 데이터베이스 콘솔 접속

브라우저에서 `http://localhost:8080/h2-console` 접속

- JDBC URL: `jdbc:h2:mem:academydb`
- Username: `sa`
- Password: (비어있음)

### 3️⃣ Swagger UI로 API 테스트

브라우저에서 `http://localhost:8080/swagger-ui/index.html` 접속

### 4️⃣ API 테스트 시나리오

#### 시나리오 1: 초등부 학생 등록 (Profile: elementary)

```bash
# 1. 디버그: 현재 프로필 확인
curl http://localhost:8080/debug/profiles

# 2. 디버그: 주입된 Bean 확인
curl http://localhost:8080/debug/beans

# 3. 학생 등록 (초등부는 출석률 검증)
curl -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": "ES001",
    "name": "홍길동",
    "grade": "FIRST",
    "division": "ELEMENTARY",
    "averageScore": 85.5,
    "attendanceRate": 90.0
  }'

# 4. 출석 체크 (AOP 동작 확인)
curl -X POST http://localhost:8080/api/students/ES001/attendance

# 5. 우수 학생 조회 (초등부 기준: 출석률 80% 이상)
curl http://localhost:8080/api/students/top?limit=5

# 6. 로그 확인
# - @Loggable에 의한 실행 시간 로그
# - @Monitored에 의한 성능 모니터링 로그
# - @AttendanceRequired에 의한 출석 체크 로그
```

#### 시나리오 2: 중등부 학생 등록 (Profile: middle)

```bash
# application.yml에서 active profile을 middle로 변경 후 재시작

# 1. 학생 등록 (중등부는 평균 점수 검증)
curl -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": "MS001",
    "name": "김철수",
    "grade": "SEVENTH",
    "division": "MIDDLE",
    "averageScore": 95.0,
    "attendanceRate": 85.0
  }'

# 2. 우수 학생 조회 (중등부 기준: 평균 90점 이상)
curl http://localhost:8080/api/students/top?limit=5
```

#### 시나리오 3: Validation 테스트

```bash
# 잘못된 학생 번호 형식 (ES 또는 MS로 시작 + 3자리 숫자)
curl -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": "INVALID",
    "name": "테스트",
    "grade": "FIRST",
    "division": "ELEMENTARY",
    "averageScore": 85.5,
    "attendanceRate": 90.0
  }'

# 응답: 400 Bad Request + 검증 에러 메시지
```

---

## 📊 학습 체크리스트

### IoC와 DI
- [ ] `@SpringBootApplication`이 무엇을 하는지 설명할 수 있다
- [ ] IoC 컨테이너가 Bean을 관리하는 방식을 이해했다
- [ ] 생성자 주입이 권장되는 이유를 설명할 수 있다
- [ ] `@RequiredArgsConstructor`의 동작 방식을 이해했다
- [ ] 생성자 주입, 필드 주입, Setter 주입의 차이를 안다

### Bean과 Component Scan
- [ ] `@Component`, `@Service`, `@Repository`의 차이를 안다
- [ ] `@Configuration`과 `@Bean`의 사용 시점을 안다
- [ ] Component Scan 범위를 이해했다

### 인터페이스 기반 설계
- [ ] 인터페이스에 의존하는 이유를 설명할 수 있다
- [ ] Profile에 따라 다른 구현체가 주입되는 원리를 이해했다
- [ ] OCP 원칙을 적용한 설계를 할 수 있다

### AOP
- [ ] AOP가 해결하는 문제(횡단 관심사)를 이해했다
- [ ] `@Aspect`, `@Around`, `@Before`, `@AfterReturning`의 차이를 안다
- [ ] 커스텀 애노테이션을 만들고 AOP를 적용할 수 있다
- [ ] Aspect에서도 DI를 사용할 수 있음을 이해했다
- [ ] 프록시 패턴으로 AOP가 동작하는 원리를 안다

### Spring Data JPA
- [ ] `JpaRepository`의 기능을 설명할 수 있다
- [ ] 메서드 이름 기반 쿼리 생성 규칙을 안다
- [ ] `@Query`로 커스텀 쿼리를 작성할 수 있다
- [ ] Spring Data JPA가 프록시로 구현체를 생성하는 원리를 안다

### Transaction
- [ ] `@Transactional`의 역할을 이해했다
- [ ] `readOnly = true`의 의미를 안다
- [ ] 트랜잭션 롤백 원리를 이해했다
- [ ] 트랜잭션이 프록시로 동작하는 원리를 안다

### Validation
- [ ] Jakarta Validation 애노테이션을 활용할 수 있다
- [ ] Entity와 DTO를 분리하는 이유를 안다

### JPA Auditing
- [ ] `@CreatedDate`, `@LastModifiedDate`의 동작 원리를 안다
- [ ] `@EntityListeners`와 `@EnableJpaAuditing`의 관계를 이해했다

---

## 🎓 심화 학습 주제

### 1. Bean 생명주기
- `@PostConstruct`, `@PreDestroy`
- `InitializingBean`, `DisposableBean` 인터페이스
- Bean Scope (Singleton, Prototype 등)

### 2. 고급 AOP
- Pointcut 표현식 작성
- `@annotation`, `execution`, `within` 등의 designator
- AOP 실행 순서 제어 (`@Order`)

### 3. 프로필 고급 활용
- 다중 프로필 활성화
- 프로필 그룹화
- `@Profile` 표현식

### 4. 트랜잭션 고급
- 트랜잭션 전파 레벨 (REQUIRED, REQUIRES_NEW 등)
- 격리 수준 (Isolation Level)
- 트랜잭션 롤백 조건 커스터마이징

---

## 🔗 참고 자료

- [Spring Framework 공식 문서](https://docs.spring.io/spring-framework/reference/)
- [Spring Boot 공식 문서](https://docs.spring.io/spring-boot/reference/)
- [Spring Data JPA 공식 문서](https://docs.spring.io/spring-data/jpa/reference/)

---

## 💡 추천 학습 방법

1. **코드를 직접 실행하며 학습**: 각 단계의 파일을 읽고, 서버를 실행하여 동작을 확인하세요
2. **디버거 활용**: IntelliJ의 디버거로 Bean 주입 과정을 단계별로 관찰하세요
3. **로그 분석**: `@Loggable` 등의 로그를 통해 AOP 동작 시점을 확인하세요
4. **코드 수정 실험**: Profile 변경, 새로운 Aspect 추가 등 직접 수정해보세요
5. **테스트 작성**: 학습한 내용을 바탕으로 테스트 코드를 작성해보세요

---

## 📂 프로젝트 구조

```
academy-management/
├── src/main/java/com/aslan/academymanagement/
│   ├── AcademyManagementApplication.java          # 1단계: 스프링 부트 시작점
│   ├── controller/
│   │   ├── StudentController.java                 # 2단계: DI 활용
│   │   └── DebugController.java                   # 11단계: 디버깅
│   ├── service/
│   │   ├── StudentManagementService.java          # 3단계: 인터페이스
│   │   ├── ElementaryStudentService.java          # 4단계: Profile (초등부)
│   │   ├── MiddleStudentService.java              # 4단계: Profile (중등부)
│   │   ├── NotificationService.java               # 인터페이스
│   │   └── NotificationServiceImpl.java           # 구현체
│   ├── repository/
│   │   └── StudentRepository.java                 # 7단계: Spring Data JPA
│   ├── domain/
│   │   ├── Student.java                           # 8단계: JPA Entity
│   │   └── enums/
│   │       ├── Division.java
│   │       └── Grade.java
│   ├── dto/
│   │   ├── StudentRequest.java                    # 10단계: Validation
│   │   └── StudentResponse.java                   # 10단계: DTO
│   ├── config/
│   │   └── SwaggerConfig.java                     # 5단계: Configuration
│   ├── aspect/
│   │   ├── LoggingAspect.java                     # 6단계: AOP (@Around)
│   │   ├── PerformanceAspect.java                 # 6단계: AOP + DI
│   │   └── AttendanceCheckAspect.java             # 6단계: AOP (@Before/@After)
│   └── annotation/
│       ├── Loggable.java                          # 6단계: 커스텀 애노테이션
│       ├── Monitored.java
│       └── AttendanceRequired.java
├── src/main/resources/
│   ├── application.yml                            # 4단계: 메인 설정
│   ├── application-elementary.yml                 # 4단계: 초등부 설정
│   └── application-middle.yml                     # 4단계: 중등부 설정
└── build.gradle                                   # Gradle 설정
```

---

## 📝 핵심 정리

### Spring의 마법은 모두 프록시 패턴과 리플렉션!

1. **IoC**: 객체 생명주기를 스프링이 관리 → 개발자는 비즈니스 로직에 집중
2. **DI**: 생성자 주입으로 의존성 자동 연결 → 코드 결합도 감소
3. **인터페이스 설계**: Profile로 구현체 교체 → 확장성 증가
4. **AOP**: 프록시 패턴으로 횡단 관심사 분리 → 코드 중복 제거
5. **Spring Data JPA**: 프록시로 Repository 구현체 자동 생성 → 반복 코드 제거
6. **트랜잭션**: 프록시로 자동 커밋/롤백 → 데이터 일관성 보장
7. **Auditing**: JPA 이벤트로 생성/수정 시간 자동 관리 → 편의성 증대

---

**Happy Learning! 🚀**

질문이나 피드백이 있다면 프로젝트 이슈에 남겨주세요.
