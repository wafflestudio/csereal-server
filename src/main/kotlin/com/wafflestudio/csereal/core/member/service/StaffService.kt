package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.api.req.CreateStaffReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyStaffReqBody
import com.wafflestudio.csereal.core.member.database.MemberSearchEntity
import com.wafflestudio.csereal.core.member.database.StaffEntity
import com.wafflestudio.csereal.core.member.database.StaffRepository
import com.wafflestudio.csereal.core.member.database.TaskEntity
import com.wafflestudio.csereal.core.member.dto.SimpleStaffDto
import com.wafflestudio.csereal.core.member.dto.StaffDto
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface StaffService {
    fun getStaff(staffId: Long): StaffDto
    fun getAllStaff(language: String): List<SimpleStaffDto>
    fun deleteStaff(staffId: Long)
    fun createStaff(createStaffRequest: CreateStaffReqBody, mainImage: MultipartFile?): StaffDto
    fun updateStaff(staffId: Long, req: ModifyStaffReqBody, newImage: MultipartFile?): StaffDto
}

@Service
@Transactional
class StaffServiceImpl(
    private val staffRepository: StaffRepository,
    private val mainImageService: MainImageService
) : StaffService {
    override fun createStaff(createStaffRequest: CreateStaffReqBody, mainImage: MultipartFile?): StaffDto {
        val staff = createStaffRequest.run {
            StaffEntity(
                language = LanguageType.makeStringToLanguageType(language),
                name,
                role,
                office,
                phone,
                email
            )
        }

        createStaffRequest.tasks.forEach {
            TaskEntity.create(it, staff)
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

    override fun updateStaff(staffId: Long, req: ModifyStaffReqBody, newImage: MultipartFile?): StaffDto {
        val staff = staffRepository.findByIdOrNull(staffId)
            ?: throw CserealException.Csereal404("해당 행정직원을 찾을 수 없습니다. staffId: $staffId")

        staff.run {
            language = LanguageType.makeStringToLanguageType(req.language)
            name = req.name
            role = req.role
            office = req.office
            phone = req.phone
            email = req.email
        }

        if (req.removeImage && newImage == null) {
            if (staff.mainImage != null) {
                mainImageService.removeImage(staff.mainImage!!)
                staff.mainImage = null
            }
        } else if (newImage != null) {
            staff.mainImage ?. let {
                mainImageService.removeImage(it)
            }
            mainImageService.uploadMainImage(staff, newImage)
        }

        // 주요 업무 업데이트
        val oldTasks = staff.tasks.map { it.name }
        val tasksToRemove = oldTasks - req.tasks
        val tasksToAdd = req.tasks - oldTasks

        staff.tasks.removeIf { it.name in tasksToRemove }

        for (task in tasksToAdd) {
            TaskEntity.create(task, staff)
        }

        // 검색 엔티티 업데이트
        staff.memberSearch?.update(staff) ?: let {
            staff.memberSearch = MemberSearchEntity.create(staff)
        }

        val imageURL = mainImageService.createImageURL(staff.mainImage)

        return StaffDto.of(staff, imageURL)
    }

    override fun deleteStaff(staffId: Long) {
        staffRepository.findByIdOrNull(staffId) ?. let { staff ->
            staff.mainImage?.let {
                mainImageService.removeImage(it)
            }
            staffRepository.delete(staff)
        }
    }
}
