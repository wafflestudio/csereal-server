package com.wafflestudio.csereal.common.config

import com.wafflestudio.csereal.common.CserealException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.RestClientException
import java.sql.SQLIntegrityConstraintViolationException

@RestControllerAdvice
class CserealExceptionHandler {
    private val log = LoggerFactory.getLogger(this::class.java)

    // @Valid로 인해 오류 떴을 때 메시지 전송
    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    fun handle(e: MethodArgumentNotValidException): ResponseEntity<Any> {
        val bindingResult: BindingResult = e.bindingResult
        return ResponseEntity(bindingResult.fieldError?.defaultMessage, HttpStatus.BAD_REQUEST)
    }

    // csereal 내부 규정 오류
    @ExceptionHandler(value = [CserealException::class])
    fun handle(e: CserealException): ResponseEntity<Any> {
        return ResponseEntity(e.message, e.status)
    }

    // db에서 중복된 값 있을 때
    @ExceptionHandler(value = [SQLIntegrityConstraintViolationException::class])
    fun handle(e: SQLIntegrityConstraintViolationException): ResponseEntity<Any> {
        log.error(e.stackTraceToString())
        return ResponseEntity("중복된 값이 있습니다.", HttpStatus.CONFLICT)
    }

    // oidc provider 서버에 문제가 있을때
    @ExceptionHandler(value = [RestClientException::class])
    fun handle(e: RestClientException): ResponseEntity<Any> {
        log.error(e.stackTraceToString())
        return ResponseEntity("idsnucse error: ${e.message}", HttpStatus.BAD_GATEWAY)
    }
}
