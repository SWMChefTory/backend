# 레시피 추적 API 문서

> **목적**: 프론트엔드에서 수집한 레시피 카드의 노출(Impression)과 클릭(Click) 데이터를 저장하여 CTR 분석 기반을 구축합니다.
>
> **프론트엔드 기획서**: `webview-v2/docs/recipe-tracking-plan.md`
>
> **작성일**: 2026-03-02

---

## 1. API 목록

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 1 | POST | `/api/v1/tracking/impressions` | 레시피 노출 배치 기록 | 필요 |
| 2 | POST | `/api/v1/tracking/clicks` | 레시피 클릭 단건 기록 | 필요 |

---

## 2. 공통 사항

### 인증

모든 요청에 `Authorization: Bearer {token}` 헤더 필수. `@UserPrincipal UUID userId`로 사용자 식별.

### JSON 필드 네이밍

프론트엔드 axios 인터셉터가 `camelCase → snake_case` 자동 변환을 수행하므로, 서버는 **snake_case** 필드명으로 수신합니다.

### 응답 규격

- 성공: `200 OK` + `SuccessOnlyResponse` (`{ "message": "success" }`)
- 프론트엔드는 응답 본문을 사용하지 않으므로 `2xx`만 반환하면 충분
- 에러: 표준 `ErrorResponse` (`{ "message": "...", "errorCode": "..." }`)

### SurfaceType Enum

프론트엔드에서 전송하는 `surface_type` 값 (8종):

| 값 | 발생 위치 | 설명 |
|---|---|---|
| `HOME_MY_RECIPES` | 홈 > 내 레시피 섹션 | 가로 스크롤 (모바일/태블릿) 또는 grid (데스크톱) |
| `HOME_POPULAR_RECIPES` | 홈 > 인기 레시피 섹션 | 가로 스크롤 또는 grid |
| `HOME_POPULAR_SHORTS` | 홈 > 인기 숏츠 섹션 | 가로 스크롤 |
| `USER_RECIPES` | `/user/recipes` 전체 목록 | 세로 리스트 + 무한스크롤 |
| `POPULAR_RECIPES` | `/popular-recipe` 인기 레시피 | grid + 무한스크롤 |
| `SEARCH_TRENDING` | `/search-recipe` 트렌딩 | grid + 무한스크롤 |
| `SEARCH_RESULTS` | `/search-results` 검색 결과 | Shorts(가로) + Normal(세로) |
| `CATEGORY_RESULTS` | `/recommend` 카테고리 결과 | Shorts(가로) + Normal(세로) 또는 grid |

---

## 3. API 1: 노출 배치 기록

### 3-1. 개요

| 항목 | 내용 |
|------|------|
| **Endpoint** | `POST /api/v1/tracking/impressions` |
| **설명** | 사용자 뷰포트에 노출된 레시피 카드를 배치로 기록합니다 |
| **인증** | 필요 (UserPrincipal) |
| **특성** | Fire-and-forget (프론트는 응답 대기 안 함) |

### 3-2. Request Body

```json
{
  "request_id": "550e8400-e29b-41d4-a716-446655440000",
  "surface_type": "POPULAR_RECIPES",
  "impressions": [
    {
      "recipe_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "position": 0,
      "timestamp": 1740912345678
    },
    {
      "recipe_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "position": 1,
      "timestamp": 1740912345700
    },
    {
      "recipe_id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
      "position": 2,
      "timestamp": 1740912345750
    }
  ]
}
```

| 필드 | 타입 | 필수 | 제약 | 설명 |
|------|------|------|------|------|
| `request_id` | String (UUID) | O | UUID v4 형식 | 리스트 로드 단위 식별자. 프론트에서 `crypto.randomUUID()`로 생성 |
| `surface_type` | String (Enum) | O | 8종 중 하나 | 레시피 카드가 노출된 위치 |
| `impressions` | Array | O | 최소 1건 | 노출된 레시피 목록 |
| `impressions[].recipe_id` | String (UUID) | O | - | 노출된 레시피 ID |
| `impressions[].position` | Integer | O | 0 이상 | 리스트 내 순서 (0-based index) |
| `impressions[].timestamp` | Long | O | Unix ms (13자리) | 뷰포트 진입 시각 (`Date.now()`) |

