package com.wafflestudio.csereal.common.domain

enum class Domain {
    // 소개
    ABOUT,              // Department information, statistics, student clubs

    // 학사 및 교과
    ACADEMICS,          // Degree programs, courses, scholarships

    // 입학
    ADMISSIONS,         // Admission requirements and procedures

    // 소식
    NEWS,               // News articles with tagging and scheduling
    NOTICE,             // Official notices with expiration dates
    SEMINAR,            // Seminar scheduling and management
    RECRUIT,            // Recruitment information
    COUNCIL,            // Student council information

    // 구성원
    PROFESSOR,          // Professor management (member subdomain)
    STAFF,              // Staff management (member subdomain)

    // 연구 교육
    RESEARCH,           // Research groups and laboratories
    LAB,                // Laboratory management (research subdomain)
    CONFERENCE,         // Conference information

    // 시설 예약
    RESERVATION,        // Room reservation system
    ROOM,               // Room information (reservation subdomain)

    // 메일링 리스트
    INTERNAL,           // Internal information
}
