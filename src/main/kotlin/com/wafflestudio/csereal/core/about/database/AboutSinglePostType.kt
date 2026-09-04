package com.wafflestudio.csereal.core.about.database

import com.fasterxml.jackson.annotation.JsonValue

/**
 * `/api/v2/about/{postType}` 가 실제로 서빙할 수 있는 종류.
 *
 * AboutPostType 은 여덟인데 그중 넷(future-careers·student-clubs·facilities·directions)은
 * 전용 경로가 따로 있고, 스프링이 **리터럴 경로를 템플릿보다 우선**하므로 이 경로로는
 * 절대 도달하지 않는다. 게다가 여러 행을 가진 종류는 findByPostType 이 예외를 던진다.
 *
 * 그런데도 경로 파라미터에 AboutPostType 을 그대로 쓰면 OpenAPI 스펙이 "이 경로에
 * student-clubs 를 넣을 수 있다"고 **거짓으로 문서화**한다. 실제로 넣으면 전혀 다른
 * 응답(목록)이 오고, 생성 타입을 믿는 클라이언트는 런타임에 어긋난다.
 * 그래서 도달 가능한 넷만 담은 타입을 따로 둔다.
 */
enum class AboutSinglePostType(
    val postType: AboutPostType
) {
    OVERVIEW(AboutPostType.OVERVIEW),
    GREETINGS(AboutPostType.GREETINGS),
    HISTORY(AboutPostType.HISTORY),
    CONTACT(AboutPostType.CONTACT);

    @JsonValue
    fun toValue() = name.lowercase().replace('_', '-')
}
