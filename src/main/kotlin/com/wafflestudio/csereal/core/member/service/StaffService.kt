package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.common.CserealException
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
    fun getAllStaff(): List<SimpleStaffDto>
    fun updateStaff(staffId: Long, updateStaffRequest: StaffDto, mainImage: MultipartFile?): StaffDto
    fun deleteStaff(staffId: Long)
    fun migrateStaff(requestList: List<StaffDto>): List<StaffDto>
}

@Service
@Transactional
class StaffServiceImpl(
    private val staffRepository: StaffRepository,
    private val mainImageService: MainImageService
) : StaffService {
    override fun createStaff(createStaffRequest: StaffDto, mainImage: MultipartFile?): StaffDto {
        val staff = StaffEntity.of(createStaffRequest)

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
    override fun getAllStaff(): List<SimpleStaffDto> {
        return staffRepository.findAll().map {
            val imageURL = mainImageService.createImageURL(it.mainImage)
            SimpleStaffDto.of(it, imageURL)
        }.sortedBy { it.name }
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
            val staff = StaffEntity.of(request)

            for (task in request.tasks) {
                TaskEntity.create(task, staff)
            }

            staffRepository.save(staff)

            list.add(StaffDto.of(staff, null))
        }

        return list
    }
}
