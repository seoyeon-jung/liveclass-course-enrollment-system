package com.example.enrollment.domain.course.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long creatorId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int capacity;       // 최대 정원

    @Column(nullable = false)
    private int currentCount;   // 현재 신청 인원

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Course(Long creatorId, String title, String description,
                  int price, int capacity, LocalDate startDate, LocalDate endDate) {
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.capacity = capacity;
        this.currentCount = 0;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = CourseStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 상태 변경
    public void changeStatus(CourseStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    // 수강 신청 시 정원 증가
    public void increaseCount() {
        if (isFull()) {
            throw new IllegalStateException("정원이 초과되었습니다.");
        }
        this.currentCount++;
        this.updatedAt = LocalDateTime.now();
    }

    // 수강 취소 시 정원 감소
    public void decreaseCount() {
        if (this.currentCount <= 0) {
            throw new IllegalStateException("현재 신청 인원이 0명입니다.");
        }
        this.currentCount--;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOpen() {
        return this.status == CourseStatus.OPEN;
    }

    public boolean isFull() {
        return this.currentCount >= this.capacity;
    }
}
