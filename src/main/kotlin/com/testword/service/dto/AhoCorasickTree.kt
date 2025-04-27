package com.testword.service.dto

import java.util.*

/**
 * Aho-Corasick 다중 문자열 검색 트리
 */
class AhoCorasickTree {
    companion object {
        private const val ALPHABET_SIZE = 26
        private const val BASE_CHAR = 'a'
    }

    private val children = mutableListOf(IntArray(ALPHABET_SIZE) { -1 })
    private lateinit var fail: IntArray
    private val output = mutableListOf<MutableList<Int>>(mutableListOf())
    private val idToPattern = mutableListOf<String>()

    /**
     * 패턴 삽입
     */
    fun insert(pattern: String) {
        val term = pattern.lowercase(Locale.getDefault())
        val patId = idToPattern.size
        idToPattern += pattern

        var node = 0
        for (ch in term) {
            val idx = ch - BASE_CHAR
            if (idx !in 0 until ALPHABET_SIZE) {
                node = 0
                continue
            }
            if (children[node][idx] == -1) {
                children[node][idx] = children.size
                children += IntArray(ALPHABET_SIZE) { -1 }
                output += mutableListOf<Int>()
            }
            node = children[node][idx]
        }
        output[node] += patId
    }

    /**
     * 실패 링크 구축
     */
    fun build() {
        fail = IntArray(children.size)
        val queue: Queue<Int> = ArrayDeque()

        // 루트 초기화: 직접 자식과 fallback 설정
        children[0].indices.forEach { c ->
            val nxt = children[0][c]
            if (nxt != -1) {
                fail[nxt] = 0
                queue.add(nxt)
            } else {
                children[0][c] = 0
            }
        }

        // BFS를 통한 링크 연결
        while (queue.isNotEmpty()) {
            val r = queue.remove()
            for (c in 0 until ALPHABET_SIZE) {
                val child = children[r][c]
                if (child != -1) {
                    fail[child] = children[fail[r]][c]
                    output[child] += output[fail[child]]
                    queue.add(child)
                } else {
                    children[r][c] = children[fail[r]][c]
                }
            }
        }
    }

    /**
     * 텍스트에서 모든 패턴 검색
     */
    fun search(text: String): Set<String> {
        var node = 0
        val found = mutableSetOf<String>()
        for (ch in text.lowercase(Locale.getDefault())) {
            val idx = ch - BASE_CHAR
            node = if (idx in 0 until ALPHABET_SIZE) children[node][idx] else 0
            output[node].forEach { pid -> found += idToPattern[pid] }
        }
        return found
    }

    fun getPatterns(): List<String> = idToPattern.toList()
    fun isEmpty(): Boolean = idToPattern.isEmpty()
}
