package com.rikai.backend.controller;

import com.rikai.backend.ai.dto.response.ChatResponseDto;
import com.rikai.backend.dto.request.agent_ai.ChatAIDto;
import com.rikai.backend.dto.request.agent_ai.ChatRequestDto;
import com.rikai.backend.model.RoadmapNode;
import com.rikai.backend.service.roadmap.RoadmapGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roadmaps")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapGeneratorService roadmapGeneratorService;

    /**
     * API Chat xã giao (Debug/Test): Chỉ trả về text, không xử lý logic phức tạp.
     * Dành cho Mentor/Admin test model.
     */
    @PostMapping("/chat")
    @PreAuthorize("hasRole('MENTOR') or hasRole('ADMIN')")
    public ResponseEntity<String> chatAI(@RequestBody ChatAIDto message) {
        String response = roadmapGeneratorService.chatWithAI(message);
        return ResponseEntity.ok(response);
    }

    /**
     * API CHÍNH (User UI): Chat thông minh & Tạo lộ trình
     * - Tự động phát hiện ý định (Chat hay Tạo lộ trình)
     * - Tự động hỏi lại nếu thiếu thông tin (Position, Duration)
     * - Trả về ActionType để Frontend render (Dropdown/Text/Roadmap)
     */
    @PostMapping("/chat-process")
    public ResponseEntity<ChatResponseDto> processMessage(
            @RequestBody ChatRequestDto request
    ) {
        ChatResponseDto response = roadmapGeneratorService.processUserMessage(
                request.getMessage(),
                request.getPositionId(),
                request.getDuration(),
                request.getBatchId()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * API ADMIN: Tạo lộ trình nhanh (Bỏ qua chat)
     * URL: POST /api/roadmaps/generate?topic=Java&positionId=1&batchId=1&duration=6 tháng
     * - Thêm param 'duration' để Admin tùy chỉnh thời gian.
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ResponseEntity<?> generateRoadmap(
            @RequestParam String topic,
            @RequestParam Long positionId,
            @RequestParam Long batchId,
            @RequestParam(required = false, defaultValue = "3 tháng") String duration
    ) {
        try {
            String notes = "Thời gian đào tạo: " + duration;
            RoadmapNode rootNode = roadmapGeneratorService.generateAndSaveRoadmap(topic, notes, positionId, batchId);
            return ResponseEntity.ok()
                    .body("Tạo lộ trình thành công! Root Node ID: " + rootNode.getId());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Lỗi logic: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}