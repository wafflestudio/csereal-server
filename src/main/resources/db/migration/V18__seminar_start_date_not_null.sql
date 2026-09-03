-- start_date는 등록 폼이 필수로 받고 목록 연도 구분·상세 날짜 표시가 non-null을 전제한다
-- (null이면 화면에 Invalid Date). 컬럼·엔티티·DTO를 함께 조인다.
-- 조이기 전 혹시 남은 null을 created_at으로 채운다 — 운영 실측 0건이라 사실상 무동작.
UPDATE seminar SET start_date = created_at WHERE start_date IS NULL;
ALTER TABLE seminar MODIFY start_date datetime(6) NOT NULL;
