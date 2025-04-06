package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.api.req.CreateStaffLanguagesReqBody
import com.wafflestudio.csereal.core.member.api.req.CreateStaffReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyStaffLanguagesReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyStaffReqBody
import com.wafflestudio.csereal.core.member.database.MemberLanguageEntity
import com.wafflestudio.csereal.core.member.database.MemberLanguageRepository
import com.wafflestudio.csereal.core.member.database.MemberSearchEntity
import com.wafflestudio.csereal.core.member.database.StaffEntity
import com.wafflestudio.csereal.core.member.database.StaffRepository
import com.wafflestudio.csereal.core.member.dto.SimpleStaffDto
import com.wafflestudio.csereal.core.member.dto.StaffDto
import com.wafflestudio.csereal.core.member.dto.StaffLanguagesDto
import com.wafflestudio.csereal.core.member.type.MemberType
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface StaffService {
    fun getStaffLanguages(staffId: Long): StaffLanguagesDto
    fun getStaff(staffId: Long): StaffDto
    fun getAllStaff(language: String): List<SimpleStaffDto>

    fun createStaffLanguages(
        createStaffLanguagesReqBody: CreateStaffLanguagesReqBody,
        mainImage: MultipartFile?
    ): StaffLanguagesDto

    fun createStaff(language: LanguageType, createStaffRequest: CreateStaffReqBody, mainImage: MultipartFile?): StaffDto

    fun updateStaffLanguages(
        koStaffId: Long,
        enStaffId: Long,
        updateStaffLanguagesReqBody: ModifyStaffLanguagesReqBody,
        newImage: MultipartFile?
    ): StaffLanguagesDto

    fun updateStaff(staffId: Long, req: ModifyStaffReqBody, newImage: MultipartFile?): StaffDto

    fun deleteStaffLanguages(koStaffId: Long, enStaffId: Long)
    fun deleteStaff(staffId: Long)
}

