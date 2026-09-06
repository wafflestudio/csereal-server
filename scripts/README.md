# 호스트 스크립트

**여기 남은 건 앱 감시 하나뿐이다.** 예약 실행되는 나머지(DB 백업)는 `ops` 컨테이너로
옮겼다 — `../compose.ops.yml` 과 `../ops/` 참고.

| 파일 | 호스트 위치 | 스케줄 |
|---|---|---|
| `monitor-restart.sh` | `~/scripts/monitor-restart.sh` | 매분 |
| `crontab` | `/var/spool/cron/crontabs/waffle` | — |
| `logrotate.conf` | `/etc/logrotate.d/csereal` | — |

## 왜 이것만 호스트에 남았나

앱 감시는 **prod 의 유일한 자동복구 수단**이다. 직접 만든 컨테이너 안에 넣으면
"감시자가 죽으면 아무도 모른다" 는 새 실패 모드가 생긴다. compose 의 healthcheck
(`start_period` 가 이 스크립트의 `GRACE` 를 대체한다) + stock `autoheal` 이미지로
가는 게 맞고, 그건 prod 동작을 바꾸는 일이라 따로 본다. 그때 이 디렉터리는 비워진다.

## 올리기

```bash
COPYFILE_DISABLE=1 tar czf - scripts | ssh -p 9122 waffle@<host> 'tar xzf - -C ~'
ssh -p 9122 waffle@<host> 'chmod +x ~/scripts/*.sh && crontab ~/scripts/crontab'
```

`COPYFILE_DISABLE=1` 은 macOS `tar` 가 `._` AppleDouble 파일을 함께 보내는 것을 막는다.
