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
    TERM_NOT_OPENED(HttpStatus.FORBIDDEN, "RESERVE-08", "정기예약 신청 기간이 아직 시작되지 않음"),
    RESERVATION_OCCUPIED(HttpStatus.CONFLICT, "RESERVE-09", "해당 시간에 이미 예약이 있습니다"),
    INVALID_RECURRING_WEEKS(HttpStatus.BAD_REQUEST, "RESERVE-10", "반복 예약 횟수가 유효하지 않음"),
    ONE_TIME_RECURRING_DENIED(HttpStatus.BAD_REQUEST, "RESERVE-11", "수시 예약은 반복할 수 없음"),
    ONE_TIME_NOT_OPENED(HttpStatus.FORBIDDEN, "RESERVE-12", "수시 예약 가능 기간이 아직 시작되지 않음"),
    PAST_RESERVATION_DENIED(HttpStatus.BAD_REQUEST, "RESERVE-13", "과거 시각은 예약할 수 없음"),
    TERM_APPLICATION_CLOSED(HttpStatus.FORBIDDEN, "RESERVE-14", "정기예약 신청 기간이 종료됨"),
    INVALID_RESERVATION_TIME(HttpStatus.BAD_REQUEST, "RESERVE-15", "예약 종료 시각은 시작 시각 이후여야 함"),
    RESERVATION_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "RESERVE-16", "예약 권한이 없음"),
    UNSUPPORTED_RESERVATION_DATE(HttpStatus.BAD_REQUEST, "RESERVE-17", "지원하지 않는 예약 날짜 범위")
}

enum class SystemErrorCode(val status: HttpStatus, val code: String, val msg: String) {
    DATA_DUPLICATION(HttpStatus.CONFLICT, "SYS-01", "중복된 값이 있습니다.")
}
