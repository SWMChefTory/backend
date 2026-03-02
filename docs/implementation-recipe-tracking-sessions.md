# 레시피 추적 API 백엔드 구현 세션 계획

> **구현계획서**: `docs/implementation-recipe-tracking.md`
>
> **API 문서**: `docs/api/recipe-tracking-api.md`
>
> **총 3세션**, 각 세션 끝에 `./gradlew compileJava`로 검증

---

## 세션 구성 요약

| 세션 | 내용 | 신규 파일 | 기존 수정 | 검증 |
|------|------|----------|----------|------|
| **Session 1** | 기반 (Enum + Exception + Error 등록) | 3개 | 1개 | `compileJava` |
| **Session 2** | 데이터 레이어 (Entity + Repository + DTO) | 6개 | 0개 | `compileJava` |
| **Session 3** | API 레이어 (Service + Controller) + DDL | 2개 | 0개 | `build` (전체) |

---

## Session 1: 기반 모듈

> 의존성 없는 독립 모듈들. 이후 세션의 기초가 됩니다.

### Step 1: SurfaceType Enum

**신규**: `src/main/java/com/cheftory/api/tracking/entity/SurfaceType.java`

- 프론트엔드 8종과 1:1 매핑
- 참조: `RankingSurfaceType.java` (단순 enum + Javadoc)

### Step 2: TrackingErrorCode

**신규**: `src/main/java/com/cheftory/api/tracking/exception/TrackingErrorCode.java`

- `implements Error` (프로젝트 커스텀 인터페이스)
- 명시적 constructor + getter (Lombok 미사용)
- 참조: `RecipeReportErrorCode.java` (완전 동일 패턴)

### Step 3: TrackingException

**신규**: `src/main/java/com/cheftory/api/tracking/exception/TrackingException.java`

- `extends CheftoryException` (checked exception)
- 1인자 + 2인자(cause) constructor
- 참조: `RecipeReportException.java` (완전 동일 패턴)

### Step 4: Error.java 등록

**수정**: `src/main/java/com/cheftory/api/exception/Error.java`

- `ERROR_ENUMS` 리스트에 `TrackingErrorCode.class` 추가
- import 추가: `import com.cheftory.api.tracking.exception.TrackingErrorCode;`

### Session 1 검증

```bash
./gradlew compileJava
```

**확인 포인트**:
- `TrackingErrorCode`가 `Error` 인터페이스 구현 (3개 메서드)
- `TrackingException`이 `CheftoryException` 상속
- `Error.ERROR_ENUMS`에 등록 완료 (에러코드 중복 충돌 없음)

---

## Session 2: 데이터 레이어

> Entity + Repository + DTO. 비즈니스 로직 없이 데이터 구조만 정의합니다.

### Step 5: RecipeImpression Entity

**신규**: `src/main/java/com/cheftory/api/tracking/entity/RecipeImpression.java`

- `extends MarketScope` (market, countryCode 자동)
- 어노테이션 순서: `@Entity` → `@Table` → `@AllArgs(PRIVATE)` → `@NoArgs` → `@Getter`
- `UUID.randomUUID()` PK 사전 할당
- static `create(Clock, ...)` 팩토리
- 프론트 timestamp → `LocalDateTime.ofInstant(..., ZoneId.of("Asia/Seoul"))`
- `clock.now()` → `createdAt` (커스텀 Clock)
- 참조: `RankingImpression.java`, `RecipeReport.java`

### Step 6: RecipeClick Entity

**신규**: `src/main/java/com/cheftory/api/tracking/entity/RecipeClick.java`

- `RecipeImpression`과 동일 구조
- 차이: `impressedAt` → `clickedAt`

### Step 7: Repository (2개)

**신규**:
- `src/main/java/com/cheftory/api/tracking/repository/RecipeImpressionJpaRepository.java`
- `src/main/java/com/cheftory/api/tracking/repository/RecipeClickJpaRepository.java`

- 단순 `JpaRepository<Entity, UUID>` 인터페이스
- 초기에는 커스텀 쿼리 없음
- 참조: `RankingImpressionRepository.java`

### Step 8: DTO (2개)

**신규**:
- `src/main/java/com/cheftory/api/tracking/dto/TrackingImpressionRequest.java`
- `src/main/java/com/cheftory/api/tracking/dto/TrackingClickRequest.java`

