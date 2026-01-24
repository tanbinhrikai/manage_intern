package com.rikai.backend.mapper;

import com.rikai.backend.dto.response.internship_batch.InternshipBatchResponse;
import com.rikai.backend.model.InternshipBatch;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InternshipBatchMapper {
    InternshipBatchResponse toInternshipBatchResponse(InternshipBatch internshipBatch);
}
