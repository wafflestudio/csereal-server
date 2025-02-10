create table about
(
    id             bigint auto_increment
        primary key,
    created_at     datetime(6) default '1999-01-01 00:00:00.000000'                                                                    null,
    modified_at    datetime(6) default '1999-01-01 00:00:00.000000'                                                                    null,
    description    mediumtext                                                                                                          null,
    eng_name       varchar(255)                                                                                                        null,
    name           varchar(255)                                                                                                        null,
    post_type      enum ('OVERVIEW', 'GREETINGS', 'HISTORY', 'FUTURE_CAREERS', 'CONTACT', 'STUDENT_CLUBS', 'FACILITIES', 'DIRECTIONS') null,
    year           int                                                                                                                 null,
    main_image_id  bigint                                                                                                              null,
    language       enum ('KO', 'EN')                                                                                                   null,
    locations      text                                                                                                                null,
    search_content text                                                                                                                null
);

create fulltext index IDX_about_search_content_fulltext
    on about (search_content);

create table about_attachments
(
    about_id       bigint not null,
    attachments_id bigint not null
);

alter table about_attachments
    add constraint UQ_about_attachments_attachments_id
        unique (attachments_id);

alter table about_attachments
    add constraint FK_about_attachments_about_about_id
        foreign key (about_id) references about (id);

create table about_language
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at datetime(6) default '1999-01-01 00:00:00.000000' null,
    english_id  bigint                                           null,
    korean_id   bigint                                           null
);

alter table about_language
    add constraint UQ_about_language_korean_id
        unique (korean_id);

alter table about_language
    add constraint UQ_about_language_english_id
        unique (english_id);

alter table about_language
    add constraint FK_about_language_about_english_id
        foreign key (english_id) references about (id);

alter table about_language
    add constraint FK_about_language_about_korean_id
        foreign key (korean_id) references about (id);

create table academics
(
    id           bigint auto_increment
        primary key,
    created_at   datetime(6) default '1999-01-01 00:00:00.000000'                                                                                                                                                      null,
    modified_at  datetime(6) default '1999-01-01 00:00:00.000000'                                                                                                                                                      null,
    description  mediumtext                                                                                                                                                                                            null,
    name         varchar(255)                                                                                                                                                                                          null,
    post_type    enum ('GUIDE', 'GENERAL_STUDIES_REQUIREMENTS', 'GENERAL_STUDIES_REQUIREMENTS_SUBJECT_CHANGES', 'CURRICULUM', 'DEGREE_REQUIREMENTS', 'DEGREE_REQUIREMENTS_YEAR_LIST', 'COURSE_CHANGES', 'SCHOLARSHIP') null,
    student_type enum ('UNDERGRADUATE', 'GRADUATE')                                                                                                                                                                    null,
    time         varchar(255)                                                                                                                                                                                          null,
    year         int                                                                                                                                                                                                   null,
    language     enum ('KO', 'EN')                                                                                                                                                                                     null
);

create table academics_search
(
    id             bigint auto_increment
        primary key,
    created_at     datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at    datetime(6) default '1999-01-01 00:00:00.000000' null,
    content        text                                             not null,
    academics_id   bigint                                           null,
    course_id      bigint                                           null,
    scholarship_id bigint                                           null,
    language       enum ('KO', 'EN')                                null
);

create fulltext index IDX_academics_search_content_fulltext
    on academics_search (content);

alter table academics_search
    add constraint FK_academics_search_academics_academics_id
        foreign key (academics_id) references academics (id);

