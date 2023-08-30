package com.wafflestudio.csereal.core.resource.mainImage.api

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.text.Charsets.UTF_8


@RequestMapping("/file")
@RestController
class FileController(
    @Value("\${csereal.upload.path}")
    private val uploadPath: String
) {

    @GetMapping("/{filename:.+}")
    fun serveFile(
        @PathVariable filename: String,
        @RequestParam(defaultValue = "false") download: Boolean,
        request: HttpServletRequest
    ): ResponseEntity<Resource> {
        val file = Paths.get(uploadPath, filename)
        val resource = UrlResource(file.toUri())

        if (resource.exists() || resource.isReadable) {
            val contentType: String? = request.servletContext.getMimeType(resource.file.absolutePath)
            val headers = HttpHeaders()

            headers.contentType =
                org.springframework.http.MediaType.parseMediaType(contentType ?: "application/octet-stream")

            if (download) {
                val originalFilename = filename.substringAfter("_")

                val encodedFilename = URLEncoder.encode(originalFilename, UTF_8.toString()).replace("+", "%20")

                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''$encodedFilename")
            }

            return ResponseEntity.ok()
                .headers(headers)
                .body(resource)
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

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
