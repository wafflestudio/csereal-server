package com.wafflestudio.csereal.common.sanitize

import com.wafflestudio.csereal.common.properties.EndpointProperties
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Base64

/**
 * 본문에 박힌 `data:image` 를 파일로 빼내고 `src` 를 그 URL 로 바꾼다.
 *
 * 한글·워드에서 이미지를 그대로 붙여넣으면 base64 로 들어간다. 운영 덤프 기준 공지 179건뿐인데
 * **본문 전체 175MB 중 95.9MB(55%)** 를 차지하고, 브라우저 이미지 캐시를 못 타 매 요청 SSR HTML 에 실린다.
 *
 * ⚠️ 세탁 **전에** 돌아야 한다 — 정책이 `data:` 를 안 받아 세탁이 `src` 를 지운다.
 */
@Component
class InlineImageExtractor(
    @Value("\${csereal.upload.path}")
    private val uploadPath: String,
    private val endpointProperties: EndpointProperties
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun extract(doc: Document) {
        for (img in doc.select("img[src^=data:]")) {
            val filename = save(img.attr("src"))
            // 못 뺐으면 요소째 지운다. 그대로 두면 세탁이 data: src 만 떼어내
            // src 없는 빈 이미지가 본문에 영구히 남는다.
            if (filename == null) img.remove() else img.attr("src", "${endpointProperties.backend}/v1/file/$filename")
        }
    }

    /** 저장에 성공하면 파일명, 못 알아볼 데이터거나 쓰기에 실패하면 null. */
    private fun save(dataUrl: String): String? {
        val bytes = decode(dataUrl) ?: return null
        // 선언된 MIME 을 믿지 않는다 — 덤프의 209개가 전부 image/png 인데 실제로는 4개가 JPEG 다.
        val extension = sniff(bytes) ?: return null

        return try {
            Files.createDirectories(Paths.get(uploadPath))
            // FileController.uploadFiles 와 같은 규칙 — 고아 파일 정리가 이 이름을 훑는다.
            val filename = "${System.currentTimeMillis()}_inline-image.$extension"
            Files.write(Paths.get(uploadPath, filename), bytes)
            filename
        } catch (e: Exception) {
            // 본문 저장 자체를 막지는 않는다 — 이미지 하나가 사라지고 로그가 남는다.
            log.warn("인라인 이미지 추출 실패", e)
            null
        }
    }

    private fun decode(dataUrl: String): ByteArray? {
        val payload = dataUrl.substringAfter("base64,", "").takeIf { it.isNotEmpty() } ?: return null
        return try {
            // 붙여넣은 값에 줄바꿈·공백이 섞여 있을 수 있다.
            Base64.getMimeDecoder().decode(payload)
        } catch (e: IllegalArgumentException) {
            log.warn("인라인 이미지 base64 디코드 실패: ${e.message}")
            null
        }
    }

    /** 파일 시작 바이트로 실제 형식을 판정한다. 아는 래스터 형식만 받는다. */
    private fun sniff(bytes: ByteArray): String? {
        fun startsWith(vararg prefix: Int) =
            bytes.size >= prefix.size && prefix.withIndex().all { (i, b) -> bytes[i] == b.toByte() }

        return when {
            startsWith(0x89, 0x50, 0x4E, 0x47) -> "png"
            startsWith(0xFF, 0xD8, 0xFF) -> "jpg"
            startsWith(0x47, 0x49, 0x46, 0x38) -> "gif"
            // RIFF....WEBP
            startsWith(0x52, 0x49, 0x46, 0x46) &&
                bytes.size >= 12 && String(bytes, 8, 4) == "WEBP" -> "webp"
            else -> null
        }
    }
}
