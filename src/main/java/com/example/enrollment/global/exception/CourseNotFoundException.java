package com.example.enrollment.global.exception;

import org.springframework.http.HttpStatus;

public class CourseNotFoundException  extends BaseException {

    public CourseNotFoundException(Long courseId) {
        super("강의를 찾을 수 없습니다. id="+courseId, HttpStatus.NOT_FOUND);
    }

}
