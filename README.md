# csereal-server

컴퓨터공학부 홈페이지 백엔드. Spring Boot(Kotlin) · MySQL · Elasticsearch.

## 로컬 실행

```bash
docker compose -f compose.yml -f compose.local.yml up -d --wait backend
```

`compose.yml` 은 서비스 목록만 담으니 override와 함께 사용합니다. 

## 배포

`develop` → staging, `main` → production. 빌드는 러너가 아니라 대상 호스트에서
합니다([`ops/host-deploy.sh`](ops/host-deploy.sh)). 대상별 설정은
[`.github/deploy-targets/`](.github/deploy-targets/).

## 새 호스트에 필요한 것

GitHub 시크릿은 `SSH_KEY` 하나이며 나머지는 호스트에 둡니다. 

| | |
|---|---|
| `~/app/secrets.env` | `MYSQL_ROOT_PASSWORD` `MYSQL_USER` `MYSQL_PASSWORD` `MYSQL_DATABASE` `OIDC_CLIENT_SECRET`(prod만) |
| `~/monitoring/.env` | `GRAFANA_ADMIN_PASSWORD` · `GF_SMTP_*` |
| `~/proxy/caddy/certs/` | TLS 인증서·키. `main.env` 의 경로와 맞아야 한다 |
| `~/ops-stack/secrets/backup_offsite` | 오프사이트 백업용 SSH 개인키 |
