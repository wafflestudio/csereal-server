#!/bin/bash
# 백업을 다른 호스트로 복사한다. ops 컨테이너 안에서 crond 가 부른다.
set -euo pipefail

# compose.ops.yml 이 정한다(기본값을 여기서 또 정하면 두 곳이 갈라진다).
: "${BACKUP_DIR:?compose.ops.yml 이 넣어준다}"
: "${METRICS_DIR:?compose.ops.yml 이 넣어준다}"
: "${OFFSITE_HOST:?compose.ops.yml 이 넣어준다 (예: ubuntu@168.107.16.249)}"
: "${OFFSITE_PATH:?compose.ops.yml 이 넣어준다}"

KEY=/secrets/backup_offsite # compose 가 읽기 전용으로 마운트한다
PORT=22
KEEP_DAYS=30
TOTAL=

# accept-new: 첫 접속만 받아들이고 이후 호스트 키 변경은 거부한다.
ssh_() {
    ssh -i "$KEY" -p "$PORT" -o StrictHostKeyChecking=accept-new \
        -o ConnectTimeout=20 -o BatchMode=yes "$OFFSITE_HOST" "$@"
}

copy_new() {
    [ -r "$KEY" ] || { echo "SSH 키를 읽을 수 없다: $KEY" >&2; exit 1; }
    ssh_ "mkdir -p '$OFFSITE_PATH'"
    local remote copied=0 f name local_size remote_size
    remote=$(ssh_ "ls -1 '$OFFSITE_PATH' 2>/dev/null || true")

    for f in "$BACKUP_DIR"/mysqldump-*.gz; do
        [ -e "$f" ] || continue
        name=$(basename "$f")
        printf '%s\n' "$remote" | grep -qxF "$name" && continue

        # .part 로 받고 성공했을 때만 옮긴다 — 끊긴 조각이 정상 파일처럼 남지 않게.
        ssh_ "cat > '$OFFSITE_PATH/$name.part' && mv '$OFFSITE_PATH/$name.part' '$OFFSITE_PATH/$name'" <"$f"

        local_size=$(stat -c %s "$f")
        remote_size=$(ssh_ "stat -c %s '$OFFSITE_PATH/$name'")
        [ "$local_size" = "$remote_size" ] || {
            echo "크기 불일치: $name (로컬 $local_size / 원격 $remote_size)" >&2
            exit 1
        }
        echo "복사: $name ($((local_size / 1024 / 1024))MB)"
        copied=$((copied + 1))
    done

    TOTAL=$(ssh_ "ls -1 '$OFFSITE_PATH'/mysqldump-*.gz 2>/dev/null | wc -l")
    echo "$(date -Is) 오프사이트 복사 완료 → $OFFSITE_HOST:$OFFSITE_PATH (새로 $copied 개, 총 $TOTAL 개)"
}

# 보관 정책을 한 곳에서만 정한다. 양쪽이 따로 늙으면 언젠가 어긋난다.
prune_remote() {
    ssh_ "find '$OFFSITE_PATH' -name 'mysqldump-*.gz' -mtime +$KEEP_DAYS -delete"
}

# 실패하면 이 함수에 도달하지 않아 값이 늙는다 — 그게 신호다.
write_heartbeat() {
    [ -d "$METRICS_DIR" ] || return 0
    local t="$METRICS_DIR/backup_offsite.prom"
    {
        echo "# HELP csereal_backup_offsite_last_success_timestamp_seconds 마지막 오프사이트 복사 성공 시각"
        echo "# TYPE csereal_backup_offsite_last_success_timestamp_seconds gauge"
        echo "csereal_backup_offsite_last_success_timestamp_seconds $(date +%s)"
        echo "# HELP csereal_backup_offsite_files 원격에 보관 중인 백업 파일 수"
        echo "# TYPE csereal_backup_offsite_files gauge"
        echo "csereal_backup_offsite_files $TOTAL"
    } >"$t.tmp"
    mv "$t.tmp" "$t"
}

copy_new
prune_remote
write_heartbeat
