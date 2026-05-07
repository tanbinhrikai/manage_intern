package com.rikai.backend.ai.tool;

import com.rikai.backend.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RoadmapToolsConfig {

    private final PositionRepository positionRepository;

    /**
     * TOOL 1: Tìm kiếm vị trí trong Database
     * AI sẽ tự quyết định khi nào cần gọi hàm này dựa trên @Description
     */
    @Bean
    @Description("Tìm kiếm thông tin vị trí công việc (Position) trong hệ thống dựa trên từ khóa. Trả về ID và Tên đầy đủ.")
    public Function<CallTool.PositionQuery, CallTool.PositionResult> searchPositionTool() {
        return request -> {
            log.info("AI đang gọi Tool tìm kiếm vị trí: {}", request.keyword());
            return positionRepository.findByTitleContainingIgnoreCase(request.keyword())
                    .stream()
                    .findFirst()
                    .map(pos -> new CallTool.PositionResult(pos.getId(), pos.getTitle(), pos.getDescription()))
                    .orElse(new CallTool.PositionResult(null, "Không tìm thấy", "Hệ thống không có vị trí này"));
        };
    }
}
