package com.wafflestudio.csereal.common.utils

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import kotlin.math.floor

class FixedPageRequest(pageable: Pageable, total: Long) :
    PageRequest(getPageNum(pageable, total), pageable.pageSize, pageable.sort) {

    companion object {
        private fun getPageNum(pageable: Pageable, total: Long): Int {
            val pageNum = pageable.pageNumber
            val pageSize = pageable.pageSize
            val requestCount = pageNum * pageSize

            if (total > requestCount) {
                return pageNum
            }

            return floor(total.toDouble() / pageSize).toInt()
        }
    }

}
