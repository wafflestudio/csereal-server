# 예약 API 클라이언트 전환 안내

## 예약 요청

`POST /api/v2/reservation` 요청에 optional `reservationType`을 추가할 수 있습니다.

```typescript
type ReservationType = "AD_HOC" | "REGULAR";
```

- 명시한 값이 우선합니다.
- 필드가 없고 `recurringWeeks === 1`이면 `AD_HOC`으로 처리합니다.
- 필드가 없고 `recurringWeeks > 1`이면 `REGULAR`로 처리합니다.
- 기존 1회성 예약 요청은 변경 없이 동작합니다.

## UI 및 권한

- `AD_HOC`: `ROLE_RESERVATION`, `ROLE_LABMASTER`, `ROLE_STAFF`가 사용합니다.
- `REGULAR`: `ROLE_LABMASTER`, `ROLE_STAFF`가 사용합니다.
- `AD_HOC` 선택 시 반복 횟수를 1로 고정하고 반복 UI를 비활성화합니다.
- 한 번만 발생하는 정기 예약은 `reservationType: "REGULAR"`, `recurringWeeks: 1`로 전송합니다.
- 서버 검증이 최종 기준이므로 클라이언트 제한만 신뢰하면 안 됩니다.

## 예약 응답

예약 상세 및 생성 응답에 확정된 `reservationType`이 추가됩니다. 기존 응답 필드는 유지됩니다.

## 오류 코드

| 코드 | 의미 | 클라이언트 처리 |
|---|---|---|
| `RESERVE-10` | 반복 횟수가 1 이상이 아니거나 허용 범위를 초과함 | 반복 횟수를 다시 선택하도록 안내 |
| `RESERVE-11` | `AD_HOC`에 반복 횟수를 지정함 | 반복을 1로 고정하거나 `REGULAR` 선택 안내 |
| `RESERVE-12` | 수시 예약 오픈 전 | 예약 가능 시작 시각 안내 |
| `RESERVE-13` | 과거 예약 | 미래 시각 선택 안내 |
| `RESERVE-14` | 정기 신청 기간 종료 | 현재 활성 정기 구간 재조회 |
| `RESERVE-15` | 종료 시각이 시작 시각보다 늦지 않음 | 시간 범위 재입력 안내 |
| `RESERVE-16` | 예약 유형에 대한 권한 없음 | 유형 선택 비활성화 및 권한 안내 |
| `RESERVE-17` | 지원 연도 `1001..9998` 또는 안전한 반복 범위를 벗어남 | 지원 범위 안의 날짜로 재입력 안내 |

클라이언트는 오류 메시지 문자열이 아니라 안정적인 오류 코드로 분기해야 합니다.

## 시간대

예약 API와 `/terms`의 offset 없는 `LocalDateTime` 값은 모두 `Asia/Seoul` 기준으로 해석합니다. 브라우저의 로컬 시간대로 다시 해석하지 말고 서울 시간 기준으로 비교·표시해야 합니다.

## 정기 구간

`GET /api/v2/reservation/terms`는 유효한 구간만 다음 순서로 반환합니다.

1. `applyStartTime` 오름차순
2. `termStartTime` 오름차순
3. `id` 오름차순

신청 창은 동시에 둘 이상 활성화될 수 있습니다. 현재 시각에 대해 다음 조건을 만족하는 항목을 `find`가 아닌 `filter`로 모두 표시해야 합니다.

```text
applyStartTime <= now < applyEndTime
```

공휴일 보정은 지원하지 않습니다.
