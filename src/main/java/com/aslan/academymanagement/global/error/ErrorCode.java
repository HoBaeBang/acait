package com.aslan.academymanagement.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "엔티티를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 내부 오류가 발생했습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C005", "잘못된 타입입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C006", "접근 권한이 없습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "회원을 찾을 수 없습니다."),
    EMAIL_DUPLICATION(HttpStatus.CONFLICT, "M002", "이미 존재하는 이메일입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "M003", "로그인에 실패했습니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증 토큰이 없거나 유효하지 않습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "만료된 토큰입니다."),

    // Academy
    ACADEMY_NOT_FOUND(HttpStatus.NOT_FOUND, "AC001", "학원을 찾을 수 없습니다."),
    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "AC002", "유효하지 않은 초대 코드입니다."),
    PLAN_LIMIT_EXCEEDED(HttpStatus.FORBIDDEN, "AC003", "학원 인원 제한을 초과했습니다."),

    // Student
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "학생을 찾을 수 없습니다."),
    DUPLICATE_STUDENT(HttpStatus.CONFLICT, "S002", "이미 등록된 학생입니다."),

    // Lecture
    LECTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "L001", "강의를 찾을 수 없습니다."),

    // Settlement
    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ST001", "정산 정보를 찾을 수 없습니다."),
    ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "ST002", "이미 마감된 정산입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
