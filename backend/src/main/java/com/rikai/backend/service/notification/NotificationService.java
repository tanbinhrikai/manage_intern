package com.rikai.backend.service.notification;

import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.response.notification.NotificationResponse;
import com.rikai.backend.model.Enum.NotificationType;
import com.rikai.backend.model.Intern;
import com.rikai.backend.model.Notification;
import com.rikai.backend.model.Users;
import com.rikai.backend.repository.InternRepository;
import com.rikai.backend.repository.NotificationRepository;
import com.rikai.backend.service.auth.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final InternRepository internRepository;
    private final NotificationRepository notificationRepository;
    private final AuthenticationService authenticationService;
    private final NotificationSseService notificationSseService;

    public void sendInternEvaluationReminders(){
        System.out.println(" đa chay tơi doạn lap lich ");
        List<Intern> interns = this.internRepository
                .findNeedEvaluationReminder(LocalDate.now().minusMonths(2));
        for (Intern intern: interns){

           if(!notificationRepository.existsByReceiverIdAndReferenceIdAndType(
                   intern.getMentor().getId(),
                   intern.getId(),
                   NotificationType.INTERN_EVALUATION_REMINDER
           )){
               System.out.println("có intern thoa man roi");
               this.createEvaluationReminder(intern);
           }

        }
    }


    private void createEvaluationReminder(Intern intern) {

        Notification notification = Notification.builder()
                .receiver(intern.getMentor())
                .title("Intern Evaluation Reminder")
                .content("Please evaluate intern "
                        + intern.getFullName())
                .referenceId(intern.getId())
                .build();

        notificationRepository.save(notification);

        NotificationResponse data  =  NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
        notificationSseService.sendNotification(intern.getMentor().getId(),data);

    }



    public long countUnreadByUser(){
        Users user = authenticationService.getCurrentUser();
        return notificationRepository.countByReceiverAndIsReadFalse(user);
    }


    public PageResponse<NotificationResponse> getMyNotifications(int pageIndex, int pageSize) {
        Users currentUser = authenticationService.getCurrentUser();

        Pageable pageable = PageRequest.of(pageIndex, pageSize);

        Page<Notification> notificationPage = notificationRepository
                .findByReceiverOrderByCreatedAtDesc(currentUser, pageable);

        Page<NotificationResponse> responsePage = notificationPage.map(this::toResponse);

        return PageResponse.fromPage(responsePage);
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .isRead(notification.isRead())
                .referenceId(notification.getReferenceId())
                .link(buildLink(notification))
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private String buildLink(Notification notification) {
        if (notification.getReferenceId() == null) return null;

        return  "";
    }
}
