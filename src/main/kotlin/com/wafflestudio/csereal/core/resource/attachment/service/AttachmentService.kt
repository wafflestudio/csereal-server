package com.wafflestudio.csereal.core.resource.attachment.service

import com.wafflestudio.csereal.common.controller.AttachmentContentEntityType
import com.wafflestudio.csereal.core.news.database.NewsEntity
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentRepository
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentDto
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths

interface AttachmentService {
    fun uploadAttachments(
        contentEntityType: AttachmentContentEntityType,
        requestAttachments: List<MultipartFile>,
    ): List<AttachmentDto>

    fun createAttachments(attachments: List<AttachmentEntity>?): List<AttachmentResponse>?
}

@Service
class AttachmentServiceImpl(
    private val attachmentRepository: AttachmentRepository,
    @Value("\${csereal_attachment.upload.path}")
    private val path: String,
) : AttachmentService {
    @Transactional
    override fun uploadAttachments(
        contentEntity: AttachmentContentEntityType,
        requestAttachments: List<MultipartFile>,
    ): List<AttachmentDto> {
        Files.createDirectories(Paths.get(path))

        val attachmentsList = mutableListOf<AttachmentDto>()

        for ((index, requestAttachment) in requestAttachments.withIndex()) {
            val extension = FilenameUtils.getExtension(requestAttachment.originalFilename)

            val timeMillis = System.currentTimeMillis()

            val filename = "${timeMillis}_${requestAttachment.originalFilename}"
            val totalFilename = path + filename
            val saveFile = Paths.get("$totalFilename.$extension")
            requestAttachment.transferTo(saveFile)

            val attachment = AttachmentEntity(
                filename = filename,
                attachmentsOrder = index + 1,
                size = requestAttachment.size,
            )

            connectAttachmentToEntity(contentEntity, attachment)
            attachmentRepository.save(attachment)

            attachmentsList.add(
                AttachmentDto(
                    filename = filename,
                    attachmentsOrder = index + 1,
                    size = requestAttachment.size
                )
            )
        }
        return attachmentsList
    }

    @Transactional
    override fun createAttachments(attachments: List<AttachmentEntity>?): List<AttachmentResponse>? {
        val list = mutableListOf<AttachmentResponse>()
        if (attachments != null) {
            for (attachment in attachments) {
                val attachmentDto = AttachmentResponse(
                    name = attachment.filename,
                    url = "http://cse-dev-waffle.bacchus.io/attachment/${attachment.filename}",
                    bytes = attachment.size,
                )
                list.add(attachmentDto)
            }
        }
        return list
    }

    private fun connectAttachmentToEntity(contentEntity: AttachmentContentEntityType, attachment: AttachmentEntity) {
        when (contentEntity) {
            is NewsEntity -> {
                contentEntity.attachments.add(attachment)
            }
        }
    }
}