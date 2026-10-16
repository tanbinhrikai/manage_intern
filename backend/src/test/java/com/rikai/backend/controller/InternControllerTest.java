package com.rikai.backend.controller;

import com.rikai.backend.exception.GlobalExceptionHandler;
import com.rikai.backend.security.CustomJwtDecoder;
import com.rikai.backend.security.JwtAuthenticationEntryPoint;
import com.rikai.backend.security.SecurityConfig;
import com.rikai.backend.service.intern.IInternService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InternController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, GlobalExceptionHandler.class})
class InternControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IInternService internService;

    @MockBean
    private CustomJwtDecoder customJwtDecoder;

    @Test
    @DisplayName("Unauthenticated request to /interns/export returns 401 Unauthorized")
    void exportInterns_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/interns/export"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(internService);
    }

    @Test
    @DisplayName("Unauthenticated request to /interns/my-interns/export -> 401 Unauthorized")
    void exportMyInterns_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/interns/my-interns/export"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(internService);
    }

    @Test
    @DisplayName("Mentor request /interns/export -> 403 Forbidden")
    @WithMockUser(roles = "MENTOR")
    void exportInterns_AsMentor_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/interns/export"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(internService);
    }

    @Test
    @DisplayName("Admin request /interns/my-interns/export -> 403 Forbidden")
    @WithMockUser(roles = "ADMIN")
    void exportMyInterns_AsAdmin_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/interns/my-interns/export"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(internService);
    }

    @Test
    @DisplayName("Admin request /interns/export -> 200 OK and include  Content-Disposition")
    @WithMockUser(roles = "ADMIN")
    void exportInterns_AsAdmin_ShouldReturn200AndContentDisposition() throws Exception {
        StreamingResponseBody stream = outputStream ->
                outputStream.write("\uFEFFFull name,Position\n".getBytes(StandardCharsets.UTF_8));

        when(internService.exportInterns(any(), any(), any(), any(), any(), any()))
                .thenReturn(stream);

        MvcResult mvcResult= mockMvc.perform(get("/interns/export"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, startsWith("attachment; filename=\"interns_")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString(".csv\"")))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8"))
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(startsWith("\uFEFFFull name,Position")));

        verify(internService, times(1)).exportInterns(null, null, null, null, null, null);
    }

    @Test
    @DisplayName("Mentor request to /interns/my-interns/export with a different mentor_id returns 200 OK, includes Content-Disposition, and ignores the mentor_id query parameter")
    @WithMockUser(roles = "MENTOR")
    void exportMyInterns_AsMentor_WithOtherMentorIdParam_ShouldReturn200AndIgnoreOtherMentorId() throws Exception {
        UUID otherMentorId = UUID.randomUUID();
        StreamingResponseBody stream = outputStream ->
                outputStream.write("\uFEFFFull name,Position\n".getBytes(StandardCharsets.UTF_8));

        when(internService.exportMyInterns(any(), any(), any(), any(), any()))
                .thenReturn(stream);

        MvcResult mvcResult= mockMvc.perform(get("/interns/my-interns/export")
                        .param("keyword", "thanh")
                        .param("mentor_id", otherMentorId.toString()))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, startsWith("attachment; filename=\"interns_")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString(".csv\"")))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8"))
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(startsWith("\uFEFFFull name,Position")));

        verify(internService, times(1)).exportMyInterns(eq("thanh"), isNull(), isNull(), isNull(), isNull());
        verify(internService, never()).exportInterns(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Admin calls /interns/export with all query parameters (snake_case) -> correctly binds each parameter to the service")
    @WithMockUser(roles = "ADMIN")
    void exportInterns_AsAdmin_WithAllQueryParams_ShouldBindParametersCorrectly() throws Exception {
        UUID mentorId = UUID.randomUUID();
        StreamingResponseBody stream = outputStream -> {};
        when(internService.exportInterns(any(), any(), any(), any(), any(), any()))
                .thenReturn(stream);

        mockMvc.perform(get("/interns/export")
                        .param("keyword", "thanh")
                        .param("status", "ACTIVE")
                        .param("start_date", "2026-10-01")
                        .param("end_date", "2026-12-31")
                        .param("position_id", "5")
                        .param("mentor_id", mentorId.toString()))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION));

        verify(internService, times(1)).exportInterns(
                eq("thanh"),
                eq("ACTIVE"),
                eq(LocalDate.of(2026, 10, 1)),
                eq(LocalDate.of(2026, 12, 31)),
                eq(5L),
                eq(mentorId)
        );

    }
}