### 3-3. 성공 응답 (200 OK)

```json
{
  "message": "success"
}
```

### 3-4. 에러 응답

| errorCode | message | HTTP Status | 발생 조건 |
|-----------|---------|-------------|----------|
| `TRACKING_001` | 유효하지 않은 surface type입니다 | 400 | `surface_type` 값이 Enum에 없음 |
| `TRACKING_002` | 노출 데이터가 비어있습니다 | 400 | `impressions` 배열이 비어있음 |

> **참고**: 프론트엔드가 fire-and-forget 방식이므로 에러 응답을 활용하지 않지만, API 정합성을 위해 유효성 검증은 수행합니다.

### 3-5. 호출 패턴

프론트엔드에서 이 API를 호출하는 3가지 시점:

| 시점 | 트리거 | 특성 |
|------|--------|------|
| **일반 전송** | 1초 debounce 후 | 스크롤 중 뷰포트 진입한 카드를 모아서 배치 전송 |
| **탭 이탈 flush** | `visibilitychange` → `hidden` | 사용자가 다른 탭으로 이동할 때 버퍼 잔여분 즉시 전송 |
| **세션 전환 flush** | `requestId` 변경 시 | 카테고리 전환, 검색어 변경 등으로 새 세션 시작 시 이전 버퍼 flush |

**동일 `request_id` 내 중복**:
- 프론트에서 `Set<recipeId>`로 중복 방지하므로 같은 `request_id + recipe_id` 조합은 1회만 전송
- 단, edge case (탭 전환 등)로 중복 도착할 수 있으므로 서버 측 idempotency 권장

---

## 4. API 2: 클릭 단건 기록

### 4-1. 개요

| 항목 | 내용 |
|------|------|
| **Endpoint** | `POST /api/v1/tracking/clicks` |
| **설명** | 레시피 카드 클릭(상세 페이지 이동)을 기록합니다 |
| **인증** | 필요 (UserPrincipal) |
| **특성** | Fire-and-forget, 즉시 전송 |

### 4-2. Request Body

```json
{
  "request_id": "550e8400-e29b-41d4-a716-446655440000",
  "surface_type": "SEARCH_RESULTS",
  "recipe_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "position": 3,
  "timestamp": 1740912400000
}
```

| 필드 | 타입 | 필수 | 제약 | 설명 |
|------|------|------|------|------|
| `request_id` | String (UUID) | O | UUID v4 형식 | 해당 노출의 requestId와 동일한 값 |
| `surface_type` | String (Enum) | O | 8종 중 하나 | 클릭이 발생한 위치 |
| `recipe_id` | String (UUID) | O | - | 클릭된 레시피 ID |
| `position` | Integer | O | 0 이상 | 클릭된 카드의 리스트 내 순서 (0-based) |
| `timestamp` | Long | O | Unix ms (13자리) | 클릭 발생 시각 (`Date.now()`) |

### 4-3. 성공 응답 (200 OK)

```json
{
  "message": "success"
}
```

### 4-4. 에러 응답

| errorCode | message | HTTP Status | 발생 조건 |
|-----------|---------|-------------|----------|
| `TRACKING_001` | 유효하지 않은 surface type입니다 | 400 | `surface_type` 값이 Enum에 없음 |

### 4-5. 호출 패턴

- 사용자가 레시피 카드를 클릭할 때 **즉시 1건** 전송 (debounce 없음)
- `RecipeStatus.SUCCESS` 상태의 레시피만 클릭 추적 (미완성 레시피는 상세 이동 불가)
- Long press (카테고리 변경)는 클릭으로 추적하지 않음

