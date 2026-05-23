package dev.kensa.teamcity

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class KensaPathsTest {

    @Test
    fun `returns explicit override when configured and exists`(@TempDir checkoutDir: Path) {
        val explicit = checkoutDir.resolve("custom/kensa").toFile().also { it.mkdirs() }

        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = "custom/kensa")

        assertEquals(explicit.canonicalFile, resolved!!.canonicalFile)
    }

    @Test
    fun `returns null when explicit override does not exist`(@TempDir checkoutDir: Path) {
        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = "missing/dir")
        assertNull(resolved)
    }

    @Test
    fun `prefers kensa-site over kensa-output when both present`(@TempDir checkoutDir: Path) {
        val site = checkoutDir.resolve("build/kensa-site").toFile().apply { mkdirs() }
        File(site, "manifest.json").writeText("{}")
        val output = checkoutDir.resolve("build/kensa-output").toFile().apply { mkdirs() }
        File(output, "indices.json").writeText("[]")

        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null)

        assertEquals(site.canonicalFile, resolved!!.canonicalFile)
    }

    @Test
    fun `falls back to kensa-output when kensa-site missing`(@TempDir checkoutDir: Path) {
        val output = checkoutDir.resolve("build/kensa-output").toFile().apply { mkdirs() }
        File(output, "indices.json").writeText("[]")

        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null)

        assertEquals(output.canonicalFile, resolved!!.canonicalFile)
    }

    @Test
    fun `walks subprojects to find kensa output in multi-module layouts`(@TempDir checkoutDir: Path) {
        val nested = checkoutDir.resolve("module-a/build/kensa-output").toFile().apply { mkdirs() }
        File(nested, "indices.json").writeText("[]")

        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null)

        assertNotNull(resolved) { "Expected walk to find module-a/build/kensa-output" }
        assertEquals(nested.canonicalFile, resolved!!.canonicalFile)
    }

    @Test
    fun `empty build kensa-site without manifest is not returned`(@TempDir checkoutDir: Path) {
        checkoutDir.resolve("build/kensa-site").toFile().mkdirs()  // empty — no marker

        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null)

        assertNull(resolved)
    }

    @Test
    fun `returns null when no Kensa output exists anywhere`(@TempDir checkoutDir: Path) {
        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null)
        assertNull(resolved)
    }

    @Test
    fun `finds custom siteRoot outside build directory via manifest`(@TempDir checkoutDir: Path) {
        val custom = checkoutDir.resolve("kensa-site").toFile().apply { mkdirs() }
        File(custom, "manifest.json").writeText("{}")

        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null)

        assertEquals(custom.canonicalFile, resolved!!.canonicalFile)
    }

    @Test
    fun `finds custom siteRoot at build slash custom-name via manifest`(@TempDir checkoutDir: Path) {
        val custom = checkoutDir.resolve("build/aggregate").toFile().apply { mkdirs() }
        File(custom, "manifest.json").writeText("{}")

        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null)

        assertEquals(custom.canonicalFile, resolved!!.canonicalFile)
    }

    @Test
    fun `finds custom siteRoot at deep nested path`(@TempDir checkoutDir: Path) {
        val custom = checkoutDir.resolve("reports/kensa").toFile().apply { mkdirs() }
        File(custom, "manifest.json").writeText("{}")

        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null)

        assertEquals(custom.canonicalFile, resolved!!.canonicalFile)
    }

    @Test
    fun `site root with manifest beats unrelated indices elsewhere`(@TempDir checkoutDir: Path) {
        val site = checkoutDir.resolve("kensa-site").toFile().apply { mkdirs() }
        File(site, "manifest.json").writeText("{}")
        val nonSite = checkoutDir.resolve("module-a/build/kensa-output").toFile().apply { mkdirs() }
        File(nonSite, "indices.json").writeText("[]")

        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null)

        assertEquals(site.canonicalFile, resolved!!.canonicalFile)
    }

    @Test
    fun `aggregate site root with source bundles returns the root, not a child`(@TempDir checkoutDir: Path) {
        val site = checkoutDir.resolve("kensa-site").toFile().apply { mkdirs() }
        File(site, "manifest.json").writeText("{}")
        val sourceA = File(site, "sources/web__test").apply { mkdirs() }
        File(sourceA, "indices.json").writeText("[]")
        val sourceB = File(site, "sources/api__test").apply { mkdirs() }
        File(sourceB, "indices.json").writeText("[]")

        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null)

        assertEquals(site.canonicalFile, resolved!!.canonicalFile)
    }

    @Test
    fun `two manifest dot json files - freshest mtime wins`(@TempDir checkoutDir: Path) {
        val stale = checkoutDir.resolve("build/kensa-site").toFile().apply { mkdirs() }
        val staleManifest = File(stale, "manifest.json").apply { writeText("{}") }
        staleManifest.setLastModified(System.currentTimeMillis() - 60_000L)

        val fresh = checkoutDir.resolve("aggregate/site").toFile().apply { mkdirs() }
        val freshManifest = File(fresh, "manifest.json").apply { writeText("{}") }
        freshManifest.setLastModified(System.currentTimeMillis())

        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null)

        assertEquals(fresh.canonicalFile, resolved!!.canonicalFile)
    }

    @Test
    fun `manifest under dot-gradle is ignored`(@TempDir checkoutDir: Path) {
        val real = checkoutDir.resolve("kensa-site").toFile().apply { mkdirs() }
        val realManifest = File(real, "manifest.json").apply { writeText("{}") }
        realManifest.setLastModified(System.currentTimeMillis() - 60_000L)

        val cached = checkoutDir.resolve(".gradle/caches/kensa-site").toFile().apply { mkdirs() }
        val cachedManifest = File(cached, "manifest.json").apply { writeText("{}") }
        cachedManifest.setLastModified(System.currentTimeMillis())  // newer, but should be ignored

        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null)

        assertEquals(real.canonicalFile, resolved!!.canonicalFile)
    }

    @Test
    fun `walk hit emits WalkResolved observation`(@TempDir checkoutDir: Path) {
        val custom = checkoutDir.resolve("kensa-site").toFile().apply { mkdirs() }
        File(custom, "manifest.json").writeText("{}")
        val events = mutableListOf<KensaPaths.ResolutionEvent>()

        KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null, onEvent = events::add)

        assertEquals(1, events.size)
        val event = events.single()
        assertTrue(event is KensaPaths.ResolutionEvent.WalkResolved)
        assertEquals(custom.canonicalFile, (event as KensaPaths.ResolutionEvent.WalkResolved).resolvedDir.canonicalFile)
    }

    @Test
    fun `multiple manifests emit MultipleSiteRoots observation`(@TempDir checkoutDir: Path) {
        val a = checkoutDir.resolve("build/kensa-site").toFile().apply { mkdirs() }
        val aManifest = File(a, "manifest.json").apply { writeText("{}") }
        aManifest.setLastModified(System.currentTimeMillis() - 60_000L)
        val b = checkoutDir.resolve("aggregate/site").toFile().apply { mkdirs() }
        val bManifest = File(b, "manifest.json").apply { writeText("{}") }
        bManifest.setLastModified(System.currentTimeMillis())
        val events = mutableListOf<KensaPaths.ResolutionEvent>()

        KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null, onEvent = events::add)

        val warning = events.filterIsInstance<KensaPaths.ResolutionEvent.MultipleSiteRoots>().single()
        assertEquals(b.canonicalFile, warning.picked.canonicalFile)
        assertEquals(setOf(a.canonicalFile, b.canonicalFile), warning.candidates.map { it.canonicalFile }.toSet())
    }

    @Test
    fun `multiple non-site outputs emit MultipleNonSiteOutputs observation`(@TempDir checkoutDir: Path) {
        val a = checkoutDir.resolve("module-a/build/kensa-output").toFile().apply { mkdirs() }
        File(a, "indices.json").writeText("[]")
        val b = checkoutDir.resolve("module-b/build/kensa-output").toFile().apply { mkdirs() }
        File(b, "indices.json").writeText("[]")
        val events = mutableListOf<KensaPaths.ResolutionEvent>()

        KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null, onEvent = events::add)

        val warning = events.filterIsInstance<KensaPaths.ResolutionEvent.MultipleNonSiteOutputs>().single()
        assertEquals(setOf(a.canonicalFile, b.canonicalFile), warning.candidates.map { it.canonicalFile }.toSet())
        assertEquals(warning.candidates.first().canonicalFile, warning.picked.canonicalFile)
    }

    @Test
    fun `clean fast-path emits no events`(@TempDir checkoutDir: Path) {
        val site = checkoutDir.resolve("build/kensa-site").toFile().apply { mkdirs() }
        File(site, "manifest.json").writeText("{}")
        val events = mutableListOf<KensaPaths.ResolutionEvent>()

        KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null, onEvent = events::add)

        assertEquals(emptyList<KensaPaths.ResolutionEvent>(), events)
    }

    @Test
    fun `finds site marker at build slash kensa-output path`(@TempDir checkoutDir: Path) {
        val site = checkoutDir.resolve("build/kensa-output").toFile().apply { mkdirs() }
        File(site, "manifest.json").writeText("{}")
        val testSource = File(site, "sources/test").apply { mkdirs() }
        File(testSource, "indices.json").writeText("[]")
        val uiSource = File(site, "sources/uiTest").apply { mkdirs() }
        File(uiSource, "indices.json").writeText("[]")

        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null)

        assertEquals(site.canonicalFile, resolved!!.canonicalFile)
    }

    @Test
    fun `finds non-site marker at build slash kensa-site path`(@TempDir checkoutDir: Path) {
        val output = checkoutDir.resolve("build/kensa-site").toFile().apply { mkdirs() }
        File(output, "indices.json").writeText("[]")

        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null)

        assertEquals(output.canonicalFile, resolved!!.canonicalFile)
    }
}
