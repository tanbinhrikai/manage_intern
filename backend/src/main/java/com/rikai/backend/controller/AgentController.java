package com.rikai.backend.controller;

import com.rikai.backend.ai.agent.SuperAgentService;
import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.agent.ChatRequest;
import com.rikai.backend.dto.response.agent.ChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for AI Agent chat endpoint.
 */
@RestController
@RequestMapping("agent")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level =  AccessLevel.PRIVATE , makeFinal = true)
public class AgentController {

   SuperAgentService superAgentService;

    @PostMapping("/chat")
    @Operation(summary = "Chat with AI Agent", description = "Send a message to the AI agent. The agent can answer questions about interns, mentors, weekly reports, and evaluations.")
    public ApiResponse<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = superAgentService.chat(request);
        return ApiResponse.buildSuccessResponse(response , SuccessCode.AGENT_RESPONSE_SUCCESSFUL);
    }
}
