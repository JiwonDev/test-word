import java.sql.DriverManager
import java.sql.Timestamp
import java.time.Instant
import kotlin.random.Random

val jdbcUrl = "jdbc:postgresql://localhost:5432/test"
val username = "postgres"
val password = "postgres"

/**
 * 1. 금칙어 사전 생성:
 *    - 이미 존재하는 금칙어를 로드
 *    - 총 100,000개의 조합어를 생성하여 INSERT
 */
fun generateForbiddenTerms() {
    DriverManager.getConnection(jdbcUrl, username, password).use { conn ->
        conn.autoCommit = false

        // 기존에 저장된 금칙어 로드
        val existingTerms = mutableListOf<String>()
        conn.prepareStatement("SELECT term FROM forbidden_term").use { stmt ->
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    existingTerms.add(rs.getString("term"))
                }
            }
        }
        println("✅ Loaded ${existingTerms.size} existing forbidden terms.")

        // INSERT 구문 준비
        val now = Timestamp.from(Instant.now())
        val insertSQL = """
            INSERT INTO forbidden_term (
                term,
                category,
                created_by,
                created_at,
                modified_by,
                modified_at
            )
            VALUES (?, NULL, 'generator', ?, 'generator', ?)
            ON CONFLICT (term) DO NOTHING
        """.trimIndent()
        val pstmt = conn.prepareStatement(insertSQL)

        val batchSize = 500
        val totalToGenerate = 100_000
        val generatedSet = mutableSetOf<String>()

        println("🚀 Generating and inserting $totalToGenerate new forbidden terms...")

        // 2~4개 단어를 랜덤으로 조합해 중복 없이 100,000개 생성
        while (generatedSet.size < totalToGenerate) {
            val partCount = Random.nextInt(2, 5)
            val parts = List(partCount) { existingTerms.random() }
            generatedSet.add(parts.joinToString(""))
        }

        // 배치로 DB 저장
        var count = 0
        for (term in generatedSet) {
            pstmt.setString(1, term)
            pstmt.setTimestamp(2, now)
            pstmt.setTimestamp(3, now)
            pstmt.addBatch()

            count++
            if (count % batchSize == 0) {
                pstmt.executeBatch()
                conn.commit()
                println("✅ Inserted forbidden terms: $count")
            }
        }
        pstmt.executeBatch()
        conn.commit()
        println("🎉 All $count new forbidden terms inserted.")
    }
}

/**
 * 2. 일반/욕설 포함 문서 생성:
 *    - 한글+영어 문장 랜덤 생성
 *    - 50만자 이하 랜덤 길이 콘텐츠
 *    - 홀수 행은 정상, 짝수 행은 10% 확률로 금칙어 삽입
 *    - 총 100,000개 문서 INSERT
 */
