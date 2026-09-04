-- 엔티티가 사라진 채 남아 있던 테이블(둘 다 0행)과 한글 컬럼(값 전부 NULL), 엔티티에 없는 attachment.research_id(값 전부 NULL).
DROP TABLE about_attachments;
DROP TABLE location;
ALTER TABLE notice DROP COLUMN `열_name`;
ALTER TABLE seminar DROP COLUMN `열_name`;
ALTER TABLE attachment DROP FOREIGN KEY FK_attachment_research_research_id;
ALTER TABLE attachment DROP COLUMN research_id;
-- 연구실 PDF의 살아 있는 연결은 lab.pdf_id 다. attachment.lab_id 는 2024-03까지 같이 채우던 잔재(16행, 전부 pdf_id 와 중복·1행은 틀린 값).
ALTER TABLE attachment DROP FOREIGN KEY FK_attachment_lab_lab_id;
ALTER TABLE attachment DROP COLUMN lab_id;

-- 첨부는 주인 FK 7개 중 하나만 채운다(배타적 아크). 지금까지는 코드 관례였고 DB는 몰랐다.
-- 0개도 허용하는 이유: 연구실 소개 PDF는 lab.pdf_id 로 반대 방향 연결이라 attachment 쪽은 전부 NULL이다.
ALTER TABLE attachment ADD CONSTRAINT chk_attachment_single_owner CHECK (
    (notice_id IS NOT NULL) + (news_id IS NOT NULL) + (seminar_id IS NOT NULL) + (about_id IS NOT NULL)
  + (academics_id IS NOT NULL) + (course_id IS NOT NULL) + (scholarship_id IS NOT NULL)
  <= 1
);
