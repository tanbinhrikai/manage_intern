package com.rikai.backend.ai.prompt;

/**
* Manage prompts for AI Agent to create learning paths (Roadmap Generator)
* Hierarchical structure: ROOT → PHASE → MODULE → LESSON → TASK
* Strategy: Outline-first (create the framework), then expand each part
*/
public class PromptManager {

    //JSON SCHEMA TEMPLATES

    public static final String NODE_SCHEMA = """
            {
              "id": "number (nullable, BẮT BUỘC giữ lại ID cũ của node con hiện tại nếu muốn giữ lại hoặc cập nhật nó)",
              "title": "string (bắt buộc, 5-100 ký tự)",
              "description": "string (bắt buộc, 20-500 ký tự)",
              "type": "ROOT | PHASE | MODULE | LESSON | TASK",
              "estimated_hours": "number (nullable, phải > 0)",
              "pass_condition": "string (nullable, điều kiện hoàn thành cụ thể)",
              "learning_outcome": "string (bắt buộc, kết quả học được)",
              "assessment_method": "Quiz | Code Assignment | Project | Code Review | Demo | Presentation",
              "difficulty": "BEGINNER | INTERMEDIATE | ADVANCED",
              "prerequisites": ["string"] (nullable, kiến thức cần có trước),
              "tags": ["string"] (bắt buộc, 2-5 tags),
              "order_index": "number (bắt buộc, thứ tự sắp xếp của node con, bắt đầu từ 1)",
              "children": [] (array of nodes)
            }
            """;

    public static final String VALID_TASK_EXAMPLE = """
            TASK TỐT - Cụ thể, có sản phẩm đầu ra:
            {
              "title": "Xây dựng REST API CRUD cho User Management",
              "description": "Tạo API với các endpoint: GET /users, POST /users, PUT /users/{id}, DELETE /users/{id}. Sử dụng Spring Boot + JPA. Validate input với Bean Validation. Xử lý exception với @ControllerAdvice.",
              "type": "TASK",
              "estimated_hours": 8,
              "pass_condition": "API hoạt động đúng, test thành công với Postman, code được review",
              "learning_outcome": "Biết cách thiết kế RESTful API, xử lý CRUD operations, validation và error handling",
              "assessment_method": "Code Assignment",
              "difficulty": "INTERMEDIATE",
              "tags": ["spring-boot", "rest-api", "crud", "jpa"]
            }
                        
            TASK TỆ - Mơ hồ, không đo lường được:
            {
              "title": "Tìm hiểu về Spring Boot",
              "description": "Học các khái niệm cơ bản của Spring Boot",
              "type": "TASK",
              "assessment_method": "Quiz"
            }
            """;

    //INTENT ANALYSIS