fun generateDynamicArticles() {
    DriverManager.getConnection(jdbcUrl, username, password).use { conn ->
        conn.autoCommit = false

        // 금칙어 목록 로드
        val forbiddenWords = mutableListOf<String>()
        conn.prepareStatement("SELECT term FROM forbidden_term").use { stmt ->
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    forbiddenWords.add(rs.getString("term"))
                }
            }
        }
        println("🚫 Forbidden words loaded: ${forbiddenWords.size}")

        // 한글 단어 풀
        val hanNouns = listOf(
            "기자", "언론", "정부", "시민", "진실", "보도", "사건", "의혹",
            "대통령", "정책", "사회", "혼란", "검찰", "재판", "방송", "뉴스",
            "인터뷰", "언어", "표현", "팩트"
        )
        val hanVerbs = listOf(
            "보도하다", "주장하다", "비판하다", "해명하다", "보호하다",
            "강조하다", "조사하다", "요구하다", "논의하다", "신고하다",
            "분석하다", "해석하다", "공개하다", "은폐하다", "확인하다"
        )
        val hanAdjs = listOf(
            "중요한", "신속한", "정확한", "의심스러운", "의도적인",
            "불안한", "급박한", "논란이 되는", "혼란스러운", "공정한",
            "의미 있는", "비정상적인", "이례적인", "의미심장한"
        )

        // 영어 단어 풀
        val engNouns = listOf(
            "reporter", "media", "truth", "government", "citizen", "case",
            "president", "investigation", "protest", "justice", "freedom",
            "journalism", "editor", "newsroom", "policy", "source",
            "statement", "broadcast"
        )
        val engVerbs = listOf(
            "reports", "claims", "criticizes", "analyzes", "exposes",
            "covers", "questions", "protects", "verifies", "confirms",
            "denies", "publishes", "discusses", "explains", "reveals"
        )
        val engAdjs = listOf(
            "important", "urgent", "reliable", "controversial", "biased",
            "credible", "shocking", "unexpected", "critical", "in-depth",
            "false", "inaccurate", "intentional", "unusual", "alarming"
        )

        // 랜덤 문장 생성 함수
        fun randomKoreanSentence(): String =
            "${hanAdjs.random()} ${hanNouns.random()}을(를) ${hanVerbs.random()}."

        fun randomEnglishSentence(): String =
            "The ${engAdjs.random()} ${engNouns.random()} ${engVerbs.random()}."

        // 콘텐츠 생성: 길이가 maxLength가 될 때까지 한/영 문장 반복, 10% 확률로 금칙어 삽입
        fun buildDynamicContent(includeForbidden: Boolean, maxLength: Int): String {
            val sb = StringBuilder()
            while (sb.length < maxLength) {
                sb.append(randomKoreanSentence()).append(" ")
                sb.append(randomEnglishSentence()).append(" ")
                if (includeForbidden && Random.nextDouble() < 0.1) {
                    repeat(Random.nextInt(1, 5)) {
                        sb.append(forbiddenWords.random()).append(" ")
                    }
                }
            }
            return sb.substring(0, maxLength)
        }

        // INSERT 준비
        val insertSQL = """
            INSERT INTO article_content (
                title, content, author, url, source, summary,
                forbidden_term_id_counts, created_at, modified_at
            )
            VALUES (?, ?, ?, ?, ?, ?, '{}'::jsonb, ?, ?)
        """.trimIndent()
        val pstmt = conn.prepareStatement(insertSQL)

        val now = Timestamp.from(Instant.now())
        val totalArticles = 100_000
        val batchSize = 500

        println("🚀 Inserting $totalArticles dynamic articles...")

        for (i in 1..totalArticles) {
            val includeForbidden = (i % 2 == 0)
            val length = Random.nextInt(100, 30_000)
            val content = buildDynamicContent(includeForbidden, length)

            pstmt.setString(1, "Generated Title $i")
            pstmt.setString(2, content)
            pstmt.setString(3, "Author $i")
            pstmt.setString(4, "http://example.com/$i")
            pstmt.setString(5, "SyntheticSource")
            pstmt.setString(6, if (includeForbidden) "욕설 포함 테스트" else "정상 테스트")
            pstmt.setTimestamp(7, now)
            pstmt.setTimestamp(8, now)

            pstmt.addBatch()
            if (i % batchSize == 0) {
                pstmt.executeBatch()
                conn.commit()
                println("✅ Dynamic articles inserted: $i")
            }
        }
        pstmt.executeBatch()
        conn.commit()
        println("✅ All $totalArticles dynamic articles inserted successfully.")
    }
}

/**
 * 3. 특이 케이스 문서 생성:
 *    - 특수문자 반복, 짧은 반복, 긴 텍스트, 공백/탭/개행 문자 혼합, 이모지 혼합
 *    - 각 케이스별로 100,000개씩 생성하여 INSERT
 */
