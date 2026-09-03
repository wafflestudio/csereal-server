#!/usr/bin/env bash
# =====================================================================================
#  archive-orphan-files.sh — 아무 데서도 참조하지 않는 업로드 파일을 보관함으로 옮긴다
# =====================================================================================
#
#  왜 필요한가
#  -----------
#  첨부·대표이미지·본문 이미지는 전부 디스크의 파일이고, DB는 그 파일명만 들고 있다.
#  글을 지우거나 이미지를 교체해도 옛 코드는 파일을 안 지웠고(소프트 삭제·교체 시 방치),
#  에디터에 이미지를 올리고 글을 저장하지 않으면 그 파일도 그대로 남는다.
#  그래서 "DB 어디서도 가리키지 않는 파일"이 디스크에 쌓인다. 이걸 골라내 옮기는 스크립트다.
#
#  절대 지우지 않는다
#  ------------------
#  `rm`이 아니라 같은 디스크 안의 archive/<날짜>/ 로 `mv` 한다. 같은 파일시스템 안의 mv는
#  데이터를 복사하지 않고 이름만 바꾸므로 순식간에 끝나고 공간도 안 든다.
#  되돌리기는 MANIFEST.txt(옮긴 파일 목록)를 보고 mv를 반대로 하면 된다.
#  충분히 지켜본 뒤 archive 디렉터리를 통째로 지우는 건 사람이 따로 결정한다.
#
#  "참조한다"의 정의
#  ------------------
#  uploads 모드(/app/files): 다음 셋의 합집합에 파일명이 있으면 참조된 것.
#    1) attachment.filename          — 게시글 첨부
#    2) main_image.filename          — 대표이미지
#    3) 모든 text 컬럼 안의 URL      — 에디터로 넣은 본문 이미지·링크 (/v1/file/<파일명>)
#       본문 이미지는 어떤 테이블에도 행이 없다. HTML 안에 URL로만 존재하므로
#       본문을 뒤져야만 찾을 수 있다. 이게 이 스크립트가 복잡해지는 유일한 이유다.
#  legacy 모드(/app/cse-files): 옛 Drupal 시절 파일. DB 행은 없고 본문 URL(/sites/default/files/<경로>)뿐이다.
#
#  사용법 (prod 호스트에서, 마이그레이션이 끝난 뒤)
#  ------------------------------------------------
#    scripts/archive-orphan-files.sh uploads            # 세기만 한다 (dry-run, 아무것도 안 바꿈)
#    scripts/archive-orphan-files.sh uploads --apply    # 실제로 옮긴다
#    scripts/archive-orphan-files.sh legacy  [--apply]
#
#  함정 — 반드시 마이그레이션 "뒤"에 돌릴 것.
#         소프트 삭제 행·학생회 행이 DB에서 지워져야 그 파일들이 비로소 "참조 없음"으로 잡힌다.
#  함정 — 에디터가 본문 이미지를 올린 순간과 글을 저장하는 순간 사이에는 그 파일을 아무도 참조하지 않는다.
#         그 틈에 이 스크립트가 돌면 방금 올린 이미지를 옮겨 버린다. 그래서 MIN_AGE_MIN(기본 60분)보다
#         새 파일은 아예 후보에서 뺀다.
#  함정 — 한글 파일명은 같은 글자라도 바이트가 두 가지(NFC/NFD)일 수 있다. 눈엔 같은데 문자열 비교는 실패한다.
#         그래서 비교는 정규화(NFC)해서 하고, 옮길 땐 디스크에 있는 원래 이름을 쓴다.
#  함정 — 연구실 소개 PDF는 lab.pdf_id 로 "반대 방향" 연결이라 attachment 쪽 FK가 전부 NULL이다.
#         FK가 비었다고 고아로 보면 틀린다. 그래서 판정은 FK가 아니라 파일명으로만 한다.
#
# =====================================================================================

# bash 안전장치 셋. -e: 명령이 실패하면 즉시 중단 / -u: 정의 안 된 변수를 쓰면 오류 / -o pipefail: 파이프 중간이 실패해도 실패로 본다.
set -euo pipefail

# ---- 인자 ------------------------------------------------------------------------
KIND=${1:?uploads | legacy}                 # 첫 인자가 없으면 이 메시지를 내고 종료한다
APPLY=false; [[ "${2:-}" == "--apply" ]] && APPLY=true   # 두 번째 인자가 --apply일 때만 실제로 옮긴다

