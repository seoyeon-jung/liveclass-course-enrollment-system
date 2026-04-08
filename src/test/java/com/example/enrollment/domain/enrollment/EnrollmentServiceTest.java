package com.example.enrollment.domain.enrollment;

import com.example.enrollment.domain.course.entity.Course;
import com.example.enrollment.domain.course.entity.CourseStatus;
import com.example.enrollment.domain.course.repository.CourseRepository;
import com.example.enrollment.domain.enrollment.dto.EnrollmentCreateRequest;
import com.example.enrollment.domain.enrollment.dto.EnrollmentPageResponse;
import com.example.enrollment.domain.enrollment.dto.EnrollmentResponse;
import com.example.enrollment.domain.enrollment.dto.EnrollmentStudentResponse;
import com.example.enrollment.domain.enrollment.entity.Enrollment;
import com.example.enrollment.domain.enrollment.entity.EnrollmentStatus;
import com.example.enrollment.domain.enrollment.repository.EnrollmentRepository;
import com.example.enrollment.domain.enrollment.service.EnrollmentService;
import com.example.enrollment.global.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceTest {

    @InjectMocks
    private EnrollmentService enrollmentService;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    private Course openCourse;
    private Course draftCourse;
    private Course fullCourse;

    @BeforeEach
    void setUp() {
        openCourse = Course.builder()
                .creatorId(1L)
                .title("Spring Boot 입문")
                .description("테스트")
                .price(50000)
                .capacity(30)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .build();
        openCourse.changeStatus(CourseStatus.OPEN);

        draftCourse = Course.builder()
                .creatorId(1L)
                .title("DRAFT 강의")
                .description("테스트")
                .price(50000)
                .capacity(30)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .build();

        fullCourse = Course.builder()
                .creatorId(1L)
                .title("꽉 찬 강의")
                .description("테스트")
                .price(50000)
                .capacity(1)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .build();
        fullCourse.changeStatus(CourseStatus.OPEN);
        fullCourse.increaseCount(); // 정원 1명 꽉 참
    }

    @Test
    @DisplayName("수강 신청 성공")
    void 수강_신청_성공() {
        // given
        EnrollmentCreateRequest request = new EnrollmentCreateRequest(1L);
        given(courseRepository.findByIdWithLock(1L)).willReturn(Optional.of(openCourse));
        given(enrollmentRepository.existsByUserIdAndCourseIdAndStatusNot(
                2L, openCourse.getId(), EnrollmentStatus.CANCELLED
        )).willReturn(false);

        Enrollment enrollment = Enrollment.builder()
                .userId(2L)
                .course(openCourse)
                .build();
        given(enrollmentRepository.save(any(Enrollment.class))).willReturn(enrollment);

        // when
        EnrollmentResponse response = enrollmentService.enroll(request, 2L);

        // then
        assertThat(response.getUserId()).isEqualTo(2L);
        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
    }

    @Test
    @DisplayName("OPEN이 아닌 강의 신청 실패")
    void OPEN이_아닌_강의_신청_실패() {
        // given
        EnrollmentCreateRequest request = new EnrollmentCreateRequest(2L);
        given(courseRepository.findByIdWithLock(2L)).willReturn(Optional.of(draftCourse));

        // when, then
        assertThatThrownBy(() -> enrollmentService.enroll(request, 2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("모집 중인 강의만 신청할 수 있습니다.");
    }

    @Test
    @DisplayName("정원 초과 신청 실패")
    void 정원_초과_신청_실패() {
        // given
        EnrollmentCreateRequest request = new EnrollmentCreateRequest(3L);
        given(courseRepository.findByIdWithLock(3L)).willReturn(Optional.of(fullCourse));

        // when & then
        assertThatThrownBy(() -> enrollmentService.enroll(request, 2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("정원이 초과되었습니다.");
    }

    @Test
    @DisplayName("중복 신청 실패")
    void 중복_신청_실패() {
        // given
        EnrollmentCreateRequest request = new EnrollmentCreateRequest(1L);
        given(courseRepository.findByIdWithLock(1L)).willReturn(Optional.of(openCourse));
        given(enrollmentRepository.existsByUserIdAndCourseIdAndStatusNot(
                2L, openCourse.getId(), EnrollmentStatus.CANCELLED)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> enrollmentService.enroll(request, 2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 신청한 강의입니다.");
    }

    @Test
    @DisplayName("결제 확정 성공")
    void 결제_확정_성공() {
        // given
        Enrollment enrollment = Enrollment.builder()
                .userId(2L)
                .course(openCourse)
                .build();
        given(enrollmentRepository.findById(1L)).willReturn(Optional.of(enrollment));

        // when
        EnrollmentResponse response = enrollmentService.confirm(1L, 2L);

        // then
        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
    }

    @Test
    @DisplayName("본인 신청이 아닌 경우 결제 확정 실패")
    void 본인_신청_아닌_경우_결제_확정_실패() {
        // given
        Enrollment enrollment = Enrollment.builder()
                .userId(2L)
                .course(openCourse)
                .build();
        given(enrollmentRepository.findById(1L)).willReturn(Optional.of(enrollment));

        // when & then
        assertThatThrownBy(() -> enrollmentService.confirm(1L, 999L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("본인의 수강 신청만 확정할 수 있습니다.");
    }

    @Test
    @DisplayName("수강 취소 성공")
    void 수강_취소_성공() {
        // given
        openCourse.increaseCount();

        Enrollment enrollment = Enrollment.builder()
                .userId(2L)
                .course(openCourse)
                .build();
        given(enrollmentRepository.findById(1L)).willReturn(Optional.of(enrollment));

        // when
        EnrollmentResponse response = enrollmentService.cancel(1L, 2L);

        // then
        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
        assertThat(openCourse.getCurrentCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("본인 신청이 아닌 경우 수강 취소 실패")
    void 본인_신청_아닌_경우_수강_취소_실패() {
        // given
        Enrollment enrollment = Enrollment.builder()
                .userId(2L)
                .course(openCourse)
                .build();
        given(enrollmentRepository.findById(1L)).willReturn(Optional.of(enrollment));

        // when & then
        assertThatThrownBy(() -> enrollmentService.cancel(1L, 999L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("본인의 수강 신청만 취소할 수 있습니다.");
    }

    @Test
    @DisplayName("이미 취소된 수강 신청 재취소 실패")
    void 이미_취소된_수강_신청_재취소_실패() {
        // given
        Enrollment enrollment = Enrollment.builder()
                .userId(2L)
                .course(openCourse)
                .build();
        enrollment.cancel();
        given(enrollmentRepository.findById(1L)).willReturn(Optional.of(enrollment));

        // when & then
        assertThatThrownBy(() -> enrollmentService.cancel(1L, 2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 취소된 수강 신청입니다.");
    }

    @Test
    @DisplayName("내 수강 신청 목록 조회 성공 (페이지네이션)")
    void 내_수강_신청_목록_조회_성공() {
        // given
        Enrollment enrollment = Enrollment.builder()
                .userId(2L)
                .course(openCourse)
                .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Enrollment> enrollmentPage = new PageImpl<>(List.of(enrollment), pageable, 1);

        given(enrollmentRepository.findAllByUserId(2L, pageable)).willReturn(enrollmentPage);

        // when
        EnrollmentPageResponse response = enrollmentService.getMyEnrollments(2L, 0, 10);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.isLast()).isTrue();
    }

    @Test
    @DisplayName("결제 확정 후 7일 이내 취소 성공")
    void 결제_확정_후_7일_이내_취소_성공() {
        // given
        openCourse.increaseCount();
        Enrollment enrollment = Enrollment.builder()
                .userId(2L)
                .course(openCourse)
                .build();
        enrollment.confirm(); // CONFIRMED 상태로 변경 (confirmedAt = now)
        given(enrollmentRepository.findById(1L)).willReturn(Optional.of(enrollment));

        // when
        EnrollmentResponse response = enrollmentService.cancel(1L, 2L);

        // then
        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("결제 확정 후 7일 초과 시 취소 실패")
    void 결제_확정_후_7일_초과_취소_실패() {
        // given
        openCourse.increaseCount();
        Enrollment enrollment = Enrollment.builder()
                .userId(2L)
                .course(openCourse)
                .build();
        enrollment.confirm();

        // confirmedAt을 8일 전으로 강제 설정
        ReflectionTestUtils.setField(enrollment, "confirmedAt",
                LocalDateTime.now().minusDays(8));

        given(enrollmentRepository.findById(1L)).willReturn(Optional.of(enrollment));

        // when & then
        assertThatThrownBy(() -> enrollmentService.cancel(1L, 2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 확정 후 7일이 지나 취소할 수 없습니다.");
    }

    @Test
    @DisplayName("강의별 수강생 목록 조회 성공")
    void 강의별_수강생_목록_조회_성공() {
        // given
        Enrollment enrollment = Enrollment.builder()
                .userId(2L)
                .course(openCourse)
                .build();
        given(courseRepository.findById(1L)).willReturn(Optional.of(openCourse));
        given(enrollmentRepository.findAllByCourseId(1L)).willReturn(List.of(enrollment));

        // when
        List<EnrollmentStudentResponse> responses = enrollmentService.getCourseEnrollments(1L, 1L);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("본인 강의 아닌 경우 수강생 목록 조회 실패")
    void 본인_강의_아닌_경우_수강생_목록_조회_실패() {
        // given
        given(courseRepository.findById(1L)).willReturn(Optional.of(openCourse));

        // when & then
        assertThatThrownBy(() -> enrollmentService.getCourseEnrollments(1L, 999L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("본인의 강의만 조회할 수 있습니다.");
    }
}
