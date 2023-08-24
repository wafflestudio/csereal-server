package com.wafflestudio.csereal.core.reservation.api

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.reservation.dto.ReservationDto
import com.wafflestudio.csereal.core.reservation.dto.ReserveRequest
import com.wafflestudio.csereal.core.reservation.service.ReservationService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.UUID

@RequestMapping("/reservation")
@RestController
class ReservationController(
    private val reservationService: ReservationService
) {

    @PostMapping
    fun reserveRoom(
        @AuthenticationPrincipal principal: OidcUser?,
        @RequestBody reserveRequest: ReserveRequest
    ): List<ReservationDto> {
        if (principal == null) {
            throw CserealException.Csereal401("로그인이 필요합니다.")
        }
        val username = principal.idToken.getClaim<String>("username")
        return reservationService.reserveRoom(username, reserveRequest)
    }

    @DeleteMapping("/{reservationId}")
    fun cancelSpecific(@AuthenticationPrincipal principal: OidcUser?, @PathVariable reservationId: Long) {
        if (principal == null) {
            throw CserealException.Csereal401("로그인이 필요합니다.")
        }
        reservationService.cancelSpecific(reservationId)
    }

    @DeleteMapping("/recurring/{recurrenceId}")
    fun cancelRecurring(@AuthenticationPrincipal principal: OidcUser?, @PathVariable recurrenceId: UUID) {
        if (principal == null) {
            throw CserealException.Csereal401("로그인이 필요합니다.")
        }
        reservationService.cancelRecurring(recurrenceId)
    }

}
