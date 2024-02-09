package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.academics.database.AcademicsSearchEntity
import com.wafflestudio.csereal.core.academics.database.AcademicsSearchType

data class AcademicsSearchResponseElement(
    val id: Long,
    val name: String?,
    val academicsType: AcademicsSearchType
) {
    companion object {
        fun of(academicsSearch: AcademicsSearchEntity): AcademicsSearchResponseElement {
            return when {
                academicsSearch.academics != null &&
                    academicsSearch.course == null &&
                    academicsSearch.scholarship == null ->
                    AcademicsSearchResponseElement(
                        id = academicsSearch.academics!!.id,
                        name = academicsSearch.academics!!.name,
                        academicsType = AcademicsSearchType.ACADEMICS
                    )
                academicsSearch.academics == null &&
                    academicsSearch.course != null &&
                    academicsSearch.scholarship == null ->
                    AcademicsSearchResponseElement(
                        id = academicsSearch.course!!.id,
                        name = academicsSearch.course!!.name,
                        academicsType = AcademicsSearchType.COURSE
                    )
                academicsSearch.academics == null &&
                    academicsSearch.course == null &&
                    academicsSearch.scholarship != null ->
                    AcademicsSearchResponseElement(
                        id = academicsSearch.scholarship!!.id,
                        name = academicsSearch.scholarship!!.name,
                        academicsType = AcademicsSearchType.SCHOLARSHIP
                    )
                else -> throw CserealException.Csereal401("AcademicsSearchEntity의 연결이 올바르지 않습니다.")
            }
        }
    }
}
