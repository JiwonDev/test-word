package com.testword.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class SampleEntity(
    val name: String? = null,
    val description: String? = null,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    override fun toString(): String {
        return "${this::class.simpleName}( " +
                "id=$id, " +
                "name=$name, " +
                "description=$description " +
                ")"
    }
}
