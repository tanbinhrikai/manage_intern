package com.rikai.backend.service.internship_batch;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.request.batch.InternshipBatchCreationRequest;
import com.rikai.backend.dto.request.batch.InternshipBatchUpdateRequest;
import com.rikai.backend.dto.response.batch.InternshipBatchResponse;
import com.rikai.backend.event.InternShipBatchCudEvent;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.mapper.InternshipBatchMapper;
import com.rikai.backend.model.Enum.BatchStatus;
import com.rikai.backend.model.InternshipBatch;
import com.rikai.backend.repository.InternRepository;
import com.rikai.backend.repository.InternshipBatchRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternshipBatchService implements IInternshipBatchService {
    InternshipBatchRepository batchRepository;
    InternshipBatchMapper batchMapper;
    InternRepository internRepository;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public InternshipBatchResponse createBatch(InternshipBatchCreationRequest request) {
        InternshipBatch batch = batchMapper.toInternshipBatch(request);
        batch.setStatus(BatchStatus.ONGOING);
        InternshipBatch savedBatch = batchRepository.save(batch);
        eventPublisher.publishEvent(new InternShipBatchCudEvent(this, "CREATED", savedBatch));
        return batchMapper.toInternshipBatchResponse(savedBatch);
    }

    @Override
    @Transactional
    public InternshipBatchResponse updateBatch(Long id, InternshipBatchUpdateRequest request) {
        InternshipBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        batchMapper.updateInternshipBatch(batch, request);
        InternshipBatch updatedBatch = batchRepository.save(batch);
        eventPublisher.publishEvent(new InternShipBatchCudEvent(this, "UPDATED", updatedBatch));
        return batchMapper.toInternshipBatchResponse(updatedBatch);
    }

    @Override
    public PageResponse<InternshipBatchResponse> getAllBatches(Pageable pageable, String keyword, BatchStatus status) {
        Page<InternshipBatch> batchPage = batchRepository.findBatches(keyword, status, pageable);
        Page<InternshipBatchResponse> responsePage = batchPage.map(batch -> {
            InternshipBatchResponse response = batchMapper.toInternshipBatchResponse(batch);
            response.setInternCount(internRepository.countByInternshipBatch_Id(batch.getId()));
            return response;
        });
        return PageResponse.fromPage(responsePage);
    }

    @Override
    public InternshipBatchResponse getBatchById(Long id) {
        InternshipBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        InternshipBatchResponse response= batchMapper.toInternshipBatchResponse(batch);
        response.setInternCount(internRepository.countByInternshipBatch_Id(batch.getId()));
        return response;
    }

    @Override
    @Transactional
    public void deleteBatch(Long id) {
        if (!batchRepository.existsById(id)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        InternshipBatch batch = batchRepository.findById(id).get();
        eventPublisher.publishEvent(new InternShipBatchCudEvent(this, "DELETED", batch));
        batchRepository.delete(batch);
    }
}
