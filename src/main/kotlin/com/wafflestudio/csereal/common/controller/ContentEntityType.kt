package com.wafflestudio.csereal.common.controller

import com.wafflestudio.csereal.core.resource.image.database.ImageEntity

interface ContentEntityType {
    fun bringMainImage(): ImageEntity?
}