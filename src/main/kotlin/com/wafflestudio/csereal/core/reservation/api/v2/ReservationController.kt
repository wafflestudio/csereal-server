package com.wafflestudio.csereal.core.reservation.api.v2

import com.wafflestudio.csereal.core.reservation.dto.ReservationDto
import com.wafflestudio.csereal.core.reservation.dto.ReserveRequest
import com.wafflestudio.csereal.core.reservation.dto.SimpleReservationDto
import com.wafflestudio.csereal.core.reservation.service.ReservationService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.UUID

@RequestMapping("/api/v2/reservation")
@RestController
class ReservationController(
    private val reservationService: ReservationService
) {

    @GetMapping("/month")
    fun getMonthlyReservations(
        @RequestParam roomId: Long,
        @RequestParam year: Int,
        @RequestParam month: Int
    ): ResponseEntity<List<SimpleReservationDto>> {
        val start = LocalDateTime.of(year, month, 1, 0, 0)
        val end = start.plusMonths(1)
        return ResponseEntity.ok(reservationService.getRoomReservationsBetween(roomId, start, end))
    }

    @GetMapping("/week")
    fun getWeeklyReservations(
        @RequestParam roomId: Long,
        @RequestParam year: Int,
        @RequestParam month: Int,
        @RequestParam day: Int
    ): ResponseEntity<List<SimpleReservationDto>> {
        val start = LocalDateTime.of(year, month, day, 0, 0).minusHours(9)
        val end = start.plusDays(7)
        return ResponseEntity.ok(reservationService.getRoomReservationsBetween(roomId, start, end))
    }

    @GetMapping("/{reservationId}")
    fun getReservation(
        @PathVariable reservationId: Long
    ): ResponseEntity<ReservationDto> {
        return ResponseEntity.ok(reservationService.getReservation(reservationId))
    }

    @PreAuthorize("hasAnyRole('STAFF','RESERVATION')")
    @PostMapping
    fun reserveRoom(
        @RequestBody reserveRequest: ReserveRequest
    ): ResponseEntity<List<ReservationDto>> {
        return ResponseEntity.ok(reservationService.reserveRoom(reserveRequest))
    }

    @PreAuthorize("hasAnyRole('STAFF','RESERVATION')")
    @DeleteMapping("/{reservationId}")
    fun cancelSpecific(@PathVariable reservationId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(reservationService.cancelSpecific(reservationId))
    }

    @PreAuthorize("hasAnyRole('STAFF','RESERVATION')")
    @DeleteMapping("/recurring/{recurrenceId}")
    fun cancelRecurring(@PathVariable recurrenceId: UUID): ResponseEntity<Any> {
        return ResponseEntity.ok(reservationService.cancelRecurring(recurrenceId))
    }
}
