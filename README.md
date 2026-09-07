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

| 브랜치 | 환경 |
|---|---|
| `develop` | staging (aarch64) |
| `main` | production (x86_64) |

`deploy.yaml` 한 파일이 두 환경을 담당한다. 이미지 태그는 **소스 내용 해시**이고
매니페스트는 amd64·arm64 둘 다 담는다. 그래서 같은 소스면 빌드를 건너뛴다 —
develop 에서 만든 이미지를 main 승격 때 그대로 쓴다.

배포된 버전은 호스트 `~/app/.env` 의 `IMAGE_TAG` 에 적힌다. **롤백은 그 값을 옛 해시로
바꾸고 `up -d`** — 다시 빌드할 필요가 없다.

## 설정값

**비밀이 아닌 것은 시크릿에 넣지 않는다** — 호스트·포트·도메인은 설정이지 비밀이 아니고,
시크릿에 넣으면 로그에서 `***` 로 가려져 디버깅만 어려워진다.

**[`.github/deploy-targets/`](.github/deploy-targets/)** — 브랜치별 배포 대상.
`SSH_HOST` · `SSH_PORT` · `SSH_USER` · `URL` · `PROFILE` · `CADDYFILE`

**GitHub 시크릿**

| | |
|---|---|
| `MYSQL_ROOT_PASSWORD` `MYSQL_USER` `MYSQL_PASSWORD` `MYSQL_DATABASE` | 양쪽 |
| `OIDC_CLIENT_SECRET` | prod (OIDC 등록이 prod 프로파일에만 있다) |
| `CERTIFICATE` `PRIVATE_KEY` | prod 엣지 (staging 은 nip.io 라 Caddy 가 자체 발급) |
| `SSH_KEY` | 접속 키. staging Environment 의 값이 레포 수준 값(prod)을 덮어쓴다 |

Environment(`production`·`staging`)는 **시크릿 격리** 용도로만 쓴다 — staging 잡이
production 키를 읽을 수 없다.

**호스트 `.env`** (git 에 없음)

| 파일 | |
|---|---|
| `~/app/.env` · `~/proxy/.env` | 배포 워크플로가 매번 덮어쓴다. 호스트에서 고치면 사라진다 |
| `~/database/.env` | 손으로. DB 계정 — ops 컨테이너가 읽는다 |
| `~/monitoring/.env` | 손으로. `GRAFANA_ADMIN_PASSWORD`(없으면 기동 거부) · `GF_SMTP_*` |
