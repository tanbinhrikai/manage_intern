package com.rikai.backend.service.intern;

import com.rikai.backend.common.InternStatus;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.request.InternCreationRequest;
import com.rikai.backend.dto.request.InternUpdateRequest;
import com.rikai.backend.dto.response.InternResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IInternService {
    PageResponse<InternResponse> getAllInterns(Pageable pageable);

    InternResponse getInternById(Long id);

    InternResponse createIntern(InternCreationRequest request);

    InternResponse updateIntern(Long id, InternUpdateRequest request);

    void deleteIntern(Long id);

    PageResponse<InternResponse> getInternsByMentor(UUID mentorId, Pageable pageable);

    PageResponse<InternResponse> getInternsByStatus(InternStatus status, Pageable pageable);

    PageResponse<InternResponse> getInternsByPositionId(Long positionId, Pageable pageable);
}
