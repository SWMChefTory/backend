# Changelog

All notable changes to the Cheftory Backend API will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added
- 향후 추가될 기능들

### Changed
- 향후 변경될 사항들

### Fixed
- 향후 수정될 버그들

## [1.1.27] - 2026-02-15

### Added
- **에러 타입/상태 해석 체계 도입**: `ErrorType`, `ErrorStatusResolver`, `ValidationErrorMapper` 추가로 예외 타입별 HTTP 상태 및 검증 오류 매핑 표준화
- **도메인 경계 전용 예외 추가**: 랭킹/검색/크레딧 경계에서 사용하는 전용 예외 및 에러코드 추가
  - `RankingException`, `RankingCandidateException`, `RankingPersonalizationException`
  - `RecipeSearchException`, `RecipeCreditException`
  - `UserCreditException`, `UserShareCreditException`
- **커서 코덱 공통 지원 추가**: `AbstractCursorCodecSupport` 도입 및 코덱별 단위 테스트 확장

### Changed
- **예외 처리 일관성 강화**: 다수 도메인 예외(`*Exception`)가 공통 cause 전달 패턴을 따르도록 정비
- **전역 예외 처리 리팩토링**: `GlobalExceptionHandler`가 에러 타입 기반 상태 결정 및 검증 에러 응답 매핑을 사용하도록 개선
- **도메인 경계 리팩토링**: Adapter/Port 계층에서 외부/인프라 예외를 도메인 전용 예외로 변환하도록 정리
  - 대상: 레시피 검색, 랭킹 후보/개인화, 레시피 랭킹, 사용자/공유 크레딧 등
- **커서 인코딩/디코딩 구조 정리**: `CountId`, `Rank`, `Ranking`, `ScoreId`, `ViewedAt` 코덱의 공통 로직 및 오류 처리 개선

### Fixed
- **검증 오류 응답 누락/불일치 개선**: 필드 검증 실패 시 에러 응답 구조와 메시지 매핑 정확도 개선
- **커서 파싱 실패 처리 개선**: 잘못된 커서 입력에 대한 예외 코드 반환 일관성 강화

### Removed
- **중복 네트워크 예외 타입 제거**: `ExternalServerNetworkException`, `ExternalServerNetworkExceptionCode` 삭제 후 공통 에러 타입 체계로 통합

### Technical
- **JavaDoc/포맷 정비**: 전반적인 JavaDoc 보강 및 lint 포맷 정리(`chore: format java doc`, `chore: format lint`)


## [1.1.26] - 2026-02-12

### Added
- **레시피 신고 기능**: 사용자가 부적절한 레시피를 신고할 수 있는 기능 추가
  - `RecipeReport` 엔티티: 신고자 ID, 레시피 ID, 신고 사유, 상세 설명, 생성일시 포함
  - 중복 신고 방지를 위한 유니크 제약조건 (`reporter_id`, `recipe_id`)
  - `RecipeReportReason` enum: INAPPROPRIATE_CONTENT, MISINFORMATION, LOW_QUALITY, OTHER
  - `RecipeReportErrorCode.DUPLICATE_REPORT`: 이미 신고한 레시피일 때 예외
  - `RecipeReportService.report()`: 레시피 신고 생성
  - `POST /api/v1/recipes/{recipeId}/reports` 엔드포인트
- **테스트 추가**: 엔티티, 서비스, 리포지토리, 컨트롤러 테스트 추가

### Changed
- **사용자 정보 조회**: `UserResponse`에 `tutorialAt` 필드 추가 (JSON: `tutorial_at`)
  - 클라이언트에서 사용자 정보 조회 시 튜토리얼 완료 시간 확인 가능


## [1.1.25] - 2026-02-11

### Changed
- **애플 로그인**: 회귀버그 수정

---

## [1.1.24] - 2026-02-11

