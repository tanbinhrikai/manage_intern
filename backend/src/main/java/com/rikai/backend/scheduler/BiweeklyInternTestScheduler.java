package com.rikai.backend.scheduler;

import com.rikai.backend.service.interntest.TestInternService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BiweeklyInternTestScheduler {

    private final TestInternService testInternService;
    @Scheduled(cron = "0 0 8 * * *")
    public void createInternTests() {


    }
}
