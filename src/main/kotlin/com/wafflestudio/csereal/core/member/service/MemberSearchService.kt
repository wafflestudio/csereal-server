package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.core.member.database.MemberSearchRepository
import com.wafflestudio.csereal.core.member.database.ProfessorEntity
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

interface MemberSearchService {

}

@Service
class MemberSearchServiceImpl (
        private val memberSearchRepository: MemberSearchRepository,
): MemberSearchService {
}