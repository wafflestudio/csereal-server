# ops 컨테이너

예약 실행되는 운영 작업을 담는다. 지금은 DB 백업 하나다.

```bash
docker compose -f compose.ops.yml up -d --build
docker logs csereal_ops              # 스케줄 실행 결과
docker exec csereal_ops /opt/ops/db-backup.sh   # 지금 한 번 돌리기
```

## 왜 컨테이너인가

전엔 스크립트·crontab·logrotate 설정이 **전부 호스트에만** 있었다. 레포에 사본을 두고
"호스트에서 고치지 마세요" 라고 주석을 다는 방식은 규율에 기대는 것이지 구조가 막는 게
아니다. 실제로 그 방식이 무너진 사례를 여럿 발견했다 — 모니터링 스크랩이 30일간 죽어
있었고, `db_config` 는 호스트에서 비어 있었고, 감시 스크립트는 레포의 compose 변경을
조용히 깨뜨릴 뻔했다.

이미지에 구우면 호스트에서 고칠 수가 없다. 의존성(docker CLI·rclone)도 호스트에
안 깔린다. 로그는 stdout → 도커 로깅 드라이버가 받으므로 호스트 logrotate 설정도 없다.

## 대가

**컨테이너가 죽으면 백업이 조용히 멈춘다.** cron 은 OS 의 일부라 사실상 항상 뜨지만
컨테이너는 아니다. 그래서 두 가지를 둔다.

- `restart: unless-stopped`
- **하트비트 지표** — 백업이 성공하면 `/metrics/db_backup.prom` 에 성공 시각을 쓴다.
  node_exporter 의 textfile collector 가 이 디렉터리를 읽어 Prometheus 에 노출하면
  `time() - csereal_db_backup_last_success_timestamp_seconds > 90000`(25시간) 으로
  경보할 수 있다. 실패하면 값이 갱신되지 않는 것이 신호다.
  (node_exporter 는 아직 없다. 파일은 미리 쓴다.)

## docker 소켓

DB 컨테이너 안 정품 `mysqldump` 를 부르려고 소켓을 마운트한다. **소켓 접근은 root 권한과
등가**지만, 호스트의 `waffle` 유저가 이미 docker 그룹이라 실질적 권한 변화는 없다.

## 백업

| | |
|---|---|
| 스케줄 | 매일 자정 KST (`ops/Dockerfile` 안 crontab) |
| 보관 | 30일 |
| 위치 | 호스트 `~/database/backup` 을 `/backup` 으로 마운트 |
| 크기 | ~95MB(gzip), DB 실크기 0.29GB |

**아직 남은 구멍: 백업이 원본과 같은 디스크에만 있다.** 호스트가 죽으면 백업도 같이
죽는다. 이미지에 `rclone` 을 넣어 둔 게 그 다음 단계를 위한 것이다.

### 옛 호스트 스크립트에서 고친 것

1. **실패가 조용했다.** `mysqldump | gzip > 파일` 에 pipefail 도 종료코드 검사도 없어,
   덤프가 실패해도 gzip 이 받은 만큼 파일을 만들고 cron 은 성공으로 끝났다.
2. **`--single-transaction` 없음.** 전 테이블 InnoDB 인데 기본값(테이블 잠금)으로 떠서
   덤프 내내 쓰기가 막혔다.
3. **파일명이 하루 앞섰다.** 호스트가 이미 KST 인데 `date -d "+9 hour"` 로 9시간을 또
   더했다(UTC 이던 시절의 보정 잔재).
4. **MySQL 서버를 MariaDB 도구로 떴다.** 호스트의 `/usr/bin/mysqldump` 가
   `mariadb-dump` 심볼릭 링크였다. 이제 `docker exec` 로 컨테이너 안 8.0.36 을 쓴다.
5. 비밀번호가 명령줄에 있어 `ps` 로 보였다 → `MYSQL_PWD`.

덤으로 `--source-data=2` 로 binlog 위치를 덤프에 남긴다(시점 복구 기준점).

### 복원

```bash
docker run -d --name restore-test -e MYSQL_ROOT_PASSWORD=x -e MYSQL_DATABASE=csereal mysql:8.0
gunzip -c ~/database/backup/mysqldump-YYYY-MM-DD.gz | docker exec -i restore-test mysql -uroot -px csereal
```

2026-09-06 에 리허설했다: 에러 0, 36 테이블 96,939 행 전부 일치, 한글 HEX·본문 md5 일치.
자동화는 아직 없다.
