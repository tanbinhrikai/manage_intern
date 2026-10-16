package com.rikai.backend.service;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.common.InternStatus;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.mapper.InternMapper;
import com.rikai.backend.model.Intern;
import com.rikai.backend.model.InternshipBatch;
import com.rikai.backend.model.Position;
import com.rikai.backend.model.Users;
import com.rikai.backend.repository.InternRepository;
import com.rikai.backend.repository.InternshipBatchRepository;
import com.rikai.backend.repository.PositionRepository;
import com.rikai.backend.service.auth.AuthenticationService;
import com.rikai.backend.service.intern.InternService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InternServiceTest {

    @InjectMocks
    private InternService internService;

    @Mock
    private InternRepository internRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private InternshipBatchRepository internshipBatchRepository;
    @Mock
    private InternMapper internMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("Admin Export CSV: includes BOM, correct column order for Admin, and null values are converted to empty cells")
    void testExportCsv_Admin_ShouldHaveBom_CorrectHeaders_AndHandleNullValues() throws Exception {
        Intern intern = new Intern();
        intern.setId(1L);
        intern.setFullName("thanh");
        intern.setEmail(null);
        intern.setPhone(null);

        Page<Intern> internPage = new PageImpl<>(List.of(intern), PageRequest.of(0, 500), 1);
        when(internRepository.getAllInternByKeyword(any(Pageable.class), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(internPage);

        StreamingResponseBody responseBody = internService.exportInterns(
                null, null, null, null, null, null
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        responseBody.writeTo(outputStream);

        byte[] csvBytes = outputStream.toByteArray();
        String csvContent = new String(csvBytes, 3, csvBytes.length - 3, StandardCharsets.UTF_8)
                .replace("\r\n", "\n");

        String expectedCsv="Full name,Position,Mentor,Start date,End date,Status,ID,Email,Phone,Batch\n" +
                "thanh,,,,,,1,,,\n";
        // 1. Check BOM (EF BB BF)
        assertTrue(csvBytes.length >= 3, "CSV file has no content");
        assertEquals((byte) 0xEF, csvBytes[0], "Missing BOM byte 1");
        assertEquals((byte) 0xBB, csvBytes[1], "Missing BOM byte 2");
        assertEquals((byte) 0xBF, csvBytes[2], "Missing BOM byte 3");
        assertEquals(expectedCsv,csvContent );

    }

    @Test
    @DisplayName("Mentor Export CSV: includes BOM, correct column order for Mentor (without the Mentor column), and null values are converted to empty cells")
    void testExportCsv_Mentor_ShouldHaveBom_CorrectHeaders_AndHandleNullValues() throws Exception {
        UUID mentorId = UUID.randomUUID();
        Users currentMentor = new Users();
        currentMentor.setId(mentorId);
        currentMentor.setFullName("Mentor A");

        when(authenticationService.getCurrentUser()).thenReturn(currentMentor);

        Intern intern = new Intern();
        intern.setId(2L);
        intern.setFullName("Intern Of Mentor A");
        intern.setEmail(null);
        intern.setPhone(null);

        Page<Intern> internPage = new PageImpl<>(List.of(intern), PageRequest.of(0, 500), 1);
        when(internRepository.getAllInternByKeyword(any(), any(), any(), any(), any(), any(), eq(mentorId)))
                .thenReturn(internPage);

        StreamingResponseBody responseBody = internService.exportMyInterns(
                null, null, null, null, null
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        responseBody.writeTo(outputStream);

        byte[] csvBytes = outputStream.toByteArray();
        String csvContent = new String(csvBytes, 3, csvBytes.length - 3, StandardCharsets.UTF_8)
                .replace("\r\n", "\n");

        assertTrue(csvBytes.length >= 3);
        assertEquals((byte) 0xEF, csvBytes[0]);
        assertEquals((byte) 0xBB, csvBytes[1]);
        assertEquals((byte) 0xBF, csvBytes[2]);



        String expectedCsv =  "Full name,Position,Start date,End date,Status,ID,Email,Phone,Batch\n" + "Intern Of Mentor A,,,,,2,,,\n";
        assertEquals(expectedCsv, csvContent);
    }

    @Test
    @DisplayName("Escape CSV: handles names containing commas, double quotes, and newline characters")
    void testExportCsv_SpecialCharacters_Comma_Quote_Newline_ShouldEscapeProperly() throws Exception {
        Intern intern = new Intern();
        intern.setId(999L);
        intern.setFullName("Nguyen, \"Huu\"\nThanh");
        intern.setEmail("thanh@rikai.technology");
        intern.setPhone("0987654321");
        intern.setInternStatus(InternStatus.ACTIVE);

        Position position = new Position();
        position.setTitle("Java Backend");
        intern.setPosition(position);

        Users mentor = new Users();
        mentor.setFullName("Mentor A");
        intern.setMentor(mentor);

        InternshipBatch batch = new InternshipBatch();
        batch.setName("Fall 2026");
        intern.setInternshipBatch(batch);

        intern.setStartDate(LocalDate.of(2026, 10, 1));
        intern.setEndDate(LocalDate.of(2026, 12, 31));

        Page<Intern> internPage = new PageImpl<>(List.of(intern), PageRequest.of(0, 500), 1);
        when(internRepository.getAllInternByKeyword(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(internPage);

        StreamingResponseBody responseBody = internService.exportInterns(null, null, null, null, null, null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        responseBody.writeTo(outputStream);

        byte[] csvBytes = outputStream.toByteArray();
        String csvContent = new String(csvBytes, 3, csvBytes.length - 3, StandardCharsets.UTF_8);

        String expectedHeader = "Full name,Position,Mentor,Start date,End date,Status,ID,Email,Phone,Batch\n";
        String expectedRow = "\"Nguyen, \"\"Huu\"\"\nThanh\",Java Backend,Mentor A,2026-10-01,2026-12-31,ACTIVE,999,thanh@rikai.technology,0987654321,Fall 2026\n";

        assertEquals(expectedHeader + expectedRow, csvContent);
    }

    @Test
    @DisplayName("CSV Injection: names starting with = are neutralized by prefixing an apostrophe")
    void testExportCsv_CsvInjectionInName_ShouldBeNeutralized() throws Exception {
        Intern intern = new Intern();
        intern.setId(100L);
        intern.setFullName("=cmd|' /C calc'!A0");

        Page<Intern> internPage = new PageImpl<>(List.of(intern), PageRequest.of(0, 500), 1);
        when(internRepository.getAllInternByKeyword(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(internPage);

        StreamingResponseBody responseBody = internService.exportInterns(null, null, null, null, null, null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        responseBody.writeTo(outputStream);

        byte[] csvBytes = outputStream.toByteArray();

        String csvContent = new String(csvBytes, 3, csvBytes.length - 3, StandardCharsets.UTF_8)
                .replace("\r\n", "\n");

        String expectedCsv =
                "Full name,Position,Mentor,Start date,End date,Status,ID,Email,Phone,Batch\n" +
                        "'=cmd|' /C calc'!A0,,,,,,100,,,\n";

        assertEquals(expectedCsv, csvContent);
    }

    @Test
    @DisplayName("Invalid status throws AppException with INVALID_INTERN_STATUS")
    void testExportCsv_WithInvalidStatus_ShouldThrowInvalidInternStatus() {
        String invalidStatus = "INVALID_STATUS";

        AppException adminEx = assertThrows(
                AppException.class,
                () -> internService.exportInterns(null, invalidStatus, null, null, null, null)
        );
        assertEquals(ErrorCode.INVALID_INTERN_STATUS, adminEx.getErrorCode());

        when(authenticationService.getCurrentUser()).thenReturn(new Users());
        AppException mentorEx = assertThrows(
                AppException.class,
                () -> internService.exportMyInterns(null, invalidStatus, null, null, null)
        );
        assertEquals(ErrorCode.INVALID_INTERN_STATUS, mentorEx.getErrorCode());
    }

    @Test
    @DisplayName("Mentor export includes only interns assigned to the currently logged-in mentor")
    void testExportMyInterns_ShouldOnlyExportLoggedInMentorInterns() throws Exception {
        UUID loggedInMentorId = UUID.randomUUID();
        Users loggedInMentor = new Users();
        loggedInMentor.setId(loggedInMentorId);
        loggedInMentor.setFullName("Logged In Mentor");

        when(authenticationService.getCurrentUser()).thenReturn(loggedInMentor);

        Intern mentorIntern = new Intern();
        mentorIntern.setId(10L);
        mentorIntern.setFullName("Intern Belonging To LoggedInMentor");
        mentorIntern.setMentor(loggedInMentor);

        Page<Intern> page = new PageImpl<>(List.of(mentorIntern), PageRequest.of(0, 500), 1);
        when(internRepository.getAllInternByKeyword(any(), any(), any(), any(), any(), any(), eq(loggedInMentorId)))
                .thenReturn(page);

        StreamingResponseBody responseBody = internService.exportMyInterns("intern", "ACTIVE", null, null, 1L);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        responseBody.writeTo(outputStream);

        ArgumentCaptor<UUID> mentorIdCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(internRepository, times(1)).getAllInternByKeyword(
                any(Pageable.class),
                eq("intern"),
                eq(InternStatus.ACTIVE),
                isNull(),
                isNull(),
                eq(1L),
                mentorIdCaptor.capture()
        );
        assertEquals(loggedInMentorId, mentorIdCaptor.getValue());
        byte[] csvBytes = outputStream.toByteArray();
        String csvContent = new String(csvBytes, 3, csvBytes.length - 3, StandardCharsets.UTF_8)
                .replace("\r\n", "\n");

        String expectedCsv =
                "Full name,Position,Start date,End date,Status,ID,Email,Phone,Batch\n" +
                        "Intern Belonging To LoggedInMentor,,,,,10,,,\n";

        assertEquals(expectedCsv, csvContent);
    }

    @Test
    @DisplayName("Batch processing: exports all rows ")
    void testExportCsv_BatchReading_MultiplePages_ShouldExportAllWithoutDuplicatesOrMissingRows() throws Exception {
        int batchSize = 500;
        int totalInterns = 505;

        List<Intern> firstBatch = new ArrayList<>();
        for (long i = 1; i <= 500; i++) {
            Intern intern = new Intern();
            intern.setId(i);
            intern.setFullName("Intern " + i);
            firstBatch.add(intern);
        }

        List<Intern> secondBatch = new ArrayList<>();
        for (long i = 501; i <= totalInterns; i++) {
            Intern intern = new Intern();
            intern.setId(i);
            intern.setFullName("Intern " + i);
            secondBatch.add(intern);
        }

        Pageable page0 = PageRequest.of(0, batchSize, Sort.by("id").ascending());
        Pageable page1 = PageRequest.of(1, batchSize, Sort.by("id").ascending());

        Page<Intern> firstPage = new PageImpl<>(firstBatch, page0, totalInterns);
        Page<Intern> secondPage = new PageImpl<>(secondBatch, page1, totalInterns);

        when(internRepository.getAllInternByKeyword(eq(page0), any(), any(), any(), any(), any(), any()))
                .thenReturn(firstPage);
        when(internRepository.getAllInternByKeyword(eq(page1), any(), any(), any(), any(), any(), any()))
                .thenReturn(secondPage);

        StreamingResponseBody responseBody = internService.exportInterns(null, null, null, null, null, null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        responseBody.writeTo(outputStream);

        verify(internRepository, times(1)).getAllInternByKeyword(eq(page0), any(), any(), any(), any(), any(), any());
        verify(internRepository, times(1)).getAllInternByKeyword(eq(page1), any(), any(), any(), any(), any(), any());

        byte[] csvBytes = outputStream.toByteArray();
        String csvContent = new String(csvBytes, 3, csvBytes.length - 3, StandardCharsets.UTF_8);
        String[] lines = csvContent.split("\n");

        // 1 dòng header + 505 dòng dữ liệu = 506 dòng
        assertEquals(totalInterns + 1, lines.length);

        Set<Long> exportedIds = new HashSet<>();
        for (int i = 1; i <= totalInterns; i++) {
            String[] cols = lines[i].replace("\r", "").split(",", -1);
            long id = Long.parseLong(cols[6]); // Cột ID ở vị trí index 6 của Admin CSV
            assertEquals("Intern " + i, cols[0]);
            assertEquals(i, id);
            assertTrue(exportedIds.add(id));
        }

        assertEquals(totalInterns, exportedIds.size());
    }

    @Test
    @DisplayName("Admin Export: normalizes keyword (trims whitespace and converts empty strings to null)")
    void testExportInterns_ShouldNormalizeKeywordProperly() throws Exception {
        Page<Intern> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 500, Sort.by("id").ascending()), 0);
        when(internRepository.getAllInternByKeyword(any(Pageable.class), any(), any(), any(), any(), any(), any()))
                .thenReturn(emptyPage);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        StreamingResponseBody stream1 = internService.exportInterns("  an  ", null, null, null, null, null);
        stream1.writeTo(outputStream);
        verify(internRepository).getAllInternByKeyword(
                any(Pageable.class),
                eq("an"),
                isNull(), isNull(), isNull(), isNull(), isNull()
        );

        StreamingResponseBody stream2 = internService.exportInterns("   ", null, null, null, null, null);
        stream2.writeTo(outputStream);
        verify(internRepository).getAllInternByKeyword(
                any(Pageable.class),
                isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull()
        );


        StreamingResponseBody stream3 = internService.exportInterns("", null, null, null, null, null);
        stream3.writeTo(outputStream);
        verify(internRepository, times(2)).getAllInternByKeyword(
                any(Pageable.class),
                isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull()
        );
    }

    @Test
    @DisplayName("Admin Export: passes all parameters to the repository correctly and completely (no missing or swapped parameters)")
    void testExportInterns_ShouldPassAllFilterParametersCorrectlyToRepository() throws Exception {
        // Arrange
        String keyword = "thanh";
        String status = "ACTIVE";
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);
        Long positionId = 5L;
        UUID mentorId = UUID.randomUUID();

        Page<Intern> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 500, Sort.by("id").ascending()), 0);
        when(internRepository.getAllInternByKeyword(
                any(Pageable.class),
                eq(keyword),
                eq(InternStatus.ACTIVE),
                eq(startDate),
                eq(endDate),
                eq(positionId),
                eq(mentorId)
        )).thenReturn(emptyPage);

        // Act
        StreamingResponseBody responseBody = internService.exportInterns(
                keyword, status, startDate, endDate, positionId, mentorId
        );
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        responseBody.writeTo(outputStream);


        verify(internRepository, times(1)).getAllInternByKeyword(
                eq(PageRequest.of(0, 500, Sort.by("id").ascending())),
                eq("thanh"),
                eq(InternStatus.ACTIVE),
                eq(LocalDate.of(2026, 1, 1)),
                eq(LocalDate.of(2026, 12, 31)),
                eq(5L),
                eq(mentorId)
        );
    }


}