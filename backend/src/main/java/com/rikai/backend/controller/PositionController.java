package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.PositionCreationRequest;
import com.rikai.backend.dto.request.PositionUpdateRequest;
import com.rikai.backend.dto.response.PositionResponse;
import com.rikai.backend.service.position.IPositionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/positions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PositionController {
    IPositionService positionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<List<PositionResponse>> getAllPositionsList() {
        return ApiResponse.buildSuccessResponse(positionService.getAllPositionsList(),
                SuccessCode.GET_ALL_POSITIONS_SUCCESSFUL);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<PositionResponse> getPositionById(@PathVariable Long id) {
        return ApiResponse.buildSuccessResponse(positionService.getPositionById(id),
                SuccessCode.GET_POSITION_SUCCESSFUL);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PositionResponse> createPosition(@Valid @RequestBody PositionCreationRequest request) {
        return ApiResponse.buildSuccessResponse(positionService.createPosition(request),
                SuccessCode.CREATE_POSITION_SUCCESSFUL);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PositionResponse> updatePosition(@PathVariable Long id,
            @Valid @RequestBody PositionUpdateRequest request) {
        return ApiResponse.buildSuccessResponse(positionService.updatePosition(id, request),
                SuccessCode.UPDATE_POSITION_SUCCESSFUL);
    }
}
