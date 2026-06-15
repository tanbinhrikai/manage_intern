package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.response.notification.NotificationResponse;
import com.rikai.backend.dto.response.notification.UnreadCountResponse;
import com.rikai.backend.service.notification.NotificationService;
import com.rikai.backend.service.notification.NotificationSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final NotificationSseService notificationSseService;

    @GetMapping("/notifications/unread-count")
    public ApiResponse<UnreadCountResponse> getUnreadCount() {

        Long count = notificationService.countUnreadByUser();
        UnreadCountResponse response = new UnreadCountResponse(count);
        return ApiResponse.buildSuccessResponse(
                        response,
                        SuccessCode.UNREAD_COUNT_NOTIFICATION_SUCCESSFUL

        );
    }

    @GetMapping("/notifications/me")
    public ApiResponse<PageResponse<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int limit)
    {
        PageResponse<NotificationResponse> data = notificationService.getMyNotifications(page,limit);
        return ApiResponse.buildSuccessResponse(
                data,
                SuccessCode.GET_MY_NOTIFICATION_SUCCESSFUL
        );
    }


    @GetMapping(value = "/notifications/subscribe",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter  subscribe(){
        return notificationSseService.subscribe();


    }


}
