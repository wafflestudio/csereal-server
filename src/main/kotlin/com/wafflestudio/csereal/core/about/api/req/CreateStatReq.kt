package com.wafflestudio.csereal.core.about.api.req

data class CreateStatReq(
    val year: Int,
    val statList: List<StatDto>
)

data class StatDto(
    val career: Career,
    val bachelor: Int,
    val master: Int,
    val doctor: Int
)

enum class Career(val krName: String) {
    SAMSUNG("삼성"), LG("LG"), LARGE("기타 대기업"),
    SMALL("중소기업"), GRADUATE("진학"), OTHER("기타")
}
