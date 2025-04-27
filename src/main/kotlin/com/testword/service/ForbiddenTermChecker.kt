package com.testword.service

import com.testword.service.dto.AhoCorasickTree
import org.springframework.stereotype.Component
import java.util.*

@Component
class ForbiddenTermChecker {

    companion object {
        private val CLEAN_TEXT_REGEX = Regex("[^\\p{L}\\s]+")
    }

    private var tree = AhoCorasickTree()

    fun getAllTerms(): Set<String> = tree.getPatterns().toSet()

    fun isTermListEmpty(): Boolean = tree.isEmpty()

    fun reloadTerms(terms: Collection<String>) {
        tree = AhoCorasickTree().apply {
            terms.forEach { insert(it) }
            build()
        }
    }

    /**
     * 금지어 검색
     * @param content     입력 텍스트
     * @param earlyReturn 첫 매칭만 필요 시 true
     */
    fun findForbiddenTerms(
        content: CharSequence,
        earlyReturn: Boolean = false,
    ): List<String> {
        if (isTermListEmpty()) throw IllegalStateException("ForbiddenTermTree is empty – reload first.")

        val cleaned = CLEAN_TEXT_REGEX
            .replace(content, "")
            .lowercase(Locale.getDefault())

        val found = tree.search(cleaned)
        return if (earlyReturn) found.firstOrNull()?.let { listOf(it) } ?: emptyList()
        else found.toList()
    }
}
