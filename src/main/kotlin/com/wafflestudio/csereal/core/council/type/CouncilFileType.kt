package com.wafflestudio.csereal.core.council.type

enum class CouncilFileType {
    RULE,
    MEETING_MINUTE;

    fun toJsonValue(): String {
        return this.name.lowercase().replace("_", "-")
    }

    companion object {
        fun fromJsonValue(value: String): CouncilFileType {
            return valueOf(value.uppercase().replace("-", "_"))
        }
    }
}

interface CouncilFileKey {
    fun value(): String
}

interface CouncilFileKeyFactory {
    fun from(value: String): CouncilFileKey
}

enum class CouncilFileRulesKey : CouncilFileKey {
    CONSTITUTION,
    BYLAW;

    override fun value(): String {
        return this.name.lowercase()
    }

    companion object : CouncilFileKeyFactory {
        override fun from(value: String): CouncilFileRulesKey {
            return valueOf(value.uppercase())
        }
    }
}

data class CouncilFileMeetingMinuteKey(
    val year: Int,
    val index: Int
) : CouncilFileKey {
    override fun value(): String {
        return "$year-$index"
    }

    companion object : CouncilFileKeyFactory {
        override fun from(value: String): CouncilFileMeetingMinuteKey {
            val splits = value.split("-")
            if (splits.size != 2) {
                throw IllegalArgumentException("Invalid format")
            }

            val year = splits[0].toInt()
            val index = splits[1].toInt()
            if (!(year in 1900..2100 && index > 0)) {
                throw IllegalArgumentException("Invalid format")
            }

            return CouncilFileMeetingMinuteKey(year, index)
        }
    }
}
