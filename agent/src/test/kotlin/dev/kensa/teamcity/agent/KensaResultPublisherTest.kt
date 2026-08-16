package dev.kensa.teamcity.agent

import dev.kensa.teamcity.model.IndicesFile
import dev.kensa.teamcity.model.TestResultFile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class KensaResultPublisherTest {

    @Test
    fun `emits testStarted and testFinished for passing test`() {
        val indices = IndicesFile.parse(readFixture("indices-passing.json"))
        val resolver: ResultResolver = { _ -> error("resolver should not be called for passing tests") }
        val sink = RecordingSink()

        KensaResultPublisher(sink, resolver).publish(indices)

        val name = "com.example.FooTest.passes"
        assertEquals(
            listOf(
                "testStarted name='$name'",
                "testFinished name='$name'",
            ),
            sink.events,
        )
    }

    @Test
    fun `emits testFailed with details from FailureNarrativeBuilder for failing test`() {
        val indices = IndicesFile.parse(readFixture("indices-mixed.json"))
        val failingResults = TestResultFile.parse(readFixture("results-failing.json"))
        val resolver: ResultResolver = { className ->
            if (className == "com.example.FooTest") failingResults else null
        }
        val sink = RecordingSink()

        KensaResultPublisher(sink, resolver).publish(indices)

        val passingName = "com.example.FooTest.passes"
        val failingName = "com.example.FooTest.fails"
        assertTrue(sink.events.contains("testStarted name='$passingName'"))
        assertTrue(sink.events.contains("testFinished name='$passingName'"))
        assertTrue(sink.events.contains("testStarted name='$failingName'"))
        assertTrue(sink.events.contains("testFinished name='$failingName'"))

        val failed = sink.events.single { it.startsWith("testFailed name='$failingName'") }
        assertTrue(failed.contains("message='Kensa test failed'"))
        assertTrue(failed.contains("Given a customer with overdraft of £500"))
        assertTrue(failed.contains("FAILURE: expected: rejected but was: approved"))
    }

    @Test
    fun `passes raw narrative through without escaping (sink owns escaping for the wire)`() {
        val indices = IndicesFile.parse(readFixture("indices-mixed.json"))
        val tweaked = TestResultFile.parse(readFixture("results-failing.json")).let { rf ->
            rf.copy(tests = rf.tests.map { t ->
                t.copy(invocations = t.invocations.map { inv ->
                    inv.copy(executionException = inv.executionException.copy(
                        message = "didn't|expect 'this'\nor that"
                    ))
                })
            })
        }
        val resolver: ResultResolver = { _ -> tweaked }
        val sink = RecordingSink()

        KensaResultPublisher(sink, resolver).publish(indices)

        val failed = sink.events.single { it.startsWith("testFailed ") }
        assertTrue(failed.contains("didn't|expect 'this'\nor that"))
    }

    @Test
    fun `falls back to default details when results unavailable for failed test`() {
        val indices = IndicesFile.parse(readFixture("indices-mixed.json"))
        val resolver: ResultResolver = { _ -> null }
        val sink = RecordingSink()

        KensaResultPublisher(sink, resolver).publish(indices)

        val failed = sink.events.single { it.startsWith("testFailed ") }
        assertTrue(failed.contains("Kensa narrative not available — see report tab"))
    }

    private class RecordingSink : KensaServiceMessageSink {
        val events = mutableListOf<String>()
        override fun testStarted(name: String) {
            events += "testStarted name='$name'"
        }
        override fun testFailed(name: String, message: String, details: String) {
            events += "testFailed name='$name' message='$message' details='$details'"
        }
        override fun testFinished(name: String) {
            events += "testFinished name='$name'"
        }
        override fun publishArtifact(sourceRelativeToCheckout: String, destination: String) {
            events += "publishArtifact source='$sourceRelativeToCheckout' destination='$destination'"
        }
    }

    private fun readFixture(name: String): String =
        KensaResultPublisherTest::class.java.getResourceAsStream("/fixtures/$name")!!
            .bufferedReader().readText()
}
