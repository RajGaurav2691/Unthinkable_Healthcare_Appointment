package com.project.service;

import com.project.entity.Appointment;
import com.project.entity.Notification;
import com.project.entity.NotificationStatus;
import com.project.entity.NotificationType;
import com.project.repository.NotificationRepository;
import com.project.service.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private NotificationRepository notificationRepository;
    private EmailService emailService;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        emailService = mock(EmailService.class);
        notificationService = new NotificationService(notificationRepository, emailService);
    }

    @Test
    void queueNotification_savesPendingNotification() {
        notificationService.queueNotification("test@example.com", NotificationType.GENERAL, "Subject", "Body", null);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        
        Notification saved = captor.getValue();
        assertEquals("test@example.com", saved.getRecipient());
        assertEquals(NotificationType.GENERAL, saved.getType());
        assertEquals(NotificationStatus.PENDING, saved.getStatus());
        assertEquals(0, saved.getRetryCount());
    }

    @Test
    void processNotification_success() {
        Notification notification = new Notification();
        notification.setRecipient("test@example.com");
        notification.setSubject("Subject");
        notification.setContent("Body");
        notification.setRetryCount(0);

        notificationService.processNotification(notification);

        verify(emailService).sendEmail("test@example.com", "Subject", "Body");
        verify(notificationRepository).save(notification);
        assertEquals(NotificationStatus.SENT, notification.getStatus());
        assertEquals(1, notification.getRetryCount());
    }

    @Test
    void processNotification_failure() {
        Notification notification = new Notification();
        notification.setRecipient("test@example.com");
        notification.setRetryCount(1);

        doThrow(new RuntimeException("API Error")).when(emailService).sendEmail(anyString(), any(), any());

        notificationService.processNotification(notification);

        verify(notificationRepository).save(notification);
        assertEquals(NotificationStatus.FAILED, notification.getStatus());
        assertEquals(2, notification.getRetryCount());
        assertEquals("API Error", notification.getErrorMessage());
    }
}
