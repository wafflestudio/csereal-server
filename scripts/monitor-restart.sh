#!/bin/bash

# ⚠️ 이 파일이 정본이다. 호스트(~/app/monitor_restart.sh)에서 직접 고치지 말 것.
#
# 왜 별도 스크립트인가: compose 의 `restart: always` 는 프로세스가 "종료될 때"만 듣는다.
# JVM 이 살아 있으면서 응답만 안 하는 경우(데드락·스레드풀 고갈)엔 영원히 재시작되지 않는다.
# Docker 의 HEALTHCHECK 는 상태를 표시할 뿐 unhealthy 컨테이너를 재시작해 주지 않는다
# (Swarm·k8s 는 해준다). 그 빈틈을 cron 으로 메운 것이다.
#
# TODO: compose 의 healthcheck(start_period 로 아래 GRACE 를 대체) + autoheal 컨테이너로
#       교체 검토. 그러면 이 로직이 셸이 아니라 compose 선언으로 옮겨간다.
#       prod 의 유일한 자동복구 수단이라 교체는 따로 신중히 볼 것.

# Function to echo with timestamp
log_message() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1"
}

URL="http://localhost:8080/api/helloworld"

# Timeout in seconds for curl
TIMEOUT=5

# Number of retries before restarting
MAX_RETRIES=3

# Delay between retries in seconds
RETRY_DELAY=5

CONTAINER="csereal_server_green"

# Grace period after (re)start, in seconds.
# A restart during boot can kill a Flyway migration halfway through, which leaves a
# failed row in flyway_schema_history and puts the app in a permanent crash loop.
# That happened on 2026-09-04 (V20): boot took 23s, this script gave it 10s.
GRACE=180

started_at=$(docker inspect -f '{{.State.StartedAt}}' "$CONTAINER" 2>/dev/null || true)
if [ -n "$started_at" ]; then
    started_epoch=$(date -d "$started_at" +%s 2>/dev/null || echo 0)
    age=$(( $(date +%s) - started_epoch ))
    if [ "$started_epoch" -gt 0 ] && [ "$age" -lt "$GRACE" ]; then
        log_message "Container started ${age}s ago (grace ${GRACE}s). Skipping check."
        exit 0
    fi
fi

check_server() {
    curl --output /dev/null --silent --head --fail --max-time $TIMEOUT "$URL"
}

for ((i=1; i<=MAX_RETRIES; i++)); do
    if check_server; then
        log_message "Server is running fine."
        exit 0
    else
        log_message "Server check failed. Attempt $i of $MAX_RETRIES."
        if [ $i -lt $MAX_RETRIES ]; then
            log_message "Waiting $RETRY_DELAY seconds before next check..."
            sleep $RETRY_DELAY
        fi
    fi
done

log_message "Server is down. Restarting..."

# 컨테이너 이름으로 직접 재시작한다.
# 전엔 `docker compose -f <파일> restart` 였는데, 그 compose 파일 경로를 하드코딩하고 있었다.
# 배포 스택 파일이 docker-compose-backend.yml → compose.yml + compose.prod.yml 로 바뀌면서
# 그 경로가 유령이 됐다(호스트엔 남아 있어 우연히 동작할 뿐이다).
# 컨테이너 이름만 알면 되는 일이라 compose 의존을 없앤다.
docker restart "$CONTAINER"

log_message "Server restarted."
