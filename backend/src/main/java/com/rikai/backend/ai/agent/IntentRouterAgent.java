package com.rikai.backend.ai.agent;

import com.rikai.backend.ai.dto.response.UserIntentDto;
import com.rikai.backend.ai.prompt.PromptManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;


@Component

// class gởi tin nhắn của user lên Al, cho AL phân tích
public class IntentRouterAgent {

    // class của spring al -> giao tiêp vs các Model AL
    private final ChatClient chatClient;

    public IntentRouterAgent(@Qualifier("routerClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }



    public UserIntentDto analyze(String userMessage, String conversationId) {
        return chatClient.prompt()
                .system(PromptManager.INTENT_ANALYSIS_SYSTEM)
                // tin nhắn của user gởi lên
                .user(userMessage)
                // câu hình cho AL nhớ lịch sử, câu hỏi trc đó    , conversationId là phòng chat cũng như nhớ lịch sử của mỗi ng riêng
                .advisors(a -> a.param(CONVERSATION_ID, conversationId))
                .call()
                // kết quả trả về
                .entity(UserIntentDto.class);
    }
}


// User nhắn: "Tôi muốn học Java trong 3 tháng"
//        ↓
//analyze() gửi lên AI kèm INTENT_ANALYSIS_SYSTEM prompt
//        ↓
//AI phân tích
//        ↓
//Trả về UserIntentDto:
//{
//  isRoadmapRequest: true,
//  detectedTopic: "Java",
//  detectedDuration: "3 tháng",
//  estimatedHours: 120.0,
//  additionalNotes: "...",
//  conversationalReply: "Tôi sẽ tạo roadmap Java 3 tháng cho bạn!"
//}