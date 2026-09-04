-- ============================================================================
-- about · scholarship 도 "콘텐츠 자체(부모) + 번역본"으로 나눈다
--   V21 에서 research·lab·professor·staff 를 나눈 것과 같은 방식이다.
--   이걸로 옆표(*_language) 네 개가 전부 사라진다.
-- ============================================================================
--
-- ## V21 과 다른 점 — about 은 짝이 두 가지 방식으로 표현돼 있다
--
-- research·member 는 모든 쌍이 옆표에 등록돼 있었다. about 은 그렇지 않다
-- (2026-09-05 prod 실측: 48행 중 옆표에 등록된 쌍은 19쌍 = 38행뿐).
--
--   post_type       행 수(KO/EN)   짝을 어떻게 아나
--   ─────────────── ───────────── ─────────────────────────────
--   STUDENT_CLUBS       8 / 8      about_language 에 등록돼 있다
--   FACILITIES          8 / 8      〃
--   DIRECTIONS          3 / 3      〃
--   OVERVIEW            1 / 1      옆표에 없다 — post_type 당 한 행뿐이라 짝이 자명
--   GREETINGS           1 / 1      〃
--   HISTORY             1 / 1      〃
--   FUTURE_CAREERS      1 / 1      〃
--   CONTACT             1 / 1      〃
--
-- 싱글턴 다섯은 "이 post_type 의 한국어 행"이 곧 유일한 행이라 옆표가 필요 없었다.
-- 그래서 아래에서 짝 목록을 임시 테이블로 **두 경로에서 모아** 만든다.
--
-- ⚠️ 싱글턴 매칭에 post_type 목록을 못박아 둔 이유: 여러 행을 가진 post_type 에
-- 같은 조인을 걸면 8×8 로 곱해진다. 만약 STUDENT_CLUBS 등에 옆표 누락 행이
-- 생기면 그 행은 부모를 못 얻고, 섹션 끝의 MODIFY … NOT NULL 에서 터진다
-- (조용히 잘못되는 대신 시끄럽게 멈춘다).
--
-- ## 필드 배치 — V21 과 같은 기준으로 실측했다
--
--   about
--     · name        → 번역본. 19쌍 중 17쌍이 다르다
--     · locations   → 번역본. 19쌍 중 8쌍이 다르고, 값이 번역 그 자체다
--                     (["302동 310-2호"] / ["301B 310-2"])
--     · description·search_content → 번역본
--     · 대표이미지  → 부모. 쌍마다 id 는 다르지만 같은 사진의 사본이다
--                     (V21 의 교수 사진과 같은 사정)
--     · 첨부        → 부모. 실물 2건이 같은 파일이다
--                     (1712387520810_CSE_Brochure.pdf / 1712388197729_… , 둘 다 29,522,424 bytes)
--
--   scholarship
--     · description → 번역본. 12쌍 전부 다르다
--     · student_type → 부모. 12쌍 전부 같다
--     · name        → **번역본**. 12쌍이 전부 "같지만" 부모로 올리지 않는다.
--                     장학금 이름은 번역 대상 콘텐츠인데 아직 영문화가 안 된 것뿐이다
--                     ("대통령과학장학금"이 영어 페이지에도 그대로 뜬다).
--                     부모로 올리면 앞으로도 번역할 수 없게 된다.
--                     ─ 실측으로 "같다"가 나와도 필드의 성격이 콘텐츠면 번역본이다.
--
-- ## 한쪽만 값이 있으면 한국어를 그대로 쓴다 (V21 과 동일, 예외 없음)
--
-- 빈 값이 "안 채웠다"인지 "지웠다"인지 DB 로는 구분할 수 없다. 한국어 행이 정본이면
-- 그 빈 값도 정본이다. 그래서 COALESCE 를 쓰지 않는다.
--
-- ## 주인을 잃는 파일 행은 여기서 지우지 않는다 (V21 과 동일)
--
-- 부모가 한국어 쪽 사진·첨부를 가져가므로 영어 쪽 행이 주인을 잃는다. 삭제는 이
-- 파일에서 유일하게 되돌릴 수 없는 동작이고, 안 지워도 화면·API·검색 어디도 그
-- 행을 보지 않는다. 참조 없는 행·파일 정리는 별도 작업이 한 번에 한다.
-- ============================================================================


