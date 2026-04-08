package com.example.enrollment.domain.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CourseCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    private String description;

    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private int price;

    @Min(value = 1, message = "정원은 1명 이상이어야 합니다.")
    private int capacity;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다.")
    private LocalDate endDate;

    // 테스트용 생성자
    public CourseCreateRequest(String title, String description, int price, int capacity,
                               LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.capacity = capacity;
        this.startDate = startDate;
        this.endDate = endDate;
    }

}
