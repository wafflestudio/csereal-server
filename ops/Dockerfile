# 예약 실행되는 운영 작업(백업)을 담는 컨테이너.
FROM alpine:3.20

# docker-cli: DB 컨테이너 안 mysqldump 호출  ·  openssh-client: 오프사이트 복사
# tzdata: 스케줄과 파일명 날짜가 KST 기준
RUN apk add --no-cache bash docker-cli openssh-client tzdata coreutils findutils

COPY db-backup.sh backup-offsite.sh /opt/ops/
RUN chmod +x /opt/ops/*.sh

# crond 스케줄.  분 시 일 월 요일  명령
RUN printf '%s\n' \
    '0 0 * * * /opt/ops/db-backup.sh' \
    '30 0 * * * /opt/ops/backup-offsite.sh' >/etc/crontabs/root

# -f 포그라운드, -d 8 로 로그를 stdout 으로
CMD ["crond", "-f", "-d", "8"]
