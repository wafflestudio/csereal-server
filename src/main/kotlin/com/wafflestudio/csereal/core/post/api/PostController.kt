package com.wafflestudio.csereal.core.post.api

import com.wafflestudio.csereal.core.post.dto.CreatePostRequest
import com.wafflestudio.csereal.core.post.dto.PostDto
import com.wafflestudio.csereal.core.post.service.PostService
import org.springframework.web.bind.annotation.*

@RequestMapping
@RestController
class PostController(
    private val postService: PostService,
) {
    @GetMapping("/node/{id}")
    fun readPost(
        @PathVariable id: Long,
    ) : PostDto {
        return postService.readPost(id)
    }

    @PostMapping("/node")
    fun createPost(
        @RequestBody request: CreatePostRequest
    ) : PostDto {
        return postService.createPost(request)
    }
}