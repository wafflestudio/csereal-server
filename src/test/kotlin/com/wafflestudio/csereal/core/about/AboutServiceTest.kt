package com.wafflestudio.csereal.core.about

// FIXME: org.springframework.dao.InvalidDataAccessResourceUsageException
//import com.querydsl.jpa.impl.JPAQueryFactory
//import com.wafflestudio.csereal.core.about.database.*
//import com.wafflestudio.csereal.core.about.dto.AboutDto
//import com.wafflestudio.csereal.core.about.service.AboutService
//import com.wafflestudio.csereal.core.about.service.AboutServiceImpl
//import com.wafflestudio.csereal.global.config.TestConfig
//import io.kotest.core.spec.style.BehaviorSpec
//import io.kotest.extensions.spring.SpringExtension
//import io.kotest.extensions.spring.SpringTestExtension
//import io.kotest.extensions.spring.SpringTestLifecycleMode
//import io.kotest.matchers.shouldBe
//import io.kotest.matchers.shouldNotBe
//import io.mockk.impl.annotations.MockK
//import io.swagger.v3.oas.annotations.extensions.Extension
//import jakarta.persistence.EntityManager
//import org.junit.jupiter.api.extension.ExtendWith
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.context.annotation.Import
//import org.springframework.context.annotation.Profile
//import org.springframework.data.repository.findByIdOrNull
//import org.springframework.test.context.ActiveProfiles
//import org.springframework.transaction.annotation.Transactional
//
////@SpringBootTest(classes = [
////    AboutCustomRepositoryImpl::class,
////    AboutServiceImpl::class,
////])
////@Import(TestConfig::class)
//@SpringBootTest
//@Transactional
//class AboutServiceTest(
//    private val aboutService: AboutService,
//    private val aboutRepository: AboutRepository,
//): BehaviorSpec ({
//    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))
//
//    Given("이미지, 첨부 파일 없는 About 정보가 주어지는 경우") {
//        val postTypeStr = "facilities"
//        val aboutDto = AboutDto(
//            name = "test name",
//            description = "<p>test description</p>",
//            language = "ko",
//            locations = listOf("testLoc1", "testLoc2"),
//            attachments = null,
//            imageURL = null,
//            createdAt = null,
//            modifiedAt = null,
//            year = null,
//        )
//
//        When("About을 생성한다면") {
//            val createdAboutDto = aboutService.createAbout(
//                postTypeStr,
//                aboutDto,
//                null,
//                null
//            )
//
//            Then("반환된 Dto가 주어진 정보와 일치하여야 한다.") {
//                createdAboutDto.id shouldNotBe null
//                createdAboutDto.name shouldBe aboutDto.name
//                createdAboutDto.description shouldBe aboutDto.description
//                createdAboutDto.language shouldBe aboutDto.language
//                createdAboutDto.locations shouldBe aboutDto.locations
//            }
//
//            Then("About이 DB에 저장되어야 한다.") {
//                val about = createdAboutDto.id?.let {
//                    aboutRepository.findByIdOrNull(it)
//                }
//
//                about shouldNotBe null
//                about!!.name shouldBe aboutDto.name
//                about.description shouldBe aboutDto.description
//                about.language shouldBe aboutDto.language
//                about.locations shouldBe aboutDto.locations
//            }
//
//            Then("About의 postType이 facilities여야 한다.") {
//                val about = createdAboutDto.id?.let {
//                    aboutRepository.findByIdOrNull(it)
//                }
//
//                about shouldNotBe null
//                about!!.postType shouldBe AboutPostType.FACILITIES
//            }
//
//            Then("About의 searchContent가 생성되어야 한다.") {
//                val about = createdAboutDto.id?.let {
//                    aboutRepository.findByIdOrNull(it)
//                }
//
//                about shouldNotBe null
//                about!!.searchContent shouldBe """
//                    test name
//                    test description
//                    testLoc1
//                    testLoc2
//                """.trimIndent()
//            }
//        }
//    }
//})
