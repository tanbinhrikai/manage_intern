package com.rikai.backend.ai.agent;

import com.rikai.backend.ai.security.AgentSecurityAdvisor;
import com.rikai.backend.ai.tools.*;
import com.rikai.backend.dto.request.agent.ChatRequest;
import com.rikai.backend.dto.response.agent.ChatResponse;
import com.rikai.backend.model.Users;
import com.rikai.backend.service.auth.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

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
            - courseGeneratorTool : Tao khoa hoc
            
            ## Tools Quản lý Khóa học (Course Generator):
            - generateCourse: Tạo mới một lộ trình học từ đầu.
            - deleteCourse: Xóa khóa học.
            - updateCourseStructure: Dùng khi user muốn sửa đổi LỚN (thêm/xóa Module, thay đổi tiêu đề khóa học).
            - updateTaskSpecifics: Dùng khi user muốn sửa chi tiết NHỎ trong 1 task (tìm link khác, viết lại mô tả chi tiết hơn, lồng nội dung học vào mô tả). HÃY ƯU TIÊN DÙNG TOOL NÀY ĐỂ TIẾT KIỆM CHI PHÍ nếu user chỉ yêu cầu sửa nội dung task.
            """;

    private final ChatClient chatClient;
    private final AuthenticationService authenticationService;
    private final AgentSecurityAdvisor agentSecurityAdvisor;

    public SuperAgentService(
            ChatClient.Builder chatClientBuilder,
            AuthenticationService authenticationService,
            InternProfileTool internProfileTool,
            WeeklyReportTool weeklyReportTool,
            EvaluationSessionTool evaluationSessionTool,
            CourseGeneratorTool courseGeneratorTool ,
            AgentSecurityAdvisor agentSecurityAdvisor,
            ChatMemory chatMemory,
            MentorInternsTool mentorInternsTool) {
        this.authenticationService = authenticationService;
        this.agentSecurityAdvisor = agentSecurityAdvisor;
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(internProfileTool, weeklyReportTool, evaluationSessionTool,courseGeneratorTool , mentorInternsTool)
                .build();
        //log.info("SuperAgentService initialized with ChatClient and 5 AI tools");
    }

    public ChatResponse chat(ChatRequest request) {
        log.info("Processing chat request: {}", request.getMessage());

        try {
            String conversationId = (request.getConversationId() != null && !request.getConversationId().isEmpty())
                    ? request.getConversationId()
                    : UUID.randomUUID().toString();
            Users currentUser = authenticationService.getCurrentUser();
            String userContext = buildUserContext(currentUser);

            String enhancedPrompt = userContext + "\n\nCâu hỏi của người dùng: " + request.getMessage();

            String response = chatClient.prompt()
                    .user(enhancedPrompt)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call()
                    .content();

            log.info("Chat response generated successfully");
            return ChatResponse.builder()
                    .message(response)
                    .timestamp(Instant.now())
                    .conversationId(conversationId).build();

        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return ChatResponse.builder()
                    .timestamp(Instant.now())
                    .message("Xin lỗi, đã có lỗi xảy ra khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.").build();
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
        context.append("- Time: ").append(LocalDate.now());

        if (agentSecurityAdvisor.isAdminOrHR()) {
            context.append("\n## Quyền hạn: ADMIN/HR - Toàn quyền truy cập.\n");
        } else {
            UUID mentorId = agentSecurityAdvisor.getCurrentMentorId();
            if (mentorId != null) {
                context.append("\n## Quyền hạn: MENTOR - Chỉ xem intern mình quản lý (ID: ").append(mentorId).append(").\n");
            } else {
                context.append("\n## Quyền hạn: Hạn chế.\n");
            }
        }

        return context.toString();
    }
}
