package com.example.enrollment.domain.enrollment.entity;

import com.example.enrollment.domain.course.entity.Course;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    private LocalDateTime confirmedAt;  // 결제 확정 시간

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Enrollment(Long userId, Course course) {
        this.userId = userId;
        this.course = course;
        this.status = EnrollmentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 결제 확정 (PENDING → CONFIRMED)
    public void confirm() {
        if (this.status != EnrollmentStatus.PENDING) {
            throw new IllegalStateException("결제 대기 상태에서만 확정할 수 있습니다.");
        }
        this.status = EnrollmentStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 수강 취소
    public void cancel() {
        if (this.status == EnrollmentStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 수강 신청입니다.");
        }

        // CONFIRMED 인 경우 7일 이내만 취소 가능
        if (this.status == EnrollmentStatus.CONFIRMED) {
            if (this.confirmedAt == null || this.confirmedAt.plusDays(7).isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("결제 확정 후 7일이 지나 취소할 수 없습니다.");
            }
        }

        this.status = EnrollmentStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isCancelled() {
        return this.status == EnrollmentStatus.CANCELLED;
    }
}
