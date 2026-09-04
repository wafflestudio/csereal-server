-- ============================================================================
-- 한국어판·영어판을 "콘텐츠 자체(부모) + 번역본"으로 나눈다
--   대상: research(연구그룹·센터) · lab(연구실) · professor(교수) · staff(행정직원)
--   about·scholarship 은 다음 단계에서 같은 방식으로.
-- ============================================================================
--
-- ## 지금 구조와 그 대가
--
-- 지금은 한 연구실의 한국어판과 영어판이 **각각 독립된 행**이다. 둘이 한 쌍이라는
-- 사실은 본체가 아니라 옆표(research_language·member_language)가 들고 있다.
--
--     lab                          research_language
--      id  language  name           korean_id  english_id  type
--       1  KO        분산시스템      1          34          LAB
--      34  EN        Distributed …
--
-- 이 구조엔 "이 연구실"을 가리키는 id 가 없다. 존재하는 건 "이 연구실의 한국어
-- 번역본" id 뿐이다. 그래서 **언어와 무관한 것을 둘 자리가 번역본밖에 없고**,
-- 결과적으로 두 벌로 저장된다. 실제로 그랬다(2026-09-04 prod 실측):
--
--   · 교수 대표이미지 74쌍 전부 중복 — 원본 파일명도 md5 도 같다
--     (1709908714916_강유.png / 1709909483437_강유.png)
--   · 직원 9쌍, 연구그룹 11쌍, 연구실 PDF 23쌍도 전부 중복
--   · 합계 130개 파일 147MB 가 순수 중복
--
-- 코드가 그렇게 되어 있었다. createLabLanguage 가 같은 MultipartFile 을
-- 한국어용·영어용으로 두 번 업로드했다.
--
-- 더 나쁜 건, 짝이 어긋나도 DB 가 막지 못한다는 것이다. 실제로 3건이 틀린 채
-- 운영 중이었다 — 연구그룹 4번의 영어 페이지가 다른 그룹 사진을, 시설 18·19번이
-- 서로 한 칸씩 밀린 사진을 달고 있었다. 스키마가 "같은 것의 두 번역본"이라는
-- 개념을 모르니 아무도 못 잡았다.
--
-- ## 바꾸는 모양
--
--     lab              (id, research_id, pdf_id, acronym, tel, …)   ← 언어 무관
--     lab_translation  (id, lab_id, language, name, description, location)
--
-- 언어가 늘어도 번역본 행만 추가하면 된다. 옆표의 korean_id/english_id 처럼
-- **언어가 컬럼 이름에 박히는 일**이 없어진다.
--
-- ## 어떤 필드를 어디에 두나 — 추측하지 않고 실측했다
--
--   · acronym  → 부모.   38쌍 전부 한/영이 같다(MRL·HCS·IMO …)
--   · location → 번역본. 38쌍 중 36쌍이 다르다("302동 312-1호" / "302 Building, Room …")
--   · office   → 번역본. 교수는 양쪽에 값이 있는 42쌍이 **전부** 다르고, 직원도 9쌍 중
--     8쌍이 다르다("301동 502호" / "301 Building, Room 502"). location 과 같은 성격이다.
--   · phone·fax·email·website·start_date → 부모. 다른 쌍이 있긴 하나 번역이 아니라
--     한쪽에만 갱신된 드리프트다(전화번호 표기 흔들림 2쌍, 이메일 1쌍, 재직시작일 8쌍).
--     값은 한국어 행 것을 그대로 쓴다 — 이유는 아래 research 섹션의 INSERT 주석 참고.
--   · pdf·대표이미지 → 부모. 해시 비교 결과 전부 같은 파일이었다
--   · 검색 색인(research_search·member_search) → 번역본.
--     한국어 색인과 영어 색인은 원래 별개다.
--
-- ## 부모 id 는 기존 한국어 행의 id 를 그대로 쓴다
--
-- 새 id 를 발급하지 않는다. 그래야 기존 한국어 URL(/research/labs/1)과, 한국어
-- 행을 가리키던 FK 가 그대로 유효하다. 영어 상세 URL 의 id 는 바뀌는데,
-- 사이트맵에 id 가 든 URL 이 하나도 없어 우리가 발행하는 링크는 안 깨진다.
--
-- ## 이 마이그레이션이 하지 않는 것 — 중복 이미지·PDF 행 삭제
--
-- 부모가 한국어 쪽 파일을 가져가므로 영어 쪽 파일 행은 아무도 안 가리키게 된다
-- (main_image 93건, attachment 23건). 그 행들을 여기서 지우지 않는다.
--
--   · 안 지워도 깨지는 게 없다. 화면·API·검색 어디도 그 행을 보지 않는다.
--   · 나중에 정확히 같은 집합을 찾을 수 있다. "아무도 안 가리키는 main_image" 를
--     세면 되는데, 지금 prod 에 그런 행이 0건이라(831행 전부 참조됨) 이 규칙이
--     깨끗하게 성립한다. 쌍 정보가 필요 없다.
--   · 실제 파일 삭제는 어차피 archive 스크립트 몫이라, 행과 파일을 한 곳에서
--     같이 정리하는 게 맞다. 여기서 행만 지우면 작업이 두 번으로 쪼개진다.
--   · 그리고 삭제는 이 파일에서 유일하게 되돌릴 수 없는 동작이다. MySQL DDL 은
--     롤백이 안 되므로, 구조 변경만 하는 파일로 두는 편이 재실행·복구에 안전하다.
--
-- ## 순서가 중요하다
--
-- research → lab → professor 순으로 쪼갠다. lab 이 research 를, professor 가
-- lab 을 가리키기 때문이다. 앞 단계에서 부모가 먼저 생겨 있어야 뒤 단계가 그
-- 부모를 가리킬 수 있다. 옆표는 짝을 찾는 데 계속 쓰이므로 맨 마지막에 지운다.
-- ============================================================================


