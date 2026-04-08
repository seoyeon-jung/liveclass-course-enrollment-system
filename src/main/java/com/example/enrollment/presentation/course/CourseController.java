package com.example.enrollment.presentation.course;

import com.example.enrollment.domain.course.dto.CourseCreateRequest;
import com.example.enrollment.domain.course.dto.CourseResponse;
import com.example.enrollment.domain.course.dto.CourseStatusUpdateRequest;
import com.example.enrollment.domain.course.entity.CourseStatus;
import com.example.enrollment.domain.course.service.CourseService;
import com.example.enrollment.domain.enrollment.dto.EnrollmentStudentResponse;
import com.example.enrollment.domain.enrollment.service.EnrollmentService;
import com.example.enrollment.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    @PostMapping()
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid CourseCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.createCourse(request, userId)));
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCourses(
            @RequestParam(required = false)CourseStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.getCourses(status)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.getCourse(id)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CourseResponse>> updateStatus(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid CourseStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.updateStatus(id, request, userId)));
    }

    @GetMapping("/{id}/enrollments")
    public ResponseEntity<ApiResponse<List<EnrollmentStudentResponse>>> getCourseEnrollments(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(enrollmentService.getCourseEnrollments(id, userId)));
    }
}