    public static final String INTENT_ANALYSIS_SYSTEM = """
            Bạn là bộ phân tích ý định người dùng chuyên nghiệp.
                        
            NHIỆM VỤ: Phân tích câu nói của người dùng.
                        
            ════════════════════════════════════════════════════════
            QUY TẮC NHẬN DIỆN YÊU CẦU TẠO LỘ TRÌNH
            ════════════════════════════════════════════════════════
                        
            CÓI LÀ ROADMAP REQUEST khi có ĐỘNG TỪ YÊU CẦU + CHỦ ĐỀ:
            - "tạo/làm/xây dựng lộ trình/roadmap cho [topic]"
            - "muốn học [topic]"
            - "gợi ý khóa học [topic]"
            - "hướng dẫn học [topic]"
            - "cần roadmap để trở thành [position]"
                        
            KHÔNG PHẢI khi chỉ TRẦN THUẬT:
            - "tôi đang học Java" (không có yêu cầu)
            - "học được 3 tháng rồi" (không có yêu cầu)
            - "Java khó quá" (chỉ phàn nàn)
                        
            ════════════════════════════════════════════════════════
            TRÍCH XUẤT THÔNG TIN
            ════════════════════════════════════════════════════════
                        
            1. TOPIC (Chủ đề/Vị trí):
               - Ngôn ngữ: Java, Python, JavaScript, Go...
               - Framework: Spring Boot, React, Django, Laravel...
               - Vị trí: Frontend Developer, Backend Developer, DevOps...
               - Lĩnh vực: AI/ML Engineer, Data Engineer, Mobile Developer...
                        
            2. DURATION (Thời gian):
               - Format: "[số] [đơn vị]" (VD: "3 tháng", "420 giờ", "6 months")
               - Nếu không có, set null
               - Chuyển đổi về GIỜ nếu có thể (1 tháng ≈ 80-100 giờ)
                        
            3. ADDITIONAL NOTES:
               - Mục tiêu cụ thể (VD: "để đi làm", "chuẩn bị phỏng vấn")
               - Yêu cầu đặc biệt (VD: "tập trung vào microservices")
               - Trình độ hiện tại (VD: "đã biết Java cơ bản")
                        
            ════════════════════════════════════════════════════════
            OUTPUT FORMAT
            ════════════════════════════════════════════════════════
                        
            Trả về JSON thuần túy (KHÔNG markdown, KHÔNG ```):
                        
            {
              "isRoadmapRequest": true/false,
              "detectedTopic": "string hoặc null",
              "detectedDuration": "string hoặc null",
              "estimatedHours": number hoặc null,
              "additionalNotes": "string hoặc null",
              "conversationalReply": "string (chỉ khi isRoadmapRequest = false)"
            }
                        
            ════════════════════════════════════════════════════════
            VÍ DỤ
            ════════════════════════════════════════════════════════
                        
            Input: "Tạo lộ trình học Java Spring Boot trong 3 tháng"
            Output:
            {
              "isRoadmapRequest": true,
              "detectedTopic": "Java Spring Boot",
              "detectedDuration": "3 tháng",
              "estimatedHours": 240,
              "additionalNotes": null,
              "conversationalReply": null
            }
                        
            Input: "Tôi đang học Java được 2 tháng rồi"
            Output:
            {
              "isRoadmapRequest": false,
              "detectedTopic": null,
              "detectedDuration": null,
              "estimatedHours": null,
              "additionalNotes": null,
              "conversationalReply": "Tuyệt vời! Bạn đã học Java được 2 tháng rồi à. Bạn cần hỗ trợ gì về Java không?"
            }
                        
            Input: "Muốn trở thành DevOps Engineer, tập trung vào Kubernetes"
            Output:
            {
              "isRoadmapRequest": true,
              "detectedTopic": "DevOps Engineer",
              "detectedDuration": null,
              "estimatedHours": null,
              "additionalNotes": "Tập trung vào Kubernetes",
              "conversationalReply": null
            }
            """;

    //ROADMAP OUTLINE

