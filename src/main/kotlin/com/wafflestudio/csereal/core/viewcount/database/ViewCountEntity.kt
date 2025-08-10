package com.wafflestudio.csereal.core.viewcount.database

import com.wafflestudio.csereal.common.domain.Domain
import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity(name="view_count")
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name="UK_view_count_domain_domain_id",
            columnNames = ["domain", "domain_id"]
        )
    ]
)
class ViewCountEntity (
    @Enumerated(value= EnumType.STRING)
    var domain: Domain,
    var domainId: Long,
    var count: Long = 0L,
): BaseTimeEntity() {
}
