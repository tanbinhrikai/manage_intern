package com.rikai.backend.ai.dto.response;


// class dùng để  AI phân tích ý định của user xem có muốn tạo roadmap k và định học gì
public record UserIntentDto(
        boolean isRoadmapRequest,      // có phải yêu cầu tạo roadmap không
        String detectedTopic,          // chủ đề được phát hiện (Java, ReactJS,...)
        String detectedDuration,       // thời gian học (1 tháng, 3 tháng,...)
        Double estimatedHours,         // ước tính số giờ học
        String additionalNotes,        // ghi chú thêm
        String conversationalReply     // câu trả lời hội thoại với user
) {}