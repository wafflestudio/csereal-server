package com.wafflestudio.csereal.core.recruit.service

import com.wafflestudio.csereal.core.recruit.api.req.ModifyRecruitReqBody
import com.wafflestudio.csereal.core.recruit.database.RecruitEntity
import com.wafflestudio.csereal.core.recruit.database.RecruitRepository
import com.wafflestudio.csereal.core.recruit.dto.RecruitPage
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface RecruitService {
    fun getRecruitPage(): RecruitPage
    fun upsertRecruitPage(modifyRecruitReqBody: ModifyRecruitReqBody, newMainImage: MultipartFile?): RecruitPage
}

@Service
@Transactional
class RecruitServiceImpl(
    private val recruitRepository: RecruitRepository,
    private val mainImageService: MainImageService
) : RecruitService {

    @Transactional(readOnly = true)
    override fun getRecruitPage(): RecruitPage {
        // return empty page if not exists
        return recruitRepository.findAll().firstOrNull()
            ?.let { RecruitPage.of(it, mainImageService.createImageURL(it.mainImage)) }
            ?: RecruitPage.empty()
    }

    @Transactional
    override fun upsertRecruitPage(
        modifyRecruitReqBody: ModifyRecruitReqBody,
        newMainImage: MultipartFile?
    ): RecruitPage {
        val oldRecruitEntities = recruitRepository.findAll()

        val modifiedRecruitEntity = when (oldRecruitEntities.size) {
            0 -> modifyRecruitReqBody.let {
                RecruitEntity(
                    it.latestRecruitTitle,
                    it.latestRecruitUrl,
                    it.description
                )
            }

            1 -> oldRecruitEntities.first().apply {
                modifyRecruitReqBody.let {
                    latestRecruitTitle = it.latestRecruitTitle
                    latestRecruitUrl = it.latestRecruitUrl
                    description = it.description
                }
            }

            else -> oldRecruitEntities.also { entities ->
                // remove leftovers
                entities.subList(1, entities.size).let {
                    it.mapNotNull(RecruitEntity::mainImage)
                        .forEach(mainImageService::removeImage)

                    recruitRepository.deleteAll(it)
                }
            }.first().apply {
                // modify first
                modifyRecruitReqBody.let {
                    latestRecruitTitle = it.latestRecruitTitle
                    latestRecruitUrl = it.latestRecruitUrl
                    description = it.description
                }
            }
        }

        if (modifiedRecruitEntity.mainImage != null &&
            (newMainImage != null || modifyRecruitReqBody.removeImage)
        ) {
            mainImageService.removeImage(modifiedRecruitEntity.mainImage!!)
            modifiedRecruitEntity.mainImage = null
        }
        newMainImage?.let { mainImageService.uploadMainImage(modifiedRecruitEntity, it) }

        return modifiedRecruitEntity.let {
            recruitRepository.save(it)
        }.let {
            RecruitPage.of(it, mainImageService.createImageURL(it.mainImage))
        }
    }
}
