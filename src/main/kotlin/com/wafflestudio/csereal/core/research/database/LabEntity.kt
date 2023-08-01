package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.member.database.ProfessorEntity
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity(name = "lab")
class LabEntity(
    
    val name: String,

    @OneToMany(mappedBy = "lab")
    val professors: MutableSet<ProfessorEntity> = mutableSetOf()
) : BaseTimeEntity()
