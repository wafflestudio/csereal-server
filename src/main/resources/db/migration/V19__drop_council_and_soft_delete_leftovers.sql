-- 학생회(council) 도메인 제거. 화면·호출부가 없어 API와 함께 걷어낸다.
DELETE FROM attachment WHERE council_file_id IS NOT NULL;
ALTER TABLE attachment DROP FOREIGN KEY FK_attachment_council_file_id;
ALTER TABLE attachment DROP COLUMN council_file_id;
DROP TABLE council_file;
ALTER TABLE council DROP FOREIGN KEY FK_council_main_image_main_image_id;
DELETE FROM main_image WHERE id IN (SELECT main_image_id FROM council WHERE main_image_id IS NOT NULL);
DROP TABLE council;

-- 첨부·대표이미지의 소프트 삭제 잔재. 삭제는 이제 행 삭제(orphanRemoval)로 통일됐다.
DELETE FROM attachment WHERE is_deleted = 1;
DELETE FROM main_image WHERE is_deleted = 1;
-- 교체 시 옛 행을 지우지 않던 시절의 고아 대표이미지(어느 테이블도 가리키지 않음)
DELETE FROM main_image
WHERE NOT EXISTS (SELECT 1 FROM about t WHERE t.main_image_id = main_image.id)
  AND NOT EXISTS (SELECT 1 FROM image_modal t WHERE t.main_image_id = main_image.id)
  AND NOT EXISTS (SELECT 1 FROM news t WHERE t.main_image_id = main_image.id)
  AND NOT EXISTS (SELECT 1 FROM professor t WHERE t.main_image_id = main_image.id)
  AND NOT EXISTS (SELECT 1 FROM recruit t WHERE t.main_image_id = main_image.id)
  AND NOT EXISTS (SELECT 1 FROM research t WHERE t.main_image_id = main_image.id)
  AND NOT EXISTS (SELECT 1 FROM seminar t WHERE t.main_image_id = main_image.id)
  AND NOT EXISTS (SELECT 1 FROM staff t WHERE t.main_image_id = main_image.id);
ALTER TABLE attachment DROP COLUMN is_deleted;
ALTER TABLE attachment DROP COLUMN attachments_order;
ALTER TABLE main_image DROP COLUMN is_deleted;