-- ============================================================================
-- 1. research — 연구그룹·연구센터
-- ============================================================================


-- ── 지금 (2026-09-04 prod 실측, 연구그룹 1번) ────────────────────────────────
--
--   research
--    id  language  post_type  main_image_id  name
--     1  KO        GROUPS              1198  그래픽스 및 사람 중심 컴퓨팅
--    15  EN        GROUPS              1224  Graphics and Human-Centered Computing
--                                      ────
--                                      다른 행인데 같은 사진이다. 업로드가 두 번
--                                      일어나 파일도 두 개 있다(md5 동일).
--
--   research_language      ← "이 둘이 한 쌍"이라는 사실은 오직 여기에만 있다
--    id  korean_id  english_id  type
--     1          1          15  RESEARCH_GROUP
--
-- ── 이 섹션이 끝나면 ────────────────────────────────────────────────────────
--
--   research  (부모 — 언어와 무관한 것만)
--    id  post_type  websiteurl  main_image_id
--     1  GROUPS     NULL                 1198   ← 1224 는 주인을 잃어 행을 지운다
--
--   research_translation  (번역본 — 언어마다 한 행)
--    id  research_id  language  name
--     1            1  KO        그래픽스 및 사람 중심 컴퓨팅
--    15            1  EN        Graphics and Human-Centered Computing
--                 ───
--                 UNIQUE(research_id, language) — "한 언어당 한 행"을 이제 스키마가
--                 강제한다. 옆표 구조에선 한국어 행이 둘 생겨도 DB 가 못 막았다.
--
-- 아래 다섯 걸음으로 간다. 네 도메인 모두 같은 순서다.
--   ① 이름이 충돌할 FK 를 뗀다   ② 원본을 _translation 으로 rename
--   ③ 원래 이름으로 부모를 만든다  ④ 값을 부모로 옮긴다   ⑤ 번역본에서 뗀 컬럼을 지운다

-- ⚠️ FK 제약 이름은 테이블이 아니라 **DB 전역**에서 유일해야 한다.
-- rename 한 테이블이 옛 이름을 계속 붙들고 있으므로, 부모가 같은 이름을 쓰려면
-- 먼저 떼야 한다. (컬럼은 아래 INSERT 에서 값을 읽어야 하니 남겨 둔다.)
ALTER TABLE research
    DROP FOREIGN KEY FK_research_main_image_main_image_id;

