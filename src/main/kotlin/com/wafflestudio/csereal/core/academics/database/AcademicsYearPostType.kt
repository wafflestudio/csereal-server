package com.wafflestudio.csereal.core.academics.database

import com.fasterxml.jackson.annotation.JsonValue

/**
 * `/api/v2/academics/{studentType}/{postType}` 계열이 실제로 서빙할 수 있는 종류.
 *
 * AcademicsPostType 은 여섯인데 셋은 이 경로로 도달하지 않는다.
 *   · guide·scholarship  → `/{studentType}/guide`·`/{studentType}/scholarship` 리터럴 경로가 이긴다
 *   · degree-requirements → `/undergraduate/degree-requirements` 리터럴 경로가 이긴다.
 *     대학원엔 그 페이지가 없어서, 좁히기 전엔 `graduate/degree-requirements` 가
 *     404 가 아니라 **빈 배열**을 돌려주고 있었다(연도 목록 핸들러로 흘러가서).
 *
 * 남는 셋만 연도별 목록(AcademicsYearResponse)을 갖는다.
 */
enum class AcademicsYearPostType(
    val postType: AcademicsPostType
) {
    GENERAL_STUDIES_REQUIREMENTS(AcademicsPostType.GENERAL_STUDIES_REQUIREMENTS),
    CURRICULUM(AcademicsPostType.CURRICULUM),
    COURSE_CHANGES(AcademicsPostType.COURSE_CHANGES);

    @JsonValue
    fun toValue() = name.lowercase().replace('_', '-')
}
