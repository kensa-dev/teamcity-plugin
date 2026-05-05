package dev.kensa.teamcity

import java.io.File

object KensaPaths {

    private val CANDIDATE_NAMES = listOf("kensa-site", "kensa-output")
    private const val MAX_WALK_DEPTH = 6

    fun resolve(checkoutDir: File, explicitPath: String?): File? {
        if (!explicitPath.isNullOrBlank()) {
            val explicit = File(checkoutDir, explicitPath)
            return explicit.takeIf { it.isDirectory }
        }

        for (name in CANDIDATE_NAMES) {
            val direct = File(checkoutDir, "build/$name")
            if (direct.isDirectory) return direct
        }

        return walkForOutputDir(checkoutDir)
    }

    private fun walkForOutputDir(root: File): File? =
        root.walkTopDown()
            .maxDepth(MAX_WALK_DEPTH)
            .filter { it.isDirectory && it.name in CANDIDATE_NAMES && it.parentFile?.name == "build" }
            .firstOrNull()
}
