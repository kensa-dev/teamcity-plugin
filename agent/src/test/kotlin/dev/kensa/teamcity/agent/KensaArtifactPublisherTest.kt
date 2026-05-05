package dev.kensa.teamcity.agent

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class KensaArtifactPublisherTest {

    @Test
    fun `publishes output dir relative to checkout, mapped to kensa-site`() {
        val checkoutDir = File("/tmp/checkout")
        val outputDir = File("/tmp/checkout/build/kensa-site")
        val sink = RecordingSink()

        KensaArtifactPublisher(sink).publish(checkoutDir, outputDir)

        assertEquals(listOf("publishArtifact source='build/kensa-site' destination='kensa-site'"), sink.events)
    }

    private class RecordingSink : KensaServiceMessageSink {
        val events = mutableListOf<String>()
        override fun testStarted(name: String) = error("not used")
        override fun testFailed(name: String, message: String, details: String) = error("not used")
        override fun testFinished(name: String) = error("not used")
        override fun publishArtifact(sourceRelativeToCheckout: String, destination: String) {
            events += "publishArtifact source='$sourceRelativeToCheckout' destination='$destination'"
        }
    }
}
