package com.rikai.backend.dto.request.agent_ai;

import lombok.Data;

import java.util.List;

@Data
public class GeneratedTestDto {

    private String title;

    private List<QuestionDto> questions;

    @Data
    public static class QuestionDto {

        private String content;

        private List<OptionDto> options;
    }

    @Data
    public static class OptionDto {

        private String content;

        private boolean correct;
    }
}