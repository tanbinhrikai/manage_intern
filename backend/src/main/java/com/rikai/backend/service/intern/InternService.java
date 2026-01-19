package com.rikai.backend.service.intern;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.common.InternStatus;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.request.InternCreationRequest;
import com.rikai.backend.dto.request.InternUpdateRequest;
import com.rikai.backend.dto.response.InternAnalysisResponse;
import com.rikai.backend.dto.response.InternResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.mapper.InternMapper;
import com.rikai.backend.model.Intern;
import com.rikai.backend.model.Position;
import com.rikai.backend.model.Users;
import com.rikai.backend.repository.InternRepository;
import com.rikai.backend.repository.PositionRepository;
import com.rikai.backend.repository.UsersRepository;
import com.rikai.backend.service.auth.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternService implements IInternService {
    InternRepository internRepository;
    PositionRepository positionRepository;
    UsersRepository usersRepository;
    InternMapper internMapper;
    AuthenticationService authenticationService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InternResponse> getAllInterns(
            PageRequest pageRequest,
            String keyword,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            Long positionId,
            UUID mentorId
    ) {
        String keywordValue = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        InternStatus internStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                internStatus = InternStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.INVALID_INTERN_STATUS);
            }
        }

        Page<Intern> internPage = internRepository
                .getAllInternByKeyword(pageRequest, keywordValue, internStatus, startDate, endDate, positionId, mentorId);
        return PageResponse.fromPage(internPage.map(internMapper::toInternResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public InternResponse getInternById(Long id) {
        Intern intern = internRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INTERN_NOT_EXISTED));
        return internMapper.toInternResponse(intern);
    }

    @Override
    @Transactional
    public InternResponse createIntern(InternCreationRequest request) {
        validateDateRange(request.getStartDate(), request.getEndDate());
        Position position = getPosition(request.getPositionId());
        Users mentor = getMentor(request.getMentorId());

        Intern intern = internMapper.toIntern(request);
        intern.setPosition(position);
        intern.setMentor(mentor);

        Intern saved = internRepository.save(intern);
        return internMapper.toInternResponse(saved);
    }

    @Override
    @Transactional
    public InternResponse updateIntern(Long id, InternUpdateRequest request) {

        Intern intern = internRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INTERN_NOT_EXISTED));

        validateDateRange(request.getStartDate(), request.getEndDate());

        Position position = getPosition(request.getPositionId());
        Users mentor = getMentor(request.getMentorId());

        intern.setFullName(request.getFullName());
        intern.setPosition(position);
        intern.setMentor(mentor);
        intern.setStartDate(request.getStartDate());
        intern.setEndDate(request.getEndDate());
        intern.setInternStatus(request.getInternStatus());

        Intern saved = internRepository.save(intern);
        return internMapper.toInternResponse(saved);
    }

    @Override
    @Transactional
    public void deleteIntern(Long id) {
        Intern intern = internRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INTERN_NOT_EXISTED));
        intern.setInternStatus(InternStatus.DROPPED);
        internRepository.save(intern);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InternResponse> getInternsByMentor(UUID mentorId, Pageable pageable) {

        if (!usersRepository.existsById(mentorId)) {
            throw new AppException(ErrorCode.MENTOR_NOT_EXISTED);
        }

        Page<Intern> internPage = internRepository.findByMentor_Id(mentorId, pageable);

        return PageResponse.fromPage(internPage.map(internMapper::toInternResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InternResponse> getInternsByPositionId(Long positionId, Pageable pageable) {

        if (!positionRepository.existsById(positionId)) {
            throw new AppException(ErrorCode.POSITION_NOT_EXISTED);
        }

        Page<Intern> internPage = internRepository.findByPosition_Id(positionId, pageable);

        return PageResponse.fromPage(internPage.map(internMapper::toInternResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InternResponse> getMyIntern(Pageable pageable, String keyword) {
        Users users = authenticationService.getCurrentUser();
        var mentorId = users.getId();
        if (!usersRepository.existsById(mentorId)) {
            throw new AppException(ErrorCode.MENTOR_NOT_EXISTED);
        }

        Page<Intern> internPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            internPage = internRepository.findByMentor_IdAndKeyword(mentorId, keyword.trim(), pageable);
        } else {
            internPage = internRepository.findByMentor_Id(mentorId, pageable);
        }

        return PageResponse.fromPage(internPage.map(internMapper::toInternResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InternResponse> getInternsByStatus(InternStatus status, Pageable pageable) {

        Page<Intern> internPage = internRepository.findByInternStatus(status, pageable);

        return PageResponse.fromPage(internPage.map(internMapper::toInternResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InternResponse> getInternsNotEvaluatedThisWeek(Pageable pageable) {
        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        LocalDate today = LocalDate.now();
        LocalDate weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        Page<Intern> internPage;
        if ("ADMIN".equals(currentUser.getRole().getRoleName())) {
            internPage = internRepository.findAllInternsNotEvaluatedThisWeek(weekStartDate, pageable);
        } else {
            UUID mentorId = currentUser.getId();
            internPage = internRepository.findInternsNotEvaluatedThisWeekByMentor(mentorId, weekStartDate, pageable);
        }

        return PageResponse.fromPage(internPage.map(internMapper::toInternResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public InternAnalysisResponse getAnalysis() {
        long totalInterns = internRepository.count();
        long totalMentors = usersRepository.countByRole_RoleName("MENTOR");
        long activeInterns = internRepository.countByInternStatus(InternStatus.ACTIVE);
        long warningInterns = internRepository.countByInternStatus(InternStatus.WARNING);
        long droppedInterns = internRepository.countByInternStatus(InternStatus.DROPPED);
        long completedInterns = internRepository.countByInternStatus(InternStatus.COMPLETED);

        return InternAnalysisResponse.builder()
                .totalInterns(totalInterns)
                .totalMentors(totalMentors)
                .activeInterns(activeInterns)
                .warningInterns(warningInterns)
                .droppedInterns(droppedInterns)
                .completedInterns(completedInterns)
                .build();
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
    }

    private Position getPosition(Long positionId) {
        return positionRepository.findById(positionId)
                .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_EXISTED));
    }

    private Users getMentor(UUID mentorId) {
        return usersRepository.findById(mentorId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_NOT_EXISTED));
    }
}
