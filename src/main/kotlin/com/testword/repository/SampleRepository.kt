package com.testword.repository

import com.testword.entity.SampleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SampleRepository : JpaRepository<SampleEntity, Long> {
    fun findByName(name: String): SampleEntity?
}
