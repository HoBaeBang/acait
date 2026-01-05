package com.aslan.academymanagement.service.notification;

public interface NotificationService {

    void notifyParent(String phoneNumber, String message);

    void notifyStudent(String phoneNumber, String message);

    void notifyTeacher(String message);
}
