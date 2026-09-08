#!/usr/bin/env bash
# 호스트에서 빌드하고 배포한다. deploy.yaml 이 작업공간을 배포할 커밋으로 맞춘 뒤
# 그 안에서 이 스크립트를 실행한다. 실행 위치 = 작업공간, GIT_SHA 필요.
set -euo pipefail

: "${GIT_SHA:?GIT_SHA 가 필요하다}"
GRADLE_VOLUME="${GRADLE_VOLUME:-csereal-gradle}"
APP_DIR="${APP_DIR:-$HOME/app}"
WORKSPACE="$PWD"
SHORT="${GIT_SHA:0:12}"
FAIL_MARKER="$WORKSPACE/.last-build-failed"
say() { echo "▸ $*"; }

# 직전 빌드가 죽었으면 증분 상태를 믿지 않는다. 반쯤 쓰인 산출물이 남아도 이력 파일은
# '최신'이라 주장해서, 다음 빌드가 조용히 옛 클래스를 섞을 수 있다.
if [ -f "$FAIL_MARKER" ]; then
    say "직전 빌드 실패 → build/ 를 비우고 처음부터"
    docker run --rm -v "$WORKSPACE:/src" -w /src alpine:3.20 rm -rf build .gradle
fi

touch "$FAIL_MARKER"
say "gradle bootJar"
docker run --rm \
    -v "$GRADLE_VOLUME:/root/.gradle" \
    -v "$WORKSPACE:/src" -w /src \
    eclipse-temurin:21-jdk ./gradlew --no-daemon bootJar -x test
rm -f "$FAIL_MARKER"

say "앱 이미지: csereal-server:$SHORT"
docker build -q \
    --build-arg JAR_STAGE=prebuilt \
    --build-arg GIT_SHA="$GIT_SHA" \
    -t "csereal-server:$SHORT" "$WORKSPACE" >/dev/null

# nori 는 공식 이미지에 없는 플러그인이라 검색 서버도 우리가 만든다.
# 태그가 내용 해시라 Dockerfile.es 가 그대로면 다시 만들지 않는다 — 다이제스트가
# 안 바뀌어야 compose 가 ES 를 재생성하지 않는다.
SEARCH_TAG=$(git ls-tree HEAD -- Dockerfile.es | sha256sum | cut -c1-12)
if docker image inspect "csereal-search:$SEARCH_TAG" >/dev/null 2>&1; then
    say "검색 이미지 그대로: $SEARCH_TAG"
else
    say "검색 이미지: $SEARCH_TAG"
    docker build -q -f "$WORKSPACE/Dockerfile.es" -t "csereal-search:$SEARCH_TAG" "$WORKSPACE" >/dev/null
fi

# compose 프로젝트 디렉터리는 ~/app 이다(프로젝트 이름과 상대 볼륨 경로가 거기 묶여 있다).
cp "$WORKSPACE/compose.yml" "$WORKSPACE/compose.prod.yml" "$APP_DIR/"
cd "$APP_DIR"
{ echo "IMAGE_TAG=$SHORT"; echo "SEARCH_TAG=$SEARCH_TAG"; } >>.env
set -a; . ./.env; set +a
say "compose up"
# down 을 쓰지 않는다 — compose 는 바뀐 서비스만 재생성하는데 down 이 그걸 무력화한다.
docker compose -f compose.yml -f compose.prod.yml up -d --remove-orphans

# 증분 빌드가 옛 산출물을 섞었다면 여기서 걸린다.
RUNNING=$(docker inspect csereal_server_green --format '{{range .Config.Env}}{{println .}}{{end}}' | sed -n 's/^GIT_SHA=//p')
[ "$RUNNING" = "$GIT_SHA" ] || { echo "✗ 배포된 커밋이 다르다: 기대 $GIT_SHA / 실제 ${RUNNING:-없음}" >&2; exit 1; }
say "배포 완료 — $SHORT"
