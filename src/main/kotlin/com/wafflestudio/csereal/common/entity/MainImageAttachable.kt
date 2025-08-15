package com.wafflestudio.csereal.common.entity

import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity

interface MainImageAttachable {
    var mainImage: MainImageEntity?

    fun getMainImageFolder(): String
}
