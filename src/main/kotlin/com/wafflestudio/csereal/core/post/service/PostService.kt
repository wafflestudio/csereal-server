package com.wafflestudio.csereal.core.post.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.post.database.PostEntity
import com.wafflestudio.csereal.core.post.database.PostRepository
import com.wafflestudio.csereal.core.post.dto.CreatePostRequest
import com.wafflestudio.csereal.core.post.dto.PostDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface PostService {
    fun readPost(postId: Long): PostDto
    fun createPost(request: CreatePostRequest): PostDto
}

@Service
class PostServiceImpl(
    private val postRepository: PostRepository,
) : PostService {

    @Transactional
    override fun readPost(postId: Long): PostDto {
        val post: Optional<PostEntity> = postRepository.findById(postId)
        if (post.isEmpty) throw CserealException.Csereal400("존재하지 않는 질문 번호 입니다.(postId: $postId)")
        return PostDto.of(post.get())
    }

    // 아직 날짜가 제대로 안 뜨는 문제점
    @Transactional
    override fun createPost(request: CreatePostRequest): PostDto {
        val newPost = PostEntity(
            title = request.title,
            description = request.description,
        )

        postRepository.save(newPost)

        return PostDto.of(newPost)
    }
}