# Spring Boot MVC 패턴으로 화면 구현하기 (with Thymeleaf & FullCalendar)

> **작성일:** 2024.01.06  
> **프로젝트:** Academy Management  
> **주제:** 서버 사이드 렌더링(SSR)과 클라이언트 사이드 렌더링(CSR)의 조화

---

## 1. MVC 패턴이란?

**MVC (Model-View-Controller)**는 사용자 인터페이스를 개발하기 위한 소프트웨어 디자인 패턴입니다.

*   **Model (데이터):** 애플리케이션의 정보(데이터)를 담는 객체입니다. (예: `Lecture`, `LectureRequest`)
*   **View (화면):** 사용자에게 보여지는 화면입니다. (예: `list.html`, `create.html`)
*   **Controller (제어):** 사용자의 요청을 받아 Model과 View를 연결해주는 중재자입니다. (예: `LectureViewController`)

### 💡 우리 프로젝트에서의 MVC 흐름
1.  **사용자:** 브라우저에서 `/lectures` 주소 입력 (요청)
2.  **Controller:** `LectureViewController`가 요청을 받음
3.  **Model:** (필요하다면) DB에서 데이터를 꺼내 `Model` 객체에 담음
4.  **View:** `lecture/list.html` 템플릿을 찾아 렌더링(HTML 생성) 후 사용자에게 반환

---

## 2. 왜 Thymeleaf인가? (SSR vs CSR)

요즘은 React, Vue 같은 **CSR(Client Side Rendering)**이 유행이지만, 관리자 페이지나 빠르게 개발해야 하는 프로젝트에서는 **SSR(Server Side Rendering)**인 Thymeleaf가 여전히 강력합니다.

| 특징 | Thymeleaf (SSR) | React/Vue (CSR) |
| :--- | :--- | :--- |
| **렌더링 위치** | 서버에서 HTML을 다 만들어서 보냄 | 브라우저에서 자바스크립트로 HTML을 그림 |
| **장점** | **개발 속도가 빠름**, SEO 유리, 설정 간편 | 사용자 경험(UX)이 좋음, 앱처럼 동작 |
| **단점** | 화면 깜빡임이 있음 | 초기 설정 복잡, API 별도 개발 필요 |

**✅ 우리의 선택:**  
빠른 개발과 생산성을 위해 **Thymeleaf**를 메인으로 사용하되, 달력(Calendar) 같이 복잡한 UI는 **JavaScript 라이브러리(FullCalendar)**를 섞어서 사용하는 **하이브리드 방식**을 채택했습니다.

---

## 3. 구현 내용 상세 분석

### 3.1. Controller 분리 전략
API용 컨트롤러와 화면용 컨트롤러를 분리하여 역할(Responsibility)을 명확히 했습니다.

*   **`LectureController` (`@RestController`):** JSON 데이터를 반환 (React, 앱, FullCalendar용)
*   **`LectureViewController` (`@Controller`):** HTML 화면을 반환 (Thymeleaf용)

```java
@Controller
@RequestMapping("/lectures")
public class LectureViewController {
    
    @GetMapping
    public String getLectureList() {
        return "lecture/list"; // templates/lecture/list.html을 찾아감
    }
}
```

### 3.2. View 구현 (Thymeleaf + JS)

#### 📅 FullCalendar 연동 (하이브리드 방식)
Thymeleaf로 HTML 뼈대를 만들고, 실제 데이터는 자바스크립트가 API를 호출해서 채워 넣습니다.

1.  **HTML:** `<div id='calendar'></div>` (빈 공간 마련)
2.  **JS:** `fetch('/api/v1/lecture/events')` (데이터 요청)
3.  **Controller:** `List<LectureEventDto>` 반환 (JSON)

#### 📝 동적 폼 처리 (Dynamic Form)
강의 스케줄을 여러 개 추가하기 위해 자바스크립트로 HTML을 동적으로 생성했습니다.

```javascript
// 버튼 클릭 시 실행
function addSchedule() {
    // 새로운 입력 박스 생성
    // name 속성의 인덱스를 증가시켜야 함 (scheduleRequest[0] -> scheduleRequest[1])
    const newBox = `... <input name="scheduleRequest[${count}].startTime"> ...`;
    container.appendChild(newBox);
}
```

### 3.3. DTO 바인딩 이슈 해결 (`Record` vs `Class`)
`LectureScheduleRequest`를 처음에 Java `record`로 만들었으나, Spring MVC가 폼 데이터를 바인딩할 때 **기본 생성자(NoArgsConstructor)**가 없어서 에러가 발생했습니다.

*   **문제:** `java.lang.NoSuchMethodException: ...<init>()`
*   **해결:** `record`를 일반 `class`로 변경하고 `@Data`, `@NoArgsConstructor` 추가.

---

## 4. 트러블슈팅 (Troubleshooting)

### 🚨 404 Not Found (Whitelabel Error Page)
*   **증상:** 로그는 찍히는데 화면이 안 나옴.
*   **원인:** Gradle 빌드 설정 문제로 `src/main/resources/templates` 파일들이 `build` 폴더로 복사되지 않음.
*   **해결:**
    1.  Gradle 버전을 8.5로 변경 (Java 17 호환성)
    2.  `./gradlew clean build`로 빌드 캐시 초기화

### 🚨 GitHub Push Protection
*   **증상:** `application.yml`에 비밀키가 있어서 푸시가 막힘.
*   **해결:**
    1.  `application-secret.yml` 생성 및 `.gitignore` 등록
    2.  `git commit --amend`로 과거 커밋 기록에서 비밀키 삭제

---

## 5. 결론
이번 실습을 통해 **"백엔드 개발자라도 간단한 화면은 직접 만들 수 있어야 한다"**는 것을 배웠습니다. Spring Boot와 Thymeleaf를 잘 활용하면, 프론트엔드 팀 없이도 훌륭한 관리자 시스템을 구축할 수 있습니다.
