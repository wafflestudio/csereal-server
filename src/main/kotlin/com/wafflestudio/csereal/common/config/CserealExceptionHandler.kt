package com.wafflestudio.csereal.common.config

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.SystemErrorCode
import com.wafflestudio.csereal.common.dto.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.RestClientException
import java.sql.SQLIntegrityConstraintViolationException

/**
 * 모든 에러를 단일 envelope(ErrorResponse{code, message})로 내보낸다.
 * 401/403은 Spring Security 필터에서 발생하므로 SecurityConfig의 entryPoint/accessDeniedHandler가
 * 같은 envelope로 처리한다(여기선 못 잡음).
 */
@RestControllerAdvice
class CserealExceptionHandler {
    private val log = LoggerFactory.getLogger(this::class.java)

    // csereal 내부 규정 오류
    @ExceptionHandler(value = [CserealException::class])
    fun handle(e: CserealException): ResponseEntity<ErrorResponse> =
        ResponseEntity(ErrorResponse(e.code, e.message), e.status)

    // @Valid 검증 실패 — 이전엔 맨 문자열을 반환해 envelope 밖이었다. 이제 동일 envelope로.
    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    fun handle(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val fieldError = e.bindingResult.fieldError
        val message = fieldError?.let { "${it.field}: ${it.defaultMessage}" }
            ?: SystemErrorCode.VALIDATION_FAILED.msg
        return ResponseEntity(
            ErrorResponse.of(SystemErrorCode.VALIDATION_FAILED, message),
            HttpStatus.BAD_REQUEST
        )
    }

    // db에서 중복된 값 있을 때
    @ExceptionHandler(value = [SQLIntegrityConstraintViolationException::class])
    fun handle(e: SQLIntegrityConstraintViolationException): ResponseEntity<ErrorResponse> {
        log.error(e.stackTraceToString())
        return ResponseEntity(
            ErrorResponse.of(SystemErrorCode.DATA_DUPLICATION),
            SystemErrorCode.DATA_DUPLICATION.status
        )
    }

    // oidc provider 서버에 문제가 있을때
    @ExceptionHandler(value = [RestClientException::class])
    fun handle(e: RestClientException): ResponseEntity<ErrorResponse> {
        log.error(e.stackTraceToString())
        return ResponseEntity(
            ErrorResponse.of(SystemErrorCode.EXTERNAL_AUTH_ERROR, "idsnucse error: ${e.message}"),
            SystemErrorCode.EXTERNAL_AUTH_ERROR.status
        )
    }

    // @PreAuthorize 메서드 인가 거부는 컨트롤러 호출 중 발생해 advice가 잡는다(필터 핸들러 아님).
    // AuthorizationDeniedException도 AccessDeniedException 하위라 함께 처리됨.
    // 명시적으로 잡지 않으면 아래 generic Exception 핸들러가 500으로 삼켜버린다.
    @ExceptionHandler(value = [AccessDeniedException::class])
    fun handle(e: AccessDeniedException): ResponseEntity<ErrorResponse> =
        ResponseEntity(
            ErrorResponse.of(SystemErrorCode.ACCESS_DENIED),
            SystemErrorCode.ACCESS_DENIED.status
        )

    // 미처리 예외 — 이전엔 Spring 기본(envelope 밖 + 스택 노출 위험). 이제 동일 envelope로.
    @ExceptionHandler(value = [Exception::class])
    fun handle(e: Exception): ResponseEntity<ErrorResponse> {
        log.error(e.stackTraceToString())
        return ResponseEntity(
            ErrorResponse.of(SystemErrorCode.INTERNAL_ERROR),
            SystemErrorCode.INTERNAL_ERROR.status
        )
    }
}
