package exercises.ex04

import exercises.SpringExerciseSpec
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.get

/**
 * @TestPropertySource overrides config for this test only — it beats application.yml.
 * free-above-cents becomes 5000 (yml says 3000), and the shop gets a different name.
 */
@TestPropertySource(properties = ["exercises.shipping.free-above-cents=5000", "bookstore.name=Test Shop"])
abstract class Ex04Spec(base: String) : SpringExerciseSpec(base) {
    @Test
    fun `04_1 shipping uses configured values`() {
        mockMvc.get("$base/shipping?orderTotalCents=4000").andExpect {
            status { isOk() }
            jsonPath("$.shippingCents") { value(499) }      // 4000 < 5000 → flat fee from yml
            jsonPath("$.freeAboveCents") { value(5000) }    // overridden value
        }
        mockMvc.get("$base/shipping?orderTotalCents=5000").andExpect {
            jsonPath("$.shippingCents") { value(0) }
        }
    }

    @Test
    fun `04_2 greeting reads bookstore name`() {
        mockMvc.get("$base/greeting").andExpect {
            status { isOk() }
            jsonPath("$.text") { value("Welcome to Test Shop") }
        }
    }
}

class Ex04SolutionTest : Ex04Spec("/solutions/ex04")

@Tag("exercise")
class Ex04ExerciseTest : Ex04Spec("/exercises/ex04")
