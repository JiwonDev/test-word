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
    fun findMatchingTerms(lowered: String, earlyReturn: Boolean): List<String>
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

    private val qForbiddenTerm = QForbiddenTerm.forbiddenTerm

    override fun findMatchingTerms(content: String, earlyReturn: Boolean): List<String> {
        val query = jpaQueryFactory
            .select(qForbiddenTerm.term)
            .from(qForbiddenTerm)
            .where(qForbiddenTerm.term.lower().like("%${content}%"))

        if (earlyReturn) {
            query.limit(1)
        }

        return query.fetch()
    }
}