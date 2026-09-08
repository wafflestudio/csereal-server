#!/bin/bash
# 프로덕션 DB 를 덤프한다. ops 컨테이너 안에서 crond 가 부른다(스케줄은 ops/Dockerfile).
set -euo pipefail

# 경로·컨테이너 이름은 compose.ops.yml 이 정한다(마운트 지점과 같은 값이라 여기서
# 기본값을 또 정하면 두 곳이 갈라진다).
: "${BACKUP_DIR:?compose.ops.yml 이 넣어준다}"
: "${METRICS_DIR:?compose.ops.yml 이 넣어준다}"
: "${DB_CONTAINER:?compose.ops.yml 이 넣어준다}"
: "${MYSQL_ROOT_PASSWORD:?}"
: "${MYSQL_DATABASE:?}"

KEEP_DAYS=30
MIN_BYTES=$((10 * 1024 * 1024)) # 정상 덤프는 ~95MB. 이보다 작으면 실패로 본다.
STAMP=$(date +%Y-%m-%d)         # TZ=Asia/Seoul 은 compose 가 넣는다
FINAL="$BACKUP_DIR/mysqldump-$STAMP.gz"
TMP="$FINAL.part"
SIZE=

dump() {
    mkdir -p "$BACKUP_DIR"
    trap 'rm -f "$TMP"' EXIT # 실패하면 조각 파일을 남기지 않는다.

    # --single-transaction : InnoDB 스냅샷으로 일관성을 얻는다. 기본값은 테이블 잠금이라
    #                        덤프 내내 쓰기가 막힌다(전 테이블 InnoDB 확인).
    # --source-data=2      : binlog 위치를 주석으로 남긴다. 시점 복구(PITR)의 기준점.
    # docker exec          : DB 컨테이너 안 정품 mysqldump 를 쓴다. 호스트의
    #                        /usr/bin/mysqldump 는 mariadb-dump 심볼릭 링크였다.
    # MYSQL_PWD            : 비밀번호를 인자로 주면 ps 에 보인다.
    docker exec -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" "$DB_CONTAINER" \
        mysqldump --user=root --single-transaction --source-data=2 \
        --routines --triggers --events --default-character-set=utf8mb4 \
        "$MYSQL_DATABASE" | gzip >"$TMP"
    # pipefail 이 없으면 mysqldump 가 실패해도 gzip 이 받은 만큼 파일을 만들어,
    # 잘린 백업이 정상처럼 남고 cron 은 성공으로 끝난다.

    SIZE=$(stat -c %s "$TMP")
    [ "$SIZE" -ge "$MIN_BYTES" ] || {
        echo "덤프가 너무 작다(${SIZE} bytes) — 실패로 본다" >&2
        exit 1
    }

    mv "$TMP" "$FINAL"
    trap - EXIT
    echo "$(date -Is) 백업 완료 $FINAL ($((SIZE / 1024 / 1024))MB)"
}

# 새 백업이 성공한 뒤에만 지운다. 실패한 날 옛 백업까지 잃으면 안 된다.
prune_old() {
    find "$BACKUP_DIR" -name 'mysqldump-*.gz' -mtime +"$KEEP_DAYS" -delete
}

# "언제 마지막으로 성공했나"를 지표로 남긴다. node_exporter 의 textfile collector 가
# 이 디렉터리를 읽어 Prometheus 에 노출하고, 25시간 넘게 안 갱신되면 경보한다.
# 실패하면 이 함수에 도달하지 않아 값이 늙는다 — 그게 신호다.
write_heartbeat() {
    [ -d "$METRICS_DIR" ] || return 0
    local t="$METRICS_DIR/db_backup.prom"
    {
        echo "# HELP csereal_db_backup_last_success_timestamp_seconds 마지막 백업 성공 시각"
        echo "# TYPE csereal_db_backup_last_success_timestamp_seconds gauge"
        echo "csereal_db_backup_last_success_timestamp_seconds $(date +%s)"
        echo "# HELP csereal_db_backup_size_bytes 마지막 백업 크기"
        echo "# TYPE csereal_db_backup_size_bytes gauge"
        echo "csereal_db_backup_size_bytes $SIZE"
    } >"$t.tmp"
    mv "$t.tmp" "$t" # 원자적 교체 — 읽는 쪽이 반쪽 파일을 보지 않게
}

dump
prune_old
write_heartbeat
