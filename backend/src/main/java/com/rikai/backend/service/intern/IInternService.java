package com.rikai.backend.service.intern;

import com.rikai.backend.dto.response.intern.InternResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IInternService {
    
    /**
     * Get all interns assigned to current mentor
     * - Mentor: only see their assigned interns
     * - Admin: see all interns
     */
    List<InternResponse> getMyInterns();
    
    /**
     * Get all interns with pagination
     * - Mentor: only see their assigned interns
     * - Admin: see all interns
     */
    com.rikai.backend.dto.response.PageResponse<InternResponse> getAllInterns(Pageable pageable);
}
