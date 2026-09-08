# 모니터링

백엔드의 `/actuator/prometheus` 와 프론트의 `:9464/metrics` 를 15초마다 긁어 Grafana 로 본다. 앱 배포와 분리된 별도
compose 프로젝트다(앱 배포 때마다 대시보드가 재시작되면 곤란하다).

## 띄우기

호스트에서:

```bash
docker compose -f monitoring/compose.yml up -d
```

배포 워크플로가 없다 — 바뀌는 일이 드물어 손으로 올린다. 대신 이 디렉터리가 정본이니
호스트에서 직접 고치지 말 것(다음에 여기서 올리면 덮인다).

## 보기

두 포트 모두 **loopback 에만** 바인딩돼 있다. 호스트에 방화벽이 없어서(ufw 미설치,
iptables INPUT ACCEPT) 게시 포트가 곧 공개 포트이기 때문이다. SSH 터널로 본다.

```bash
ssh -p 9122 -L 3001:localhost:3001 -L 9090:localhost:9090 waffle@<prod-host>
# Grafana     http://localhost:3001   (admin / GF_SECURITY_ADMIN_PASSWORD)
# Prometheus  http://localhost:9090
```

`GF_SECURITY_ADMIN_PASSWORD` 는 호스트의 `~/secrets/monitoring.env` 에서 온다(`env_file`).
파일이 없으면 compose 가 기동을 거부한다.

⚠️ **이 값은 Grafana DB 가 처음 만들어질 때만 적용된다.** 이미 admin 유저가 있는 볼륨에
   나중에 값을 넣거나 바꿔도 **무시된다** — 로그인하면 `invalid password` 만 나온다.
   이미 만들어진 뒤에 바꾸려면 CLI 로 재설정한다:

   ```bash
   docker exec -it grafana grafana cli admin reset-admin-password '새비밀번호'
   ```

   `~/secrets/monitoring.env` 값도 같이 맞춰 두면 나중에 볼륨을 새로 만들 때 어긋나지 않는다.
   아이디는 항상 `admin` 이다(`GF_SECURITY_ADMIN_USER` 로 바꿀 수 있다).

## 대시보드

데이터소스와 대시보드 모두 provisioning 으로 자동 등록된다. 손댈 것 없이 열면 된다.

- `grafana/provisioning/datasources/prometheus.yml` — Prometheus 연결. `uid: prometheus`
  로 못박아 뒀다(안 주면 서버마다 uid 가 달라져 대시보드 패널이 전부 빈다).
- `grafana/dashboards/csereal-server.json` — 백엔드 대시보드. 패널 11 개.
- `grafana/dashboards/csereal-web.json` — 프론트 대시보드. 프론트 레포 `server.ts` 가 내는
  지표라 `page` 라벨(정규화한 URL)로 나눈다.

패널 질의는 **prod 에서 실제로 값이 나오는 것만** 골랐다. 히스토그램 버킷
(`http_server_requests_seconds_bucket`)과 tomcat 스레드 지표는 이 앱에 없어서 뺐다 —
그래서 응답시간은 p95 가 아니라 평균이다. p95 가 필요하면 앱에
`management.metrics.distribution.percentiles-histogram.http.server.requests: true` 를
켜야 한다(시계열 수가 크게 는다).

⚠️ provisioning 대시보드는 **UI 에서 고쳐도 재기동하면 파일 내용으로 되돌아간다**
(`allowUiUpdates: false`). 실험은 UI 에서 새 대시보드를 만들어 하고, 쓸 만해지면
JSON 을 뽑아 `grafana/dashboards/` 에 커밋한다.

## 경보

Slack appender 를 걷어내면서 이 시스템에 알림 수단이 없어졌다. 그 자리를 Grafana 경보가
메운다. 규칙과 수신처 모두 `grafana/provisioning/alerting/` 에 코드로 있다.

| 경보 | 조건 | 비고 |
|---|---|---|
| 앱이 응답하지 않는다 | `up{job="spring"} == 0`, 2분 | Slack 으로는 못 잡던 것 — 앱이 죽으면 로그도 안 나온다 |
| 프론트가 응답하지 않는다 | `up{job="frontend"} == 0`, 2분 | 프론트가 죽어도 백엔드는 멀쩡해 다른 규칙엔 안 걸린다 |
| 프론트 5xx 응답이 나가고 있다 | 5xx 발생, 5분 | SSR 실패·백엔드 호출 실패 |
| 에러 로그가 늘고 있다 | ERROR 초당 0.1건 초과, 5분 | Slack appender 의 대체 |
| 5xx 응답이 나가고 있다 | 5xx 발생, 5분 | 사용자가 겪는 실패 |
| DB 백업이 25시간 넘게 성공 안 함 | 하트비트가 늙음 | ops 컨테이너가 남기는 지표 |
| 디스크 여유 10GB 미만 | node-exporter | |

**Slack 과 달라진 점**: ERROR 한 건마다 울리지 않고 비율·지속시간으로 판단한다.
소음이 줄지만 **"무슨 에러인지"는 안 온다** — 알림을 받고 로그를 보러 가야 한다.
내용까지 받으려면 로그 수집기(Loki 등)가 따로 필요하다.

### 메일 발송 설정

수신처는 `yeolyi1310@gmail.com` 이다. **SMTP 자격증명이 없으면 Grafana 는 조용히 메일을
안 보낸다**(로그에만 남는다). 호스트 `~/secrets/monitoring.env` 에 넣는다:

```
GF_SMTP_ENABLED=true
GF_SMTP_HOST=smtp.gmail.com:587
GF_SMTP_USER=<보내는 계정>
GF_SMTP_PASSWORD=<Gmail 앱 비밀번호>
GF_SMTP_FROM_ADDRESS=<보내는 계정>
```

Gmail 은 일반 비밀번호가 아니라 **앱 비밀번호**가 필요하다(2단계 인증 켠 뒤 발급).

## 이력

- 같은 날: `0.0.0.0:9090` 이라 학외에서 무인증으로 `/api/v1/*` 이 응답하던 것을 막았다.
- 그전까지 이 스택은 어느 레포에도 없이 호스트에만 있었다. 그래서 위 두 고장이
  조용히 유지됐다.

## 호스트로 올릴 때 함정

macOS 에서 `tar` 로 보내면 확장 속성 때문에 `._` 로 시작하는 AppleDouble 파일이 함께
간다. Grafana 는 provisioning 디렉터리의 **모든 파일**을 설정으로 읽으므로
`._prometheus.yml`(바이너리)을 만나 `yaml: control characters are not allowed` 로
크래시 루프에 빠진다. 보낼 때 `COPYFILE_DISABLE=1` 을 주거나, 받은 쪽에서
`find ~/monitoring -name "._*" -delete` 로 지운다.