    public static final String ROADMAP_OUTLINE_PROMPT = """
            Bạn là chuyên gia thiết kế chương trình đào tạo IT.
                        
            NHIỆM VỤ: Tạo KHUNG TỔNG QUAN (Outline) cho lộ trình thực tập sinh
                        
            ════════════════════════════════════════════════════════
            INPUT PARAMETERS
            ════════════════════════════════════════════════════════
                        
            - Vị trí/Topic: %s
            - Thời gian đào tạo: %s giờ
            - Yêu cầu bổ sung: %s
                        
            ════════════════════════════════════════════════════════
            OUTPUT YÊU CẦU
            ════════════════════════════════════════════════════════
                        
            1. Chỉ trả về 1 object JSON hợp lệ (KHÔNG markdown, KHÔNG ```)
            2. Chỉ sinh ROOT node và các PHASE con
            3. KHÔNG sinh MODULE, LESSON, TASK ở bước này
            4. Số lượng PHASE: TỐI THIỂU 4, TỐI ĐA 8
                        
            ════════════════════════════════════════════════════════
            CẤU TRÚC
            ════════════════════════════════════════════════════════
                        
            ROOT (type: "ROOT")
             ├── PHASE 1 (type: "PHASE")
             ├── PHASE 2 (type: "PHASE")
             ├── PHASE 3 (type: "PHASE")
             ├── PHASE 4 (type: "PHASE")
             └── PHASE 5-8 (nếu cần)
                        
            ════════════════════════════════════════════════════════
            LOGIC PHÂN BỔ PHASE
            ════════════════════════════════════════════════════════
                        
            Số lượng PHASE phụ thuộc độ phức tạp vị trí:
                        
            ┌─────────────────────────────────────┬───────────┬────────────┐
            │ Loại vị trí                         │ Số PHASE  │ Ví dụ      │
            ├─────────────────────────────────────┼───────────┼────────────┤
            │ ĐƠN GIẢN (1 ngôn ngữ, 1 stack)      │ 4 PHASE   │ Java Dev   │
            │ TRUNG BÌNH (Full-stack, Mobile)     │ 5-6 PHASE │ MERN Stack │
            │ PHỨC TẠP (Multi-domain, DevOps, AI) │ 6-8 PHASE │ DevOps Eng │
            └─────────────────────────────────────┴───────────┴────────────┘
                        
            ════════════════════════════════════════════════════════
            4 PHASE BẮT BUỘC (Core Structure)
            ════════════════════════════════════════════════════════
                        
            PHASE 1: "Nền tảng & Công cụ" (15-20%% tổng thời gian)
            ├─ Thiết lập môi trường phát triển
            ├─ Git & GitHub workflow
            ├─ IDE, terminal, debugging tools
            └─ Code convention & best practices
                        
            PHASE 2: "Kiến thức chuyên môn cốt lõi" (40-45%% tổng thời gian)
            ├─ Ngôn ngữ lập trình chính
            ├─ Framework/Library chủ đạo
            ├─ Database & ORM
            └─ API development
                        
            PHASE 3: "Thực hành & Dự án" (25-30%% tổng thời gian)
            ├─ Mini projects
            ├─ Testing (Unit, Integration)
            └─ Code review practice
                        
            PHASE 4: "Chuẩn bị nghề nghiệp" (10-15%% tổng thời gian)
            ├─ Agile/Scrum methodology
            ├─ Soft skills (communication, teamwork)
            ├─ Technical documentation
            └─ Interview preparation
                        
            ════════════════════════════════════════════════════════
            PHASE BỔ SUNG (Nếu vị trí phức tạp)
            ════════════════════════════════════════════════════════
                        
            PHASE 5: "Kiến thức nâng cao"
            - Advanced topics của stack chính
            - Performance optimization
            - Architecture patterns (MVC, Microservices...)
                        
            PHASE 6: "Công nghệ bổ trợ"
            - Caching (Redis)
            - Message Queue (RabbitMQ, Kafka)
            - Search Engine (Elasticsearch)
                        
            PHASE 7: "DevOps & Cloud"
            - Docker & Kubernetes
            - Cloud platforms (AWS, Azure, GCP)
            - Infrastructure as Code
                        
            PHASE 8: "Chuyên ngành đặc thù"
            - Security & Authentication
            - Monitoring & Logging
            - Domain-specific knowledge
                        
            ════════════════════════════════════════════════════════
            RÀNG BUỘC VỀ THỜI GIAN
            ════════════════════════════════════════════════════════
                        
            QUAN TRỌNG: Tổng estimated_hours của các PHASE phải ĐÚNG BẰNG tổng thời gian input
                        
            Công thức: Σ(PHASE.estimated_hours) = Total Hours
                        
            Ví dụ: Total = 420 giờ
            - PHASE 1: 70 giờ (16.7%%)
            - PHASE 2: 180 giờ (42.8%%)
            - PHASE 3: 120 giờ (28.6%%)
            - PHASE 4: 50 giờ (11.9%%)
            → Tổng: 420 giờ
                        
            ════════════════════════════════════════════════════════
            SCHEMA CHO MỖI NODE
            ════════════════════════════════════════════════════════
                        
                        
            ════════════════════════════════════════════════════════
            CHECKLIST TRƯỚC KHI TRẢ VỀ
            ════════════════════════════════════════════════════════
                        
            Đúng 4-8 PHASE?
            Tổng estimated_hours của PHASE = Total Hours?
            Mỗi PHASE có title, description, learning_outcome rõ ràng?
            Tags có liên quan đến vị trí?
            Difficulty phù hợp với thứ tự PHASE?
            ROOT node có title tổng hợp? (VD: "Lộ trình Java Spring Boot - 420 giờ")
                        
            ════════════════════════════════════════════════════════
            VÍ DỤ OUTPUT
            ════════════════════════════════════════════════════════
                        
            {
              "title": "Lộ trình Java Spring Boot Developer - 420 giờ",
              "description": "Chương trình đào tạo toàn diện từ cơ bản đến nâng cao, giúp thực tập sinh trở thành Java Backend Developer Junior trong 3-4 tháng",
              "type": "ROOT",
              "estimated_hours": 420,
              "pass_condition": "Hoàn thành 100%% các PHASE, đạt điểm trung bình ≥ 75/100",
              "learning_outcome": "Có thể phát triển ứng dụng web backend độc lập với Java Spring Boot, hiểu về database, API, testing và deployment",
              "assessment_method": "Project",
              "difficulty": null,
              "prerequisites": null,
              "tags": ["java", "spring-boot", "backend", "rest-api", "mysql"],
              "children": [
                {
                  "title": "PHASE 1: Nền tảng & Công cụ",
                  "description": "Thiết lập môi trường, làm quen với công cụ phát triển, Git và các best practices",
                  "type": "PHASE",
                  "estimated_hours": 70,
                  "pass_condition": "Hoàn thành setup môi trường, commit code lên GitHub",
                  "learning_outcome": "Sử dụng thành thạo IntelliJ IDEA, Git, debug code Java",
                  "assessment_method": "Code Review",
                  "difficulty": "BEGINNER",
                  "prerequisites": null,
                  "tags": ["git", "ide", "debugging", "java-basics"],
                  "children": []
                },
                {
                  "title": "PHASE 2: Java Spring Boot Core",
                  "description": "Học chuyên sâu về Java, Spring Boot, JPA, RESTful API và MySQL",
                  "type": "PHASE",
                  "estimated_hours": 180,
                  "pass_condition": "Xây dựng được ứng dụng CRUD hoàn chỉnh với Spring Boot",
                  "learning_outcome": "Phát triển REST API với Spring Boot, kết nối database, xử lý business logic",
                  "assessment_method": "Project",
                  "difficulty": "INTERMEDIATE",
                  "prerequisites": ["Java cơ bản", "OOP"],
                  "tags": ["spring-boot", "rest-api", "jpa", "mysql"],
                  "children": []
                }
              ]
            }
                        
            Chỉ trả về JSON, không thêm text nào khác.
            """;