-- 기존 테이블이 곧 번역본이다. rename 하면 이 테이블을 가리키던 FK 들
-- (research_search.research_id 등)이 자동으로 따라온다 — 검색 색인은 번역본에
-- 붙는 게 맞으므로 이게 원하는 결과다.
RENAME TABLE research TO research_translation;

CREATE TABLE research
(
    id            BIGINT                     NOT NULL AUTO_INCREMENT,
    post_type     ENUM ('GROUPS', 'CENTERS') NOT NULL,
    websiteurl    VARCHAR(255)               NULL,
    main_image_id BIGINT                     NULL,
    created_at    DATETIME(6)                NULL,
    modified_at   DATETIME(6)                NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_research_main_image_main_image_id FOREIGN KEY (main_image_id) REFERENCES main_image (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- 한국어 행이 부모가 된다(id 를 그대로 물려받는다).
--
-- ## 한쪽만 값이 있을 때 어느 쪽을 취하나 — 한국어를 그대로 쓴다, 예외 없이
--
-- 값이든 참조든 **한국어 행이 정본**이다. 영어 쪽 값을 끌어와 메우지 않는다.
--
--   · 빈 값이 "아직 안 채웠다"인지 "지웠다"인지 DB 로는 구분할 수 없다. 한국어 행이
--     정본이면 그 빈 값도 정본이다. 실제로 교수 두 명(문봉기·박근수)은 한국어 행이
--     영어 행보다 2주 뒤에 수정됐고 그 뒤로 전화·팩스가 비어 있다 — 지운 것으로 보는
--     게 맞고, 되살렸다가 틀리면 지워 달라던 번호를 한국어 페이지에까지 노출하게 된다.
--   · FK 도 마찬가지다. 한쪽만 값이 있는 경우는 실측상 딱 하나 — 염헌영 교수의
--     연구실이 영어 행에만 남아 있었다. 그런데 역대 교수 25명 중 23명은 양쪽 다
--     연구실이 없다(은퇴하면 떼는 것이 관례). 즉 한국어가 미입력인 게 아니라 영어가
--     정리 누락이고, 지금 영어 연구실 페이지에만 역대 교수가 섞여 나오고 있다.
--
-- 이 규칙 덕분에 **id 변환이 아예 필요 없다.** 한국어 행의 FK 는 언제나 한국어 행을
-- 가리키고(실측 0건 예외), 부모 id 가 곧 한국어 id 이므로 k.research_id·k.lab_id 를
-- 그대로 넣으면 정확히 부모를 가리킨다.
INSERT INTO research (id, post_type, websiteurl, main_image_id, created_at, modified_at)
SELECT k.id,
       k.post_type,
       k.websiteurl,
       k.main_image_id,
       k.created_at,
       k.modified_at
FROM research_language rl
         JOIN research_translation k ON k.id = rl.korean_id
         JOIN research_translation e ON e.id = rl.english_id
-- ⚠️ 옆표 하나가 연구그룹·연구센터·**연구실**을 함께 담고 type 으로만 구분한다.
-- 그래서 korean_id 가 가리키는 테이블이 type 마다 다르다 — RESEARCH_GROUP 의 1 은
-- research 1번이고, LAB 의 1 은 lab 1번이다. type 을 걸지 않으면 이 조인은 서로 다른
-- 것끼리 id 숫자만 맞춰 보는 꼴이 된다. (이번 prod 데이터는 id 범위가 안 겹쳐 빼도
-- 결과가 같지만 그건 우연이지 근거가 아니다. 겹치면 에러 없이 조용히 틀린다.)
WHERE rl.type IN ('RESEARCH_GROUP', 'RESEARCH_CENTER');

-- 번역본이 부모를 가리키게 한다. 한국어 행은 자기 자신이 부모다.
ALTER TABLE research_translation
    ADD COLUMN research_id BIGINT NULL;

-- 옆표 한 줄이 두 행을 가리키므로 UPDATE 를 두 번 돈다.
--
--   research_language 의 한 줄:  korean_id = 1,  english_id = 15
--
--     1번째  korean_id  로 매칭 → 한국어 행(id 1) 의 research_id = 1   (자기 자신이 부모)
--     2번째  english_id 로 매칭 → 영어  행(id 15) 의 research_id = 1   (짝의 한국어 id)
--
-- 두 UPDATE 모두 오른쪽 값이 rl.korean_id 인 게 핵심이다 — 부모 id 는 언제나 한국어 행 id.
UPDATE research_translation t
    JOIN research_language rl ON rl.korean_id = t.id AND rl.type IN ('RESEARCH_GROUP', 'RESEARCH_CENTER')
SET t.research_id = rl.korean_id;

UPDATE research_translation t
    JOIN research_language rl ON rl.english_id = t.id AND rl.type IN ('RESEARCH_GROUP', 'RESEARCH_CENTER')
SET t.research_id = rl.korean_id;

-- 이 컬럼을 떼면 영어 번역본이 가리키던 main_image 행을 아무도 안 가리키게 된다.
-- 그 main_image 행은 여기서 지우지 않는다(번역본 행 자체는 당연히 남는다) —
-- 파일 맨 위 "이 마이그레이션이 하지 않는 것" 참고.
ALTER TABLE research_translation
    DROP COLUMN main_image_id,
    DROP COLUMN websiteurl,
    DROP COLUMN post_type;

-- UNIQUE (research_id, language) 가 "한 언어당 번역본 하나"를 스키마로 보장한다.
-- 예전 구조에선 같은 연구의 한국어 행이 둘 생겨도 DB 가 막지 못했다.
ALTER TABLE research_translation
    MODIFY research_id BIGINT NOT NULL,
    MODIFY language ENUM ('KO','EN') NOT NULL,
    ADD CONSTRAINT FK_research_translation_research_research_id FOREIGN KEY (research_id) REFERENCES research (id),
    ADD CONSTRAINT UQ_research_translation_research_id_language UNIQUE (research_id, language);


-- ============================================================================
-- 2. lab — 연구실
-- ============================================================================


-- ── 지금 (연구실 1번 = 데이터 마이닝 연구실) ─────────────────────────────────
--
--   두 행이 **똑같은 값**을 들고 있는 컬럼 — 부모로 올라갈 것들
--    id  language  acronym  tel            websiteurl
--     1  0 (=KO)   DM       (02) 880-7263  http://datalab.snu.ac.kr/
--    34  1 (=EN)   DM       (02) 880-7263  http://datalab.snu.ac.kr/
--        ────────
--        ⚠️ 이 테이블만 language 가 tinyint 다(아래 language_str 주석 참고).
--
--   두 행이 **다른 값**을 들고 있는 컬럼
--    id  research_id  pdf_id  name / location
--     1            3   16321  데이터 마이닝 연구실  ·  301동 515호 / 518호 / …
--    34           14   16322  Data Mining Lab   ·  301 Building, Room 515 / …
--                ───   ─────
--                 │      └ 같은 PDF 를 두 번 업로드한 사본이다(23쌍이 그랬다)
--                 └ 다르지만 "다른 연구그룹"이 아니다. 3 은 한국어 데이터 시스템,
--                   14 는 그 영어 행이다. 즉 **FK 까지 언어별로 두 벌**이었다.
--
-- ── 이 섹션이 끝나면 ────────────────────────────────────────────────────────
--
--   lab  (부모)
--    id  research_id  pdf_id  acronym  tel            websiteurl
--     1            3   16321  DM       (02) 880-7263  http://datalab.snu.ac.kr/
--                ───
--                14 → 3 으로 접힌다. 연구그룹도 이미 부모로 합쳐졌으니 가리킬 곳이 하나뿐이다.
--
--   lab_translation  (번역본)      name·description·location 만 남는다
--    id  lab_id  language  name
--     1       1  KO        데이터 마이닝 연구실
--    34       1  EN        Data Mining Lab

ALTER TABLE lab
    DROP FOREIGN KEY FK_lab_research_research_id,
    DROP FOREIGN KEY FK_lab_attachment_pdf_id;

RENAME TABLE lab TO lab_translation;

CREATE TABLE lab
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    research_id BIGINT       NULL,
    pdf_id      BIGINT       NULL,
    acronym     VARCHAR(255) NULL,
    tel         VARCHAR(255) NULL,
    websiteurl  VARCHAR(255) NULL,
    youtube     VARCHAR(255) NULL,
    created_at  DATETIME(6)  NULL,
    modified_at DATETIME(6)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_lab_research_research_id FOREIGN KEY (research_id) REFERENCES research (id),
    CONSTRAINT FK_lab_attachment_pdf_id FOREIGN KEY (pdf_id) REFERENCES attachment (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- research_id 를 변환 없이 그대로 옮긴다. 한국어 연구실은 한국어 연구그룹을 가리키고
-- 있고(실측: 언어 교차 참조 0건), 그 그룹의 id 가 방금 만든 부모 id 라 그대로 유효하다.
INSERT INTO lab (id, research_id, pdf_id, acronym, tel, websiteurl, youtube, created_at, modified_at)
SELECT k.id,
       k.research_id,
       k.pdf_id,
       k.acronym,
       k.tel,
       k.websiteurl,
       k.youtube,
       k.created_at,
       k.modified_at
FROM research_language rl
         JOIN lab_translation k ON k.id = rl.korean_id
         JOIN lab_translation e ON e.id = rl.english_id
WHERE rl.type = 'LAB';

ALTER TABLE lab_translation
    ADD COLUMN lab_id BIGINT NULL,
    -- ⚠️ 이 테이블만 language 가 tinyint 다. LabEntity 에 @Enumerated(STRING) 이
    -- 빠져 ORDINAL(0=KO, 1=EN)로 저장돼 있었다. enum 상수 순서를 바꾸면 모든 행의
    -- 언어가 조용히 뒤집히는 함정이라, 이 참에 문자열로 바꾼다.
    ADD COLUMN language_str ENUM ('KO','EN') NULL;

UPDATE lab_translation t
    JOIN research_language rl ON rl.korean_id = t.id AND rl.type = 'LAB'
SET t.lab_id = rl.korean_id;

UPDATE lab_translation t
    JOIN research_language rl ON rl.english_id = t.id AND rl.type = 'LAB'
SET t.lab_id = rl.korean_id;

UPDATE lab_translation SET language_str = IF(language = 0, 'KO', 'EN');

-- 연구실 PDF 는 한 문서인데 언어별로 한 번씩 업로드돼 두 벌이었다(23쌍, 내용 동일).
-- pdf_id 를 떼면 영어 번역본이 가리키던 attachment 행이 주인을 잃는데, 그 행도
-- 여기서 지우지 않는다.
ALTER TABLE lab_translation
    DROP COLUMN pdf_id,
    DROP COLUMN research_id,
    DROP COLUMN acronym,
    DROP COLUMN tel,
    DROP COLUMN websiteurl,
    DROP COLUMN youtube,
    DROP COLUMN language;

ALTER TABLE lab_translation
    CHANGE COLUMN language_str language ENUM ('KO','EN') NOT NULL,
    MODIFY lab_id BIGINT NOT NULL,
    ADD CONSTRAINT FK_lab_translation_lab_lab_id FOREIGN KEY (lab_id) REFERENCES lab (id),
    ADD CONSTRAINT UQ_lab_translation_lab_id_language UNIQUE (lab_id, language);


-- ============================================================================
-- 3. professor — 교수
-- ============================================================================


-- ── 지금 (교수 1번 = 강유) ──────────────────────────────────────────────────
--
--   두 행이 **똑같은 값** — 부모로
--    id  language  email            phone          website
--     1  KO        ukang@snu.ac.kr  (02) 880-7254  http://datalab.snu.ac.kr/~ukang
--    63  EN        ukang@snu.ac.kr  (02) 880-7254  http://datalab.snu.ac.kr/~ukang
--
--   두 행이 **다른 값** — 번역본으로
--    id  lab_id  main_image_id  name / academic_rank / office
--     1       1            986  강유    ·  교수, 협동과정 인공지능 전공주임  ·  301동 502호
--    63      34           1056  U Kang ·  Professor, IPAI Head Professor ·  301 Building, Room 502
--                                                                           ─────────────────────
--                                                       office 는 주소 표기라 번역 대상이다.
--                                                       양쪽에 값이 있는 42쌍이 전부 달랐다.
--           ───           ─────
--            │              └ 74쌍 **전부** 같은 사진의 사본이었다(md5 동일).
--            └ lab 과 같은 사정. 1 은 한국어 데이터 마이닝 연구실, 34 는 그 영어 행.
--              둘 다 부모 연구실 1 로 접힌다.
--
-- ── 이 섹션이 끝나면 ────────────────────────────────────────────────────────
--
--   professor  (부모)                     professor_translation  (번역본)
--    id  status  lab_id  main_image_id     id  professor_id  language  name
--     1  ACTIVE       1            986      1             1  KO        강유
--                                          63             1  EN        U Kang

ALTER TABLE professor
    DROP FOREIGN KEY FK_professor_lab_lab_id,
    DROP FOREIGN KEY FK_professor_main_image_main_image_id;

RENAME TABLE professor TO professor_translation;

CREATE TABLE professor
(
    id            BIGINT                                  NOT NULL AUTO_INCREMENT,
    status        ENUM ('ACTIVE', 'INACTIVE', 'VISITING') NULL,
    lab_id        BIGINT                                  NULL,
    start_date    DATE                                    NULL,
    end_date      DATE                                    NULL,
    phone         VARCHAR(255)                            NULL,
    fax           VARCHAR(255)                            NULL,
    email         VARCHAR(255)                            NULL,
    website       VARCHAR(255)                            NULL,
    main_image_id BIGINT                                  NULL,
    created_at    DATETIME(6)                             NULL,
    modified_at   DATETIME(6)                             NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_professor_lab_lab_id FOREIGN KEY (lab_id) REFERENCES lab (id),
    CONSTRAINT FK_professor_main_image_main_image_id FOREIGN KEY (main_image_id) REFERENCES main_image (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- lab_id 도 변환 없이 그대로 옮긴다(한국어 교수 → 한국어 연구실 → 그 id 가 부모 id).
INSERT INTO professor (id, status, lab_id, start_date, end_date, phone, fax, email, website,
                       main_image_id, created_at, modified_at)
SELECT k.id,
       k.status,
       k.lab_id,
       k.start_date,
       k.end_date,
       k.phone,
       k.fax,
       k.email,
       k.website,
       k.main_image_id,
       k.created_at,
       k.modified_at
FROM member_language ml
         JOIN professor_translation k ON k.id = ml.korean_id
         JOIN professor_translation e ON e.id = ml.english_id
WHERE ml.type = 'PROFESSOR';

ALTER TABLE professor_translation
    ADD COLUMN professor_id BIGINT NULL;

UPDATE professor_translation t
    JOIN member_language ml ON ml.korean_id = t.id AND ml.type = 'PROFESSOR'
SET t.professor_id = ml.korean_id;

UPDATE professor_translation t
    JOIN member_language ml ON ml.english_id = t.id AND ml.type = 'PROFESSOR'
SET t.professor_id = ml.korean_id;

-- 74쌍 전부 같은 사진이 두 벌로 저장돼 있었다. main_image_id 를 떼면 영어 번역본이
-- 가리키던 main_image 행이 주인을 잃는데, 그 행은 여기서 지우지 않는다.
ALTER TABLE professor_translation
    DROP COLUMN lab_id,
    DROP COLUMN main_image_id,
    DROP COLUMN status,
    DROP COLUMN start_date,
    DROP COLUMN end_date,
    DROP COLUMN phone,
    DROP COLUMN fax,
    DROP COLUMN email,
    DROP COLUMN website;

ALTER TABLE professor_translation
    MODIFY professor_id BIGINT NOT NULL,
    MODIFY language ENUM ('KO','EN') NOT NULL,
    ADD CONSTRAINT FK_professor_translation_professor_professor_id FOREIGN KEY (professor_id) REFERENCES professor (id),
    ADD CONSTRAINT UQ_professor_translation_professor_id_language UNIQUE (professor_id, language);


-- ============================================================================
-- 4. staff — 행정직원
-- ============================================================================


-- 앞의 셋과 같은 모양이라 짧다. 연구실·연구그룹 같은 FK 가 없어 서브쿼리도 없다.
--
-- ── 지금 (직원 = 진연서) ────────────────────────────────────────────────────
--
--    id  language  email             phone          main_image_id  name / role / office
--    14  KO        ys.jin@snu.ac.kr  (02) 880-1527           1630  진연서       ·  교원인사, 일반서무           ·  301동 316호
--    21  EN        ys.jin@snu.ac.kr  (02) 880-1527           1631  Yeonseo Jin ·  Faculty Personnel Affairs ·  301 Building, Room 316
--        ─────────────────────────────────────────           ────                                             ─────────────────────
--        9쌍 전부 동일 → 부모로                                 사진 사본                                        9쌍 중 8쌍이 다르다
--                                                            → 한 장만                                        → 번역본으로

ALTER TABLE staff
    DROP FOREIGN KEY FK_staff_main_image_main_image_id;

RENAME TABLE staff TO staff_translation;

CREATE TABLE staff
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    phone         VARCHAR(255) NULL,
    email         VARCHAR(255) NULL,
    main_image_id BIGINT       NULL,
    created_at    DATETIME(6)  NULL,
    modified_at   DATETIME(6)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_staff_main_image_main_image_id FOREIGN KEY (main_image_id) REFERENCES main_image (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO staff (id, phone, email, main_image_id, created_at, modified_at)
SELECT k.id,
       k.phone,
       k.email,
       k.main_image_id,
       k.created_at,
       k.modified_at
FROM member_language ml
         JOIN staff_translation k ON k.id = ml.korean_id
         JOIN staff_translation e ON e.id = ml.english_id
WHERE ml.type = 'STAFF';

ALTER TABLE staff_translation
    ADD COLUMN staff_id BIGINT NULL;

UPDATE staff_translation t
    JOIN member_language ml ON ml.korean_id = t.id AND ml.type = 'STAFF'
SET t.staff_id = ml.korean_id;

UPDATE staff_translation t
    JOIN member_language ml ON ml.english_id = t.id AND ml.type = 'STAFF'
SET t.staff_id = ml.korean_id;

-- 여기도 마찬가지 — 영어 번역본이 가리키던 main_image 행은 남겨 둔다.
ALTER TABLE staff_translation
    DROP COLUMN main_image_id,
    DROP COLUMN phone,
    DROP COLUMN email;

ALTER TABLE staff_translation
    MODIFY staff_id BIGINT NOT NULL,
    MODIFY language ENUM ('KO','EN') NOT NULL,
    ADD CONSTRAINT FK_staff_translation_staff_staff_id FOREIGN KEY (staff_id) REFERENCES staff (id),
    ADD CONSTRAINT UQ_staff_translation_staff_id_language UNIQUE (staff_id, language);


-- ============================================================================
-- 5. 뒷정리
-- ============================================================================


-- ── 다 끝나면 이런 모양이 된다 ──────────────────────────────────────────────
--
--     research ◀───── research_translation
--        ▲
--        │ research_id
--       lab     ◀───── lab_translation
--        ▲
--        │ lab_id
--    professor  ◀───── professor_translation
--
--      staff    ◀───── staff_translation
--
--   왼쪽(부모)이 "이 연구실 자체", 오른쪽이 "그 연구실의 한 언어판"이다.
--   부모끼리의 FK 는 한 벌뿐이라 언어별로 어긋날 여지가 없다. 언어가 늘어도
--   오른쪽에 행만 추가하면 된다 — korean_id/english_id 처럼 컬럼 이름에 언어가
--   박히는 일이 없어졌다.

-- member_search 도 lab 과 같은 함정이었다(@Enumerated(STRING) 누락 → ORDINAL 저장).
ALTER TABLE member_search ADD COLUMN language_str ENUM ('KO','EN') NULL;
UPDATE member_search SET language_str = IF(language = 0, 'KO', 'EN');
ALTER TABLE member_search DROP COLUMN language;
ALTER TABLE member_search CHANGE COLUMN language_str language ENUM ('KO','EN') NOT NULL;

-- 짝은 이제 번역본의 부모 FK 로 표현된다. 옆표는 여기까지 매핑에 쓰였고 이제 필요 없다.
-- about_language·scholarship_language 는 그 도메인을 쪼갤 때 함께 지운다.
DROP TABLE research_language;
DROP TABLE member_language;
