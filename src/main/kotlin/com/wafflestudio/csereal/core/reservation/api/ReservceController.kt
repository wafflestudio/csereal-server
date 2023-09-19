package com.wafflestudio.csereal.core.reservation.api

import com.wafflestudio.csereal.common.aop.AuthenticatedForReservation
import com.wafflestudio.csereal.core.reservation.dto.ReservationDto
import com.wafflestudio.csereal.core.reservation.dto.ReserveRequest
import com.wafflestudio.csereal.core.reservation.dto.SimpleReservationDto
import com.wafflestudio.csereal.core.reservation.service.ReservationService
import org.springframework.http.ResponseEntity
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

@RequestMapping("/api/v1/reservation")
@RestController
class ReservationController(
    private val reservationService: ReservationService
) {

    @GetMapping("/month")
//    @AuthenticatedForReservation  TODO: CBT 끝나면 주석 제거
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
//    @AuthenticatedForReservation
    fun getWeeklyReservations(
        @RequestParam roomId: Long,
        @RequestParam year: Int,
        @RequestParam month: Int,
        @RequestParam day: Int,
    ): ResponseEntity<List<SimpleReservationDto>> {
        val start = LocalDateTime.of(year, month, day, 0, 0)
        val end = start.plusDays(7)
        return ResponseEntity.ok(reservationService.getRoomReservationsBetween(roomId, start, end))
    }

    @GetMapping("/{reservationId}")
//    @AuthenticatedForReservation
    fun getReservation(@PathVariable reservationId: Long): ResponseEntity<ReservationDto> {
        return ResponseEntity.ok(reservationService.getReservation(reservationId))
    }

    @PostMapping
    @AuthenticatedForReservation
    fun reserveRoom(
        @RequestBody reserveRequest: ReserveRequest
    ): ResponseEntity<List<ReservationDto>> {
        return ResponseEntity.ok(reservationService.reserveRoom(reserveRequest))
    }

    @DeleteMapping("/{reservationId}")
    @AuthenticatedForReservation
    fun cancelSpecific(@PathVariable reservationId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(reservationService.cancelSpecific(reservationId))
    }

    @DeleteMapping("/recurring/{recurrenceId}")
    @AuthenticatedForReservation
    fun cancelRecurring(@PathVariable recurrenceId: UUID): ResponseEntity<Any> {
        return ResponseEntity.ok(reservationService.cancelRecurring(recurrenceId))
    }

}
