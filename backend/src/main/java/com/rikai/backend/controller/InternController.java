package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.response.PageResponse;
import com.rikai.backend.dto.response.intern.InternResponse;
import com.rikai.backend.service.intern.IInternService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interns")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternController {
    
    IInternService internService;
    
    /**
     * GET /interns/my-interns
     * Get all interns assigned to current mentor
     * - Mentor: only see their assigned interns
     * - Admin: see all interns
     */
    @GetMapping("/my-interns")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<List<InternResponse>> getMyInterns() {
        List<InternResponse> result = internService.getMyInterns();
        return ApiResponse.buildSuccessResponse(result, SuccessCode.GET_ALL_INTERNS_SUCCESSFUL);
    }
    
    /**
     * GET /interns
     * Get all interns with pagination
     * - Mentor: only see their assigned interns
     * - Admin: see all interns
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<PageResponse<InternResponse>> getAllInterns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);
        PageResponse<InternResponse> result = internService.getAllInterns(pageable);
        return ApiResponse.buildSuccessResponse(result, SuccessCode.GET_ALL_INTERNS_SUCCESSFUL);
    }
}
