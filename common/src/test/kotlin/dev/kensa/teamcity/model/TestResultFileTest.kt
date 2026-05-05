package dev.kensa.teamcity.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TestResultFileTest {

    @Test
    fun `parses passing results fixture`() {
        val parsed = TestResultFile.parse(readFixture("results-passing.json"))

        assertEquals("com.example.FooTest", parsed.testClass)
        assertEquals(TestState.Passed, parsed.state)
        assertEquals(1, parsed.tests.size)
        val test = parsed.tests[0]
        assertEquals("passes", test.testMethod)
        assertEquals(TestState.Passed, test.state)
        val inv = test.invocations[0]
        assertNull(inv.executionException.message)
    }

    @Test
    fun `parses failing results fixture with exception and tokenised sentences`() {
        val parsed = TestResultFile.parse(readFixture("results-failing.json"))

        val test = parsed.tests.single { it.testMethod == "fails" }
        val inv = test.invocations.single()
        assertEquals(TestState.Failed, inv.state)
        assertEquals("expected: rejected but was: approved", inv.executionException.message)
        assertTrue(inv.executionException.stackTrace.contains("at com.example.FooTest.fails(FooTest.kt:12)"))
        assertTrue(inv.executionException.stackTrace.contains("at jdk.internal.reflect.GeneratedMethodAccessor.invoke(...)"))
        assertEquals(3, inv.sentences.size)

        val firstTokens = inv.sentences[0].tokens
        assertEquals(listOf("tk-kw"), firstTokens[0].types)
        assertEquals("Given", firstTokens[0].value)
        assertEquals(listOf("tk-fx"), firstTokens[2].types)
        assertEquals("£500", firstTokens[2].value)
    }

    private fun readFixture(name: String): String =
        javaClass.getResourceAsStream("/fixtures/$name")!!.bufferedReader().readText()
}