### Added
- **인증 클라이언트 인터페이스**: `AppleTokenClient`, `GoogleTokenClient` 인터페이스 도입으로 모듈성 및 테스트 용이성 개선
- **WebClient 기반 외부 API 클라이언트**: `AppleTokenExternalClient`, `GoogleTokenExternalClient`로 외부 API 호출 WebClient로 마이그레이션
- **외부 API 응답 DTO**: `AppleJwksResponse` 등 외부 API 응답을 위한 전용 DTO 추가
- **WebClient 설정 추가**: `WebclientConfig`에 Apple 및 Google 인증 서비스를 위한 `WebClient` 빈 추가
- **광범위한 단위 테스트 추가**: 다음 컴포넌트에 대한 포괄적인 단위 테스트 도입
  - `AppleTokenVerifier`, `GoogleTokenVerifier` - 인증 토큰 검증 로직
  - `RecipeCategoryService` - 레시피 카테고리 관리
  - `RecipeChallengeService` - 레시피 챌린지 기능
  - `RecipeCreationTxService` - 레시피 생성 트랜잭션 처리
  - `RecipeSearchAdapter` - 레시피 검색 어댑터
  - `Iso8601DurationToSecondConverter` - ISO 8601 기간 변환
  - `RecipeCategoryCounts` - 카테고리별 집계
  - `YoutubeMetaExternalClient` - YouTube 메타데이터 외부 클라이언트

### Changed
- **인증 토큰 검증 리팩토링**: `AppleTokenVerifier`, `GoogleTokenVerifier`를 전용 클라이언트 인터페이스 사용하도록 리팩토링
- **HttpClient → WebClient 마이그레이션**: 기존 `HttpClient` 기반 구현을 Spring WebFlux의 `WebClient`로 마이그레이션으로 비동기 처리 및 확장성 확보
- **캐싱 전략 개선**: 수동 캐싱(`AtomicReference`)에서 Spring의 `@Cacheable` 어노테이션 기반 캐싱으로 전환
- **레시피 랭킹 서비스 개선**: `RecipeRankService`의 커서 기반 페이지네이션 로직을 `RecipeRankRepository`로 위임하여 서비스 계층 간소화 및 데이터 액세스 관심사 분리

---

## [1.1.23] - 2026-02-10

### Added
- **AI 생성 레시피 제목**: `RecipeDetailMeta` 엔티티에 `title` 필드 추가 (nullable, 마이그레이션 중)
  - AI가 생성한 레시피 제목 저장을 위한 필드
  - 마이그레이션 진행 중으로 null 허용
- **응답 DTO에 title 필드 추가**: 클라이언트 응답에 레시피 제목 포함
  - `RecipeOverview`, `RecipeBookmarkOverview`에 `videoTitle` 필드
  - `FullRecipeResponse.DetailMeta`에 `title` 필드
- **제목 fallback 로직**: `detailMeta.title`이 없으면 `youtubeMeta.title`(유튜브 영상 제목)로 대체
  - AI 제목 없이도 클라이언트에 항상 제목 제공
  - null 또는 빈 문자열일 때 fallback 동작
- **테스트 추가**: title fallback 동작에 대한 단위 테스트 3개 추가

### Changed
- **레시피 제목 반환 방식**: 클라이언트 응답에서 유튜브 영상 제목 대신 AI 생성 제목 우선 사용

---

## [1.1.22] - 2026-02-10

### Changed
- **Repository 계층 완전 분리**: 모든 Repository를 3계층 패턴(인터페이스/구현체/JPA)으로 리팩토링
  - 레시피 관련: `RecipeInfoRepository`, `RecipeDetailMetaRepository`, `RecipeBriefingRepository`, `RecipeIngredientRepository`, `RecipeStepRepository`, `RecipeTagRepository`, `RecipeYoutubeMetaRepository`
  - 생성 관련: `RecipeIdentifyRepository`, `RecipeProgressRepository`
  - 기타: `RecipeRankRepository`, `RecipeCategoryRepository`
  - 각 Repository 인터페이스는 비즈니스 로직을, `*Impl`은 예외 변환을, `*JpaRepository`는 데이터 접근을 담당
