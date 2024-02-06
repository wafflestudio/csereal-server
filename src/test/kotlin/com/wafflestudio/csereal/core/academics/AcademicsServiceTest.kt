//package com.wafflestudio.csereal.core.academics
//
//import com.wafflestudio.csereal.core.academics.database.AcademicsRepository
//import com.wafflestudio.csereal.core.academics.database.AcademicsSearchRepository
//import com.wafflestudio.csereal.core.academics.dto.AcademicsDto
//import com.wafflestudio.csereal.core.academics.service.AcademicsService
//import io.kotest.core.spec.style.BehaviorSpec
//import io.kotest.extensions.spring.SpringTestExtension
//import io.kotest.extensions.spring.SpringTestLifecycleMode
//import io.kotest.matchers.shouldBe
//import io.kotest.matchers.shouldNotBe
//import jakarta.transaction.Transactional
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.data.repository.findByIdOrNull
//import org.springframework.test.context.ActiveProfiles
//
//
// TODO: Fix test issue
//@SpringBootTest
//@ActiveProfiles("test")
//@Transactional
//class AcademicsServiceTest (
//    private val academicsService: AcademicsService,
//    private val academicsRepository: AcademicsRepository,
//    private val academicsSearchRepository: AcademicsSearchRepository,
//): BehaviorSpec({
//    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))
//
//    Given("첨부파일 없는 Academics를 생성하려고 할 때") {
//        val studentType = "undergraduate"
//        val enumPostType = "guide"
//        val request = AcademicsDto(
//            name = "name",
//            description = "<p>description</p>",
//            year = 2023,
//            time = "12:43",
//        )
//
//        When("Academics를 생성한다면") {
//            val returnDto = academicsService.createAcademics(studentType, enumPostType, request, null)
//
//            Then("Academics가 생성되어야 한다.") {
//                val id = returnDto.id
//                val savedAcademics = academicsRepository.findByIdOrNull(id)
//
//                savedAcademics shouldNotBe null
//                savedAcademics!!.let {
//                    it.name shouldBe request.name
//                    it.description shouldBe request.description
//                    it.year shouldBe request.year
//                    it.time shouldBe request.time
//                }
//            }
//
//            Then("검색 데이터가 생성되어야 한다.") {
//                val savedAcademics = academicsRepository.findByIdOrNull(returnDto.id)!!
//                val createdSearch = savedAcademics.academicsSearch?.id.let {
//                    academicsSearchRepository.findByIdOrNull(it)
//                }
//
//                createdSearch shouldNotBe null
//                createdSearch!!.let {
//                    it.academics shouldBe savedAcademics
//                    it.course shouldBe null
//                    it.scholarship shouldBe null
//                    it.content shouldBe """
//                        name
//                        12:43
//                        2023
//                        학부생
//                        description
//
//                    """.trimIndent()
//                }
//            }
//        }
//    }
//})