create table admissions
(
    id             bigint auto_increment
        primary key,
    created_at     datetime(6) default '1999-01-01 00:00:00.000000'                                                                null,
    modified_at    datetime(6) default '1999-01-01 00:00:00.000000'                                                                null,
    description    mediumtext                                                                                                      null,
    post_type      enum ('EARLY_ADMISSION', 'REGULAR_ADMISSION', 'UNDERGRADUATE', 'GRADUATE', 'EXCHANGE_VISITING', 'SCHOLARSHIPS') null,
    language       enum ('KO', 'EN')                                                                                               null,
    main_type      enum ('UNDERGRADUATE', 'GRADUATE', 'INTERNATIONAL')                                                             null,
    name           varchar(255)                                                                                                    null,
    search_content mediumtext                                                                                                      not null
);

create fulltext index IDX_admissions_search_content_fulltext
    on admissions (search_content);

alter table admissions
    add constraint UQ_admissions_language_main_type_post_type
        unique (language, main_type, post_type);

create table attachment
(
    id                bigint auto_increment
        primary key,
    created_at        datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at       datetime(6) default '1999-01-01 00:00:00.000000' null,
    attachments_order int                                              not null,
    filename          varchar(255)                                     null,
    is_deleted        bit                                              null,
    size              bigint                                           not null,
    about_id          bigint                                           null,
    academics_id      bigint                                           null,
    course_id         bigint                                           null,
    lab_id            bigint                                           null,
    news_id           bigint                                           null,
    notice_id         bigint                                           null,
    research_id       bigint                                           null,
    scholarship_id    bigint                                           null,
    seminar_id        bigint                                           null
);

alter table about_attachments
    add constraint FK_about_attachments_about_attachments_id
        foreign key (attachments_id) references attachment (id);

alter table attachment
    add constraint UQ_attachment_filename
        unique (filename);

alter table attachment
    add constraint FK_attachment_academics_academics_id
        foreign key (academics_id) references academics (id);

alter table attachment
    add constraint FK_attachment_about_about_id
        foreign key (about_id) references about (id);

create table career
(
    id           bigint auto_increment
        primary key,
    created_at   datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    name         mediumtext                                       null,
    professor_id bigint                                           null
);

create table company
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at datetime(6) default '1999-01-01 00:00:00.000000' null,
    name        varchar(255)                                     null,
    url         varchar(255)                                     null,
    year        int                                              not null
);

create table conference
(
    id                 bigint auto_increment
        primary key,
    created_at         datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at        datetime(6) default '1999-01-01 00:00:00.000000' null,
    abbreviation       varchar(255)                                     null,
    name               varchar(255)                                     null,
    conference_page_id bigint                                           null,
    is_deleted         bit                                              not null,
    language           enum ('KO', 'EN')                                null
);

create table conference_page
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at datetime(6) default '1999-01-01 00:00:00.000000' null,
    author_id   bigint                                           null
);

alter table conference
    add constraint FKo7i5yihmfex51pi3xmw0u5r6n
        foreign key (conference_page_id) references conference_page (id);

create table council
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at datetime(6) default '1999-01-01 00:00:00.000000' null
);

create table course
(
    id             bigint auto_increment
        primary key,
    created_at     datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at    datetime(6) default '1999-01-01 00:00:00.000000' null,
    classification varchar(255)                                     null,
    code           varchar(255)                                     null,
    credit         int                                              not null,
    description    mediumtext                                       null,
    grade          int                                              null,
    name           varchar(255)                                     null,
    language       enum ('KO', 'EN')                                null,
    student_type   enum ('GRADUATE', 'UNDERGRADUATE')               null
);

alter table academics_search
    add constraint FK_academics_search_course_course_id
        foreign key (course_id) references course (id);

alter table attachment
    add constraint FK_attachment_course_course_id
        foreign key (course_id) references course (id);

create table education
(
    id           bigint auto_increment
        primary key,
    created_at   datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    name         varchar(255)                                     null,
    professor_id bigint                                           null
);

create table internal
(
    id          bigint auto_increment
        primary key,
    description text                                             null,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at datetime(6) default '1999-01-01 00:00:00.000000' null
);

