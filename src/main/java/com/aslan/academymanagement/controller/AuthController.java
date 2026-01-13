package com.aslan.academymanagement.controller;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.enums.MemberStatus;
import com.aslan.academymanagement.dto.auth.SignupRequest;
import com.aslan.academymanagement.repository.MemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "인증 및 회원가입 API")
@RequiredArgsConstructor
public class AuthController {

    private final MemberRepository memberRepository;

    @PostMapping("/signup")
    @Operation(summary = "회원가입 신청", description = "구글 로그인 후 추가 정보를 입력하여 가입을 신청합니다.")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {

        // 이미 가입된 이메일인지 확인
        if (memberRepository.findByGoogleEmail(request.getGoogleEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 가입된 이메일입니다.");
        }

        // 회원 생성 (상태: PENDING)
        Member member = Member.builder()
                .googleEmail(request.getGoogleEmail())
                .name(request.getName())
                .phone(request.getPhone())
                .contactEmail(request.getContactEmail())
                .role(request.getRole())
                .status(MemberStatus.PENDING) // 승인 대기 상태
                .provider("google")
                .build();

        memberRepository.save(member);

        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 신청이 완료되었습니다. 원장님의 승인을 기다려주세요.");
    }
}
