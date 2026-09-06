# 예약 실행되는 운영 작업(백업 등)을 담는 컨테이너.
#
# 왜 컨테이너인가: 전엔 스크립트·crontab·logrotate 설정이 전부 호스트에만 있었고,
# "호스트에서 고치지 마세요" 라는 주석에 기대는 구조였다. 이미지에 구우면 호스트에서
# 고칠 수가 없다. 의존성(docker CLI·rclone)도 호스트를 더럽히지 않는다.
FROM alpine:3.20

# docker-cli: DB 컨테이너 안 정품 mysqldump 를 부르려면 필요하다(소켓은 compose 가 마운트).
# rclone:     백업을 호스트 밖으로 복사할 때 쓴다.
# tzdata:     스케줄과 파일명 날짜가 KST 기준이어야 한다.
RUN apk add --no-cache bash docker-cli rclone tzdata coreutils findutils

COPY db-backup.sh /opt/ops/db-backup.sh
COPY crontab /etc/crontabs/root
RUN chmod +x /opt/ops/*.sh

# busybox crond. -f 포그라운드, -d 8 로 로그를 stdout 으로 보낸다
# (도커 로깅 드라이버가 받으므로 호스트 logrotate 설정이 필요 없다).
CMD ["crond", "-f", "-d", "8"]
