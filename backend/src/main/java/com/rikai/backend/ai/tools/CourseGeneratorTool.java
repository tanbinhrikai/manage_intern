package com.rikai.backend.ai.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rikai.backend.dto.record.SingleTaskUpdateDTO;
import com.rikai.backend.dto.response.ai.CoursePlanDTO;
import com.rikai.backend.model.*;
import com.rikai.backend.model.Enum.TaskStatus;
import com.rikai.backend.repository.*;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.google.genai.common.GoogleGenAiThinkingLevel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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
          "title": "Comprehensive Fullstack Java Bootcamp (Fresher Level - 6 Months)",
          "description": "A rigorous, step-by-step roadmap tailored for fresh graduates. Starting from strong Computer Science fundamentals and Java Core, moving through Database mastery, diving deep into the Spring Boot ecosystem, and finishing with modern Frontend (ReactJS) and CI/CD practices. The goal is to produce a developer capable of building end-to-end applications independently.",
          "modules": [
            {
              "title": "Phase 1: Solidifying Foundations (Month 1)",
              "focus_topic": "Java Core & Algorithms",
              "tasks": [
                {
                  "title": "OOP Mastery & Design Principles",
                  "description": "Don't just learn syntax. Deeply understand the 4 pillars of OOP (Encapsulation, Inheritance, Polymorphism, Abstraction). Learn standard coding conventions (Naming, JavaDocs). Study SOLID principles and apply them to refactor bad code.",
                  "resource_link": "https://www.baeldung.com/java-oop",
                  "estimated_minutes": 360
                },
                {
                  "title": "Java Collections Framework & Memory",
                  "description": "Master the Hierarchy (Collection, List, Set, Queue, Map). Understand internal implementations (How HashMap works, ArrayList vs LinkedList). Learn about Stack vs Heap memory and basic Garbage Collection concepts.",
                  "resource_link": "https://docs.oracle.com/javase/tutorial/collections/",
                  "estimated_minutes": 480
                },
                {
                  "title": "Modern Java Features (Java 8 to 21)",
                  "description": "This is crucial for modern development. Master Lambda Expressions, Functional Interfaces, and the Stream API (filtering, mapping, reducing). Learn Optional to handle nulls safely. Introduction to Java 14 Records and Pattern Matching.",
                  "resource_link": "https://dev.java/learn/modern-java/",
                  "estimated_minutes": 420
                }
              ]
            },
            {
              "title": "Phase 2: Database Engineering (Month 2)",
              "focus_topic": "SQL & ORM",
              "tasks": [
                {
                  "title": "Relational Database Design",
                  "description": "Learn to draw ERDs (Entity Relationship Diagrams). Understand Normalization (1NF, 2NF, 3NF) to avoid data redundancy. Practice complex SQL queries using JOINs (INNER, LEFT, RIGHT), GROUP BY, and HAVING clauses.",
                  "resource_link": "https://www.w3schools.com/sql/",
                  "estimated_minutes": 480
                },
                {
                  "title": "JPA & Hibernate Mastery",
                  "description": "This is a complex topic bridging Java and SQL. It requires a sub-plan to understand the abstraction layer fully.",
                  "estimated_minutes": 900,
                  "sub_plan": {
                    "title": "Hibernate/JPA Deep Dive",
                    "description": "Understanding how Java objects map to Database tables.",
                    "modules": [
                      {
                        "title": "Entity Mapping",
                        "focus_topic": "Annotations",
                        "tasks": [
                          {
                            "title": "Basic & Relationship Mapping",
                            "description": "Master @Entity, @Table, @Id. Implement @OneToMany, @ManyToOne, and @ManyToMany (with a join entity). Understand Cascade types and Fetch types (LAZY vs EAGER).",
                            "estimated_minutes": 300
                          },
                          {
                            "title": "JPQL & Criteria API",
                            "description": "Write database-agnostic queries using JPQL. Use Criteria API for dynamic query construction.",
                            "estimated_minutes": 240
                          }
                        ]
                      },
                      {
                        "title": "Performance Optimization",
                        "focus_topic": "N+1 Problem",
                        "tasks": [
                          {
                            "title": "Solving N+1 Query Problem",
                            "description": "Understand why N+1 happens and how to fix it using `JOIN FETCH` or Entity Graphs.",
                            "estimated_minutes": 180
                          }
                        ]
                      }
                    ]
                  }
                }
              ]
            },
            {
              "title": "Phase 3: Backend Development with Spring Boot (Month 3-4)",
              "focus_topic": "Spring Framework",
              "tasks": [
                {
                  "title": "Spring Core & Dependency Injection",
                  "description": "Understand the IoC Container and ApplicationContext. Master Bean Scopes (Singleton, Prototype) and Lifecycle (`@PostConstruct`, `@PreDestroy`). Practice Constructor Injection.",
                  "resource_link": "https://docs.spring.io/spring-framework/reference/core/beans.html",
                  "estimated_minutes": 360
                },
                {
                  "title": "Building Robust REST APIs",
                  "description": "Design clean URLs and use proper HTTP Methods. Implement DTO pattern using MapStruct. Add global exception handling using `@RestControllerAdvice`. Validate inputs with Hibernate Validator (`@NotNull`, `@Email`).",
                  "resource_link": "https://spring.io/guides/tutorials/rest/",
                  "estimated_minutes": 600
                },
                {
                  "title": "Spring Security Fundamentals",
                  "description": "Secure APIs using Spring Security 6. Implement Stateless Authentication with JWT. Understand the Security Filter Chain, UserDetailsService, and Password Encoding (BCrypt).",
                  "resource_link": "https://spring.io/projects/spring-security",
                  "estimated_minutes": 720
                }
              ]
            },
            {
              "title": "Phase 4: Frontend & Integration (Month 5)",
              "focus_topic": "ReactJS & API Integration",
              "tasks": [
                {
                  "title": "Modern JavaScript (ES6+) & TypeScript",
                  "description": "Master Arrow Functions, Destructuring, Promises, and Async/Await. Learn TypeScript basics: Interfaces, Types, and Generics for type safety.",
                  "resource_link": "https://javascript.info/",
                  "estimated_minutes": 300
                },
                {
                  "title": "ReactJS Essentials",
                  "description": "Understand Component-Based Architecture. Master Hooks (`useState`, `useEffect`, `useContext`). Handle forms and client-side validation.",
                  "resource_link": "https://react.dev/learn",
                  "estimated_minutes": 480
                },
                {
                  "title": "Integrating Backend & Frontend",
                  "description": "Use `Axios` or `Fetch API` to call Spring Boot APIs. Handle CORS issues. Implement JWT storage (LocalStorage vs HttpOnly Cookies) and Authentication Interceptors.",
                  "estimated_minutes": 360
                }
              ]
            },
            {
              "title": "Phase 5: Capstone Project & DevOps (Month 6)",
              "focus_topic": "Real-world Application",
              "tasks": [
                {
                  "title": "Fullstack E-commerce Project",
                  "description": "Build a complete shop app. Features: Product Listing, Cart, Checkout, User/Admin Roles. Must include Unit Tests (JUnit/Mockito) for backend logic.",
                  "estimated_minutes": 1200
                },
                {
                  "title": "Deployment & CI/CD",
                  "description": "Containerize the app using Docker (Dockerfile, docker-compose). Set up a basic CI pipeline using GitHub Actions to build and test code automatically on push.",
                  "resource_link": "https://docs.docker.com/get-started/",
                  "estimated_minutes": 300
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
        Level : Intern Fresher

        CRITICAL INSTRUCTIONS (MUST FOLLOW):
        1.  **MAXIMUM DETAIL**: Do not be brief. Each task description must be a paragraph explaining *what*, *why*, and *how*.
        2.  **RECURSIVE DEPTH**: For every major concept (e.g., "Security", "Database", "Cloud"), you MUST create a `sub_plan` instead of a simple task. I want a deep nested structure.
        3.  **VOLUME**: The output should contain at least 4-6 major Modules, and each Module must have 4-6 Tasks.
        4.  **STRICT JSON**: Output ONLY valid JSON.
        
        USE THIS JSON STRUCTURE (See how detailed the description and sub_plans are):
        {json_schema}
        """;

    private static final String MODIFICATION_PROMPT = """
        You are a Technical Curriculum Developer.
        
        GOAL: Modify the existing learning plan based STRICTLY on the user's instruction.
        
        CURRENT PLAN (JSON):
        {current_plan_json}
        
        USER INSTRUCTION: "{instruction}"
        
        INSTRUCTIONS:
        1. Analyze the Current Plan and the Instruction.
        2. Generate a NEW fully structured JSON that incorporates the changes.
        3. Keep the parts of the plan that shouldn't change.
        4. If the instruction is to "Delete" something, remove it from the JSON.
        5. If the instruction is to "Add" something, insert it logically.
        6. OUTPUT MUST BE STRICT JSON following the same schema.
        """;

    private static final String TASK_REFINEMENT_PROMPT = """
        You are an expert Technical Mentor.
        
        OBJECTIVE: Refine a SPECIFIC TASK in a learning plan based on user instructions.
        
        CURRENT TASK DATA:
        {current_task_json}
        
        USER INSTRUCTION: "{instruction}"
        
        REQUIREMENTS:
        1. If user asks for a better source, provide a high-quality, free, up-to-date URL.
        2. If user asks for more details/embedded course, rewrite the 'description' field to be very detailed (use Markdown list inside the string if needed), acting like a mini-syllabus.
        3. OUTPUT MUST BE STRICT JSON containing ONLY: title, description, resource_link, estimated_minutes.
        """;

    public CourseGeneratorTool(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper, LearningPlanRepository planRepo, PlanModuleRepository moduleRepo, PlanTaskRepository taskRepo) {
        this.chatClient = chatClientBuilder.build();
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

    @Tool(description = "Delete an existing course/learning plan by its ID.")
    @Transactional
    public String deleteCourse(@ToolParam(description = "The ID of the course/plan to delete") Long planId) {
        log.info("Request to delete course ID: {}", planId);
        if (!planRepo.existsById(planId)) {
            return "Course with ID " + planId + " not found.";
        }
        try {
            planRepo.deleteById(planId);
            return "Successfully deleted course with ID: " + planId;
        } catch (Exception e) {
            return "Failed to delete: " + e.getMessage();
        }
    }

    @Tool(description = "Update the structure of an existing course (Add/Remove Modules, Change Title). Use this for high-level changes.")
    @Transactional
    public String updateCourseStructure(
            @ToolParam(description = "The ID of the course to update") Long planId,
            @ToolParam(description = "Instruction (e.g., 'Add a module about Docker at the end', 'Remove the Database phase')") String instruction
    ) {
        log.info("Request to update course structure ID: {}", planId);
        LearningPlan existingPlan = planRepo.findById(planId).orElse(null);
        if (existingPlan == null) return "Course ID " + planId + " not found.";

        try {
            CoursePlanDTO currentDto = mapEntityToDTO(existingPlan);
            String currentJson = objectMapper.writeValueAsString(currentDto);

            PromptTemplate template = new PromptTemplate(MODIFICATION_PROMPT);
            String promptContent = template.render(Map.of(
                    "current_plan_json", currentJson,
                    "instruction", instruction
            ));

            String jsonResponse = callAi(promptContent, 0.4);
            CoursePlanDTO updatedDTO = objectMapper.readValue(jsonResponse, CoursePlanDTO.class);

            existingPlan.setTitle(updatedDTO.getTitle());
            existingPlan.setDescription(updatedDTO.getDescription());
            existingPlan.setGeneratedFromPrompt("Modified: " + instruction);

            moduleRepo.deleteAll(existingPlan.getModules());
            existingPlan.getModules().clear();
            planRepo.saveAndFlush(existingPlan);

            saveModulesForPlan(existingPlan, updatedDTO.getModules());

            return "Updated course structure successfully.";
        } catch (Exception e) {
            log.error("Update failed", e);
            return "Update failed: " + e.getMessage();
        }
    }

    @Tool(description = "Update a SPECIFIC task's details. Use this when user wants to change description, find better link, or expand details/add sub-course into ONE task without changing the whole plan.")
    @Transactional
    public String updateTaskSpecifics(
            @ToolParam(description = "The ID of the task to update") Long taskId,
            @ToolParam(description = "Instruction (e.g., 'Find a new link', 'Make description detailed like a mini-course')") String instruction
    ) {
        log.info("Request to update Task ID: {} - Instruction: {}", taskId, instruction);
        PlanTask task = taskRepo.findById(taskId).orElse(null);
        if (task == null) return "Task ID " + taskId + " not found.";

        try {
            SingleTaskUpdateDTO currentTaskDto = new SingleTaskUpdateDTO(
                    task.getTitle(), task.getDescription(), task.getResourceLink(), task.getEstimatedMinutes()
            );
            String taskJson = objectMapper.writeValueAsString(currentTaskDto);

            PromptTemplate template = new PromptTemplate(TASK_REFINEMENT_PROMPT);
            String promptContent = template.render(Map.of(
                    "current_task_json", taskJson,
                    "instruction", instruction
            ));

            String jsonResponse = callAi(promptContent, 0.3);
            SingleTaskUpdateDTO updatedDto = objectMapper.readValue(jsonResponse, SingleTaskUpdateDTO.class);

            task.setTitle(updatedDto.title());
            task.setDescription(updatedDto.description());
            task.setResourceLink(updatedDto.resource_link());
            task.setEstimatedMinutes(updatedDto.estimated_minutes());

            taskRepo.save(task);

            return "Updated task details successfully. New title: " + updatedDto.title();
        } catch (Exception e) {
            log.error("Task update failed", e);
            return "Task update failed: " + e.getMessage();
        }
    }


    private String callAi(String prompt, double temp) {
        var options = GoogleGenAiChatOptions.builder()
                .model(TYPE_MODEL_GEMINI)
                .temperature(temp)
                .build();
        String response = chatClient.prompt().user(prompt).options(options).call().content();
        assert response != null;
        return cleanJson(response);
    }

    private LearningPlan savePlanRecursively(CoursePlanDTO dto) {
        LearningPlan plan = LearningPlan.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .generatedFromPrompt("Auto-generated")
                .build();
        plan = planRepo.save(plan);
        saveModulesForPlan(plan, dto.getModules());
        return plan;
    }

    private void saveModulesForPlan(LearningPlan plan, List<CoursePlanDTO.ModuleDTO> moduleDTOs) {
        if (moduleDTOs == null) return;
        int modOrder = 1;
        for (CoursePlanDTO.ModuleDTO modDto : moduleDTOs) {
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

    private CoursePlanDTO mapEntityToDTO(LearningPlan plan) {
        CoursePlanDTO dto = new CoursePlanDTO();
        dto.setTitle(plan.getTitle());
        dto.setDescription(plan.getDescription());

        List<CoursePlanDTO.ModuleDTO> modDTOs = new ArrayList<>();
        if (plan.getModules() != null) {
            for (PlanModule mod : plan.getModules()) {
                CoursePlanDTO.ModuleDTO modDto = new CoursePlanDTO.ModuleDTO();
                modDto.setTitle(mod.getTitle());
                modDto.setFocusTopic(mod.getFocusTopic());

                List<CoursePlanDTO.TaskDTO> taskDTOs = getTaskDTOS(mod);
                modDto.setTasks(taskDTOs);
                modDTOs.add(modDto);
            }
        }
        dto.setModules(modDTOs);
        return dto;
    }

    private static @NonNull List<CoursePlanDTO.TaskDTO> getTaskDTOS(PlanModule mod) {
        List<CoursePlanDTO.TaskDTO> taskDTOs = new ArrayList<>();
        if (mod.getTasks() != null) {
            for (PlanTask task : mod.getTasks()) {
                CoursePlanDTO.TaskDTO taskDto = new CoursePlanDTO.TaskDTO();
                taskDto.setTitle(task.getTitle());
                taskDto.setDescription(task.getDescription());
                taskDto.setResourceLink(task.getResourceLink());
                taskDto.setEstimatedMinutes(task.getEstimatedMinutes());
                taskDTOs.add(taskDto);
            }
        }
        return taskDTOs;
    }

    private String cleanJson(String text) {
        if (text.startsWith("```json")) return text.replace("```json", "").replace("```", "").trim();
        if (text.startsWith("```")) return text.replace("```", "").trim();
        return text;
    }
}