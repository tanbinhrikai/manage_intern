package com.rikai.backend.service.intershipbatch;

import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.response.internship_batch.InternshipBatchResponse;
import org.springframework.data.domain.PageRequest;

public interface IInternshipBatchService {
    PageResponse<InternshipBatchResponse> getAllInternshipBatches(String keyword, PageRequest pageRequest);
}
