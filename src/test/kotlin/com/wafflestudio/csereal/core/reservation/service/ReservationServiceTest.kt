package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.mockauth.CustomOidcUser
import com.wafflestudio.csereal.core.reservation.config.ReservationProperties
import com.wafflestudio.csereal.core.reservation.database.ReservationEntity
import com.wafflestudio.csereal.core.reservation.database.ReservationRepository
import com.wafflestudio.csereal.core.reservation.database.ReservationType
import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermRepository
import com.wafflestudio.csereal.core.reservation.database.ReserveTermType
import com.wafflestudio.csereal.core.reservation.database.RoomEntity
import com.wafflestudio.csereal.core.reservation.database.RoomRepository
import com.wafflestudio.csereal.core.reservation.database.RoomType
import com.wafflestudio.csereal.core.reservation.dto.ReserveRequest
import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.core.user.database.UserRepository
import com.wafflestudio.csereal.core.user.service.UserService
import com.wafflestudio.csereal.global.config.MySQLTestContainerConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@Import(MySQLTestContainerConfig::class)
class ReservationServiceTest(
    private val roomRepository: RoomRepository,
    private val reservationRepository: ReservationRepository,
    private val reservationService: ReservationService,
    private val reserveTermPolicy: ReserveTermPolicy,
    private val reserveTermRepository: ReserveTermRepository,
    private val userRepository: UserRepository
) : StringSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))
    lateinit var lab: RoomEntity
    lateinit var seminar: RoomEntity

    beforeSpec {
        lab = roomRepository.save(RoomEntity("staff lab", "302", 20, RoomType.LAB))
        seminar = roomRepository.save(RoomEntity("cancellation room", "304", 20, RoomType.SEMINAR))
    }
    afterTest { SecurityContextHolder.clearContext() }

    "creation fails closed without authentication" {
        SecurityContextHolder.clearContext()
        val start = reserveTermPolicy.now().plusDays(1)
        shouldThrow<CserealException.Csereal401> { reservationService.reserveRoom(request(lab.id, start, 1)) }
    }

    "staff can reserve every room as UNRESTRICTED with the configured maximum" {
        authenticate(userRepository, "staff-policy", "ROLE_STAFF")
        val start = reserveTermPolicy.now().plusDays(1)
        val result = reservationService.reserveRoom(request(lab.id, start, 20))
        result.size shouldBe 20
        result.all { it.reservationType == ReservationType.UNRESTRICTED } shouldBe true
    }

    "staff recurrence above the configured maximum is rejected before generation" {
        authenticate(userRepository, "staff-policy", "ROLE_STAFF")
        val start = reserveTermPolicy.now().plusDays(1)
        shouldThrow<CserealException> {
            reservationService.reserveRoom(request(lab.id, start, 21))
        } shouldBe CserealException(ErrorCode.INVALID_RECURRING_WEEKS)
    }

    "a request at the fixed UTC now is rejected as past" {
        val nowUtc = LocalDateTime.of(2027, 3, 10, 10, 0)
        val fixture = harness(nowUtc)
        unitAuthenticate("ROLE_STAFF")

        shouldThrow<CserealException> {
            fixture.service.reserveRoom(request(fixture.room.id, nowUtc, 1))
        } shouldBe CserealException(ErrorCode.PAST_RESERVATION_DENIED)
    }

    "fixed clocks enforce UTC applyStart just-before and exact boundaries" {
        val descriptor = defaultAt(LocalDateTime.of(2027, 2, 1, 0, 0)).descriptor(
            2027,
            ReserveTermType.FIRST_SEMESTER
        )
        val target = descriptor.termStartTime.plusDays(10).plusHours(10)

        listOf(
            listOf("ROLE_LABMASTER") to ErrorCode.TERM_NOT_OPENED,
            listOf("ROLE_RESERVATION") to ErrorCode.LABMASTER_ONLY,
            listOf("ROLE_LABMASTER", "ROLE_RESERVATION") to ErrorCode.TERM_NOT_OPENED
        ).forEach { (roles, expectedError) ->
            val before = harness(descriptor.applyStartTime.minusSeconds(1))
            before.persistCanonical(descriptor)
            unitAuthenticate(*roles.toTypedArray())

            shouldThrow<CserealException> {
                before.service.reserveRoom(request(before.room.id, target, 1))
            } shouldBe CserealException(expectedError)
            verify(exactly = 0) { before.reservationRepository.saveAll(any<List<ReservationEntity>>()) }
            before.verifySingleTermLookup()
        }

        val exact = harness(descriptor.applyStartTime)
        exact.persistCanonical(descriptor)
        unitAuthenticate("ROLE_LABMASTER")
        exact.service.reserveRoom(request(exact.room.id, target, 1))
            .single().reservationType shouldBe ReservationType.REGULAR
    }

    "fixed clocks enforce UTC termStart just-before and exact boundaries" {
        val descriptor = defaultAt(LocalDateTime.of(2027, 2, 28, 15, 0)).descriptor(
            2027,
            ReserveTermType.FIRST_SEMESTER
        )
        val target = descriptor.termStartTime.plusDays(10).plusHours(10)

        val before = harness(descriptor.termStartTime.minusSeconds(1))
        before.persistCanonical(descriptor)
        unitAuthenticate("ROLE_LABMASTER")
        before.service.reserveRoom(request(before.room.id, target, 1))
            .single().reservationType shouldBe ReservationType.REGULAR

        val exact = harness(descriptor.termStartTime)
        unitAuthenticate("ROLE_LABMASTER")
        exact.service.reserveRoom(request(exact.room.id, target, 1))
            .single().reservationType shouldBe ReservationType.ONE_TIME
        exact.verifySingleTermLookup()
    }

    "persisted UTC custom phases include an exact GAP and active-term end bound" {
        val term = ReserveTermEntity(
            LocalDateTime.of(2027, 2, 3, 0, 0),
            LocalDateTime.of(2027, 2, 20, 9, 0),
            LocalDateTime.of(2027, 2, 28, 15, 0),
            LocalDateTime.of(2027, 6, 25, 3, 0)
        )
        val gap = harness(term.applyEndTime)
        every { gap.termRepository.findContainingRequestStart(any()) } returns listOf(term)
        unitAuthenticate("ROLE_LABMASTER")
        shouldThrow<CserealException> {
            gap.service.reserveRoom(request(gap.room.id, term.termStartTime.plusDays(1), 1))
        } shouldBe CserealException(ErrorCode.TERM_APPLICATION_CLOSED)

        val active = harness(term.termEndTime.minusDays(1))
        val requestStartUtc = term.termEndTime.minusMinutes(30)
        every { active.termRepository.findContainingRequestStart(any()) } returns listOf(term)
        unitAuthenticate("ROLE_RESERVATION")
        shouldThrow<CserealException> {
            active.service.reserveRoom(request(active.room.id, requestStartUtc, 1))
        } shouldBe CserealException(ErrorCode.INVALID_RESERVATION_PERIOD)
    }

    "modified persisted times drive REGULAR without canonical matching" {
        val term = ReserveTermEntity(
            LocalDateTime.of(2027, 2, 3, 0, 0),
            LocalDateTime.of(2027, 2, 20, 9, 0),
            LocalDateTime.of(2027, 3, 4, 15, 0),
            LocalDateTime.of(2027, 6, 24, 15, 0)
        )
        val fixture = harness(LocalDateTime.of(2027, 2, 10, 3, 0))
        every { fixture.termRepository.findContainingRequestStart(any()) } returns listOf(term)
        unitAuthenticate("ROLE_LABMASTER")

        fixture.service.reserveRoom(request(fixture.room.id, term.termStartTime.plusDays(1), 1))
            .single().reservationType shouldBe ReservationType.REGULAR
    }

    "a UTC ONE_TIME request opens at the weekend-adjusted Monday derived in Seoul" {
        val reservationStartUtc = LocalDateTime.of(2026, 7, 19, 13, 0)
        val openingUtc = LocalDateTime.of(2026, 7, 6, 0, 0)

        val before = harness(openingUtc.minusSeconds(1))
        unitAuthenticate("ROLE_RESERVATION")
        shouldThrow<CserealException> {
            before.service.reserveRoom(request(before.room.id, reservationStartUtc, 1))
        } shouldBe
            CserealException(ErrorCode.ONE_TIME_NOT_OPENED)

        val exact = harness(openingUtc)
        unitAuthenticate("ROLE_RESERVATION")
        exact.service.reserveRoom(request(exact.room.id, reservationStartUtc, 1))
            .single().reservationType shouldBe ReservationType.ONE_TIME
        exact.verifySingleTermLookup()
    }

    "REGULAR one-occurrence and repeated requests persist exact canonical rows" {
        val now = LocalDateTime.of(2027, 2, 1, 0, 0)
        listOf(1, 3).forEach { weeks ->
            val fixture = harness(now)
            val descriptor = fixture.defaultPolicy.descriptor(2027, ReserveTermType.FIRST_SEMESTER)
            val canonical = fixture.persistCanonical(descriptor)
            val start = descriptor.termStartTime.plusDays(10).plusHours(10)
            unitAuthenticate("ROLE_LABMASTER")

            fixture.service.reserveRoom(request(fixture.room.id, start, weeks))
            fixture.saved.size shouldBe weeks
            fixture.saved.forEachIndexed { index, row ->
                row.room shouldBe fixture.room
                row.startTime shouldBe start.plusWeeks(index.toLong())
                row.endTime shouldBe start.plusWeeks(index.toLong()).plusHours(1)
                row.recurringWeeks shouldBe weeks
                row.reservationType shouldBe ReservationType.REGULAR
            }
            canonical.termYear shouldBe descriptor.termYear
            canonical.termType shouldBe descriptor.termType
        }
    }

    "REGULAR recurring reservations apply the three-hour limit per occurrence" {
        val fixture = harness(LocalDateTime.of(2027, 2, 1, 0, 0))
        val descriptor = fixture.defaultPolicy.descriptor(2027, ReserveTermType.FIRST_SEMESTER)
        fixture.persistCanonical(descriptor)
        val start = descriptor.termStartTime.plusDays(10).plusHours(10)
        unitAuthenticate("ROLE_LABMASTER")

        fixture.service.reserveRoom(request(fixture.room.id, start, weeks = 2, hours = 3))

        fixture.saved.size shouldBe 2
        fixture.saved.forEachIndexed { index, reservation ->
            reservation.startTime shouldBe start.plusWeeks(index.toLong())
            reservation.endTime shouldBe start.plusWeeks(index.toLong()).plusHours(3)
            reservation.reservationType shouldBe ReservationType.REGULAR
        }
        fixture.saved.sumOf { Duration.between(it.startTime, it.endTime).toMinutes() } shouldBe 360L
    }

    "only a truly missing target enables fallback while malformed and multiple targets fail closed" {
        val now = LocalDateTime.of(2027, 2, 1, 0, 0)
        val missing = harness(now)
        val descriptor = missing.defaultPolicy.descriptor(2027, ReserveTermType.FIRST_SEMESTER)
        missing.persistMissing()
        unitAuthenticate("ROLE_LABMASTER")
        shouldThrow<CserealException> {
            missing.service.reserveRoom(
                request(missing.room.id, descriptor.termStartTime.plusDays(10).plusHours(10), 1)
            )
        } shouldBe CserealException(ErrorCode.ONE_TIME_NOT_OPENED)

        val malformed = harness(now)
        malformed.persistMismatch(descriptor)
        unitAuthenticate("ROLE_LABMASTER")
        shouldThrow<CserealException> {
            malformed.service.reserveRoom(
                request(malformed.room.id, descriptor.termStartTime.plusDays(10).plusHours(10), 1)
            )
        } shouldBe CserealException(ErrorCode.TERM_NOT_REGISTERED)

        val multiple = harness(now)
        multiple.persistMultiple(descriptor)
        unitAuthenticate("ROLE_LABMASTER")
        shouldThrow<CserealException> {
            multiple.service.reserveRoom(
                request(multiple.room.id, descriptor.termStartTime.plusDays(10).plusHours(10), 1)
            )
        } shouldBe CserealException(ErrorCode.TERM_NOT_REGISTERED)
    }

    "a missing target is resolved once and permits ONE_TIME after opening" {
        val fixture = harness(LocalDateTime.of(2027, 3, 8, 0, 0))
        fixture.persistMissing()
        unitAuthenticate("ROLE_RESERVATION")
        val target = LocalDateTime.of(2027, 3, 20, 10, 0)

        fixture.service.reserveRoom(request(fixture.room.id, target, 1)).single().reservationType shouldBe
            ReservationType.ONE_TIME
        fixture.verifySingleTermLookup()
    }

    "creation-role precedence and room authorization are explicit" {
        val staff = harness(LocalDateTime.of(2027, 2, 1, 0, 0), RoomType.LAB)
        unitAuthenticate("ROLE_STAFF", "ROLE_LABMASTER", "ROLE_RESERVATION")
        staff.service.reserveRoom(request(staff.room.id, LocalDateTime.of(2027, 3, 10, 10, 0), 1))
            .single().reservationType shouldBe ReservationType.UNRESTRICTED

        val labmaster = harness(LocalDateTime.of(2027, 2, 1, 0, 0))
        val descriptor = labmaster.defaultPolicy.descriptor(2027, ReserveTermType.FIRST_SEMESTER)
        labmaster.persistCanonical(descriptor)
        unitAuthenticate("ROLE_LABMASTER", "ROLE_RESERVATION")
        labmaster.service.reserveRoom(
            request(labmaster.room.id, descriptor.termStartTime.plusDays(10).plusHours(10), 1)
        ).single().reservationType shouldBe ReservationType.REGULAR

        val reservationOnly = harness(LocalDateTime.of(2027, 2, 1, 0, 0))
        reservationOnly.persistCanonical(descriptor)
        unitAuthenticate("ROLE_RESERVATION")
        shouldThrow<CserealException> {
            reservationOnly.service.reserveRoom(
                request(reservationOnly.room.id, LocalDateTime.of(2027, 3, 10, 10, 0), 1)
            )
        } shouldBe CserealException(ErrorCode.LABMASTER_ONLY)

        val nonSeminar = harness(LocalDateTime.of(2027, 2, 28, 15, 0), RoomType.LECTURE)
        unitAuthenticate("ROLE_RESERVATION")
        shouldThrow<CserealException> {
            nonSeminar.service.reserveRoom(
                request(nonSeminar.room.id, LocalDateTime.of(2027, 3, 20, 10, 0), 1)
            )
        } shouldBe CserealException(ErrorCode.ONLY_SEMINAR_RESERVABLE)
    }

    "room 8 enforces the complete role matrix" {
        val target = LocalDateTime.of(2027, 3, 20, 10, 0)
        val afterOpening = LocalDateTime.of(2027, 3, 8, 0, 0)

        val staff = harness(afterOpening, roomId = 8)
        unitAuthenticate("ROLE_STAFF")
        staff.service.reserveRoom(request(8, target, 1))
            .single().reservationType shouldBe ReservationType.UNRESTRICTED

        val professorLabmaster = harness(afterOpening, roomId = 8)
        unitAuthenticate("ROLE_PROFESSOR", "ROLE_LABMASTER")
        professorLabmaster.service.reserveRoom(request(8, target, 1))
            .single().reservationType shouldBe ReservationType.ONE_TIME
        professorLabmaster.verifySingleTermLookup()

        val professorReservation = harness(afterOpening, roomId = 8)
        unitAuthenticate("ROLE_PROFESSOR", "ROLE_RESERVATION")
        professorReservation.service.reserveRoom(request(8, target, 1))
            .single().reservationType shouldBe ReservationType.ONE_TIME
        professorReservation.verifySingleTermLookup()

        listOf("ROLE_LABMASTER", "ROLE_RESERVATION").forEach { role ->
            val withoutProfessor = harness(afterOpening, roomId = 8)
            unitAuthenticate(role)
            shouldThrow<CserealException> {
                withoutProfessor.service.reserveRoom(request(8, target, 1))
            } shouldBe CserealException(ErrorCode.PROFESSOR_ROOM_DENIED)
        }

        val professorOnly = harness(afterOpening, roomId = 8)
        unitAuthenticate("ROLE_PROFESSOR")
        shouldThrow<CserealException> {
            professorOnly.service.reserveRoom(request(8, target, 1))
        } shouldBe CserealException(ErrorCode.RESERVATION_PERMISSION_DENIED)
    }

    "a UTC midnight boundary within one Seoul date is allowed for non-staff" {
        val fixture = harness(LocalDateTime.of(2027, 3, 8, 0, 0))
        unitAuthenticate("ROLE_RESERVATION")

        fixture.service.reserveRoom(
            request(fixture.room.id, LocalDateTime.of(2027, 3, 20, 23, 30), 1, hours = 1)
        ).single().reservationType shouldBe ReservationType.ONE_TIME
    }

    "a Seoul midnight boundary is rejected for non-staff even within three hours" {
        val fixture = harness(LocalDateTime.of(2027, 3, 8, 0, 0))
        unitAuthenticate("ROLE_RESERVATION")

        shouldThrow<CserealException> {
            fixture.service.reserveRoom(
                request(fixture.room.id, LocalDateTime.of(2027, 3, 20, 14, 30), 1, hours = 1)
            )
        } shouldBe CserealException(ErrorCode.RESERVATION_TIME_EXCEEDED)
    }

    "an over-three-hour occurrence is rejected for non-staff" {
        val fixture = harness(LocalDateTime.of(2027, 3, 8, 0, 0))
        val start = LocalDateTime.of(2027, 3, 20, 1, 0)
        unitAuthenticate("ROLE_RESERVATION")

        shouldThrow<CserealException> {
            fixture.service.reserveRoom(
                request(fixture.room.id, start, 1).copy(endTime = start.plusHours(3).plusMinutes(1))
            )
        } shouldBe CserealException(ErrorCode.RESERVATION_TIME_EXCEEDED)
    }

    "staff are exempt from both Seoul date and three-hour limits" {
        val fixture = harness(LocalDateTime.of(2027, 3, 1, 0, 0))
        unitAuthenticate("ROLE_STAFF")

        fixture.service.reserveRoom(
            request(fixture.room.id, LocalDateTime.of(2027, 3, 20, 13, 0), 1, hours = 4)
        ).single().reservationType shouldBe ReservationType.UNRESTRICTED
    }

    "the pessimistic room lookup occurs before the login-user query" {
        val fixture = harness(LocalDateTime.of(2027, 3, 8, 0, 0))
        unitAuthenticate("ROLE_RESERVATION")
        fixture.service.reserveRoom(
            request(fixture.room.id, LocalDateTime.of(2027, 3, 20, 10, 0), 1)
        )

        verifyOrder {
            fixture.roomRepository.findRoomById(fixture.room.id)
            fixture.userService.getLoginUser()
        }
    }

    "specific cancellation preserves owner, staff, other-user, and missing behavior" {
        val now = reserveTermPolicy.now()
        val start = now.toLocalDate().plusDays(1).atTime(9, 0)
        reserveTermRepository.deleteAll()
        reserveTermRepository.save(
            ReserveTermEntity(now.minusDays(2), now.minusDays(1), now.minusDays(3), now.plusYears(1))
        )
        authenticate(userRepository, "reservation-owner", "ROLE_RESERVATION")
        val owned = reservationService.reserveRoom(request(seminar.id, start, 1)).single()
        reservationService.cancelSpecific(owned.id)
        reservationRepository.findById(owned.id).isPresent shouldBe false

        authenticate(userRepository, "reservation-owner", "ROLE_RESERVATION")
        val cancellable = reservationService.reserveRoom(request(seminar.id, start.plusHours(2), 1)).single()
        authenticate(userRepository, "cancelling-staff", "ROLE_STAFF")
        reservationService.cancelSpecific(cancellable.id)

        authenticate(userRepository, "reservation-owner", "ROLE_RESERVATION")
        val forbidden = reservationService.reserveRoom(request(seminar.id, start.plusHours(4), 1)).single()
        authenticate(userRepository, "other-user", "ROLE_RESERVATION")
        shouldThrow<CserealException.Csereal403> {
            reservationService.cancelSpecific(forbidden.id)
        }.message shouldBe "Cannot cancel other's reservation"
        shouldThrow<CserealException.Csereal404> {
            reservationService.cancelSpecific(Long.MAX_VALUE)
        }.message shouldBe "reservation not found"
    }

    "recurring cancellation deletes the recurrence group" {
        authenticate(userRepository, "recurrence-owner", "ROLE_STAFF")
        val start = reserveTermPolicy.now().plusDays(3)
        val reservations = reservationService.reserveRoom(request(seminar.id, start, 2))
        reservationService.cancelRecurring(reservations.first().recurrenceId)
        reservationRepository.findFirstByRecurrenceId(reservations.first().recurrenceId) shouldBe null
    }
}) {
    private class UnitHarness(
        val service: ReservationServiceImpl,
        val policy: ReserveTermPolicy,
        val defaultPolicy: ReserveTermDefaultPolicy,
        val room: RoomEntity,
        val reservationRepository: ReservationRepository,
        val roomRepository: RoomRepository,
        val termRepository: ReserveTermRepository,
        val userService: UserService,
        val saved: MutableList<ReservationEntity>
    ) {
        fun persistCanonical(descriptor: ReserveTermDescriptor): ReserveTermEntity {
            val entity = canonicalEntity(descriptor)
            every { termRepository.findContainingRequestStart(any()) } returns listOf(entity)
            return entity
        }

        fun persistMissing() {
            every { termRepository.findContainingRequestStart(any()) } returns emptyList()
        }

        fun persistMismatch(descriptor: ReserveTermDescriptor) {
            val entity = ReserveTermEntity(
                descriptor.applyStartTime,
                descriptor.applyStartTime,
                descriptor.termStartTime,
                descriptor.termEndTime,
                descriptor.termYear,
                descriptor.termType
            )
            every { termRepository.findContainingRequestStart(any()) } returns listOf(entity)
        }

        fun persistMultiple(descriptor: ReserveTermDescriptor) {
            every { termRepository.findContainingRequestStart(any()) } returns
                listOf(canonicalEntity(descriptor), canonicalEntity(descriptor))
        }

        fun verifySingleTermLookup() {
            verify(exactly = 1) { termRepository.findContainingRequestStart(any()) }
        }
    }

    companion object {
        private val SEOUL: ZoneId = ZoneId.of("Asia/Seoul")

        private fun request(
            roomId: Long,
            start: LocalDateTime,
            weeks: Int,
            hours: Long = 1
        ) = ReserveRequest(
            roomId,
            "title",
            "a@a.com",
            "010-1234-5678",
            "prof",
            "purpose",
            start,
            start.plusHours(hours),
            true,
            weeks
        )

        private fun policyAt(now: LocalDateTime) = ReserveTermPolicy(
            Clock.fixed(now.toInstant(ZoneOffset.UTC), SEOUL)
        )

        private fun defaultAt(now: LocalDateTime) = ReserveTermDefaultPolicy(
            Clock.fixed(now.toInstant(ZoneOffset.UTC), SEOUL)
        )

        private fun harness(
            now: LocalDateTime,
            roomType: RoomType = RoomType.SEMINAR,
            roomId: Long = 101
        ): UnitHarness {
            val reservationRepository = mockk<ReservationRepository>(relaxed = true)
            val roomRepository = mockk<RoomRepository>()
            val termRepository = mockk<ReserveTermRepository>(relaxed = true)
            val userService = mockk<UserService>()
            val policy = policyAt(now)
            val defaultPolicy = defaultAt(now)
            val room = RoomEntity("unit room", "unit", 20, roomType).also { setId(it, roomId) }
            val user = UserEntity("unit-user", "unit", "unit@example.com", "0000-00000")
            val saved = mutableListOf<ReservationEntity>()

            every { roomRepository.findRoomById(roomId) } returns room
            every { userService.getLoginUser() } returns user
            every { termRepository.findContainingRequestStart(any()) } returns emptyList()
            every { reservationRepository.findByRoomIdAndTimeOverlap(any(), any(), any()) } returns emptyList()
            every { reservationRepository.saveAll(any<List<ReservationEntity>>()) } answers {
                firstArg<List<ReservationEntity>>().also(saved::addAll)
            }

            val validation = ReserveTermValidationService(termRepository, policy)
            val service = ReservationServiceImpl(
                reservationRepository,
                roomRepository,
                policy,
                validation,
                userService,
                ReservationProperties()
            )
            return UnitHarness(
                service,
                policy,
                defaultPolicy,
                room,
                reservationRepository,
                roomRepository,
                termRepository,
                userService,
                saved
            )
        }

        private fun canonicalEntity(
            descriptor: ReserveTermDescriptor,
            applyStartTime: LocalDateTime = descriptor.applyStartTime
        ) = ReserveTermEntity(
            applyStartTime,
            descriptor.applyEndTime,
            descriptor.termStartTime,
            descriptor.termEndTime,
            descriptor.termYear,
            descriptor.termType
        )

        private fun setId(entity: BaseTimeEntity, id: Long) {
            BaseTimeEntity::class.java.getDeclaredField("id").apply {
                isAccessible = true
                setLong(entity, id)
            }
        }

        private fun unitAuthenticate(vararg roles: String) {
            val authorities = roles.map(::SimpleGrantedAuthority)
            SecurityContextHolder.getContext().authentication =
                UsernamePasswordAuthenticationToken("unit-principal", null, authorities)
        }

        private fun authenticate(repository: UserRepository, username: String, vararg roles: String) {
            val user = repository.findByUsername(username) ?: repository.save(
                UserEntity(username, username, "$username@example.com", "0000-00000")
            )
            val authorities = roles.map(::SimpleGrantedAuthority)
            val issuedAt = Instant.now()
            val principal = CustomOidcUser(
                user,
                authorities,
                OidcIdToken("mock-token", issuedAt, issuedAt.plusSeconds(3600), mapOf("sub" to username))
            )
            SecurityContextHolder.getContext().authentication =
                UsernamePasswordAuthenticationToken(principal, null, authorities)
        }
    }
}
