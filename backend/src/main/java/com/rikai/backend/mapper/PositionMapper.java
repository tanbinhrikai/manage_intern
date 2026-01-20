package com.rikai.backend.mapper;

import com.rikai.backend.dto.request.position.PositionCreationRequest;
import com.rikai.backend.dto.response.position.PositionResponse;
import com.rikai.backend.model.Position;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PositionMapper {

    PositionResponse toPositionResponse(Position position);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "interns", ignore = true)
    Position toPosition(PositionCreationRequest request);
}
