package com.rikai.backend.service.intern;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.dto.response.PageResponse;
import com.rikai.backend.dto.response.intern.InternResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.model.Intern;
import com.rikai.backend.model.Users;
import com.rikai.backend.repository.InternRepository;
import com.rikai.backend.service.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternService implements IInternService {
    
    InternRepository internRepository;
    AuthenticationService authenticationService;
    
    @Override
    @Transactional(readOnly = true)
    public List<InternResponse> getMyInterns() {
        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        List<Intern> interns;

        if ("ADMIN".equals(currentUser.getRole().getRoleName())) {
            interns = internRepository.findAll();
        } else {
            interns = internRepository.findByMentorId(currentUser.getId());
        }
        
        return interns.stream()
                .map(InternResponse::fromIntern)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<InternResponse> getAllInterns(Pageable pageable) {
        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
        Page<Intern> internsPage;
        
        if ("ADMIN".equals(currentUser.getRole().getRoleName())) {
            internsPage = internRepository.findAll(pageable);
        } else {
            internsPage = internRepository.findByMentorId(currentUser.getId(), pageable);
        }
        
        List<InternResponse> responses = internsPage.getContent().stream()
                .map(InternResponse::fromIntern)
                .collect(Collectors.toList());
        
        return PageResponse.<InternResponse>builder()
                .items(responses)
                .currentPage(internsPage.getNumber())
                .totalPages(internsPage.getTotalPages())
                .totalItems(internsPage.getTotalElements())
                .pageSize(internsPage.getSize())
                .build();
    }
}
