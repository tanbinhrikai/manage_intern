package com.rikai.backend.ai.agent;

import com.rikai.backend.ai.tools.EvaluationSessionTool;
import com.rikai.backend.ai.tools.InternProfileTool;
import com.rikai.backend.ai.tools.MentorInternsTool;
import com.rikai.backend.ai.tools.WeeklyReportTool;
import com.rikai.backend.dto.request.agent.ChatRequest;
import com.rikai.backend.dto.response.agent.ChatResponse;
import com.rikai.backend.model.Users;
import com.rikai.backend.service.auth.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SuperAgentService {

    private static final String SYSTEM_PROMPT = """
            Bạn là Quản lý Đào tạo AI của Rikai Technology.

            ## Nhiệm vụ:
            Trả lời mọi thắc mắc về thực tập sinh, mentor và quy trình đánh giá.

            ## Quy tắc xử lý:
            1. Luôn ưu tiên dùng DATA thực tế từ các Tools được cung cấp.
            2. Nếu người dùng hỏi về "Điểm số", hãy dùng Tool lấy báo cáo tuần.
            3. Tuyệt đối không bịa đặt thông tin. Nếu không có data, hãy nói "Tôi không tìm thấy thông tin".
            4. Trả lời ngắn gọn, súc tích, chuyên nghiệp bằng Tiếng Việt.
            5. Format câu trả lời rõ ràng với bullet points khi cần thiết.

            ## Tools có sẵn:
            - findInternProfile: Tìm thông tin intern theo tên hoặc keyword
            - getWeeklyReportAnalysis: Lấy báo cáo tuần và điểm số của intern
            - getEvaluationSessionResults: Lấy kết quả đánh giá chính thức (FIRST_TERM, MID_TERM, FINAL)
            - getInternsByMentor: Lấy danh sách intern của một mentor
            """;

    private final ChatClient chatClient;
    private final AuthenticationService authenticationService;

    public SuperAgentService(
            ChatClient.Builder chatClientBuilder,
            AuthenticationService authenticationService,
            InternProfileTool internProfileTool,
            WeeklyReportTool weeklyReportTool,
            EvaluationSessionTool evaluationSessionTool,
            MentorInternsTool mentorInternsTool) {

        this.authenticationService = authenticationService;

        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(internProfileTool, weeklyReportTool, evaluationSessionTool, mentorInternsTool)
                .build();

        log.info("SuperAgentService initialized with ChatClient and 4 AI tools");
    }

    public ChatResponse chat(ChatRequest request) {
        log.info("Processing chat request: {}", request.getMessage());

        try {
            // Get current user for context injection
            Users currentUser = authenticationService.getCurrentUser();
            String userContext = buildUserContext(currentUser);

            String enhancedPrompt = userContext + "\n\nCâu hỏi của người dùng: " + request.getMessage();

            String response = chatClient.prompt()
                    .user(enhancedPrompt)
                    .call()
                    .content();

            log.info("Chat response generated successfully");
            return ChatResponse.of(response);

        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return ChatResponse.of("Xin lỗi, đã có lỗi xảy ra khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.");
        }
    }

    private String buildUserContext(Users currentUser) {
        if (currentUser == null) {
            return "Người dùng chưa đăng nhập.";
        }

        String roleName = currentUser.getRole() != null ? currentUser.getRole().getRoleName() : "UNKNOWN";

        StringBuilder context = new StringBuilder();
        context.append("## Thông tin người hỏi:\n");
        context.append("- Tên: ").append(currentUser.getFullName()).append("\n");
        context.append("- Email: ").append(currentUser.getEmail()).append("\n");
        context.append("- Role: ").append(roleName).append("\n");

        switch (roleName) {
            case "ADMIN":
            case "HR":
                context.append("\n## Quyền hạn: Có toàn quyền truy cập mọi thông tin.\n");
                break;
            case "MENTOR":
                context.append("\n## Quyền hạn: Chỉ được xem thông tin của các intern do mình hướng dẫn.\n");
                context.append("Mentor ID: ").append(currentUser.getId()).append("\n");
                break;
            default:
                context.append("\n## Quyền hạn: Quyền truy cập hạn chế.\n");
                break;
        }

        return context.toString();
    }
}
