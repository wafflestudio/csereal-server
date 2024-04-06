package com.wafflestudio.csereal.core.about.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.enums.LanguageType
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
    fun createAbout(
        postType: String,
        request: AboutDto,
        mainImage: MultipartFile?,
        attachments: List<MultipartFile>?
    ): AboutDto

    fun readAbout(language: String, postType: String): AboutDto
    fun readAllClubs(language: String): List<StudentClubDto>
    fun readAllFacilities(language: String): List<AboutDto>
    fun readAllDirections(language: String): List<AboutDto>
    fun readFutureCareers(language: String): FutureCareersPage

    fun searchTopAbout(
        keyword: String,
        language: LanguageType,
        number: Int,
        amount: Int
    ): AboutSearchResBody

    fun searchPageAbout(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int,
        amount: Int
    ): AboutSearchResBody

    fun migrateAbout(requestList: List<AboutRequest>): List<AboutDto>
    fun migrateFutureCareers(request: FutureCareersRequest): FutureCareersPage
    fun migrateStudentClubs(requestList: List<StudentClubDto>): List<StudentClubDto>
    fun migrateFacilities(requestList: List<FacilityDto>): List<FacilityDto>
    fun migrateDirections(requestList: List<DirectionDto>): List<DirectionDto>
    fun migrateAboutImageAndAttachments(
        aboutId: Long,
        mainImage: MultipartFile?,
        attachments: List<MultipartFile>?
    ): AboutDto
}

