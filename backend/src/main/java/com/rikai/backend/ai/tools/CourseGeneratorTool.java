package com.rikai.backend.ai.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rikai.backend.dto.response.ai.CoursePlanDTO;
import com.rikai.backend.model.*;
import com.rikai.backend.model.Enum.TaskStatus;
import com.rikai.backend.repository.*;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.google.genai.common.GoogleGenAiThinkingLevel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@Slf4j
public class CourseGeneratorTool {

    @NonFinal
    @Value("${TYPE_MODEL_GEMINI}")
    String TYPE_MODEL_GEMINI;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final LearningPlanRepository planRepo;
    private final PlanModuleRepository moduleRepo;
    private final PlanTaskRepository taskRepo;


    private static final String JSON_SCHEMA_EXAMPLE = """
        {
          "title": "Senior Java Fullstack Architecture Roadmap (6 Months)",
          "description": "An exhaustive, production-grade roadmap transforming a mid-level developer into a Senior Architect. Focuses on High Performance, Distributed Systems, Cloud Native patterns, and Advanced Security.",
          "modules": [
            {
              "title": "Phase 1: JVM Internals & High-Performance Java",
              "focus_topic": "Deep Java",
              "tasks": [
                {
                  "title": "JVM Memory Management & Garbage Collection Tuning",
                  "description": "Master the Heap structure (Eden, Survivor, Old Gen). Analyze GC algorithms (G1, ZGC, Shenandoah). Learn to interpret GC logs and use tools like JVisualVM and Java Flight Recorder to diagnose Memory Leaks.",
                  "resource_link": "https://docs.oracle.com/en/java/javase/21/gctuning/",
                  "estimated_minutes": 480
                },
                {
                  "title": "Advanced Concurrency Models",
                  "description": "Move beyond `synchronized`. Implement non-blocking algorithms using `Atomic` variables and `StampedLock`. Master Java 21 Virtual Threads (Project Loom) for high-throughput I/O. Contrast Reactive Programming (WebFlux) vs Virtual Threads.",
                  "resource_link": "https://spring.io/blog/2023/10/16/runtime-efficiency-with-project-loom",
                  "estimated_minutes": 600
                }
              ]
            },
            {
              "title": "Phase 2: Distributed Systems & Microservices Patterns",
              "focus_topic": "System Design",
              "tasks": [
                {
                  "title": "Microservices Communication & Resiliency",
                  "description": "Implement synchronous (Feign/REST) vs asynchronous (Kafka/RabbitMQ) communication. Apply resiliency patterns: Circuit Breaker (Resilience4j), Bulkhead, Retry, and Rate Limiter. Understand Distributed Tracing with Zipkin/Micrometer.",
                  "resource_link": "https://microservices.io/patterns/index.html",
                  "estimated_minutes": 720
                },
                {
                  "title": "Event-Driven Architecture (EDA) Deep Dive",
                  "description": "This is a massive topic requiring a dedicated study path on Event Sourcing and CQRS.",
                  "estimated_minutes": 1200,
                  "sub_plan": {
                    "title": "Mastering Event-Driven Systems",
                    "description": "Specialized track for Kafka and Event Sourcing patterns.",
                    "modules": [
                      {
                        "title": "Apache Kafka Internals",
                        "focus_topic": "Streaming",
                        "tasks": [
                          {
                            "title": "Kafka Architecture",
                            "description": "Understand Topics, Partitions, Offsets, Consumer Groups, and ISR (In-Sync Replicas). Configure `acks=all` for durability.",
                            "estimated_minutes": 240
                          },
                          {
                            "title": "Kafka Streams API",
                            "description": "Build stateful stream processing applications (KStream, KTable). Implement Windowing and Aggregations.",
                            "estimated_minutes": 360
                          }
                        ]
                      },
                      {
                        "title": "CQRS & Event Sourcing",
                        "focus_topic": "Design Patterns",
                        "tasks": [
                          {
                            "title": "Implementing Axon Framework",
                            "description": "Separate Read and Write models. Replay events to rebuild state. Handle eventual consistency challenges.",
                            "estimated_minutes": 400
                          }
                        ]
                      }
                    ]
                  }
                }
              ]
            }
          ]
        }
        """;

