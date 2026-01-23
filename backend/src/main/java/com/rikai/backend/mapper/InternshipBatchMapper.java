package com.rikai.backend.mapper;


import com.rikai.backend.dto.request.batch.InternshipBatchCreationRequest;
import com.rikai.backend.dto.request.batch.InternshipBatchUpdateRequest;
import com.rikai.backend.dto.response.batch.InternshipBatchResponse;
import com.rikai.backend.model.InternshipBatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {UserMapper.class, InternMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InternshipBatchMapper {

    @Mapping(target = "internResponses", source = "interns")
    InternshipBatchResponse toInternshipBatchResponse(InternshipBatch internshipBatch);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "interns", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    InternshipBatch toInternshipBatch(InternshipBatchCreationRequest internshipBatchCreationRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "interns", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateInternshipBatch(@MappingTarget InternshipBatch internshipBatch , InternshipBatchUpdateRequest  internshipBatchUpdateRequest);
}
