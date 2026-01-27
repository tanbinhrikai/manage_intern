package com.rikai.backend.mapper;

import com.rikai.backend.dto.request.intern.InternCreationRequest;
import com.rikai.backend.dto.response.intern.InternResponse;
import com.rikai.backend.dto.response.mentor.MentorResponse;
import com.rikai.backend.dto.response.position.PositionResponse;
import com.rikai.backend.model.Intern;
import com.rikai.backend.model.Position;
import com.rikai.backend.model.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
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
    @Mapping(target = "internshipBatch" , ignore = true)
    @Mapping(target = "offerStatus" , ignore = true)
    @Mapping(target = "offerDate" , ignore = true)
    @Mapping(target = "offerNotes" , ignore = true)
    @Mapping(target = "email" , ignore = true)
    @Mapping(target = "phone" , ignore = true)
    Intern toIntern(InternCreationRequest request);
}
