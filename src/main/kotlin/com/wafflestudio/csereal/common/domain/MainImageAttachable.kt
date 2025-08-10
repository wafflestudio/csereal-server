package com.wafflestudio.csereal.common.domain

import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity

interface MainImageAttachable {
    val mainImage: MainImageEntity?
}