    private static final String GENERATION_PROMPT = """
        You are a Distinguished Technical Architect and Educator.
        
        GOAL: Create an EXTENSIVE, COMPREHENSIVE, and DETAILED learning path.
        Topic: {topic}
        Duration: {duration}

        CRITICAL INSTRUCTIONS (MUST FOLLOW):
        1.  **MAXIMUM DETAIL**: Do not be brief. Each task description must be a paragraph explaining *what*, *why*, and *how*.
        2.  **RECURSIVE DEPTH**: For every major concept (e.g., "Security", "Database", "Cloud"), you MUST create a `sub_plan` instead of a simple task. I want a deep nested structure.
        3.  **VOLUME**: The output should contain at least 4-6 major Modules, and each Module must have 4-6 Tasks.
        4.  **STRICT JSON**: Output ONLY valid JSON.
        
        USE THIS JSON STRUCTURE (See how detailed the description and sub_plans are):
        {json_schema}
        """;

    public CourseGeneratorTool(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper, LearningPlanRepository planRepo, PlanModuleRepository moduleRepo, PlanTaskRepository taskRepo) {
        this.chatClient = chatClientBuilder
                .defaultSystem("You are an expert curriculum designer.")
                .build();
        this.objectMapper = objectMapper;
        this.planRepo = planRepo;
        this.moduleRepo = moduleRepo;
        this.taskRepo = taskRepo;
    }

    @Tool(description = "Generate a structured learning course/roadmap. Use this when user asks to 'create a course', 'design a learning path' or 'study plan'. Nếu ")
    @Transactional
    public String generateCourse(
            @ToolParam(description = "Topic/Technology (e.g. 'ReactJS', 'Spring Boot')") String topic,
            @ToolParam(description = "Duration (e.g. '2 weeks')") String duration
    ) {
        log.info("Generating course for topic: {}", topic);

        try {
            PromptTemplate template = new PromptTemplate(GENERATION_PROMPT);
            String promptContent = template.render(Map.of(
                    "topic", topic,
                    "duration", duration ,
                    "json_schema" , JSON_SCHEMA_EXAMPLE
            ));

            log.info("prompt {}" ,promptContent);
            var options = GoogleGenAiChatOptions.builder()
                    .model(TYPE_MODEL_GEMINI)
                    .thinkingLevel(GoogleGenAiThinkingLevel.HIGH)
                    .temperature(0.7)
                    .maxOutputTokens(8000)
                    .build();
            String jsonResponse = chatClient.prompt()
                    .user(promptContent)
                    .options(options)
                    .call()
                    .content();
            assert jsonResponse != null;
            jsonResponse = cleanJson(jsonResponse);

            CoursePlanDTO planDTO = objectMapper.readValue(jsonResponse, CoursePlanDTO.class);

            LearningPlan learningPlan = savePlanRecursively(planDTO);

            log.info("json {}" , jsonResponse);

            return  String.format("Generated extensive course '%s' (ID: %d)",
                    learningPlan.getTitle(), learningPlan.getId());
        } catch (Exception e) {
            log.error("Course generation failed", e);
            return "Failed to generate course: " + e.getMessage();
        }
    }

    private LearningPlan savePlanRecursively(CoursePlanDTO dto) {
        LearningPlan plan = LearningPlan.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .generatedFromPrompt("Auto-generated by Gemini")
                .build();
        plan = planRepo.save(plan);

        if (dto.getModules() != null) {
            int modOrder = 1;
            for (CoursePlanDTO.ModuleDTO modDto : dto.getModules()) {
                PlanModule module = PlanModule.builder()
                        .learningPlan(plan)
                        .title(modDto.getTitle())
                        .focusTopic(modDto.getFocusTopic())
                        .orderIndex(modOrder++)
                        .build();
                module = moduleRepo.save(module);

                if (modDto.getTasks() != null) {
                    int taskOrder = 1;
                    for (CoursePlanDTO.TaskDTO taskDto : modDto.getTasks()) {
                        PlanTask task = PlanTask.builder()
                                .module(module)
                                .title(taskDto.getTitle())
                                .description(taskDto.getDescription())
                                .resourceLink(taskDto.getResourceLink())
                                .estimatedMinutes(taskDto.getEstimatedMinutes())
                                .status(TaskStatus.TODO)
                                .orderIndex(taskOrder++)
                                .build();

                        if (taskDto.getSubPlan() != null) {
                            LearningPlan subPlan = savePlanRecursively(taskDto.getSubPlan());
                            task.setSubPlan(subPlan);
                        }
                        taskRepo.save(task);
                    }
                }
            }
        }
        return plan;
    }

    private String cleanJson(String text) {
        if (text.startsWith("```json")) return text.replace("```json", "").replace("```", "").trim();
        if (text.startsWith("```")) return text.replace("```", "").trim();
        return text;
    }
}