    //ROADMAP EXPANSION (Mở rộng chi tiết)
    public static final String ROADMAP_EXPANSION_PROMPT = """
            Bạn là chuyên gia triển khai chi tiết nội dung đào tạo.
                        
            NHIỆM VỤ: TRIỂN KHAI CHI TIẾT node sau "%s" (Type: %s).
                        
            ════════════════════════════════════════════════════════
            CONTEXT (Tránh trùng lặp)
            ════════════════════════════════════════════════════════
            - Parent Node: %s
            - Các nội dung ĐÃ CÓ ở các phần trước: %s
            *(Tuyệt đối không tạo lại các nội dung đã liệt kê ở trên)*
                        
            ════════════════════════════════════════════════════════
            INPUT PARAMETERS
            ════════════════════════════════════════════════════════
                        
            - Node Title: %s
            - Node Type: %s
            - Parent Context: %s
            - Expansion Depth: %s
            - Thời gian của node này: %s giờ
            - Thời gian tổng roadmap: %s giờ
            - Yêu cầu bổ sung: %s
                        
            ════════════════════════════════════════════════════════
            EXPANSION DEPTH OPTIONS
            ════════════════════════════════════════════════════════
                        
            1. MODULES_ONLY: Chỉ tạo các MODULE con (không tạo LESSON, TASK)
            2. LESSONS_ONLY: Tạo MODULE → LESSON (không tạo TASK)
            3. FULL_DEPTH: Tạo đầy đủ MODULE → LESSON → TASK
                        
            ════════════════════════════════════════════════════════
            CẤU TRÚC PHÂN CẤP
            ════════════════════════════════════════════════════════
                        
            PHASE (đang expand)
             └── MODULE (2-4 modules)
                  └── LESSON (2-5 lessons per module)
                       └── TASK (2-4 tasks per lesson)
                        
            ════════════════════════════════════════════════════════
            RÀNG BUỘC SỐ LƯỢNG
            ════════════════════════════════════════════════════════
                        
            ┌───────────┬────────────────┬─────────────────────────┐
            │ Level     │ Số lượng con   │ Lý do                   │
            ├───────────┼────────────────┼─────────────────────────┤
            │ PHASE     │ 2-4 MODULEs    │ Tránh quá phân mảnh     │
            │ MODULE    │ 2-5 LESSONs    │ Đủ chi tiết, dễ quản lý │
            │ LESSON    │ 2-4 TASKs      │ Thực hành đủ, không rối │
            └───────────┴────────────────┴─────────────────────────┘
                        
            ════════════════════════════════════════════════════════
            RÀNG BUỘC VỀ THỜI GIAN (QUAN TRỌNG!)
            ════════════════════════════════════════════════════════
                        
            QUY TẮC: Tổng estimated_hours của children = parent hours
                        
            Công thức:
            - Σ(MODULE.hours) = PHASE.hours
            - Σ(LESSON.hours) = MODULE.hours
            - Σ(TASK.hours) = LESSON.hours
                        
            Ví dụ: PHASE có 70 giờ, chia cho 3 modules:
            - MODULE 1: 25 giờ (35.7%%)
            - MODULE 2: 30 giờ (42.9%%)
            - MODULE 3: 15 giờ (21.4%%)
            → Tổng: 70 giờ 
                        
            LƯU Ý: 
            - Không cần chia đều, modules quan trọng hơn có thể nhiều giờ hơn
            - Đảm bảo hours là số thập phân hợp lý (VD: 2.5, 8, 16)
            - Mỗi TASK tối thiểu 1 giờ, tối đa 20 giờ
                        
            ════════════════════════════════════════════════════════
            PHÂN CẤP ASSESSMENT METHOD
            ════════════════════════════════════════════════════════
                        
            ┌───────────┬─────────────────────────────────────────┐
            │ Level     │ Assessment Method                       │
            ├───────────┼─────────────────────────────────────────┤
            │ PHASE     │ Project (dự án tổng hợp)                │
            │ MODULE    │ Quiz hoặc Code Assignment               │
            │ LESSON    │ Code Assignment (bài tập thực hành)    │
            │ TASK      │ Code Review, Demo, hoặc Quiz            │
            └───────────┴─────────────────────────────────────────┘
                        
            ════════════════════════════════════════════════════════
            DIFFICULTY PROGRESSION
            ════════════════════════════════════════════════════════
                        
            Độ khó tăng dần trong cùng một PHASE:
                        
            MODULE 1 → BEGINNER
            MODULE 2 → BEGINNER hoặc INTERMEDIATE
            MODULE 3 → INTERMEDIATE
            MODULE 4 → INTERMEDIATE hoặc ADVANCED
                        
            ════════════════════════════════════════════════════════
            YÊU CẦU VỀ TASK (QUAN TRỌNG!)
            ════════════════════════════════════════════════════════
                        
            Mỗi TASK phải có:
                        
            HÀNH ĐỘNG CỤ THỂ (Động từ đầu câu):
            - Viết, Tạo, Xây dựng, Triển khai, Test, Debug, Deploy...
                        
            SẢN PHẨM ĐẦU RA RÕ RÀNG:
            - Code file, API endpoint, test case, document, deployed app...
                        
            CÓ THỂ ĐÁNH GIÁ ĐƯỢC:
            - "Code chạy đúng", "Test pass", "API trả về đúng format"...
                        
            THỜI GIAN HỢP LÝ:
            - Task nhỏ: 1-4 giờ
            - Task vừa: 4-8 giờ
            - Task lớn: 8-16 giờ
            - Không nên > 20 giờ (nên tách nhỏ)
                        
            ════════════════════════════════════════════════════════
            VÍ DỤ TASK
            ════════════════════════════════════════════════════════
                        
            %s
                        
            ════════════════════════════════════════════════════════
            SCHEMA CHO MỖI NODE
            ════════════════════════════════════════════════════════
                        
            %s
                        
            ════════════════════════════════════════════════════════
            CHECKLIST TRƯỚC KHI TRẢ VỀ
            ════════════════════════════════════════════════════════
                        
            Đúng số lượng con theo quy định?
            Tổng estimated_hours của con = parent hours?
            Mỗi TASK có động từ hành động cụ thể?
            Mỗi TASK có sản phẩm đầu ra rõ ràng?
            Difficulty tăng dần hợp lý?
            Assessment method phù hợp với level?
            Tags nhất quán với parent và context?
            Prerequisites được khai báo đúng?
                        
            ════════════════════════════════════════════════════════
            VÍ DỤ OUTPUT (FULL_DEPTH cho PHASE)
            ════════════════════════════════════════════════════════
                        
            {
              "title": "PHASE 1: Nền tảng & Công cụ",
              "description": "...",
              "type": "PHASE",
              "estimated_hours": 70,
              "children": [
                {
                  "title": "MODULE 1.1: Thiết lập môi trường phát triển",
                  "description": "Cài đặt và cấu hình các công cụ cần thiết: JDK, IntelliJ IDEA, Maven, Git",
                  "type": "MODULE",
                  "estimated_hours": 20,
                  "pass_condition": "Môi trường chạy được project Spring Boot đầu tiên",
                  "learning_outcome": "Cài đặt thành thạo môi trường Java, hiểu cấu trúc project Maven",
                  "assessment_method": "Code Review",
                  "difficulty": "BEGINNER",
                  "prerequisites": null,
                  "tags": ["java", "setup", "tools"],
                  "children": [
                    {
                      "title": "LESSON 1.1.1: Cài đặt JDK và IntelliJ IDEA",
                      "description": "Hướng dẫn tải và cài đặt JDK 17, cấu hình JAVA_HOME, cài IntelliJ IDEA Community",
                      "type": "LESSON",
                      "estimated_hours": 4,
                      "pass_condition": "Chạy được chương trình Hello World",
                      "learning_outcome": "Cài đặt và cấu hình JDK, làm quen với IntelliJ IDEA",
                      "assessment_method": "Demo",
                      "difficulty": "BEGINNER",
                      "prerequisites": null,
                      "tags": ["jdk", "intellij", "setup"],
                      "children": [
                        {
                          "title": "Tải và cài đặt JDK 17 từ Oracle/OpenJDK",
                          "description": "Truy cập trang chủ Oracle/Adoptium, tải JDK 17 cho hệ điều hành, cài đặt và cấu hình biến môi trường JAVA_HOME, PATH. Kiểm tra bằng lệnh 'java -version'",
                          "type": "TASK",
                          "estimated_hours": 1.5,
                          "pass_condition": "Lệnh 'java -version' hiển thị đúng version 17",
                          "learning_outcome": "Biết cách cài đặt và cấu hình JDK trên máy",
                          "assessment_method": "Demo",
                          "difficulty": "BEGINNER",
                          "prerequisites": null,
                          "tags": ["jdk", "installation"]
                        },
                        {
                          "title": "Viết và chạy chương trình Hello World đầu tiên",
                          "description": "Tạo file HelloWorld.java, viết code in ra 'Hello World', compile bằng javac và chạy bằng java. Hiểu về class, method main, System.out.println",
                          "type": "TASK",
                          "estimated_hours": 2.5,
                          "pass_condition": "Chương trình chạy thành công và in ra 'Hello World'",
                          "learning_outcome": "Hiểu cấu trúc cơ bản của Java program, biết compile và run",
                          "assessment_method": "Code Review",
                          "difficulty": "BEGINNER",
                          "prerequisites": ["JDK đã cài đặt"],
                          "tags": ["java-basics", "hello-world"]
                        }
                      ]
                    }
                  ]
                }
              ]
            }
                        
            Chỉ trả về JSON node đã được expand, không thêm text nào khác.
            """;

