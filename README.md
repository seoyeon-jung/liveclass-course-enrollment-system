# 수강 신청 시스템

## 프로젝트 개요

크리에이터(강사)가 강의를 개설하고, 클래스메이트(수강생)가 수강 신청을 할 수 있는 REST API 서버입니다.
정원 초과 방지를 위한 동시성 처리와 수강 신청 상태 관리를 핵심 기능으로 구현했습니다.

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| ORM | Spring Data JPA (Hibernate) |
| Database | MySQL 8.0 |
| Build | Gradle |
| Test | JUnit 5, Mockito |

---

## 실행 방법

### 사전 요구사항
- Java 17
- Docker Desktop

### 1. 레포지토리 클론
```bash
git clone https://github.com/{username}/{repo-name}.git
cd {repo-name}
```

### 2. 환경변수 설정
```bash
cp .env.example .env
```
`.env` 파일을 열어 값을 채워주세요:
```env
DB_ROOT_PASSWORD=password
DB_NAME=enrollment_db
DB_USER=enrollment_user
DB_PASSWORD=password
```

### 3. MySQL 실행
```bash
docker-compose up -d
```

### 4. 애플리케이션 실행
```bash
./gradlew bootRun
```
서버가 실행되면 `http://localhost:8080` 으로 접근할 수 있습니다.

---

## API 목록 및 예시

모든 API는 `X-User-Id` 헤더로 사용자를 식별합니다.

### 강의 API

<details>
<summary>POST /courses - 강의 등록</summary>

**Request**
```
POST /courses
X-User-Id: 1
Content-Type: application/json

{
  "title": "Spring Boot 입문",
  "description": "스프링 부트 기초 강의",
  "price": 50000,
  "capacity": 30,
  "startDate": "2025-05-01",
  "endDate": "2025-06-01"
}
```

**Response**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "creatorId": 1,
    "title": "Spring Boot 입문",
    "description": "스프링 부트 기초 강의",
    "price": 50000,
    "capacity": 30,
    "currentCount": 0,
    "startDate": "2025-05-01",
    "endDate": "2025-06-01",
    "status": "DRAFT",
    "createdAt": "2025-04-06T10:00:00",
    "updatedAt": "2025-04-06T10:00:00"
  }
}
```
</details>

<details>
<summary>GET /courses - 강의 목록 조회</summary>

**Request**
```
GET /courses
GET /courses?status=OPEN
```

**Response**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "creatorId": 1,
      "title": "Spring Boot 입문",
      "description": "스프링 부트 기초 강의",
      "price": 50000,
      "capacity": 30,
      "currentCount": 5,
      "startDate": "2025-05-01",
      "endDate": "2025-06-01",
      "status": "OPEN",
      "createdAt": "2025-04-06T10:00:00",
      "updatedAt": "2025-04-06T10:00:00"
    }
  ]
}
```
</details>

<details>
<summary>GET /courses/{id} - 강의 상세 조회</summary>

**Request**
```
GET /courses/1
```

**Response**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "creatorId": 1,
    "title": "Spring Boot 입문",
    "description": "스프링 부트 기초 강의",
    "price": 50000,
    "capacity": 30,
    "currentCount": 5,
    "startDate": "2025-05-01",
    "endDate": "2025-06-01",
    "status": "OPEN",
    "createdAt": "2025-04-06T10:00:00",
    "updatedAt": "2025-04-06T10:00:00"
  }
}
```
</details>

<details>
<summary>PATCH /courses/{id}/status - 강의 상태 변경</summary>

강의 상태 전이: `DRAFT` → `OPEN` → `CLOSED`

**Request**
```
PATCH /courses/1/status
X-User-Id: 1
Content-Type: application/json

{
  "status": "OPEN"
}
```

**Response**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "creatorId": 1,
    "title": "Spring Boot 입문",
    "status": "OPEN",
    "createdAt": "2025-04-06T10:00:00",
    "updatedAt": "2025-04-06T10:00:00"
  }
}
```
</details>

