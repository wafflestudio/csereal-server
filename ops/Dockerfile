# 예약 실행되는 운영 작업(백업 등)을 담는 컨테이너.
#
# 왜 컨테이너인가: 전엔 스크립트·crontab·logrotate 설정이 전부 호스트에만 있었고,
# "호스트에서 고치지 마세요" 라는 주석에 기대는 구조였다. 이미지에 구우면 호스트에서
# 고칠 수가 없다. 의존성(docker CLI·rclone)도 호스트를 더럽히지 않는다.
FROM alpine:3.20

# docker-cli:      DB 컨테이너 안 정품 mysqldump 를 부르려면 필요하다(소켓은 compose 가 마운트).
# openssh-client: 백업을 다른 호스트로 복사한다. rsync 는 쓰지 않는다 — 백업 파일은
#                 한 번 만들어지면 안 바뀌어 델타 전송이 아낄 게 없고, rsync 는 받는
#                 쪽에도 설치돼 있어야 한다. 도착지는 sshd 만 있으면 되게 둔다.
# tzdata:          스케줄과 파일명 날짜가 KST 기준이어야 한다.
RUN apk add --no-cache bash docker-cli openssh-client tzdata coreutils findutils

COPY db-backup.sh backup-offsite.sh /opt/ops/
RUN chmod +x /opt/ops/*.sh

# busybox crond 스케줄.  분 시 일 월 요일  명령
# 덤프는 매일 자정 KST(TZ 는 compose 가 넣는다). 오프사이트 복사는 덤프가 끝날 시간을
# 두고 30분 뒤 — 백업이 원본과 같은 디스크에만 있으면 그 디스크가 죽을 때 같이 죽는다.
RUN printf '%s\n' \
    '0 0 * * * /opt/ops/db-backup.sh' \
    '30 0 * * * /opt/ops/backup-offsite.sh' >/etc/crontabs/root

# busybox crond. -f 포그라운드, -d 8 로 로그를 stdout 으로 보낸다
# (도커 로깅 드라이버가 받으므로 호스트 logrotate 설정이 필요 없다).
CMD ["crond", "-f", "-d", "8"]
