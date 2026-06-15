package com.rikai.backend.service.position;

import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.request.position.PositionCreationRequest;
import com.rikai.backend.dto.request.position.PositionUpdateRequest;
import com.rikai.backend.dto.response.position.PositionResponse;
import org.springframework.data.domain.Pageable;

public interface IPositionService {

    PageResponse<PositionResponse> getAllPositionsList(Pageable pageable , String keyword);

    PositionResponse getPositionById(Long id);

    PositionResponse createPosition(PositionCreationRequest request);

    PositionResponse updatePosition(Long id, PositionUpdateRequest request);

    boolean deletePostion(Long id);
}
