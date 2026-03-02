# 레시피 추적 API 백엔드 구현계획서

> **API 문서**: `docs/api/recipe-tracking-api.md`
>
> **작성일**: 2026-03-02
>
> **총 신규 파일**: 11개 + 기존 파일 수정 1개

---

## 구현 순서

```
Step 1: SurfaceType (Enum)           ← 의존성 없음
Step 2: Entity (2개)                  ← Step 1 필요
Step 3: Repository (2개)              ← Step 2 필요
Step 4: DTO (2개)                     ← Step 1 필요
Step 5: Exception (ErrorCode + Exception) ← 의존성 없음
Step 6: Error.java 등록              ← Step 5 필요
Step 7: Service                       ← Step 2, 3, 4, 5 필요
Step 8: Controller                    ← Step 4, 7 필요
Step 9: 빌드 검증
```

---

## Step 1: SurfaceType Enum

**파일**: `src/main/java/com/cheftory/api/tracking/entity/SurfaceType.java`

**참조 패턴**: `RankingSurfaceType.java` — 단순 enum, Javadoc 주석

```java
package com.cheftory.api.tracking.entity;

/**
 * 프론트엔드에서 레시피 카드가 노출되는 위치.
 *
 * <p>프론트엔드 SurfaceType과 1:1 매핑됩니다.</p>
 */
public enum SurfaceType {
    /** 홈 > 내 레시피 섹션 */
    HOME_MY_RECIPES,
    /** 홈 > 인기 레시피 섹션 */
    HOME_POPULAR_RECIPES,
    /** 홈 > 인기 숏츠 섹션 */
    HOME_POPULAR_SHORTS,
    /** /user/recipes 전체 목록 */
    USER_RECIPES,
    /** /popular-recipe 인기 레시피 */
    POPULAR_RECIPES,
    /** /search-recipe 트렌딩 */
    SEARCH_TRENDING,
    /** /search-results 검색 결과 */
    SEARCH_RESULTS,
    /** /recommend 카테고리 결과 */
    CATEGORY_RESULTS
}
```

---

## Step 2: Entity (2개)

### 2-1. RecipeImpression

**파일**: `src/main/java/com/cheftory/api/tracking/entity/RecipeImpression.java`

**참조 패턴**: `RankingImpression.java` (가장 유사), `RecipeReport.java`

- `extends MarketScope` → `market`, `countryCode` 자동 설정
- `@AllArgsConstructor(access = AccessLevel.PRIVATE)` + `@NoArgsConstructor`
- `UUID.randomUUID()` PK 사전 할당
- static `create(Clock, ...)` 팩토리
- `clock.now()` → `LocalDateTime` (프로젝트 커스텀 Clock)
- 프론트 timestamp → `LocalDateTime.ofInstant(Instant.ofEpochMilli(...), ZoneId.of("Asia/Seoul"))`

```java
package com.cheftory.api.tracking.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 레시피 노출(impression) 엔티티.
 *
 * <p>사용자 뷰포트에 레시피 카드가 노출된 기록을 저장합니다.</p>
 */
@Entity
@Table(name = "recipe_impression")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
public class RecipeImpression extends MarketScope {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID requestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SurfaceType surfaceType;

    @Column(nullable = false)
    private UUID recipeId;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private LocalDateTime impressedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 레시피 노출 기록을 생성합니다.
     *
     * @param clock 시간 제공자 (com.cheftory.api._common.Clock)
     * @param userId 사용자 ID
     * @param requestId 리스트 로드 식별자
     * @param surfaceType 노출 위치
     * @param recipeId 노출된 레시피 ID
     * @param position 리스트 내 순서 (0-based)
     * @param frontendTimestamp 프론트엔드 뷰포트 진입 시각 (Unix ms)
     * @return 레시피 노출 엔티티
     */
    public static RecipeImpression create(
            Clock clock,
            UUID userId,
            UUID requestId,
            SurfaceType surfaceType,
            UUID recipeId,
            int position,
            long frontendTimestamp) {
        return new RecipeImpression(
                UUID.randomUUID(),
                userId,
                requestId,
                surfaceType,
                recipeId,
                position,
                LocalDateTime.ofInstant(Instant.ofEpochMilli(frontendTimestamp), ZoneId.of("Asia/Seoul")),
                clock.now());
    }
}
```

