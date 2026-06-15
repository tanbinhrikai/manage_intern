package com.rikai.backend.service.notification;

import com.rikai.backend.model.Users;
import com.rikai.backend.service.auth.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationSseService {
    // lưu lại kết nối  của từng user
    private final AuthenticationService authenticationService;

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    public NotificationSseService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }


    public SseEmitter subscribe(){
        Users concurrentUser = authenticationService.getCurrentUser();
        UUID userId = concurrentUser.getId();

        SseEmitter oldEmitter = emitters.get(userId);

        if (oldEmitter != null) {
            oldEmitter.complete();
        }

        SseEmitter emitter  = new SseEmitter(0L);

        emitters.put(userId,emitter);
        System.out.println("Emitter size before: " + emitters.size());
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));

        return emitter;
    }


    public void sendNotification(UUID userId, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(data));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }
}