@Service
@Transactional
class StaffServiceImpl(
    private val staffRepository: StaffRepository,
    private val mainImageService: MainImageService,
    private val memberLanguageRepository: MemberLanguageRepository
) : StaffService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun createStaffLanguages(
        createStaffLanguagesReqBody: CreateStaffLanguagesReqBody,
        mainImage: MultipartFile?
    ): StaffLanguagesDto {
        val koreanStaffDto = createStaff(LanguageType.KO, createStaffLanguagesReqBody.ko, mainImage)
        val englishStaffDto = createStaff(LanguageType.EN, createStaffLanguagesReqBody.en, mainImage)

        memberLanguageRepository.save(
            MemberLanguageEntity(MemberType.STAFF, koreanId = koreanStaffDto.id, englishId = englishStaffDto.id)
        )

        return StaffLanguagesDto(koreanStaffDto, englishStaffDto)
    }

    override fun createStaff(
        language: LanguageType,
        createStaffRequest: CreateStaffReqBody,
        mainImage: MultipartFile?
    ): StaffDto {
        val staff = createStaffRequest.run {
            StaffEntity(
                language = language,
                name,
                role,
                office,
                phone,
                email,
                tasks = tasks.map {it.trim() }.toMutableList()
            )
        }

        if (mainImage != null) {
            mainImageService.uploadMainImage(staff, mainImage)
        }

        staff.memberSearch = MemberSearchEntity.create(staff)

        staffRepository.save(staff)

        val imageURL = mainImageService.createImageURL(staff.mainImage)

        return StaffDto.of(staff, imageURL)
    }

    @Transactional(readOnly = true)
    override fun getStaff(staffId: Long): StaffDto {
        val staff = staffRepository.findByIdOrNull(staffId)
            ?: throw CserealException.Csereal404("해당 행정직원을 찾을 수 없습니다. staffId: $staffId")

        val imageURL = mainImageService.createImageURL(staff.mainImage)

        return StaffDto.of(staff, imageURL)
    }

    @Transactional(readOnly = true)
    override fun getStaffLanguages(staffId: Long): StaffLanguagesDto {
        val staffs = staffRepository.findStaffAllLanguages(staffId)
        if (staffs.isEmpty()) {
            throw CserealException.Csereal404("해당 행정직원을 찾을 수 없습니다. staffId: $staffId")
        }

        if (staffs.any { it.value.size > 1 }) {
            logger.error("staff 데이터 정합성 오류: $staffId")
        }

        return StaffLanguagesDto(
            ko = staffs[LanguageType.KO]?.let {
                StaffDto.of(
                    it.first(),
                    mainImageService.createImageURL(it.first().mainImage)
                )
            },
            en = staffs[LanguageType.EN]?.let {
                StaffDto.of(
                    it.first(),
                    mainImageService.createImageURL(it.first().mainImage)
                )
            }
        )
    }

    @Transactional(readOnly = true)
    override fun getAllStaff(language: String): List<SimpleStaffDto> {
        val enumLanguageType = LanguageType.makeStringToLanguageType(language)

        val sortedStaff = staffRepository.findAllByLanguage(enumLanguageType).map {
            val imageURL = mainImageService.createImageURL(it.mainImage)
            SimpleStaffDto.of(it, imageURL)
        }.sortedBy { it.name }.toMutableList()

        sortedStaff.indexOfFirst { it.email == "misuk@snu.ac.kr" }.takeIf { it != -1 }?.let { index ->
            val headStaff = sortedStaff.removeAt(index)
            sortedStaff.add(0, headStaff)
        }

        return sortedStaff
    }

    override fun updateStaffLanguages(
        koStaffId: Long,
        enStaffId: Long,
        updateStaffLanguagesReqBody: ModifyStaffLanguagesReqBody,
        newImage: MultipartFile?
    ): StaffLanguagesDto {
        // check given id is paired
        if (!memberLanguageRepository.existsByKoreanIdAndEnglishIdAndType(koStaffId, enStaffId, MemberType.STAFF)) {
            throw CserealException.Csereal404("해당 행정직원을 쌍을 찾을 수 없습니다. <$koStaffId, $enStaffId>")
        }

        val koStaffDto = updateStaff(koStaffId, updateStaffLanguagesReqBody.ko, newImage)
        val enStaffDto = updateStaff(enStaffId, updateStaffLanguagesReqBody.en, newImage)
        return StaffLanguagesDto(ko = koStaffDto, en = enStaffDto)
    }

    override fun updateStaff(staffId: Long, req: ModifyStaffReqBody, newImage: MultipartFile?): StaffDto {
        val staff = staffRepository.findByIdOrNull(staffId)
            ?: throw CserealException.Csereal404("해당 행정직원을 찾을 수 없습니다. staffId: $staffId")

        staff.run {
            name = req.name
            role = req.role
            office = req.office
            phone = req.phone
            email = req.email
            tasks = req.tasks.map {it.trim() }.toMutableList()
        }

        if (req.removeImage && newImage == null) {
            if (staff.mainImage != null) {
                mainImageService.removeImage(staff.mainImage!!)
                staff.mainImage = null
            }
        } else if (newImage != null) {
            staff.mainImage?.let {
                mainImageService.removeImage(it)
            }
            mainImageService.uploadMainImage(staff, newImage)
        }

        // 검색 엔티티 업데이트
        staff.memberSearch?.update(staff) ?: let {
            staff.memberSearch = MemberSearchEntity.create(staff)
        }

        val imageURL = mainImageService.createImageURL(staff.mainImage)

        return StaffDto.of(staff, imageURL)
    }

    override fun deleteStaffLanguages(koStaffId: Long, enStaffId: Long) {
        deleteStaff(koStaffId)
        deleteStaff(enStaffId)

        memberLanguageRepository.findByKoreanIdAndEnglishIdAndType(koStaffId, enStaffId, MemberType.STAFF)
            ?.let { memberLanguageRepository.delete(it) }
            ?: throw CserealException.Csereal404("해당 행정직원을 찾을 수 없습니다. <$koStaffId, $enStaffId>")
    }

    override fun deleteStaff(staffId: Long) {
        staffRepository.findByIdOrNull(staffId)?.let { staff ->
            staff.mainImage?.let {
                mainImageService.removeImage(it)
            }
            staffRepository.delete(staff)
        } ?: throw CserealException.Csereal404("해당 행정직원을 찾을 수 없습니다. staffId: $staffId")
    }
}