### 2-2. RecipeClick

**파일**: `src/main/java/com/cheftory/api/tracking/entity/RecipeClick.java`

`RecipeImpression`과 동일 구조. 차이점: `impressedAt` → `clickedAt`

```java
package com.cheftory.api.tracking.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 레시피 클릭 엔티티.
 *
 * <p>사용자가 레시피 카드를 클릭(상세 이동)한 기록을 저장합니다.</p>
 */
@Entity
@Table(name = "recipe_click")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
public class RecipeClick extends MarketScope {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID requestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SurfaceType surfaceType;

    @Column(nullable = false)
    private UUID recipeId;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private LocalDateTime clickedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 레시피 클릭 기록을 생성합니다.
     *
     * @param clock 시간 제공자
     * @param userId 사용자 ID
     * @param requestId 리스트 로드 식별자
     * @param surfaceType 클릭 발생 위치
     * @param recipeId 클릭된 레시피 ID
     * @param position 리스트 내 순서 (0-based)
     * @param frontendTimestamp 프론트엔드 클릭 시각 (Unix ms)
     * @return 레시피 클릭 엔티티
     */
    public static RecipeClick create(
            Clock clock,
            UUID userId,
            UUID requestId,
            SurfaceType surfaceType,
            UUID recipeId,
            int position,
            long frontendTimestamp) {
        return new RecipeClick(
                UUID.randomUUID(),
                userId,
                requestId,
                surfaceType,
                recipeId,
                position,
                LocalDateTime.ofInstant(Instant.ofEpochMilli(frontendTimestamp), ZoneId.of("Asia/Seoul")),
                clock.now());
    }
}
```

---

## Step 3: Repository (2개)

**참조 패턴**: `RankingImpressionRepository.java` — 단순 `JpaRepository` 인터페이스

### 3-1. RecipeImpressionJpaRepository

**파일**: `src/main/java/com/cheftory/api/tracking/repository/RecipeImpressionJpaRepository.java`

```java
package com.cheftory.api.tracking.repository;

import com.cheftory.api.tracking.entity.RecipeImpression;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 레시피 노출 JPA 리포지토리.
 */
public interface RecipeImpressionJpaRepository extends JpaRepository<RecipeImpression, UUID> {}
```

### 3-2. RecipeClickJpaRepository

**파일**: `src/main/java/com/cheftory/api/tracking/repository/RecipeClickJpaRepository.java`

```java
package com.cheftory.api.tracking.repository;

import com.cheftory.api.tracking.entity.RecipeClick;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 레시피 클릭 JPA 리포지토리.
 */
public interface RecipeClickJpaRepository extends JpaRepository<RecipeClick, UUID> {}
```

---

## Step 4: DTO (2개)

**참조 패턴**: `RecipeReportRequest.java` — record + `@JsonProperty` + Bean Validation

> **중요**: 프로젝트에 글로벌 Jackson SNAKE_CASE 설정이 없음.
> 각 필드에 `@JsonProperty("snake_case")` 필수.

### 4-1. TrackingImpressionRequest

**파일**: `src/main/java/com/cheftory/api/tracking/dto/TrackingImpressionRequest.java`

```java
package com.cheftory.api.tracking.dto;

import com.cheftory.api.tracking.entity.SurfaceType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/**
 * 레시피 노출 배치 기록 요청 DTO
 *
 * @param requestId 리스트 로드 식별자
 * @param surfaceType 노출 위치
 * @param impressions 노출된 레시피 목록 (내부 요소 cascading validation)
 */
public record TrackingImpressionRequest(
        @JsonProperty("request_id") @NotNull UUID requestId,
        @JsonProperty("surface_type") @NotNull SurfaceType surfaceType,
        @JsonProperty("impressions") @NotNull @Size(min = 1) @Valid List<ImpressionItem> impressions) {

    /**
     * 개별 노출 항목
     *
     * @param recipeId 노출된 레시피 ID
     * @param position 리스트 내 순서 (0-based)
     * @param timestamp 뷰포트 진입 시각 (Unix ms)
     */
    public record ImpressionItem(
            @JsonProperty("recipe_id") @NotNull UUID recipeId,
            @JsonProperty("position") @NotNull Integer position,
            @JsonProperty("timestamp") @NotNull Long timestamp) {}
}
```

