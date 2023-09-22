package com.wafflestudio.csereal.core.admin.dto

data class AdminImportantResponse(
    val total: Long,
    val importants: List<AdminImportantElement> = listOf()
)