-- ============================================================================
-- 1. about — 학부 소개(개요·연혁·시설·동아리·오시는 길 …)
-- ============================================================================
--
-- ── 지금 (시설 한 건) ───────────────────────────────────────────────────────
--
--   about
--    id  post_type   language  main_image_id  name / locations
--     5  FACILITIES  KO                 1250  세미나실  ·  ["301동 417호", …]
--    29  FACILITIES  EN                 1268  Seminar Room  ·  ["301B 417", …]
--                                       ────
--                                       같은 사진의 사본
--
--   about_language      ← 이 쌍은 옆표에 등록돼 있다
--    id  korean_id  english_id
--     …          5          29
--
-- ── 이 섹션이 끝나면 ────────────────────────────────────────────────────────
--
--   about  (부모)                       about_translation  (번역본)
--    id  post_type   main_image_id       id  about_id  language  name
--     5  FACILITIES           1250        5         5  KO        세미나실
--                                        29         5  EN        Seminar Room
--                                                  ───
--                                                  UNIQUE(about_id, language)
-- ============================================================================

-- ⚠️ 들어오는 FK 다. rename 하면 참조가 about_translation 을 따라가는데, 첨부는
-- 언어판이 아니라 콘텐츠에 붙어야 하므로 원하지 않는 결과다. 먼저 끊고 뒤에서
-- 부모를 가리키도록 다시 건다. (나가는 FK 인 main_image 는 이름 충돌 때문에 뗀다.)
ALTER TABLE attachment
    DROP FOREIGN KEY FK_attachment_about_about_id;

ALTER TABLE about
    DROP FOREIGN KEY FK_about_main_image_main_image_id;

RENAME TABLE about TO about_translation;

