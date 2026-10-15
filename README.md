# manage_intern
manage intern

## 1. Overview & Purpose

The Intern Management System is a centralized platform designed to manage intern data, track progress, and ensure a structured and transparent internship process within an organization. It enables consistent monitoring of intern performance, maintains a clear history of activities, and supports data-driven evaluation. The system currently involves two primary roles: Admin and Mentor.

---

## 2. Tech Stack

- **Backend:** Spring Boot (Java 21, Gradle)
- **Frontend:** Vue 3 + Vite + Node.js 18 or higher (with npm)
- **Database:** MySQL
- **Documentation:** Swagger/OpenAPI

---

## 3. Features


- **As an** Admin, **I want to** view the dashboard overview, **so that** I can quickly monitor the overall status of the system  
- **As an** Admin, **I want to** manage mentors, **so that** I can assign and maintain mentors responsible for guiding interns  
- **As an** Admin, **I want to** manage intern information (create, update, view), **so that** the organization can track all interns in a centralized system  
- **As an** Admin, **I want to** manage departments, **so that** the system reflects the organizational structure accurately  
- **As an** Admin, **I want to** manage positions, **so that** interns can be categorized based on their roles  
- **As an** Admin, **I want to** manage internship batches, **so that** interns can be organized and tracked by intake period  
- **As an** Admin, **I want to** view my profile, **so that** I can review and update my personal information  

- **As a** Mentor, **I want to** view the mentor dashboard, **so that** I can quickly track the status of my assigned interns  
- **As a** Mentor, **I want to** view my assigned interns, **so that** I can monitor and support them effectively  
- **As a** Mentor, **I want to** update intern information, **so that** I can keep their progress and details accurate  
- **As a** Mentor, **I want to** manage reports for intern evaluation, **so that** I can record feedback and assess performance consistently  
- **As a** Mentor, **I want to** view my profile, **so that** I can review and update my personal information  

---

## 4. Architecture

- **REST API:** Spring Boot backend serving RESTful endpoints
- **Single Page Application (SPA):** Vue 3 with client-side routing
- **MySQL Database:** Persistent data storage with structured schema

---

## 5. Project Structure

**Backend Structure:**
```
backend/src/main/java/com/rikai/backend/
├── controller/          # REST endpoints (UserController, InternController, etc.)
├── service/             # Business logic (interfaces + implementations)
│   └── user/
│   └── intern/
│   └── auth/
├── repository/          # Data access (UsersRepository, InternsRepository, etc.)
├── model/               # JPA entities (@Entity classes)
├── dto/                 # Data Transfer Objects
│   ├── request/        # Incoming request DTOs
│   └── response/       # Outgoing response DTOs
├── mapper/             # MapStruct mappers
├── exception/          # Custom exceptions
├── common/             # Shared utilities (ApiResponse, ErrorCode, PageResponse)
├── config/             # Configuration classes
├── security/           # Security configuration
├── validation/         # Custom validators
└── ai/                 # AI integration modules
    ├── config/         # AI client configuration
    ├── tool/           # AI tools/functions
    └── prompt/         # Prompt management
```

**Frontend Structure:**
```
frontend/src/
├── views/              # Page components (View layer)
│   ├── auth/
│   ├── dashboard/
│   ├── intern-management/
│   ├── evaluation-session/
│   └── ...
├── components/         # Reusable UI components
│   ├── layout/        # Layout components
│   ├── dashboard/     # Dashboard-specific components
│   ├── intern/        # Intern-related components
│   └── ui/            # Generic UI components
├── composables/        # Reusable composition functions
├── stores/            # Pinia state stores
├── api/               # HTTP API layer
├── routers/           # Vue Router configuration
├── locales/           # i18n translations
├── types/             # Type definitions
├── utils/             # Utility functions
└── layouts/           # Layout templates
```
---
## 6. Getting Started
   * Thứ tự chạy
     + Cài đặt phiên bản JDK, Node.js, MySQL và IntelliJ.
     + Tạo database, cấu hình .env ở FrontEnd và BackEnd và kiểm tra kết nối.
   * Cách tạo tài khoản admin đầu tiên
     + Ở file .env trong /BackEnd cập nhật 2 giá trị ADMIN_EMAIL & ADMIN_PASSWORD
     + ADMIN_EMAIL & ADMIN_PASSWORD dùng để khởi tạo tài khoản với role là ADMIN ( tài khoản này được tạo trong file ApplicationInitConfig.java)
   * ## 8. Troubleshooting
     + Error: DotenvException: Malformed entry (fix: Sửa dòng chú thích bắt đầu bằng // trong .env thành #)
     + Error: No configured target data sources (fix: Cấu hình data source và schema trong IntelliJ trước khi chạy SQL bằng IDE.)
   * Environment Information
     + OS: Ubuntu 24.04.5 LTS
     + Java: JDK 21
     + Node: 24
     + MySql: Ver 8.0.46
### 6.1 Prerequisites
 -  **Install the required versions of JDK, Node.js, MySQL, and IntelliJ IDEA.**
 -  **Create the database and run scripts `backend/ddl` and configure the .env files for both the frontend and backend.**
 -  **Verify the database connection before starting the application.**
 -  **Set the `ADMIN_EMAIL` and `ADMIN_PASSWORD` environment variables in `backend/.env`**
 - Before you begin, ensure you have the following installed:
   - **Java 21**
   - **Node.js 24** or higher (with npm)
   - **MySQL 8.0**
   - **Git**

### 6.2 Environment Variables

1. Navigate to the `frontend` or `backend` directory
2. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
3. Update the `.env` file with your configuration:

### 6.3 Backend Setup

```bash
cd backend
./gradlew build
./gradlew bootRun
```

The backend will start on `http://localhost:8080`

### 6.4 Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

The frontend will start on `http://localhost:5173`

---

## 7. API Documentation

The API documentation is available via Swagger UI:

**Access Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

This provides:
- Interactive API endpoints documentation
- Request/response examples
- Try-it-out functionality to test endpoints directly

---

---
## 8. Troubleshooting
- **Error: `DotenvException: Malformed entry`**  
Fix: Remove or replace comment lines starting with `//` in the `.env` file with `#`.

- **Error: `No configured target data sources`**  
  Fix: Configure the data source and schema c ..
- in IntelliJ IDEA before executing SQL queries through the IDE.
---

