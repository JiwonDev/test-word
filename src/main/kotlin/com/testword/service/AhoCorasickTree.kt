package com.testword.service

/**
 * Aho-Corasick 다중 문자열 검색 트리
 */
class AhoCorasickTree {
    private val children = mutableListOf<MutableMap<Char, Int>>(mutableMapOf())
    private lateinit var fail: IntArray
    private val output = mutableListOf<MutableList<Int>>(mutableListOf())
    private val idToPattern = mutableListOf<String>()

    /**
     * 패턴 삽입
     */
    fun insert(pattern: String) {
        val term = pattern.lowercase()
        val patId = idToPattern.size
        idToPattern += pattern

        var node = 0
        for (ch in term) {
            if (!children[node].containsKey(ch)) {
                val newNode = children.size
                children += mutableMapOf()
                output += mutableListOf<Int>()
                children[node][ch] = newNode
            }
            node = children[node][ch]!!
        }
        output[node] += patId
    }

    /**
     * 실패 링크 구축
     */
    fun build() {
        fail = IntArray(children.size) { 0 }
        val queue = ArrayDeque<Int>()

        for ((_, node) in children[0]) {
            fail[node] = 0
            queue.add(node)
        }

        while (queue.isNotEmpty()) {
            val r = queue.removeFirst()
            for ((ch, u) in children[r]) {
                var f = fail[r]
                while (f != 0 && !children[f].containsKey(ch)) {
                    f = fail[f]
                }
                fail[u] = children[f][ch] ?: 0
                output[u] += output[fail[u]]
                queue.add(u)
            }
        }
    }

    /**
     * 텍스트에서 모든 패턴 검색
     */
    fun search(text: String): Set<String> {
        var node = 0
        val found = mutableSetOf<String>()
        val seen = BooleanArray(idToPattern.size)

        for (ch in text.lowercase()) {
            while (node != 0 && !children[node].containsKey(ch)) {
                node = fail[node]
            }
            node = children[node][ch] ?: 0
            for (pid in output[node]) {
                if (!seen[pid]) {
                    seen[pid] = true
                    found += idToPattern[pid]
                }
            }
        }
        return found
    }

    fun getPatterns(): List<String> = idToPattern.toList()
    fun isEmpty(): Boolean = idToPattern.isEmpty()
}
