package com.example.enrollment.domain.course.service;

import com.example.enrollment.domain.course.dto.CourseCreateRequest;
import com.example.enrollment.domain.course.dto.CourseResponse;
import com.example.enrollment.domain.course.dto.CourseStatusUpdateRequest;
import com.example.enrollment.domain.course.entity.Course;
import com.example.enrollment.domain.course.entity.CourseStatus;
import com.example.enrollment.domain.course.repository.CourseRepository;
import com.example.enrollment.global.exception.CourseNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;

    @Transactional
    public CourseResponse createCourse(CourseCreateRequest request, Long creatorId) {
        Course course = Course.builder()
                .creatorId(creatorId)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .capacity(request.getCapacity())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        return new CourseResponse(courseRepository.save(course));
    }

    public List<CourseResponse> getCourses(CourseStatus status) {
        List<Course> courses = (status != null)
                ? courseRepository.findAllByStatus(status)
                : courseRepository.findAll();

        return courses.stream()
                .map(CourseResponse::new)
                .collect(Collectors.toList());
    }

    public CourseResponse getCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
        return new CourseResponse(course);
    }

    @Transactional
    public CourseResponse updateStatus(Long courseId, CourseStatusUpdateRequest request, Long creatorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        if (!course.getCreatorId().equals(creatorId)) {
            throw new IllegalArgumentException("본인의 강의만 수정할 수 있습니다.");
        }

        course.changeStatus(request.getStatus());
        return new CourseResponse(course);
    }
}
