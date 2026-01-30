package com.rikai.backend.mapper;

import com.rikai.backend.dto.response.chat_memory.ChatMemoryResponse;
import com.rikai.backend.model.ChatMemory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ChatMemoryMapper {

    @Mapping(target = "type", source = "type")
    ChatMemoryResponse toChatMemoryResponse(ChatMemory chatMemory);

    List<ChatMemoryResponse> toChatMemoryResponseList(List<ChatMemory> chatMemories);
}
