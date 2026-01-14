package com.rikai.backend.service.position;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.dto.request.PositionCreationRequest;
import com.rikai.backend.dto.request.PositionUpdateRequest;
import com.rikai.backend.dto.response.PositionResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.mapper.PositionMapper;
import com.rikai.backend.model.Position;
import com.rikai.backend.repository.PositionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PositionService implements IPositionService {
    PositionRepository positionRepository;
    PositionMapper positionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PositionResponse> getAllPositionsList() {
        return positionRepository.findAll()
                .stream()
                .map(positionMapper::toPositionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PositionResponse getPositionById(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_EXISTED));
        return positionMapper.toPositionResponse(position);
    }

    @Override
    @Transactional
    public PositionResponse createPosition(PositionCreationRequest request) {
        Position position = positionMapper.toPosition(request);
        Position saved = positionRepository.save(position);
        return positionMapper.toPositionResponse(saved);
    }

    @Override
    @Transactional
    public PositionResponse updatePosition(Long id, PositionUpdateRequest request) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_EXISTED));

        position.setTitle(request.getTitle());

        Position saved = positionRepository.save(position);
        return positionMapper.toPositionResponse(saved);
    }
}