fun generateSpecialCaseArticles() {
    DriverManager.getConnection(jdbcUrl, username, password).use { conn ->
        conn.autoCommit = false

        val insertSQL = """
            INSERT INTO article_content (
                title, content, author, url, source, summary,
                forbidden_term_id_counts, created_at, modified_at
            )
            VALUES (?, ?, ?, ?, ?, ?, '{}'::jsonb, ?, ?)
        """.trimIndent()
        val pstmt = conn.prepareStatement(insertSQL)

        val now = Timestamp.from(Instant.now())
        val totalCases = 100_000
        val batchSize = 500

        println("🚀 Inserting $totalCases special-case articles...")

        // 특이 케이스 콘텐츠 생성 함수
        fun buildSpecialContent(caseType: Int): String {
            return when (caseType) {
                1 -> List(Random.nextInt(500, 5000)) { "!@#\$%^&*()_+-=[]{}|;:',.<>?/`~" }
                    .joinToString("")

                2 -> List(Random.nextInt(1, 10)) { "짧" }.joinToString("")
                3 -> List(30_000) { if (it % 100 == 0) "\n" else "긴" }.joinToString("")
                4 -> List(Random.nextInt(50, 30_000)) {
                    listOf(" ", "\t", "\n").random()
                }.joinToString("")

                5 -> List(Random.nextInt(1000, 30_000)) {
                    listOf("😂", "🤣", "😱", "🔥", "❤️", "👍", "💯", "🤯", "💥").random()
                }.joinToString("")

                else -> "Invalid Case"
            }
        }

        for (i in 1..totalCases) {
            val caseType = (i - 1) % 5 + 1
            val content = buildSpecialContent(caseType)

            pstmt.setString(1, "특이케이스 Title $i")
            pstmt.setString(2, content)
            pstmt.setString(3, "특이User $i")
            pstmt.setString(4, "http://weird.example.com/$i")
            pstmt.setString(5, "WeirdSource")
            pstmt.setString(6, "케이스 $caseType 테스트")
            pstmt.setTimestamp(7, now)
            pstmt.setTimestamp(8, now)

            pstmt.addBatch()
            if (i % batchSize == 0) {
                pstmt.executeBatch()
                conn.commit()
                println("✅ Special-case articles inserted: $i")
            }
        }
        pstmt.executeBatch()
        conn.commit()
        println("🎉 All $totalCases special-case articles inserted.")
    }
}

/**
 * 4. 욕설 포함 게시글 생성:
 *    - 금칙어 목록 로드
 *    - 5~100개 사이의 금칙어 랜덤 선택하여 반복 삽입
 *    - 총 100,000개 게시글 INSERT
 */
fun generateCurseArticles() {
    DriverManager.getConnection(jdbcUrl, username, password).use { conn ->
        conn.autoCommit = false

        // 금칙어 목록 불러오기
        val forbiddenTerms = mutableListOf<String>()
        conn.prepareStatement("SELECT term FROM forbidden_term").use { stmt ->
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    forbiddenTerms.add(rs.getString("term"))
                }
            }
        }
        println("✅ Loaded ${forbiddenTerms.size} forbidden terms for curse articles.")

        val now = Timestamp.from(Instant.now())
        val insertSQL = """
            INSERT INTO article_content (
                title, content, author, url, source, summary,
                forbidden_term_id_counts, created_at, modified_at
            )
            VALUES (?, ?, ?, ?, ?, ?, '{}'::jsonb, ?, ?)
        """.trimIndent()
        val pstmt = conn.prepareStatement(insertSQL)

        val batchSize = 500
        val totalCursed = 100_000
        val shuffledTerms = forbiddenTerms.shuffled()

        println("🚀 Inserting $totalCursed curse-containing articles...")

        for (i in 0 until totalCursed) {
            val title = "욕설 포함 게시글 $i"
            val author = "욕테스터 $i"
            val url = "http://example.com/curse/$i"
            val source = "욕랩소스"
            val summary = "욕설 테스트 데이터"

            val numCurseWords = Random.nextInt(5, 100)
            val curseWords = shuffledTerms.drop(i % (shuffledTerms.size - 100)).take(numCurseWords)

            val content = buildString {
                repeat(50) {
                    append("이것은 테스트 문장입니다. 욕은 ")
                    append(curseWords.random())
                    append(" 입니다.\n")
                }
                append(curseWords.joinToString(" "))
            }

            pstmt.setString(1, title)
            pstmt.setString(2, content)
            pstmt.setString(3, author)
            pstmt.setString(4, url)
            pstmt.setString(5, source)
            pstmt.setString(6, summary)
            pstmt.setTimestamp(7, now)
            pstmt.setTimestamp(8, now)

            pstmt.addBatch()
            if ((i + 1) % batchSize == 0) {
                pstmt.executeBatch()
                conn.commit()
                println("✅ Curse articles inserted: ${i + 1}")
            }
        }
        pstmt.executeBatch()
        conn.commit()
        println("🎉 All $totalCursed curse-containing articles inserted.")
    }
}