### 4-2. TrackingClickRequest

**파일**: `src/main/java/com/cheftory/api/tracking/dto/TrackingClickRequest.java`

```java
package com.cheftory.api.tracking.dto;

import com.cheftory.api.tracking.entity.SurfaceType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * 레시피 클릭 단건 기록 요청 DTO
 *
 * @param requestId 리스트 로드 식별자
 * @param surfaceType 클릭 발생 위치
 * @param recipeId 클릭된 레시피 ID
 * @param position 리스트 내 순서 (0-based)
 * @param timestamp 클릭 시각 (Unix ms)
 */
public record TrackingClickRequest(
        @JsonProperty("request_id") @NotNull UUID requestId,
        @JsonProperty("surface_type") @NotNull SurfaceType surfaceType,
        @JsonProperty("recipe_id") @NotNull UUID recipeId,
        @JsonProperty("position") @NotNull Integer position,
        @JsonProperty("timestamp") @NotNull Long timestamp) {}
```

---

## Step 5: Exception (2개)

**참조 패턴**: `RecipeReportErrorCode.java`, `RecipeReportException.java`

> **중요**: ErrorCode enum은 Lombok 미사용. 명시적 constructor + getter 패턴.

### 5-1. TrackingErrorCode

**파일**: `src/main/java/com/cheftory/api/tracking/exception/TrackingErrorCode.java`

```java
package com.cheftory.api.tracking.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;

/**
 * 레시피 추적 관련 에러 코드
 */
public enum TrackingErrorCode implements Error {
    INVALID_SURFACE_TYPE("TRACKING_001", "유효하지 않은 surface type입니다", ErrorType.VALIDATION),
    EMPTY_IMPRESSIONS("TRACKING_002", "노출 데이터가 비어있습니다", ErrorType.VALIDATION);

    private final String errorCode;
    private final String message;
    private final ErrorType type;

    TrackingErrorCode(String errorCode, String message, ErrorType type) {
        this.errorCode = errorCode;
        this.message = message;
        this.type = type;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public ErrorType getType() {
        return type;
    }
}
```

### 5-2. TrackingException

**파일**: `src/main/java/com/cheftory/api/tracking/exception/TrackingException.java`

```java
package com.cheftory.api.tracking.exception;

import com.cheftory.api.exception.CheftoryException;

/**
 * 레시피 추적 관련 예외
 */
public class TrackingException extends CheftoryException {

    public TrackingException(TrackingErrorCode errorCode) {
        super(errorCode);
    }

    public TrackingException(TrackingErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
```

---

## Step 6: Error.java 등록

**파일**: `src/main/java/com/cheftory/api/exception/Error.java` (기존 파일 수정)

**작업**: `ERROR_ENUMS` 리스트 마지막에 `TrackingErrorCode.class` 추가

**현재 코드** (line 40~66):
```java
List<Class<? extends Enum<?>>> ERROR_ENUMS = List.of(
        GlobalErrorCode.class,
        // ... 24개 기존 ErrorCode ...
        RecipeSearchErrorCode.class);
```

**수정 후**:
```java
List<Class<? extends Enum<?>>> ERROR_ENUMS = List.of(
        GlobalErrorCode.class,
        // ... 24개 기존 ErrorCode ...
        RecipeSearchErrorCode.class,
        TrackingErrorCode.class);
```

**추가할 import**:
```java
import com.cheftory.api.tracking.exception.TrackingErrorCode;
```

---

## Step 7: Service

**파일**: `src/main/java/com/cheftory/api/tracking/TrackingService.java`

**참조 패턴**: `RankingInteractionService.java` (배치 save + Clock), `RecipeReportService.java` (단건 save)

- `saveImpressions`: `@Transactional` + `saveAll()` (배치 INSERT, `batch_size=50` + `order_inserts=true` 적용)
- `saveClick`: 단건 `save()` (`@Transactional` 생략 — Spring Data JPA 내부 트랜잭션으로 충분)