@Service
class AboutServiceImpl(
    private val aboutRepository: AboutRepository,
    private val companyRepository: CompanyRepository,
    private val statRepository: StatRepository,
    private val mainImageService: MainImageService,
    private val attachmentService: AttachmentService
) : AboutService {
    @Transactional
    override fun createAbout(
        postType: String,
        request: AboutDto,
        mainImage: MultipartFile?,
        attachments: List<MultipartFile>?
    ): AboutDto {
        val enumPostType = makeStringToEnum(postType)
        val enumLanguageType = LanguageType.makeStringToLanguageType(request.language)
        var newAbout = AboutEntity.of(enumPostType, enumLanguageType, request)

        if (mainImage != null) {
            mainImageService.uploadMainImage(newAbout, mainImage)
        }

        if (attachments != null) {
            attachmentService.uploadAllAttachments(newAbout, attachments)
        }

        syncSearchOfAbout(newAbout)

        newAbout = aboutRepository.save(newAbout)

        val imageURL = mainImageService.createImageURL(newAbout.mainImage)
        val attachmentResponses =
            attachmentService.createAttachmentResponses(newAbout.attachments)

        return AboutDto.of(newAbout, imageURL, attachmentResponses)
    }

    @Transactional(readOnly = true)
    override fun readAbout(language: String, postType: String): AboutDto {
        val languageType = LanguageType.makeStringToLanguageType(language)
        val enumPostType = makeStringToEnum(postType)
        val about = aboutRepository.findByLanguageAndPostType(languageType, enumPostType)
        val imageURL = mainImageService.createImageURL(about.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(about.attachments)

        return AboutDto.of(about, imageURL, attachmentResponses)
    }

    @Transactional(readOnly = true)
    override fun readAllClubs(language: String): List<StudentClubDto> {
        val languageType = LanguageType.makeStringToLanguageType(language)
        val clubs =
            aboutRepository.findAllByLanguageAndPostTypeOrderByName(
                languageType,
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
    override fun readAllFacilities(language: String): List<AboutDto> {
        val languageType = LanguageType.makeStringToLanguageType(language)
        val facilities =
            aboutRepository.findAllByLanguageAndPostTypeOrderByName(
                languageType,
                AboutPostType.FACILITIES
            ).map {
                val imageURL = mainImageService.createImageURL(it.mainImage)
                val attachmentResponses =
                    attachmentService.createAttachmentResponses(it.attachments)
                AboutDto.of(it, imageURL, attachmentResponses)
            }

        return facilities
    }

    @Transactional(readOnly = true)
    override fun readAllDirections(language: String): List<AboutDto> {
        val languageType = LanguageType.makeStringToLanguageType(language)
        val directions =
            aboutRepository.findAllByLanguageAndPostTypeOrderByName(
                languageType,
                AboutPostType.DIRECTIONS
            ).map {
                val imageURL = mainImageService.createImageURL(it.mainImage)
                val attachments = attachmentService.createAttachmentResponses(it.attachments)
                AboutDto.of(it, imageURL, attachments)
            }

        return directions
    }

    @Transactional
    override fun readFutureCareers(language: String): FutureCareersPage {
        val languageType = LanguageType.makeStringToLanguageType(language)
        val description =
            aboutRepository.findByLanguageAndPostType(
                languageType,
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
        val companyList = companyRepository.findAllByOrderByYearDesc().map {
            FutureCareersCompanyDto.of(it)
        }
        return FutureCareersPage(description, statList, companyList)
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

    @Transactional(readOnly = true)
    override fun searchPageAbout(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int,
        amount: Int
    ): AboutSearchResBody {
        val (searchEntities, searchCnt) = aboutRepository.searchAbouts(
            keyword,
            language,
            pageSize,
            pageNum
        )
        return AboutSearchResBody(
            searchCnt,
            searchEntities.map {
                AboutSearchElementDto.of(it, keyword, amount)
            }
        )
    }

    @Transactional
    override fun migrateAbout(requestList: List<AboutRequest>): List<AboutDto> {
        // Todo: add about migrate search
        val list = mutableListOf<AboutDto>()

        for (request in requestList) {
            val language = request.language
            val description = request.description
            val enumPostType = makeStringToEnum(request.postType)

            val aboutDto = AboutDto(
                id = null,
                language = language,
                name = null,
                description = description,
                year = null,
                createdAt = null,
                modifiedAt = null,
                locations = null,
                imageURL = null,
                attachments = listOf()
            )

            val languageType = LanguageType.makeStringToLanguageType(language)
            var newAbout = AboutEntity.of(enumPostType, languageType, aboutDto)
            syncSearchOfAbout(newAbout)

            newAbout = aboutRepository.save(newAbout)

            list.add(AboutDto.of(newAbout, null, listOf()))
        }
        return list
    }

    @Transactional
    override fun migrateFutureCareers(request: FutureCareersRequest): FutureCareersPage {
        // Todo: add about migrate search
        val description = request.description
        val language = request.language
        val statList = mutableListOf<FutureCareersStatDto>()
        val companyList = mutableListOf<FutureCareersCompanyDto>()

        val aboutDto = AboutDto(
            id = null,
            language = language,
            name = null,
            description = description,
            year = null,
            createdAt = null,
            modifiedAt = null,
            locations = null,
            imageURL = null,
            attachments = listOf()
        )

        val languageType = LanguageType.makeStringToLanguageType(language)

        var newAbout = AboutEntity.of(AboutPostType.FUTURE_CAREERS, languageType, aboutDto)

        for (stat in request.stat) {
            val year = stat.year
            val bachelorList = mutableListOf<FutureCareersStatDegreeDto>()
            val masterList = mutableListOf<FutureCareersStatDegreeDto>()
            val doctorList = mutableListOf<FutureCareersStatDegreeDto>()

            for (bachelor in stat.bachelor) {
                val newBachelor = StatEntity.of(year, Degree.BACHELOR, bachelor)
                statRepository.save(newBachelor)

                bachelorList.add(bachelor)
            }
            for (master in stat.master) {
                val newMaster = StatEntity.of(year, Degree.MASTER, master)
                statRepository.save(newMaster)

                masterList.add(master)
            }
            for (doctor in stat.doctor) {
                val newDoctor = StatEntity.of(year, Degree.DOCTOR, doctor)
                statRepository.save(newDoctor)

                doctorList.add(doctor)
            }
        }

        for (company in request.companies) {
            val newCompany = CompanyEntity.of(company)
            companyRepository.save(newCompany)

            companyList.add(company)
        }

        syncSearchOfAbout(newAbout)
        newAbout = aboutRepository.save(newAbout)

        return FutureCareersPage(description, statList.toList(), companyList.toList())
    }

    @Transactional
    override fun migrateStudentClubs(requestList: List<StudentClubDto>): List<StudentClubDto> {
        val list = mutableListOf<StudentClubDto>()

        for (request in requestList) {
            val language = request.language
            val name = request.name.split("(")[0]
            val engName = request.name.split("(")[1].replaceFirst(")", "")

            val aboutDto = AboutDto(
                id = null,
                language = language,
                name = name,
                description = request.description,
                year = null,
                createdAt = null,
                modifiedAt = null,
                locations = null,
                imageURL = null,
                attachments = listOf()
            )
            val languageType = LanguageType.makeStringToLanguageType(language)

            var newAbout = AboutEntity.of(AboutPostType.STUDENT_CLUBS, languageType, aboutDto)

            syncSearchOfAbout(newAbout)
            newAbout = aboutRepository.save(newAbout)

            list.add(StudentClubDto.of(newAbout, name, engName, null, listOf()))
        }
        return list
    }

    @Transactional
    override fun migrateFacilities(requestList: List<FacilityDto>): List<FacilityDto> =
        // Todo: add about migrate search
        requestList.map {
            AboutDto(
                id = null,
                language = it.language,
                name = it.name,
                description = it.description,
                year = null,
                createdAt = null,
                modifiedAt = null,
                locations = it.locations,
                imageURL = null,
                attachments = listOf()
            ).let { dto ->
                AboutEntity.of(
                    AboutPostType.FACILITIES,
                    LanguageType.makeStringToLanguageType(it.language),
                    dto
                )
            }.also {
                syncSearchOfAbout(it)
            }
        }.let {
            aboutRepository.saveAll(it)
        }.map {
            FacilityDto.of(it)
        }

    @Transactional
    override fun migrateDirections(requestList: List<DirectionDto>): List<DirectionDto> {
        // Todo: add about migrate search
        val list = mutableListOf<DirectionDto>()

        for (request in requestList) {
            val language = request.language
            val name = request.name
            val description = request.description

            val aboutDto = AboutDto(
                id = null,
                language = language,
                name = name,
                description = description,
                year = null,
                createdAt = null,
                modifiedAt = null,
                locations = null,
                imageURL = null,
                attachments = listOf()
            )

            val languageType = LanguageType.makeStringToLanguageType(language)
            var newAbout = AboutEntity.of(AboutPostType.DIRECTIONS, languageType, aboutDto)
            syncSearchOfAbout(newAbout)

            newAbout = aboutRepository.save(newAbout)

            list.add(DirectionDto.of(newAbout))
        }
        return list
    }

    @Transactional
    override fun migrateAboutImageAndAttachments(
        aboutId: Long,
        mainImage: MultipartFile?,
        attachments: List<MultipartFile>?
    ): AboutDto {
        val about = aboutRepository.findByIdOrNull(aboutId)
            ?: throw CserealException.Csereal404("해당 소개는 존재하지 않습니다.")

        if (mainImage != null) {
            mainImageService.uploadMainImage(about, mainImage)
        }

        val imageURL = mainImageService.createImageURL(about.mainImage)
        val attachmentResponses =
            attachmentService.createAttachmentResponses(about.attachments)

        return AboutDto.of(about, imageURL, attachmentResponses)
    }

    private fun makeStringToEnum(postType: String): AboutPostType {
        try {
            val upperPostType = postType.replace("-", "_").uppercase()
            return AboutPostType.valueOf(upperPostType)
        } catch (e: IllegalArgumentException) {
            throw CserealException.Csereal400("해당하는 enum을 찾을 수 없습니다")
        }
    }
}
