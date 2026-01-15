package com.rikai.backend.dto.response.intern;

import com.rikai.backend.model.Enum.InternStatus;
import com.rikai.backend.model.Enum.OfferStatus;
import com.rikai.backend.model.Intern;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InternResponse {
    private Integer id;
    private String fullName;
    private String email;
    private String phone;
    private Integer positionId;
    private String positionTitle;
    private Integer batchId;
    private String batchName;
    private String mentorId;
    private String mentorName;
    private LocalDate startDate;
    private LocalDate endDate;
    private InternStatus status;
    private OfferStatus offerStatus;
    private LocalDate offerDate;
    private String offerNotes;
    private Instant createdAt;
    private Instant updatedAt;
    
    public static InternResponse fromIntern(Intern intern) {
        if (intern == null) {
            return null;
        }
        
        return InternResponse.builder()
                .id(intern.getId())
                .fullName(intern.getFullName())
                .email(intern.getEmail())
                .phone(intern.getPhone())
                .positionId(intern.getPosition() != null ? intern.getPosition().getId() : null)
                .positionTitle(intern.getPosition() != null ? intern.getPosition().getTitle() : null)
                .batchId(intern.getBatch() != null ? intern.getBatch().getId() : null)
                .batchName(intern.getBatch() != null ? intern.getBatch().getName() : null)
                .mentorId(intern.getMentor() != null ? intern.getMentor().getId().toString() : null)
                .mentorName(intern.getMentor() != null ? intern.getMentor().getFullName() : null)
                .startDate(intern.getStartDate())
                .endDate(intern.getEndDate())
                .status(intern.getStatus())
                .offerStatus(intern.getOfferStatus())
                .offerDate(intern.getOfferDate())
                .offerNotes(intern.getOfferNotes())
                .createdAt(intern.getCreatedAt())
                .updatedAt(intern.getUpdatedAt())
                .build();
    }
}
