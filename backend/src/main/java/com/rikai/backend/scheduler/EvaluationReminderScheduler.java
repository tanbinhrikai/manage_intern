package com.rikai.backend.scheduler;

import com.rikai.backend.service.intern.IInternService;
import com.rikai.backend.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EvaluationReminderScheduler {
    private final NotificationService notificationService;

    @Scheduled(cron = "0 37 15 * * *")
    public void sendEvaluationReminder() {
        notificationService.sendInternEvaluationReminders();
    }



}
