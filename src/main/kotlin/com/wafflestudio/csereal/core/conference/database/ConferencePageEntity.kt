package com.wafflestudio.csereal.core.conference.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.user.database.UserEntity
import jakarta.persistence.*

@Entity(name = "conferencePage")
class ConferencePageEntity(

    @OneToOne
    @JoinColumn(name = "author_id")
    var author: UserEntity,

    @OneToMany(mappedBy = "conferencePage", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("code ASC")
    val conferences: MutableSet<ConferenceEntity> = mutableSetOf()

) : BaseTimeEntity()