- **도메인 엔티티 이동**: `RankingKeyGenerator`를 `RecipeRanking` 엔티티로 통합하여 랭킹 키 생성 로직을 도메인 계층으로 이동
- **데이터 접근 메서드 명확화**: `RecipeInfoRepository.gets()`가 `RecipeStatus.SUCCESS`만 반환하도록 변경하여 FAILED 레시피 노출 방지
- **서비스 책임 분리**: `RecipeInfoService.getSuccess()`에서 `increaseCount()` 호출 제거하여 조회 책임을 호출처(Facade)로 이관

### Added
- **JavaDoc 대규모 추가**: 전체 코드베이스의 Controller, Service, Facade, Port, Repository 클래스에 포괄적인 JavaDoc 추가
  - 클래스 및 메소드 레벨 문서화로 가독성 및 유지보수성 개선
- **검색 Port 도입**: `RecipeSearchPort`, `RecipeRankingPort`, `RankingPersonalizationSearchPort`, `RankingCandidateSearchPort` 인터페이스로 외부 시스템과의 계약 명확화
- **챌린지 Repository JavaDoc**: `RecipeChallengeRepository`, `RecipeUserChallengeRepository`, `RecipeUserChallengeCompletionRepository` 문서화

### Fixed
- **요리 종류(cuisine) 레시피 조회에서 FAILED 상태 노출 문제 수정**: `RecipeInfoRepository.gets()`가 `RecipeStatus.SUCCESS` 필터링 적용하여 실패 레시피가 결과에 포함되지 않도록 수정
- **.DS_Store 파일 커밋 방지**: `.gitignore`에 `.DS_Store` 패턴 추가

### Technical
- **JUnit 4 → JUnit 5 마이그레이션 완료**: `mockwebserver`를 `mockwebserver3-junit5`로 변경 및 JUnit 4 의존성 제거
- **불필요한 의존성 제거**: `junit`, `junit-vintage-engine`, `hamcrest`, `xmlunit-core`, `jsonassert` 의존성 제거
- **테스트 리팩토링**: BDD 스타일 테스트 패턴 적용 및 리포지토리 테스트 구조화

---

## [1.1.21] - 2026-02-09

### Added
- **낙관적 락 재시도 메커니즘**: 동시성 문제에 대한 회복성 강화를 위한 `OptimisticRetryExecutor` 클래스 도입
  - 구성 가능한 maxAttempts 및 백오프 전략으로 선언적 재시도 지원
  - 낙관적 락 실패가 발생할 수 있는 작업의 재시도 로직 중앙화
- **커서 오류 처리**: 페이징에서 유효하지 않은 커서에 대한 구체적인 오류 처리를 제공하는 `CursorErrorCode` 및 `CursorException` 클래스 추가
- **커서 기반 페이징 오류 처리 강화**: `ViewedAtCursorCodec`가 새로운 커서 예외 클래스를 활용하여 디코딩/인코딩 실패 시 정밀한 예외 발생
- **테스트 커버리지 확대**: 리팩토링된 repository 구현체(`RecipeCategoryRepositoryImpl`, `RecipeBookmarkRepositoryImpl`)에 대한 포괄적인 단위 테스트 추가

### Changed
- **레시피 카테고리 Repository 리팩토링**: 직접적인 JPA repository에서 인터페이스 기반 설계(`RecipeCategoryRepository` + `RecipeCategoryRepositoryImpl`)로 리팩토링
  - 데이터 접근 계층의 추상화 개선으로 유지보수성 강화
  - Service 계층과 직접적인 JPA repository 사용의 결합도 감소
- **레시피 카테고리 삭제 보안 강화**: `RecipeController`, `RecipeFacade`, `RecipeCategoryService`에서 레시피 카테고리 삭제 시 명시적으로 `userId`를 요구하도록 업데이트
  - 카테고리의 소유자만 삭제를 시작할 수 있도록 보장
  - 권한 없는 사용자의 카테고리 삭제 방지로 데이터 무결성 개선
