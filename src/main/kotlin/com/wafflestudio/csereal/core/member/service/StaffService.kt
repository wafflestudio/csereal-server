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
    fun createStaff(staffDto: StaffDto): StaffDto
    fun getStaff(staffId: Long): StaffDto
    fun getAllStaff(): List<StaffDto>
    fun updateStaff(staffDto: StaffDto): StaffDto
    fun deleteStaff(staffId: Long)
}

@Service
@Transactional
class StaffServiceImpl(
    private val staffRepository: StaffRepository
) : StaffService {
    override fun createStaff(staffDto: StaffDto): StaffDto {
        val staff = StaffEntity.of(staffDto)

        for (task in staffDto.tasks) {
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

    override fun updateStaff(staffDto: StaffDto): StaffDto {
        val staffId = staffDto.id ?: throw CserealException.Csereal400("업데이트 시 행정직원 id가 필요합니다.")

        val staff = staffRepository.findByIdOrNull(staffId)
            ?: throw CserealException.Csereal404("해당 행정직원을 찾을 수 없습니다. staffId: ${staffId}")

        staff.update(staffDto)

        // 주요 업무 업데이트

        return StaffDto.of(staff)
    }

    override fun deleteStaff(staffId: Long) {
        staffRepository.deleteById(staffId)
    }


}
