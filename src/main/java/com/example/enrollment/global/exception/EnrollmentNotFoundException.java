package com.example.enrollment.global.exception;

public class EnrollmentNotFoundException extends RuntimeException {
    public EnrollmentNotFoundException(Long enrollmentId) {
        super("수강 신청을 찾을 수 없습니다. id=" + enrollmentId);
    }
}