**Impression 보충 메커니즘**:
- 카드가 뷰포트에 50% 미만 노출된 상태에서 클릭 발생 가능
- 이 경우 프론트에서 Impression 보충 전송 후 Click 전송
- 따라서 **Click보다 Impression이 먼저 도착**하는 것이 일반적이나, 네트워크 지연으로 **Click이 먼저 도착할 수 있음**
- 서버는 Click 처리 시 Impression 미존재를 허용해야 함

---

## 5. 요청 예시

### cURL — Impression 배치

```bash
curl -X POST "https://api.cheftory.com/api/v1/tracking/impressions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access_token>" \
  -d '{
    "request_id": "550e8400-e29b-41d4-a716-446655440000",
    "surface_type": "HOME_POPULAR_RECIPES",
    "impressions": [
      { "recipe_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "position": 0, "timestamp": 1740912345678 },
      { "recipe_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901", "position": 1, "timestamp": 1740912345700 }
    ]
  }'
```

### cURL — Click 단건

```bash
curl -X POST "https://api.cheftory.com/api/v1/tracking/clicks" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access_token>" \
  -d '{
    "request_id": "550e8400-e29b-41d4-a716-446655440000",
    "surface_type": "HOME_POPULAR_RECIPES",
    "recipe_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "position": 0,
    "timestamp": 1740912400000
  }'
```

---

## 6. requestId 생명주기

`request_id`는 **같은 맥락의 리스트를 식별하는 세션 단위 키**입니다. Impression과 Click을 연결하는 핵심입니다.

| 상황 | requestId | 이유 |
|------|-----------|------|
| 페이지 최초 진입 | 새로 생성 | 새 리스트 로드 |
| 무한스크롤 다음 페이지 | **유지** | 같은 리스트의 연장 |
| 카테고리 탭 전환 | 새로 생성 | 다른 리스트 |
| 검색어 변경 | 새로 생성 | 다른 리스트 |
| 페이지 이탈 후 재진입 | 새로 생성 | 새 세션 |

**홈 화면 특수 사항**: 홈 화면 1개 페이지에 3개 Surface가 공존하며, 각각 독립적인 `request_id`를 가짐.

---

## 7. 데이터 모델

### 7-1. `recipe_impression` 테이블

| 컬럼 | 타입 | Nullable | 설명 |
|------|------|----------|------|
| `id` | BINARY(16) / UUID | NO | PK, `UUID.randomUUID()` |
| `user_id` | BINARY(16) / UUID | NO | 사용자 ID (`@UserPrincipal`) |
| `request_id` | BINARY(16) / UUID | NO | 리스트 로드 식별자 |
| `surface_type` | VARCHAR(30) | NO | Enum: SurfaceType |
| `recipe_id` | BINARY(16) / UUID | NO | 노출된 레시피 ID |
| `position` | INT | NO | 리스트 내 순서 (0-based) |
| `impressed_at` | DATETIME(3) | NO | 뷰포트 진입 시각 (프론트 timestamp → LocalDateTime 변환) |
| `created_at` | DATETIME | NO | DB 저장 시각 (`clock.now()`) |
| `market` | VARCHAR(20) | NO | MarketScope (자동) |
| `country_code` | VARCHAR(2) | NO | MarketScope (자동) |

**인덱스**:

| 인덱스명 | 컬럼 | 용도 |
|----------|------|------|
| `idx_impression_request` | `request_id` | requestId로 세션별 조회 |
| `idx_impression_recipe_date` | `recipe_id, impressed_at` | 레시피별 노출 추이 분석 |
| `idx_impression_user_surface` | `user_id, surface_type, impressed_at` | 사용자별 Surface 분석 |

**Unique Constraint** (선택사항):

```sql
UNIQUE KEY uq_impression_request_recipe (request_id, recipe_id)
```

