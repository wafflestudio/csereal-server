package com.wafflestudio.csereal.common.aop

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.user.database.Role
import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.core.user.database.UserRepository
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

@Aspect
@Component
class SecurityAspect(private val userRepository: UserRepository) {

    @Before("@annotation(AuthenticatedStaff)")
    fun checkStaffAuthentication() {
        val user = getLoginUser()

        if (user.role != Role.ROLE_STAFF) {
            throw CserealException.Csereal401("권한이 없습니다.")
        }
    }

    @Before("@annotation(AuthenticatedForReservation)")
    fun checkReservationAuthentication() {
        val user = getLoginUser()

        if (user.role == null) {
            throw CserealException.Csereal401("권한이 없습니다.")
        }
    }

    private fun getLoginUser(): UserEntity {
        val authentication = SecurityContextHolder.getContext().authentication
        val principal = authentication.principal

        if (principal !is OidcUser) {
            throw CserealException.Csereal401("로그인이 필요합니다.")
        }

        val username = principal.idToken.getClaim<String>("username")
        val user = userRepository.findByUsername(username) ?: throw CserealException.Csereal404("재로그인이 필요합니다.")

        RequestContextHolder.getRequestAttributes()?.setAttribute("loggedInUser", user, RequestAttributes.SCOPE_REQUEST)

        return user
    }
}
