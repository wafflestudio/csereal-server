package com.wafflestudio.csereal.common.config

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.dto.ErrorResponse
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.RestClientException
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MultipartException
import org.springframework.web.multipart.support.MissingServletRequestPartException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.sql.SQLIntegrityConstraintViolationException

/**
 * 모든 오류 응답을 [ErrorResponse] `{ code }` 한 모양으로 내보낸다. 문구는 없다 — 프론트가 코드로 조립한다.
 *
 * 프레임워크 예외(405·415·400 계열 등 20여 종)는 [ResponseEntityExceptionHandler] 가 이미 상태 코드까지 정해 두었으므로
 * 목록을 손으로 나열하지 않고 상속해서 [handleExceptionInternal] 로 **본문만** 갈아끼운다.
 * 손으로 나열하던 시절엔 빠뜨린 예외가 마지막 그물로 떨어져 405 가 500 + ERROR 로그(Slack 알림)로 나갔다.
 */
@RestControllerAdvice
class CserealExceptionHandler : ResponseEntityExceptionHandler() {

    private fun errorResponse(code: ErrorCode): ResponseEntity<ErrorResponse> =
        ResponseEntity(ErrorResponse(code), code.status)

    // ── 우리가 던지는 것 ────────────────────────────────────────────

    /** 도메인 규칙 위반 — 서비스가 고른 코드 그대로. */
    @ExceptionHandler(CserealException::class)
    fun handle(e: CserealException): ResponseEntity<ErrorResponse> = errorResponse(e.errorCode)

    // ── 부모가 다루지 않는 것 ───────────────────────────────────────

    /** 서비스 계층의 jakarta 검증(@Validated). 부모는 웹 계층 검증만 안다. */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handle(e: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        logClientError(
            ErrorCode.VALIDATION_FAILED,
            e.constraintViolations.joinToString { "${it.propertyPath}: ${it.message}" }
        )
        return errorResponse(ErrorCode.VALIDATION_FAILED)
    }

    /** multipart 파싱 자체가 실패(경계 문자열이 깨진 요청 등). 부모는 용량 초과만 다룬다. */
    @ExceptionHandler(MultipartException::class)
    fun handle(e: MultipartException): ResponseEntity<ErrorResponse> {
        logClientError(ErrorCode.MALFORMED_REQUEST, e.message)
        return errorResponse(ErrorCode.MALFORMED_REQUEST)
    }

    /**
     * **메서드 단계**(`@PreAuthorize`) 인가 거부. 익명이면 "로그인 필요"(401), 로그인했는데 권한이 없으면 403.
     *
     * URL 규칙(`SecurityConfig.authorizeHttpRequests`)에서 막히는 건 필터 체인이 처리하므로 여기까지 오지 않는다
     * — 그쪽은 `SecurityConfig` 의 entryPoint·accessDeniedHandler 담당이다(실측으로 확인).
     * 그러니 이 핸들러와 그쪽 설정은 중복이 아니라 **다른 경로**를 각각 막는다.
     *
     * 그리고 이 핸들러를 지우면 안 된다. 마지막 그물 [handleUnknown] 이 `Exception` 을 선언하고 있어서
     * `AccessDeniedException` 이 거기로 떨어져 500 + ERROR 로그(Slack)가 된다.
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handle(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
        val auth = SecurityContextHolder.getContext().authentication
        val anonymous = auth == null || auth is AnonymousAuthenticationToken
        return errorResponse(if (anonymous) ErrorCode.UNAUTHENTICATED else ErrorCode.FORBIDDEN)
    }

    /** DB 유니크 제약 위반. */
    @ExceptionHandler(SQLIntegrityConstraintViolationException::class)
    fun handle(e: SQLIntegrityConstraintViolationException): ResponseEntity<ErrorResponse> {
        logger.error("data duplication", e)
        return errorResponse(ErrorCode.DATA_DUPLICATION)
    }

    /** OIDC 공급자(id.snucse.org) 호출 실패. */
    @ExceptionHandler(RestClientException::class)
    fun handle(e: RestClientException): ResponseEntity<ErrorResponse> {
        logger.error("oidc provider call failed", e)
        return errorResponse(ErrorCode.OIDC_UNAVAILABLE)
    }

    /** 마지막 그물. 내부 정보(예외 메시지·스택)는 응답에 싣지 않고 로그로만 남긴다. */
    @ExceptionHandler(Exception::class)
    fun handleUnknown(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("unhandled exception", e)
        return errorResponse(ErrorCode.INTERNAL)
    }

    // ── 프레임워크 예외: 상태는 부모 판단을 따르고 본문만 우리 모양으로 ──

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val code = frameworkErrorCode(ex, statusCode)
        if (code.status.is5xxServerError) {
            logger.error("framework exception", ex)
        } else {
            logClientError(code, describe(ex))
        }
        // headers 를 그대로 넘긴다 — 405 의 Allow 헤더처럼 부모가 채운 것이 있다.
        return super.handleExceptionInternal(ex, ErrorResponse(code), headers, statusCode, request)
    }

    private fun frameworkErrorCode(ex: Exception, status: HttpStatusCode): ErrorCode = when (ex) {
        is MethodArgumentNotValidException, is HandlerMethodValidationException, is BindException ->
            ErrorCode.VALIDATION_FAILED
        else -> when (status.value()) {
            400 -> ErrorCode.MALFORMED_REQUEST
            404 -> ErrorCode.NOT_FOUND
            405 -> ErrorCode.METHOD_NOT_ALLOWED
            406 -> ErrorCode.NOT_ACCEPTABLE
            413 -> ErrorCode.PAYLOAD_TOO_LARGE
            415 -> ErrorCode.UNSUPPORTED_MEDIA_TYPE
            503 -> ErrorCode.SERVICE_UNAVAILABLE
            else -> ErrorCode.INTERNAL
        }
    }

    /** 로그에 남길 한 줄. 어느 필드·파라미터가 문제였는지가 여기밖엔 안 남는다. */
    private fun describe(ex: Exception): String? = when (ex) {
        is MethodArgumentNotValidException ->
            ex.bindingResult.fieldErrors.joinToString { "${it.field}: ${it.code}" }
        is HandlerMethodValidationException ->
            ex.parameterValidationResults.flatMap { result ->
                // codes 는 "Positive.컨트롤러#메서드.파라미터" 꼴이라 제약 이름만 잘라 쓴다
                result.resolvableErrors.map {
                    "${result.methodParameter.parameterName}: ${it.codes?.firstOrNull()?.substringBefore('.')}"
                }
            }.joinToString()
        is MissingServletRequestParameterException -> "missing param ${ex.parameterName}"
        is MissingServletRequestPartException -> "missing part ${ex.requestPartName}"
        is MethodArgumentTypeMismatchException -> "type mismatch ${ex.name}"
        else -> ex.message
    }

    /**
     * 4xx 는 사용자가 고칠 수 있는 실패가 아니라 대개 프론트 버그·URL 조작이라 응답엔 코드만 싣고 상세는 로그로.
     * WARN 이라 Slack 알림(ERROR 기준)은 울리지 않는다.
     */
    private fun logClientError(code: ErrorCode, detail: String?) {
        if (!detail.isNullOrBlank()) logger.warn("${code.code} — $detail")
    }
}
