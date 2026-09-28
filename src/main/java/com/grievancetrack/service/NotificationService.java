package com.grievancetrack.service;

import com.grievancetrack.dto.response.NotificationResponse;
import com.grievancetrack.entity.Grievance;
import com.grievancetrack.entity.enums.NotificationType;

import java.util.List;

public interface NotificationService {
    void createNotification(Grievance grievance, String message, NotificationType type);
    List<NotificationResponse> getAllNotifications();
    List<NotificationResponse> getRecentNotifications();
    long getUnreadCount();
    NotificationResponse markAsRead(Long id);
    void markAllAsRead();
}
