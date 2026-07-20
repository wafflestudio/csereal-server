package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermRepository
import com.wafflestudio.csereal.core.reservation.database.ReserveTermType
import com.wafflestudio.csereal.global.config.MySQLTestContainerConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
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
    private val reserveTermDefaultPolicy: ReserveTermDefaultPolicy,
    private val reserveTermCreationService: ReserveTermCreationService,
    private val generationService: ReserveTermGenerationService,
    transactionManager: PlatformTransactionManager
) : FunSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))
    val transactionTemplate = TransactionTemplate(transactionManager).apply {
        propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
    }

    beforeTest { reserveTermRepository.deleteAll() }
    afterTest { reserveTermRepository.deleteAll() }
    afterSpec { reserveTermRepository.deleteAll() }

    test("concurrent generation converges through the proxied creation service") {
        AopUtils.isAopProxy(reserveTermCreationService) shouldBe true
        val descriptors = reserveTermDefaultPolicy.currentAndNextDescriptors()
        val barrier = CountDownLatch(2)
        val executor = Executors.newFixedThreadPool(2)
        val results = Collections.synchronizedList(
            mutableListOf<Result<List<ReserveTermGenerationOutcome>>>()
        )

        repeat(2) {
            executor.submit {
                barrier.countDown()
                results.add(
                    runCatching {
                        barrier.await()
                        generationService.ensureCurrentAndNext()
                    }
                )
            }
        }
        executor.shutdown()
        try {
            executor.awaitTermination(20, TimeUnit.SECONDS) shouldBe true

            results.size shouldBe 2
            results.all { it.isSuccess } shouldBe true
            val outcomes = results.flatMap { it.getOrThrow() }
            descriptors.forEach { descriptor ->
                val matching = outcomes.filter {
                    it.descriptor.termYear == descriptor.termYear && it.descriptor.termType == descriptor.termType
                }
                matching.size shouldBe 2
                matching.count { it.result == ReserveTermGenerationResult.CREATED } shouldBe 1
                matching.count {
                    it.result in setOf(
                        ReserveTermGenerationResult.CONCURRENTLY_CREATED,
                        ReserveTermGenerationResult.EXISTING
                    )
                } shouldBe 1
                reserveTermRepository.findByTermYearAndTermTypeOrderByIdAsc(
                    descriptor.termYear,
                    descriptor.termType
                ).size shouldBe 1
            }
            reserveTermRepository.count() shouldBe descriptors.size.toLong()
        } finally {
            executor.shutdownNow()
        }
    }

    test("a committed duplicate is classified through a new proxied inspection transaction") {
        AopUtils.isAopProxy(reserveTermCreationService) shouldBe true
        val descriptor = reserveTermDefaultPolicy.descriptor(2040, ReserveTermType.FIRST_SEMESTER)
        transactionTemplate.executeWithoutResult {
            reserveTermRepository.saveAndFlush(descriptor.toEntity())
        }

        val exception = runCatching {
            transactionTemplate.executeWithoutResult {
                reserveTermRepository.saveAndFlush(descriptor.toEntity())
            }
        }.exceptionOrNull()
        (exception is DataIntegrityViolationException) shouldBe true

        reserveTermCreationService.inspectAfterIntegrityFailure(descriptor).result shouldBe
            ReserveTermGenerationResult.CONCURRENTLY_CREATED
    }

    test("an invalid current keyed row is preserved while the next default is still created") {
        val descriptors = reserveTermDefaultPolicy.currentAndNextDescriptors()
        val current = descriptors.first()
        val next = descriptors.last()
        reserveTermRepository.saveAndFlush(current.toEntity(applyEndOffsetDays = -40))

        val outcomes = generationService.ensureCurrentAndNext()

        outcomes.first().result shouldBe ReserveTermGenerationResult.SKIPPED_INVALID_EXISTING
        outcomes.last().result shouldBe ReserveTermGenerationResult.CREATED
        reserveTermRepository.findByTermYearAndTermTypeOrderByIdAsc(next.termYear, next.termType).size shouldBe 1
    }
}) {
    companion object {
        private fun ReserveTermDescriptor.toEntity(applyEndOffsetDays: Long = 0): ReserveTermEntity {
            return ReserveTermEntity(
                applyStartTime,
                applyEndTime.plusDays(applyEndOffsetDays),
                termStartTime,
                termEndTime,
                termYear,
                termType
            )
        }
    }
}
