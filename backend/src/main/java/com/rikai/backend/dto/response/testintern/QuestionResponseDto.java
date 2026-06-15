package com.rikai.backend.dto.response.testintern;


import java.util.List;

public class QuestionResponseDto {
    private Long questionId;
    private String content;
    private String type;

    private List<OptionResponseDto> options;
}