CREATE TABLE about
(
    id            BIGINT NOT NULL AUTO_INCREMENT,
    post_type     ENUM ('OVERVIEW','GREETINGS','HISTORY','FUTURE_CAREERS','CONTACT',
        'STUDENT_CLUBS','FACILITIES','DIRECTIONS') NOT NULL,
    main_image_id BIGINT      NULL,
    created_at    DATETIME(6) NULL,
    modified_at   DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_about_main_image_main_image_id FOREIGN KEY (main_image_id) REFERENCES main_image (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- 짝 목록을 한곳에 모은다 — 옆표에 있는 것 + 싱글턴.
CREATE TEMPORARY TABLE tmp_about_pair
(
    korean_id  BIGINT NOT NULL PRIMARY KEY,
    english_id BIGINT NOT NULL
);

INSERT INTO tmp_about_pair (korean_id, english_id)
SELECT korean_id, english_id
FROM about_language;

INSERT INTO tmp_about_pair (korean_id, english_id)
SELECT k.id, e.id
FROM about_translation k
         JOIN about_translation e ON e.post_type = k.post_type AND e.language = 'EN'
WHERE k.language = 'KO'
  AND k.post_type IN ('OVERVIEW', 'GREETINGS', 'HISTORY', 'FUTURE_CAREERS', 'CONTACT');

-- 한국어 행이 부모가 된다(id 를 그대로 물려받는다).
INSERT INTO about (id, post_type, main_image_id, created_at, modified_at)
SELECT k.id,
       k.post_type,
       k.main_image_id,
       k.created_at,
       k.modified_at
FROM tmp_about_pair p
         JOIN about_translation k ON k.id = p.korean_id;

ALTER TABLE about_translation
    ADD COLUMN about_id BIGINT NULL;

-- 두 번 도는 이유는 V21 과 같다 — 짝 한 줄이 두 행을 가리키므로 각각 훑는다.
-- 오른쪽이 언제나 korean_id 인 게 핵심이다(부모 id = 한국어 행 id).
UPDATE about_translation t
    JOIN tmp_about_pair p ON p.korean_id = t.id
SET t.about_id = p.korean_id;

UPDATE about_translation t
    JOIN tmp_about_pair p ON p.english_id = t.id
SET t.about_id = p.korean_id;

-- 첨부를 부모로 옮긴다. 한국어 행 id 가 곧 부모 id 라 값은 그대로 유효하고,
-- 영어 행에 붙어 있던 첨부는 주인을 없앤다 — 부모의 첨부 목록에 사본이 두 벌
-- 뜨는 것을 막는다. 남은 행은 "주인 없는 첨부"가 되어 정리 작업이 걷어간다
-- (그런 행이 이미 37건 있다).
UPDATE attachment att
    JOIN tmp_about_pair p ON p.english_id = att.about_id
SET att.about_id = NULL;

ALTER TABLE about_translation
    DROP COLUMN main_image_id,
    DROP COLUMN post_type,
    -- 엔티티가 읽지 않는 죽은 컬럼이다. 48행 전부 NULL 이라 옮길 값도 없다.
    DROP COLUMN eng_name,
    DROP COLUMN year;

DROP TEMPORARY TABLE tmp_about_pair;

ALTER TABLE about_translation
    MODIFY about_id BIGINT NOT NULL,
    MODIFY language ENUM ('KO','EN') NOT NULL,
    ADD CONSTRAINT FK_about_translation_about_about_id FOREIGN KEY (about_id) REFERENCES about (id),
    ADD CONSTRAINT UQ_about_translation_about_id_language UNIQUE (about_id, language);

ALTER TABLE attachment
    ADD CONSTRAINT FK_attachment_about_about_id FOREIGN KEY (about_id) REFERENCES about (id);


-- ============================================================================
-- 2. scholarship — 장학금
-- ============================================================================
--
-- 가장 단순하다. 나가는 FK 가 없고, 짝도 12쌍 전부 옆표에 등록돼 있다.
--
--   scholarship
--    id  student_type    language  name / description
--     1  UNDERGRADUATE   KO        성적우수 국가장학금 (이공계)  ·  <한국어 설명>
--    14  UNDERGRADUATE   EN        성적우수 국가장학금 (이공계)  ·  <영어 설명>
--                                  ────────────────────────
--                                  이름은 아직 영문화가 안 됐을 뿐이라 번역본에 남긴다
--
-- ⚠️ academics_search.scholarship_id 는 rename 을 따라가 번역본을 가리키게 되는데,
-- 검색 색인은 언어별이므로 그게 맞다. 반면 attachment.scholarship_id 는 부모를
-- 가리켜야 하므로 먼저 끊는다(실물 0건이지만 규칙은 같다).
-- ============================================================================

ALTER TABLE attachment
    DROP FOREIGN KEY FK_attachment_scholarship_scholarship_id;

RENAME TABLE scholarship TO scholarship_translation;

CREATE TABLE scholarship
(
    id           BIGINT                             NOT NULL AUTO_INCREMENT,
    student_type ENUM ('UNDERGRADUATE','GRADUATE')  NOT NULL,
    created_at   DATETIME(6)                        NULL,
    modified_at  DATETIME(6)                        NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO scholarship (id, student_type, created_at, modified_at)
SELECT k.id,
       k.student_type,
       k.created_at,
       k.modified_at
FROM scholarship_language sl
         JOIN scholarship_translation k ON k.id = sl.korean_id;

ALTER TABLE scholarship_translation
    ADD COLUMN scholarship_id BIGINT NULL;

UPDATE scholarship_translation t
    JOIN scholarship_language sl ON sl.korean_id = t.id
SET t.scholarship_id = sl.korean_id;

UPDATE scholarship_translation t
    JOIN scholarship_language sl ON sl.english_id = t.id
SET t.scholarship_id = sl.korean_id;

ALTER TABLE scholarship_translation
    DROP COLUMN student_type;

ALTER TABLE scholarship_translation
    MODIFY scholarship_id BIGINT NOT NULL,
    MODIFY language ENUM ('KO','EN') NOT NULL,
    ADD CONSTRAINT FK_scholarship_translation_scholarship_scholarship_id
        FOREIGN KEY (scholarship_id) REFERENCES scholarship (id),
    ADD CONSTRAINT UQ_scholarship_translation_scholarship_id_language UNIQUE (scholarship_id, language);

ALTER TABLE attachment
    ADD CONSTRAINT FK_attachment_scholarship_scholarship_id
        FOREIGN KEY (scholarship_id) REFERENCES scholarship (id);


-- ============================================================================
-- 3. 뒷정리 — 옆표가 전부 사라진다
-- ============================================================================
--
-- V21 에서 research_language·member_language 를 지웠고, 이제 남은 둘도 지운다.
-- 짝은 이제 번역본의 부모 FK 로 표현되고, 언어가 늘어도 컬럼 이름에 언어가
-- 박히는 일(korean_id/english_id)이 없다.
-- ============================================================================

DROP TABLE about_language;
DROP TABLE scholarship_language;
