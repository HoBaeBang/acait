package com.aslan.academymanagement.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender emailSender;

    @Override
    public void notifyParent(String contactInfo, String message) {
        // 전화번호 대신 이메일로 발송한다고 가정 (실제로는 SMS API 연동 필요)
        // 여기서는 contactInfo가 이메일 형식인지 체크하거나, 별도 이메일 필드를 사용해야 함.
        // 현재 Student 엔티티에 parentEmail 필드가 있으므로, 호출하는 쪽에서 이메일을 넘겨줘야 함.

        if (isValidEmail(contactInfo)) {
            sendEmail(contactInfo, "[ACAIT] 학부모 알림", message);
        } else {
            log.warn("⚠️ 유효하지 않은 이메일 주소입니다. (SMS 발송은 미지원): {}", contactInfo);
        }
    }

    @Override
    public void notifyStudent(String contactInfo, String message) {
        if (isValidEmail(contactInfo)) {
            sendEmail(contactInfo, "[ACAIT] 학생 알림", message);
        } else {
            log.warn("⚠️ 유효하지 않은 이메일 주소입니다.: {}", contactInfo);
        }
    }

    @Override
    public void notifyTeacher(String message) {
        // 강사 알림은 보통 시스템 내부 알림이나 앱 푸시로 처리하지만, 여기서는 로그만 남김
        log.info("👨‍🏫 [선생님 알림] {}", message);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
            log.info("📧 이메일 발송 성공: {} -> {}", to, subject);
        } catch (Exception e) {
            log.error("❌ 이메일 발송 실패: {}", e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }
}
