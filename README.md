# csereal-server

cse.snu.ac.kr 백엔드

## 로컬 실행

```bash
docker compose -f compose.yml -f compose.local.yml up -d --wait backend
```

`compose.yml` 은 서비스 목록만 담으니 override와 함께 사용합니다.

## CI/CD

`develop` → staging, `main` → production.

GitHub 시크릿은 `SSH_KEY` 하나이며 나머지는 호스트에 둡니다.

호스트의 `~/secrets/`에 아래 환경변수를 둡니다.

| | |
|---|---|
| `app.env` | `MYSQL_ROOT_PASSWORD` `MYSQL_USER` `MYSQL_PASSWORD` `MYSQL_DATABASE` `OIDC_CLIENT_SECRET`(prod만) |
| `monitoring.env` | `GF_SECURITY_ADMIN_PASSWORD` · `GF_SMTP_*`. prod만 |
| `certs/` | TLS 인증서·키. `main.env` 의 경로와 맞아야 합니다. prod만 |
| `backup_offsite` | 백업용 SSH 개인키. prod만 |

## DB 백업 복원

백업은 ops 컨테이너가 매일 자정 호스트 `~/database/backup/` 에 남깁니다([`ops/db-backup.sh`](ops/db-backup.sh)).

```bash
docker run -d --name restore-test -e MYSQL_ROOT_PASSWORD=x -e MYSQL_DATABASE=csereal mysql:8.0
gunzip -c ~/database/backup/mysqldump-YYYY-MM-DD.gz | docker exec -i restore-test mysql -uroot -px csereal
```