```java
package com.cheftory.api.tracking;

import com.cheftory.api._common.Clock;
import com.cheftory.api.tracking.dto.TrackingClickRequest;
import com.cheftory.api.tracking.dto.TrackingImpressionRequest;
import com.cheftory.api.tracking.entity.RecipeClick;
import com.cheftory.api.tracking.entity.RecipeImpression;
import com.cheftory.api.tracking.repository.RecipeClickJpaRepository;
import com.cheftory.api.tracking.repository.RecipeImpressionJpaRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 레시피 추적 서비스.
 *
 * <p>프론트엔드에서 수집한 노출/클릭 데이터를 저장합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final RecipeImpressionJpaRepository impressionRepository;
    private final RecipeClickJpaRepository clickRepository;
    private final Clock clock;

    /**
     * 레시피 노출 배치 저장.
     *
     * @param userId 사용자 ID
     * @param request 노출 배치 요청
     */
    @Transactional
    public void saveImpressions(UUID userId, TrackingImpressionRequest request) {
        List<RecipeImpression> entities = request.impressions().stream()
                .map(item -> RecipeImpression.create(
                        clock,
                        userId,
                        request.requestId(),
                        request.surfaceType(),
                        item.recipeId(),
                        item.position(),
                        item.timestamp()))
                .toList();
        impressionRepository.saveAll(entities);
    }

    /**
     * 레시피 클릭 단건 저장.
     *
     * @param userId 사용자 ID
     * @param request 클릭 요청
     */
    public void saveClick(UUID userId, TrackingClickRequest request) {
        RecipeClick entity = RecipeClick.create(
                clock,
                userId,
                request.requestId(),
                request.surfaceType(),
                request.recipeId(),
                request.position(),
                request.timestamp());
        clickRepository.save(entity);
    }
}
```

---

## Step 8: Controller

**파일**: `src/main/java/com/cheftory/api/tracking/TrackingController.java`

**참조 패턴**: `RecipeReportController.java`

- `@RequestMapping` 클래스 레벨 (공통 경로 추출)
- `@UserPrincipal UUID userId` (커스텀 어노테이션, `com.cheftory.api._common.security.UserPrincipal`)
- `throws TrackingException` (checked exception 선언 필수)
- `SuccessOnlyResponse.create()` (`com.cheftory.api._common.reponse` — 패키지명 오타 주의)

```java
package com.cheftory.api.tracking;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.tracking.dto.TrackingClickRequest;
import com.cheftory.api.tracking.dto.TrackingImpressionRequest;
import com.cheftory.api.tracking.exception.TrackingException;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 레시피 추적 API 컨트롤러.
 *
 * <p>프론트엔드에서 수집한 노출/클릭 데이터를 수신합니다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tracking")
public class TrackingController {

    private final TrackingService trackingService;

    /**
     * 레시피 노출 배치 기록.
     *
     * @param request 노출 배치 요청
     * @param userId 사용자 ID
     * @return 성공 응답
     * @throws TrackingException 추적 관련 예외
     */
    @PostMapping("/impressions")
    public SuccessOnlyResponse trackImpressions(
            @RequestBody @Valid TrackingImpressionRequest request, @UserPrincipal UUID userId)
            throws TrackingException {
        trackingService.saveImpressions(userId, request);
        return SuccessOnlyResponse.create();
    }

    /**
     * 레시피 클릭 단건 기록.
     *
     * @param request 클릭 요청
     * @param userId 사용자 ID
     * @return 성공 응답
     * @throws TrackingException 추적 관련 예외
     */
    @PostMapping("/clicks")
    public SuccessOnlyResponse trackClick(
            @RequestBody @Valid TrackingClickRequest request, @UserPrincipal UUID userId)
            throws TrackingException {
        trackingService.saveClick(userId, request);
        return SuccessOnlyResponse.create();
    }
}
```

---

## Step 9: 빌드 검증

```bash
# 1. Spotless 포매터 적용
./gradlew spotlessApply

# 2. 컴파일 확인
./gradlew compileJava

# 3. 전체 빌드 (테스트 포함)
./gradlew build
```