create table lab
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at datetime(6) default '1999-01-01 00:00:00.000000' null,
    acronym     varchar(255)                                     null,
    description mediumtext                                       null,
    location    varchar(255)                                     null,
    name        varchar(255)                                     null,
    tel         varchar(255)                                     null,
    websiteurl  varchar(255)                                     null,
    youtube     varchar(255)                                     null,
    pdf_id      bigint                                           null,
    research_id bigint                                           null,
    language    tinyint                                          null
);

alter table attachment
    add constraint FK_attachment_lab_lab_id
        foreign key (lab_id) references lab (id);

alter table lab
    add constraint FK_lab_attachment_pdf_id
        foreign key (pdf_id) references attachment (id);

create table location
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at datetime(6) default '1999-01-01 00:00:00.000000' null,
    name        varchar(255)                                     null,
    about_id    bigint                                           null
);

alter table location
    add constraint FK_location_about_about_id
        foreign key (about_id) references about (id);

create table main_image
(
    id           bigint auto_increment
        primary key,
    created_at   datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    filename     varchar(255)                                     null,
    images_order int                                              not null,
    is_deleted   bit                                              null,
    size         bigint                                           not null
);

alter table about
    add constraint FK_about_main_image_main_image_id
        foreign key (main_image_id) references main_image (id);

alter table main_image
    add constraint UQ_main_image_filename
        unique (filename);

create table member_language
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at datetime(6) default '1999-01-01 00:00:00.000000' null,
    english_id  bigint                                           not null,
    korean_id   bigint                                           not null,
    type        enum ('PROFESSOR', 'STAFF')                      not null
);

create index IDX_member_language_korean_id
    on member_language (korean_id);

create index IDX_member_language_english_id
    on member_language (english_id);

create index IDX_member_language_type
    on member_language (type);

create table member_search
(
    id           bigint auto_increment
        primary key,
    created_at   datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    content      text                                             null,
    professor_id bigint                                           null,
    staff_id     bigint                                           null,
    language     tinyint                                          null
);

create fulltext index IDX_member_search_content_fulltext
    on member_search (content);

create table news
(
    id                     bigint auto_increment
        primary key,
    created_at             datetime(6) default '1999-01-01 00:00:00.000000' null,
    migrate_id             bigint                                           null,
    modified_at            datetime(6) default '1999-01-01 00:00:00.000000' null,
    description            mediumtext                                       null,
    is_deleted             bit                                              not null,
    is_important           bit                                              not null,
    is_private             bit                                              not null,
    is_slide               bit                                              not null,
    plain_text_description mediumtext                                       null,
    title                  varchar(255)                                     null,
    title_for_main         text                                             null,
    main_image_id          bigint                                           null,
    date                   datetime(6)                                      null
);

alter table attachment
    add constraint FK_attachment_news_news_id
        foreign key (news_id) references news (id);

create fulltext index IDX_news_title_description_fulltext
    on news (title, plain_text_description);

alter table news
    add constraint FK_news_main_image_main_image_id
        foreign key (main_image_id) references main_image (id);

create table news_tag
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at datetime(6) default '1999-01-01 00:00:00.000000' null,
    news_id     bigint                                           null,
    tag_id      bigint                                           null
);

alter table news_tag
    add constraint FK_news_tag_news_news_id
        foreign key (news_id) references news (id);

create table notice
(
    id                     bigint auto_increment
        primary key,
    migrate_id             bigint                                           null,
    created_at             datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at            datetime(6) default '1999-01-01 00:00:00.000000' null,
    description            mediumtext                                       null,
    is_deleted             bit                                              not null,
    is_important           bit                                              not null,
    is_pinned              bit                                              not null,
    is_private             bit                                              not null,
    plain_text_description mediumtext                                       null,
    title                  varchar(255)                                     null,
    users_id               bigint                                           null,
    열_name                 int                                              null,
    title_for_main         text                                             null
);

alter table attachment
    add constraint FK_attachment_notice_notice_id
        foreign key (notice_id) references notice (id);

