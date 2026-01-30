package com.rikai.backend.ai.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponseDto {
    private String message;      // Lời thoại của AI (VD: "Vui lòng chọn vị trí bạn muốn học")
    private ActionType action;   // Loại hành động Frontend cần làm
    private Object data;         // Dữ liệu kèm theo (List positions, List months...)

    public enum ActionType {
        NORMAL_CHAT,        // Chỉ hiện text bình thường
        SELECT_POSITION,    // Hiện dropdown chọn Position
        SELECT_DURATION,    // Hiện dropdown chọn thời gian
        DISPLAY_ROADMAP     // Hiện lộ trình đã tạo xong
    }
}