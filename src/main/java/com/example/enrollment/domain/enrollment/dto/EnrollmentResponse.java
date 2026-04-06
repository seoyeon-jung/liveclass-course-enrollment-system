package com.example.enrollment.domain.enrollment.dto;

import com.example.enrollment.domain.enrollment.entity.Enrollment;
import com.example.enrollment.domain.enrollment.entity.EnrollmentStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EnrollmentResponse {

    private final Long id;
    private final Long userId;
    private final Long courseId;
    private final String courseTitle;
    private final EnrollmentStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public EnrollmentResponse(Enrollment enrollment) {
        this.id = enrollment.getId();
        this.userId = enrollment.getUserId();
        this.courseId = enrollment.getCourse().getId();
        this.courseTitle = enrollment.getCourse().getTitle();
        this.status = enrollment.getStatus();
        this.createdAt = enrollment.getCreatedAt();
        this.updatedAt = enrollment.getUpdatedAt();
    }
}
