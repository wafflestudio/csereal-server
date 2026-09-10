#!/usr/bin/env python3
"""
운영 DB 본문에서 측정용 코퍼스를 뽑는다.

  # 1) 본문을 한 파일로 (DB 컨테이너에서)
  docker exec <db> mysql -uroot -p<pw> --default-character-set=utf8mb4 -N --raw -e "
    select concat('###ROW###', id, '###', replace(replace(description,'\n',' '),'\r',' '))
      from notice
    union all select concat('###ROW###n', id, '###', replace(replace(description,'\n',' '),'\r',' '))
      from news
    -- … 본문을 가진 나머지 테이블도 같은 모양으로
  " > rows.txt

  # 2) 정책 밖 속성을 쓰는 문서를 골라 코퍼스로
  python3 extract-corpus.py rows.txt --policy policy.txt --out corpus.json

`--policy` 는 현재 허용 목록(한 줄에 하나). 아래로 만든다.
  ContentCssSchema.INSTANCE.allowedProperties() 를 찍거나,
  OWASP CssSchema.DEFAULT.allowedProperties() 에 코드의 가감을 적용한다.
"""
import argparse
import collections
import json
import random
import re

# 브라우저가 무시하는 벤더·레거시 접두사. 렌더에 영향이 없어 측정할 이유가 없다.
VENDOR = re.compile(
    r"^(mso-|-ms-|-webkit-|-moz-|-o-|-en-|--|hwp|layout-grid|text-autospace"
    r"|punctuation-wrap|orphans$|widows$|tab-stops$|language$|text-underline$"
    r"|text-justify$|line-$)"
)

# 본문에 박힌 base64 이미지는 1x1 투명 PNG 로 바꾼다. 측정 대상은 style 속성이라 이미지
# 내용은 상관없고, 수 MB 짜리 payload 를 그대로 두면 렌더가 몇 분씩 걸려 시간 초과로 빠진다.
# 세탁기도 저장 시점에 base64 를 파일로 빼내므로, 지우고 재는 쪽이 실제 렌더 경로에 가깝다.
# width/height 속성은 남아 있어 자리 크기는 유지된다.
BASE64_IMAGE = re.compile(r'src="data:image/[^"]*"')
PLACEHOLDER = (
    'src="data:image/png;base64,'
    'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=="'
)


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("rows")
    ap.add_argument("--policy", required=True, help="현재 허용 목록 (한 줄에 한 속성)")
    ap.add_argument("--out", required=True)
    ap.add_argument("--samples", type=int, default=12)
    ap.add_argument("--seed", type=int, default=7)
    ap.add_argument(
        "--only",
        help="이 속성들만 (쉼표 구분). 허용 목록 '안'의 속성을 역검증할 때 쓴다",
    )
    args = ap.parse_args()

    policy = {l.strip() for l in open(args.policy) if l.strip()}
    only = {p.strip() for p in args.only.split(",")} if args.only else None

    docs: dict[str, str] = {}
    by_prop: dict[str, list[str]] = collections.defaultdict(list)

    raw = open(args.rows, encoding="utf-8", errors="replace").read()
    for chunk in raw.split("###ROW###")[1:]:
        sep = chunk.find("###")
        if sep < 0:
            continue
        doc_id, html = chunk[:sep], BASE64_IMAGE.sub(PLACEHOLDER, chunk[sep + 3 :])

        used = set()
        for m in re.finditer(r'style="([^"]*)"', html):
            for decl in m.group(1).split(";"):
                if ":" in decl:
                    used.add(decl.split(":")[0].strip().lower())

        if only is not None:
            hit = used & only
        else:
            hit = {p for p in used if p not in policy and not VENDOR.match(p)}
        if not hit:
            continue

        docs[doc_id] = html
        for p in hit:
            by_prop[p].append(doc_id)

    random.seed(args.seed)
    props = {
        p: {"total": len(ids), "sample": random.sample(ids, min(args.samples, len(ids)))}
        for p, ids in by_prop.items()
    }
    needed = {i for v in props.values() for i in v["sample"]}
    json.dump(
        {"props": props, "docs": {k: v for k, v in docs.items() if k in needed}},
        open(args.out, "w"),
        ensure_ascii=False,
    )
    print(f"속성 {len(props)}종 · 렌더할 문서 {len(needed)}건 → {args.out}")


if __name__ == "__main__":
    main()
