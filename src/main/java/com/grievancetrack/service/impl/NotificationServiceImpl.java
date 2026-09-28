package com.grievancetrack.service.impl;

import com.grievancetrack.dto.response.NotificationResponse;
import com.grievancetrack.entity.Grievance;
import com.grievancetrack.entity.Notification;
import com.grievancetrack.entity.enums.NotificationType;
import com.grievancetrack.exception.ResourceNotFoundException;
import com.grievancetrack.repository.NotificationRepository;
import com.grievancetrack.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void createNotification(Grievance grievance, String message, NotificationType type) {
        Notification notification = new Notification(grievance, message, type);
        notificationRepository.save(notification);
        log.info("[NOTIFICATION] type={} | grievance={} | {}",
                type,
                (grievance != null ? grievance.getComplaintCode() : "N/A"),
                message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getRecentNotifications() {
        return notificationRepository.findTop30ByOrderByCreatedAtDesc().stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return notificationRepository.countByReadFalse();
    }

    @Override
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));
        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(updated);
    }

    @Override
    public void markAllAsRead() {
        notificationRepository.markAllAsRead();
    }
}
