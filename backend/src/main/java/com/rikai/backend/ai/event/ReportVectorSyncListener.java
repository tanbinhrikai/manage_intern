package com.rikai.backend.ai.event;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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

        if (report.getMentorOverallComment() == null || report.getMentorOverallComment().isBlank()) {
            log.debug("Skipping vector sync - no mentor comment for report id: {}", report.getId());
            return;
        }

        try {
            log.info("Syncing mentor comment to vector store for report id: {}", report.getId());
            Document document = new Document(
                    report.getMentorOverallComment(),
                    Map.of(
                            "report_id", report.getId().toString(),
                            "intern_id", report.getIntern().getId().toString(),
                            "intern_name", report.getIntern().getFullName(),
                            "mentor_id", report.getMentor().getId().toString(),
                            "mentor_name", report.getMentor().getFullName(),
                            "week_number", report.getWeekNumber().toString(),
                            "week_start_date", report.getWeekStartDate().toString(),
                            "type", "mentor_comment"));
            vectorStore.add(List.of(document));

            log.info("Successfully synced mentor comment to vector store for report id: {}", report.getId());
        } catch (Exception e) {
            log.error("Failed to sync mentor comment to vector store for report id: {}", report.getId(), e);
        }
    }
}
