package com.rikai.backend.ai.util;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public record ValidationResult(boolean valid, List<String> errors) {
}
