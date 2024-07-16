package com.wafflestudio.csereal.core.resource.common.service

import com.wafflestudio.csereal.core.resource.common.event.FileDeleteEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.io.File

interface CommonFileService {
    fun removeFile(fileDeleteEvent: FileDeleteEvent)
}

@Service
class CommonFileServiceImpl() : CommonFileService {
    private val log = LoggerFactory.getLogger(this::class.java)

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    override fun removeFile(fileDeleteEvent: FileDeleteEvent) {
        val file = File(fileDeleteEvent.filename)
        if (file.exists()) {
            if (!file.delete()) {
                log.warn("${file.path} is not deleted.")
            }
        }
    }
}