- 프론트에서 중복 방지하지만, edge case 대비 서버에서도 idempotency 보장
- `INSERT IGNORE` 또는 `ON DUPLICATE KEY UPDATE` 패턴 사용 시 적용
- 대안: unique constraint 없이 전수 저장 → 집계 시 `DISTINCT` 처리

### 7-2. `recipe_click` 테이블

| 컬럼 | 타입 | Nullable | 설명 |
|------|------|----------|------|
| `id` | BINARY(16) / UUID | NO | PK, `UUID.randomUUID()` |
| `user_id` | BINARY(16) / UUID | NO | 사용자 ID |
| `request_id` | BINARY(16) / UUID | NO | 리스트 로드 식별자 |
| `surface_type` | VARCHAR(30) | NO | Enum: SurfaceType |
| `recipe_id` | BINARY(16) / UUID | NO | 클릭된 레시피 ID |
| `position` | INT | NO | 리스트 내 순서 (0-based) |
| `clicked_at` | DATETIME(3) | NO | 클릭 시각 (프론트 timestamp → LocalDateTime 변환) |
| `created_at` | DATETIME | NO | DB 저장 시각 (`clock.now()`) |
| `market` | VARCHAR(20) | NO | MarketScope (자동) |
| `country_code` | VARCHAR(2) | NO | MarketScope (자동) |

**인덱스**:

| 인덱스명 | 컬럼 | 용도 |
|----------|------|------|
| `idx_click_request` | `request_id` | requestId로 세션별 조회 |
| `idx_click_recipe_date` | `recipe_id, clicked_at` | 레시피별 클릭 추이 분석 |

---

## 8. 구현 가이드 (프로젝트 컨벤션 기반)

> **코드 스타일**: 프로젝트는 Spotless + `palantirJavaFormat('2.87.0')`을 사용합니다.
> 커밋 전 반드시 `./gradlew spotlessApply`를 실행하세요.

### 8-1. 패키지 구조

```
src/main/java/com/cheftory/api/tracking/
├── TrackingController.java
├── TrackingService.java
├── dto/
│   ├── TrackingImpressionRequest.java
│   └── TrackingClickRequest.java
├── entity/
│   ├── RecipeImpression.java
│   ├── RecipeClick.java
│   └── SurfaceType.java              // Enum
├── exception/
│   ├── TrackingErrorCode.java
│   └── TrackingException.java
└── repository/
    ├── RecipeImpressionJpaRepository.java
    └── RecipeClickJpaRepository.java
```

### 8-2. Controller

> **컨벤션 참고**: `RecipeReportController`, `RecipeBookmarkController` 패턴.
> `CheftoryException`은 **checked exception** (`extends Exception`)이므로 `throws` 선언 필수.

```java
import com.cheftory.api._common.reponse.SuccessOnlyResponse;  // 주의: reponse (오타 아님, 프로젝트 원본)

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tracking")
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/impressions")
    public SuccessOnlyResponse trackImpressions(
            @RequestBody @Valid TrackingImpressionRequest request,
            @UserPrincipal UUID userId) throws TrackingException {
        trackingService.saveImpressions(userId, request);
        return SuccessOnlyResponse.create();
    }

    @PostMapping("/clicks")
    public SuccessOnlyResponse trackClick(
            @RequestBody @Valid TrackingClickRequest request,
            @UserPrincipal UUID userId) throws TrackingException {
        trackingService.saveClick(userId, request);
        return SuccessOnlyResponse.create();
    }
}
```

> **보안 설정**: `/api/v1/tracking/**`는 `SecurityConfig`의 `.anyRequest().authenticated()` 규칙에 의해
> 자동으로 인증 필수 경로가 됩니다. 별도 설정 변경 불필요.

### 8-3. DTO (Request)

```java
public record TrackingImpressionRequest(
        @JsonProperty("request_id") @NotNull UUID requestId,
        @JsonProperty("surface_type") @NotNull SurfaceType surfaceType,
        @JsonProperty("impressions") @NotNull @Size(min = 1) @Valid List<ImpressionItem> impressions) {

    public record ImpressionItem(
            @JsonProperty("recipe_id") @NotNull UUID recipeId,
            @JsonProperty("position") @NotNull Integer position,
            @JsonProperty("timestamp") @NotNull Long timestamp) {}
}
```

