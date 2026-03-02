# 레시피 신고 API 문서

## 1. API 개요

| 항목 | 내용 |
|------|------|
| **Endpoint** | `POST /api/v1/recipes/{recipeId}/reports` |
| **설명** | 특정 레시피를 신고합니다 |
| **인증** | 필요 (UserPrincipal) |
| **중복 신고** | 불가능 (동일 사용자가 동일 레시피 재신고 시 에러) |

---

## 2. 요청 (Request)

### Path Parameter

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `recipeId` | UUID (String) | O | 신고할 레시피 ID |

### Request Body

```json
{
  "reason": "INAPPROPRIATE_CONTENT",
  "description": "부적절한 이미지가 포함되어 있습니다."
}
```

| 필드 | 타입 | 필수 | 제약 | 설명 |
|------|------|------|------|------|
| `reason` | String (Enum) | O | - | 신고 사유 (아래 Enum 참조) |
| `description` | String | X | 최대 500자 | 상세 설명 |

### 신고 사유 Enum (`RecipeReportReason`)

| 값 | 설명 |
|------|------|
| `INAPPROPRIATE_CONTENT` | 부적절한 콘텐츠 |
| `MISINFORMATION` | 잘못된 정보 |
| `LOW_QUALITY` | 낮은 품질 |
| `OTHER` | 기타 |

---

## 3. 응답 (Response)

### 성공 응답 (200 OK)

```json
{
  "message": "success"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `message` | String | 항상 `"success"` 반환 |

### 에러 응답

모든 에러 응답은 동일한 형식을 가집니다.

```json
{
  "message": "에러 메시지",
  "errorCode": "ERROR_CODE"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `message` | String | 사용자용 에러 메시지 |
| `errorCode` | String | 개발자용 에러 코드 |

---

## 4. 에러 코드

### 비즈니스 에러 (RecipeReportErrorCode)

| errorCode | message | HTTP Status | ErrorType | 발생 조건 |
|-----------|---------|-------------|-----------|----------|
| `REPORT_001` | 이미 신고한 레시피입니다 | 409 Conflict | CONFLICT | 동일 사용자가 동일 레시피를 중복 신고 |

### 검증 에러 (Validation)

| HTTP Status | 발생 조건 |
|-------------|----------|
| 400 Bad Request | `reason` 필드 누락 또는 null |
| 400 Bad Request | `description`이 500자 초과 |
| 400 Bad Request | `reason`이 유효하지 않은 Enum 값 |
| 400 Bad Request | `recipeId`가 유효하지 않은 UUID 형식 |
| 400 Bad Request | 존재하지 않는 레시피 ID |

---

## 5. 요청 예시

### cURL

```bash
curl -X POST "https://api.cheftory.com/api/v1/recipes/550e8400-e29b-41d4-a716-446655440000/reports" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access_token>" \
  -d '{
    "reason": "INAPPROPRIATE_CONTENT",
    "description": "부적절한 내용이 포함되어 있습니다."
  }'
```

### JavaScript (Fetch)

```javascript
const response = await fetch(
  'https://api.cheftory.com/api/v1/recipes/550e8400-e29b-41d4-a716-446655440000/reports',
  {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${accessToken}`
    },
    body: JSON.stringify({
      reason: 'INAPPROPRIATE_CONTENT',
      description: '부적절한 내용이 포함되어 있습니다.'
    })
  }
);

// 성공
const data = await response.json();
// { message: "success" }

// 에러
if (!response.ok) {
  const error = await response.json();
  // { message: "이미 신고한 레시피입니다", errorCode: "REPORT_001" }
}
```

---

## 6. 내부 동작 방식

### 중복 신고 방지 메커니즘

```
Entity: RecipeReport
Unique Constraint: uq_report_user_recipe (reporter_id, recipe_id)
```

1. 사용자가 신고 요청
2. `RecipeReport` 엔티티 생성
3. JPA 저장 시도
4. DB Unique Constraint 위반 → `DataIntegrityViolationException` 발생
5. `RecipeReportException(DUPLICATE_REPORT)`으로 변환
6. HTTP 409 Conflict + `REPORT_001` 에러 코드 반환

### 데이터 저장 구조

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | UUID | 신고 ID |
| `recipe_id` | UUID | 신고된 레시피 ID |
| `reporter_id` | UUID | 신고자 ID |
| `reason` | Enum | 신고 사유 |
| `description` | String (max 500) | 상세 설명 (null 시 빈 문자열 저장) |
| `created_at` | LocalDateTime | 신고 일시 |

---

## 7. 관련 파일 경로

| 파일 | 경로 |
|------|------|
| Controller | `src/main/java/com/cheftory/api/recipe/report/RecipeReportController.java` |
| Service | `src/main/java/com/cheftory/api/recipe/report/RecipeReportService.java` |
| Entity | `src/main/java/com/cheftory/api/recipe/report/entity/RecipeReport.java` |
| Request DTO | `src/main/java/com/cheftory/api/recipe/report/dto/RecipeReportRequest.java` |
| ErrorCode | `src/main/java/com/cheftory/api/recipe/report/exception/RecipeReportErrorCode.java` |
| Exception | `src/main/java/com/cheftory/api/recipe/report/exception/RecipeReportException.java` |
| Repository | `src/main/java/com/cheftory/api/recipe/report/repository/RecipeReportRepository.java` |
| Repository Impl | `src/main/java/com/cheftory/api/recipe/report/repository/RecipeReportRepositoryImpl.java` |
