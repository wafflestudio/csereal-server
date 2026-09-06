# 스크립트

호스트에서 손으로 돌리는 도구만 남았다. **예약 실행되는 것은 전부 컨테이너로 옮겼다.**

| 파일 | 무엇 |
|---|---|
| `archive-orphan-files.sh` | 참조 없는 첨부 파일을 찾아 정리한다(수동) |

## 여기 있던 것들이 어디로 갔나

| 옛 파일 | 지금 |
|---|---|
| `db-backup.sh` | `ops/db-backup.sh` — ops 컨테이너, 매일 자정 |
| `backup-offsite.sh` | `ops/backup-offsite.sh` — ops 컨테이너, 매일 00:30 |
| `monitor-restart.sh` | **삭제.** compose 의 healthcheck + `autoheal` 컨테이너가 대신한다 |
| `crontab` | 스케줄이 `ops/crontab` 으로(이미지 안). 호스트 cron 은 비었다 |
| `logrotate.conf` | **삭제.** 컨테이너 로그는 도커 로깅 드라이버가 돌린다 |

호스트에 파일을 두고 "여기서 고치지 마세요" 라고 주석을 다는 방식은 규율에 기대는 것이지
구조가 막는 게 아니다. 이미지에 구우면 호스트에서 고칠 수가 없다.