- **Repository 낙관적 재시도 통합**: `RecipeBookmarkRepositoryImpl` 및 `UserShareRepositoryImpl`가 새로운 `OptimisticRetryExecutor`를 활용하도록 업데이트
  - repository 메서드에서 직접적인 @Retryable 어노테이션 제거로 관심사의 분리 개선
  - 일관적인 재시도 동작 보장
- **커서 예외 시그니처 정확화**: `keysetRecents` 및 `keysetCategorized` 메서드의 throws 절을 `RecipeBookmarkException`에서 `CursorException`으로 변경
  - 실제로 발생하는 예외와 메서드 시그니처 일치로 오류 처리 정확성 개선
  - RecipeBookmarkRepository, RecipeBookmarkService, RecipeFacade의 해당 메서드 업데이트

### Technical
- **Javadoc 문서화**: recipe.bookmark 및 recipe.category 패키지 내의 클래스, 인터페이스, 열거형, 메서드에 다수의 Javadoc 주석 추가
  - 코드 가독성 및 유지보수성 대폭 향상
  - 향후 개발 및 협업 효율성 증가

---

## [1.1.20] - 2026-02-08

### Changed
- **Repository 리팩토링**: Repository 계층을 인터페이스와 구현체로 분리하여 책임 명확화 및 테스트 용이성 개선
  - `UserRepository` → `UserRepository` (인터페이스) + `UserRepositoryImpl` (구현체)
  - `UserShareRepository` → `UserShareRepository` (인터페이스) + `UserShareRepositoryImpl` (구현체)
  - `LoginRepository` → `LoginRepository` (인터페이스) + `LoginRepositoryImpl` (구현체)
  - `RecipeBookmarkRepository` → `RecipeBookmarkRepository` (인터페이스) + `RecipeBookmarkRepositoryImpl` (구현체)
- **Ports 패턴 도입**: 외부 시스템과의 통합을 위한 `UserCreditPort`, `UserShareCreditPort`, `RecipeCreditPort` 인터페이스 도입으로 의존성 역전 원칙 적용
- **JavaDoc 추가**: 인증 관련 클래스 (`Login`, `LoginRepository`, `AuthService`, `TokenProvider` 등)에 포괄적인 JavaDoc 추가로 가독성 및 유지보수성 개선

### Fixed
- **일일 공유 횟수 초과 시 에러코드 수정**: `UserShareTxService`의 `@Retryable` 어노테이션에 `notRetryFor = UserShareException.class` 추가 — 일일 공유 횟수 초과 시 재시도로 인해 글로벌 에러코드가 아닌 구체적인 `USER_SHARE_DAILY_LIMIT_EXCEEDED` 에러코드가 반환되도록 수정
- **보상 트랜잭션 복구 로직 개선**: `UserShareTxService.recover()` 메서드를 `UserShareException`과 `ObjectOptimisticLockingFailureException`으로 오버로드하여 각 예외 타입별 적절한 복구 로직 구현

### Added
- **검증 도구 추가**: `.sisyphus/` 디렉토리를 `.gitignore`에 추가하여 개발 환경 도구 관리
- **테스트 커버리지 확대**: 새로운 Repository 구현체에 대한 단위 테스트 추가 (`UserRepositoryImplTest`, `UserShareRepositoryImplTest`, `RecipeBookmarkJpaRepositoryTest` 등)

---

## [1.1.19] - 2026-02-07

