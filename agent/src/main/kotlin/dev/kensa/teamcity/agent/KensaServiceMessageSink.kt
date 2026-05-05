package dev.kensa.teamcity.agent

interface KensaServiceMessageSink {
    fun testStarted(name: String)
    fun testFailed(name: String, message: String, details: String)
    fun testFinished(name: String)
    fun publishArtifact(sourceRelativeToCheckout: String, destination: String)
}
