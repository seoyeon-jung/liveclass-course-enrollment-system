package com.example.enrollment.domain.enrollment.dto;

import com.example.enrollment.domain.enrollment.entity.Enrollment;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class EnrollmentCreateRequest {

    @NotNull(message = "강의 ID는 필수입니다.")
    private Long courseId;

    // test
    public EnrollmentCreateRequest(Long courseId) {
        this.courseId = courseId;
    }
}
