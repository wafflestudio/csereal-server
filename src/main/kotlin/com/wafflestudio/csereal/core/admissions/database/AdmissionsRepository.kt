package com.wafflestudio.csereal.core.admissions.database

import org.springframework.data.jpa.repository.JpaRepository

interface AdmissionsRepository : JpaRepository<AdmissionsEntity, Long> {
    fun findByStudentTypeAndPostType(studentType: StudentType, postType: AdmissionPostType): AdmissionsEntity
    fun findByPostType(postType: AdmissionPostType) : AdmissionsEntity
}