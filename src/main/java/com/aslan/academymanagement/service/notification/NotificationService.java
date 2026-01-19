package com.aslan.academymanagement.service.notification;

public interface NotificationService {

    // 학부모에게 알림 발송 (SMS -> Email로 변경)
    void notifyParent(String contactInfo, String message);

    // 학생에게 알림 발송
    void notifyStudent(String contactInfo, String message);

    // 강사에게 알림 발송
    void notifyTeacher(String message);
}
