package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermRepository
import com.wafflestudio.csereal.core.reservation.dto.CreateCustomReserveTermRequest
import com.wafflestudio.csereal.global.config.MySQLTestContainerConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@SpringBootTest
@Import(MySQLTestContainerConfig::class)
class ReserveTermManualCreationServiceTest(
    private val reserveTermRepository: ReserveTermRepository,
    private val manualCreationService: ReserveTermManualCreationService
) : FunSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    beforeTest { reserveTermRepository.deleteAll() }
    afterTest { reserveTermRepository.deleteAll() }
    afterSpec { reserveTermRepository.deleteAll() }

    test("existing term end equal to candidate start is persisted without changing the existing row") {
        val request = validRequest()
        val existing = reserveTermRepository.saveAndFlush(
            existingTerm(
                termStartTime = request.termStartTime.minusMonths(2),
                termEndTime = request.termStartTime
            )
        )
        val snapshot = existing.snapshot()

        val created = manualCreationService.createCustom(request)

        reserveTermRepository.count() shouldBe 2
        reserveTermRepository.findById(existing.id).orElseThrow().snapshot() shouldBe snapshot
        created.termYear shouldBe null
        created.termType shouldBe null
        created.termStartTime shouldBe request.termStartTime
    }

    test("existing term start equal to candidate end is persisted without changing the existing row") {
        val request = validRequest()
        val existing = reserveTermRepository.saveAndFlush(
            existingTerm(
                termStartTime = request.termEndTime,
                termEndTime = request.termEndTime.plusMonths(2)
            )
        )
        val snapshot = existing.snapshot()

        val created = manualCreationService.createCustom(request)

        reserveTermRepository.count() shouldBe 2
        reserveTermRepository.findById(existing.id).orElseThrow().snapshot() shouldBe snapshot
        created.termYear shouldBe null
        created.termType shouldBe null
        created.termEndTime shouldBe request.termEndTime
    }

    test("positive overlap returns conflict and preserves the only existing row") {
        val request = validRequest()
        val existing = reserveTermRepository.saveAndFlush(
            existingTerm(
                termStartTime = request.termEndTime.minusDays(1),
                termEndTime = request.termEndTime.plusMonths(2)
            )
        )
        val snapshot = existing.snapshot()

        val exception = shouldThrow<CserealException> {
            manualCreationService.createCustom(request)
        }

        exception.status shouldBe HttpStatus.CONFLICT
        reserveTermRepository.count() shouldBe 1
        reserveTermRepository.findById(existing.id).orElseThrow().snapshot() shouldBe snapshot
    }
}) {
    companion object {
        private fun validRequest() = CreateCustomReserveTermRequest(
            applyStartTime = LocalDateTime.of(2027, 2, 1, 9, 0),
            applyEndTime = LocalDateTime.of(2027, 3, 1, 0, 0),
            termStartTime = LocalDateTime.of(2027, 3, 1, 0, 0),
            termEndTime = LocalDateTime.of(2027, 7, 1, 0, 0)
        )

        private fun existingTerm(
            termStartTime: LocalDateTime,
            termEndTime: LocalDateTime
        ) = ReserveTermEntity(
            applyStartTime = termStartTime.minusMonths(2),
            applyEndTime = termStartTime.minusMonths(1),
            termStartTime = termStartTime,
            termEndTime = termEndTime
        )

        private fun ReserveTermEntity.snapshot() = TermSnapshot(
            id = id,
            applyStartTime = applyStartTime,
            applyEndTime = applyEndTime,
            termStartTime = termStartTime,
            termEndTime = termEndTime,
            termYear = termYear,
            termType = termType
        )
    }

    private data class TermSnapshot(
        val id: Long,
        val applyStartTime: LocalDateTime,
        val applyEndTime: LocalDateTime,
        val termStartTime: LocalDateTime,
        val termEndTime: LocalDateTime,
        val termYear: Int?,
        val termType: com.wafflestudio.csereal.core.reservation.database.ReserveTermType?
    )
}
