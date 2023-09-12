package com.wafflestudio.csereal.core.resource.attachment.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.controller.AttachmentContentEntityType
import com.wafflestudio.csereal.common.properties.EndpointProperties
import com.wafflestudio.csereal.core.about.database.AboutEntity
import com.wafflestudio.csereal.core.academics.database.AcademicsEntity
import com.wafflestudio.csereal.core.academics.database.CourseEntity
import com.wafflestudio.csereal.core.news.database.NewsEntity
import com.wafflestudio.csereal.core.notice.database.NoticeEntity
import com.wafflestudio.csereal.core.research.database.LabEntity
import com.wafflestudio.csereal.core.research.database.ResearchEntity
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentRepository
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentDto
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse
import com.wafflestudio.csereal.core.seminar.database.SeminarEntity
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Value
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
        contentEntityType: AttachmentContentEntityType,
        requestAttachments: List<MultipartFile>,
    ): List<AttachmentDto>

    fun createAttachmentResponses(attachments: List<AttachmentEntity>?): List<AttachmentResponse>
//    fun updateAttachmentResponses(
//        contentEntity: AttachmentContentEntityType,
//        attachmentsList: List<AttachmentResponse>
//    )

    fun deleteAttachments(ids: List<Long>)
}

@Service
class AttachmentServiceImpl(
    private val attachmentRepository: AttachmentRepository,
    @Value("\${csereal.upload.path}")
    private val path: String,
    private val endpointProperties: EndpointProperties,
) : AttachmentService {
    override fun uploadAttachmentInLabEntity(labEntity: LabEntity, requestAttachment: MultipartFile): AttachmentDto {
        Files.createDirectories(Paths.get(path))

        val extension = FilenameUtils.getExtension(requestAttachment.originalFilename)

        val timeMillis = System.currentTimeMillis()

        val filename = "${timeMillis}_${requestAttachment.originalFilename}"
        val totalFilename = path + filename
        val saveFile = Paths.get("$totalFilename.$extension")
        requestAttachment.transferTo(saveFile)

        val attachment = AttachmentEntity(
            filename = filename,
            attachmentsOrder = 1,
            size = requestAttachment.size,
        )

        labEntity.pdf = attachment
        attachmentRepository.save(attachment)

        return AttachmentDto(
            filename = filename,
            attachmentsOrder = 1,
            size = requestAttachment.size
        )

    }

    @Transactional
    override fun uploadAllAttachments(
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
    override fun createAttachmentResponses(attachments: List<AttachmentEntity>?): List<AttachmentResponse> {
        val list = mutableListOf<AttachmentResponse>()
        if (attachments != null) {
            for (attachment in attachments) {
                if (attachment.isDeleted == false) {
                    val attachmentDto = AttachmentResponse(
                        id = attachment.id,
                        name = attachment.filename.substringAfter("_"),
                        url = "${endpointProperties.backend}/v1/file/${attachment.filename}",
                        bytes = attachment.size,
                    )
                    list.add(attachmentDto)
                }

            }
        }
        return list
    }

//    @Transactional
//    override fun updateAttachmentResponses(
//        contentEntity: AttachmentContentEntityType,
//        attachmentsList: List<AttachmentResponse>
//    ) {
//        val oldAttachments = contentEntity.bringAttachments().map { it.filename }
//
//        val attachmentsToRemove = oldAttachments - attachmentsList.map { it.name }
//
//        when (contentEntity) {
//            is SeminarEntity -> {
//                for (attachmentFilename in attachmentsToRemove) {
//                    val attachmentEntity = attachmentRepository.findByFilename(attachmentFilename)
//                    attachmentEntity.isDeleted = true
//                    attachmentEntity.seminar = null
//                }
//            }
//        }
//    }

    @Transactional
    override fun deleteAttachments(ids: List<Long>) {
        for (id in ids) {
            val attachment = attachmentRepository.findByIdOrNull(id)
                ?: throw CserealException.Csereal404("id:${id}인 첨부파일을 찾을 수 없습니다.")
            attachment.isDeleted = true
        }
    }


    private fun connectAttachmentToEntity(contentEntity: AttachmentContentEntityType, attachment: AttachmentEntity) {
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

            is CourseEntity -> {
                contentEntity.attachments.add(attachment)
                attachment.course = contentEntity
            }

            is ResearchEntity -> {
                contentEntity.attachments.add(attachment)
                attachment.research = contentEntity
            }
        }
    }

    @Transactional
    override fun deleteAttachment(attachment: AttachmentEntity) {
        attachment.isDeleted = true
        attachment.news = null
        attachment.notice = null
        attachment.seminar = null
        attachment.about = null
        attachment.academics = null
        attachment.course = null
        attachment.research = null
    }
}
