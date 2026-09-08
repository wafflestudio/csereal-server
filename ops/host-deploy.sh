#!/usr/bin/env bash
# 호스트에서 빌드하고 배포한다. deploy.yaml 이 작업공간을 배포할 커밋으로 맞춘 뒤
# 그 안에서 이 스크립트를 실행한다.
set -euo pipefail

: "${GIT_SHA:?GIT_SHA 가 필요하다}"
# 아래는 .github/deploy-targets/<브랜치>.env 에서 온다.
: "${PROFILE:?PROFILE 이 필요하다}"
: "${URL:?URL 이 필요하다}"
: "${CADDYFILE:?CADDYFILE 이 필요하다}"

WORKSPACE=$PWD
APP_DIR=$HOME/app
PROXY_DIR=$HOME/proxy
GRADLE_VOLUME=csereal-gradle
TAG=${GIT_SHA:0:12}
SECRETS_FILE=$HOME/secrets/app.env

say() { echo "▸ $*"; }

build_jar() {
    say "gradle bootJar"
    docker run --rm -v "$GRADLE_VOLUME:/root/.gradle" -v "$WORKSPACE:/src" -w /src \
        eclipse-temurin:21-jdk ./gradlew --no-daemon bootJar -x test
}

build_images() {
    say "앱 이미지: csereal-server:$TAG"
    docker build -q --build-arg JAR_STAGE=prebuilt --build-arg GIT_SHA="$GIT_SHA" \
        -t "csereal-server:$TAG" "$WORKSPACE"

    # nori 는 공식 이미지에 없는 플러그인이라 검색 서버도 우리가 만든다. 
    # 해시를 확인해 Dockerfile.es 가 그대로면 다시 만들지 않는다.
    SEARCH_TAG=$(git ls-tree HEAD -- Dockerfile.es | sha256sum | cut -c1-12)
    if docker image inspect "csereal-search:$SEARCH_TAG" >/dev/null 2>&1; then
        say "검색 이미지 그대로: $SEARCH_TAG"
    else
        say "검색 이미지: $SEARCH_TAG"
        docker build -q -f "$WORKSPACE/Dockerfile.es" -t "csereal-search:$SEARCH_TAG" "$WORKSPACE"
    fi
}

# 호스트의 비밀 + 레포의 설정 + 이번 빌드의 태그를 합쳐 compose 가 읽을 .env 를 만든다.
write_env() {
    local dir=$1; shift
    [ -f "$SECRETS_FILE" ] || {
        echo "✗ $SECRETS_FILE 이 없다. README 의 '호스트 .env' 를 보고 만들 것." >&2
        exit 1
    }
    for key in MYSQL_ROOT_PASSWORD MYSQL_USER MYSQL_PASSWORD MYSQL_DATABASE; do
        grep -q "^$key=" "$SECRETS_FILE" || { echo "✗ $SECRETS_FILE 에 $key 가 없다" >&2; exit 1; }
    done
    {
        echo "# host-deploy.sh 가 매 배포마다 다시 만든다. 여기서 고치지 말 것."
        echo "# 비밀은 secrets.env 에 있다."
        for kv in "$@"; do echo "$kv"; done
    } >"$dir/.env"
}

deploy_app() {
    # compose 프로젝트 디렉터리는 ~/app 이다(프로젝트 이름과 상대 볼륨 경로가 거기 묶여 있다).
    cp "$WORKSPACE/compose.yml" "$WORKSPACE/compose.prod.yml" "$APP_DIR/"
    cd "$APP_DIR"
    write_env "$APP_DIR" "PROFILE=$PROFILE" "URL=$URL" "IMAGE_TAG=$TAG" "SEARCH_TAG=$SEARCH_TAG"
    cat "$SECRETS_FILE" >>.env

    say "compose up"
    # --wait 은 healthcheck 가 healthy 가 될 때까지 기다린다. 없으면 앱이 크래시 루프여도 배포가 초록불로 끝난다.
    docker compose -f compose.yml -f compose.prod.yml up -d --wait --remove-orphans

    # 의도한 커밋이 실제로 떴는지 본다. 
    # 이미지가 잘못 태깅됐거나 compose 가 옛 태그를 잡았다면 여기서 걸린다.
    local running
    running=$(docker inspect csereal_server --format '{{range .Config.Env}}{{println .}}{{end}}' |
        sed -n 's/^GIT_SHA=//p')
    [ "$running" = "$GIT_SHA" ] ||
        { echo "✗ 배포된 커밋이 다르다: 기대 $GIT_SHA / 실제 ${running:-없음}" >&2; exit 1; }
}

deploy_edge() {
    # 앱을 먼저 띄우고 여기로 온다. 이 아래가 실패해도 앱은 이미 서비스 중이다.
    # caddy 는 공식 이미지라 빌드할 것이 없고, reload 는 무중단이며 설정이 잘못되면
    # 적용하지 않고 옛 설정을 유지한다. 그래서 매 배포마다 돌려도 안전하다.
    cp "$WORKSPACE/compose.caddy.yml" "$PROXY_DIR/"
    mkdir -p "$PROXY_DIR/caddy"
    cp "$WORKSPACE/$CADDYFILE" "$PROXY_DIR/caddy/Caddyfile"
    cd "$PROXY_DIR"
    # 인증서 경로는 비밀이 아니라 deploy-targets 에 있다. staging 은 아예 없다.
    {
        echo "URL=$URL"
        [ -n "${CERTIFICATE:-}" ] && echo "CERTIFICATE=$CERTIFICATE"
        [ -n "${PRIVATE_KEY:-}" ] && echo "PRIVATE_KEY=$PRIVATE_KEY"
    } >.env || true

    say "caddy 반영"
    docker compose -f compose.caddy.yml up -d --remove-orphans
    for _ in $(seq 1 15); do docker exec csereal_caddy caddy version >/dev/null 2>&1 && break; sleep 2; done
    docker exec csereal_caddy caddy validate --config /etc/caddy/Caddyfile
    docker exec csereal_caddy caddy reload --config /etc/caddy/Caddyfile
}

# 옛 이미지가 커밋마다 423MB 씩 쌓인다. 최근 5개는 남겨 .env 의 IMAGE_TAG 만 바꿔
# 롤백할 수 있게 한다.
prune_old_images() {
    docker images csereal-server --format '{{.Tag}}' | tail -n +6 |
        xargs -r -I{} docker rmi "csereal-server:{}" >/dev/null 2>&1 || true
}

build_jar
build_images
deploy_app
deploy_edge
prune_old_images
say "배포 완료 — $TAG"