```java
public record TrackingClickRequest(
        @JsonProperty("request_id") @NotNull UUID requestId,
        @JsonProperty("surface_type") @NotNull SurfaceType surfaceType,
        @JsonProperty("recipe_id") @NotNull UUID recipeId,
        @JsonProperty("position") @NotNull Integer position,
        @JsonProperty("timestamp") @NotNull Long timestamp) {}
```

> **`@JsonProperty` 필수**: 프로젝트에 글로벌 Jackson `SNAKE_CASE` 설정이 없으므로,
> 각 필드에 `@JsonProperty("snake_case")`를 명시해야 합니다.
> (참고: `RecipeCreateRequest`, `RecipeBookmarkRequest` 등 기존 DTO 동일 패턴)

### 8-4. Entity

> **`Clock` 주의**: 프로젝트 커스텀 클래스 `com.cheftory.api._common.Clock` 사용.
> `java.time.Clock`이 아닙니다. `clock.now()`는 `LocalDateTime`을 반환합니다.
>
> **`LocalDateTime` 사용**: 프론트 timestamp(Unix ms)는 `LocalDateTime`으로 변환하여 저장합니다.
> 코드베이스 전체가 `LocalDateTime`을 사용하며, `Instant` 사용 선례가 없습니다.

```java
import com.cheftory.api._common.Clock;  // java.time.Clock 아님!

@Entity
@Table(name = "recipe_impression")
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    public static RecipeImpression create(
            Clock clock, UUID userId, UUID requestId,
            SurfaceType surfaceType, UUID recipeId,
            int position, long frontendTimestamp) {
        return new RecipeImpression(
                UUID.randomUUID(), userId, requestId, surfaceType,
                recipeId, position,
                LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(frontendTimestamp),
                        ZoneId.of("Asia/Seoul")),
                clock.now());
    }
}
```

`RecipeClick` 엔티티도 동일한 구조 (필드명: `clickedAt`).

> **참고**: `RankingImpression` 엔티티가 가장 유사한 기존 참조 모델입니다.
> `MarketScope`의 `market`/`countryCode` 필드는 `@TenantId`와 `@PrePersist`로 자동 설정됩니다.

### 8-5. Enum

```java
public enum SurfaceType {
    HOME_MY_RECIPES,
    HOME_POPULAR_RECIPES,
    HOME_POPULAR_SHORTS,
    USER_RECIPES,
    POPULAR_RECIPES,
    SEARCH_TRENDING,
    SEARCH_RESULTS,
    CATEGORY_RESULTS
}
```

### 8-6. Service

```java
import com.cheftory.api._common.Clock;  // 프로젝트 커스텀 Clock

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final RecipeImpressionJpaRepository impressionRepository;
    private final RecipeClickJpaRepository clickRepository;
    private final Clock clock;

    @Transactional
    public void saveImpressions(UUID userId, TrackingImpressionRequest request) {
        List<RecipeImpression> entities = request.impressions().stream()
                .map(item -> RecipeImpression.create(
                        clock, userId, request.requestId(), request.surfaceType(),
                        item.recipeId(), item.position(), item.timestamp()))
                .toList();
        impressionRepository.saveAll(entities);
    }

    public void saveClick(UUID userId, TrackingClickRequest request) {
        RecipeClick entity = RecipeClick.create(
                clock, userId, request.requestId(), request.surfaceType(),
                request.recipeId(), request.position(), request.timestamp());
        clickRepository.save(entity);
    }
}
```

> **`hibernate.jdbc.batch_size=50`**: `application-dev.yml`에 이미 설정되어 있으므로
> `saveAll()` 호출 시 배치 INSERT가 자동 적용됩니다.