- Java `record` 타입
- 각 필드에 `@JsonProperty("snake_case")` 필수 (글로벌 SNAKE_CASE 설정 없음)
- `@NotNull`, `@Size(min = 1)` Bean Validation
- 중첩 record: `@Valid List<ImpressionItem>` (cascading validation)
- 참조: `RecipeReportRequest.java`

### Session 2 검증

```bash
./gradlew compileJava
```

**확인 포인트**:
- Entity가 `MarketScope` 정상 상속
- `SurfaceType` enum import 정상
- DTO record의 `@JsonProperty` + `@Valid` 어노테이션 정상
- Repository 인터페이스 제네릭 타입 매칭

---

## Session 3: API 레이어 + 통합 검증

> Service + Controller로 전체 API를 완성하고 빌드 검증합니다.

### Step 9: TrackingService

**신규**: `src/main/java/com/cheftory/api/tracking/TrackingService.java`

- `@Service @RequiredArgsConstructor`
- `Clock`, `ImpressionRepository`, `ClickRepository` 주입
- `saveImpressions()`: `@Transactional` + `saveAll()` (배치 INSERT)
- `saveClick()`: `@Transactional` 없음 (단건, Spring Data JPA 내부 트랜잭션)
- 참조: `RankingInteractionService.java` (배치), `RecipeReportService.java` (단건)

### Step 10: TrackingController

**신규**: `src/main/java/com/cheftory/api/tracking/TrackingController.java`

- `@RestController @RequiredArgsConstructor @RequestMapping("/api/v1/tracking")`
- `@PostMapping("/impressions")`, `@PostMapping("/clicks")`
- `@UserPrincipal UUID userId` (커스텀 어노테이션)
- `throws TrackingException` (checked exception)
- `SuccessOnlyResponse.create()` 반환 (import: `_common.reponse` 오타 주의)
- SecurityConfig 변경 불필요 (`.anyRequest().authenticated()` 자동 적용)
- 참조: `RecipeReportController.java`

### Session 3 검증

```bash
# 1. Spotless 포매팅
./gradlew spotlessApply

# 2. 전체 빌드
./gradlew build
```

**확인 포인트**:
- Controller → Service → Repository → Entity 의존성 체인 정상
- `@Valid` 요청 바인딩 정상
- Spotless 포매팅 통과
- 전체 빌드 성공

---

## 세션별 파일 체크리스트

### Session 1 (3 신규 + 1 수정)

- [ ] `tracking/entity/SurfaceType.java`
- [ ] `tracking/exception/TrackingErrorCode.java`
- [ ] `tracking/exception/TrackingException.java`
- [ ] `exception/Error.java` (ERROR_ENUMS 등록)
- [ ] `./gradlew compileJava` 통과

### Session 2 (6 신규)

- [ ] `tracking/entity/RecipeImpression.java`
- [ ] `tracking/entity/RecipeClick.java`
- [ ] `tracking/repository/RecipeImpressionJpaRepository.java`
- [ ] `tracking/repository/RecipeClickJpaRepository.java`
- [ ] `tracking/dto/TrackingImpressionRequest.java`
- [ ] `tracking/dto/TrackingClickRequest.java`
- [ ] `./gradlew compileJava` 통과

### Session 3 (2 신규)

- [ ] `tracking/TrackingService.java`
- [ ] `tracking/TrackingController.java`
- [ ] `./gradlew spotlessApply` 통과
- [ ] `./gradlew build` 통과

---

## 이미 완료된 사항

| 항목 | 파일 | 상태 |
|------|------|------|
| `order_inserts: true` | `application-dev.yml` | 반영 완료 |
| `order_inserts: true` | `application-prod.yml` | 반영 완료 |

---

## DDL (Session 3 완료 후 수동 실행)

Hibernate `ddl-auto` 설정에 따라 자동 생성 가능하나, 인덱스는 수동 추가 필요.
전체 DDL은 `docs/implementation-recipe-tracking.md` 하단 참조.

---

## 주의사항 (세션 공통)

1. **Clock**: `com.cheftory.api._common.Clock` 사용 (java.time.Clock 아님)
2. **SuccessOnlyResponse**: `com.cheftory.api._common.reponse` (패키지 오타 원본)
3. **ErrorCode**: Lombok 미사용, 명시적 constructor + getter
4. **어노테이션 순서**: `@Entity` → `@Table` → `@AllArgs` → `@NoArgs` → `@Getter`
5. **Spotless**: 최종 빌드 전 `./gradlew spotlessApply` 필수