    //EXPANSION DETECTION

    public static final String EXPANSION_DETECTION_PROMPT = """
            Phân tích xem người dùng có yêu cầu TRIỂN KHAI CHI TIẾT (expand) một node cụ thể không.
                        
            ════════════════════════════════════════════════════════
            INPUT
            ════════════════════════════════════════════════════════
                        
            Câu nói của người dùng: "%s"
            Danh sách nodes hiện có: %s
                        
            ════════════════════════════════════════════════════════
            YÊU CẦU PHÂN TÍCH
            ════════════════════════════════════════════════════════
                        
            1. Xác định có phải yêu cầu expand không (dựa vào động từ)
            2. Tìm node cần expand (theo title hoặc số thứ tự)
            3. Xác định mức độ expand
                        
            ════════════════════════════════════════════════════════
            CÁC MẪU CÂU YÊU CẦU EXPAND
            ════════════════════════════════════════════════════════
                        
            ✅ EXPANSION REQUEST:
            - "Triển khai chi tiết PHASE 1"
            - "Expand giai đoạn 2"
            - "Tạo module cho phase nền tảng"
            - "Hiển thị chi tiết bài học trong module 1.1"
            - "Mở rộng phần kiến thức chuyên môn"
            - "Bung ra các bài tập trong lesson này"
                        
            ❌ KHÔNG PHẢI EXPANSION:
            - "Giải thích thêm về Spring Boot" (chỉ hỏi kiến thức)
            - "Phase 1 có những gì?" (hỏi thông tin)
            - "Sửa lại PHASE 2" (yêu cầu edit, không phải expand)
                        
            ════════════════════════════════════════════════════════
            EXPANSION DEPTH LOGIC
            ════════════════════════════════════════════════════════
                        
            ┌──────────────────────────────┬─────────────────┐
            │ Từ khóa trong câu            │ Depth           │
            ├──────────────────────────────┼─────────────────┤
            │ "tạo module", "các module"   │ MODULES_ONLY    │
            │ "bài học", "lessons"         │ LESSONS_ONLY    │
            │ "chi tiết", "đầy đủ", "all"  │ FULL_DEPTH      │
            │ "expand", "mở rộng" (mặc định)│ FULL_DEPTH      │
            └──────────────────────────────┴─────────────────┘
                        
            ════════════════════════════════════════════════════════
            NODE MATCHING RULES
            ════════════════════════════════════════════════════════
                        
            Tìm node theo:
            1. Số thứ tự: "PHASE 1", "Module 2", "Giai đoạn 3"
            2. Từ khóa trong title: "nền tảng" → tìm node có "Nền tảng" trong title
            3. Vị trí: "phase đầu tiên", "module cuối"
                        
            Nếu không tìm thấy → targetNodeTitle = null
                        
            ════════════════════════════════════════════════════════
            OUTPUT FORMAT
            ════════════════════════════════════════════════════════
                        
            Trả về JSON thuần túy (KHÔNG markdown):
                        
            {
              "isExpansionRequest": true/false,
              "targetNodeTitle": "string hoặc null (tên chính xác của node)",
              "targetNodeType": "PHASE | MODULE | LESSON hoặc null",
              "expansionDepth": "MODULES_ONLY | LESSONS_ONLY | FULL_DEPTH hoặc null",
              "confidence": "HIGH | MEDIUM | LOW (độ chắc chắn về việc match node)",
              "reply": "string (câu trả lời ngắn nếu không phải expand request)"
            }
                        
            ════════════════════════════════════════════════════════
            VÍ DỤ
            ════════════════════════════════════════════════════════
                        
            Input: "Triển khai chi tiết PHASE 1"
            Nodes: ["PHASE 1: Nền tảng", "PHASE 2: Core", ...]
            Output:
            {
              "isExpansionRequest": true,
              "targetNodeTitle": "PHASE 1: Nền tảng",
              "targetNodeType": "PHASE",
              "expansionDepth": "FULL_DEPTH",
              "confidence": "HIGH",
              "reply": null
            }
                        
            Input: "Tạo các module cho giai đoạn nền tảng"
            Nodes: ["PHASE 1: Nền tảng & Công cụ", ...]
            Output:
            {
              "isExpansionRequest": true,
              "targetNodeTitle": "PHASE 1: Nền tảng & Công cụ",
              "targetNodeType": "PHASE",
              "expansionDepth": "MODULES_ONLY",
              "confidence": "HIGH",
              "reply": null
            }
                        
            Input: "Giải thích thêm về Spring Boot đi"
            Output:
            {
              "isExpansionRequest": false,
              "targetNodeTitle": null,
              "targetNodeType": null,
              "expansionDepth": null,
              "confidence": null,
              "reply": "Spring Boot là framework giúp đơn giản hóa việc phát triển ứng dụng Java. Bạn muốn biết chi tiết về phần nào của Spring Boot?"
            }
            """;

