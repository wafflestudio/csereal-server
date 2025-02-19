package com.wafflestudio.csereal.core.council.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.council.database.CouncilFileEntity
import com.wafflestudio.csereal.core.council.database.CouncilFileRepository
import com.wafflestudio.csereal.core.council.dto.CouncilFileDto
import com.wafflestudio.csereal.core.council.dto.CouncilFileMeetingMinuteDto
import com.wafflestudio.csereal.core.council.dto.CouncilFileRuleDto
import com.wafflestudio.csereal.core.council.type.CouncilFileKey
import com.wafflestudio.csereal.core.council.type.CouncilFileMeetingMinuteKey
import com.wafflestudio.csereal.core.council.type.CouncilFileRulesKey
import com.wafflestudio.csereal.core.council.type.CouncilFileType
import com.wafflestudio.csereal.core.resource.attachment.service.AttachmentService
import com.wafflestudio.csereal.core.resource.common.event.FileDeleteEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface CouncilFileService {
    fun createCouncilRule(
        key: CouncilFileRulesKey,
        attachments: List<MultipartFile>
    ): CouncilFileRuleDto

    fun updateCouncilRule(
        key: CouncilFileRulesKey,
        removeFileIds: List<Long>,
        addFiles: List<MultipartFile>
    ): CouncilFileRuleDto

    fun deleteCouncilRule(key: CouncilFileRulesKey)
    fun getCouncilRule(key: CouncilFileRulesKey): CouncilFileRuleDto
    fun getCouncilRules(): List<CouncilFileRuleDto>

    fun createCouncilMeetingMinute(
        year: Int,
        files: List<MultipartFile>
    ): CouncilFileMeetingMinuteDto

    fun updateCouncilMeetingMinute(
        year: Int,
        index: Int,
        removeFileIds: List<Long>,
        addFiles: List<MultipartFile>
    ): CouncilFileMeetingMinuteDto

    fun deleteCouncilMeetingMinute(year: Int, index: Int)
    fun getCouncilMeetingMinute(year: Int, index: Int): CouncilFileMeetingMinuteDto
    fun getCouncilMeetingMinutes(year: Int): List<CouncilFileMeetingMinuteDto>
}

