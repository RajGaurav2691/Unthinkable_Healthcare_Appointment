package com.project.scheduler;

import com.project.entity.Notification;
import com.project.repository.NotificationRepository;
import com.project.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailRetryScheduler {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    // Run every 2 minutes
    @Scheduled(fixedRate = 120000)
    public void retryFailedEmails() {
        // Find PENDING or FAILED with retryCount < 3
        List<Notification> toRetry = notificationRepository.findEligibleForRetry(3);

        if (!toRetry.isEmpty()) {
            log.info("Found {} notifications eligible for processing/retry", toRetry.size());
            for (Notification notification : toRetry) {
                notificationService.processNotification(notification);
            }
        }
    }
}
