package com.wafflestudio.csereal.common.dto

import com.wafflestudio.csereal.common.ErrorCode
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 모든 오류 응답의 모양. 코드 하나뿐이다 — 문구는 프론트가 코드로 조립하고, 진단값(어느 id·어느 필드)은 서버 로그에만 남긴다.
 * 사용자가 실제로 고칠 수 있는 정보가 코드만으로 부족해지면 그때 그 오류 전용의 타입 있는 필드를 더한다.
 * OpenApiConfig 가 이 클래스를 직접 스키마로 풀어서 Kotlin non-null 이 required 로 번역되지 않으므로 명시한다.
 */
data class ErrorResponse(
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    val code: ErrorCode
)
