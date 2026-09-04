package com.wafflestudio.csereal.core.resource.attachment.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.entity.AttachmentAttachable
import com.wafflestudio.csereal.common.properties.EndpointProperties
import com.wafflestudio.csereal.core.about.database.AboutEntity
import com.wafflestudio.csereal.core.academics.database.AcademicsEntity
import com.wafflestudio.csereal.core.news.database.NewsEntity
import com.wafflestudio.csereal.core.notice.database.NoticeEntity
import com.wafflestudio.csereal.core.research.database.LabEntity
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentRepository
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentDto
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse
import com.wafflestudio.csereal.core.resource.common.event.FileDeleteEvent
import com.wafflestudio.csereal.core.seminar.database.SeminarEntity
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths

interface AttachmentService {
    fun uploadAttachmentInLabEntity(
        labEntity: LabEntity,
        requestAttachment: MultipartFile
    ): AttachmentDto

    fun uploadAllAttachments(
        contentEntityType: AttachmentAttachable,
        requestAttachments: List<MultipartFile>
    ): List<AttachmentDto>

    fun createOneAttachmentResponse(attachment: AttachmentEntity?): AttachmentResponse?
    fun createAttachmentResponses(attachments: List<AttachmentEntity>?): List<AttachmentResponse>

    fun deleteAttachment(attachment: AttachmentEntity)
    fun deleteAttachments(ids: List<Long>?)

    /**
     * 첨부를 요청의 최종 상태에 맞춘다.
     * [attachmentIds]에 없는 기존 첨부를 지운다(빈 목록 = 전부 삭제, null = 건드리지 않음).
     * 그 뒤 [newFiles]를 올린다.
     */
    fun syncAttachments(
        owner: AttachmentAttachable,
        attachmentIds: List<Long>?,
        newFiles: List<MultipartFile>?
    )
}

@Service
class AttachmentServiceImpl(
    private val attachmentRepository: AttachmentRepository,
    @Value("\${csereal.upload.path}")
    private val path: String,
    private val endpointProperties: EndpointProperties,
    private val eventPublisher: ApplicationEventPublisher
) : AttachmentService {
    override fun uploadAttachmentInLabEntity(labEntity: LabEntity, requestAttachment: MultipartFile): AttachmentDto {
        Files.createDirectories(Paths.get(path))

        val timeMillis = System.currentTimeMillis()

        val filename = "${timeMillis}_${requestAttachment.originalFilename}"
        val totalFilename = path + filename
        val saveFile = Paths.get(totalFilename)
        requestAttachment.transferTo(saveFile)

        val attachment = AttachmentEntity(
            filename = filename,
            size = requestAttachment.size
        )

        labEntity.pdf = attachment
        attachmentRepository.save(attachment)

        return AttachmentDto(
            filename = filename,
            size = requestAttachment.size
        )
    }

    @Transactional
    override fun uploadAllAttachments(
        contentEntityType: AttachmentAttachable,
        requestAttachments: List<MultipartFile>
    ): List<AttachmentDto> {
        Files.createDirectories(Paths.get(path))

        val attachmentsList = mutableListOf<AttachmentDto>()

        for ((index, requestAttachment) in requestAttachments.withIndex()) {
            val timeMillis = System.currentTimeMillis()

            val filename = "${timeMillis}_${requestAttachment.originalFilename}"
            val totalFilename = path + filename
            val saveFile = Paths.get(totalFilename)
            requestAttachment.transferTo(saveFile)

            val attachment = AttachmentEntity(
                filename = filename,
                size = requestAttachment.size
            )

            connectAttachmentToEntity(contentEntityType, attachment)
            attachmentRepository.save(attachment)

            attachmentsList.add(
                AttachmentDto(
                    filename = filename,
                    size = requestAttachment.size
                )
            )
        }
        return attachmentsList
    }

    @Transactional
    override fun createOneAttachmentResponse(attachment: AttachmentEntity?): AttachmentResponse? {
        var attachmentDto: AttachmentResponse? = null
        if (attachment != null) {
            attachmentDto = AttachmentResponse(
                id = attachment.id,
                name = attachment.filename.substringAfter("_"),
                url = "${endpointProperties.backend}/v1/file/${attachment.filename}",
                bytes = attachment.size
            )
        }

        return attachmentDto
    }

    @Transactional
    override fun createAttachmentResponses(attachments: List<AttachmentEntity>?): List<AttachmentResponse> {
        val list = mutableListOf<AttachmentResponse>()
        if (attachments != null) {
            for (attachment in attachments) {
                val attachmentDto = AttachmentResponse(
                    id = attachment.id,
                    name = attachment.filename.substringAfter("_"),
                    url = "${endpointProperties.backend}/v1/file/${attachment.filename}",
                    bytes = attachment.size
                )
                list.add(attachmentDto)
            }
        }
        return list
    }

    @Transactional
    override fun syncAttachments(
        owner: AttachmentAttachable,
        attachmentIds: List<Long>?,
        newFiles: List<MultipartFile>?
    ) {
        // null이면(만들기 등 기존 첨부를 건드리지 않는 요청) 아무것도 지우지 않는다.
        val toDelete = if (attachmentIds == null) emptyList() else owner.attachments.filter { it.id !in attachmentIds }
        // repository.delete 대신 owner 컬렉션에서 뺀다 — orphanRemoval이 행을 지우고, 컬렉션에
        // 지운 엔티티가 남지 않아 이어지는 flush·응답 생성이 안전하다. 파일은 커밋 후 지운다.
        toDelete.forEach {
            owner.attachments.remove(it)
            eventPublisher.publishEvent(FileDeleteEvent(path + it.filename))
        }
        if (newFiles != null) {
            uploadAllAttachments(owner, newFiles)
        }
    }

    @Transactional
    override fun deleteAttachment(attachment: AttachmentEntity) {
        val fileDirectory = path + attachment.filename
        attachmentRepository.delete(attachment)
        eventPublisher.publishEvent(FileDeleteEvent(fileDirectory))
    }

    @Transactional
    override fun deleteAttachments(ids: List<Long>?) {
        if (ids != null) {
            for (id in ids) {
                val attachment = attachmentRepository.findByIdOrNull(id)
                    ?: throw CserealException(ErrorCode.ATTACHMENT_NOT_FOUND, mapOf("id" to id))
                deleteAttachment(attachment)
            }
        }
    }

    private fun connectAttachmentToEntity(contentEntity: AttachmentAttachable, attachment: AttachmentEntity) {
        when (contentEntity) {
            is NewsEntity -> {
                contentEntity.attachments.add(attachment)
                attachment.news = contentEntity
            }

            is NoticeEntity -> {
                contentEntity.attachments.add(attachment)
                attachment.notice = contentEntity
            }

            is SeminarEntity -> {
                contentEntity.attachments.add(attachment)
                attachment.seminar = contentEntity
            }

            is AboutEntity -> {
                contentEntity.attachments.add(attachment)
                attachment.about = contentEntity
            }

            is AcademicsEntity -> {
                contentEntity.attachments.add(attachment)
                attachment.academics = contentEntity
            }
        }
    }
}
