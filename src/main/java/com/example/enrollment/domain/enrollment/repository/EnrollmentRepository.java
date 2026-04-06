package com.example.enrollment.domain.enrollment.repository;

import com.example.enrollment.domain.enrollment.entity.Enrollment;
import com.example.enrollment.domain.enrollment.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // 내 수강신청 목록 조회
    List<Enrollment> findAllByUserId(Long userId);

    // 중복 신청 체크 (CANCELLED X)
    boolean existsByUserIdAndCourseIdAndStatusNot(Long userId, Long courseId, EnrollmentStatus status);
}
