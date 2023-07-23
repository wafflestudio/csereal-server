package com.wafflestudio.csereal.core.post.api

import com.wafflestudio.csereal.core.post.dto.PostDto
import com.wafflestudio.csereal.core.post.service.PostService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping
@RestController
class PostController(
    private val postService: PostService,
) {
    @GetMapping("/node/{id}")
    fun getPost(
        @PathVariable id: Long,
    ) : PostDto {
        return postService.getPost(id)
    }
}