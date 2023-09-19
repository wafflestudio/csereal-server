package com.wafflestudio.csereal.core.conference.database

import org.springframework.data.jpa.repository.JpaRepository

interface ConferenceRepository : JpaRepository<ConferenceEntity, Long>
