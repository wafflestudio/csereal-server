-- Drop existing FULLTEXT indexes and recreate with ngram parser

-- about.search_content
DROP INDEX IDX_about_search_content_fulltext ON about;
CREATE FULLTEXT INDEX IDX_about_search_content_fulltext ON about(search_content) WITH PARSER ngram;

-- academics_search.content
DROP INDEX IDX_academics_search_content_fulltext ON academics_search;
CREATE FULLTEXT INDEX IDX_academics_search_content_fulltext ON academics_search(content) WITH PARSER ngram;

-- admissions.search_content
DROP INDEX IDX_admissions_search_content_fulltext ON admissions;
CREATE FULLTEXT INDEX IDX_admissions_search_content_fulltext ON admissions(search_content) WITH PARSER ngram;

-- member_search.content
DROP INDEX IDX_member_search_content_fulltext ON member_search;
CREATE FULLTEXT INDEX IDX_member_search_content_fulltext ON member_search(content) WITH PARSER ngram;

-- news(title, plain_text_description)
DROP INDEX IDX_news_title_description_fulltext ON news;
CREATE FULLTEXT INDEX IDX_news_title_description_fulltext ON news(title, plain_text_description) WITH PARSER ngram;

-- notice(title, plain_text_description)
DROP INDEX IDX_notice_title_description_fulltext ON notice;
CREATE FULLTEXT INDEX IDX_notice_title_description_fulltext ON notice(title, plain_text_description) WITH PARSER ngram;

-- research_search.content
DROP INDEX IDX_research_search_content_fulltext ON research_search;
CREATE FULLTEXT INDEX IDX_research_search_content_fulltext ON research_search(content) WITH PARSER ngram;

-- seminar(7 columns)
DROP INDEX IDX_seminar_multicolumn_fulltext ON seminar;
CREATE FULLTEXT INDEX IDX_seminar_multicolumn_fulltext ON seminar(
    title, name, affiliation, location, plain_text_description, 
    plain_text_introduction, plain_text_additional_note
) WITH PARSER ngram;
