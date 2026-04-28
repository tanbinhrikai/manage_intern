package com.rikai.backend.ai.tool;

import com.rikai.backend.ai.dto.request.AddNodeRequest;
import com.rikai.backend.ai.dto.request.RemoveNodeRequest;
import com.rikai.backend.ai.dto.request.UpdateNodeRequest;
import com.rikai.backend.ai.service.roadmap.DraftRoadmapManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.BiFunction;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RoadmapEditingTools {
    private final DraftRoadmapManager draftManager;

    @Bean
    @Description("Thêm một Node mới (Module, Lesson, Task) vào lộ trình học tập.")
    public BiFunction<AddNodeRequest, ToolContext, String> addNodeTool() {
        return (request, context) -> {
            String sessionId = (String) context.getContext().get("sessionId");
            log.info("Tool AddNode called for Session: {}", sessionId);

            if (sessionId == null || !draftManager.exists(sessionId)) {
                return "Error: Session not found (Session ID is missing or expired).";
            }
            boolean success = draftManager.addNodeByFuzzyName(
                    sessionId,
                    request.targetParentName(),
                    request.newNodeTitle(),
                    request.nodeType(),
                    request.description()
            );
            if (success) {
                return "Success: Added '" + request.newNodeTitle() + "' to '" + request.targetParentName() + "'.";
            } else {
                return "Failure: No parent item with name matching '" + request.targetParentName() + "'.";
            }
        };
    }

    @Bean
    @Description("Xóa một Node (Bài học, Module) khỏi lộ trình.")
    public BiFunction<RemoveNodeRequest, ToolContext, String> removeNodeTool() {
        return (request, context) -> {
            String sessionId = (String) context.getContext().get("sessionId");
            log.info("Tool RemoveNode called for Session: {}", sessionId);
            if (sessionId == null || !draftManager.exists(sessionId)) {
                return "Error: Invalid session.";
            }
            boolean success = draftManager.removeNodeByFuzzyName(sessionId, request.targetNodeName());
            if (success) {
                return "Success: Item has been removed " + request.targetNodeName() + "'.";
            } else {
                return "Failure: No item found with name matching '" + request.targetNodeName() + "'.";
            }
        };
    }

    @Bean
    @Description("Cập nhật tiêu đề hoặc mô tả của một Node trong lộ trình.")
    public BiFunction<UpdateNodeRequest, ToolContext, String> updateNodeTool() {
        return (request, context) -> {
            String sessionId = (String) context.getContext().get("sessionId");
            log.info("Tool UpdateNode called for Session: {}", sessionId);
            if (sessionId == null || !draftManager.exists(sessionId)) {
                return "Error: Session not found (Session ID is missing or expired).";
            }
            boolean success = draftManager.updateNodeByFuzzyName(
                    sessionId,
                    request.targetNodeName(),
                    request.newTitle(),
                    request.newDescription()
            );
            if (success) {
                return "Success: Updated node matching '" + request.targetNodeName() + "'.";
            } else {
                return "Failure: No item found with name matching '" + request.targetNodeName() + "'.";
            }
        };
    }
}