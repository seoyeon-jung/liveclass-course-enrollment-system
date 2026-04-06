package com.example.enrollment.global.exception;

public class CourseNotFoundException  extends RuntimeException {

    public CourseNotFoundException(Long courseId) {
        super("강의를 찾을 수 없습니다. id="+courseId);
    }

}
