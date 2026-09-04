package com.wafflestudio.csereal.common.config

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.dto.ErrorResponse
import com.wafflestudio.csereal.global.authenticateRoles
import com.wafflestudio.csereal.global.clearAuthentication
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.context.request.ServletWebRequest

/**
 * 오류 응답이 어떤 예외에서든 `{code}` 한 모양인지, 상태 코드가 예외 성격에 맞는지.
 * 프레임워크 예외는 스프링이 실제로 부르는 입구(`handleException`)로 통과시켜 매핑까지 함께 본다.
 */
class CserealExceptionHandlerTest : StringSpec({
    val handler = CserealExceptionHandler()
    val request = ServletWebRequest(MockHttpServletRequest())

    fun body(response: org.springframework.http.ResponseEntity<ErrorResponse>): ErrorResponse = response.body!!

    afterTest { clearAuthentication() }

    "도메인 예외는 코드만 낸다 — 진단값은 로그 메시지에만" {
        val e = CserealException(ErrorCode.NOTICE_NOT_FOUND, mapOf("noticeId" to 7))
        val r = handler.handle(e)
        r.statusCode shouldBe HttpStatus.NOT_FOUND
        body(r).code shouldBe ErrorCode.NOTICE_NOT_FOUND
        e.message shouldBe "NOTICE-01 공지 없음 {noticeId=7}"
    }

    "익명의 접근 거부는 401(로그인 필요)" {
        clearAuthentication()
        val r = handler.handle(AccessDeniedException("denied"))
        r.statusCode shouldBe HttpStatus.UNAUTHORIZED
        body(r).code shouldBe ErrorCode.UNAUTHENTICATED
    }

    "로그인했지만 권한이 없으면 403" {
        authenticateRoles("ROLE_RESERVE")
        val r = handler.handle(AccessDeniedException("denied"))
        r.statusCode shouldBe HttpStatus.FORBIDDEN
        body(r).code shouldBe ErrorCode.FORBIDDEN
    }

    "모르는 예외는 500이고 내부 정보를 싣지 않는다" {
        val r = handler.handleUnknown(IllegalStateException("SELECT * FROM secret"))
        r.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
        body(r).code shouldBe ErrorCode.INTERNAL
    }

    // 손으로 예외를 나열하던 시절 이게 500 + ERROR 로그(Slack)로 나갔다. 부모 상속으로 잡히는지 고정한다.
    "허용하지 않는 메서드는 405" {
        val r = handler.handleException(HttpRequestMethodNotSupportedException("PUT"), request)
        r?.statusCode shouldBe HttpStatus.METHOD_NOT_ALLOWED
        (r?.body as ErrorResponse).code shouldBe ErrorCode.METHOD_NOT_ALLOWED
    }

    "본문 검증 실패는 400 VALIDATION_FAILED" {
        val binding = BeanPropertyBindingResult(Any(), "request").apply {
            addError(FieldError("request", "title", null, false, arrayOf("NotBlank"), null, "제목은 필수입니다."))
        }
        val param = MethodParameter(String::class.java.getMethod("length"), -1)
        val r = handler.handleException(MethodArgumentNotValidException(param, binding), request)
        r?.statusCode shouldBe HttpStatus.BAD_REQUEST
        (r?.body as ErrorResponse).code shouldBe ErrorCode.VALIDATION_FAILED
    }
})
