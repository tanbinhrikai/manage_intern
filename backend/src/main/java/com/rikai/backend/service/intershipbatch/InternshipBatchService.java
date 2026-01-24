package com.rikai.backend.service.intershipbatch;

import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.response.internship_batch.InternshipBatchResponse;
import com.rikai.backend.mapper.InternshipBatchMapper;
import com.rikai.backend.model.InternshipBatch;
import com.rikai.backend.repository.InternshipBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InternshipBatchService implements IInternshipBatchService {
    private final InternshipBatchRepository internshipBatchRepository;
    private final InternshipBatchMapper internshipBatchMapper;

    @Override
    public PageResponse<InternshipBatchResponse> getAllInternshipBatches(String keyword, PageRequest pageRequest) {
        Page<InternshipBatch> internshipBatchPage = internshipBatchRepository.getAllInternshipBatchByKeyword(keyword, pageRequest);
        return PageResponse.fromPage(
                internshipBatchPage.map(internshipBatchMapper::toInternshipBatchResponse)
        );
    }
}
