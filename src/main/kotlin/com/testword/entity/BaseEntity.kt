package com.testword.entity

import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant


@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity {
    @CreatedBy
    var createdBy: String? = "anonymous"

    @CreatedDate
    var createdAt: Instant = Instant.now()

    @LastModifiedBy
    var modifiedBy: String? = "anonymous"

    @LastModifiedDate
    var modifiedAt: Instant = Instant.now()
}