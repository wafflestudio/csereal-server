package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.core.member.database.syncSearch
import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.api.req.CreateStaffLanguagesReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyStaffLanguagesReqBody
import com.wafflestudio.csereal.core.member.database.MemberSearchEntity
import com.wafflestudio.csereal.core.member.database.StaffEntity
import com.wafflestudio.csereal.core.member.database.StaffRepository
import com.wafflestudio.csereal.core.member.database.StaffTranslationEntity
import com.wafflestudio.csereal.core.member.database.StaffTranslationRepository
import com.wafflestudio.csereal.core.member.dto.SimpleStaffDto
import com.wafflestudio.csereal.core.member.dto.StaffLanguagesDto
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface StaffService {
    fun getStaffLanguages(staffId: Long): StaffLanguagesDto
    fun getAllStaff(language: LanguageType): List<SimpleStaffDto>
    fun createStaffLanguages(request: CreateStaffLanguagesReqBody, mainImage: MultipartFile?): StaffLanguagesDto
    fun updateStaffLanguages(
        staffId: Long,
        request: ModifyStaffLanguagesReqBody,
        newImage: MultipartFile?
    ): StaffLanguagesDto

    fun deleteStaffLanguages(staffId: Long)
}

@Service
@Transactional
class StaffServiceImpl(
    private val staffRepository: StaffRepository,
    private val staffTranslationRepository: StaffTranslationRepository,
    private val mainImageService: MainImageService
) : StaffService {

    override fun createStaffLanguages(
        request: CreateStaffLanguagesReqBody,
        mainImage: MultipartFile?
    ): StaffLanguagesDto {
        val staff = StaffEntity(
            phone = request.phone,
            email = request.email
        )
        listOf(LanguageType.KO to request.ko, LanguageType.EN to request.en).forEach { (language, content) ->
            staff.translations.add(
                StaffTranslationEntity(
                    staff = staff,
                    language = language,
                    name = content.name,
                    role = content.role,
                    office = content.office,
                    tasks = content.tasks.map { it.trim() }.toMutableList()
                )
            )
        }

        // 사진은 사람에게 하나뿐이라 한 번만 올린다.
        if (mainImage != null) {
            mainImageService.uploadMainImage(staff, mainImage)
        }
        staff.translations.forEach { it.memberSearch = MemberSearchEntity.create(it) }
        staffRepository.save(staff)

        return staff.toLanguagesDto()
    }

    @Transactional(readOnly = true)
    override fun getStaffLanguages(staffId: Long): StaffLanguagesDto {
        val staff = staffRepository.findByIdOrNull(staffId)
            ?: throw CserealException(ErrorCode.STAFF_NOT_FOUND, mapOf("staffId" to staffId))
        return staff.toLanguagesDto()
    }

    @Transactional(readOnly = true)
    override fun getAllStaff(language: LanguageType): List<SimpleStaffDto> {
        val sortedStaff = staffTranslationRepository.findAllByLanguage(language)
            .map { SimpleStaffDto.of(it, mainImageService.createImageURL(it.staff.mainImage)) }
            .sortedBy { it.name }
            .toMutableList()

        // 행정실장을 맨 앞으로 — 정렬 규칙이 아니라 화면 요구다.
        sortedStaff.indexOfFirst { it.email == "misuk@snu.ac.kr" }.takeIf { it != -1 }?.let { index ->
            sortedStaff.add(0, sortedStaff.removeAt(index))
        }
        return sortedStaff
    }

    override fun updateStaffLanguages(
        staffId: Long,
        request: ModifyStaffLanguagesReqBody,
        newImage: MultipartFile?
    ): StaffLanguagesDto {
        val staff = staffRepository.findByIdOrNull(staffId)
            ?: throw CserealException(ErrorCode.STAFF_NOT_FOUND, mapOf("staffId" to staffId))

        staff.updateShared(request.phone, request.email)

        listOf(LanguageType.KO to request.ko, LanguageType.EN to request.en).forEach { (language, content) ->
            val translation = staff.translationOf(language)
                ?: throw CserealException(ErrorCode.STAFF_NOT_FOUND, mapOf("staffId" to staffId))
            translation.name = content.name
            translation.role = content.role
            translation.office = content.office
            translation.tasks = content.tasks.map { it.trim() }.toMutableList()
            translation.syncSearch()
        }

        mainImageService.replaceMainImage(staff, newImage, request.removeImage)

        return staff.toLanguagesDto()
    }

    override fun deleteStaffLanguages(staffId: Long) {
        val staff = staffRepository.findByIdOrNull(staffId)
            ?: throw CserealException(ErrorCode.STAFF_NOT_FOUND, mapOf("staffId" to staffId))

        staff.mainImage?.let { mainImageService.removeImage(it) }
        // 번역본과 검색 색인은 cascade + orphanRemoval 로 함께 지워진다.
        staffRepository.delete(staff)
    }

    private fun StaffEntity.toLanguagesDto(): StaffLanguagesDto =
        StaffLanguagesDto.of(this, mainImageService.createImageURL(mainImage))
}
