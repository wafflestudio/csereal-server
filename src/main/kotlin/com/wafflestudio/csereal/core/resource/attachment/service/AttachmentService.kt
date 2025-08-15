package com.wafflestudio.csereal.core.resource.attachment.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.entity.AttachmentAttachable
import com.wafflestudio.csereal.common.properties.EndpointProperties
import com.wafflestudio.csereal.core.about.database.AboutEntity
import com.wafflestudio.csereal.core.academics.database.AcademicsEntity
import com.wafflestudio.csereal.core.council.database.CouncilFileEntity
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
import java.lang.invoke.WrongMethodTypeException
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
    fun deleteAttachmentsDeprecated(ids: List<Long>?)
    fun deleteAttachmentDeprecated(attachment: AttachmentEntity)
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
        val folder = "attachment/lab"
        val uploadDir = Paths.get(path, folder)
        Files.createDirectories(uploadDir)

        val timeMillis = System.currentTimeMillis()

        val filename = "${timeMillis}_${requestAttachment.originalFilename}"
        val saveFile = Paths.get(path, folder, filename)
        requestAttachment.transferTo(saveFile)

        val attachment = AttachmentEntity(
            filename = filename,
            folder = folder,
            attachmentsOrder = 1,
            size = requestAttachment.size
        )

        labEntity.pdf = attachment
        attachment.lab = labEntity
        attachmentRepository.save(attachment)

        return AttachmentDto(
            filename = filename,
            attachmentsOrder = 1,
            size = requestAttachment.size
        )
    }

    @Transactional
    override fun uploadAllAttachments(
        contentEntityType: AttachmentAttachable,
        requestAttachments: List<MultipartFile>
    ): List<AttachmentDto> {
        val folder = contentEntityType.getAttachmentFolder()
        val uploadDir = Paths.get(path, folder)
        Files.createDirectories(uploadDir)

        val attachmentsList = mutableListOf<AttachmentDto>()

        for ((index, requestAttachment) in requestAttachments.withIndex()) {
            val timeMillis = System.currentTimeMillis()

            val filename = "${timeMillis}_${requestAttachment.originalFilename}"
            val saveFile = uploadDir.resolve(filename)
            requestAttachment.transferTo(saveFile)

            val attachment = AttachmentEntity(
                filename = filename,
                folder = folder,
                attachmentsOrder = index + 1,
                size = requestAttachment.size
            )

            connectAttachmentToEntity(contentEntityType, attachment)
            //Todo: update에서도 uploadAllAttachments 사용, 이에 따른 attachmentsOrder에 대한 조정 필요
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
    override fun createOneAttachmentResponse(attachment: AttachmentEntity?): AttachmentResponse? {
        var attachmentDto: AttachmentResponse? = null
        if (attachment != null) {
            if (attachment.isDeleted == false) {
                attachmentDto = AttachmentResponse(
                    id = attachment.id,
                    name = attachment.filename.substringAfter("_"),
                    url = "${endpointProperties.backend}/v1/file/${attachment.filePath()}",
                    bytes = attachment.size
                )
            }
        }

        return attachmentDto
    }

    @Transactional
    override fun createAttachmentResponses(attachments: List<AttachmentEntity>?): List<AttachmentResponse> {
        val list = mutableListOf<AttachmentResponse>()
        if (attachments != null) {
            for (attachment in attachments) {
                if (attachment.isDeleted == false) {
                    val attachmentDto = AttachmentResponse(
                        id = attachment.id,
                        name = attachment.filename.substringAfter("_"),
                        url = "${endpointProperties.backend}/v1/file/${attachment.filePath()}",
                        bytes = attachment.size
                    )
                    list.add(attachmentDto)
                }
            }
        }
        return list
    }

    @Transactional
    override fun deleteAttachmentsDeprecated(ids: List<Long>?) {
        if (ids != null) {
            for (id in ids) {
                val attachment = attachmentRepository.findByIdOrNull(id)
                    ?: throw CserealException.Csereal404("id:${id}인 첨부파일을 찾을 수 없습니다.")
                attachment.isDeleted = true
            }
        }
    }

    @Transactional
    override fun deleteAttachmentDeprecated(attachment: AttachmentEntity) {
        attachment.isDeleted = true
    }

    @Transactional
    override fun deleteAttachment(attachment: AttachmentEntity) {
        val fileDirectory = path + attachment.filePath()
        attachmentRepository.delete(attachment)
        eventPublisher.publishEvent(FileDeleteEvent(fileDirectory))
    }

    @Transactional
    override fun deleteAttachments(ids: List<Long>?) {
        if (ids != null) {
            for (id in ids) {
                val attachment = attachmentRepository.findByIdOrNull(id)
                    ?: throw CserealException.Csereal404("id:${id}인 첨부파일을 찾을 수 없습니다.")
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

            is CouncilFileEntity -> {
                contentEntity.attachments.add(attachment)
                attachment.councilFile = contentEntity
            }

            else -> {
                throw WrongMethodTypeException("파일을 엔티티에 연결할 수 없습니다")
            }
        }
    }
}