    //VALIDATION PROMPT

    public static final String VALIDATION_PROMPT = """
            Kiểm tra tính hợp lệ của roadmap JSON vừa tạo.
                        
            ════════════════════════════════════════════════════════
            INPUT
            ════════════════════════════════════════════════════════
                        
            Roadmap JSON: %s
                        
            ════════════════════════════════════════════════════════
            CÁC ĐIỂM CẦN KIỂM TRA
            ════════════════════════════════════════════════════════
                        
            1. THỜI GIAN:
               ☑ Tổng hours của children = parent hours?
               ☑ Mỗi node có estimated_hours > 0?
               ☑ Không có task nào > 20 giờ?
                        
            2. CẤU TRÚC:
               ☑ ROOT có đủ 4-8 PHASE?
               ☑ Mỗi PHASE có 2-4 MODULE?
               ☑ Mỗi MODULE có 2-5 LESSON?
               ☑ Mỗi LESSON có 2-4 TASK?
                        
            3. NỘI DUNG:
               ☑ Mỗi node có đủ các trường bắt buộc?
               ☑ TASK có động từ hành động cụ thể?
               ☑ TASK có sản phẩm đầu ra rõ ràng?
               ☑ Description có đủ chi tiết (> 20 ký tự)?
                        
            4. LOGIC:
               ☑ Difficulty progression hợp lý?
               ☑ Tags nhất quán trong cùng nhánh?
               ☑ Prerequisites hợp lý?
                        
            ════════════════════════════════════════════════════════
            OUTPUT FORMAT
            ════════════════════════════════════════════════════════
                        
            {
              "isValid": true/false,
              "overallScore": 85 (điểm tổng 0-100),
              "errors": [
                {
                  "severity": "ERROR | WARNING | INFO",
                  "location": "path.to.node (VD: ROOT.PHASE[0].MODULE[1])",
                  "message": "Mô tả lỗi cụ thể",
                  "suggestion": "Cách sửa"
                }
              ],
              "summary": {
                "totalNodes": 150,
                "totalHours": 420,
                "hoursValidation": "PASS | FAIL",
                "structureValidation": "PASS | FAIL",
                "contentValidation": "PASS | FAIL"
              }
            }
                        
            Nếu không có lỗi, trả về isValid=true và errors=[].
            """;

