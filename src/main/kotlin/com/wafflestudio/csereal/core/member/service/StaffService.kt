package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.properties.LanguageType
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
    fun createStaff(createStaffRequest: StaffDto, mainImage: MultipartFile?): StaffDto
    fun getStaff(staffId: Long): StaffDto
    fun getAllStaff(language: String): List<SimpleStaffDto>
    fun updateStaff(staffId: Long, updateStaffRequest: StaffDto, mainImage: MultipartFile?): StaffDto
    fun deleteStaff(staffId: Long)
    fun migrateStaff(requestList: List<StaffDto>): List<StaffDto>
    fun migrateStaffImage(staffId: Long, mainImage: MultipartFile): StaffDto
}

@Service
@Transactional
class StaffServiceImpl(
    private val staffRepository: StaffRepository,
    private val mainImageService: MainImageService
) : StaffService {
    override fun createStaff(createStaffRequest: StaffDto, mainImage: MultipartFile?): StaffDto {
        val enumLanguageType = LanguageType.makeStringToLanguageType(createStaffRequest.language)
        val staff = StaffEntity.of(enumLanguageType, createStaffRequest)

        for (task in createStaffRequest.tasks) {
            TaskEntity.create(task, staff)
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

    override fun updateStaff(staffId: Long, updateStaffRequest: StaffDto, mainImage: MultipartFile?): StaffDto {
        val staff = staffRepository.findByIdOrNull(staffId)
            ?: throw CserealException.Csereal404("해당 행정직원을 찾을 수 없습니다. staffId: $staffId")

        staff.update(updateStaffRequest)

        if (mainImage != null) {
            mainImageService.uploadMainImage(staff, mainImage)
        } else {
            staff.mainImage = null
        }

        // 주요 업무 업데이트
        val oldTasks = staff.tasks.map { it.name }

        val tasksToRemove = oldTasks - updateStaffRequest.tasks
        val tasksToAdd = updateStaffRequest.tasks - oldTasks

        staff.tasks.removeIf { it.name in tasksToRemove }

        for (task in tasksToAdd) {
            TaskEntity.create(task, staff)
        }

        // 검색 엔티티 업데이트
        staff.memberSearch?.update(staff)

        val imageURL = mainImageService.createImageURL(staff.mainImage)

        return StaffDto.of(staff, imageURL)
    }

    override fun deleteStaff(staffId: Long) {
        staffRepository.deleteById(staffId)
    }

    @Transactional
    override fun migrateStaff(requestList: List<StaffDto>): List<StaffDto> {
        val list = mutableListOf<StaffDto>()

        for (request in requestList) {
            val enumLanguageType = LanguageType.makeStringToLanguageType(request.language)
            val staff = StaffEntity.of(enumLanguageType, request)

            for (task in request.tasks) {
                TaskEntity.create(task, staff)
            }

            staff.memberSearch = MemberSearchEntity.create(staff)

            staffRepository.save(staff)

            list.add(StaffDto.of(staff, null))
        }

        return list
    }

    @Transactional
    override fun migrateStaffImage(staffId: Long, mainImage: MultipartFile): StaffDto {
        val staff = staffRepository.findByIdOrNull(staffId)
            ?: throw CserealException.Csereal404("해당 행정직원을 찾을 수 없습니다. staffId: $staffId")

        mainImageService.uploadMainImage(staff, mainImage)

        val imageURL = mainImageService.createImageURL(staff.mainImage)

        return StaffDto.of(staff, imageURL)
    }
}
