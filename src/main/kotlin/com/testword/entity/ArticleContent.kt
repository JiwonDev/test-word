package com.testword.entity

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.Type

@Entity
class ArticleContent(
    val content: String,

    val title: String,

    val author: String,

    val url: String,

    val source: String,

    val summary: String,

    @Type(JsonType::class)
    var forbiddenTermIdCounts: Map<Long, Int>,
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L
}