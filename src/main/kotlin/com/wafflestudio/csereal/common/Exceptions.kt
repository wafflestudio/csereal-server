package com.wafflestudio.csereal.common

import org.springframework.http.HttpStatus

open class CserealException(msg: String, val status: HttpStatus) : RuntimeException(msg) {
    class Csereal400(msg: String) : CserealException(msg, HttpStatus.BAD_REQUEST)
    class Csereal404(msg: String) : CserealException(msg, HttpStatus.NOT_FOUND)
}