---

## 최종 파일 목록

### 신규 생성 (11개)

| # | 파일 | Step |
|---|------|------|
| 1 | `tracking/entity/SurfaceType.java` | 1 |
| 2 | `tracking/entity/RecipeImpression.java` | 2 |
| 3 | `tracking/entity/RecipeClick.java` | 2 |
| 4 | `tracking/repository/RecipeImpressionJpaRepository.java` | 3 |
| 5 | `tracking/repository/RecipeClickJpaRepository.java` | 3 |
| 6 | `tracking/dto/TrackingImpressionRequest.java` | 4 |
| 7 | `tracking/dto/TrackingClickRequest.java` | 4 |
| 8 | `tracking/exception/TrackingErrorCode.java` | 5 |
| 9 | `tracking/exception/TrackingException.java` | 5 |
| 10 | `tracking/TrackingService.java` | 7 |
| 11 | `tracking/TrackingController.java` | 8 |

> 모든 경로의 기준: `src/main/java/com/cheftory/api/`

### 기존 수정 (1개)

| # | 파일 | Step | 변경 내용 |
|---|------|------|----------|
| 1 | `exception/Error.java` | 6 | `ERROR_ENUMS`에 `TrackingErrorCode.class` 추가 + import |

### 이미 반영 완료 (별도 커밋 필요)

| # | 파일 | 변경 내용 |
|---|------|----------|
| 1 | `src/main/resources/application-dev.yml` | `order_inserts: true` 추가 |
| 2 | `src/main/resources/application-prod.yml` | `order_inserts: true` 추가 |

---

## 컨벤션 체크리스트

| 항목 | 확인 |
|------|------|
| `Clock` → `com.cheftory.api._common.Clock` (NOT `java.time.Clock`) | ✓ |
| `SuccessOnlyResponse` → `com.cheftory.api._common.reponse` (오타 원본) | ✓ |
| `@UserPrincipal` → `com.cheftory.api._common.security.UserPrincipal` | ✓ |
| Entity: `extends MarketScope`, UUID PK 사전 할당, static `create()` | ✓ |
| DTO: `record` + `@JsonProperty("snake_case")` 각 필드 | ✓ |
| ErrorCode: Lombok 미사용, 명시적 constructor + getter | ✓ |
| Exception: `extends CheftoryException`, 1인자 + 2인자(cause) constructor | ✓ |
| `Error.java`: `ERROR_ENUMS`에 등록 (타입: `Enum<?>`) | ✓ |
| Controller: `@RequestMapping` 클래스 레벨, `throws` 선언 | ✓ |
| `@Transactional`: 배치(`saveImpressions`)에만 적용 | ✓ |
| Spotless: `./gradlew spotlessApply` 실행 | ✓ |
| SecurityConfig: `/api/v1/tracking/**` → `anyRequest().authenticated()` 자동 적용 | ✓ |
| `batch_size=50` + `order_inserts=true` (dev/prod 설정 완료) | ✓ |

---

## DDL (수동 실행 또는 Flyway)

> Hibernate `ddl-auto` 설정에 따라 자동 생성될 수 있으나, 인덱스는 수동 추가 필요.

```sql
CREATE TABLE recipe_impression (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    request_id BINARY(16) NOT NULL,
    surface_type VARCHAR(30) NOT NULL,
    recipe_id BINARY(16) NOT NULL,
    position INT NOT NULL,
    impressed_at DATETIME(3) NOT NULL,
    created_at DATETIME NOT NULL,
    market VARCHAR(20) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_impression_request (request_id),
    INDEX idx_impression_recipe_date (recipe_id, impressed_at),
    INDEX idx_impression_user_surface (user_id, surface_type, impressed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE recipe_click (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    request_id BINARY(16) NOT NULL,
    surface_type VARCHAR(30) NOT NULL,
    recipe_id BINARY(16) NOT NULL,
    position INT NOT NULL,
    clicked_at DATETIME(3) NOT NULL,
    created_at DATETIME NOT NULL,
    market VARCHAR(20) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_click_request (request_id),
    INDEX idx_click_recipe_date (recipe_id, clicked_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```