@Service
class CouncilFileServiceImpl(
    private val councilFileRepository: CouncilFileRepository,
    private val attachmentService: AttachmentService,
    private val eventPublisher: ApplicationEventPublisher
) : CouncilFileService {
    // Rule

    @Transactional
    override fun createCouncilRule(
        key: CouncilFileRulesKey,
        attachments: List<MultipartFile>
    ): CouncilFileRuleDto =
        createCouncilFile(CouncilFileType.RULE, key, attachments)
            .let { CouncilFileRuleDto.from(it) }

    @Transactional
    override fun updateCouncilRule(
        key: CouncilFileRulesKey,
        removeFileIds: List<Long>,
        addFiles: List<MultipartFile>
    ): CouncilFileRuleDto =
        updateCouncilFile(CouncilFileType.RULE, key, removeFileIds, addFiles)
            .let { CouncilFileRuleDto.from(it) }

    @Transactional
    override fun deleteCouncilRule(key: CouncilFileRulesKey) {
        deleteCouncilFile(CouncilFileType.RULE, key)
    }

    @Transactional(readOnly = true)
    override fun getCouncilRule(key: CouncilFileRulesKey): CouncilFileRuleDto =
        getCouncilFile(CouncilFileType.RULE, key)
            .let { CouncilFileRuleDto.from(it) }

    @Transactional(readOnly = true)
    override fun getCouncilRules(): List<CouncilFileRuleDto> =
        getCouncilFiles(CouncilFileType.RULE)
            .map { CouncilFileRuleDto.from(it) }

    // MeetingMinute

    @Transactional
    override fun createCouncilMeetingMinute(
        year: Int,
        files: List<MultipartFile>
    ): CouncilFileMeetingMinuteDto {
        val lastIndex = getLastIndexOfMeetingMinute(year)
        val newKey = CouncilFileMeetingMinuteKey(year, lastIndex?.plus(1) ?: 1)
        return createCouncilFile(CouncilFileType.MEETING_MINUTE, newKey, files)
            .let { CouncilFileMeetingMinuteDto.from(it) }
    }

    @Transactional
    override fun updateCouncilMeetingMinute(
        year: Int,
        index: Int,
        removeFileIds: List<Long>,
        addFiles: List<MultipartFile>
    ): CouncilFileMeetingMinuteDto {
        val key = CouncilFileMeetingMinuteKey(year, index)
        return updateCouncilFile(CouncilFileType.MEETING_MINUTE, key, removeFileIds, addFiles)
            .let { CouncilFileMeetingMinuteDto.from(it) }
    }

    // TODO: Add validation for this is the last index of minute
    @Transactional
    override fun deleteCouncilMeetingMinute(year: Int, index: Int) {
        val lastIndex = getLastIndexOfMeetingMinute(year)
        if (lastIndex != index) {
            throw CserealException.Csereal400("해당 회의록은 마지막 회의록이 아닙니다.")
        }

        val key = CouncilFileMeetingMinuteKey(year, index)
        deleteCouncilFile(CouncilFileType.MEETING_MINUTE, key)
    }

    @Transactional(readOnly = true)
    override fun getCouncilMeetingMinute(year: Int, index: Int): CouncilFileMeetingMinuteDto {
        val key = CouncilFileMeetingMinuteKey(year, index)
        return getCouncilFile(CouncilFileType.MEETING_MINUTE, key)
            .let { CouncilFileMeetingMinuteDto.from(it) }
    }

    @Transactional(readOnly = true)
    override fun getCouncilMeetingMinutes(year: Int): List<CouncilFileMeetingMinuteDto> {
        return getCouncilFilesWithKeyPrefixOf(CouncilFileType.MEETING_MINUTE, "$year-")
            .map { CouncilFileMeetingMinuteDto.from(it) }
    }

    @Transactional(readOnly = true)
    fun getLastIndexOfMeetingMinute(year: Int): Int? {
        val keys = getCouncilFilesWithKeyPrefixOf(CouncilFileType.MEETING_MINUTE, "$year-")
            .map { it.key }
            .map { CouncilFileMeetingMinuteKey.from(it) }
            .sortedBy { it.index }
        return keys.lastOrNull()?.index
    }

    // Common File methods

    @Transactional
    fun createCouncilFile(
        type: CouncilFileType,
        key: CouncilFileKey,
        attachments: List<MultipartFile>
    ): CouncilFileDto {
        val councilFile = CouncilFileEntity(type = type, key = key.value())
            .let { councilFileRepository.save(it) }

        attachmentService.uploadAllAttachments(councilFile, attachments)

        return councilFileRepository.save(councilFile).let { entity ->
            CouncilFileDto.from(entity) { attachmentService.createAttachmentResponses(it) }
        }
    }

    @Transactional
    fun updateCouncilFile(
        type: CouncilFileType,
        key: CouncilFileKey,
        removeFileIds: List<Long>,
        addFiles: List<MultipartFile>
    ): CouncilFileDto {
        val councilFile = councilFileRepository.findByTypeAndKey(type, key.value())
            ?: throw CserealException.Csereal400("CouncilFile을 찾을 수 없습니다.")

        councilFile.attachments
            .also { attachments ->
                attachments.filter { it.id in removeFileIds }
                    .map { it.filename }
                    .map { FileDeleteEvent(it) }
                    .forEach { eventPublisher.publishEvent(it) }
            }.also { attachments ->
                attachments.removeAll { it.id in removeFileIds }
            }

        attachmentService.uploadAllAttachments(councilFile, addFiles)

        return councilFileRepository.save(councilFile).let { entity ->
            CouncilFileDto.from(entity) { attachmentService.createAttachmentResponses(it) }
        }
    }

    @Transactional
    fun deleteCouncilFile(type: CouncilFileType, key: CouncilFileKey) {
        val councilFile = councilFileRepository.findByTypeAndKey(type, key.value())
            ?: throw CserealException.Csereal400("CouncilFile을 찾을 수 없습니다.")

        councilFile.attachments
            .map { it.filename }
            .map { FileDeleteEvent(it) }
            .forEach { eventPublisher.publishEvent(it) }

        councilFileRepository.delete(councilFile)
    }

    @Transactional(readOnly = true)
    fun getCouncilFile(type: CouncilFileType, key: CouncilFileKey): CouncilFileDto {
        val councilFile = councilFileRepository.findByTypeAndKey(type, key.value())
            ?: throw CserealException.Csereal400("CouncilFile을 찾을 수 없습니다.")

        return CouncilFileDto.from(councilFile) { attachmentService.createAttachmentResponses(it) }
    }

    @Transactional(readOnly = true)
    fun getCouncilFiles(type: CouncilFileType): List<CouncilFileDto> {
        return councilFileRepository.findAllByType(type).map { entity ->
            CouncilFileDto.from(entity) { attachmentService.createAttachmentResponses(it) }
        }
    }

    @Transactional(readOnly = true)
    fun getCouncilFilesWithKeyPrefixOf(type: CouncilFileType, keyPrefix: String): List<CouncilFileDto> {
        return councilFileRepository.findAllByTypeAndKeyStartsWith(type, keyPrefix).map { entity ->
            CouncilFileDto.from(entity) { attachmentService.createAttachmentResponses(it) }
        }
    }
}
