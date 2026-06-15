package com.rikai.backend.service.notification;

import com.rikai.backend.dto.response.dashboard.ActivityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class SseNotificationService {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        // Set timeout to 30 minutes (1800000 ms)
        SseEmitter emitter = new SseEmitter(1800000L);
        this.emitters.add(emitter);

        emitter.onCompletion(() -> {
            log.info("SSE emitter completed");
            this.emitters.remove(emitter);
        });

        emitter.onTimeout(() -> {
            log.info("SSE emitter timed out");
            this.emitters.remove(emitter);
        });

        emitter.onError((ex) -> {
            // Check if it's a typical client disconnect to avoid noisy error logs
            if (ex instanceof IOException || ex.getClass().getSimpleName().equals("ClientAbortException")) {
                log.debug("SSE client disconnected normally (Broken pipe).");
            } else {
                log.error("SSE emitter encountered error: {}", ex.getMessage());
            }
            this.emitters.remove(emitter);
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected to activity stream"));
        } catch (IOException e) {
            log.debug("Failed to send connection message (Client likely disconnected immediately).");
            emitter.complete();
            this.emitters.remove(emitter);
        }

        return emitter;
    }

    public void broadcast(ActivityResponse activity) {
        if (emitters.isEmpty()) {
            return;
        }
        
        log.info("Broadcasting activity to {} SSE subscribers", emitters.size());
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("activity")
                        .data(activity));
            } catch (IOException e) {
                // Catching IOException handles the "Broken pipe" specifically
                log.debug("SSE client disconnected unexpectedly (Broken pipe). Removing emitter.");
                deadEmitters.add(emitter);
            } catch (Exception e) {
                // Fallback for actual unexpected system errors
                log.error("Unexpected error sending message to SSE client, removing emitter", e);
                deadEmitters.add(emitter);
            }
        }
        
        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
        }
    }
}