package com.example.enrollment.domain.enrollment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class EnrollmentCreateRequest {

    @NotNull(message = "강의 ID는 필수입니다.")
    private Long courseId;

}
