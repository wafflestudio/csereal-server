package com.wafflestudio.csereal.core.user

import com.fasterxml.jackson.annotation.JsonValue

/** OIDC가 주는 건 그룹(staff·professor…)이고, 로그인 시 그룹을 이 역할로 번역한다 */
enum class RoleType(@get:JsonValue val authority: String) {
    STAFF("ROLE_STAFF"),
    RESERVE("ROLE_RESERVE"),
    RESERVE_PROFESSOR_ROOM("ROLE_RESERVE_PROFESSOR_ROOM"),
    COUNCIL("ROLE_COUNCIL"),
    LABMASTER("ROLE_LABMASTER");

    companion object {
        private val byAuthority = entries.associateBy { it.authority }

        fun fromAuthority(authority: String): RoleType? = byAuthority[authority]
    }
}
