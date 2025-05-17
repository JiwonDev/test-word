//import com.testword.TestWordApplication
//import com.testword.service.forbidden_term.ForbiddenTermService
//import io.kotest.assertions.assertSoftly
//import io.kotest.matchers.booleans.shouldBeFalse
//import io.kotest.matchers.booleans.shouldBeTrue
//import io.kotest.matchers.collections.shouldBeEmpty
//import io.kotest.matchers.collections.shouldNotBeEmpty
//import org.junit.jupiter.api.DisplayName
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.test.context.ActiveProfiles
//
//@SpringBootTest(classes = [TestWordApplication::class])
//@ActiveProfiles("local")
//class ForbiddenTermServiceTest {
//
//    @Autowired
//    private lateinit var service: ForbiddenTermService
//
//    @Test
//    @DisplayName("금칙어가 없을 경우 false")
//    fun checkForbiddenTerms_noMatch() {
//        val result = service.checkForbiddenTerms("This is a clean sentence.")
//
//        assertSoftly {
//            result.hasForbiddenTerm.shouldBeFalse()
//            result.matchedTerms.shouldBeEmpty()
//        }
//    }
//
//    @Test
//    @DisplayName("금칙어가 있을 경우 true")
//    fun checkForbiddenTerms_withMatch() {
//        val result = service.checkForbiddenTerms("You are such a fuck.")
//
//        assertSoftly {
//            result.hasForbiddenTerm.shouldBeTrue()
//            result.matchedTerms.shouldNotBeEmpty()
//        }
//    }
//
//}
