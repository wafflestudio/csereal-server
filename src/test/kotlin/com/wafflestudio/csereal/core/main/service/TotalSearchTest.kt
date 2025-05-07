package com.wafflestudio.csereal.core.main.service

import com.wafflestudio.csereal.common.enums.ContentSearchSortType
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.about.api.req.ClubReqBody
import com.wafflestudio.csereal.core.about.api.req.CreateClubReq
import com.wafflestudio.csereal.core.about.api.req.CreateFacReq
import com.wafflestudio.csereal.core.about.dto.FacReq
import com.wafflestudio.csereal.core.about.service.AboutService
import com.wafflestudio.csereal.core.academics.api.req.CreateScholarshipReq
import com.wafflestudio.csereal.core.academics.api.req.CreateYearReq
import com.wafflestudio.csereal.core.academics.dto.GroupedCourseDto
import com.wafflestudio.csereal.core.academics.dto.SingleCourseDto
import com.wafflestudio.csereal.core.academics.service.AcademicsSearchService
import com.wafflestudio.csereal.core.academics.service.AcademicsService
import com.wafflestudio.csereal.core.admissions.api.req.AdmissionReqBody
import com.wafflestudio.csereal.core.admissions.service.AdmissionsService
import com.wafflestudio.csereal.core.admissions.type.AdmissionsMainType
import com.wafflestudio.csereal.core.admissions.type.AdmissionsPostType
import com.wafflestudio.csereal.core.member.api.req.CreateProfessorReqBody
import com.wafflestudio.csereal.core.member.api.req.CreateStaffReqBody
import com.wafflestudio.csereal.core.member.database.ProfessorStatus
import com.wafflestudio.csereal.core.member.service.MemberSearchService
import com.wafflestudio.csereal.core.member.service.ProfessorService
import com.wafflestudio.csereal.core.member.service.StaffService
import com.wafflestudio.csereal.core.news.dto.NewsDto
import com.wafflestudio.csereal.core.news.service.NewsService
import com.wafflestudio.csereal.core.notice.dto.NoticeDto
import com.wafflestudio.csereal.core.notice.service.NoticeService
import com.wafflestudio.csereal.core.research.api.req.CreateResearchCenterReqBody
import com.wafflestudio.csereal.core.research.api.req.CreateResearchLanguageReqBody
import com.wafflestudio.csereal.core.research.service.ResearchSearchService
import com.wafflestudio.csereal.core.research.service.ResearchService
import com.wafflestudio.csereal.core.seminar.dto.SeminarDto
import com.wafflestudio.csereal.core.seminar.service.SeminarService
import com.wafflestudio.csereal.global.config.TestContainerInitializer
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = [TestContainerInitializer::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class TotalSearchTest(
    private val aboutService: AboutService,
    private val noticeService: NoticeService,
    private val newsService: NewsService,
    private val seminarService: SeminarService,
    private val professorService: ProfessorService,
    private val staffService: StaffService,
    private val memberSearchService: MemberSearchService,
    private val researchService: ResearchService,
    private val researchSearchService: ResearchSearchService,
    private val admissionsService: AdmissionsService,
    private val academicsService: AcademicsService,
    private val academicsSearchService: AcademicsSearchService,
    private val mainService: MainService
) : BehaviorSpec({
    Given("각 서비스에 keyword가 포함된 여러 글이 있을 때") {
        val keyword = "description"
        val aboutDataNumber = 2
        val noticeDataNumber = 1
        val newsDataNumber = 1
        val seminarDataNumber = 1
        val memberDataNumber = 2
        val researchDataNumber = 1
        val admissionsDataNumber = 1
        val academicsDataNumber = 3

        aboutService.createClub(
            CreateClubReq(
                ko = ClubReqBody(
                    name = "name",
                    description = keyword
                ),
                en = ClubReqBody(
                    name = "name",
                    description = keyword
                )
            ),
            mainImage = null
        )

        aboutService.createFacilities(
            CreateFacReq(
                ko = FacReq(
                    name = "name",
                    description = keyword,
                    locations = mutableListOf()
                ),
                en = FacReq(
                    name = "name",
                    description = keyword,
                    locations = mutableListOf()
                )
            ),
            mainImage = null
        )

        noticeService.createNotice(
            NoticeDto(
                id = -1,
                title = "title",
                titleForMain = null,
                description = keyword,
                author = "username",
                tags = emptyList(),
                createdAt = null,
                modifiedAt = null,
                isPrivate = false,
                isPinned = false,
                pinnedUntil = null,
                isImportant = false,
                importantUntil = null,
                prevId = null,
                prevTitle = null,
                nextId = null,
                nextTitle = null,
                attachments = null
            ),
            attachments = null
        )

        newsService.createNews(
            NewsDto(
                id = -1,
                title = "title",
                titleForMain = null,
                description = keyword,
                tags = emptyList(),
                createdAt = null,
                modifiedAt = null,
                date = LocalDateTime.now(),
                isPrivate = false,
                isSlide = false,
                isImportant = false,
                prevId = null,
                prevTitle = null,
                nextId = null,
                nextTitle = null,
                imageURL = null,
                attachments = null
            ),
            mainImage = null,
            attachments = null
        )

        seminarService.createSeminar(
            SeminarDto(
                id = -1,
                title = "title",
                titleForMain = null,
                description = keyword,
                introduction = "introduction",
                name = "name",
                speakerURL = "speakerURL",
                speakerTitle = "speakerTitle",
                affiliation = "affiliation",
                affiliationURL = "affiliationURL",
                startDate = LocalDateTime.now(),
                endDate = LocalDateTime.now(),
                location = "location",
                host = "host",
                additionalNote = "additionalNote",
                createdAt = null,
                modifiedAt = null,
                isPrivate = false,
                isImportant = false,
                prevId = null,
                prevTitle = null,
                nextId = null,
                nextTitle = null,
                imageURL = null,
                attachments = null
            ),
            mainImage = null,
            attachments = null
        )

        professorService.createProfessor(
            LanguageType.KO,
            CreateProfessorReqBody(
                name = "name",
                email = "email",
                status = ProfessorStatus.ACTIVE,
                academicRank = "academicRank",
                labId = null,
                startDate = null,
                endDate = null,
                office = "office",
                phone = "phone",
                fax = "fax",
                website = "website",
                educations = listOf(keyword, "education2"),
                researchAreas = listOf("researchArea1", "researchArea2"),
                careers = listOf("career1", "career2")
            ),
            mainImage = null
        )

        staffService.createStaff(
            LanguageType.KO,
            CreateStaffReqBody(
                name = "name",
                role = "role",
                office = "office",
                phone = "phone",
                email = "email",
                tasks = listOf(keyword, "task2")
            ),
            mainImage = null
        )

        researchService.createResearchLanguage(
            CreateResearchLanguageReqBody(
                ko = CreateResearchCenterReqBody(
                    name = "한국어 연구소",
                    description = keyword,
                    mainImageUrl = null,
                    websiteURL = "https://www.koreanlab.com"
                ),
                en = CreateResearchCenterReqBody(
                    name = "English Research Center",
                    description = keyword,
                    mainImageUrl = null,
                    websiteURL = "https://www.englishlab.com"
                )
            ),
            mainImage = null
        )

        admissionsService.createAdmission(
            AdmissionReqBody(
                name = "name",
                language = "ko",
                description = "<p>$keyword</p>"
            ),
            AdmissionsMainType.UNDERGRADUATE,
            AdmissionsPostType.REGULAR_ADMISSION
        )

        academicsService.createCourse(
            GroupedCourseDto(
                code = "code",
                credit = 3,
                grade = 1,
                studentType = "undergraduate",
                ko = SingleCourseDto(
                    name = "name",
                    description = keyword,
                    classification = "classification"
                ),
                en = SingleCourseDto(
                    name = "name",
                    description = keyword,
                    classification = "classification"
                )
            )
        )

        academicsService.createAcademicsYearResponse(
            language = "ko",
            studentType = "undergraduate",
            postType = "CURRICULUM",
            request = CreateYearReq(
                name = "name",
                year = 2000,
                description = "<p>$keyword</p>"
            ),
            attachments = null
        )

        academicsService.createScholarship(
            studentType = "undergraduate",
            request = CreateScholarshipReq(
                koName = "name",
                koDescription = "<p>$keyword</p>",
                enName = "name",
                enDescription = "<p>$keyword</p>"
            )
        )

        When("totalSearch를 호출하면") {
            val number = 3
            val memberNumber = 10
            val stringLength = 200
            val language = LanguageType.KO

            val totalSearchResult = mainService.totalSearch(
                keyword = keyword,
                number = number,
                memberNumber = memberNumber,
                stringLength = stringLength,
                language = language
            )

            Then("keyword가 포함된 게시글이 전부 검색이 되야 한다") {
                totalSearchResult.aboutResult.total shouldBe aboutDataNumber
                totalSearchResult.noticeResult.total shouldBe noticeDataNumber
                totalSearchResult.newsResult.total shouldBe newsDataNumber
                totalSearchResult.seminarResult.total shouldBe seminarDataNumber
                totalSearchResult.memberResult.total shouldBe memberDataNumber
                totalSearchResult.researchResult.total shouldBe researchDataNumber
                totalSearchResult.admissionsResult.total shouldBe admissionsDataNumber
                totalSearchResult.academicsResult.total shouldBe academicsDataNumber
            }

            Then("하나씩 검색한 것과 같은 결과야 한다") {
                totalSearchResult.aboutResult shouldBe
                    aboutService.searchTopAbout(
                        keyword,
                        language,
                        number,
                        stringLength
                    )
                totalSearchResult.noticeResult shouldBe
                    noticeService.searchTotalNotice(
                        keyword,
                        number,
                        stringLength
                    )
                totalSearchResult.newsResult shouldBe
                    newsService.searchTotalNews(
                        keyword,
                        number,
                        stringLength
                    )
                totalSearchResult.seminarResult shouldBe
                    seminarService.searchSeminar(
                        keyword,
                        PageRequest.of(0, 10),
                        usePageBtn = true,
                        ContentSearchSortType.DATE
                    )
                totalSearchResult.memberResult shouldBe
                    memberSearchService.searchTopMember(
                        keyword,
                        language,
                        memberNumber
                    )
                totalSearchResult.researchResult shouldBe
                    researchSearchService.searchTopResearch(
                        keyword,
                        language,
                        number,
                        stringLength
                    )
                totalSearchResult.admissionsResult shouldBe
                    admissionsService.searchTopAdmission(
                        keyword,
                        language,
                        number,
                        stringLength
                    )
                totalSearchResult.academicsResult shouldBe
                    academicsSearchService.searchTopAcademics(
                        keyword,
                        language,
                        number,
                        stringLength
                    )
            }
        }
    }
})
