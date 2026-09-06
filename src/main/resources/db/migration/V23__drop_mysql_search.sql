-- 검색을 Elasticsearch 가 맡으면서 MySQL FULLTEXT 를 읽는 코드가 사라졌다.
-- 여기 있는 것들은 모두 원본에서 다시 만들 수 있는 파생 데이터다.

-- 파생 검색 테이블. 부모(교수·직원·연구·연구실·학회·학사·교과목·장학)에서 본문을 다시 만든다.
DROP TABLE academics_search;
DROP TABLE member_search;
DROP TABLE research_search;

-- 파생 검색 컬럼. 컬럼이 사라지면 그 위의 FULLTEXT 인덱스도 함께 사라진다.
ALTER TABLE about_translation DROP COLUMN search_content;
ALTER TABLE admissions DROP COLUMN search_content;

-- 게시판 목록의 키워드 검색도 ES 로 옮겨서 더는 쓰지 않는다.
DROP INDEX IDX_notice_title_description_fulltext ON notice;
DROP INDEX IDX_news_title_description_fulltext ON news;
DROP INDEX IDX_seminar_multicolumn_fulltext ON seminar;
