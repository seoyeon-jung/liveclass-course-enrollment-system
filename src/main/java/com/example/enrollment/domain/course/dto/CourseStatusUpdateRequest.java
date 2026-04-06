package com.example.enrollment.domain.course.dto;

import com.example.enrollment.domain.course.entity.CourseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CourseStatusUpdateRequest {

    @NotNull(message = "변경할 상태는 필수입니다.")
    private CourseStatus status;

}