    //ADJUSTMENT PROMPT

    public static final String ROADMAP_ADJUSTMENT_PROMPT = """
            Điều chỉnh roadmap theo yêu cầu của người dùng.
                        
            ════════════════════════════════════════════════════════
            INPUT
            ════════════════════════════════════════════════════════
                        
            - Roadmap hiện tại: %s
            - Yêu cầu điều chỉnh: %s
            - Node cần chỉnh (nếu có): %s
                        
            ════════════════════════════════════════════════════════
            CÁC LOẠI ĐIỀU CHỈNH HỖ TRỢ
            ════════════════════════════════════════════════════════
                        
            1. THAY ĐỔI THỜI GIAN:
               - "Tăng/giảm thời gian PHASE X lên/xuống Y giờ"
               - "Rút ngắn tổng thời gian còn 200 giờ"
               → Tự động tái phân bổ hours cho children
                        
            2. THÊM/XÓA NỘI DUNG:
               - "Thêm module về Docker vào PHASE 3"
               - "Bỏ phần Kubernetes đi"
               → Thêm/xóa node và điều chỉnh lại hours
                        
            3. THAY ĐỔI THỨ TỰ:
               - "Đổi PHASE 2 và PHASE 3"
               - "Đưa module Git lên đầu"
               → Sắp xếp lại children
                        
            4. THAY ĐỔI ĐỘ KHÓ:
               - "Làm đơn giản PHASE 1 hơn"
               - "Tăng độ khó của các TASK trong module X"
               → Điều chỉnh difficulty và content
                        
            5. THÊM YÊU CẦU:
               - "Thêm phần về Security vào roadmap"
               - "Bổ sung thêm bài tập thực hành"
               → Mở rộng hoặc thêm nodes mới
                        
            ════════════════════════════════════════════════════════
            QUY TẮC KHI ĐIỀU CHỈNH
            ════════════════════════════════════════════════════════
                        
            1. GIỮ NGUYÊN CẤU TRÚC: ROOT → PHASE → MODULE → LESSON → TASK
            2. BẢO TỒN TỔNG THỜI GIAN: Σ(children.hours) = parent.hours
            3. DUY TRÌ LOGIC: Cơ bản → Nâng cao → Thực hành
            4. CẬP NHẬT TAGS: Đảm bảo tags nhất quán sau khi chỉnh
                        
            ════════════════════════════════════════════════════════
            OUTPUT FORMAT
            ════════════════════════════════════════════════════════
                        
            Trả về roadmap JSON đã được điều chỉnh (cùng structure ban đầu).
                        
            Kèm theo metadata:
            {
              "roadmap": { ...updated roadmap... },
              "changeLog": [
                "Tăng thời gian PHASE 1 từ 70h lên 90h",
                "Thêm MODULE 2.4: Docker & Containerization (20h)",
                "Điều chỉnh lại hours của PHASE 2,3,4"
              ]
            }
            """;