### Fixed
- **튜토리얼 크레딧 지급 실패 수정**: `UserRepository.completeTutorialIfNotCompleted()`, `revertTutorial()`에 `@Transactional` 추가 — `@Modifying` 쿼리가 트랜잭션 없이 실행되어 `TransactionRequiredException` 발생하던 문제 해결
- **크레딧 트랜잭션 reason 컬럼 길이 명시**: `CreditTransaction.reason`에 `@Column(length = 50)` 추가 — 프로덕션 DB 컬럼 크기 부족으로 `SHARE` 값 저장 시 `Data truncated` 오류 발생하던 문제 대응 (DB ALTER TABLE 필요)
- **예외 처리 범위 확대**: `UserService.tutorial()`과 `UserShareService.share()`의 catch 블록을 `Exception`으로 변경하여 모든 예외에서 보상(revert/compensation) 트랜잭션이 실행되도록 보장
- **크레딧 동시성 예외 타입 명확화**: `CreditTxService.loadOrCreateBalance()`의 `orElseThrow()`에 `CreditException(CREDIT_CONCURRENCY_CONFLICT)` 명시 — 기존 `NoSuchElementException`이 GLOBAL_3으로 처리되던 문제 해결

---

## [1.1.18] - 2026-02-07

### Added
- **튜토리얼 완료 크레딧 기능 추가**: `POST /api/v1/users/tutorial` 엔드포인트 및 사용자 튜토리얼 완료 시 크레딧 지급 로직 도입
- **공유 크레딧 기능 추가**: `POST /api/v1/users/share` 엔드포인트, `UserShare` 엔티티/리포지토리, `UserShareService`, `UserShareTxService`를 추가하고 일일 지급 제한 정책 적용
- **크레딧 사유 확장**: `CreditReason`에 `TUTORIAL`, `SHARE` 타입 추가
- **사용자 크레딧 어댑터 도입**: 사용자 액션(튜토리얼/공유) 보상 처리를 위한 `UserCreditAdapter` 추가

### Changed
- **에러 처리 인터페이스 통합**: `ErrorMessage`를 `Error`로 전역 전환하여 예외 코드 및 응답 처리 일관성 강화
- **크레딧 어댑터 책임 분리**: 기존 `CreditAdapter`를 `RecipeCreditAdapter`로 명확화하여 레시피 보상 로직과 사용자 보상 로직의 경계를 분리

---

## [1.1.17] - 2026-02-05

### Added
- **AI 분석 리소스 정리 로직 도입**: 레시피 생성 완료(성공/실패) 후 AI 서버의 임시 파일(Gemini File)을 삭제하는 `RecipeCreationCleanupStep` 추가
- **북마크 생성 안정화**: `RecipeBookmarkService.create` 시 `saveAndFlush`를 사용하여 유니크 제약 조건 위반 예외를 즉시 감지하고 처리하도록 개선

### Changed
- **레시피 생성 파이프라인 구조 개선**: 파이프라인 종료 시점에 `finally` 블록을 통한 리소스 정리 보장
- **실패 처리 로직 세분화**: `AsyncRecipeCreationService`에서 `banned`와 `failed` 상태 처리를 분리하여 데이터 일관성 확보

---

## [1.1.16] - 2026-02-04

### Added
- **DB 스로틀링 시스템 도입**: Virtual Thread 환경에서 DB 커넥션 풀 고갈 방지를 위한 `@DbThrottled` 어노테이션 및 `DbThrottlingAspect` 추가 (Semaphore 20개 제한)
- **영상 검증 단계 추가**: 레시피 생성 파이프라인에 `RecipeCreationVerifyStep` 도입

### Changed
- **영상 기반 AI 분석 전환**: 자막(Caption) 기반 분석에서 영상 파일 직접 분석 방식으로 변경 (`/verify`, `/meta/video`, `/steps/video` API 연동)
- **레시피 생성 파이프라인 리팩토링**: `RecipeCaption` 의존성 제거 및 `fileUri`, `mimeType` 기반 흐름으로 개선

### Removed
- **자막 모듈 제거**: 더 이상 사용되지 않는 `RecipeCaption` 엔티티, 리포지토리 및 관련 로직 제거

---

## [1.1.15] - 2026-01-22

### Changed