create fulltext index IDX_notice_title_description_fulltext
    on notice (title, plain_text_description);

create table notice_tag
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at datetime(6) default '1999-01-01 00:00:00.000000' null,
    notice_id   bigint                                           null,
    tag_id      bigint                                           null
);

alter table notice_tag
    add constraint FK_notice_tag_notice_notice_id
        foreign key (notice_id) references notice (id);

create table professor
(
    id            bigint auto_increment
        primary key,
    created_at    datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at   datetime(6) default '1999-01-01 00:00:00.000000' null,
    academic_rank varchar(255)                                     null,
    email         varchar(255)                                     null,
    end_date      date                                             null,
    fax           varchar(255)                                     null,
    name          varchar(255)                                     null,
    office        varchar(255)                                     null,
    phone         varchar(255)                                     null,
    start_date    date                                             null,
    status        enum ('ACTIVE', 'INACTIVE', 'VISITING')          null,
    website       varchar(255)                                     null,
    lab_id        bigint                                           null,
    main_image_id bigint                                           null,
    language      enum ('KO', 'EN')                                null
);

alter table career
    add constraint FK_career_professor_professor_id
        foreign key (professor_id) references professor (id);

alter table education
    add constraint FK_education_professor_professor_id
        foreign key (professor_id) references professor (id);

alter table member_search
    add constraint FK_member_search_professor_professor_id
        foreign key (professor_id) references professor (id);

alter table professor
    add constraint FK_professor_main_image_main_image_id
        foreign key (main_image_id) references main_image (id);

alter table professor
    add constraint FK_professor_lab_lab_id
        foreign key (lab_id) references lab (id);

create table recruit
(
    id                   bigint auto_increment
        primary key,
    created_at           datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at          datetime(6) default '1999-01-01 00:00:00.000000' null,
    description          text                                             null,
    latest_recruit_title varchar(255)                                     null,
    latest_recruit_url   varchar(255)                                     null,
    title                varchar(255)                                     null,
    main_image_id        bigint                                           null
);

alter table recruit
    add constraint UK_m4l21yiolw4a4g7cgvgptj1ly
        unique (main_image_id);

alter table recruit
    add constraint FK_recruit_main_image_main_image_id
        foreign key (main_image_id) references main_image (id);

create table research
(
    id            bigint auto_increment
        primary key,
    created_at    datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at   datetime(6) default '1999-01-01 00:00:00.000000' null,
    description   mediumtext                                       null,
    name          varchar(255)                                     null,
    post_type     enum ('GROUPS', 'CENTERS')                       null,
    main_image_id bigint                                           null,
    language      enum ('KO', 'EN')                                null,
    websiteurl    varchar(255)                                     null
);

alter table attachment
    add constraint FK_attachment_research_research_id
        foreign key (research_id) references research (id);

alter table lab
    add constraint FK_lab_research_research_id
        foreign key (research_id) references research (id);

alter table research
    add constraint FK_research_main_image_main_image_id
        foreign key (main_image_id) references main_image (id);

create table research_area
(
    id           bigint auto_increment
        primary key,
    created_at   datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    name         varchar(255)                                     null,
    professor_id bigint                                           null
);

alter table research_area
    add constraint FK_research_area_professor_professor_id
        foreign key (professor_id) references professor (id);

create table research_language
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000'                null,
    modified_at datetime(6) default '1999-01-01 00:00:00.000000'                null,
    english_id  bigint                                                          not null,
    korean_id   bigint                                                          not null,
    type        enum ('RESEARCH_GROUP', 'RESEARCH_CENTER', 'LAB', 'CONFERENCE') not null
);

alter table research_language
    add constraint UK_research_language_korean_id_english_id_type
        unique (korean_id, english_id, type);

alter table research_language
    add constraint research_language_unique
        unique (korean_id, english_id, type);

