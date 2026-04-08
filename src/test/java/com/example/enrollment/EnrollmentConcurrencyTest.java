package com.example.enrollment;

import com.example.enrollment.domain.course.entity.Course;
import com.example.enrollment.domain.course.entity.CourseStatus;
import com.example.enrollment.domain.course.repository.CourseRepository;
import com.example.enrollment.domain.enrollment.dto.EnrollmentCreateRequest;
import com.example.enrollment.domain.enrollment.repository.EnrollmentRepository;
import com.example.enrollment.domain.enrollment.service.EnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class EnrollmentConcurrencyTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private Course course;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();

        // 정원 5명 강의
        course = Course.builder()
                .creatorId(1L)
                .title("동시성 테스트용 강의")
                .description("Test description")
                .price(10000)
                .capacity(5)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .build();

        course.changeStatus(CourseStatus.OPEN);
        courseRepository.save(course);
    }

    @Test
    @DisplayName("정원 5명인 강의에 10명이 동시에 신청하면 5명 성공해야 한다")
    void 동시_수강신청_정원_초과_방지() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final long userId = i + 1L;
            executor.submit(() -> {
                try {
                    EnrollmentCreateRequest request = new EnrollmentCreateRequest(course.getId());
                    enrollmentService.enroll(request, userId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // DB에서 최신 상태 조회
        Course updatedCourse = courseRepository.findById(course.getId()).orElseThrow();

        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failCount.get());
        System.out.println("currentCount: " + updatedCourse.getCurrentCount());

        assertThat(successCount.get()).isEqualTo(5);
        assertThat(failCount.get()).isEqualTo(5);
        assertThat(updatedCourse.getCurrentCount()).isEqualTo(5);
    }
}