# ---- 환경 -----------------------------------------------------------------------
# 전부 환경변수로 덮어쓸 수 있다(예: 로컬 리허설 때 DB_CONTAINER=csereal-local-db-1 DB_NAME=csereal).
DB_CONTAINER=${DB_CONTAINER:-csereal_db_container}   # MySQL이 도는 docker 컨테이너 이름
DB_NAME=${DB_NAME:-csereal_db}
MIN_AGE_MIN=${MIN_AGE_MIN:-60}                       # 이 분(分)보다 새 파일은 건드리지 않는다
STAMP=$(date +%Y%m%d)

case "$KIND" in
  uploads) FILES_DIR=${FILES_DIR:-/home/waffle/app/files};     URL_PREFIX='/v1/file/' ;;
  legacy)  FILES_DIR=${FILES_DIR:-/home/waffle/app/cse-files}; URL_PREFIX='/sites/default/files/' ;;
  *) echo "unknown kind: $KIND" >&2; exit 2 ;;
esac
ARCHIVE_DIR=${ARCHIVE_DIR:-/home/waffle/app/archive/$STAMP/$KIND}

# ---- DB 질의 헬퍼 -----------------------------------------------------------------
# 표준입력으로 받은 SQL을 컨테이너 안의 mysql에 넘긴다.
#   docker exec -i        : 표준입력을 컨테이너 안 명령에 연결한다
#   sh -c '...' "$DB_NAME": 작은따옴표 안의 $MYSQL_ROOT_PASSWORD 는 호스트가 아니라 컨테이너 안에서 풀린다(비밀번호가 밖으로 안 나온다)
#   MYSQL_PWD             : -p 옵션 대신 환경변수로 주면 "비밀번호를 명령줄에 쓰지 말라"는 경고가 출력에 섞이지 않는다
#   -N --raw              : 컬럼 머리글 없이, 이스케이프 없이 값만 한 줄에 하나씩
#   --default-character-set=utf8mb4 : 한글 파일명이 ???? 로 깨지지 않게
sql() { docker exec -i "$DB_CONTAINER" sh -c 'export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"; mysql -uroot -N --raw --default-character-set=utf8mb4 "$0"' "$DB_NAME"; }

# 중간 결과를 둘 임시 디렉터리. 스크립트가 어떻게 끝나든(오류 포함) trap이 지워 준다.
WORK=$(mktemp -d); trap 'rm -rf "$WORK"' EXIT

# ---- 1) DB가 직접 가리키는 파일명 (uploads만 해당) --------------------------------
if [[ $KIND == uploads ]]; then
  sql <<<"SELECT filename FROM attachment; SELECT filename FROM main_image;" > "$WORK/db_names.txt"
else
  : > "$WORK/db_names.txt"     # legacy는 DB 행이 없다. 빈 파일을 만들어 아래 로직을 같게 유지한다
fi

# ---- 2) 본문 안의 URL ---------------------------------------------------------------
# 어느 테이블·컬럼에 URL이 들어 있는지 미리 알 필요가 없다. information_schema(MySQL이 스키마 정보를
# 테이블처럼 보여 주는 곳)에서 text 계열 컬럼을 전부 뽑고, 컬럼마다 "URL을 포함한 행의 그 컬럼값을 SELECT"
# 하는 SQL을 *생성*한다. 즉 SQL을 만드는 SQL이다. 컬럼 이름에 예약어(key 등)가 있을 수 있어 백틱으로 감싼다.
sql <<<"SELECT CONCAT('SELECT \`', column_name, '\` FROM \`', table_name, '\` WHERE \`', column_name, '\` LIKE \"%${URL_PREFIX}%\";')
        FROM information_schema.columns
        WHERE table_schema='${DB_NAME}' AND data_type IN ('text','mediumtext','longtext','varchar');" > "$WORK/body_queries.sql"
# 생성된 SQL을 실행해 본문(HTML)을 받아 오고, 그 안에서 URL 조각만 오려낸다.
#   grep -oE  : 정규식에 맞는 부분만(-o) 확장 정규식으로(-E) 출력. 한 본문에 URL이 여러 개면 각각 한 줄
#   [^...]+   : 따옴표·공백·꺾쇠·괄호·?·# 가 나오기 전까지 = 파일명 끝까지
#   sed       : 앞의 /v1/file/ 같은 접두어를 떼서 파일명만 남긴다
#   || true   : 매치가 0건이면 grep이 실패(exit 1)로 끝나는데, 그건 오류가 아니므로 -e 에 걸리지 않게 한다
sql < "$WORK/body_queries.sql" | grep -oE "${URL_PREFIX}[^\"'[:space:]<>)?#]+" | sed "s|^${URL_PREFIX}||" > "$WORK/body_refs.txt" || true

