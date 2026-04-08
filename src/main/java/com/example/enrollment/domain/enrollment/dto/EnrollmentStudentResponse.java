package com.example.enrollment.domain.enrollment.dto;

import com.example.enrollment.domain.enrollment.entity.Enrollment;
import com.example.enrollment.domain.enrollment.entity.EnrollmentStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EnrollmentStudentResponse {

    private final Long enrollmentId;
    private final Long userId;
    private final EnrollmentStatus status;
    private final LocalDateTime enrolledAt;
    private final LocalDateTime confirmedAt;

    public EnrollmentStudentResponse(Enrollment enrollment) {
        this.enrollmentId = enrollment.getId();
        this.userId = enrollment.getUserId();
        this.status = enrollment.getStatus();
        this.enrolledAt = enrollment.getCreatedAt();
        this.confirmedAt = enrollment.getConfirmedAt();
    }

}
