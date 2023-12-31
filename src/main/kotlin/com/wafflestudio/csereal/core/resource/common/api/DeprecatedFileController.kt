package com.wafflestudio.csereal.core.resource.common.api

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URLEncoder
import java.nio.file.Paths
import kotlin.text.Charsets.UTF_8

@RestController
@RequestMapping("/sites/default/files")
class DeprecatedFileController(
    @Value("\${oldFiles.path}")
    private val oldFilesPath: String
) {
    @GetMapping("/{map}/**")
    fun serveOldFile(
        @PathVariable map: String, // Just for ensure at least one path variable
        request: HttpServletRequest
    ): ResponseEntity<Resource> {
        // Extract path from pattern
        val fileSubDir = AntPathMatcher().extractPathWithinPattern(
            "/sites/default/files/**",
            request.servletPath
        ).substringAfter("/sites/default/files/")
        val file = Paths.get(oldFilesPath, fileSubDir)
        val resource = UrlResource(file.toUri())

        return if (resource.exists() || resource.isReadable) {
            val contentType: String? = request.servletContext.getMimeType(resource.file.absolutePath)
            val headers = HttpHeaders()

            headers.contentType = MediaType.parseMediaType(contentType ?: "application/octet-stream")

            val originalFilename = fileSubDir.substringAfterLast("/")
            val encodedFilename = URLEncoder.encode(originalFilename, UTF_8.toString()).replace("+", "%20")

            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''$encodedFilename")

            ResponseEntity.ok()
                .headers(headers)
                .body(resource)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }
}
