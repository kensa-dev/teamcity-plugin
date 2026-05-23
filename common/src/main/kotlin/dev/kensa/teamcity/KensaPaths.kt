package dev.kensa.teamcity

import java.io.File

object KensaPaths {

    const val DEFAULT_SITE_PATH = "build/kensa-site"
    const val DEFAULT_OUTPUT_PATH = "build/kensa-output"

    private const val SITE_MARKER = "manifest.json"
    private const val NON_SITE_MARKER = "indices.json"
    private const val MAX_WALK_DEPTH = 6
    private val NOISE_DIRS = setOf(".git", ".gradle", ".idea", "node_modules", "out")

    sealed interface ResolutionEvent {
        data class WalkResolved(val resolvedDir: File) : ResolutionEvent
        data class MultipleSiteRoots(val candidates: List<File>, val picked: File) : ResolutionEvent
        data class MultipleNonSiteOutputs(val candidates: List<File>, val picked: File) : ResolutionEvent
    }

    fun resolve(
        checkoutDir: File,
        explicitPath: String?,
        onEvent: (ResolutionEvent) -> Unit = {},
    ): File? {
        if (!explicitPath.isNullOrBlank()) {
            val explicit = File(checkoutDir, explicitPath)
            return explicit.takeIf { it.isDirectory }
        }

        val sites = mutableListOf<File>()
        val nonSites = mutableListOf<File>()
        val defaults = listOf(File(checkoutDir, DEFAULT_SITE_PATH), File(checkoutDir, DEFAULT_OUTPUT_PATH))

        // Both default paths can hold either marker. The marker, not the directory name, determines mode.
        for (dir in defaults) {
            when {
                File(dir, SITE_MARKER).isFile -> sites += dir
                File(dir, NON_SITE_MARKER).isFile -> nonSites += dir
            }
        }

        var walkUsed = false
        checkoutDir.walkTopDown()
            .onEnter { it.name !in NOISE_DIRS }
            .maxDepth(MAX_WALK_DEPTH)
            .filter { it.isDirectory && it !in defaults }
            .forEach { dir ->
                if (File(dir, SITE_MARKER).isFile) { sites += dir; walkUsed = true }
                else if (File(dir, NON_SITE_MARKER).isFile) { nonSites += dir; walkUsed = true }
            }

        if (sites.isNotEmpty()) {
            val picked = sites.maxByOrNull { File(it, SITE_MARKER).lastModified() }!!
            if (sites.size > 1) onEvent(ResolutionEvent.MultipleSiteRoots(sites.toList(), picked))
            else if (walkUsed && picked !in defaults) onEvent(ResolutionEvent.WalkResolved(picked))
            return picked
        }

        if (nonSites.isNotEmpty()) {
            val picked = nonSites.first()
            if (nonSites.size > 1) onEvent(ResolutionEvent.MultipleNonSiteOutputs(nonSites.toList(), picked))
            else if (walkUsed && picked !in defaults) onEvent(ResolutionEvent.WalkResolved(picked))
            return picked
        }

        return null
    }
}
