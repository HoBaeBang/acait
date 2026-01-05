package com.aslan.academymanagement.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {
    @Override
    public void notifyParent(String phoneNumber, String message) {
        log.info("📱 [학부모 알림] {} -> {}", phoneNumber, message);
    }

    @Override
    public void notifyStudent(String phoneNumber, String message) {
        log.info("📱 [학생 알림] {} -> {}", phoneNumber, message);
    }

    @Override
    public void notifyTeacher(String message) {
        log.info("👨‍🏫 [선생님 알림] {}", message);
    }
}
