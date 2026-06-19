package com.rikai.backend.event;

import com.rikai.backend.dto.response.dashboard.ActivityResponse;
import com.rikai.backend.model.*;
import com.rikai.backend.model.Enum.SessionType;
import com.rikai.backend.repository.AuditLogRepository;
import com.rikai.backend.service.auth.AuthenticationService;
import com.rikai.backend.service.notification.SseNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogEventListener {

    private final AuditLogRepository auditLogRepository;
    private final AuthenticationService authenticationService;
    private final SseNotificationService sseNotificationService;

    private Users getActor(Users fallback) {
        try {
            Users current = authenticationService.getCurrentUser();
            if (current != null) {
                return current;
            }
        } catch (Exception e) {
            log.debug("Failed to get current user from security context: {}", e.getMessage());
        }
        return fallback;
    }

    private void saveAndBroadcast(AuditLog logEntry) {
        // Save to DB
        AuditLog saved = auditLogRepository.save(logEntry);
        log.info("Saved audit log: ID={}, Action={}, EntityType={}", saved.getId(), saved.getAction(),
                saved.getEntityType());

        // Map and broadcast via SSE
        String activityType = mapActionToActivityType(saved.getAction(), saved.getEntityType(), saved.getDetails());
        ActivityResponse response = ActivityResponse.builder()
                .type(activityType)
                .text(saved.getDetails())
                .timestamp(saved.getCreatedAt() != null ? saved.getCreatedAt() : Instant.now())
                .build();

        sseNotificationService.broadcast(response);
    }

    private String mapActionToActivityType(String action, String entityType, String details) {
        if ("CREATE".equals(action) && "INTERN".equals(entityType)) {
            return "new";
        } else if ("DELETE".equals(action) && "INTERN".equals(entityType)) {
            return "warning";
        } else if ("WEEKLY_REPORT".equals(entityType) || "EVALUATION_SESSION".equals(entityType)) {
            return "evaluation";
        } else if ("STATUS_CHANGE".equals(action)) {
            if (details != null) {
                if (details.contains("WARNING")) {
                    return "warning";
                } else if (details.contains("COMPLETE")) {
                    return "completed";
                }
            }
            return "status_change";
        } else if ("PUBLISH".equals(action)) {
            return "completed";
        }
        return "system";
    }

    private String formatSessionType(SessionType sessionType) {
        if (sessionType == null) {
            return "evaluation";
        }
        String name = sessionType.toString();
        String[] parts = name.split("_");
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                formatted.append(" ");
            }
            String part = parts[i];
            if (part.length() > 0) {
                formatted.append(part.substring(0, 1).toUpperCase());
                if (part.length() > 1) {
                    formatted.append(part.substring(1).toLowerCase());
                }
            }
        }
        return formatted.toString();
    }

    @EventListener
    @Transactional
    public void handleCuDUserEvent(UserCudEvent event) {
        log.info("Handling UserCreatedEvent for user: {}", event.getUser().getFullName());
        Users user = event.getUser();
        Users actor = getActor(null);
        if (actor == null) {
            log.error("Cannot log UserCreatedEvent because no authenticated user is present in context.");
            return;
        }

        String roleName = "User";
        if (user.getRole() != null) {
            String rawRole = user.getRole().getRoleName();
            if ("MENTOR".equalsIgnoreCase(rawRole)) {
                roleName = "Mentor";
            } else if ("HR".equalsIgnoreCase(rawRole)) {
                roleName = "HR";
            } else if ("ADMIN".equalsIgnoreCase(rawRole)) {
                roleName = "Admin";
            }
        }

        String details = String.format("%s %s %s %s",
                actor.getFullName(), event.getEventType().toLowerCase(), roleName, user.getFullName());

        AuditLog logEntry = AuditLog.builder()
                .user(actor)
                .action(event.getEventType())
                .entityType("USER")
                .entityId(String.valueOf(user.getId()))
                .details(details)
                .build();

        saveAndBroadcast(logEntry);
    }

    @EventListener
    public void handleCudDepartmentEvent(DepartmentCudEvent event) {
        log.info("Handling handleCudDepartmentEvent for user: {}", event.getDepartment().getTitle());
        Department department = event.getDepartment();
        Users actor = getActor(null);
        if (actor == null) {
            log.error("Cannot log UserCreatedEvent because no authenticated user is present in context.");
            return;
        }

        String details = String.format("%s %s department %s",
                actor.getFullName(), event.getEventType().toLowerCase(), department.getTitle());

        AuditLog logEntry = AuditLog.builder()
                .user(actor)
                .action(event.getEventType())
                .entityType("DEPARTMENT")
                .entityId(String.valueOf(department.getId()))
                .details(details)
                .build();

        saveAndBroadcast(logEntry);
    }

    @EventListener
    public void handleCudPositionEvent(PositionCudEvent event) {
        log.info("Handling PositionCudEvent for user: {}", event.getPosition().getTitle());
        Position position = event.getPosition();
        Users actor = getActor(null);
        if (actor == null) {
            log.error("Cannot log PositionCudEvent because no authenticated user is present in context.");
            return;
        }

        String details = String.format("%s %s position %s",
                actor.getFullName(), event.getEventType().toLowerCase(), position.getTitle());

        AuditLog logEntry = AuditLog.builder()
                .user(actor)
                .action(event.getEventType())
                .entityType("POSITION")
                .entityId(String.valueOf(position.getId()))
                .details(details)
                .build();

        saveAndBroadcast(logEntry);
    }

    @EventListener
    public void handleCudInternshipBatchCud(InternShipBatchCudEvent event) {
        log.info("Handling InternShipBatchCudEvent for internship batch: {}", event.getInternshipBatch().getName());
        InternshipBatch internshipBatch = event.getInternshipBatch();
        String eventType = event.getEventType();
        Users actor = getActor(null);

        if (actor == null) {
            log.error("Cannot log InternShipBatchCudEvent because no authenticated user is present in context.");
            return;
        }

        String details = String.format("%s was %s internship-batch %s",
                actor.getFullName(), eventType.toLowerCase(), internshipBatch.getName());

        AuditLog logEntry = AuditLog.builder()
                .user(actor)
                .action(eventType)
                .entityType("INTERNSHIP_BATCH")
                .entityId(String.valueOf(internshipBatch.getId()))
                .details(details)
                .build();
        saveAndBroadcast(logEntry);
    }

    @EventListener
    public void handleCudInternEvent(InternCudEvent event) {
        log.info("Handling InternCRUDEvent for intern: {}", event.getIntern().getFullName());
        Intern intern = event.getIntern();
        String eventType = event.getEventType();
        Users actor = getActor(null);

        if (actor == null) {
            log.error("Cannot log InternCRUDEvent because no authenticated user is present in context.");
            return;
        }

        String details = String.format("%s was %s intern %s",
                actor.getFullName(), eventType.toLowerCase(), intern.getFullName());

        AuditLog logEntry = AuditLog.builder()
                .user(actor)
                .action(eventType)
                .entityType("INTERN")
                .entityId(String.valueOf(intern.getId()))
                .details(details)
                .build();
        saveAndBroadcast(logEntry);
    }

    @EventListener
    @Transactional
    public void handleInternStatusChanged(InternStatusChangedEvent event) {
        log.info("Handling InternStatusChangedEvent for intern: {}",
                event.getStatusHistory().getIntern().getFullName());
        InternStatusHistory history = event.getStatusHistory();
        Users actor = history.getChangedBy();

        String details = String.format(
                "%s changed status of intern %s to %s",
                actor.getFullName(), history.getIntern().getFullName(), history.getNewStatus());

        AuditLog logEntry = AuditLog.builder()
                .user(actor)
                .action("STATUS_CHANGE")
                .entityType("INTERN")
                .entityId(String.valueOf(history.getIntern().getId()))
                .details(details)
                .build();

        saveAndBroadcast(logEntry);
    }


    @EventListener
    @Transactional
    public void handleUserStatusChanged(UserStatusChangedEvent event) {
        log.info("Handling UserStatusChangedEvent for users: {}", event.getUsers().getFullName());
        Users user = event.getUsers();
        Users actor = getActor(null);
        if (actor == null) {
            log.error("Cannot log UserStatusChangedEvent because no authenticated user is present in context.");
            return;
        }

        String roleName = "User";
        if (user.getRole() != null) {
            String rawRole = user.getRole().getRoleName();
            if ("MENTOR".equalsIgnoreCase(rawRole)) {
                roleName = "Mentor";
            } else if ("HR".equalsIgnoreCase(rawRole)) {
                roleName = "HR";
            } else if ("ADMIN".equalsIgnoreCase(rawRole)) {
                roleName = "Admin";
            }
        }

        String statusAction = user.getIsActive() ? "WAS UNLOCKED" : "WAS LOCKED";
        String details = String.format("%s %s %s by %s",
                roleName, user.getFullName(), statusAction.toLowerCase(), actor.getFullName());

        AuditLog logEntry = AuditLog.builder()
                .user(actor)
                .action("STATUS_CHANGE")
                .entityType("USER")
                .entityId(null)
                .details(details)
                .build();

        saveAndBroadcast(logEntry);
    }

    @EventListener
    @Transactional
    public void handleWeeklyReportCreated(WeeklyReportCreatedEvent event) {
        log.info("Handling WeeklyReportCreatedEvent");
        WeeklyReport report = event.getWeeklyReport();
        Users actor = report.getMentor();

        String details = String.format("Mentor <strong>%s</strong> completed weekly evaluation for <strong>%s</strong>",
                actor.getFullName(), report.getIntern().getFullName());

        AuditLog logEntry = AuditLog.builder()
                .user(actor)
                .action("CREATE")
                .entityType("WEEKLY_REPORT")
                .entityId(String.valueOf(report.getId().longValue()))
                .details(details)
                .build();

        saveAndBroadcast(logEntry);
    }

    @EventListener
    @Transactional
    public void handleWeeklyReportUpdated(WeeklyReportUpdatedEvent event) {
        log.info("Handling WeeklyReportUpdatedEvent");
        WeeklyReport report = event.getWeeklyReport();
        Users actor = report.getMentor();

        String details = String.format("Weekly evaluation for <strong>%s</strong> was updated by <strong>%s</strong>",
                report.getIntern().getFullName(), actor.getFullName());

        AuditLog logEntry = AuditLog.builder()
                .user(actor)
                .action("UPDATE")
                .entityType("WEEKLY_REPORT")
                .entityId(String.valueOf(report.getId().longValue()))
                .details(details)
                .build();

        saveAndBroadcast(logEntry);
    }

    @EventListener
    @Transactional
    public void handleWeeklyReportDeleted(WeeklyReportDeletedEvent event) {
        log.info("Handling WeeklyReportDeletedEvent");
        Users actor = getActor(null);
        if (actor == null) {
            log.error("Cannot log WeeklyReportDeletedEvent because no authenticated user is present in context.");
            return;
        }

        String details = String.format("Weekly evaluation for <strong>%s</strong> was removed by <strong>%s</strong>",
                event.getInternName(), event.getMentorName());

        AuditLog logEntry = AuditLog.builder()
                .user(actor)
                .action("DELETE")
                .entityType("WEEKLY_REPORT")
                .entityId(String.valueOf(event.getReportId().longValue()))
                .details(details)
                .build();

        saveAndBroadcast(logEntry);
    }

    @EventListener
    @Transactional
    public void handleEvaluationSessionCreated(EvaluationSessionCreatedEvent event) {
        log.info("Handling EvaluationSessionCreatedEvent");
        EvaluationSession session = event.getEvaluationSession();
        Users actor = session.getMentor();

        String details = String.format("Mentor <strong>%s</strong> completed %s evaluation for <strong>%s</strong>",
                actor.getFullName(), formatSessionType(session.getSessionType()), session.getIntern().getFullName());

        AuditLog logEntry = AuditLog.builder()
                .user(actor)
                .action("CREATE")
                .entityType("EVALUATION_SESSION")
                .entityId(String.valueOf(session.getId().longValue()))
                .details(details)
                .build();

        saveAndBroadcast(logEntry);
    }

    @EventListener
    @Transactional
    public void handleEvaluationSessionUpdated(EvaluationSessionUpdatedEvent event) {
        log.info("Handling EvaluationSessionUpdatedEvent");
        EvaluationSession session = event.getEvaluationSession();
        Users actor = session.getMentor();

        String details = String.format("%s evaluation for <strong>%s</strong> was updated by <strong>%s</strong>",
                formatSessionType(session.getSessionType()), session.getIntern().getFullName(), actor.getFullName());

        AuditLog logEntry = AuditLog.builder()
                .user(actor)
                .action("UPDATE")
                .entityType("EVALUATION_SESSION")
                .entityId(String.valueOf(session.getId().longValue()))
                .details(details)
                .build();

        saveAndBroadcast(logEntry);
    }

    @EventListener
    @Transactional
    public void handleEvaluationSessionDeleted(EvaluationSessionDeletedEvent event) {
        log.info("Handling EvaluationSessionDeletedEvent");
        Users actor = getActor(null);
        if (actor == null) {
            log.error("Cannot log EvaluationSessionDeletedEvent because no authenticated user is present in context.");
            return;
        }

        String details = String.format("%s evaluation for <strong>%s</strong> was removed by <strong>%s</strong>",
                event.getSessionType(), event.getInternName(), event.getMentorName());

        AuditLog logEntry = AuditLog.builder()
                .user(actor)
                .action("DELETE")
                .entityType("EVALUATION_SESSION")
                .entityId(String.valueOf(event.getSessionId().longValue()))
                .details(details)
                .build();

        saveAndBroadcast(logEntry);
    }

    @EventListener
    @Transactional
    public void handleRoadmapPublished(RoadmapPublishedEvent event) {
        log.info("Handling RoadmapPublishedEvent for roadmap: {}", event.getRoadmap().getTitle());
        Roadmap roadmap = event.getRoadmap();
        Users actor = getActor(null);
        if (actor == null) {
            log.error("Cannot log RoadmapPublishedEvent because no authenticated user is present in context.");
            return;
        }

        String details = String.format("%s published roadmap: %s",
                actor.getFullName(), roadmap.getTitle());

        AuditLog logEntry = AuditLog.builder()
                .user(actor)
                .action("PUBLISH")
                .entityType("ROADMAP")
                .entityId(String.valueOf(roadmap.getId()))
                .details(details)
                .build();

        saveAndBroadcast(logEntry);
    }
}