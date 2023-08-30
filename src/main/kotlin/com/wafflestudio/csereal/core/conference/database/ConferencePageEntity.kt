package com.wafflestudio.csereal.core.conference.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.user.database.UserEntity
import jakarta.persistence.*

@Entity(name = "conferencePage")
class ConferencePageEntity(

    @OneToOne
    @JoinColumn(name = "author_id")
    val author: UserEntity,

    @OneToMany(mappedBy = "conferencePage")
    @OrderBy("code ASC")
    val conferences: List<ConferenceEntity> = mutableListOf()

) : BaseTimeEntity()