<details>
<summary>GET /courses/{id}/enrollments - 강의별 수강생 목록 조회 (크리에이터 전용)</summary>

**Request**
```
GET /courses/1/enrollments
X-User-Id: 1
```

**Response**
```json
{
  "success": true,
  "data": [
    {
      "enrollmentId": 1,
      "userId": 2,
      "status": "CONFIRMED",
      "enrolledAt": "2025-04-06T10:00:00",
      "confirmedAt": "2025-04-06T10:00:00"
    }
  ]
}
```
</details>

---

### 수강 신청 API

<details>
<summary>POST /enrollments - 수강 신청</summary>

**Request**
```
POST /enrollments
X-User-Id: 2
Content-Type: application/json

{
  "courseId": 1
}
```

**Response**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 2,
    "courseId": 1,
    "courseTitle": "Spring Boot 입문",
    "status": "PENDING",
    "confirmedAt": null,
    "createdAt": "2025-04-06T10:00:00",
    "updatedAt": "2025-04-06T10:00:00"
  }
}
```
</details>

<details>
<summary>PATCH /enrollments/{id}/confirm - 결제 확정</summary>

수강 신청 상태: `PENDING` → `CONFIRMED`

**Request**
```
PATCH /enrollments/1/confirm
X-User-Id: 2
```

**Response**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 2,
    "courseId": 1,
    "courseTitle": "Spring Boot 입문",
    "status": "CONFIRMED",
    "confirmedAt": "2025-04-06T10:00:00",
    "createdAt": "2025-04-06T10:00:00",
    "updatedAt": "2025-04-06T10:00:00"
  }
}
```
</details>

<details>
<summary>PATCH /enrollments/{id}/cancel - 수강 취소</summary>

- `PENDING` 상태는 기간 제한 없이 취소 가능
- `CONFIRMED` 상태는 결제 확정 후 **7일 이내**만 취소 가능

**Request**
```
PATCH /enrollments/1/cancel
X-User-Id: 2
```

**Response**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 2,
    "courseId": 1,
    "courseTitle": "Spring Boot 입문",
    "status": "CANCELLED",
    "confirmedAt": null,
    "createdAt": "2025-04-06T10:00:00",
    "updatedAt": "2025-04-06T10:00:00"
  }
}
```
</details>

<details>
<summary>GET /enrollments/me - 내 수강 신청 목록 조회</summary>

**Request**
```
GET /enrollments/me
GET /enrollments/me?page=0&size=10
X-User-Id: 2
```

| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| page | int | 0 | 페이지 번호 (0부터 시작) |
| size | int | 10 | 페이지 당 항목 수 |

**Response**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "userId": 2,
        "courseId": 1,
        "courseTitle": "Spring Boot 입문",
        "status": "CONFIRMED",
        "confirmedAt": "2025-04-06T10:00:00",
        "createdAt": "2025-04-06T10:00:00",
        "updatedAt": "2025-04-06T10:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```
</details>

---

### 에러 응답

<details>
<summary>에러 응답 형식 및 HTTP Status</summary>

```json
{
  "success": false,
  "message": "에러 메시지"
}
```

| 상황 | HTTP Status |
|------|-------------|
| 강의/신청 없음 | 404 Not Found |
| 본인 아닌 접근 | 403 Forbidden |
| 정원 초과, 상태 오류 | 400 Bad Request |
| 입력값 오류 | 400 Bad Request |
| 헤더 누락 | 400 Bad Request |
| 서버 오류 | 500 Internal Server Error |

</details>

---

## 데이터 모델 설명

<details>
<summary>ERD 및 상태 전이</summary>

