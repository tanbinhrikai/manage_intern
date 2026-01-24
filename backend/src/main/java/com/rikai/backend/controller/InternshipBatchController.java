package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.response.internship_batch.InternshipBatchResponse;
import com.rikai.backend.service.intershipbatch.IInternshipBatchService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internship-batches")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternshipBatchController {
    private final IInternshipBatchService internshipBatchService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<PageResponse<InternshipBatchResponse>> getAllInternshipBatches(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false, name = "keyword") String keyword
    ) {
        PageRequest pageable = PageRequest.of(page, limit, Sort.sort(InternshipBatchResponse.class).by(InternshipBatchResponse::getStartDate).descending());
        return ApiResponse.buildSuccessResponse(
                internshipBatchService.getAllInternshipBatches(keyword, pageable),
                SuccessCode.GET_ALL_INTERNSHIP_BATCHES_SUCCESSFUL);
    }

}
