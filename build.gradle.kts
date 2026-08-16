import java.io.ByteArrayOutputStream
import org.gradle.process.ExecOperations
import org.jetbrains.changelog.Changelog
import javax.inject.Inject

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.teamcity.server) apply false
    alias(libs.plugins.teamcity.agent) apply false
    alias(libs.plugins.changelog)
}

allprojects {
    group = "dev.kensa.teamcity"
    version = "0.3.2"
}

changelog {
    repositoryUrl = "https://github.com/kensa-dev/teamcity-plugin"
    versionPrefix = ""
    groups.empty()
}

// ---------- Local docker dev harness ----------

tasks.register<Exec>("devUp") {
    dependsOn(":server:serverPlugin")
    workingDir = file("docker")
    commandLine("docker", "compose", "up", "-d")
    description = "Builds the plugin and starts TeamCity in docker."
}

tasks.register<Exec>("devReload") {
    dependsOn(":server:serverPlugin")
    workingDir = file("docker")
    commandLine("docker", "compose", "restart", "teamcity-server")
    description = "Rebuilds the plugin and restarts the TC server container."
}

tasks.register<Exec>("devDown") {
    workingDir = file("docker")
    commandLine("docker", "compose", "down")
    description = "Stops the docker stack."
}

// ---------- JetBrains Marketplace signing & publishing ----------
// Mirrors the env-var contract used by the kensa-dev/intellij-plugin so CI
// can carry the same secrets across both repos:
//   PRIVATE_KEY            - PEM private key contents
//   CERTIFICATE_CHAIN      - PEM cert chain contents
//   PRIVATE_KEY_PASSWORD   - passphrase for the private key (optional)
//   PUBLISH_TOKEN          - JetBrains Marketplace upload token

val marketplaceZipSignerVersion = "0.1.24"
val pluginArchive = layout.projectDirectory.file("server/build/distributions/kensa-teamcity-plugin.zip")
val signedPluginArchive = layout.buildDirectory.file("distributions/kensa-teamcity-plugin-signed.zip")
val zipSignerJar = layout.buildDirectory.file("zip-signer/marketplace-zip-signer-cli.jar")

val downloadZipSigner = tasks.register<Exec>("downloadZipSigner") {
    group = "publishing"
    description = "Downloads the JetBrains marketplace-zip-signer CLI."
    val target = zipSignerJar.get().asFile
    outputs.file(target)
    doFirst { target.parentFile.mkdirs() }
    commandLine(
        "curl", "-fsSL",
        "-o", target.absolutePath,
        "https://github.com/JetBrains/marketplace-zip-signer/releases/download/$marketplaceZipSignerVersion/marketplace-zip-signer-cli.jar",
    )
    onlyIf { !target.exists() }
}

tasks.register<JavaExec>("signPlugin") {
    group = "publishing"
    description = "Signs the plugin zip with the JetBrains Marketplace key (reads PRIVATE_KEY, CERTIFICATE_CHAIN, PRIVATE_KEY_PASSWORD env vars)."
    dependsOn(":server:serverPlugin", downloadZipSigner)

    val keyEnv = providers.environmentVariable("PRIVATE_KEY")
    val certEnv = providers.environmentVariable("CERTIFICATE_CHAIN")
    val passEnv = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    val signerJarFile = zipSignerJar.map { it.asFile }
    val unsigned = pluginArchive
    val signed = signedPluginArchive.map { it.asFile }

    inputs.file(unsigned)
    outputs.file(signed)

    doFirst {
        require(keyEnv.isPresent) { "PRIVATE_KEY env var must contain the PEM private key contents" }
        require(certEnv.isPresent) { "CERTIFICATE_CHAIN env var must contain the PEM cert chain contents" }

        val workDir = layout.buildDirectory.dir("zip-signer/work").get().asFile.also { it.mkdirs() }
        val keyFile = workDir.resolve("private.pem").apply { writeText(keyEnv.get()); setReadable(false, false); setReadable(true, true) }
        val certFile = workDir.resolve("chain.crt").apply { writeText(certEnv.get()) }

        signed.get().parentFile.mkdirs()
        classpath = files(signerJarFile.get())
        args = mutableListOf(
            "sign",
            "-in", unsigned.asFile.absolutePath,
            "-out", signed.get().absolutePath,
            "-key-file", keyFile.absolutePath,
            "-cert-file", certFile.absolutePath,
        ).also {
            if (passEnv.isPresent && passEnv.get().isNotBlank()) {
                it += listOf("-key-pass", passEnv.get())
            }
        }
    }
}

abstract class PublishPluginTask : DefaultTask() {
    @get:Inject
    abstract val execOps: ExecOperations

    @get:org.gradle.api.tasks.InputFile
    abstract val signedZip: org.gradle.api.file.RegularFileProperty

    @get:org.gradle.api.tasks.Input
    abstract val xmlId: org.gradle.api.provider.Property<String>

    @get:org.gradle.api.tasks.Input
    @get:org.gradle.api.tasks.Optional
    abstract val notes: org.gradle.api.provider.Property<String>

    @get:org.gradle.api.tasks.Internal
    abstract val publishToken: org.gradle.api.provider.Property<String>

    @org.gradle.api.tasks.TaskAction
    fun publish() {
        val token = publishToken.orNull?.takeIf { it.isNotBlank() }
            ?: error("PUBLISH_TOKEN env var must contain a JetBrains Marketplace upload token")
        val zip = signedZip.get().asFile
        require(zip.isFile) { "Signed plugin not found at $zip (run :signPlugin first)" }

        val stdout = ByteArrayOutputStream()
        val cmd = mutableListOf(
            "curl", "-fsSL",
            "-X", "POST",
            "-H", "Authorization: Bearer $token",
            "-F", "xmlId=${xmlId.get()}",
            "-F", "file=@${zip.absolutePath}",
        )
        notes.orNull?.takeIf { it.isNotBlank() }?.let {
            // --form-string (vs -F) so curl doesn't interpret a leading `<` or
            // `@` in the HTML body as a filename to read from.
            cmd += listOf("--form-string", "notes=$it")
        }
        cmd += "https://plugins.jetbrains.com/plugin/uploadPlugin"

        val result = execOps.exec {
            commandLine(cmd)
            standardOutput = stdout
            isIgnoreExitValue = true
        }
        if (result.exitValue != 0) {
            error("Marketplace upload failed (curl exit ${result.exitValue}): ${stdout.toString().trim()}")
        }
        logger.lifecycle("Marketplace response: ${stdout.toString().trim()}")
    }
}

tasks.register<PublishPluginTask>("publishPlugin") {
    group = "publishing"
    description = "Uploads the signed plugin to JetBrains Marketplace (reads PUBLISH_TOKEN env var)."
    dependsOn("signPlugin")
    signedZip.set(signedPluginArchive)
    // Marketplace stores TC plugins with a `teamcity_` xmlId prefix (namespace
    // separate from IntelliJ plugins). The plugin descriptor's `<name>` stays
    // bare; only the marketplace upload needs the prefix.
    xmlId.set("teamcity_kensa-teamcity-plugin")
    publishToken.set(providers.environmentVariable("PUBLISH_TOKEN"))

    val changelog = project.changelog
    val pluginVersion = project.version.toString()
    notes.set(provider {
        with(changelog) {
            renderItem(
                (getOrNull(pluginVersion) ?: getUnreleased())
                    .withHeader(false)
                    .withEmptySections(false),
                Changelog.OutputType.HTML,
            )
        }
    })
}
