package com.wafflestudio.csereal.core.council.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.council.database.CouncilEntity
import com.wafflestudio.csereal.core.council.database.CouncilRepository
import com.wafflestudio.csereal.core.council.database.CouncilType
import com.wafflestudio.csereal.core.council.dto.*
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional
class CouncilService(
    private val councilRepository: CouncilRepository,
    private val mainImageService: MainImageService
) {
    fun createReport(request: ReportCreateRequest, mainImage: MultipartFile?) {
        val report = CouncilEntity.createReport(request)

        if (mainImage != null) {
            mainImageService.uploadMainImage(report, mainImage)
        }

        councilRepository.save(report)
    }

    @Transactional(readOnly = true)
    fun readReport(id: Long): ReportDto {
        val report = councilRepository.findByIdOrNull(id) ?: throw CserealException.Csereal404("Report Not Found")
        val prevReport = councilRepository.findPreviousByType(report.createdAt!!, CouncilType.REPORT)
        val nextReport = councilRepository.findNextByType(report.createdAt!!, CouncilType.REPORT)
        return ReportDto.of(report, prevReport, nextReport)
    }

    @Transactional(readOnly = true)
    fun readAllReports(pageRequest: PageRequest): ReportListDto {
        val reports = councilRepository.findAllByType(CouncilType.REPORT, pageRequest)
        val reportList = reports.content.map {
            val imageURL = mainImageService.createImageURL(it.mainImage)
            SimpleReportDto.of(it, imageURL)
        }
        return ReportListDto(
            total = reports.totalElements,
            reports = reportList
        )
    }

    fun updateReport(id: Long, request: ReportUpdateRequest, newMainImage: MultipartFile?) {
        val report = councilRepository.findByIdOrNull(id) ?: throw CserealException.Csereal404("Report Not Found")
        report.apply {
            title = request.title
            description = request.description
            sequence = request.sequence
            name = request.name
        }
        if (request.removeImage || newMainImage != null) {
            report.mainImage?.let {
                mainImageService.removeImage(it)
                report.mainImage = null
            }
        }
        newMainImage?.let { mainImageService.uploadMainImage(report, it) }
    }

    fun deleteReport(id: Long) {
        val report = councilRepository.findByIdOrNull(id) ?: throw CserealException.Csereal404("Report Not Found")
        report.mainImage?.let { mainImageService.removeImage(it) }
        councilRepository.delete(report)
    }

    @Transactional(readOnly = true)
    fun readIntro(): CouncilIntroDto {
        val intro = councilRepository.findFirstByType(CouncilType.INTRO)
            ?: throw CserealException.Csereal404("Council Intro Not Found")
        val imageURL = mainImageService.createImageURL(intro.mainImage)
        return CouncilIntroDto.of(intro, imageURL)
    }

    fun upsertIntro(request: CouncilIntroUpdateRequest, newMainImage: MultipartFile?) {
        val intro = councilRepository.findFirstByType(CouncilType.INTRO)
            ?: councilRepository.save(CouncilEntity(CouncilType.INTRO, "intro", "", null, 0, ""))
        intro.apply {
            description = request.description
            sequence = request.sequence
            name = request.name
        }
        if (request.removeImage || newMainImage != null) {
            intro.mainImage?.let {
                mainImageService.removeImage(it)
                intro.mainImage = null
            }
        }
        newMainImage?.let { mainImageService.uploadMainImage(intro, it) }
    }
}
