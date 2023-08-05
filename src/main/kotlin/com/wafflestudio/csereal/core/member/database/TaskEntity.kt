package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class TaskEntity(
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    val staff: StaffEntity
) : BaseTimeEntity() {
    companion object {
        fun create(name: String, staff: StaffEntity): TaskEntity {
            val task = TaskEntity(
                name = name,
                staff = staff
            )
            staff.tasks.add(task)
            return task
        }
    }
}
