package com.example.enrollment.domain.course;

import com.example.enrollment.domain.course.dto.CourseCreateRequest;
import com.example.enrollment.domain.course.dto.CourseResponse;
import com.example.enrollment.domain.course.dto.CourseStatusUpdateRequest;
import com.example.enrollment.domain.course.entity.Course;
import com.example.enrollment.domain.course.entity.CourseStatus;
import com.example.enrollment.domain.course.repository.CourseRepository;
import com.example.enrollment.domain.course.service.CourseService;
import com.example.enrollment.global.exception.CourseNotFoundException;
import com.example.enrollment.global.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @InjectMocks
    private CourseService courseService;

    @Mock
    private CourseRepository courseRepository;

    private Course course;

    @BeforeEach
    void setUp() {
        course = Course.builder()
                .creatorId(1L)
                .title("Spring Boot 입문")
                .description("스프링 부트 기초 강의")
                .price(50000)
                .capacity(30)
                .startDate(LocalDate.of(2025, 5, 1))
                .endDate(LocalDate.of(2025, 6, 1))
                .build();
    }

    @Test
    @DisplayName("강의 등록 성공")
    void 강의_등록_성공() {
        // given
        CourseCreateRequest request = new CourseCreateRequest(
                "Spring Boot 입문", "스프링 부트 기초 강의", 50000, 30,
                LocalDate.of(2025, 5, 1), LocalDate.of(2025, 6, 1)
        );
        given(courseRepository.save(any(Course.class))).willReturn(course);

        // when
        CourseResponse response = courseService.createCourse(request, 1L);

        // then
        assertThat(response.getTitle()).isEqualTo("Spring Boot 입문");
        assertThat(response.getStatus()).isEqualTo(CourseStatus.DRAFT);
        assertThat(response.getCurrentCount()).isEqualTo(0);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("강의 목록 전체 조회 성공")
    void 강의_목록_전체_조회() {
        // given
        given(courseRepository.findAll()).willReturn(List.of(course));

        // when
        List<CourseResponse> responses = courseService.getCourses(null);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("Spring Boot 입문");
    }

    @Test
    @DisplayName("강의 목록 상태 필터 조회 성공")
    void 강의_목록_상태_필터_조회() {
        // given
        given(courseRepository.findAllByStatus(CourseStatus.DRAFT)).willReturn(List.of(course));

        // when
        List<CourseResponse> responses = courseService.getCourses(CourseStatus.DRAFT);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo(CourseStatus.DRAFT);
    }

    @Test
    @DisplayName("강의 상세 조회 성공")
    void 강의_상세_조회_성공() {
        // given
        given(courseRepository.findById(1L)).willReturn(Optional.of(course));

        // when
        CourseResponse response = courseService.getCourse(1L);

        // then
        assertThat(response.getTitle()).isEqualTo("Spring Boot 입문");
        assertThat(response.getCapacity()).isEqualTo(30);
    }

    @Test
    @DisplayName("존재하지 않는 강의 조회 시 예외 발생")
    void 존재하지_않는_강의_조회_실패() {
        // given
        given(courseRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> courseService.getCourse(999L))
                .isInstanceOf(CourseNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("강의 상태 변경 성공")
    void 강의_상태_변경_성공() {
        // given
        given(courseRepository.findById(1L)).willReturn(Optional.of(course));
        CourseStatusUpdateRequest request = new CourseStatusUpdateRequest(CourseStatus.OPEN);

        // when
        CourseResponse response = courseService.updateStatus(1L, request, 1L);

        // then
        assertThat(response.getStatus()).isEqualTo(CourseStatus.OPEN);
    }

    @Test
    @DisplayName("본인 강의가 아닌 경우 상태 변경 실패")
    void 본인_강의_아닌_경우_상태_변경_실패() {
        // given
        given(courseRepository.findById(1L)).willReturn(Optional.of(course));
        CourseStatusUpdateRequest request = new CourseStatusUpdateRequest(CourseStatus.OPEN);

        // when & then
        assertThatThrownBy(() -> courseService.updateStatus(1L, request, 999L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("본인의 강의만 수정할 수 있습니다.");
    }
}
