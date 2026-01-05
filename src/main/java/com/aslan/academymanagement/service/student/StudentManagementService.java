package com.aslan.academymanagement.service.student;

import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.dto.StudentRequest;

import java.util.List;

/**
 * 학생 관리 서비스 인터페이스
 *
 * ========================================
 * Spring DI (Dependency Injection) 설명
 * ========================================
 * DI는 "의존성 주입"을 의미합니다.
 * 객체가 필요로 하는 의존성을 외부에서 주입받는 패턴입니다.
 *
 * 인터페이스 기반 DI의 장점:
 * 1. 느슨한 결합(Loose Coupling): 구현체가 변경되어도 인터페이스를 사용하는 코드는 변경 불필요
 * 2. 다형성 활용: 하나의 인터페이스로 여러 구현체를 사용 가능
 * 3. 테스트 용이성: Mock 객체로 쉽게 대체 가능
 * 4. @Profile을 통한 환경별 구현체 선택 가능
 *
 * 이 인터페이스의 구현체:
 * - ElementaryStudentService (@Profile("elementary")): 초등부 학생 관리
 * - MiddleStudentService (@Profile("middle")): 중등부 학생 관리
 *
 * Spring은 활성화된 Profile에 따라 적절한 구현체를 자동으로 주입합니다.
 */
public interface StudentManagementService {

    /**
     * 학생 등록
     * - 초등부: 학부모 연락처 필수
     * - 중등부: 학생 본인 연락처 필수
     */
    Student registerStudent(StudentRequest request);

    /**
     * 학생 정보 조회
     */
    Student getStudent(String studentId);

    /**
     * 학생 정보 수정
     */
    Student updateStudent(String studentId, StudentRequest request);

    /**
     * 출석 체크
     * - @AttendanceRequired AOP 적용
     */
    void checkAttendance(String studentId);

    /**
     * 성적 입력
     */
    void updateScore(String studentId, Double score);

    /**
     * 우수 학생 조회
     * - 초등부: 출석 80일 이상
     * - 중등부: 평균 90점 이상
     */
    List<Student> getTopStudents();

    /**
     * 현재 부서 타입 반환
     */
    String getDivisionType();
}
