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
