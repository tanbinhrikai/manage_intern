package com.rikai.backend.ai.tools;

import com.rikai.backend.dto.record.InternInfo;
import com.rikai.backend.dto.record.MentorInternsResult;
import com.rikai.backend.model.Intern;
import com.rikai.backend.model.Users;
import com.rikai.backend.repository.InternRepository;
import com.rikai.backend.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI Tool for retrieving interns by mentor.
 * This tool allows the AI agent to get the list of interns assigned to a
 * specific mentor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MentorInternsTool {

        private final InternRepository internRepository;
        private final UsersRepository usersRepository;

        @Tool(description = """
                        Get list of interns assigned to a specific mentor.
                        Returns mentor details and all interns they are supervising.

                        Use this tool when user asks:
                        - "Who are mentor [name]'s interns?"
                        - "List all interns supervised by [mentor name]"
                        - "How many interns does mentor [name] have?"
                        - "Show me [mentor]'s team"
                        """)
        public MentorInternsResult getInternsByMentor(
                        @ToolParam(description = "Name or email of the mentor to find interns for") String mentorKeyword) {

                log.info("AI Tool: getInternsByMentor called with mentorKeyword: {}", mentorKeyword);

                if (mentorKeyword == null || mentorKeyword.trim().isEmpty()) {
                        return new MentorInternsResult("Unknown", null, null, Collections.emptyList());
                }

                // Find mentor by full name (using existing repository method)
                List<Users> mentors = usersRepository.findByFullNameContainingIgnoreCase(mentorKeyword);

                if (mentors.isEmpty()) {
                        // Try by email
                        var mentorByEmail = usersRepository.findByEmail(mentorKeyword);
                        if (mentorByEmail.isEmpty()) {
                                log.warn("Mentor not found with keyword: {}", mentorKeyword);
                                return new MentorInternsResult("Not Found", null, null, Collections.emptyList());
                        }
                        mentors = List.of(mentorByEmail.get());
                }

                Users mentor = mentors.getFirst(); // Take first match

                // Get interns for this mentor using findByMentor_Id
                List<Intern> interns = internRepository.findByMentor_Id(mentor.getId(), PageRequest.of(0, 100))
                                .getContent();

                List<InternInfo> internInfos = interns.stream()
                                .map(this::mapToInternInfo)
                                .collect(Collectors.toList());

                return new MentorInternsResult(
                                mentor.getFullName(),
                                mentor.getEmail(),
                                mentor.getDepartment() != null ? mentor.getDepartment().getTitle() : null,
                                internInfos);
        }

        private InternInfo mapToInternInfo(Intern intern) {
                return new InternInfo(
                                intern.getId(),
                                intern.getFullName(),
                                intern.getEmail(),
                                intern.getPosition() != null ? intern.getPosition().getTitle() : null,
                                intern.getInternStatus() != null ? intern.getInternStatus().name() : null,
                                intern.getStartDate() != null ? intern.getStartDate().toString() : null,
                                intern.getEndDate() != null ? intern.getEndDate().toString() : null);
        }
}