    //HELPER: Time Distribution

    public static final String TIME_DISTRIBUTION_GUIDELINES = """
            ════════════════════════════════════════════════════════
            HƯỚNG DẪN PHÂN BỔ THỜI GIAN
            ════════════════════════════════════════════════════════
                        
            PHASE Level (% of Total):
            ├─ PHASE 1 (Nền tảng): 15-20%%
            ├─ PHASE 2 (Core): 40-45%%
            ├─ PHASE 3 (Thực hành): 25-30%%
            └─ PHASE 4 (Chuẩn bị): 10-15%%
                        
            MODULE Level (% of PHASE):
            ├─ Module quan trọng: 30-40%%
            ├─ Module trung bình: 20-30%%
            └─ Module bổ trợ: 10-20%%
                        
            LESSON Level (giờ):
            ├─ Lesson lý thuyết: 2-6 giờ
            ├─ Lesson thực hành: 4-12 giờ
            └─ Lesson project: 8-20 giờ
                        
            TASK Level (giờ):
            ├─ Task nhỏ (reading, setup): 0.5-2 giờ
            ├─ Task vừa (coding exercise): 2-8 giờ
            └─ Task lớn (mini project): 8-16 giờ
                        
            LƯU Ý: Không chia đều cứng nhắc, ưu tiên nội dung quan trọng hơn!
            """;

    // ==================== CONSTANTS ====================

    public static final int MIN_PHASES = 4;
    public static final int MAX_PHASES = 8;
    public static final int MIN_MODULES_PER_PHASE = 2;
    public static final int MAX_MODULES_PER_PHASE = 4;
    public static final int MIN_LESSONS_PER_MODULE = 2;
    public static final int MAX_LESSONS_PER_MODULE = 5;
    public static final int MIN_TASKS_PER_LESSON = 2;
    public static final int MAX_TASKS_PER_LESSON = 4;

    public static final double PHASE1_TIME_RATIO = 0.175; // 15-20%
    public static final double PHASE2_TIME_RATIO = 0.425; // 40-45%
    public static final double PHASE3_TIME_RATIO = 0.275; // 25-30%
    public static final double PHASE4_TIME_RATIO = 0.125; // 10-15%

}