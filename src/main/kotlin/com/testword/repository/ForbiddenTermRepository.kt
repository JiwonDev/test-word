package com.testword.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.testword.entity.ForbiddenTerm
import com.testword.entity.QForbiddenTerm
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ForbiddenTermRepository : JpaRepository<ForbiddenTerm, Long>, CustomForbiddenTermRepository {
}

interface CustomForbiddenTermRepository {
    fun findAllTerms(): List<String>
}

class CustomForbiddenTermRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : CustomForbiddenTermRepository {

    override fun findAllTerms(): List<String> {
        val qForbiddenTerm = QForbiddenTerm.forbiddenTerm

        return jpaQueryFactory
            .select(qForbiddenTerm.term)
            .from(qForbiddenTerm)
            .fetch()
    }
}