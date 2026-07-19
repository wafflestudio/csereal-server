# 예약 API 클라이언트 전환 안내

## 요청과 응답 계약

`POST /api/v2/reservation` 요청에서 `reservationType`을 제거합니다. 서버가 인증 역할, 방, 대상 학기, 현재 시각과 `recurringWeeks`를 기준으로 `AD_HOC`, `REGULAR`, `UNRESTRICTED` 중 하나를 결정합니다. 이전 클라이언트가 보내는 `reservationType` 값은 정책에 영향을 주지 않습니다.

응답의 `reservationType`은 nullable입니다. 새 예약에는 서버가 결정한 값이 항상 있지만 V16 이전 legacy 행은 `null`일 수 있으므로 클라이언트는 `null`을 과거 미분류 값으로 표시해야 합니다.

```typescript
type ReservationType = "AD_HOC" | "REGULAR" | "UNRESTRICTED";
type ReservationTypeResponse = ReservationType | null;
```

요청의 `LocalDateTime` 필드는 기존과 같이 offset 없는 문자열로 전송합니다. 반면 예약 및 학기(term) 응답의 `LocalDateTime` 문자열은 전역 serializer 때문에 끝에 `Z`가 붙습니다. 이 `Z`는 서버가 Asia/Seoul wall-clock 값을 실제 UTC instant로 변환했다는 뜻이 아닙니다. 서버는 저장된 `LocalDateTime`의 숫자 날짜·시각 구성요소를 그대로 유지한 채 `Z`를 붙이므로, API wire contract가 별도로 versioning될 때까지 클라이언트는 응답을 일반 instant로 해석한 후 Asia/Seoul로 time-zone 변환하여 표시 시각을 9시간 늦추면 안 됩니다. 대신 숫자 구성요소를 보존하여 Asia/Seoul local wall-clock으로 해석하고 표시해야 합니다. 예를 들어 저장값 `2027-03-20T10:00`은 wire에서 `2027-03-20T10:00:00Z`이고 화면에는 `2027-03-20 10:00 KST`로 표시해야 하며, `19:00 KST`로 표시하면 안 됩니다.

## 서버 정책 요약

서버는 요청이 대상으로 삼는 canonical 학기의 단계와 현재 시각을 기준으로 다음 정책을 적용합니다.

| 사용자 역할 | 방 | 대상 학기 단계와 현재 시각 | 반복 | 결과 |
|---|---|---|---|---|
| `ROLE_STAFF` | 모든 방 | 미래 시각 | `1..15`(서버 설정값) | `UNRESTRICTED` |
| `ROLE_LABMASTER` | 세미나실 | 신청 시작 전 | 무관 | 거부 |
| `ROLE_LABMASTER` | 세미나실 | 신청 창 `[applyStartTime, termStartTime)` | 1회 또는 반복 | `REGULAR` |
| `ROLE_RESERVATION` | 세미나실 | 대상 학기 시작 전(신청 창 포함) | 무관 | 거부 |
| `ROLE_LABMASTER` 또는 `ROLE_RESERVATION` | 세미나실 | 대상 학기 시작 이상이지만 해당 예약의 조정된 AD_HOC 오픈 시각 전 | 무관 | 거부 |
| `ROLE_LABMASTER` 또는 `ROLE_RESERVATION` | 세미나실 | 대상 학기 시작 이상이고 해당 예약의 조정된 AD_HOC 오픈 시각 이상 | 1회 | `AD_HOC` |
| 모든 비 staff | 세미나실 | 대상 학기 시작 이상 | 2회 이상 | 거부 |
| 모든 비 staff | 세미나실이 아닌 방 | 모든 단계 | 무관 | 거부 |

`REGULAR` 판정에는 요청의 모든 주차가 동일한 local wall-clock 시각으로 하나의 canonical 학기에 들어가야 합니다. 해당 학기의 persisted canonical 데이터가 없거나 요청과 일치하지 않으면 서버는 fail-closed로 거부하며 `AD_HOC`으로 fallback하지 않습니다.

AD_HOC 오픈 시각은 각 예약 시각의 2주 전 09:00 Asia/Seoul입니다. 오픈일이 토요일 또는 일요일이면 다음 월요일 09:00로 이동하고 공휴일 보정은 하지 않습니다. 다른 학기의 신청 창이 현재 활성 상태여도 대상 학기의 AD_HOC 판정을 막지 않습니다.

비 staff는 각 발생이 Asia/Seoul 기준 같은 날짜 안에서 3시간 이하여야 합니다. 방 ID 8은 기존과 같이 `ROLE_PROFESSOR` 또는 `ROLE_STAFF`만 허용됩니다. `ROLE_PROFESSOR`만으로는 예약 생성 권한이 생기지 않습니다.

## 클라이언트 작업

1. 요청 type과 type 선택 UI를 제거합니다.
2. 응답의 nullable `reservationType`과 `UNRESTRICTED`를 처리합니다.
3. 서버 오류 코드와 `/terms` 결과를 최종 기준으로 사용합니다.
4. 요청은 기존 offset 없는 `LocalDateTime` 형식을 유지하고, 응답은 위의 trailing `Z` 호환 규칙에 따라 숫자 구성요소를 보존하여 Asia/Seoul local wall-clock으로 표시합니다.

`GET /api/v2/reservation/terms`의 신청 창은 `applyStartTime <= now < applyEndTime`이며 `applyEndTime`은 `termStartTime`과 같습니다.
