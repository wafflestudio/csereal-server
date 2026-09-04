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
    private val aboutLanguageRepository: AboutLanguageRepository
) : AboutService {

    @Transactional(readOnly = true)
    override fun readAbout(language: LanguageType, postType: AboutPostType): AboutDto {
        val about = aboutRepository.findByLanguageAndPostType(language, postType)
        val imageURL = mainImageService.createImageURL(about.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(about.attachments)

        return AboutDto.of(about, imageURL, attachmentResponses)
    }

    @Transactional
    override fun updateAbout(
        postType: AboutPostType,
        request: UpdateAboutReq,
        newMainImage: MultipartFile?,
        newAttachments: List<MultipartFile>?
    ) {
        val languages = listOf(LanguageType.KO, LanguageType.EN)
        val abouts = languages.map { lang ->
            aboutRepository.findByLanguageAndPostType(lang, postType).apply {
                description = if (lang == LanguageType.KO) request.ko.description else request.en.description
            }
        }

        abouts.forEach { it.syncSearchContent() }

        if (newMainImage != null) {
            abouts.forEach {
                it.mainImage?.let { image -> mainImageService.removeImage(image) }
                mainImageService.uploadMainImage(it, newMainImage)
            }
        } else if (request.removeImage) {
            abouts.forEach {
                it.mainImage?.let { image -> mainImageService.removeImage(image) }
                it.mainImage = null
            }
        }

        val (koAbout, enAbout) = abouts
        attachmentService.syncAttachments(koAbout, request.ko.attachmentIds, newAttachments)
        attachmentService.syncAttachments(enAbout, request.en.attachmentIds, newAttachments)
    }

    @Transactional
    override fun createClub(request: CreateClubReq, mainImage: MultipartFile?) {
        val langToReq = listOf(
            LanguageType.KO to request.ko,
            LanguageType.EN to request.en
        )

        val clubs = langToReq.map { (lang, req) ->
            AboutEntity(
                AboutPostType.STUDENT_CLUBS,
                lang,
                req.name,
                req.description,
                searchContent = ""
            ).apply { syncSearchContent() }
        }

        if (mainImage != null) {
            clubs.forEach { mainImageService.uploadMainImage(it, mainImage) }
        }

        aboutRepository.save(clubs[0])
        aboutRepository.save(clubs[1])
        aboutLanguageRepository.save(AboutLanguageEntity(clubs[0], clubs[1]))
    }

    @Transactional
    override fun updateClub(request: UpdateClubReq, newMainImage: MultipartFile?) {
        val (ko, en) = listOf(request.ko.id, request.en.id).map { id ->
            aboutRepository.findByIdOrNull(id) ?: throw CserealException(ErrorCode.CLUB_NOT_FOUND)
        }

        if (ko.language != LanguageType.KO || en.language != LanguageType.EN) {
            throw CserealException(ErrorCode.LANGUAGE_MISMATCH)
        }

        listOf(ko to request.ko, en to request.en).forEach { (club, clubDto) ->
            updateClubDetails(club, clubDto)
        }

        if (newMainImage != null) {
            listOf(ko, en).forEach { club ->
                club.mainImage?.let { image -> mainImageService.removeImage(image) }
                mainImageService.uploadMainImage(club, newMainImage)
            }
        } else if (request.removeImage) {
            listOf(ko, en).forEach {
                it.mainImage?.let { image -> mainImageService.removeImage(image) }
                it.mainImage = null
            }
        }
    }

    private fun updateClubDetails(club: AboutEntity, clubDto: ClubDto) {
        club.name = clubDto.name
        club.description = clubDto.description
        club.syncSearchContent()
    }

    @Transactional
    override fun deleteClub(id: Long) {
        val club = aboutRepository.findByIdOrNull(id) ?: throw CserealException(ErrorCode.CLUB_NOT_FOUND)
        val aboutLanguage = when (club.language) {
            LanguageType.KO -> aboutLanguageRepository.findByKoAbout(club)
            LanguageType.EN -> aboutLanguageRepository.findByEnAbout(club)
        }

        listOf(aboutLanguage!!.koAbout, aboutLanguage.enAbout).forEach {
            it.mainImage?.let { image -> mainImageService.removeImage(image) }
        }

        aboutLanguageRepository.delete(aboutLanguage)
        aboutRepository.delete(aboutLanguage.koAbout)
        aboutRepository.delete(aboutLanguage.enAbout)
    }

    @Transactional(readOnly = true)
    override fun readAllClubs(language: LanguageType): List<StudentClubDto> {
        val clubs =
            aboutRepository.findAllByLanguageAndPostTypeOrderByName(
                language,
                AboutPostType.STUDENT_CLUBS
            ).map {
                val name = it.name!!.split("(")[0]
                val engName = it.name!!.split("(")[1].replaceFirst(")", "")
                val imageURL = mainImageService.createImageURL(it.mainImage)
                val attachmentResponses =
                    attachmentService.createAttachmentResponses(it.attachments)
                StudentClubDto.of(it, name, engName, imageURL, attachmentResponses)
            }

        return clubs
    }

    @Transactional(readOnly = true)
    override fun readAllGroupedClubs(): List<GroupedClubDto> {
        val clubs = aboutLanguageRepository.findAllByKoAboutPostType(AboutPostType.STUDENT_CLUBS)
            .sortedBy { it.koAbout.name }
        return clubs.map {
            val imageURL = mainImageService.createImageURL(it.koAbout.mainImage)
            GroupedClubDto(ko = ClubDto.of(it.koAbout, imageURL), en = ClubDto.of(it.enAbout, imageURL))
        }
    }

    @Transactional
    override fun createFacilities(request: CreateFacReq, mainImage: MultipartFile?) {
        val langToReq = listOf(
            LanguageType.KO to request.ko,
            LanguageType.EN to request.en
        )

        val facilities = langToReq.map { (lang, req) ->
            AboutEntity(
                AboutPostType.FACILITIES,
                lang,
                req.name,
                req.description,
                searchContent = "",
                locations = req.locations
            ).apply { syncSearchContent() }
        }

        if (mainImage != null) {
            facilities.forEach { mainImageService.uploadMainImage(it, mainImage) }
        }
        aboutRepository.save(facilities[0])
        aboutRepository.save(facilities[1])
        aboutLanguageRepository.save(AboutLanguageEntity(facilities[0], facilities[1]))
    }

    @Transactional
    override fun updateFacility(id: Long, request: UpdateFacReq, newMainImage: MultipartFile?) {
        val facility = aboutRepository.findByIdOrNull(id) ?: throw CserealException(ErrorCode.FACILITY_NOT_FOUND)

        val corresponding = when (facility.language) {
            LanguageType.KO -> aboutLanguageRepository.findByKoAbout(facility)!!.enAbout
            LanguageType.EN -> aboutLanguageRepository.findByEnAbout(facility)!!.koAbout
        }

        when (facility.language) {
            LanguageType.KO -> {
                updateFacility(facility, request.ko)
                updateFacility(corresponding, request.en)
            }

            LanguageType.EN -> {
                updateFacility(facility, request.en)
                updateFacility(corresponding, request.ko)
            }
        }

        facility.syncSearchContent()
        corresponding.syncSearchContent()

        if (newMainImage != null) {
            listOf(facility, corresponding).forEach {
                it.mainImage?.let { image -> mainImageService.removeImage(image) }
                mainImageService.uploadMainImage(it, newMainImage)
            }
        } else if (request.removeImage) {
            listOf(facility, corresponding).forEach {
                it.mainImage?.let { image -> mainImageService.removeImage(image) }
                it.mainImage = null
            }
        }
    }

    private fun updateFacility(facility: AboutEntity, facReq: FacReq) {
        facility.name = facReq.name
        facility.description = facReq.description
        facility.locations = facReq.locations
    }

    @Transactional
    override fun deleteFacility(id: Long) {
        val facility = aboutRepository.findByIdOrNull(id) ?: throw CserealException(ErrorCode.FACILITY_NOT_FOUND)

        val facilityLanguage = when (facility.language) {
            LanguageType.KO -> aboutLanguageRepository.findByKoAbout(facility)
            LanguageType.EN -> aboutLanguageRepository.findByEnAbout(facility)
        }

        listOf(facilityLanguage!!.koAbout, facilityLanguage.enAbout).forEach {
            it.mainImage?.let { image -> mainImageService.removeImage(image) }
        }

        aboutLanguageRepository.delete(facilityLanguage)
        aboutRepository.delete(facilityLanguage.koAbout)
        aboutRepository.delete(facilityLanguage.enAbout)
    }

    @Transactional(readOnly = true)
    override fun readAllGroupedFacilities(): List<GroupedFacDto> {
        val facilities =
            aboutLanguageRepository.findAllByKoAboutPostType(AboutPostType.FACILITIES).sortedBy { it.koAbout.name }
        return facilities.map {
            val koImageURL = mainImageService.createImageURL(it.koAbout.mainImage)
            val enImageURL = mainImageService.createImageURL(it.enAbout.mainImage)
            GroupedFacDto(ko = FacDto.of(it.koAbout, koImageURL), en = FacDto.of(it.enAbout, enImageURL))
        }
    }

    @Transactional(readOnly = true)
    override fun readAllGroupedDirections(): List<GroupedDirectionDto> {
        val directions =
            aboutLanguageRepository.findAllByKoAboutPostType(AboutPostType.DIRECTIONS).sortedBy { it.koAbout.name }
        return directions.map {
            GroupedDirectionDto(ko = DirDto.of(it.koAbout), en = DirDto.of(it.enAbout))
        }
    }

    @Transactional
    override fun updateDirection(id: Long, request: UpdateDescriptionReq) {
        val direction = aboutRepository.findByIdOrNull(id) ?: throw CserealException(ErrorCode.DIRECTION_NOT_FOUND)

        val corresponding = when (direction.language) {
            LanguageType.KO -> aboutLanguageRepository.findByKoAbout(direction)!!.enAbout
            LanguageType.EN -> aboutLanguageRepository.findByEnAbout(direction)!!.koAbout
        }

        when (direction.language) {
            LanguageType.KO -> {
                direction.description = request.koDescription
                corresponding.description = request.enDescription
            }

            LanguageType.EN -> {
                direction.description = request.enDescription
                corresponding.description = request.koDescription
            }
        }

        direction.syncSearchContent()
        corresponding.syncSearchContent()
    }

    @Transactional
    override fun updateFutureCareersPage(request: UpdateDescriptionReq) {
        val ko = aboutRepository.findByLanguageAndPostType(LanguageType.KO, AboutPostType.FUTURE_CAREERS)
        val en = aboutRepository.findByLanguageAndPostType(LanguageType.EN, AboutPostType.FUTURE_CAREERS)

        ko.description = request.koDescription
        en.description = request.enDescription

        ko.syncSearchContent()
        en.syncSearchContent()
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
        val description =
            aboutRepository.findByLanguageAndPostType(
                language,
                AboutPostType.FUTURE_CAREERS
            ).description

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
        aboutRepository.findAll().forEach {
            syncSearchOfAbout(it)
        }
    }

    @Transactional
    fun syncSearchOfAbout(about: AboutEntity) {
        if (about.postType == AboutPostType.FUTURE_CAREERS) {
            about.syncSearchContent(
                statRepository.findAll().map { it.name },
                companyRepository.findAll().map { it.name }
            )
        } else {
            about.syncSearchContent()
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
            aboutRepository.searchAbouts(keyword, language, number, 1)
        return AboutSearchResBody(
            searchCnt,
            searchEntities.map {
                AboutSearchElementDto.of(it, keyword, amount)
            }
        )
    }
}
