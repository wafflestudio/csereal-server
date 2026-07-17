# 정기 예약 구간 복구 Runbook

## 탐지

서버 로그에서 다음 structured event를 모니터링합니다.

| event | 필드 |
|---|---|
| `reserve_term_invalid` | `termYear`, `termType`, `reason`, `candidateIds` 또는 partial metadata의 `termId` |
| `reserve_term_reconciliation_failed` | `termYear`, `termType`, `reason`, `candidateIds` |
| `reserve_term_concurrent_insert_invalid` | `termYear`, `termType`, `reason`, `candidateIds` |

유효하지 않은 구간은 예약 정책과 `/terms` 응답에서 fail closed됩니다. `candidateIds`가 비어 있으면 `termYear`와 `termType`으로 keyed row를 먼저 조회한 뒤 같은 term window와 겹치는 legacy row를 함께 확인합니다.

## 원칙

- 운영 중인 행의 시간을 자동으로 덮어쓰지 않습니다.
- 자동화가 불일치 행을 삭제하거나 canonical 값으로 정규화하지 않습니다.
- 수정 전 대상 행과 연관 예약을 백업하고 담당자 승인을 받습니다.

## 확인 쿼리

```sql
SELECT id, term_year, term_type,
       apply_start_time, apply_end_time, term_start_time, term_end_time
FROM reserve_term
WHERE id IN (<candidateIds>)
   OR (term_year = <termYear> AND term_type = '<termType>')
ORDER BY term_start_time, id;
```

전환 기간에는 잘못된 enum 문자열도 JPA hydration을 실패시킬 수 있으므로 raw SQL로 함께 audit합니다.

```sql
SELECT id, recurring_weeks, reservation_type
FROM reservation
WHERE reservation_type IS NULL
   OR reservation_type NOT IN ('AD_HOC', 'REGULAR');

SELECT id, term_year, term_type
FROM reserve_term
WHERE (term_year IS NULL) <> (term_type IS NULL)
   OR (term_type IS NOT NULL AND term_type NOT IN (
       'WINTER', 'FIRST_SEMESTER', 'SUMMER', 'SECOND_SEMESTER'
   ));
```

다음 항목을 canonical 정책과 비교합니다.

- `term_year`, `term_type`
- `apply_start_time`, `apply_end_time`
- `term_start_time`, `term_end_time`
- 같은 정기 기간과 부분 또는 전체가 겹치는 다른 행

## 복구

1. 잘못된 행이 왜 생성되었는지 확인합니다.
2. 중복 또는 비정규 행을 보존해야 하는지 운영 담당자와 결정합니다.
3. 승인된 SQL로 잘못된 metadata 또는 시간값을 수정하거나 중복 행을 제거합니다.
4. 트랜잭션을 커밋한 뒤 동일 조회로 canonical 행이 정확히 하나인지 확인합니다.
5. 다음 토요일 스케줄러 실행을 기다리거나 운영 절차에 따라 reconciliation을 호출합니다.
6. 오류 로그가 사라지고 `/terms`에 구간이 다시 노출되는지 확인합니다.

DB를 직접 수정할 수 없는 경우에는 행을 변경하지 말고 운영 장애로 escalation합니다.
