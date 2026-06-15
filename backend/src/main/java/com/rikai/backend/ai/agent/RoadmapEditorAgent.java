package com.rikai.backend.ai.agent;

import com.rikai.backend.ai.service.roadmap.DraftRoadmapManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;


// chỉnh sửa roadmap
@Service
public class RoadmapEditorAgent {
    private final ChatClient chatClient;

    // quản lý roadmap tạm thời
    private final DraftRoadmapManager draftManager;

    public RoadmapEditorAgent(@Qualifier("creatorClient") ChatClient chatClient, DraftRoadmapManager draftManager) {
        this.chatClient = chatClient;
        this.draftManager = draftManager;
    }


    // fix sau : nên lấy theo userId vì sesionId có thể bị thay đổi liên tục
    public String processEditRequest(String userMessage, String sessionId) {
        return processEditRequest(userMessage, sessionId, sessionId);
    }


    public String processEditRequest(String userMessage, String sessionId, String conversationId) {
        String currentStructure = draftManager.getSkeletonStructure(sessionId);
        String systemPrompt = """
                Bạn là trợ lý chỉnh sửa lộ trình học tập.
                CẤU TRÚC LỘ TRÌNH HIỆN TẠI:
                %s
                NHIỆM VỤ:
                Dựa vào yêu cầu người dùng, hãy gọi tool thích hợp (addNode, removeNode, updateNode) để chỉnh sửa.
                Nếu người dùng nói tên không chính xác tuyệt đối, hãy cố gắng suy luận tên gần đúng nhất trong cấu trúc trên.
                Sau khi chỉnh sửa xong, hãy phản hồi bằng tiếng Việt cho người dùng biết kết quả.
                """.formatted(currentStructure);
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .tools("addNodeTool", "removeNodeTool", "updateNodeTool")
                .toolContext(Map.of("sessionId", sessionId))
                .advisors(a -> a.param(CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}