create table research_search
(
    id            bigint auto_increment
        primary key,
    created_at    datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at   datetime(6) default '1999-01-01 00:00:00.000000' null,
    content       text                                             null,
    conference_id bigint                                           null,
    lab_id        bigint                                           null,
    research_id   bigint                                           null,
    language      enum ('KO', 'EN')                                null
);

create fulltext index IDX_research_search_content_fulltext
    on research_search (content);

alter table research_search
    add constraint FK_research_search_research_research_id
        foreign key (research_id) references research (id);

alter table research_search
    add constraint FK_research_search_conference_conference_id
        foreign key (conference_id) references conference (id);

alter table research_search
    add constraint FK_research_search_lab_lab_id
        foreign key (lab_id) references lab (id);

create table reservation
(
    id              bigint auto_increment
        primary key,
    created_at      datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at     datetime(6) default '1999-01-01 00:00:00.000000' null,
    contact_email   varchar(255)                                     null,
    contact_phone   varchar(255)                                     null,
    end_time        datetime(6)                                      null,
    professor       varchar(255)                                     null,
    purpose         varchar(255)                                     null,
    recurrence_id   binary(16)                                       null,
    recurring_weeks int                                              not null,
    start_time      datetime(6)                                      null,
    title           varchar(255)                                     null,
    room_id         bigint                                           null,
    user_id         bigint                                           null,
    agreed          bit                                              not null
);

create table room
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at datetime(6) default '1999-01-01 00:00:00.000000' null,
    capacity    int                                              not null,
    location    varchar(255)                                     null,
    name        varchar(255)                                     null,
    type        enum ('LAB', 'LECTURE', 'SEMINAR')               null
);

alter table reservation
    add constraint FK_reservation_room_room_id
        foreign key (room_id) references room (id);

create table scholarship
(
    id           bigint auto_increment
        primary key,
    created_at   datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    description  text                                             null,
    name         varchar(255)                                     null,
    student_type enum ('UNDERGRADUATE', 'GRADUATE')               null,
    language     enum ('KO', 'EN')                                null
);

alter table academics_search
    add constraint FK_academics_search_scholarship_scholarship_id
        foreign key (scholarship_id) references scholarship (id);

alter table attachment
    add constraint FK_attachment_scholarship_scholarship_id
        foreign key (scholarship_id) references scholarship (id);

create table scholarship_language
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at datetime(6) default '1999-01-01 00:00:00.000000' null,
    english_id  bigint                                           null,
    korean_id   bigint                                           null
);

alter table scholarship_language
    add constraint UK_scholarship_en_id
        unique (english_id);

alter table scholarship_language
    add constraint UK_scholarship_ko_id
        unique (korean_id);

alter table scholarship_language
    add constraint FK_scholarship_en_id
        foreign key (english_id) references scholarship (id);

alter table scholarship_language
    add constraint FK_scholarship_ko_id
        foreign key (korean_id) references scholarship (id);

create table seminar
(
    id                         bigint auto_increment
        primary key,
    created_at                 datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at                datetime(6) default '1999-01-01 00:00:00.000000' null,
    additional_note            text                                             null,
    affiliation                varchar(255)                                     null,
    affiliationurl             varchar(255)                                     null,
    description                mediumtext                                       null,
    end_date                   datetime(6)                                      null,
    host                       varchar(255)                                     null,
    introduction               mediumtext                                       null,
    migrate_id                 bigint                                           null,
    is_deleted                 bit                                              not null,
    is_important               bit                                              not null,
    is_private                 bit                                              not null,
    location                   varchar(255)                                     null,
    name                       varchar(255)                                     null,
    plain_text_additional_note text                                             null,
    plain_text_description     mediumtext                                       null,
    plain_text_introduction    mediumtext                                       null,
    speaker_title              varchar(255)                                     null,
    speakerurl                 varchar(2047)                                    null,
    start_date                 datetime(6)                                      null,
    title                      varchar(255)                                     null,
    main_image_id              bigint                                           null,
    열_name                     int                                              null,
    title_for_main             text                                             null
);

