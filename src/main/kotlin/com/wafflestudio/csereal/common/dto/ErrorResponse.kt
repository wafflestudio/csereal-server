package com.wafflestudio.csereal.common.dto

import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.SystemErrorCode

// 모든 에러 경로가 공유하는 단일 envelope. code로 분기, message는 표시용(code는 없으면 null).
data class ErrorResponse(
    val code: String?,
    val message: String?
) {
    companion object {
        fun of(errorCode: ErrorCode) = ErrorResponse(errorCode.code, errorCode.msg)
        fun of(errorCode: SystemErrorCode, message: String? = null) =
            ErrorResponse(errorCode.code, message ?: errorCode.msg)
    }
}
