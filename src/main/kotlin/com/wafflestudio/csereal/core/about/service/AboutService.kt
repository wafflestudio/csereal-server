package com.wafflestudio.csereal.core.about.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.about.api.req.*
import com.wafflestudio.csereal.core.about.api.res.AboutSearchElementDto
import com.wafflestudio.csereal.core.about.api.res.AboutSearchResBody
import com.wafflestudio.csereal.core.about.database.*
import com.wafflestudio.csereal.core.about.dto.*
import com.wafflestudio.csereal.core.main.event.RefreshSearchEvent
import com.wafflestudio.csereal.core.resource.attachment.service.AttachmentService
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.springframework.context.event.EventListener
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface AboutService {
    fun readAbout(language: LanguageType, postType: AboutPostType): AboutDto
    fun updateAbout(
        postType: AboutPostType,
        request: UpdateAboutReq,
        newMainImage: MultipartFile?,
        newAttachments: List<MultipartFile>?
    )

    fun createClub(request: CreateClubReq, mainImage: MultipartFile?)
    fun updateClub(request: UpdateClubReq, newMainImage: MultipartFile?)
    fun deleteClub(id: Long)

    fun readAllClubs(language: LanguageType): List<StudentClubDto>
    fun readAllGroupedClubs(): List<GroupedClubDto>
    fun createFacilities(request: CreateFacReq, mainImage: MultipartFile?)
    fun updateFacility(id: Long, request: UpdateFacReq, newMainImage: MultipartFile?)
    fun deleteFacility(id: Long)
    fun readAllGroupedFacilities(): List<GroupedFacDto>
    fun readAllGroupedDirections(): List<GroupedDirectionDto>
    fun updateDirection(id: Long, request: UpdateDescriptionReq)
    fun updateFutureCareersPage(request: UpdateDescriptionReq)
    fun createFutureCareersStat(request: CreateStatReq)
    fun updateFutureCareersStat(request: CreateStatReq)
    fun readFutureCareers(language: LanguageType): FutureCareersPage
    fun createCompany(request: CreateCompanyReq)
    fun updateCompany(id: Long, request: CreateCompanyReq)
    fun deleteCompany(id: Long)

    fun searchTopAbout(
        keyword: String,
        language: LanguageType,
        number: Int,
        amount: Int
    ): AboutSearchResBody
}

