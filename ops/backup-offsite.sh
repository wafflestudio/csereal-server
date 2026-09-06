#!/bin/bash
# 백업을 다른 호스트로 복사한다. ops 컨테이너 안에서 crond 가 부른다.
#
# 왜 필요한가: 백업이 원본 DB 와 같은 디스크(/dev/sda2)에 있으면 그 디스크가 죽을 때
# 같이 죽는다. 그건 백업이 아니라 스냅샷이다.
#
# 왜 rsync 가 아닌가: rsync 의 강점은 "바뀐 파일의 델타만 보내는 것"인데 백업 파일은
# 한 번 만들어지면 절대 바뀌지 않는다. 매일 새 파일 하나가 늘 뿐이라 아낄 델타가 없다.
# 그리고 rsync 는 **받는 쪽에도 설치**돼 있어야 한다. 도착지는 단순할수록 좋다 —
# 요구하는 게 많을수록 "준비 안 된 채로 실패"할 여지가 는다. 여기서는 sshd 만 있으면 된다.
#
# 왜 staging 호스트인가: 클라우드 스토리지도 되지만 인증 갱신이 따라온다. staging 은 이미
# 있고 SSH 키 하나로 끝나며 prod 와 물리적으로 다른 기계다.
# ⚠️ 다만 같은 사람이 관리하는 인프라라 "완전한 독립"은 아니다. 계정·조직 사고까지
#    대비하려면 별도 클라우드가 낫다.
set -euo pipefail

SRC="${BACKUP_DIR:-/backup}"
METRICS_DIR="${METRICS_DIR:-/metrics}"
KEY="${OFFSITE_KEY:-/secrets/backup_offsite}"
KEEP_DAYS="${KEEP_DAYS:-30}"

: "${OFFSITE_HOST:?OFFSITE_HOST 가 필요하다 (예: ubuntu@168.107.16.249)}"
DIR="${OFFSITE_PATH:-db-backup-from-prod}"
PORT="${OFFSITE_PORT:-22}"

[ -r "$KEY" ] || {
    echo "SSH 키를 읽을 수 없다: $KEY" >&2
    exit 1
}

# accept-new: 첫 접속의 호스트 키는 받아들이되 이후 변경은 거부한다
# (no 로 두면 중간자 공격을 영영 못 잡는다).
ssh_() { ssh -i "$KEY" -p "$PORT" -o StrictHostKeyChecking=accept-new \
    -o ConnectTimeout=20 -o BatchMode=yes "$OFFSITE_HOST" "$@"; }

ssh_ "mkdir -p '$DIR'"
REMOTE=$(ssh_ "ls -1 '$DIR' 2>/dev/null || true")

copied=0
for f in "$SRC"/mysqldump-*.gz; do
    [ -e "$f" ] || continue
    b=$(basename "$f")
    printf '%s\n' "$REMOTE" | grep -qxF "$b" && continue

    # .part 로 받고 성공했을 때만 최종 이름으로 옮긴다. 중간에 끊기면 조각이
    # 정상 파일처럼 남지 않는다(로컬 덤프 스크립트와 같은 규율).
    ssh_ "cat > '$DIR/$b.part' && mv '$DIR/$b.part' '$DIR/$b'" <"$f"

    local_size=$(stat -c %s "$f")
    remote_size=$(ssh_ "stat -c %s '$DIR/$b'")
    [ "$local_size" = "$remote_size" ] || {
        echo "크기 불일치: $b (로컬 $local_size / 원격 $remote_size)" >&2
        exit 1
    }
    echo "복사: $b ($((local_size / 1024 / 1024))MB)"
    copied=$((copied + 1))
done

# 보관 정책을 한 곳에서만 정한다. 양쪽이 따로 늙으면 언젠가 어긋난다.
ssh_ "find '$DIR' -name 'mysqldump-*.gz' -mtime +$KEEP_DAYS -delete"

TOTAL=$(ssh_ "ls -1 '$DIR'/mysqldump-*.gz 2>/dev/null | wc -l")
echo "$(date -Is) 오프사이트 복사 완료 → $OFFSITE_HOST:$DIR (새로 $copied 개, 총 $TOTAL 개)"

# 하트비트. 실패하면 이 줄에 도달하지 않아 값이 늙는다 — 그게 신호다.
if [ -d "$METRICS_DIR" ]; then
    T="$METRICS_DIR/backup_offsite.prom"
    {
        echo "# HELP csereal_backup_offsite_last_success_timestamp_seconds 마지막 오프사이트 복사 성공 시각"
        echo "# TYPE csereal_backup_offsite_last_success_timestamp_seconds gauge"
        echo "csereal_backup_offsite_last_success_timestamp_seconds $(date +%s)"
        echo "# HELP csereal_backup_offsite_files 원격에 보관 중인 백업 파일 수"
        echo "# TYPE csereal_backup_offsite_files gauge"
        echo "csereal_backup_offsite_files $TOTAL"
    } >"$T.tmp"
    mv "$T.tmp" "$T"
fi
