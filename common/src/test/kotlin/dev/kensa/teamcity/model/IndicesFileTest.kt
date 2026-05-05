package dev.kensa.teamcity.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IndicesFileTest {

    @Test
    fun `parses indices fixture into class and method nodes`() {
        val json = readFixture("indices-passing.json")

        val parsed = IndicesFile.parse(json)

        assertEquals(1, parsed.indices.size)
        val classNode = parsed.indices[0]
        assertEquals("com.example.FooTest", classNode.testClass)
        assertEquals(TestState.Passed, classNode.state)
        assertEquals(2, classNode.children.size)

        val passing = classNode.children[0]
        assertEquals("passes", passing.testMethod)
        assertEquals("the happy path passes", passing.displayName)
        assertEquals(TestState.Passed, passing.state)

        val failing = classNode.children[1]
        assertEquals("fails", failing.testMethod)
        assertEquals("the unhappy path fails", failing.displayName)
        assertEquals(TestState.Failed, failing.state)
    }

    private fun readFixture(name: String): String =
        javaClass.getResourceAsStream("/fixtures/$name")!!.bufferedReader().readText()
}
