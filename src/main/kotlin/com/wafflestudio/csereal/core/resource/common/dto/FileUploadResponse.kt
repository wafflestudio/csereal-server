package com.wafflestudio.csereal.core.resource.common.dto

data class FileUploadResponse(
    val errorMessage: String? = null,
    val result: List<UploadFileInfo>? = null
)

data class UploadFileInfo(
    val url: String,
    val name: String,
    val size: Long
)
