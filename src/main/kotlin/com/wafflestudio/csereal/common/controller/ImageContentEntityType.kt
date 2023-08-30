package com.wafflestudio.csereal.common.controller

import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity

interface ImageContentEntityType {
    fun bringMainImage(): MainImageEntity?
}