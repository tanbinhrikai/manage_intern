package com.rikai.backend.dto.request.intern;

import com.rikai.backend.common.InternStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InternStatusUpdateRequest {
    @NotNull(message = "internStatus is required")
    private InternStatus internStatus;
}
