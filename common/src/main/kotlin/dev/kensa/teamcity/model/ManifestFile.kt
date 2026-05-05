package dev.kensa.teamcity.model

import com.google.gson.Gson

data class ManifestFile(
    val schemaVersion: Int,
    val kensaVersion: String,
    val sources: List<ManifestSource>,
) {
    companion object {
        private val gson = Gson()
        fun parse(json: String): ManifestFile = gson.fromJson(json, ManifestFile::class.java)
    }
}

data class ManifestSource(val id: String, val title: String, val url: String)