# ---- 3) 디스크에 실제로 있는 파일 목록 ------------------------------------------------
#   -type f          : 파일만(디렉터리 제외)
#   -mmin +N         : 마지막 수정이 N분보다 오래된 것만 → 방금 올린 파일 보호
#   -printf '%P\n'   : FILES_DIR 기준 상대 경로만 출력 (legacy는 하위 폴더가 있어 경로가 필요하다)
( cd "$FILES_DIR" && find . -type f -mmin +"$MIN_AGE_MIN" -printf '%P\n' ) > "$WORK/disk.txt"
# 보호 때문에 후보에서 뺀 "너무 새 파일"도 센다. 이 수가 0이 아니면 누군가 지금 글을 쓰는 중일 수 있다 —
# 그 파일들은 다음 실행 때 다시 판정되니 놓치는 건 아니지만, 결과를 읽는 사람이 알고 있어야 한다.
SKIPPED_RECENT=$( cd "$FILES_DIR" && find . -type f -mmin -"$MIN_AGE_MIN" | wc -l )

# ---- 4) 대조 ----------------------------------------------------------------------------
# 참조 집합(1+2)에 없는 디스크 파일이 고아다. 문자열 처리가 필요한 부분이라 python3에 맡긴다.
#   unquote  : 본문 URL은 %EA%B0%80 처럼 퍼센트 인코딩돼 있을 수 있다 → 원래 글자로
#   NFC      : 한글 정규화(위 "함정" 참고). 비교에만 쓰고, 출력은 디스크의 원래 이름(name)이다
#   surrogateescape : 디스크에 utf-8이 아닌 이름이 있어도 죽지 않고 그대로 돌려준다
python3 - "$WORK" > "$WORK/orphans.txt" <<'PY'
import sys, unicodedata, urllib.parse
w = sys.argv[1]
nfc = lambda s: unicodedata.normalize('NFC', s)
ref = set()
for f in ('db_names.txt', 'body_refs.txt'):
    for line in open(f'{w}/{f}', encoding='utf-8', errors='replace'):
        s = line.rstrip('\n')
        if s: ref.add(nfc(urllib.parse.unquote(s)))
for line in open(f'{w}/disk.txt', encoding='utf-8', errors='surrogateescape'):
    name = line.rstrip('\n')
    if name and nfc(name) not in ref:
        print(name)
PY

# ---- 5) 보고 --------------------------------------------------------------------------
TOTAL=$(wc -l < "$WORK/disk.txt"); ORPHANS=$(wc -l < "$WORK/orphans.txt"); REFS=$(sort -u "$WORK/db_names.txt" "$WORK/body_refs.txt" | wc -l)
echo "kind=$KIND dir=$FILES_DIR  files(older than ${MIN_AGE_MIN}m)=$TOTAL  skipped-recent(<${MIN_AGE_MIN}m)=$SKIPPED_RECENT  referenced-names=$REFS  orphans=$ORPHANS"

if ! $APPLY; then
  # dry-run. 목록까지 보고 싶으면 KEEP_WORK=1 로 실행하면 현재 디렉터리에 남긴다.
  if [[ -n "${KEEP_WORK:-}" ]]; then
    cp "$WORK/orphans.txt" "./orphans-$KIND-$STAMP.txt"; echo "saved ./orphans-$KIND-$STAMP.txt"
  else
    echo "dry-run. 목록을 파일로 남기려면 KEEP_WORK=1 로 다시 실행"
  fi
  exit 0
fi

# ---- 6) 옮기기 (--apply) ----------------------------------------------------------------
mkdir -p "$ARCHIVE_DIR"
cp "$WORK/orphans.txt" "$ARCHIVE_DIR/MANIFEST.txt"     # 무엇을 옮겼는지 = 되돌리기의 근거
moved=0
while IFS= read -r rel; do                               # IFS= 와 -r: 공백·역슬래시가 든 이름을 그대로 읽는다
  [[ -z "$rel" ]] && continue
  mkdir -p "$ARCHIVE_DIR/$(dirname "$rel")"              # legacy처럼 하위 폴더가 있으면 같은 구조로
  mv -n -- "$FILES_DIR/$rel" "$ARCHIVE_DIR/$rel" && moved=$((moved+1))   # -n: 같은 이름이 이미 있으면 덮어쓰지 않는다
done < "$WORK/orphans.txt"
echo "moved $moved files -> $ARCHIVE_DIR (되돌리기: MANIFEST.txt 의 각 줄을 반대 방향으로 mv)"
