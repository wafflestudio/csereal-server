package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.member.database.StaffEntity
import com.wafflestudio.csereal.core.member.database.StaffRepository
import com.wafflestudio.csereal.core.member.database.TaskEntity
import com.wafflestudio.csereal.core.member.dto.StaffDto
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface StaffService {
    fun createStaff(createStaffRequest: StaffDto): StaffDto
    fun getStaff(staffId: Long): StaffDto
    fun getAllStaff(): List<StaffDto>
    fun updateStaff(staffId: Long, updateStaffRequest: StaffDto): StaffDto
    fun deleteStaff(staffId: Long)
}

@Service
@Transactional
class StaffServiceImpl(
    private val staffRepository: StaffRepository
) : StaffService {
    override fun createStaff(createStaffRequest: StaffDto): StaffDto {
        val staff = StaffEntity.of(createStaffRequest)

        for (task in createStaffRequest.tasks) {
            TaskEntity.create(task, staff)
        }

        staffRepository.save(staff)

        return StaffDto.of(staff)
    }

    @Transactional(readOnly = true)
    override fun getStaff(staffId: Long): StaffDto {
        val staff = staffRepository.findByIdOrNull(staffId)
            ?: throw CserealException.Csereal404("해당 행정직원을 찾을 수 없습니다. staffId: ${staffId}")
        return StaffDto.of(staff)
    }

    @Transactional(readOnly = true)
    override fun getAllStaff(): List<StaffDto> {
        return staffRepository.findAll().map { StaffDto.of(it) }.sortedBy { it.name }
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

        return StaffDto.of(staff)
    }

    override fun deleteStaff(staffId: Long) {
        staffRepository.deleteById(staffId)
    }


}
