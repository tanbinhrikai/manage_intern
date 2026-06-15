package com.rikai.backend.repository;

import com.rikai.backend.model.Enum.NotificationType;
import com.rikai.backend.model.Notification;
import com.rikai.backend.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByReceiverOrderByCreatedAtDesc(Users receiver, Pageable pageable);
    long countByReceiverAndIsReadFalse(Users receiver);
    boolean existsByReceiverIdAndReferenceIdAndType(UUID receiverId, Long referenceId, NotificationType type);
}