@Service
class AboutServiceImpl(
    private val aboutRepository: AboutRepository,
    private val companyRepository: CompanyRepository,
    private val statRepository: StatRepository,
    private val mainImageService: MainImageService,
    private val attachmentService: AttachmentService,
    private val aboutTranslationRepository: AboutTranslationRepository
) : AboutService {

    @Transactional(readOnly = true)
    override fun readAbout(language: LanguageType, postType: AboutPostType): AboutDto {
        val about = aboutRepository.findByPostType(postType)
        val translation = about.translationOf(language)
            ?: throw CserealException(ErrorCode.ABOUT_NOT_FOUND)
        val imageURL = mainImageService.createImageURL(about.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(about.attachments)

        return AboutDto.of(translation, imageURL, attachmentResponses)
    }

    @Transactional
    override fun updateAbout(
        postType: AboutPostType,
        request: UpdateAboutReq,
        newMainImage: MultipartFile?,
        newAttachments: List<MultipartFile>?
    ) {
        val about = aboutRepository.findByPostType(postType)

        listOf(LanguageType.KO to request.ko, LanguageType.EN to request.en).forEach { (language, content) ->
            val translation = about.translationOf(language)
                ?: throw CserealException(ErrorCode.ABOUT_NOT_FOUND)
            translation.description = content.description
            syncSearchOfTranslation(translation)
        }

        mainImageService.replaceMainImage(about, newMainImage, request.removeImage)

        // 첨부는 콘텐츠에 한 벌뿐이다.
        attachmentService.syncAttachments(about, request.attachmentIds, newAttachments)
    }

    @Transactional
    override fun createClub(request: CreateClubReq, mainImage: MultipartFile?) {
        val club = AboutEntity(postType = AboutPostType.STUDENT_CLUBS)

        listOf(LanguageType.KO to request.ko, LanguageType.EN to request.en).forEach { (language, content) ->
            club.translations.add(
                AboutTranslationEntity(
                    about = club,
                    language = language,
                    name = content.name,
                    description = content.description
                )
            )
        }
        club.translations.forEach { it.syncSearchContent() }

        // 사진은 동아리에 하나뿐이라 한 번만 올린다.
        if (mainImage != null) {
            mainImageService.uploadMainImage(club, mainImage)
        }

        aboutRepository.save(club)
    }

    @Transactional
    override fun updateClub(request: UpdateClubReq, newMainImage: MultipartFile?) {
        val club = aboutRepository.findByIdOrNull(request.id)
            ?: throw CserealException(ErrorCode.CLUB_NOT_FOUND)

        listOf(LanguageType.KO to request.ko, LanguageType.EN to request.en).forEach { (language, content) ->
            val translation = club.translationOf(language)
                ?: throw CserealException(ErrorCode.CLUB_NOT_FOUND)
            translation.name = content.name
            translation.description = content.description
            translation.syncSearchContent()
        }

        mainImageService.replaceMainImage(club, newMainImage, request.removeImage)
    }

    @Transactional
    override fun deleteClub(id: Long) {
        val club = aboutRepository.findByIdOrNull(id) ?: throw CserealException(ErrorCode.CLUB_NOT_FOUND)
        club.mainImage?.let { mainImageService.removeImage(it) }
        // 번역본은 cascade + orphanRemoval 로 함께 지워진다.
        aboutRepository.delete(club)
    }

    @Transactional(readOnly = true)
    override fun readAllClubs(language: LanguageType): List<StudentClubDto> {
        return aboutRepository.findAllByPostType(AboutPostType.STUDENT_CLUBS)
            .mapNotNull { club -> club.translationOf(language) }
            .sortedBy { it.name }
            .map {
                val name = it.name!!.split("(")[0]
                val engName = it.name!!.split("(")[1].replaceFirst(")", "")
                val imageURL = mainImageService.createImageURL(it.about.mainImage)
                val attachmentResponses = attachmentService.createAttachmentResponses(it.about.attachments)
                StudentClubDto.of(it, name, engName, imageURL, attachmentResponses)
            }
    }

    @Transactional(readOnly = true)
    override fun readAllGroupedClubs(): List<GroupedClubDto> {
        return aboutRepository.findAllByPostType(AboutPostType.STUDENT_CLUBS)
            .sortedBy { it.translationOf(LanguageType.KO)?.name }
            .map { GroupedClubDto.of(it, mainImageService.createImageURL(it.mainImage)) }
    }

    @Transactional
    override fun createFacilities(request: CreateFacReq, mainImage: MultipartFile?) {
        val facility = AboutEntity(postType = AboutPostType.FACILITIES)

        listOf(LanguageType.KO to request.ko, LanguageType.EN to request.en).forEach { (language, content) ->
            facility.translations.add(
                AboutTranslationEntity(
                    about = facility,
                    language = language,
                    name = content.name,
                    description = content.description,
                    locations = content.locations
                )
            )
        }
        facility.translations.forEach { it.syncSearchContent() }

        if (mainImage != null) {
            mainImageService.uploadMainImage(facility, mainImage)
        }

        aboutRepository.save(facility)
    }

    @Transactional
    override fun updateFacility(id: Long, request: UpdateFacReq, newMainImage: MultipartFile?) {
        val facility = aboutRepository.findByIdOrNull(id) ?: throw CserealException(ErrorCode.FACILITY_NOT_FOUND)

        listOf(LanguageType.KO to request.ko, LanguageType.EN to request.en).forEach { (language, content) ->
            val translation = facility.translationOf(language)
                ?: throw CserealException(ErrorCode.FACILITY_NOT_FOUND)
            translation.name = content.name
            translation.description = content.description
            translation.locations = content.locations
            translation.syncSearchContent()
        }

        mainImageService.replaceMainImage(facility, newMainImage, request.removeImage)
    }

    @Transactional
    override fun deleteFacility(id: Long) {
        val facility = aboutRepository.findByIdOrNull(id) ?: throw CserealException(ErrorCode.FACILITY_NOT_FOUND)
        facility.mainImage?.let { mainImageService.removeImage(it) }
        aboutRepository.delete(facility)
    }

    @Transactional(readOnly = true)
    override fun readAllGroupedFacilities(): List<GroupedFacDto> {
        return aboutRepository.findAllByPostType(AboutPostType.FACILITIES)
            .sortedBy { it.translationOf(LanguageType.KO)?.name }
            .map { GroupedFacDto.of(it, mainImageService.createImageURL(it.mainImage)) }
    }

    @Transactional(readOnly = true)
    override fun readAllGroupedDirections(): List<GroupedDirectionDto> {
        return aboutRepository.findAllByPostType(AboutPostType.DIRECTIONS)
            .sortedBy { it.translationOf(LanguageType.KO)?.name }
            .map { GroupedDirectionDto.of(it) }
    }

    @Transactional
    override fun updateDirection(id: Long, request: UpdateDescriptionReq) {
        val direction = aboutRepository.findByIdOrNull(id) ?: throw CserealException(ErrorCode.DIRECTION_NOT_FOUND)

        listOf(
            LanguageType.KO to request.koDescription,
            LanguageType.EN to request.enDescription
        ).forEach { (language, description) ->
            val translation = direction.translationOf(language)
                ?: throw CserealException(ErrorCode.DIRECTION_NOT_FOUND)
            translation.description = description
            translation.syncSearchContent()
        }
    }

    @Transactional
    override fun updateFutureCareersPage(request: UpdateDescriptionReq) {
        val page = aboutRepository.findByPostType(AboutPostType.FUTURE_CAREERS)

        listOf(
            LanguageType.KO to request.koDescription,
            LanguageType.EN to request.enDescription
        ).forEach { (language, description) ->
            val translation = page.translationOf(language)
                ?: throw CserealException(ErrorCode.ABOUT_NOT_FOUND)
            translation.description = description
            syncSearchOfTranslation(translation)
        }
    }

    @Transactional
    override fun createFutureCareersStat(request: CreateStatReq) {
        if (statRepository.findAllByYear(request.year).isNotEmpty()) {
            throw CserealException(ErrorCode.STAT_YEAR_ALREADY_EXISTS)
        }
        if (request.statList.size != 6) {
            throw CserealException(ErrorCode.STAT_ROWS_REQUIRED)
        }
        for (stat in request.statList) {
            statRepository.save(StatEntity(request.year, Degree.BACHELOR, stat.career.krName, stat.bachelor))
            statRepository.save(StatEntity(request.year, Degree.MASTER, stat.career.krName, stat.master))
            statRepository.save(StatEntity(request.year, Degree.DOCTOR, stat.career.krName, stat.doctor))
        }
    }

    @Transactional
    override fun updateFutureCareersStat(request: CreateStatReq) {
        val stats = statRepository.findAllByYear(request.year)
        val statsMap = stats.associateBy { it.name to it.degree }

        request.statList.forEach { update ->
            listOf(
                Degree.BACHELOR to update.bachelor,
                Degree.MASTER to update.master,
                Degree.DOCTOR to update.doctor
            ).forEach { (degree, count) ->
                statsMap[update.career.krName to degree]?.count = count
            }
        }
    }

    @Transactional
    override fun readFutureCareers(language: LanguageType): FutureCareersPage {
        val description = aboutRepository.findByPostType(AboutPostType.FUTURE_CAREERS)
            .translationOf(language)?.description
            ?: throw CserealException(ErrorCode.ABOUT_NOT_FOUND)

        val statList = mutableListOf<FutureCareersStatDto>()
        val maxYear = statRepository.findMaxYear()
        for (i: Int in maxYear downTo 2011) {
            val bachelor = statRepository.findAllByYearAndDegree(i, Degree.BACHELOR).map {
                FutureCareersStatDegreeDto.of(it)
            }
            val master = statRepository.findAllByYearAndDegree(i, Degree.MASTER).map {
                FutureCareersStatDegreeDto.of(it)
            }
            val doctor = statRepository.findAllByYearAndDegree(i, Degree.DOCTOR).map {
                FutureCareersStatDegreeDto.of(it)
            }
            statList.add(
                FutureCareersStatDto(i, bachelor, master, doctor)
            )
        }
        val companyList = companyRepository.findAllByOrderByNameDesc().map {
            FutureCareersCompanyDto.of(it)
        }
        return FutureCareersPage(description, statList, companyList)
    }

    @Transactional
    override fun createCompany(request: CreateCompanyReq) {
        companyRepository.save(CompanyEntity(request.name, request.url, request.year))
    }

    @Transactional
    override fun updateCompany(id: Long, request: CreateCompanyReq) {
        val company = companyRepository.findByIdOrNull(id) ?: throw CserealException(ErrorCode.COMPANY_NOT_FOUND)
        company.name = request.name
        company.url = request.url
        company.year = request.year
    }

    @Transactional
    override fun deleteCompany(id: Long) {
        companyRepository.deleteById(id)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @EventListener
    fun refreshSearchListener(event: RefreshSearchEvent) {
        aboutTranslationRepository.findAll().forEach {
            syncSearchOfTranslation(it)
        }
    }

    // 졸업생 진로 페이지의 색인만 통계·기업 이름을 함께 담는다.
    @Transactional
    fun syncSearchOfTranslation(translation: AboutTranslationEntity) {
        if (translation.about.postType == AboutPostType.FUTURE_CAREERS) {
            translation.syncSearchContent(
                statRepository.findAll().map { it.name },
                companyRepository.findAll().map { it.name }
            )
        } else {
            translation.syncSearchContent()
        }
    }

    @Transactional(readOnly = true)
    override fun searchTopAbout(
        keyword: String,
        language: LanguageType,
        number: Int,
        amount: Int
    ): AboutSearchResBody {
        val (searchEntities, searchCnt) =
            aboutTranslationRepository.searchAbouts(keyword, language, number, 1)
        return AboutSearchResBody(
            searchCnt,
            searchEntities.map {
                AboutSearchElementDto.of(it, keyword, amount)
            }
        )
    }
}
