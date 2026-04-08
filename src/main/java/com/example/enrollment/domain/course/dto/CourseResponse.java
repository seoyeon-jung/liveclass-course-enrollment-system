package com.example.enrollment.domain.course.dto;

import com.example.enrollment.domain.course.entity.Course;
import com.example.enrollment.domain.course.entity.CourseStatus;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class CourseResponse {

    private final Long id;
    private final Long creatorId;
    private final String title;
    private final String description;
    private final int price;
    private final int capacity;
    private final int currentCount;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final CourseStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CourseResponse(Course course) {
        this.id = course.getId();
        this.creatorId = course.getCreatorId();
        this.title = course.getTitle();
        this.description = course.getDescription();
        this.price = course.getPrice();
        this.capacity = course.getCapacity();
        this.currentCount = course.getCurrentCount();
        this.startDate = course.getStartDate();
        this.endDate = course.getEndDate();
        this.status = course.getStatus();
        this.createdAt = course.getCreatedAt();
        this.updatedAt = course.getUpdatedAt();
    }
}
