package dev.kensa.teamcity.agent

import dev.kensa.teamcity.model.SentenceEntry
import dev.kensa.teamcity.model.TestResultFile
import dev.kensa.teamcity.model.TestState

object FailureNarrativeBuilder {

    private const val FALLBACK = "Kensa narrative not available — see report tab"

    fun build(results: TestResultFile, testMethod: String): String {
        val test = results.tests.firstOrNull { it.testMethod == testMethod } ?: return FALLBACK
        val failed = test.invocations.firstOrNull { it.state == TestState.Failed } ?: return FALLBACK

        val sentenceLines = failed.sentences.map { renderSentence(it) }.filter { it.isNotBlank() }
        val message = failed.executionException.message ?: "(no message)"
        val stack = failed.executionException.stackTrace.trimEnd().prependIndent("    ")

        return buildString {
            sentenceLines.forEach { appendLine(it) }
            appendLine()
            appendLine(DIVIDER)
            appendLine()
            append("FAILURE: ").appendLine(message)
            append(stack)
        }
    }

    private fun renderSentence(sentence: SentenceEntry): String =
        sentence.tokens
            .filterNot { "tk-nl" in it.types }
            .joinToString(separator = " ") { it.value }
            .replace(WHITESPACE_RUN, " ")
            .trim()

    private val WHITESPACE_RUN = Regex("\\s+")
    private const val DIVIDER = "----------------------------------------"
}
