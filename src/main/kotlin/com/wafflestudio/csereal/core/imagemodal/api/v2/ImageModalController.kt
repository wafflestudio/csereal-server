package com.wafflestudio.csereal.core.imagemodal.api.v2

import com.wafflestudio.csereal.core.imagemodal.api.req.CreateImageModalReq
import com.wafflestudio.csereal.core.imagemodal.dto.ImageModalDto
import com.wafflestudio.csereal.core.imagemodal.service.ImageModalService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v2/image-modal")
@RestController
class ImageModalController(
    private val imageModalService: ImageModalService
) {
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping
    fun createImageModal(
        @RequestPart("request") request: CreateImageModalReq,
        @RequestPart("mainImage") mainImage: MultipartFile
    ): ResponseEntity<ImageModalDto> {
        return ResponseEntity.ok(imageModalService.createImageModal(request, mainImage))
    }

    @PreAuthorize("hasRole('STAFF')")
    @PatchMapping("/{imageModalId}")
    fun updateImageModal(
        @PathVariable imageModalId: Long,
        @RequestPart("request") request: CreateImageModalReq,
        @RequestPart("newMainImage") newMainImage: MultipartFile?
    ): ResponseEntity<ImageModalDto> {
        return ResponseEntity.ok(imageModalService.updateImageModal(imageModalId, request, newMainImage))
    }

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/{imageModalId}")
    fun deleteImageModal(
        @PathVariable imageModalId: Long
    ) {
        imageModalService.deleteImageModal(imageModalId)
    }

    @GetMapping
    fun readImageModals(): ResponseEntity<List<ImageModalDto>> {
        return ResponseEntity.ok(imageModalService.readImageModals())
    }
}