### 8-7. Repository

```java
public interface RecipeImpressionJpaRepository extends JpaRepository<RecipeImpression, UUID> {}
```

```java
public interface RecipeClickJpaRepository extends JpaRepository<RecipeClick, UUID> {}
```

> 초기에는 단순 저장만 필요. 집계 쿼리는 추후 필요 시 추가.
> 향후 idempotency(중복 방지) 처리가 필요하면 도메인 인터페이스 + Impl 패턴으로 전환.
> (참고: `RecipeReportRepository` + `RecipeReportRepositoryImpl` 패턴)

### 8-8. Exception

> **컨벤션 주의**: ErrorCode enum은 Lombok(`@Getter`, `@RequiredArgsConstructor`) 미사용.
> 프로젝트 전체 (`GlobalErrorCode`, `RecipeReportErrorCode` 등)가 **명시적 constructor + getter** 패턴입니다.

```java
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

```java
public class TrackingException extends CheftoryException {
    public TrackingException(TrackingErrorCode errorCode) {
        super(errorCode);
    }

    public TrackingException(TrackingErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
```

### 8-9. Error.java 등록 (필수!)

> **반드시 수행**: `TrackingErrorCode`를 `Error` 인터페이스의 `ERROR_ENUMS` 리스트에 등록해야 합니다.
> 등록하지 않으면 `Error.resolveErrorCode("TRACKING_001")`이 `UNKNOWN_ERROR`를 반환합니다.

**파일**: `src/main/java/com/cheftory/api/exception/Error.java`

`ERROR_ENUMS` 리스트에 추가:
```java
// 인터페이스 상수 (암묵적 public static final, 접근제한자 불필요)
List<Class<? extends Enum<?>>> ERROR_ENUMS = List.of(
        GlobalErrorCode.class,
        // ... 기존 ErrorCode들 ...
        TrackingErrorCode.class    // ← 추가
);
```

---

## 9. 성능 고려사항

### 9-1. 예상 트래픽

| 지표 | 추정치 | 근거 |
|------|--------|------|
| Impression API 호출 / 페이지뷰 | 2~5회 | 1초 debounce 배치 + flush |
| Click API 호출 / 페이지뷰 | 0~1회 | 카드 클릭 시 1회 |
| Impression 건수 / API 호출 | 2~10건 | 뷰포트에 동시 보이는 카드 수 |

### 9-2. 최적화 전략

| 단계 | 전략 | 적용 시점 |
|------|------|----------|
| **1단계 (현재)** | JPA `saveAll()` + `batch_size=50` (이미 설정됨) | 초기 출시. `application-dev.yml`에 `batch_size: 50` 이미 적용 |
| **2단계** | `@Async` 비동기 처리 | 응답 지연이 문제될 때 (프론트가 fire-and-forget이므로 우선순위 낮음) |
| **3단계** | Native bulk INSERT 또는 메시지 큐(Redis Stream) | 대규모 트래픽 시 |

### 9-3. 데이터 관리

| 전략 | 설명 |
|------|------|
| **월별 파티셔닝** | `impressed_at` / `clicked_at` 기준 RANGE 파티셔닝. 오래된 데이터 아카이빙 용이 |
| **보관 기간** | 원본: 90일 → 일별 집계 테이블로 요약 후 원본 삭제 가능 |
| **집계 배치** | Spring Batch로 일간/주간 CTR 집계 → 별도 집계 테이블 저장 |

---

## 10. 분석 쿼리 예시

### 레시피별 CTR

```sql
SELECT
    i.recipe_id,
    COUNT(DISTINCT i.id) AS impression_count,
    COUNT(DISTINCT c.id) AS click_count,
    ROUND(COUNT(DISTINCT c.id) * 100.0 / NULLIF(COUNT(DISTINCT i.id), 0), 2) AS ctr_percent
FROM recipe_impression i
LEFT JOIN recipe_click c
    ON i.request_id = c.request_id AND i.recipe_id = c.recipe_id
WHERE i.impressed_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY i.recipe_id
ORDER BY ctr_percent DESC;
```

### Surface별 CTR 비교

```sql
SELECT
    i.surface_type,
    COUNT(DISTINCT i.id) AS impressions,
    COUNT(DISTINCT c.id) AS clicks,
    ROUND(COUNT(DISTINCT c.id) * 100.0 / NULLIF(COUNT(DISTINCT i.id), 0), 2) AS ctr_percent
FROM recipe_impression i
LEFT JOIN recipe_click c
    ON i.request_id = c.request_id AND i.recipe_id = c.recipe_id
WHERE i.impressed_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY i.surface_type
ORDER BY ctr_percent DESC;
```

### Position별 클릭률 (포지션 효과 분석)

```sql
SELECT
    i.position,
    COUNT(DISTINCT i.id) AS impressions,
    COUNT(DISTINCT c.id) AS clicks,
    ROUND(COUNT(DISTINCT c.id) * 100.0 / NULLIF(COUNT(DISTINCT i.id), 0), 2) AS ctr_percent
FROM recipe_impression i
LEFT JOIN recipe_click c
    ON i.request_id = c.request_id
    AND i.recipe_id = c.recipe_id
    AND i.position = c.position
WHERE i.surface_type = 'POPULAR_RECIPES'
    AND i.impressed_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY i.position
ORDER BY i.position;
```

### 일간 순 노출 유저 수

```sql
SELECT
    recipe_id,
    DATE(impressed_at) AS date,
    COUNT(DISTINCT user_id) AS daily_unique_users,
    COUNT(*) AS total_impressions,
    ROUND(COUNT(*) * 1.0 / COUNT(DISTINCT user_id), 1) AS avg_frequency
FROM recipe_impression
WHERE impressed_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY recipe_id, DATE(impressed_at)
ORDER BY date DESC, daily_unique_users DESC;
```

---

## 11. DDL 참고

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

---

## 12. 관련 파일 경로

### 백엔드 (신규 생성)

| 파일 | 경로 |
|------|------|
| Controller | `src/main/java/com/cheftory/api/tracking/TrackingController.java` |
| Service | `src/main/java/com/cheftory/api/tracking/TrackingService.java` |
| Impression Entity | `src/main/java/com/cheftory/api/tracking/entity/RecipeImpression.java` |
| Click Entity | `src/main/java/com/cheftory/api/tracking/entity/RecipeClick.java` |
| SurfaceType Enum | `src/main/java/com/cheftory/api/tracking/entity/SurfaceType.java` |
| Impression Request DTO | `src/main/java/com/cheftory/api/tracking/dto/TrackingImpressionRequest.java` |
| Click Request DTO | `src/main/java/com/cheftory/api/tracking/dto/TrackingClickRequest.java` |
| ErrorCode | `src/main/java/com/cheftory/api/tracking/exception/TrackingErrorCode.java` |
| Exception | `src/main/java/com/cheftory/api/tracking/exception/TrackingException.java` |
| Impression Repository | `src/main/java/com/cheftory/api/tracking/repository/RecipeImpressionJpaRepository.java` |
| Click Repository | `src/main/java/com/cheftory/api/tracking/repository/RecipeClickJpaRepository.java` |

### 프론트엔드 (구현 완료)

| 파일 | 경로 |
|------|------|
| Type 정의 | `webview-v2/src/shared/tracking/model/types.ts` |
| API 클라이언트 | `webview-v2/src/shared/tracking/api/trackingApi.ts` |
| Impression Observer | `webview-v2/src/shared/tracking/hooks/useImpressionObserver.ts` |
| Recipe Tracking Hook | `webview-v2/src/shared/tracking/hooks/useRecipeTracking.ts` |
| Public Export | `webview-v2/src/shared/tracking/index.ts` |
| 기획서 | `webview-v2/docs/recipe-tracking-plan.md` |
