package com.rikai.backend.ai.tools;

import com.rikai.backend.dto.record.EvaluationResult;
import com.rikai.backend.dto.record.EvaluationScoreResult;
import com.rikai.backend.model.EvaluationScore;
import com.rikai.backend.model.EvaluationSession;
import com.rikai.backend.repository.EvaluationSessionRepository;
import com.rikai.backend.repository.InternRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI Tool for retrieving evaluation session results.
 * This tool allows the AI agent to get formal evaluation data (FIRST_TERM,
 * MID_TERM, FINAL).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EvaluationSessionTool {

        private final EvaluationSessionRepository evaluationSessionRepository;
        private final InternRepository internRepository;

        @Tool(description = """
                        Get evaluation session results for an intern. Returns formal evaluation data including:
                        - Session type: FIRST_TERM (2 months), MID_TERM (4 months), FINAL (end of internship)
                        - Final score and level assessment
                        - Conclusion: PASS, FAIL, NEEDS_IMPROVEMENT
                        - Overall comment from evaluator
                        - Individual criteria scores

                        Use this tool when user asks:
                        - "What is the evaluation result for intern [id]?"
                        - "Did intern [id] pass the evaluation?"
                        - "What is intern [id]'s mid-term score?"
                        - "Can intern [id] become a full-time employee?"
                        - "How did intern [id] perform in the final evaluation?"
                        """)
        public List<EvaluationResult> getEvaluationSessionResults(
                        @ToolParam(description = "ID of the intern to get evaluation results for") Long internId) {

                log.info("AI Tool: getEvaluationSessionResults called for internId: {}", internId);

                if (internId == null) {
                        return Collections.emptyList();
                }

                if (!internRepository.existsById(internId)) {
                        log.warn("Intern not found with id: {}", internId);
                        return Collections.emptyList();
                }

                List<EvaluationSession> sessions = evaluationSessionRepository
                                .findByInternIdOrderByEvaluationDateDesc(internId);

                return sessions.stream()
                                .map(this::mapToResult)
                                .collect(Collectors.toList());
        }

        private EvaluationResult mapToResult(EvaluationSession session) {
                List<EvaluationScoreResult> scores = session.getScores() != null
                                ? session.getScores().stream()
                                                .map(this::mapToScoreResult)
                                                .collect(Collectors.toList())
                                : Collections.emptyList();

                return new EvaluationResult(
                                session.getId(),
                                session.getSessionType() != null ? session.getSessionType().name() : null,
                                session.getEvaluationDate() != null ? session.getEvaluationDate().toString() : null,
                                session.getFinalScore(),
                                session.getLevelAssessment(),
                                session.getConclusion() != null ? session.getConclusion().name() : null,
                                session.getOverallComment(),
                                session.getMentor() != null ? session.getMentor().getFullName() : null,
                                scores);
        }

        private EvaluationScoreResult mapToScoreResult(EvaluationScore score) {
                return new EvaluationScoreResult(
                                score.getCriteria() != null ? score.getCriteria().getName() : null,
                                score.getScore(),
                                score.getComment());
        }
}
