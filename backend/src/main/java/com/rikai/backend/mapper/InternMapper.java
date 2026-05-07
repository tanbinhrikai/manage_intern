package com.rikai.backend.mapper;

import com.rikai.backend.dto.request.intern.InternCreationRequest;
import com.rikai.backend.dto.response.batch.InternshipBatchResponse;
import com.rikai.backend.dto.response.intern.InternResponse;
import com.rikai.backend.dto.response.mentor.MentorResponse;
import com.rikai.backend.dto.response.position.PositionResponse;
import com.rikai.backend.model.Intern;
import com.rikai.backend.model.InternshipBatch;
import com.rikai.backend.model.Position;
import com.rikai.backend.model.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InternMapper {

    @Mapping(target = "position", source = "position")
    @Mapping(target = "mentor", source = "mentor")
    @Mapping(target = "internshipBatch", source = "internshipBatch")
    InternResponse toInternResponse(Intern intern);

    PositionResponse toPositionResponse(Position position);

    MentorResponse toMentorResponse(Users users);

    InternshipBatchResponse toInternshipBatchResponse(InternshipBatch internshipBatch);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "mentor", ignore = true)
    @Mapping(target = "internshipBatch", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "internStatus", ignore = true)
    Intern toIntern(InternCreationRequest request);
}
