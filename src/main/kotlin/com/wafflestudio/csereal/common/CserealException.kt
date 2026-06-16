package com.wafflestudio.csereal.common

import org.springframework.http.HttpStatus

open class CserealException(
    msg: String,
    val status: HttpStatus,
    val code: String? = null
) : RuntimeException(msg) {

    constructor(errorCode: ErrorCode, customMsg: String? = null) : this(
        msg = customMsg ?: errorCode.msg,
        status = errorCode.status,
        code = errorCode.code
    )

    class Csereal400(msg: String) : CserealException(msg, HttpStatus.BAD_REQUEST)
    class Csereal404(msg: String) : CserealException(msg, HttpStatus.NOT_FOUND)
    class Csereal401(msg: String) : CserealException(msg, HttpStatus.UNAUTHORIZED)
    class Csereal409(msg: String) : CserealException(msg, HttpStatus.CONFLICT)
    class Csereal403(msg: String) : CserealException(msg, HttpStatus.FORBIDDEN)
}

enum class ErrorCode(val status: HttpStatus, val code: String, val msg: String) {
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVE-01", "Room Not Found"),
    ONLY_SEMINAR_RESERVABLE(HttpStatus.FORBIDDEN, "RESERVE-02", "일반 예약 권한으로 세미나실만 예약 가능"),
    PROFESSOR_ROOM_DENIED(HttpStatus.FORBIDDEN, "RESERVE-03", "교수회의실은 스태프 또는 교수만 예약 가능"),
    LABMASTER_ONLY(HttpStatus.FORBIDDEN, "RESERVE-04", "정기예약 기간에는 랩대표만 예약을 가능"),
    INVALID_RESERVATION_PERIOD(HttpStatus.BAD_REQUEST, "RESERVE-05", "정기예약은 지정된 학기 내에서만 가능"),
    RESERVATION_TIME_EXCEEDED(HttpStatus.BAD_REQUEST, "RESERVE-06", "정기예약 기간에 3시간을 초과한 예약 불가"),
    TERM_NOT_REGISTERED(HttpStatus.FORBIDDEN, "RESERVE-07", "아직 등록되지 않은 기간은 예약 불가"),
    TERM_NOT_OPENED(HttpStatus.FORBIDDEN, "RESERVE-08", "겹치는 정기예약 기간이 끝난 이후에 예약 가능"),
    RESERVATION_OCCUPIED(HttpStatus.CONFLICT, "RESERVE-09", "해당 시간에 이미 예약이 있습니다"),

    // research/lab (v3 — 도메인별 안정 코드 시범. 기존 code:null Csereal404를 코드 보유로 전환)
    LAB_NOT_FOUND(HttpStatus.NOT_FOUND, "RESEARCH-01", "해당 연구실을 찾을 수 없습니다."),
    LAB_PROFESSOR_OCCUPIED(HttpStatus.BAD_REQUEST, "RESEARCH-02", "이미 다른 연구실에 속한 교수님이 존재합니다."),
    RESEARCH_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "RESEARCH-03", "해당 연구그룹을 찾을 수 없습니다."),
    PROFESSORS_NOT_FOUND(HttpStatus.NOT_FOUND, "RESEARCH-04", "해당 교수님들을 찾을 수 없습니다."),

    // member (v3 — 단일-id 확장)
    PROFESSOR_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-01", "해당 교수님을 찾을 수 없습니다."),
    STAFF_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-02", "해당 직원을 찾을 수 없습니다.")
}

// 도메인 무관 횡단 에러. 모든 에러 경로(검증·인가·인증·미처리)가 동일한 envelope로 나가도록
// 코드를 부여한다. (이전엔 @Valid·401·403·500이 envelope 밖 맨 문자열/Spring 기본이었음)
enum class SystemErrorCode(val status: HttpStatus, val code: String, val msg: String) {
    DATA_DUPLICATION(HttpStatus.CONFLICT, "SYS-01", "중복된 값이 있습니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "SYS-02", "요청 값이 올바르지 않습니다."),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "SYS-03", "로그인이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "SYS-04", "접근 권한이 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS-05", "서버 오류가 발생했습니다."),
    EXTERNAL_AUTH_ERROR(HttpStatus.BAD_GATEWAY, "SYS-06", "인증 서버 오류가 발생했습니다.")
}
