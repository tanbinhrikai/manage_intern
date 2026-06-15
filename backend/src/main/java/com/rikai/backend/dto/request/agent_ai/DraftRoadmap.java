package com.rikai.backend.dto.request.agent_ai;

import com.rikai.backend.model.RoadmapNode;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder

// roadMap tạm thời
public class DraftRoadmap {
    private final String sessionId;       // ID phiên làm việc → biết roadmap này của ai
    private RoadmapNode rootNode;         // Cây roadmap AI tạo ra (có thể chỉnh sửa)
    private final Long positionId;        // Vị trí công việc (Java Intern, ReactJS Intern,...)
    private final Long batchId;           // Thuộc batch nào (Batch 2025 kỳ 1,...)
    private final String duration;        // Thời gian học ("3 tháng", "6 tháng",...)
    private Instant lastModified;         // Lần cuối chỉnh sửa
    private final String additionalNotes; // Ghi chú thêm từ user
}

//
//User: "Tạo roadmap Java 3 tháng"
//        ↓
//AI tạo roadmap → lưu vào DraftRoadmap (chưa vào DB)
//        ↓
//User xem và chỉnh sửa
//        ↓
//User confirm → save DraftRoadmap vào DB  ✅
//User hủy    → xóa DraftRoadmap đi  ❌