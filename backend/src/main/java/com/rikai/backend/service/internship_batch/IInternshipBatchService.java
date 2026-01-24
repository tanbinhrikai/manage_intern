package com.rikai.backend.service.internship_batch;

import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.request.batch.InternshipBatchCreationRequest;
import com.rikai.backend.dto.request.batch.InternshipBatchUpdateRequest;
import com.rikai.backend.dto.response.batch.InternshipBatchResponse;
import com.rikai.backend.model.Enum.BatchStatus;
import org.springframework.data.domain.Pageable;

public interface IInternshipBatchService {
    InternshipBatchResponse createBatch(InternshipBatchCreationRequest request);
    InternshipBatchResponse updateBatch(Long id, InternshipBatchUpdateRequest request);
    PageResponse<InternshipBatchResponse> getAllBatches(Pageable pageable, String keyword, BatchStatus status);
    InternshipBatchResponse getBatchById(Long id);
    void deleteBatch(Long id);
}
