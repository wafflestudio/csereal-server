package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.common.CserealException
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
    fun getAllStaff(): List<SimpleStaffDto>
    fun updateStaff(staffId: Long, updateStaffRequest: StaffDto): StaffDto
    fun deleteStaff(staffId: Long)
}

@Service
@Transactional
class StaffServiceImpl(
    private val staffRepository: StaffRepository,
    private val mainImageService: MainImageService,
) : StaffService {
    override fun createStaff(createStaffRequest: StaffDto, mainImage: MultipartFile?): StaffDto {
        val staff = StaffEntity.of(createStaffRequest)

        for (task in createStaffRequest.tasks) {
            TaskEntity.create(task, staff)
        }

        if(mainImage != null) {
            mainImageService.uploadMainImage(staff, mainImage)
        }

        staffRepository.save(staff)

        val imageURL = mainImageService.createImageURL(staff.mainImage)

        return StaffDto.of(staff, imageURL)
    }

    @Transactional(readOnly = true)
    override fun getStaff(staffId: Long): StaffDto {
        val staff = staffRepository.findByIdOrNull(staffId)
            ?: throw CserealException.Csereal404("해당 행정직원을 찾을 수 없습니다. staffId: ${staffId}")

        val imageURL = mainImageService.createImageURL(staff.mainImage)

        return StaffDto.of(staff, imageURL)
    }

    @Transactional(readOnly = true)
    override fun getAllStaff(): List<SimpleStaffDto> {
        return staffRepository.findAll().map {
            val imageURL = mainImageService.createImageURL(it.mainImage)
            SimpleStaffDto.of(it, imageURL)
        }.sortedBy { it.name }
    }

    override fun updateStaff(staffId: Long, updateStaffRequest: StaffDto): StaffDto {

        val staff = staffRepository.findByIdOrNull(staffId)
            ?: throw CserealException.Csereal404("해당 행정직원을 찾을 수 없습니다. staffId: ${staffId}")

        staff.update(updateStaffRequest)

        // 주요 업무 업데이트
        val oldTasks = staff.tasks.map { it.name }

        val tasksToRemove = oldTasks - updateStaffRequest.tasks
        val tasksToAdd = updateStaffRequest.tasks - oldTasks

        staff.tasks.removeIf { it.name in tasksToRemove }

        for (task in tasksToAdd) {
            TaskEntity.create(task, staff)
        }

        val imageURL = mainImageService.createImageURL(staff.mainImage)

        return StaffDto.of(staff, imageURL)
    }

    override fun deleteStaff(staffId: Long) {
        staffRepository.deleteById(staffId)
    }


}
