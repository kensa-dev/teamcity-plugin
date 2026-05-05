package dev.kensa.teamcity.agent

import java.io.File

class KensaArtifactPublisher(private val sink: KensaServiceMessageSink) {

    fun publish(checkoutDir: File, outputDir: File) {
        val relative = checkoutDir.toPath().relativize(outputDir.toPath()).toString().replace(File.separatorChar, '/')
        sink.publishArtifact(relative, "kensa-site")
    }
}
