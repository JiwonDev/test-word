package com.testword.service.forbidden_term

import com.testword.entity.ArticleContent
import com.testword.entity.ForbiddenTerm
import com.testword.repository.ArticleContentRepository
import com.testword.repository.ForbiddenTermRepository
import com.testword.service.ForbiddenTermChecker
import com.testword.service.dto.ForbiddenTermCheckResult
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.data.repository.findByIdOrNull
import java.util.*

class ForbiddenTermServiceTreeImpl(
    private val repo: ForbiddenTermRepository,
    private val checker: ForbiddenTermChecker,
    private val articleContentRepository: ArticleContentRepository,
) : ForbiddenTermService {

    private val log = KotlinLogging.logger {}

    private fun reloadForbiddenTerms() {
        val terms = repo.findAllTerms()
            .map { it.lowercase(Locale.ROOT) }
            .toSet()

        checker.reloadTerms(terms)
    }

    @PostConstruct
    override fun reload() {
        reloadForbiddenTerms()
    }

    override fun registerForbiddenTerms(rawTokens: List<String>) {
        val inputTokens = rawTokens
            .map { it.lowercase(Locale.ROOT) }
            .filter { it.isNotBlank() }
            .toSet()

        if (inputTokens.isEmpty()) return

        val existing = repo.findAllTerms()
            .map { it.lowercase(Locale.ROOT) }
            .toSet()

        val needInsert = inputTokens - existing
        if (needInsert.isEmpty()) return

        repo.saveAll(needInsert.map { ForbiddenTerm(term = it) })
        reloadForbiddenTerms()
    }

    /**
     * 텍스트에서 금지어 검사 및 출현 횟수 기록
     */
    override fun checkForbiddenTerms(contentId: Long, earlyReturn: Boolean): ForbiddenTermCheckResult {
        val article: ArticleContent = articleContentRepository.findByIdOrNull(contentId)
            ?: throw IllegalArgumentException("Article with ID $contentId not found")

        // 금지어 출현 횟수 계산
        val termCounts: Map<String, Int> = checker.findForbiddenTermWithCounts(article.content)

        // 금지어 term → ID 매핑
        val termToIdMap: Map<String, Long> = repo.findAll()
            .associateBy({ it.term.lowercase() }, { it.id })

        // termId → count 매핑 구성
        val termIdCounts: Map<Long, Int> = termCounts.mapNotNull { (term, count) ->
            termToIdMap[term]?.let { id -> id to count }
        }.toMap()

        // ArticleContent 에 저장
        article.forbiddenTermIdCounts = termIdCounts
        articleContentRepository.save(article)

        return ForbiddenTermCheckResult(
            hasForbiddenTerm = termIdCounts.isNotEmpty(),
            matchedTerms = termIdCounts.keys.mapNotNull { id ->
                termToIdMap.entries.find { it.value == id }?.key
            },
            termCounts = termCounts
        )
    }
}
