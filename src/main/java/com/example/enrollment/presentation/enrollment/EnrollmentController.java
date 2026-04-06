package com.example.enrollment.presentation.enrollment;

import com.example.enrollment.domain.enrollment.dto.EnrollmentCreateRequest;
import com.example.enrollment.domain.enrollment.dto.EnrollmentResponse;
import com.example.enrollment.domain.enrollment.service.EnrollmentService;
import com.example.enrollment.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping()
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll (
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid EnrollmentCreateRequest request
            ) {
        return ResponseEntity.ok(ApiResponse.ok(enrollmentService.enroll(request, userId)));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> confirm(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(enrollmentService.confirm(id, userId)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> cancel(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(enrollmentService.cancel(id, userId)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getMyEnrollments(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(enrollmentService.getMyEnrollments(userId)));
    }
}