/**
 * 5. 혼합 대용량 테스트 데이터 생성:
 *    - 10% 확률로 일반 문장, 90% 확률로 금칙어 병합하여 최소 10,000개 단어 삽입
 *    - 총 500,000개 게시글 INSERT
 */
fun generateHeavyMixArticles() {
    DriverManager.getConnection(jdbcUrl, username, password).use { conn ->
        conn.autoCommit = false

        // 금칙어 목록 로드
        val forbiddenTerms = mutableListOf<String>()
        conn.prepareStatement("SELECT term FROM forbidden_term").use { stmt ->
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    forbiddenTerms.add(rs.getString("term"))
                }
            }
        }
        println("✅ Loaded ${forbiddenTerms.size} forbidden terms for heavy mix articles.")

        val now = Timestamp.from(Instant.now())
        val insertSQL = """
            INSERT INTO article_content (
                title, content, author, url, source, summary,
                forbidden_term_id_counts, created_at, modified_at
            )
            VALUES (?, ?, ?, ?, ?, ?, '{}'::jsonb, ?, ?)
        """.trimIndent()
        val pstmt = conn.prepareStatement(insertSQL)

        val batchSize = 500
        val totalHeavy = 500_000
        val shuffledTerms = forbiddenTerms.shuffled()

        println("🚀 Inserting $totalHeavy heavy mix test articles...")

        for (i in 0 until totalHeavy) {
            val title = "게시글 $i"
            val author = "작성자 $i"
            val url = "http://example.com/post/$i"
            val source = "테스트소스"
            val summary = "욕설 포함 테스트"

            val isNormal = Random.nextInt(100) < 10  // 10% 확률로 정상 글
            val content = buildString {
                if (isNormal) {
                    repeat(100) {
                        append("이건 정상적인 문장입니다. 욕설 없음.\n")
                    }
                } else {
                    // 최소 10,000단어 이상 금칙어 삽입
                    val curseMap = mutableMapOf<String, Int>()
                    while (curseMap.values.sum() < 10_000) {
                        val term = forbiddenTerms.random()
                        val count = Random.nextInt(1, 100)
                        curseMap[term] = (curseMap[term] ?: 0) + count
                    }
                    // 맵을 각 단어별 count만큼 문자열로 변환 후 셔플
                    val lines = mutableListOf<String>()
                    for ((term, count) in curseMap) {
                        repeat(count) {
                            lines.add("욕 단어 삽입: $term\n")
                        }
                    }
                    lines.shuffle()
                    lines.forEach { append(it) }
                    append("\n최종 욕설 정리:\n")
                    append(curseMap.entries.joinToString(" ") { "${it.key}(${it.value})" })
                }
            }

            pstmt.setString(1, title)
            pstmt.setString(2, content)
            pstmt.setString(3, author)
            pstmt.setString(4, url)
            pstmt.setString(5, source)
            pstmt.setString(6, summary)
            pstmt.setTimestamp(7, now)
            pstmt.setTimestamp(8, now)

            pstmt.addBatch()
            if ((i + 1) % batchSize == 0) {
                pstmt.executeBatch()
                conn.commit()
                println("✅ Heavy mix articles inserted: ${i + 1}")
            }
        }
        pstmt.executeBatch()
        conn.commit()
        println("🎉 All $totalHeavy heavy mix test articles inserted.")
    }
}

/**
 * main()에서 각 함수 호출 순서를 명시합니다.
 * 1) 금칙어 테이블 채우기
 * 2) 동적 문장 생성 테스트
 * 3) 특이 케이스 생성
 * 4) 욕설 집중 삽입 생성
 * 5) 대량 혼합 삽입 생성
 */
fun main() {
    generateForbiddenTerms()
    generateDynamicArticles()
    generateSpecialCaseArticles()
    generateCurseArticles()
    generateHeavyMixArticles()
}
