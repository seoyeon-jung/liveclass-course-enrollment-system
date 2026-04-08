package com.example.enrollment.global.exception;

import org.springframework.http.HttpStatus;

public class EnrollmentNotFoundException extends BaseException {
    public EnrollmentNotFoundException(Long enrollmentId) {
        super("수강 신청을 찾을 수 없습니다. id=" + enrollmentId, HttpStatus.NOT_FOUND);
    }
}