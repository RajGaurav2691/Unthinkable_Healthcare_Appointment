package com.project.repository;

import com.project.entity.Notification;
import com.project.entity.NotificationStatus;
import com.project.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE (n.status = 'PENDING' OR n.status = 'FAILED') AND n.retryCount < :maxRetries ORDER BY n.createdAt ASC")
    List<Notification> findEligibleForRetry(int maxRetries);

    boolean existsByRelatedAppointmentIdAndType(Long appointmentId, NotificationType type);

    @Query("SELECT COUNT(n) > 0 FROM Notification n WHERE n.recipient = :recipient AND n.type = :type AND n.createdAt >= :startOfDay")
    boolean existsByRecipientAndTypeSince(String recipient, NotificationType type, java.time.LocalDateTime startOfDay);

    long countByStatus(NotificationStatus status);
}
