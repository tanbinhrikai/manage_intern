package com.rikai.backend.service.position;

import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.request.PositionCreationRequest;
import com.rikai.backend.dto.request.PositionUpdateRequest;
import com.rikai.backend.dto.response.PositionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IPositionService {

    PageResponse<PositionResponse> getAllPositionsList(Pageable pageable , String keyword);

    PositionResponse getPositionById(Long id);

    PositionResponse createPosition(PositionCreationRequest request);

    PositionResponse updatePosition(Long id, PositionUpdateRequest request);
}
