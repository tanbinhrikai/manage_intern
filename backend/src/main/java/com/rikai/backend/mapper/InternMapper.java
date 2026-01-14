package com.rikai.backend.mapper;

import com.rikai.backend.dto.request.InternCreationRequest;
import com.rikai.backend.dto.response.InternResponse;
import com.rikai.backend.dto.response.MentorResponse;
import com.rikai.backend.dto.response.PositionResponse;
import com.rikai.backend.model.Intern;
import com.rikai.backend.model.Position;
import com.rikai.backend.model.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InternMapper {

    @Mapping(target = "position", source = "position")
    @Mapping(target = "mentor", source = "mentor")
    InternResponse toInternResponse(Intern intern);

    PositionResponse toPositionResponse(Position position);

    MentorResponse toMentorResponse(Users users);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "mentor", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "internStatus", ignore = true)
    Intern toIntern(InternCreationRequest request);
}
