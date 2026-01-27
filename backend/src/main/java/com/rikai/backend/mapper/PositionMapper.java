package com.rikai.backend.mapper;

import com.rikai.backend.dto.request.position.PositionCreationRequest;
import com.rikai.backend.dto.response.position.PositionResponse;
import com.rikai.backend.model.Position;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring" , nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PositionMapper {

    PositionResponse toPositionResponse(Position position);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "interns", ignore = true)
    @Mapping(target = "department" , ignore = true)
    @Mapping(target = "description" , ignore = true)
    Position toPosition(PositionCreationRequest request);
}
