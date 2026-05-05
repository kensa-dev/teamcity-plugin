package dev.kensa.teamcity.agent

import dev.kensa.teamcity.model.TestResultFile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FailureNarrativeBuilderTest {

    @Test
    fun `builds narrative from failed invocation with sentences and exception`() {
        val results = TestResultFile.parse(readFixture("results-failing.json"))

        val narrative = FailureNarrativeBuilder.build(results, testMethod = "fails")

        val expected = """
            Given a customer with overdraft of £500
            When they request a withdrawal of £600
            Then withdrawal is rejected

            ----------------------------------------

            FAILURE: expected: rejected but was: approved
                at com.example.FooTest.fails(FooTest.kt:12)
                at jdk.internal.reflect.GeneratedMethodAccessor.invoke(...)
        """.trimIndent()

        assertEquals(expected, narrative)
    }

    @Test
    fun `returns fallback when test method not found`() {
        val results = TestResultFile.parse(readFixture("results-failing.json"))

        val narrative = FailureNarrativeBuilder.build(results, testMethod = "missingMethod")

        assertEquals("Kensa narrative not available — see report tab", narrative)
    }

    @Test
    fun `returns fallback when no failed invocation found`() {
        val results = TestResultFile.parse(readFixture("results-passing.json"))

        val narrative = FailureNarrativeBuilder.build(results, testMethod = "passes")

        assertEquals("Kensa narrative not available — see report tab", narrative)
    }

    private fun readFixture(name: String): String =
        FailureNarrativeBuilderTest::class.java.getResourceAsStream("/fixtures/$name")!!
            .bufferedReader().readText()
}
