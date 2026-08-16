package dev.kensa.teamcity.agent

import dev.kensa.teamcity.model.IndexNode
import dev.kensa.teamcity.model.IndicesFile
import dev.kensa.teamcity.model.TestResultFile
import dev.kensa.teamcity.model.TestState

typealias ResultResolver = (testClassName: String) -> TestResultFile?

class KensaResultPublisher(
    private val sink: KensaServiceMessageSink,
    private val resolveResults: ResultResolver,
) {

    fun publish(indices: IndicesFile) {
        for (classNode in indices.indices) {
            val className = classNode.testClass ?: continue
            for (methodNode in classNode.children) {
                publishMethod(className, methodNode)
            }
        }
    }

    private fun publishMethod(className: String, method: IndexNode) {
        val testMethod = method.testMethod ?: return
        // Must match the name the test runner already reported under, so TeamCity
        // merges this with the runner's result instead of counting a second test.
        val tcName = "$className.$testMethod"

        sink.testStarted(tcName)

        if (method.state == TestState.Failed) {
            val results = resolveResults(className)
            val details = if (results != null) {
                FailureNarrativeBuilder.build(results, testMethod)
            } else {
                "Kensa narrative not available — see report tab"
            }
            sink.testFailed(tcName, "Kensa test failed", details)
        }

        sink.testFinished(tcName)
    }
}
