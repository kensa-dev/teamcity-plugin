package dev.kensa.teamcity

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
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
        checkoutDir.resolve("build/kensa-output").toFile().mkdirs()

        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null)

        assertEquals(site.canonicalFile, resolved!!.canonicalFile)
    }

    @Test
    fun `falls back to kensa-output when kensa-site missing`(@TempDir checkoutDir: Path) {
        val output = checkoutDir.resolve("build/kensa-output").toFile().apply { mkdirs() }

        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null)

        assertEquals(output.canonicalFile, resolved!!.canonicalFile)
    }

    @Test
    fun `walks subprojects to find kensa output in multi-module layouts`(@TempDir checkoutDir: Path) {
        val nested = checkoutDir.resolve("module-a/build/kensa-output").toFile().apply { mkdirs() }

        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null)

        assertEquals(nested.canonicalFile, resolved!!.canonicalFile)
    }

    @Test
    fun `returns null when no Kensa output exists anywhere`(@TempDir checkoutDir: Path) {
        val resolved = KensaPaths.resolve(checkoutDir.toFile(), explicitPath = null)
        assertNull(resolved)
    }
}
