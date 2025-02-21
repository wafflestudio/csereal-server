package com.wafflestudio.csereal.core.council.database

import com.wafflestudio.csereal.core.council.type.CouncilFileType
import org.springframework.data.jpa.repository.JpaRepository

interface CouncilFileRepository :
    JpaRepository<CouncilFileEntity, Long> {
    fun findByTypeAndKey(type: CouncilFileType, key: String): CouncilFileEntity?
    fun findAllByType(type: CouncilFileType): List<CouncilFileEntity> // TODO: 해당 method의 결과값이 너무 많아지는 경우 key만 추출하는 별도의 method 생성하기.
    fun findAllByTypeAndKeyStartsWith( // TODO: 해당 method의 결과값이 너무 많아지는 경우 key만 추출하는 별도의 method 생성하기.
        type: CouncilFileType,
        keyPrefix: String
    ): List<CouncilFileEntity>
}
