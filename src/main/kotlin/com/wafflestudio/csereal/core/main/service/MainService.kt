package com.wafflestudio.csereal.core.main.service

import com.wafflestudio.csereal.core.main.database.CustomMainRepository
import com.wafflestudio.csereal.core.main.database.MainRepository
import com.wafflestudio.csereal.core.main.dto.MainResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface MainService {
    fun readMain() : MainResponse
}

@Service
class MainServiceImpl(
    private val mainRepository: CustomMainRepository
) : MainService {
    @Transactional
    override fun readMain(): MainResponse {
        return mainRepository.readMain()
    }
}