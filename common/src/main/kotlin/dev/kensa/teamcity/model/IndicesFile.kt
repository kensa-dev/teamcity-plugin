package dev.kensa.teamcity.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

enum class TestState {
    @SerializedName("Passed") Passed,
    @SerializedName("Failed") Failed,
}

data class IndicesFile(val indices: List<IndexNode>) {
    companion object {
        private val gson = Gson()
        fun parse(json: String): IndicesFile = gson.fromJson(json, IndicesFile::class.java)
    }
}

data class IndexNode(
    val id: String,
    val testClass: String? = null,
    val testMethod: String? = null,
    val displayName: String,
    val state: TestState,
    val children: List<IndexNode> = emptyList(),
)
