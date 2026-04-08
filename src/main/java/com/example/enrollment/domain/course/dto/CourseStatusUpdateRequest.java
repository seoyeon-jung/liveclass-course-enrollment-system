package com.example.enrollment.domain.course.dto;

import com.example.enrollment.domain.course.entity.CourseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CourseStatusUpdateRequest {

    @NotNull(message = "변경할 상태는 필수입니다.")
    private CourseStatus status;

    // 테스트용 생성자
    public CourseStatusUpdateRequest(CourseStatus status) {
        this.status = status;
    }

}
