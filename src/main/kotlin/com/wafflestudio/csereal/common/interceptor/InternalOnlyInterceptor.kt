package com.wafflestudio.csereal.common.interceptor

import com.wafflestudio.csereal.common.CserealException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.net.InetAddress

@Configuration
class InternalOnlyInterceptor : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (handler !is HandlerMethod || !handler.hasMethodAnnotation(InternalOnly::class.java)) {
            return true
        }
        if (!InetAddress.getByName(request.remoteAddr).isLoopbackAddress) {
            throw CserealException.Csereal403("접근 권한이 없습니다.")
        }
        return true
    }
}
