package com.wafflestudio.csereal.core.about.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.about.database.*
import com.wafflestudio.csereal.core.about.dto.*
import com.wafflestudio.csereal.core.resource.attachment.service.AttachmentService
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface AboutService {
    fun createAbout(
        postType: String,
        request: AboutDto,
        mainImage: MultipartFile?,
        attachments: List<MultipartFile>?
    ): AboutDto

    fun readAbout(postType: String): AboutDto
    fun readAllClubs(): List<AboutDto>
    fun readAllFacilities(): List<AboutDto>
    fun readAllDirections(): List<AboutDto>
    fun readFutureCareers(): FutureCareersPage
    fun migrateAbout(requestList: List<AboutRequest>): List<AboutDto>
    fun migrateFutureCareers(request: FutureCareersRequest): FutureCareersResponse

}

@Service
class AboutServiceImpl(
    private val aboutRepository: AboutRepository,
    private val companyRepository: CompanyRepository,
    private val statRepository: StatRepository,
    private val mainImageService: MainImageService,
    private val attachmentService: AttachmentService,
) : AboutService {
    @Transactional
    override fun createAbout(
        postType: String,
        request: AboutDto,
        mainImage: MultipartFile?,
        attachments: List<MultipartFile>?
    ): AboutDto {
        val enumPostType = makeStringToEnum(postType)
        val newAbout = AboutEntity.of(enumPostType, request)

        if (request.locations != null) {
            for (location in request.locations) {
                LocationEntity.create(location, newAbout)
            }
        }

        if (mainImage != null) {
            mainImageService.uploadMainImage(newAbout, mainImage)
        }

        if (attachments != null) {
            attachmentService.uploadAllAttachments(newAbout, attachments)
        }
        aboutRepository.save(newAbout)

        val imageURL = mainImageService.createImageURL(newAbout.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(newAbout.attachments)

        return AboutDto.of(newAbout, imageURL, attachmentResponses)
    }

    @Transactional(readOnly = true)
    override fun readAbout(postType: String): AboutDto {
        val enumPostType = makeStringToEnum(postType)
        val about = aboutRepository.findByPostType(enumPostType)
        val imageURL = mainImageService.createImageURL(about.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(about.attachments)


        return AboutDto.of(about, imageURL, attachmentResponses)
    }

    @Transactional(readOnly = true)
    override fun readAllClubs(): List<AboutDto> {
        val clubs = aboutRepository.findAllByPostTypeOrderByName(AboutPostType.STUDENT_CLUBS).map {
            val imageURL = mainImageService.createImageURL(it.mainImage)
            val attachmentResponses = attachmentService.createAttachmentResponses(it.attachments)
            AboutDto.of(it, imageURL, attachmentResponses)
        }

        return clubs
    }

    @Transactional(readOnly = true)
    override fun readAllFacilities(): List<AboutDto> {
        val facilities = aboutRepository.findAllByPostTypeOrderByName(AboutPostType.FACILITIES).map {
            val imageURL = mainImageService.createImageURL(it.mainImage)
            val attachmentResponses = attachmentService.createAttachmentResponses(it.attachments)
            AboutDto.of(it, imageURL, attachmentResponses)
        }

        return facilities
    }

    @Transactional(readOnly = true)
    override fun readAllDirections(): List<AboutDto> {
        val directions = aboutRepository.findAllByPostTypeOrderByName(AboutPostType.DIRECTIONS).map {
            val imageURL = mainImageService.createImageURL(it.mainImage)
            val attachments = attachmentService.createAttachmentResponses(it.attachments)
            AboutDto.of(it, imageURL, attachments)
        }

        return directions
    }

    @Transactional
    override fun readFutureCareers(): FutureCareersPage {
        val description = "컴퓨터공학을 전공함으로써 벤처기업을 창업할 수 있을 뿐 " +
                "아니라 시스템엔지니어, 보안전문가, 소프트웨어개발자, 데이터베이스관리자 등 " +
                "많은 IT 전문 분야로의 진출이 가능하다. 또한 컴퓨터공학은 바이오, 전자전기, " +
                "로봇, 기계, 의료 등 이공계 영역뿐만 아니라 정치, 경제, 사회, 문화의 다양한 분야와 " +
                "결합되어 미래 지식정보사회에 대한 새로운 가능성을 제시하고 있고 새로운 학문적 과제가 " +
                "지속적으로 생산되기 때문에 많은 전문연구인력이 필요하다.\n" +
                "\n" +
                "서울대학교 컴퓨터공학부의 경우 학부 졸업생 절반 이상이 대학원에 진학하고 있다. " +
                "대학원에 진학하면 여러 전공분야 중 하나를 선택하여 보다 깊이 있는 지식의 습득과 연구과정을 거치게 되며 " +
                "그 이후로는 국내외 관련 산업계, 학계에 주로 진출하고 있고, 새로운 아이디어로 벤처기업을 창업하기도 한다."

        val statList = mutableListOf<StatDto>()
        for (i: Int in 2021 downTo 2011) {
            val bachelor = statRepository.findAllByYearAndDegree(i, Degree.BACHELOR).map {
                CompanyNameAndCountDto(
                    id = it.id,
                    name = it.name,
                    count = it.count
                )
            }
            val master = statRepository.findAllByYearAndDegree(i, Degree.MASTER).map {
                CompanyNameAndCountDto(
                    id = it.id,
                    name = it.name,
                    count = it.count,
                )
            }
            val doctor = statRepository.findAllByYearAndDegree(i, Degree.DOCTOR).map {
                CompanyNameAndCountDto(
                    id = it.id,
                    name = it.name,
                    count = it.count,
                )
            }
            statList.add(
                StatDto(
                    year = i,
                    bachelor = bachelor,
                    master = master,
                    doctor = doctor,
                )
            )
        }
        val companyList = companyRepository.findAllByOrderByYearDesc().map {
            CompanyDto(
                name = it.name,
                url = it.url,
                year = it.year
            )
        }
        return FutureCareersPage(description, statList, companyList)
    }

    override fun migrateAbout(requestList: List<AboutRequest>): List<AboutDto> {
        val list = mutableListOf<AboutDto>()

        for (request in requestList) {
            val enumPostType = makeStringToEnum(request.postType)

            val aboutDto = AboutDto(
                id = request.id,
                name = request.name,
                engName = request.engName,
                description = request.description,
                year = request.year,
                createdAt = request.createdAt,
                modifiedAt = request.modifiedAt,
                locations = request.locations,
                imageURL = null,
                attachments = listOf()
            )
            val newAbout = AboutEntity.of(enumPostType, aboutDto)

            if (request.locations != null) {
                for (location in request.locations) {
                    LocationEntity.create(location, newAbout)
                }
            }

            aboutRepository.save(newAbout)

            list.add(AboutDto.of(newAbout, null, listOf()))

        }
        return list
    }

    override fun migrateFutureCareers(request: FutureCareersRequest): FutureCareersResponse {
        val description = request.description
        val statList = mutableListOf<FutureCareersStatDto>()
        val companyList = mutableListOf<FutureCareersCompanyDto>()

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


        return FutureCareersResponse(description, statList.toList(), companyList.toList())
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