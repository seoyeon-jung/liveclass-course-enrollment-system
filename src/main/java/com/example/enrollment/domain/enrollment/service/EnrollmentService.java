package com.example.enrollment.domain.enrollment.service;

import com.example.enrollment.domain.course.entity.Course;
import com.example.enrollment.domain.course.repository.CourseRepository;
import com.example.enrollment.domain.enrollment.dto.EnrollmentCreateRequest;
import com.example.enrollment.domain.enrollment.dto.EnrollmentResponse;
import com.example.enrollment.domain.enrollment.dto.EnrollmentStudentResponse;
import com.example.enrollment.domain.enrollment.entity.Enrollment;
import com.example.enrollment.domain.enrollment.entity.EnrollmentStatus;
import com.example.enrollment.domain.enrollment.repository.EnrollmentRepository;
import com.example.enrollment.global.exception.CourseNotFoundException;
import com.example.enrollment.global.exception.EnrollmentNotFoundException;
import com.example.enrollment.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    // 수강 신청
    @Transactional
    public EnrollmentResponse enroll(EnrollmentCreateRequest request, Long userId) {
        // 비관적 락으로 course 조회 (동시성 처리를 위해)
        Course course = courseRepository.findByIdWithLock(request.getCourseId())
                .orElseThrow(() -> new CourseNotFoundException(request.getCourseId()));

        // OPEN 강의만 신청 가능
        if (!course.isOpen()) {
            throw new IllegalStateException("모집 중인 강의만 신청할 수 있습니다.");
        }

        // 정원 초과 체크
        if (course.isFull()) {
            throw new IllegalStateException("정원이 초과되었습니다.");
        }

        // 중복 신청 체크 (CANCELLED 제외)
        boolean alreadyEnrolled = enrollmentRepository
                .existsByUserIdAndCourseIdAndStatusNot(userId, course.getId(), EnrollmentStatus.CANCELLED);
        if (alreadyEnrolled) {
            throw new IllegalStateException("이미 신청한 강의입니다.");
        }

        // 정원 증가
        course.increaseCount();

        // 수강 신청 생성
        Enrollment enrollment = Enrollment.builder()
                .userId(userId)
                .course(course)
                .build();

        return new EnrollmentResponse(enrollmentRepository.save(enrollment));
    }

    // 결제 확정 (PENDING > CONFIRMED)
    @Transactional
    public EnrollmentResponse confirm(Long enrollmentId, Long userId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        // 본인 신청한 것만 가능
        if (!enrollment.getUserId().equals(userId)) {
            throw new UnauthorizedException("본인의 수강 신청만 확정할 수 있습니다.");
        }

        enrollment.confirm();
        return new EnrollmentResponse(enrollment);
    }

    // 수강 취소
    @Transactional
    public EnrollmentResponse cancel(Long enrollmentId, Long userId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        // 본인 신청한 것만 가능
        if (!enrollment.getUserId().equals(userId)) {
            throw new UnauthorizedException("본인의 수강 신청만 취소할 수 있습니다.");
        }

        // PENDING or CONFIRMED 일 때만 정원 감소
        boolean shouldDecreaseCount = enrollment.getStatus() == EnrollmentStatus.PENDING
                || enrollment.getStatus() == EnrollmentStatus.CONFIRMED;

        enrollment.cancel(); // CANCELLED이면 예외 발생

        if (shouldDecreaseCount) {
            enrollment.getCourse().decreaseCount();
        }

        return new EnrollmentResponse(enrollment);
    }

    // 내 수강 신청 목록 조회
    public List<EnrollmentResponse> getMyEnrollments(Long userId) {
        return enrollmentRepository.findAllByUserId(userId)
                .stream()
                .map(EnrollmentResponse::new)
                .collect(Collectors.toList());
    }

    // 강의별 수강생 목록 조회
    public List<EnrollmentStudentResponse> getCourseEnrollments(Long courseId, Long creatorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        // 본인 강의만 조회 가능
        if (!course.getCreatorId().equals(creatorId)) {
            throw new UnauthorizedException("본인의 강의만 조회할 수 있습니다.");
        }

        return enrollmentRepository.findAllByCourseId(courseId)
                .stream()
                .map(EnrollmentStudentResponse::new)
                .collect(Collectors.toList());
    }
}