alter table attachment
    add constraint FK_attachment_seminar_seminar_id
        foreign key (seminar_id) references seminar (id);

create fulltext index IDX_seminar_multicolumn_fulltext
    on seminar (title, name, affiliation, location, plain_text_description, plain_text_introduction, plain_text_additional_note);

alter table seminar
    add constraint FK_seminar_main_image_main_image_id
        foreign key (main_image_id) references main_image (id);

create table staff
(
    id            bigint auto_increment
        primary key,
    created_at    datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at   datetime(6) default '1999-01-01 00:00:00.000000' null,
    email         varchar(255)                                     null,
    name          varchar(255)                                     null,
    office        varchar(255)                                     null,
    phone         varchar(255)                                     null,
    role          varchar(255)                                     null,
    main_image_id bigint                                           null,
    language      enum ('KO', 'EN')                                null
);

alter table member_search
    add constraint FK_member_search_staff_staff_id
        foreign key (staff_id) references staff (id);

alter table staff
    add constraint FK_staff_main_image_main_image_id
        foreign key (main_image_id) references main_image (id);

create table stat
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at datetime(6) default '1999-01-01 00:00:00.000000' null,
    count       int                                              not null,
    degree      enum ('BACHELOR', 'MASTER', 'DOCTOR')            null,
    name        varchar(255)                                     null,
    year        int                                              not null
);

create table tag_in_news
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000'                                                                         null,
    modified_at datetime(6) default '1999-01-01 00:00:00.000000'                                                                         null,
    name        enum ('EVENT', 'RESEARCH', 'AWARDS', 'RECRUIT', 'COLUMN', 'LECTURE', 'EDUCATION', 'INTERVIEW', 'CAREER', 'UNCLASSIFIED') null
);

alter table news_tag
    add constraint FK_news_tag_tag_tag_id
        foreign key (tag_id) references tag_in_news (id);

create table tag_in_notice
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000'                                                                                                                                                                               null,
    modified_at datetime(6) default '1999-01-01 00:00:00.000000'                                                                                                                                                                               null,
    name        enum ('CLASS', 'SCHOLARSHIP', 'UNDERGRADUATE', 'GRADUATE', 'MINOR', 'REGISTRATIONS', 'ADMISSIONS', 'GRADUATIONS', 'RECRUIT', 'STUDENT_EXCHANGE', 'INNER_EVENTS_PROGRAMS', 'OUTER_EVENTS_PROGRAMS', 'FOREIGN', 'INTERNATIONAL') null
);

alter table notice_tag
    add constraint FK_notice_tag_tag_tag_id
        foreign key (tag_id) references tag_in_notice (id);

create table task
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000' null,
    modified_at datetime(6) default '1999-01-01 00:00:00.000000' null,
    name        varchar(255)                                     null,
    staff_id    bigint                                           null
);

alter table task
    add constraint FK_task_staff_staff_id
        foreign key (staff_id) references staff (id);

create table users
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000'       null,
    modified_at datetime(6) default '1999-01-01 00:00:00.000000'       null,
    email       varchar(255)                                           null,
    name        varchar(255)                                           null,
    role        enum ('ROLE_GRADUATE', 'ROLE_PROFESSOR', 'ROLE_STAFF') null,
    student_id  varchar(255)                                           null,
    username    varchar(255)                                           not null
);

alter table conference_page
    add constraint FK_conference_page_author_author_id
        foreign key (author_id) references users (id);

alter table notice
    add constraint FK_notice_users_users_id
        foreign key (users_id) references users (id);

alter table reservation
    add constraint FK_reservation_users_user_id
        foreign key (user_id) references users (id);

alter table users
    add constraint UQ_users_username
        unique (username);

