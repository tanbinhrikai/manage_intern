package com.rikai.backend.service.position;

import com.rikai.backend.dto.request.PositionCreationRequest;
import com.rikai.backend.dto.request.PositionUpdateRequest;
import com.rikai.backend.dto.response.PositionResponse;

import java.util.List;

public interface IPositionService {

    List<PositionResponse> getAllPositionsList();

    PositionResponse getPositionById(Long id);

    PositionResponse createPosition(PositionCreationRequest request);

    PositionResponse updatePosition(Long id, PositionUpdateRequest request);
}
