package com.project.service;

import com.project.entity.Appointment;
import com.project.entity.Notification;
import com.project.entity.NotificationStatus;
import com.project.entity.NotificationType;
import com.project.repository.NotificationRepository;
import com.project.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Transactional
    public void queueNotification(String recipient, NotificationType type, String subject, String content, Appointment relatedAppointment) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .subject(subject)
                .content(content)
                .relatedAppointment(relatedAppointment)
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .build();
        
        notificationRepository.save(notification);
        log.info("Queued {} notification for {}", type, recipient);
    }

    @Transactional
    public void processNotification(Notification notification) {
        notification.setLastAttemptAt(LocalDateTime.now());
        notification.setRetryCount(notification.getRetryCount() + 1);

        try {
            emailService.sendEmail(notification.getRecipient(), notification.getSubject(), notification.getContent());
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setErrorMessage(null);
            log.info("Successfully processed notification ID {}", notification.getId());
        } catch (Exception e) {
            notification.setErrorMessage(e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            log.error("Failed to process notification ID {}: {}", notification.getId(), e.getMessage());
        }

        notificationRepository.save(notification);
    }
}
