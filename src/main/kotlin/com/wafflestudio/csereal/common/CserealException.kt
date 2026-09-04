package com.wafflestudio.csereal.common

import com.fasterxml.jackson.annotation.JsonValue
import org.springframework.http.HttpStatus

enum class ErrorCode(val status: HttpStatus, @get:JsonValue val code: String, val description: String) {
    // 프레임워크·인프라
    INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR, "SYS-00", "처리되지 않은 예외"),
    DATA_DUPLICATION(HttpStatus.CONFLICT, "SYS-01", "DB 유니크 제약 위반"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "SYS-02", "요청 본문·파라미터 검증 실패(fields 참고)"),
    OIDC_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "SYS-03", "OIDC 공급자 호출 실패"),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "SYS-04", "JSON 파싱 실패·타입 불일치·파라미터/파트 누락"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "SYS-05", "지원하지 않는 Content-Type"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "SYS-06", "없는 경로"),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "SYS-07", "로그인 필요"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "SYS-08", "권한 없음"),
    INVALID_ENUM_VALUE(HttpStatus.BAD_REQUEST, "SYS-09", "enum 에 없는 값"),
    LANGUAGE_MISMATCH(HttpStatus.BAD_REQUEST, "SYS-10", "요청한 언어와 데이터의 언어가 다름"),
    PRIVATE_POST(HttpStatus.FORBIDDEN, "SYS-11", "비공개 글은 교직원만 볼 수 있음"),
    SEARCH_INDEX_BROKEN(HttpStatus.INTERNAL_SERVER_ERROR, "SYS-12", "검색 색인이 어느 글에도 연결돼 있지 않음"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "SYS-13", "그 경로가 허용하지 않는 HTTP 메서드"),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "SYS-14", "클라이언트가 받을 수 있는 형식으로 응답할 수 없음"),
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "SYS-15", "업로드 용량 초과"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "SYS-16", "처리 시간 초과 등 일시적 불가"),

    // 게시글
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE-01", "공지 없음"),
    NOTICE_TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE-02", "공지 태그 없음"),
    NEWS_NOT_FOUND(HttpStatus.NOT_FOUND, "NEWS-01", "새소식 없음"),
    NEWS_TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "NEWS-02", "새소식 태그 없음"),
    SEMINAR_NOT_FOUND(HttpStatus.NOT_FOUND, "SEMINAR-01", "세미나 없음"),
    CONFERENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "CONFERENCE-01", "학회 없음"),
    INTERNAL_NOT_FOUND(HttpStatus.NOT_FOUND, "INTERNAL-01", "내부 페이지 없음"),
    ADMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "ADMISSIONS-01", "입학 페이지 없음"),
    IMAGE_MODAL_NOT_FOUND(HttpStatus.NOT_FOUND, "IMAGEMODAL-01", "이미지 모달 없음"),

    // 소개
    FACILITY_NOT_FOUND(HttpStatus.NOT_FOUND, "ABOUT-01", "시설 없음"),
    CLUB_NOT_FOUND(HttpStatus.NOT_FOUND, "ABOUT-02", "동아리 없음"),
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "ABOUT-03", "진로 회사 없음"),
    DIRECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "ABOUT-04", "찾아오는 길 없음"),
    STAT_YEAR_ALREADY_EXISTS(HttpStatus.CONFLICT, "ABOUT-05", "해당 연도 통계가 이미 있음"),
    STAT_ROWS_REQUIRED(HttpStatus.BAD_REQUEST, "ABOUT-06", "통계 행이 전부 필요"),
    ABOUT_NOT_FOUND(HttpStatus.NOT_FOUND, "ABOUT-07", "소개 글 없음"),

    // 학사
    ACADEMICS_NOT_FOUND(HttpStatus.NOT_FOUND, "ACADEMICS-01", "학사 글 없음"),
    GUIDE_NOT_FOUND(HttpStatus.NOT_FOUND, "ACADEMICS-02", "안내 없음"),
    DEGREE_REQUIREMENTS_NOT_FOUND(HttpStatus.NOT_FOUND, "ACADEMICS-03", "졸업 요건 없음"),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "ACADEMICS-04", "교과목 없음"),
    COURSE_CODE_DUPLICATED(HttpStatus.CONFLICT, "ACADEMICS-05", "같은 학수번호가 이미 있음"),
    SCHOLARSHIP_NOT_FOUND(HttpStatus.NOT_FOUND, "ACADEMICS-06", "장학제도 없음"),
    YEAR_ALREADY_EXISTS(HttpStatus.CONFLICT, "ACADEMICS-07", "해당 연도 항목이 이미 있음"),

    // 구성원
    PROFESSOR_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-01", "교수 없음"),
    PROFESSOR_PAIR_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-02", "교수 한/영 쌍 없음"),
    STAFF_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-03", "행정직원 없음"),
    STAFF_PAIR_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-04", "행정직원 한/영 쌍 없음"),

    // 연구
    RESEARCH_NOT_FOUND(HttpStatus.NOT_FOUND, "RESEARCH-01", "연구 글 없음"),
    RESEARCH_PAIR_NOT_FOUND(HttpStatus.NOT_FOUND, "RESEARCH-02", "연구 글 한/영 쌍 없음"),
    RESEARCH_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "RESEARCH-03", "한/영 글의 종류(그룹·센터)가 다름"),
    RESEARCH_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "RESEARCH-04", "연구 그룹 없음"),
    NOT_A_RESEARCH_GROUP(HttpStatus.BAD_REQUEST, "RESEARCH-05", "연구 그룹이 아닌 글"),
    LAB_NOT_FOUND(HttpStatus.NOT_FOUND, "RESEARCH-06", "연구실 없음"),
    LAB_PAIR_NOT_FOUND(HttpStatus.NOT_FOUND, "RESEARCH-07", "연구실 한/영 쌍 없음"),
    PROFESSORS_NOT_FOUND(HttpStatus.NOT_FOUND, "RESEARCH-08", "교수 목록 중 없는 교수"),
    PROFESSOR_ALREADY_IN_LAB(HttpStatus.CONFLICT, "RESEARCH-09", "이미 다른 연구실 소속인 교수"),

    // 파일
    ATTACHMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE-01", "첨부 없음"),
    INVALID_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "FILE-02", "대표이미지는 jpg·jpeg·png 만"),

    // 예약
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVE-01", "강의실 없음"),
    ONLY_SEMINAR_RESERVABLE(HttpStatus.FORBIDDEN, "RESERVE-02", "일반 예약 권한은 세미나실만"),
    PROFESSOR_ROOM_DENIED(HttpStatus.FORBIDDEN, "RESERVE-03", "교수회의실은 교직원·교수만"),
    LABMASTER_ONLY(HttpStatus.FORBIDDEN, "RESERVE-04", "정기예약 기간엔 랩 대표만"),
    INVALID_RESERVATION_PERIOD(HttpStatus.BAD_REQUEST, "RESERVE-05", "정기예약은 학기 안에서만"),
    RESERVATION_TIME_EXCEEDED(HttpStatus.BAD_REQUEST, "RESERVE-06", "비교직원은 같은 날 회차당 3시간까지"),
    TERM_NOT_REGISTERED(HttpStatus.FORBIDDEN, "RESERVE-07", "등록되지 않은 기간"),
    TERM_NOT_OPENED(HttpStatus.FORBIDDEN, "RESERVE-08", "정기예약 신청 기간 전"),
    RESERVATION_OCCUPIED(HttpStatus.CONFLICT, "RESERVE-09", "그 시간에 이미 예약 있음"),
    INVALID_RECURRING_WEEKS(HttpStatus.BAD_REQUEST, "RESERVE-10", "반복 횟수 범위 밖"),
    ONE_TIME_RECURRING_DENIED(HttpStatus.BAD_REQUEST, "RESERVE-11", "수시 예약은 반복 불가"),
    ONE_TIME_NOT_OPENED(HttpStatus.FORBIDDEN, "RESERVE-12", "수시 예약 기간 전"),
    PAST_RESERVATION_DENIED(HttpStatus.BAD_REQUEST, "RESERVE-13", "과거 시각"),
    TERM_APPLICATION_CLOSED(HttpStatus.FORBIDDEN, "RESERVE-14", "정기예약 신청 기간 종료"),
    INVALID_RESERVATION_TIME(HttpStatus.BAD_REQUEST, "RESERVE-15", "종료가 시작보다 앞"),
    RESERVATION_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "RESERVE-16", "예약 권한 없음"),
    UNSUPPORTED_RESERVATION_DATE(HttpStatus.BAD_REQUEST, "RESERVE-17", "지원하지 않는 날짜 범위"),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVE-18", "예약 없음"),
    CANNOT_CANCEL_OTHERS_RESERVATION(HttpStatus.FORBIDDEN, "RESERVE-19", "남의 예약은 취소 불가"),
    POLICY_NOT_AGREED(HttpStatus.BAD_REQUEST, "RESERVE-20", "예약 규정 미동의"),
    TERM_OVERLAP(HttpStatus.CONFLICT, "RESERVE-21", "예약 기간이 겹침")
}

class CserealException(
    val errorCode: ErrorCode,
    val params: Map<String, Any?> = emptyMap(),
    cause: Throwable? = null
) : RuntimeException(
    "${errorCode.code} ${errorCode.description}" + if (params.isEmpty()) "" else " $params",
    cause
) {
    val status: HttpStatus get() = errorCode.status
}
