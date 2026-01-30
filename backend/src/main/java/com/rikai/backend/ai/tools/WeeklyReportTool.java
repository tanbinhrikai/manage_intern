package com.rikai.backend.ai.tools;

import com.rikai.backend.ai.security.AgentSecurityAdvisor;
import com.rikai.backend.dto.record.WeeklyReportInfo;
import com.rikai.backend.dto.record.WeeklyReportResult;
import com.rikai.backend.dto.record.CriteriaScore;
import com.rikai.backend.model.Intern;
import com.rikai.backend.model.WeeklyReport;
import com.rikai.backend.model.WeeklyReportDetail;
import com.rikai.backend.repository.InternRepository;
import com.rikai.backend.repository.WeeklyReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI Tool for retrieving and searching weekly reports.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportTool {

        private final WeeklyReportRepository weeklyReportRepository;
        private final InternRepository internRepository;
        private final VectorStore vectorStore;
        private final AgentSecurityAdvisor agentSecurityAdvisor;

        @Tool(description = """
            Get weekly report analysis for a specific intern by ID.
            Use this when you know the intern's ID and want to see their history.
            """)
        public WeeklyReportResult getWeeklyReportAnalysis(
                @ToolParam(description = "ID of the intern to get weekly reports for") Long internId) {
                log.info("AI Tool: getWeeklyReportAnalysis called for internId: {}", internId);

                if (!agentSecurityAdvisor.canAccessIntern(internId)) {
                        log.warn("Access denied for user accessing intern {}", internId);
                        return new WeeklyReportResult("Access Denied", Collections.emptyList(), 0.0);
                }
                if (internId == null) {
                        return new WeeklyReportResult("Unknown", Collections.emptyList(), 0.0);
                }

                String internName = internRepository.findById(internId)
                        .map(Intern::getFullName)
                        .orElse("Unknown");

                List<WeeklyReport> reports = weeklyReportRepository.findByInternIdOrderByWeekStartDateDesc(
                        internId, PageRequest.of(0, 20)).getContent();

                if (reports.isEmpty()) {
                        return new WeeklyReportResult(internName, Collections.emptyList(), 0.0);
                }

                List<WeeklyReportInfo> reportInfos = reports.stream()
                        .map(this::mapToReportInfo)
                        .collect(Collectors.toList());

                double overallAverage = reports.stream()
                        .filter(r -> r.getAverageScore() != null)
                        .mapToDouble(r -> r.getAverageScore().doubleValue())
                        .average()
                        .orElse(0.0);

                return new WeeklyReportResult(internName, reportInfos, overallAverage);
        }

        @Tool(description = """
            Search for weekly reports based on meaning, keywords, or qualitative descriptions.
            Use this tool to answer questions like:
            - "Find interns who are lazy or not working hard"
            - "Show me reports with low scores in Communication"
            - "Who is having trouble with Java tasks?"
            - "Find positive feedback about proactivity"
            """)
        public List<String> searchReports(
                @ToolParam(description = "The search query description (e.g., 'lazy intern', 'low technical score')") String query) {

                log.info("AI Tool: searchReports called with query: {}", query);

                List<Document> results = vectorStore.similaritySearch(
                        SearchRequest.builder().query(query).topK(5).build()
                );

                return results.stream()
                        .map(Document::getFormattedContent)
                        .collect(Collectors.toList());
        }

        private WeeklyReportInfo mapToReportInfo(WeeklyReport report) {
                List<CriteriaScore> criteriaScores = report.getDetails() != null
                        ? report.getDetails().stream()
                        .map(this::mapToCriteriaScore)
                        .collect(Collectors.toList())
                        : Collections.emptyList();

                return new WeeklyReportInfo(
                        report.getWeekNumber(),
                        report.getWeekStartDate() != null ? report.getWeekStartDate().toString() : null,
                        report.getTasksAssigned(),
                        report.getTasksCompleted(),
                        report.getIssuesRisks(),
                        report.getMentorOverallComment(),
                        report.getAverageScore(),
                        criteriaScores);
        }

        private CriteriaScore mapToCriteriaScore(WeeklyReportDetail detail) {
                return new CriteriaScore(
                        detail.getCriteria() != null ? detail.getCriteria().getName() : null,
                        detail.getScore(),
                        detail.getComment());
        }
}