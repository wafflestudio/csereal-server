package com.wafflestudio.csereal.core.reservation.service

// TODO : resolve concurrency issue, then use proper test
//import com.wafflestudio.csereal.common.CserealException
//import com.wafflestudio.csereal.core.reservation.database.*
//import com.wafflestudio.csereal.core.reservation.dto.ReserveRequest
//import com.wafflestudio.csereal.global.config.MySQLTestContainerConfig
//import io.kotest.core.spec.style.BehaviorSpec
//import io.kotest.extensions.spring.SpringTestExtension
//import io.kotest.extensions.spring.SpringTestLifecycleMode
//import io.kotest.matchers.shouldBe
//import io.kotest.matchers.types.shouldBeInstanceOf
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.context.annotation.Import
//import org.springframework.test.context.ActiveProfiles
//import org.springframework.transaction.annotation.Transactional
//import java.time.LocalDateTime
//import java.util.*
//import java.util.concurrent.CountDownLatch
//import java.util.concurrent.Executors
//import java.util.concurrent.TimeUnit
//
//@ActiveProfiles("test")
//@SpringBootTest
//@Transactional
//@Import(MySQLTestContainerConfig::class)
//class ReservationServiceTest(
//    private val roomRepository: RoomRepository,
//    private val reservationRepository: ReservationRepository,
//    private val reservationService: ReservationService
//) : BehaviorSpec({
//    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))
//
//    lateinit var dummyRoom: RoomEntity
//
//    beforeSpec {
//        dummyRoom = roomRepository.save(RoomEntity("test room", "301", 20, RoomType.SEMINAR))
//    }
//
//    given("User and Request provided") {
//        val startTime = LocalDateTime.now()
//        val endTime = startTime.plusHours(1)
//        val reserveRequest =
//            ReserveRequest(
//                dummyRoom.id,
//                "title",
//                "a@a.com",
//                "010-1234-5678",
//                "prof",
//                "purp",
//                startTime,
//                endTime,
//                true
//            )
//        `when`("multiple concurrent threads try to reserve the room") {
//            val threadCount = 20
//            val latch = CountDownLatch(threadCount)
//            val executor = Executors.newFixedThreadPool(threadCount)
//            val results = Collections.synchronizedList(mutableListOf<Result<Unit>>())
//
//            val task = Runnable {
//                try {
//                    latch.countDown()
//                    latch.await()
//                    reservationService.reserveRoom(reserveRequest)
//                    results.add(Result.success(Unit))
//                } catch (e: Exception) {
//                    results.add(Result.failure(e))
//                }
//            }
//
//            repeat(threadCount) {
//                executor.submit(task)
//            }
//
//            executor.shutdown()
//            executor.awaitTermination(10, TimeUnit.SECONDS)
//
//            then("only one reservation should be successfully saved and the other should fail with a conflict") {
//                val successes = results.count { it.isSuccess }
//                val failures = results.count { it.isFailure }
//
//                successes shouldBe 1
//                failures shouldBe (threadCount - 1)
//
//                results.filter { it.isFailure }
//                    .forEach { result ->
//                        result.exceptionOrNull().shouldBeInstanceOf<CserealException.Csereal409>()
//                    }
//
//                val reservations = reservationRepository.findByRoomIdAndTimeOverlap(
//                    reserveRequest.roomId,
//                    reserveRequest.startTime,
//                    reserveRequest.endTime
//                )
//                reservations.size shouldBe 1
//            }
//        }
//    }
//})
