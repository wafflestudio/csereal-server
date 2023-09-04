package com.wafflestudio.csereal.common.controller

import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity

interface ContentEntityType {
    fun bringMainImage(): MainImageEntity?
}