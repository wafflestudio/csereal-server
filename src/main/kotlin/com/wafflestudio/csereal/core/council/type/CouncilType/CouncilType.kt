package com.wafflestudio.csereal.core.council.type.CouncilType

import com.wafflestudio.csereal.common.CserealException

private const val MEETING_MINUTE = "MEETING_MINUTE"
private const val RULE = "RULE"
private const val CONSTITUTION = "CONSTITUTION"
private const val BYLAW = "BYLAW"

sealed class CouncilType {
    abstract fun typeValue(): String
    abstract fun dataType(): CouncilDataType

    companion object {
        fun fromString(type: String, subType: String?): CouncilType {
            return if (subType != null) {
                when (type) {
                    MEETING_MINUTE -> MeetingMinute.fromString(subType)
                    RULE -> Rule.fromString(subType)
                    else -> throw CserealException.Csereal400("잘못된 council 타입이 주어졌습니다.")
                }
            } else {
                when (type) {
                    else -> throw CserealException.Csereal400("잘못된 council 타입이 주어졌습니다.")
                }
            }
        }
    }

    data class MeetingMinute( // 회의록
        val year: Int,
        val index: Int
    ) : CouncilType(), CouncilSubType {
        override fun typeValue(): String = MEETING_MINUTE
        override fun subTypeValue(): String = "$year-$index"
        override fun dataType(): CouncilDataType = CouncilDataType.ATTACHMENT

        companion object {
            fun fromString(value: String): MeetingMinute {
                try {
                    val (year, index) = value.split("-").map { it.toInt() }
                    return MeetingMinute(year, index)
                } catch (e: Exception) {
                    throw CserealException.Csereal400("잘못된 council 타입이 주어졌습니다.")
                }
            }
        }
    }

    sealed class Rule( // 규정
    ) : CouncilType(), CouncilSubType {
        object Constitution : Rule() { // 회칙
            override fun typeValue(): String = RULE
            override fun subTypeValue(): String = CONSTITUTION
            override fun dataType(): CouncilDataType = CouncilDataType.ATTACHMENT
        }

        object Bylaw : Rule() { // 세칙
            override fun typeValue(): String = RULE
            override fun subTypeValue(): String = BYLAW
            override fun dataType(): CouncilDataType = CouncilDataType.ATTACHMENT
        }

        companion object {
            fun fromString(value: String): Rule {
                return when (value) {
                    CONSTITUTION -> Constitution
                    BYLAW -> Bylaw
                    else -> throw CserealException.Csereal400("잘못된 council 타입이 주어졌습니다.")
                }
            }
        }
    }
}

interface CouncilSubType {
    fun subTypeValue(): String
}

enum class CouncilDataType {
    ATTACHMENT,
    CONTENT,
    ATTACHMENT_CONTENT
}