- **레시피 히스토리 리팩토링**: `RecipeHistory`를 `RecipeBookmark`로 변경하여 북마크 기능으로 전환
- **레시피 생성 로직 개선**: 레시피 생성 시 북마크 자동 추가 및 크레딧 차감 로직 통합
- **레시피 조회 로직 개선**: 레시피 조회 시 북마크 여부 확인 로직 추가

---

## [1.1.14] - 2026-01-22

### Changed

- 추천 후보 검색 쿼리 스코어링을 가중치 합산 방식으로 조정
- 키워드/채널/신선도 함수 점수 구성 및 감쇠 파라미터 정비

---

## [1.1.13] - 2026-01-22

### Added

- 랭킹/추천 시스템 도입 및 개인화 프로필 집계 로직 추가
- 추천 후보 검색을 위한 PIT 기반 커서 페이지네이션
- 랭킹 스냅샷 및 요청 상태 관리용 Redis 저장소

### Changed

- 요리 종류 추천 흐름을 랭킹 서비스 기반으로 전환
- 추천 결과 커서 관리(요청 ID + searchAfter)로 통일

### Fixed

- 추천 노출 포지션 TTL 일관성 보장
- 요리 종류 매핑 로직을 명시적 매핑으로 개선

---

## [1.1.12] - 2026-01-19

### Changed

- 검색/자동완성/히스토리 API를 scope 기반 시그니처로 정비하고 컨트롤러 엔드포인트를 `/api/v1/search/*`로 통합
- 검색 리포지토리(OpenSearch)에서 scope/market 필터링 및 정렬 조건 정비

### Fixed

- 검색/자동완성/히스토리 관련 테스트가 최신 scope/커서 동작과 일치하도록 수정

---

## [1.1.11] - 2026-01-19

### Added

- **CursorPageable.probe 도입**: hasNext 판정을 위한 pageSize+1 프로브 Pageable 추가
- **챌린지 커서 페이지네이션**: 챌린지 레시피 조회에 cursor 기반 페이지네이션 지원

### Changed

- **CursorPageable.firstPage 동작 정리**: 응답 사이즈와 일치하도록 firstPage 크기 정렬
- **커서 페이지네이션 로직 정비**: 레시피 관련 서비스에서 probe 기반 조회로 hasNext 일관성 확보
- **챌린지 레시피 응답 확장**: `next_cursor` 필드 추가

### Fixed

- **커서 페이지네이션 테스트 보완**: RecipeInfo/History 서비스 테스트에서 probe 결과 반영

---

## [1.1.10] - 2026-01-18

### Added

- **커서 공통 모듈**: CountId/ScoreId/ViewedAt/Rank 커서 및 Codec 추가

### Changed

- **커서 기반 페이지네이션 전환**: 레시피 목록/검색/히스토리/랭킹 관련 조회 API를 cursor 기반으로 리팩토링
- **검색/조회 응답 구조 정리**: CursorPage 응답 포맷 통일 및 관련 조회 로직 정비

---

## [1.1.9] - 2026-01-05

### Added

- **Apple 웹 로그인 지원**: Web Service ID를 통한 Apple Sign In 웹 환경 지원
  - `AppleProperties`에 `appId` (iOS/Android)와 `serviceId` (Web) 분리
  - Backward compatibility: 기존 `clientId` 설정만으로도 작동

### Changed

- **AppleTokenVerifier 개선**: Null-safe audience 검증 로직
  - `Stream.of().filter(Objects::nonNull)` 패턴으로 null 안전성 확보
  - `Collections.disjoint()` 사용으로 가독성 향상
  - 명시적인 설정 오류 검증 추가

### Technical

- **환경 변수 추가**: `APPLE_APP_ID`, `APPLE_SERVICE_ID`
- **배포 설정 업데이트**: GitHub Actions 및 Docker Compose 환경 변수 구성 변경

---

## [1.1.8] - 2026-01-02

### Added
- **유튜브 채널명 노출**: 유튜브 영상 메타데이터에 채널명 추가 및 레시피/영상 관련 API 응답 확장

