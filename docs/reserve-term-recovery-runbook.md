# 정기 예약 구간 복구 Runbook

## 배포 전제와 탐지

Reconciliation은 **토요일 03:00 Asia/Seoul에만** 실행되며 startup reconciliation은 없습니다. 배포 전에 현재 및 다음 canonical 학기 행이 모두 존재하고 정확한지 확인해야 합니다. 누락된 상태로 토요일 이전에 배포하면 pre-start `REGULAR` 생성은 fail closed됩니다.

다음 structured event를 모니터링합니다.

| event | 핵심 필드 |
|---|---|
| `reserve_term_invalid` | `termYear`, `termType`, `reason`, `candidateIds`, `expected`, `actualCandidates`, `action` |
| `reserve_term_reconciliation_failed` | `termYear`, `termType`, `reason`, `candidateIds`, `expected`, `actualCandidates`, `action` |
| `reserve_term_concurrent_insert_invalid` | `termYear`, `termType`, `reason`, `candidateIds`, `expected`, `actualCandidates`, `action` |

`expected`는 `termYear`, `termType`, `applyStartTime`, `applyEndTime`, `termStartTime`, `termEndTime`을 모두 포함하는 canonical descriptor입니다. `actualCandidates`의 각 항목은 `id`와 동일한 metadata/네 시간 필드를 모두 포함합니다. 후보가 없으면 `candidateIds=[] actualCandidates=[]`로 명시됩니다. Partial metadata도 `termStartTime`이 속한 canonical 학기를 기대값으로 산출하고 실제 nullable metadata를 그대로 기록합니다. 모든 보존형 실패는 정확히 `action=preserved_fail_closed`를 기록합니다.

유효하지 않거나 충돌하는 행은 자동 수정·삭제하지 않고 보존하며 예약 정책과 `/terms`에서 fail closed됩니다.

## 확인

```sql
SELECT id, term_year, term_type,
       apply_start_time, apply_end_time, term_start_time, term_end_time
FROM reserve_term
WHERE id IN (<candidateIds>)
   OR (term_year = <termYear> AND term_type = '<termType>')
ORDER BY term_start_time, id;
```

metadata와 네 시간 필드를 canonical 정책과 비교합니다. `apply_end_time`은 반드시 `term_start_time`과 같아야 하며, 같은 term window에 겹치는 다른 행도 확인합니다.

```sql
SELECT id, recurring_weeks, reservation_type
FROM reservation
WHERE reservation_type IS NULL
   OR reservation_type NOT IN ('AD_HOC', 'REGULAR', 'UNRESTRICTED');

SELECT id, term_year, term_type
FROM reserve_term
WHERE (term_year IS NULL) <> (term_type IS NULL)
   OR (term_type IS NOT NULL AND term_type NOT IN (
       'WINTER', 'FIRST_SEMESTER', 'SUMMER', 'SECOND_SEMESTER'
   ));
```

Legacy reservation의 `reservation_type IS NULL`은 정상이며 backfill하지 않습니다.

## 복구

1. 대상 행과 연관 예약을 백업하고 담당자 승인을 받습니다.
2. 생성 원인과 canonical 기대값을 확인합니다.
3. 승인된 SQL로 metadata/시간을 수정하거나 충돌 행을 제거합니다. 자동화는 기존 값을 덮어쓰지 않습니다.
4. 커밋 후 canonical 행이 정확히 하나이고 metadata와 네 시간 필드가 모두 일치하는지 재조회합니다.
5. 다음 토요일 실행을 기다립니다. 그 전에 서비스가 필요하면 운영 승인 절차로 reconciliation을 명시적으로 실행합니다.
6. current/next 두 학기 모두 재검증하고 로그와 `/terms` 노출을 확인합니다.

DB를 안전하게 수정할 수 없거나 원인이 불명확하면 행을 변경하지 말고 운영 장애로 escalation합니다.
