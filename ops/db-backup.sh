#!/bin/bash
# 프로덕션 DB 를 덤프한다. ops 컨테이너 안에서 crond 가 부른다(스케줄은 ops/crontab).
#
# 설정은 전부 환경변수로 받는다. 컨테이너라 호스트 경로를 알 필요가 없다.
set -euo pipefail

BACKUP_DIR="${BACKUP_DIR:-/backup}"
DB_CONTAINER="${DB_CONTAINER:-csereal_db_container}"
METRICS_DIR="${METRICS_DIR:-/metrics}"
KEEP_DAYS="${KEEP_DAYS:-30}"
MIN_BYTES=$((10 * 1024 * 1024)) # 정상 덤프는 ~95MB. 이보다 작으면 실패로 본다.

: "${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD 가 필요하다}"
: "${MYSQL_DATABASE:?MYSQL_DATABASE 가 필요하다}"

mkdir -p "$BACKUP_DIR"

# TZ=Asia/Seoul 을 compose 가 넣어 준다.
# ⚠️ 옛 호스트 스크립트는 `date -d "+9 hour"` 로 9시간을 더했는데, 호스트가 UTC 이던 시절의
#    보정이 남은 것이었다. 그래서 파일 이름이 실제 날짜보다 하루 앞서 있었다.
STAMP=$(date +%Y-%m-%d)
FINAL="$BACKUP_DIR/mysqldump-$STAMP.gz"
TMP="$FINAL.part"

trap 'rm -f "$TMP"' EXIT # 실패하면 조각 파일을 남기지 않는다.

# --single-transaction : InnoDB 스냅샷으로 일관성을 얻는다. 없으면 기본값이 테이블 잠금이라
#                        덤프 내내 쓰기가 막힌다(전 테이블 InnoDB 확인).
# --source-data=2      : binlog 위치를 주석으로 남긴다. 시점 복구(PITR)의 기준점이 된다.
# docker exec          : DB 컨테이너 안 정품 mysqldump(8.0.36)를 쓴다. 호스트의
#                        /usr/bin/mysqldump 는 mariadb-dump 심볼릭 링크였다.
# MYSQL_PWD            : 비밀번호를 인자로 주면 ps 에 보인다.
docker exec -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" "$DB_CONTAINER" \
    mysqldump \
    --user=root \
    --single-transaction \
    --source-data=2 \
    --routines --triggers --events \
    --default-character-set=utf8mb4 \
    "$MYSQL_DATABASE" |
    gzip >"$TMP"
# pipefail 이 있어 mysqldump 가 실패하면 여기서 멈춘다.
# ⚠️ 옛 스크립트엔 이게 없어 덤프가 실패해도 gzip 이 받은 만큼 파일을 만들었다.
#    잘린 백업이 정상 크기처럼 남고 cron 은 성공으로 끝났다.

SIZE=$(stat -c %s "$TMP")
[ "$SIZE" -ge "$MIN_BYTES" ] || {
    echo "덤프가 너무 작다(${SIZE} bytes) — 실패로 본다" >&2
    exit 1
}

mv "$TMP" "$FINAL"
trap - EXIT
echo "$(date -Is) 백업 완료 $FINAL ($((SIZE / 1024 / 1024))MB)"

# 오래된 것 정리는 새 백업이 성공한 뒤에만. 실패한 날 옛 백업까지 잃으면 안 된다.
find "$BACKUP_DIR" -name 'mysqldump-*.gz' -mtime +"$KEEP_DAYS" -delete

# 하트비트 — "언제 마지막으로 성공했나"를 지표로 남긴다.
# node_exporter 의 textfile collector 가 이 디렉터리를 읽어 Prometheus 에 노출하고,
# `time() - csereal_db_backup_last_success_timestamp_seconds > 25시간` 으로 경보한다.
# 실패하면 이 줄에 도달하지 않으므로 값이 갱신되지 않는다 — 그게 신호다.
if [ -d "$METRICS_DIR" ]; then
    T="$METRICS_DIR/db_backup.prom"
    {
        echo "# HELP csereal_db_backup_last_success_timestamp_seconds 마지막 백업 성공 시각"
        echo "# TYPE csereal_db_backup_last_success_timestamp_seconds gauge"
        echo "csereal_db_backup_last_success_timestamp_seconds $(date +%s)"
        echo "# HELP csereal_db_backup_size_bytes 마지막 백업 크기"
        echo "# TYPE csereal_db_backup_size_bytes gauge"
        echo "csereal_db_backup_size_bytes $SIZE"
    } >"$T.tmp"
    mv "$T.tmp" "$T" # 원자적 교체 — 읽는 쪽이 반쪽 파일을 보지 않게
fi
