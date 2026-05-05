package dev.kensa.teamcity.model

import com.google.gson.Gson

data class TestResultFile(
    val testClass: String,
    val displayName: String,
    val state: TestState,
    val packageName: String,
    val tests: List<TestEntry>,
) {
    companion object {
        private val gson = Gson()
        fun parse(json: String): TestResultFile = gson.fromJson(json, TestResultFile::class.java)
    }
}

data class TestEntry(
    val elapsedTime: String,
    val testMethod: String,
    val displayName: String,
    val state: TestState,
    val invocations: List<InvocationEntry>,
)

data class InvocationEntry(
    val elapsedTime: String,
    val state: TestState,
    val displayName: String,
    val sentences: List<SentenceEntry>,
    val executionException: ExecutionException,
)

data class SentenceEntry(
    val lineNumber: Int,
    val tokens: List<TokenEntry>,
)

data class TokenEntry(
    val types: List<String>,
    val value: String,
)

data class ExecutionException(
    val message: String? = null,
    val stackTrace: String = "",
)
