package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.search.SearchIndexed
import com.wafflestudio.csereal.common.search.SearchType
import com.wafflestudio.csereal.common.enums.LanguageType
import jakarta.persistence.*

@Entity(name = "lab_translation")
class LabTranslationEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id")
    var lab: LabEntity,

    @Enumerated(EnumType.STRING)
    var language: LanguageType,

    var name: String,

    @Column(columnDefinition = "mediumText")
    var description: String? = null,

    var location: String? = null

) : BaseTimeEntity(), SearchIndexed {

    override val searchType get() = SearchType.LAB
    override val searchSourceId get() = lab.id
}
