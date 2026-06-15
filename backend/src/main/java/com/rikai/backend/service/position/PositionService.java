package com.rikai.backend.service.position;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.request.position.PositionCreationRequest;
import com.rikai.backend.dto.request.position.PositionUpdateRequest;
import com.rikai.backend.dto.response.position.PositionResponse;
import com.rikai.backend.event.DepartmentCudEvent;
import com.rikai.backend.event.PositionCudEvent;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.mapper.PositionMapper;
import com.rikai.backend.model.Department;
import com.rikai.backend.model.Position;
import com.rikai.backend.repository.InternRepository;
import com.rikai.backend.repository.PositionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PositionService implements IPositionService {
    PositionRepository positionRepository;
    PositionMapper positionMapper;
    InternRepository internRepository;
    ApplicationEventPublisher eventPublisher;

    @Override
    public PageResponse<PositionResponse> getAllPositionsList(Pageable pageable , String keyword) {

        String keywordValue = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        Page<Position> positions = positionRepository.getAllPositionByKeyword(pageable , keywordValue);
        return PageResponse.fromPage(positions.map(pos -> {
            PositionResponse response = positionMapper.toPositionResponse(pos);
            response.setInternCount(internRepository.countByPosition_Id(pos.getId()));
            return response;
        }));
    }

    @Override
    public PositionResponse getPositionById(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_EXISTED));
        return positionMapper.toPositionResponse(position);
    }

    @Override
    public PositionResponse createPosition(PositionCreationRequest request) {
        Position position = positionMapper.toPosition(request);
        Position saved = positionRepository.save(position);
        eventPublisher.publishEvent(new PositionCudEvent(this, "CREATED", saved));
        return positionMapper.toPositionResponse(saved);
    }

    @Override
    public PositionResponse updatePosition(Long id, PositionUpdateRequest request) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_EXISTED));

        position.setTitle(request.getTitle());
            Position saved = positionRepository.save(position);
        eventPublisher.publishEvent(new PositionCudEvent(this, "UPDATED", saved));
        return positionMapper.toPositionResponse(saved);
    }

     @Override
    public boolean deletePostion(Long id) {
        try {
            Position position = positionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_EXISTED));
            positionRepository.delete(position);
            eventPublisher.publishEvent(new PositionCudEvent(this, "DELETED", position));
            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }


}
