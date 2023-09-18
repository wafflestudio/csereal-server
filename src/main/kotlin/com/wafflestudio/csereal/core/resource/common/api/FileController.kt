package com.wafflestudio.csereal.core.resource.common.api

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.common.properties.EndpointProperties
import com.wafflestudio.csereal.core.resource.common.dto.FileUploadResponse
import com.wafflestudio.csereal.core.resource.common.dto.UploadFileInfo
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths


@RequestMapping("/api/v1/file")
@RestController
class FileController(
    @Value("\${csereal.upload.path}")
    private val uploadPath: String,
    private val endpointProperties: EndpointProperties
) {

    @GetMapping("/{filename:.+}")
    fun serveFile(
        @PathVariable filename: String,
        request: HttpServletRequest
    ): ResponseEntity<Resource> {
        val file = Paths.get(uploadPath, filename)
        val resource = UrlResource(file.toUri())

        if (resource.exists() || resource.isReadable) {
            val contentType: String? = request.servletContext.getMimeType(resource.file.absolutePath)
            val headers = HttpHeaders()

            headers.contentType =
                org.springframework.http.MediaType.parseMediaType(contentType ?: "application/octet-stream")

            return ResponseEntity.ok()
                .headers(headers)
                .body(resource)
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    @PostMapping("/upload")
    fun uploadFiles(@RequestParam files: Map<String, MultipartFile>): ResponseEntity<FileUploadResponse> {
        return try {
            Files.createDirectories(Paths.get(uploadPath))

            val results = mutableListOf<UploadFileInfo>()

            for ((_, file) in files) {
                val timeMillis = System.currentTimeMillis()

                val filename = "${timeMillis}_${file.originalFilename}"
                val totalFilename = uploadPath + filename
                val saveFile = Paths.get(totalFilename)
                file.transferTo(saveFile)

                val imageUrl = "${endpointProperties.backend}/v1/file/${filename}"

                results.add(
                    UploadFileInfo(
                        url = imageUrl,
                        name = file.originalFilename ?: "unknown",
                        size = file.size
                    )
                )
            }

            ResponseEntity(
                FileUploadResponse(result = results),
                HttpStatus.OK
            )
        } catch (e: Exception) {
            ResponseEntity(
                FileUploadResponse(errorMessage = "An error occurred while uploading the images"),
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @AuthenticatedStaff
    @DeleteMapping("/{filename:.+}")
    fun deleteFile(@PathVariable filename: String): ResponseEntity<Any> {
        val file = Paths.get(uploadPath, filename)

        if (Files.exists(file)) {
            Files.delete(file)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("파일을 찾을 수 없습니다.")
        }
    }

}
