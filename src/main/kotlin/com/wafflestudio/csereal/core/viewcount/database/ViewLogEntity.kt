package com.wafflestudio.csereal.core.viewcount.database

import com.wafflestudio.csereal.common.domain.Domain
import com.wafflestudio.csereal.common.dto.ClientInfo
import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import jakarta.persistence.*

@Entity(name = "view_log")
class ViewLogEntity(
    @Enumerated(value = EnumType.STRING)
    var domain: Domain,
    var domainId: Long,
    var clientInfo: ClientInfo,
) : BaseTimeEntity()