### Changed
- **유튜브 API 토큰 분리**: 기본 요청과 차단 체크용 토큰을 분리하여 설정/배포 구성 반영

### Removed
- **Dev 배포 워크플로우**: `deploy.dev.yml` 워크플로우 제거

---

## [1.1.7] - 2025-12-29

### Added

#### 인증 및 사용자 관리
- **Apple Sign In**: Apple OAuth 인증 지원
- **Google Sign In**: Google OAuth 인증 지원
- **JWT 토큰 인증**: Access Token 및 Refresh Token 기반 인증 시스템
- **사용자 프로필**: 닉네임, 태그 기반 사용자 관리

#### 레시피 기능
- **레시피 CRUD**: 레시피 생성, 조회, 수정, 삭제 API
- **레시피 검색**: OpenSearch 기반 레시피 검색 엔진
- **레시피 카테고리**: 카테고리별 레시피 필터링
- **레시피 단계**: 단계별 조리 가이드 제공
- **재료 관리**: 레시피 재료 및 양 관리

#### 유튜브 연동
- **유튜브 영상 분석**: YouTube Data API v3 연동
- **레시피 자동 생성**: 유튜브 쿠킹 영상에서 레시피 추출
- **AI 요약**: 유튜브 영상 내용 AI 요약 기능

#### 타이머 기능
- **다중 타이머**: 여러 타이머 동시 관리
- **타이머 알림**: 타이머 완료 시 푸시 알림 전송
- **타이머 상태 관리**: in-progress, idle, completed 상태 추적

#### 챌린지 시스템
- **챌린지 생성**: 요리 챌린지 이벤트 관리
- **참여 관리**: 사용자 챌린지 참여 및 진행 상태 추적
- **레시피 완료**: 챌린지 레시피 완료 기록

#### 인프라 및 모니터링
- **Sentry 통합**: 에러 모니터링 및 추적
- **Prometheus 메트릭**: 애플리케이션 성능 메트릭 수집
- **Spring Actuator**: 헬스 체크 및 애플리케이션 상태 모니터링
- **Redis 캐싱**: 성능 최적화를 위한 캐싱 레이어
- **Spring Batch**: 배치 작업 처리

#### API 문서화
- **Spring REST Docs**: API 문서 자동 생성
- **OpenAPI (Swagger)**: API 스펙 문서화

### Technical Details

#### Backend Stack
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
- **Database**: MySQL 8.0
- **Search Engine**: OpenSearch 2.0.2
- **Cache**: Redis
- **Build Tool**: Gradle
- **Container**: Docker

#### Security
- **JWT**: Access Token 및 Refresh Token 기반 인증
- **Spring Security**: 엔드포인트 보안 및 권한 관리
- **OAuth 2.0**: Apple, Google 소셜 로그인

#### DevOps
- **CI/CD**: GitHub Actions
- **Container Registry**: Docker Hub
- **Deployment**: AWS EC2 (Docker Compose)
- **Monitoring**: Sentry, Prometheus

---

## Release Notes

### 배포 정보

- **Version**: 1.0.0
- **Release Date**: 2025-12-29
- **Environment**: Production
- **Docker Image**: `cheftory-proxy-server:latest`

### 배포 방법

```bash
# Release 브랜치 생성
git checkout -b release/1.0.0

# build.gradle 버전 변경
# version = '1.0.0' 으로 수정

# CHANGELOG.md 업데이트
# 변경사항 작성

# 커밋 및 푸시
git add build.gradle CHANGELOG.md
git commit -m "chore: release v1.0.0"
git push origin release/1.0.0

# main 브랜치로 PR 생성 및 머지

# 태그 생성 및 푸시 (main 브랜치에서)
git tag v1.0.0
git push origin v1.0.0
```

태그 푸시 시 자동으로:
- GitHub Release 생성
- Docker 이미지 빌드 및 배포
- Production 환경 배포

---

## Version History

- **1.0.0** (2025-12-29): Initial production release
