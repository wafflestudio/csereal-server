# 모니터링

앱의 `/actuator/prometheus` 를 15초마다 긁어 Grafana 로 본다. 앱 배포와 분리된 별도
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
# Grafana     http://localhost:3001   (admin / GRAFANA_ADMIN_PASSWORD)
# Prometheus  http://localhost:9090
```

`GRAFANA_ADMIN_PASSWORD` 는 호스트의 `monitoring/.env` 에서 온다. 없으면 `admin` 으로
뜨니 최초 접속 후 반드시 바꿀 것.

## 대시보드

데이터소스와 대시보드 모두 provisioning 으로 자동 등록된다. 손댈 것 없이 열면 된다.

- `grafana/provisioning/datasources/prometheus.yml` — Prometheus 연결. `uid: prometheus`
  로 못박아 뒀다(안 주면 서버마다 uid 가 달라져 대시보드 패널이 전부 빈다).
- `grafana/dashboards/csereal-server.json` — 기본 대시보드. 패널 11 개.

패널 질의는 **prod 에서 실제로 값이 나오는 것만** 골랐다. 히스토그램 버킷
(`http_server_requests_seconds_bucket`)과 tomcat 스레드 지표는 이 앱에 없어서 뺐다 —
그래서 응답시간은 p95 가 아니라 평균이다. p95 가 필요하면 앱에
`management.metrics.distribution.percentiles-histogram.http.server.requests: true` 를
켜야 한다(시계열 수가 크게 는다).

⚠️ provisioning 대시보드는 **UI 에서 고쳐도 재기동하면 파일 내용으로 되돌아간다**
(`allowUiUpdates: false`). 실험은 UI 에서 새 대시보드를 만들어 하고, 쓸 만해지면
JSON 을 뽑아 `grafana/dashboards/` 에 커밋한다.

## 이력

- 2026-09-06 이전: `green:8080` 을 긁도록 돼 있었는데 앱과 프로메테우스가 서로 다른
  도커 네트워크에 있어 **30일간 스크랩 성공이 0회**였다. 앱 지표가 하나도 안 쌓였고
  아무도 몰랐다(소비자가 없었다). 스크랩 대상을 호스트 게이트웨이로 바꿔 고쳤다.
- 같은 날: `0.0.0.0:9090` 이라 학외에서 무인증으로 `/api/v1/*` 이 응답하던 것을 막았다.
- 그전까지 이 스택은 어느 레포에도 없이 호스트에만 있었다. 그래서 위 두 고장이
  조용히 유지됐다.

## 호스트로 올릴 때 함정

macOS 에서 `tar` 로 보내면 확장 속성 때문에 `._` 로 시작하는 AppleDouble 파일이 함께
간다. Grafana 는 provisioning 디렉터리의 **모든 파일**을 설정으로 읽으므로
`._prometheus.yml`(바이너리)을 만나 `yaml: control characters are not allowed` 로
크래시 루프에 빠진다. 보낼 때 `COPYFILE_DISABLE=1` 을 주거나, 받은 쪽에서
`find ~/monitoring -name "._*" -delete` 로 지운다.
