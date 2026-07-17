package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermRepository
import com.wafflestudio.csereal.core.reservation.database.ReserveTermType
import com.wafflestudio.csereal.global.config.MySQLTestContainerConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.springframework.aop.support.AopUtils
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@ActiveProfiles("test")
@SpringBootTest
@Import(MySQLTestContainerConfig::class)
class ReserveTermConcurrencyIntegrationTest(
    private val reserveTermRepository: ReserveTermRepository,
    private val reserveTermPolicy: ReserveTermPolicy,
    private val reconciliationService: ReserveTermReconciliationService,
    private val generationService: ReserveTermGenerationService,
    transactionManager: PlatformTransactionManager
) : FunSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))
    val transactionTemplate = TransactionTemplate(transactionManager).apply {
        propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
    }

    beforeTest {
        reserveTermRepository.deleteAll()
    }

    test("a canonical unique race rolls back and re-audits through the proxied service") {
        AopUtils.isAopProxy(reconciliationService) shouldBe true
        val descriptor = reserveTermPolicy.descriptor(2040, ReserveTermType.FIRST_SEMESTER)
        val barrier = CountDownLatch(2)
        val executor = Executors.newFixedThreadPool(2)
        val results = Collections.synchronizedList(mutableListOf<ReserveTermReconciliationResult>())

        repeat(2) {
            executor.submit {
                try {
                    transactionTemplate.executeWithoutResult {
                        barrier.countDown()
                        barrier.await()
                        reserveTermRepository.saveAndFlush(descriptor.toEntity())
                    }
                    results.add(ReserveTermReconciliationResult.CREATED)
                } catch (exception: DataIntegrityViolationException) {
                    results.add(reconciliationService.verifyAfterConcurrentInsert(descriptor))
                }
            }
        }
        executor.shutdown()
        executor.awaitTermination(10, TimeUnit.SECONDS) shouldBe true

        results.shouldContainExactlyInAnyOrder(
            ReserveTermReconciliationResult.CREATED,
            ReserveTermReconciliationResult.CONCURRENTLY_CREATED
        )
        reserveTermRepository.findByTermYearAndTermType(
            descriptor.termYear,
            descriptor.termType
        ).size shouldBe 1
    }

    test("a failed current descriptor does not roll back creation of the next descriptor") {
        val descriptors = reserveTermPolicy.currentAndNextDescriptors()
        val current = descriptors.first()
        val next = descriptors.last()
        reserveTermRepository.saveAndFlush(
            current.toEntity(applyStartOffsetDays = 1)
        )

        val outcomes = generationService.ensureCurrentAndNext()

        (outcomes.first().error is InvalidReserveTermStateException) shouldBe true
        outcomes.last().result shouldBe ReserveTermReconciliationResult.CREATED
        reserveTermRepository.findByTermYearAndTermType(next.termYear, next.termType).size shouldBe 1
    }
}) {
    companion object {
        private fun ReserveTermDescriptor.toEntity(applyStartOffsetDays: Long = 0): ReserveTermEntity {
            return ReserveTermEntity(
                applyStartTime.plusDays(applyStartOffsetDays),
                applyEndTime,
                termStartTime,
                termEndTime,
                termYear,
                termType
            )
        }
    }
}
