package com.wafflestudio.csereal.core.viewcount.database

import com.wafflestudio.csereal.common.domain.Domain
import com.wafflestudio.csereal.common.dto.ClientInfo
import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import jakarta.persistence.*

@Entity(name = "view_log")
@Table(
    indexes = [
        Index(
            name = "IDX_view_log_domain_domain_id_created_at",
            columnList = "domain, domain_id, created_at"
        )
    ]
)
class ViewLogEntity(
    @Enumerated(value = EnumType.STRING)
    var domain: Domain,
    var domainId: Long,
    @Convert
    var clientInfo: ClientInfo,
) : BaseTimeEntity()
