package com.rikai.backend.controller;

import com.rikai.backend.model.Test;
import com.rikai.backend.service.ai.AiGenericService;
import com.rikai.backend.service.interntest.TestInternService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final AiGenericService aiGenericService;
    private final TestInternService testInternService;
    @PostMapping("/generic")

    public ResponseEntity<Void> test(){
        testInternService.autoCreateInternTests();
        return ResponseEntity.noContent().build();
    }
}
