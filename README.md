# csereal-server

컴퓨터공학부 홈페이지 리뉴얼 프로젝트 백엔드. Spring Boot(Kotlin) · MySQL · Elasticsearch.

```bash
# 로컬
docker compose -f compose.yml -f compose.local.yml up -d --wait backend
```

`compose.yml` 은 단독으로 쓰지 않는다 — 서비스 목록만 담고 override 가 나머지를 채운다.
별도 프로젝트: `compose.caddy.yml`(엣지) · [`compose.ops.yml`](ops/)(백업) ·
[`monitoring/compose.yml`](monitoring/)(Prometheus·Grafana).

## 배포

| 브랜치 | 환경 | 러너 |
|---|---|---|
| `develop` | staging | `ubuntu-24.04-arm` (호스트 aarch64) |
| `main` | production | `ubuntu-latest` (호스트 x86_64) |

`deploy.yaml` 한 파일이 두 환경을 담당한다. ⚠️ 러너 아키텍처가 호스트와 어긋나면
실행할 수 없는 이미지가 나온다.

## 설정값

**비밀이 아닌 것은 시크릿에 넣지 않는다** — 호스트·포트·도메인은 설정이지 비밀이 아니고,
시크릿에 넣으면 로그에서 `***` 로 가려져 디버깅만 어려워진다.

**[`.github/deploy-targets/`](.github/deploy-targets/)** — 브랜치별 배포 대상.
`SSH_HOST` · `SSH_PORT` · `SSH_USER` · `URL` · `PROFILE` · `CADDYFILE`

**GitHub 시크릿 — `SSH_KEY` 하나뿐이다**

호스트 접속 키. Environment(`production`·`staging`)로 격리한다 — staging 잡이
production 키를 읽을 수 없다.

DB 계정·OIDC 는 GitHub 에 두지 않는다. 거의 바뀌지 않는 값이라 매 배포마다 날라야 할
이유가 없고, 호스트가 이미 갖고 있다(아래).

**호스트 `~/app/secrets.env`** — 사람이 한 번 만든다. git 에도 GitHub 에도 없다.

```
MYSQL_ROOT_PASSWORD=…
MYSQL_USER=…
MYSQL_PASSWORD=…
MYSQL_DATABASE=…
OIDC_CLIENT_SECRET=…   # prod 만. OIDC 등록이 prod 프로파일에만 있다
```

없거나 키가 빠지면 배포가 명확한 메시지로 멈춘다. 권한은 `600`.
호스트를 새로 세우면 이 파일부터 만들어야 한다.

**호스트 `.env`** (git 에 없음)

| 파일 | |
|---|---|
| `~/app/secrets.env` | **손으로.** 위 참고 |
| `~/app/.env` · `~/proxy/.env` | `host-deploy.sh` 가 매 배포마다 다시 만든다. 고치면 사라진다 |
| `~/database/.env` | 손으로. DB 계정 — ops 컨테이너가 읽는다 |
| `~/monitoring/.env` | 손으로. `GRAFANA_ADMIN_PASSWORD`(없으면 기동 거부) · `GF_SMTP_*` |
