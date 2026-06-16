package com.wafflestudio.csereal.common.dto

import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.SystemErrorCode

/**
 * 모든 에러 경로가 공유하는 단일 envelope.
 *
 * 이전엔 경로마다 모양이 달랐다(@Valid→맨 문자열, 401/403→Spring 기본, CserealException→{code,message}).
 * 프론트가 `code`로 분기하고 `message`를 표시할 수 있도록 전 경로를 이 형태로 통일한다.
 * `code`는 안정 머신코드(없으면 null), `message`는 표시용.
 */
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
