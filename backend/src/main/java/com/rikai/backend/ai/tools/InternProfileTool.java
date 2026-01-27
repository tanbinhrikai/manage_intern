package com.rikai.backend.ai.tools;

import com.rikai.backend.model.Intern;
import com.rikai.backend.repository.InternRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI Tool for finding intern profiles.
 * This tool allows the AI agent to search and retrieve intern information.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InternProfileTool {

        private final InternRepository internRepository;

        public record InternProfileResult(
                        Long id,
                        String fullName,
                        String email,
                        String phone,
                        String positionName,
                        String mentorName,
                        String mentorEmail,
                        String batchName,
                        String status,
                        String startDate,
                        String endDate,
                        String offerStatus) {
        }

        @Tool(description = """
                        Find intern profile by name or keyword. Returns intern details including:
                        - Personal info: name, email, phone
                        - Position and department
                        - Mentor assigned
                        - Internship batch
                        - Current status and offer status

                        Use this tool when user asks:
                        - "Who is intern [name]?"
                        - "Tell me about intern [name]"
                        - "What team is [name] in?"
                        - "Who is [name]'s mentor?"
                        - "List interns matching [keyword]"
                        """)
        public List<InternProfileResult> findInternProfile(
                        @ToolParam(description = "Name or keyword to search for intern") String keyword) {

                log.info("AI Tool: findInternProfile called with keyword: {}", keyword);

                Page<Intern> interns = internRepository.getAllInternByKeyword(
                                PageRequest.of(0, 1000),
                                keyword,
                                null, null, null, null, null);

                return interns.getContent().stream()
                                .map(this::mapToResult)
                                .collect(Collectors.toList());
        }

        private InternProfileResult mapToResult(Intern intern) {
                return new InternProfileResult(
                                intern.getId(),
                                intern.getFullName(),
                                intern.getEmail(),
                                intern.getPhone(),
                                intern.getPosition() != null ? intern.getPosition().getTitle() : null,
                                intern.getMentor() != null ? intern.getMentor().getFullName() : null,
                                intern.getMentor() != null ? intern.getMentor().getEmail() : null,
                                intern.getInternshipBatch() != null ? intern.getInternshipBatch().getName() : null,
                                intern.getInternStatus() != null ? intern.getInternStatus().name() : null,
                                intern.getStartDate() != null ? intern.getStartDate().toString() : null,
                                intern.getEndDate() != null ? intern.getEndDate().toString() : null,
                                intern.getOfferStatus() != null ? intern.getOfferStatus().name() : null);
        }
}