```
courses
├── id            BIGINT PK
├── creator_id    BIGINT        (강사 userId)
├── title         VARCHAR
├── description   TEXT
├── price         INT
├── capacity      INT           (최대 정원)
├── current_count INT           (현재 신청 인원)
├── start_date    DATE
├── end_date      DATE
├── status        VARCHAR       (DRAFT / OPEN / CLOSED)
├── created_at    DATETIME
└── updated_at    DATETIME

enrollments
├── id            BIGINT PK
├── user_id       BIGINT
├── course_id     BIGINT FK → courses.id
├── status        VARCHAR       (PENDING / CONFIRMED / CANCELLED)
├── confirmed_at  DATETIME
├── created_at    DATETIME
└── updated_at    DATETIME
```

**강의 상태 전이**
```
DRAFT → OPEN → CLOSED
```

**수강 신청 상태 전이**
```
PENDING → CONFIRMED
        → CANCELLED
```
</details>

---

## 요구사항 해석 및 가정

- 회원 도메인은 구현하지 않고 `X-User-Id` 헤더로 사용자를 식별합니다.
- 강의 상태 변경은 본인(creatorId)만 가능합니다.
- 수강 취소는 `PENDING`, `CONFIRMED` 모두 가능하며 취소 시 정원이 감소합니다.
- `CONFIRMED` 상태의 취소는 결제 확정 후 7일 이내에만 가능합니다.
- 취소된 강의는 재신청이 가능합니다. (중복 신청 체크 시 `CANCELLED` 제외)
- 결제 시스템 연동 없이 상태 변경으로 결제 확정을 대체합니다.

---

## 설계 결정과 이유

<details>
<summary>비관적 락 (Pessimistic Lock) 선택</summary>

수강 신청 시 동시에 여러 사람이 마지막 자리에 신청하는 상황을 처리하기 위해 비관적 락을 적용했습니다.
낙관적 락은 충돌 시 재시도 로직이 필요해 복잡도가 높아지는 반면,
비관적 락은 트랜잭션 진입 시점에 락을 걸어 정확한 정원 제어가 가능합니다.
</details>

<details>
<summary>currentCount 컬럼 직접 관리</summary>

현재 신청 인원을 매번 서브쿼리로 조회하지 않고 `current_count` 컬럼으로 직접 관리합니다.
비관적 락과 함께 사용하여 동시성 상황에서도 정합성을 유지합니다.
</details>

<details>
<summary>도메인 로직을 엔티티 내부에서 처리</summary>

정원 증가/감소(`increaseCount`, `decreaseCount`), 상태 전이(`confirm`, `cancel`) 등
비즈니스 규칙을 엔티티 메서드로 구현하여 Service 레이어가 비대해지는 것을 방지했습니다.
</details>

<details>
<summary>X-User-Id 헤더 인증</summary>

실무에서 JWT 기반 인증으로 교체를 가정하여 헤더 방식을 선택했습니다.
쿼리 파라미터 방식은 URL에 사용자 정보가 노출되어 적합하지 않다고 판단했습니다.
</details>

---

## 테스트 실행 방법

### 단위 테스트 (DB 불필요)
```bash
./gradlew test --tests "com.example.enrollment.domain.course.service.CourseServiceTest"
./gradlew test --tests "com.example.enrollment.domain.enrollment.service.EnrollmentServiceTest"
```

### 동시성 테스트 (Docker MySQL 필요)
```bash
docker-compose up -d
./gradlew test --tests "com.example.enrollment.EnrollmentConcurrencyTest"
```

### 전체 테스트
```bash
docker-compose up -d
./gradlew test
```

---

## 미구현 / 제약사항

- 대기열(waitlist) 기능 미구현
- 실제 인증/인가 미구현 (`X-User-Id` 헤더로 대체)
- 실제 결제 시스템 연동 미구현 (상태 변경으로 대체)

---

## AI 활용 범위

- 동시성 처리 방식 (비관적 락 vs 낙관적 락) 비교 검토
- 테스트 코드 초안 생성
- 발생한 버그 (취소 시 상태 검증 순서, Jackson 설정 오류 등) 원인 분석