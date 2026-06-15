package com.rikai.backend.dto.response.testintern;

import java.util.List;

public class TestResponseDto {
    private Long testId;
    private String title;
    private int evaluationNumber;

    private List<QuestionResponseDto> questions;
}
