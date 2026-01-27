package com.rikai.backend.ai.event;

import com.rikai.backend.model.WeeklyReport;
import com.rikai.backend.model.WeeklyReportDetail;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportVectorSyncListener {

    VectorStore vectorStore;

    @Async
    @EventListener
    public void handleReportSaved(ReportSavedEvent event) {
        var report = event.getReport();
        log.info("Starting vector sync for report id: {}", report.getId());

        try {
            StringBuilder contentBuilder = new StringBuilder();

            contentBuilder.append(String.format("Weekly Report for Intern: %s (Week %d).\n",
                    report.getIntern().getFullName(), report.getWeekNumber()));

            contentBuilder.append(String.format("Average Score: %s.\n", report.getAverageScore()));

            if (report.getMentorOverallComment() != null && !report.getMentorOverallComment().isBlank()) {
                contentBuilder.append("Mentor Overall Comment: ").append(report.getMentorOverallComment()).append("\n");
            }

            if (report.getIssuesRisks() != null && !report.getIssuesRisks().isBlank()) {
                contentBuilder.append("Issues and Risks: ").append(report.getIssuesRisks()).append("\n");
            }
            if (report.getDetails() != null && !report.getDetails().isEmpty()) {
                contentBuilder.append("Detailed Criteria Scores:\n");
                for (WeeklyReportDetail detail : report.getDetails()) {
                    if (detail.getCriteria() != null && detail.getScore() != null) {
                        contentBuilder.append(String.format("- %s: %s", detail.getCriteria().getName(), detail.getScore()));
                        if (detail.getComment() != null && !detail.getComment().isBlank()) {
                            contentBuilder.append(String.format(" (Comment: %s)", detail.getComment()));
                        }
                        contentBuilder.append("\n");
                    }
                }
            }

            String content = contentBuilder.toString();
            Document document = filterDocument(report, content);
            vectorStore.add(List.of(document));
            log.info("Successfully synced report ID {} to vector store. Content length: {}", report.getId(), content.length());
        } catch (Exception e) {
            log.error("Failed to sync report ID {} to vector store", report.getId(), e);
        }
    }

    private static @NonNull Document filterDocument(WeeklyReport report, String content) {
        Map<String, Object> metadata = Map.of(
                "report_id", report.getId().toString(),
                "intern_id", report.getIntern().getId().toString(),
                "intern_name", report.getIntern().getFullName(),
                "mentor_id", report.getMentor().getId().toString(),
                "week_number", report.getWeekNumber().toString(),
                "week_start_date", report.getWeekStartDate().toString(),
                "average_score", report.getAverageScore().doubleValue(),
                "type", "weekly_report"
        );
        return new Document(content, metadata);
    }
}