package dev.kensa.teamcity.agent

import dev.kensa.teamcity.KensaConstants
import dev.kensa.teamcity.KensaPaths
import dev.kensa.teamcity.model.IndicesFile
import dev.kensa.teamcity.model.ManifestFile
import dev.kensa.teamcity.model.TestResultFile
import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.util.EventDispatcher
import java.io.File

class KensaAgentLifecycleAdapter(
    events: EventDispatcher<AgentLifeCycleListener>,
    private val artifactsWatcher: ArtifactsWatcher,
) : AgentLifeCycleAdapter() {

    init {
        Loggers.AGENT.info("[Kensa] adapter constructed, registering listener")
        events.addListener(this)
    }

    override fun beforeBuildFinish(runningBuild: AgentRunningBuild, buildStatus: BuildFinishedStatus) {
        try {
            run(runningBuild)
        } catch (t: Throwable) {
            runningBuild.buildLogger.warning("Kensa plugin: unexpected error, skipping (${t.message})")
        }
    }

    private fun run(runningBuild: AgentRunningBuild) {
        val log = runningBuild.buildLogger
        val feature = runningBuild.getBuildFeaturesOfType(KensaConstants.FEATURE_TYPE).firstOrNull() ?: return
        val params = feature.parameters
        val checkoutDir = runningBuild.checkoutDirectory

        val outputDir = KensaPaths.resolve(checkoutDir, params[KensaConstants.PARAM_OUTPUT_PATH]) ?: run {
            log.message("Kensa: no output directory found, skipping")
            return
        }

        val sink = BuildLoggerSink(log, artifactsWatcher)
        val reportTab = params[KensaConstants.PARAM_REPORT_TAB]?.toBoolean() != false
        val testReporter = params[KensaConstants.PARAM_TEST_REPORTER]?.toBoolean() != false
        val failureSummary = params[KensaConstants.PARAM_FAILURE_SUMMARY]?.toBoolean() != false

        if (reportTab) {
            KensaArtifactPublisher(sink).publish(checkoutDir, outputDir)
        }

        if (testReporter) {
            for (sourcesetDir in resolveSourcesets(outputDir, runningBuild)) {
                val indices = parseIndices(sourcesetDir, runningBuild) ?: continue
                val resolver: ResultResolver = { className ->
                    if (!failureSummary) null else loadResults(sourcesetDir, className, runningBuild)
                }
                KensaResultPublisher(sink, resolver).publish(indices)
            }
        }
    }

    private fun resolveSourcesets(outputDir: File, runningBuild: AgentRunningBuild): List<File> {
        val manifestFile = File(outputDir, "manifest.json")
        if (!manifestFile.isFile) return listOf(outputDir)
        return try {
            ManifestFile.parse(manifestFile.readText()).sources.map { File(outputDir, it.url) }
        } catch (e: Exception) {
            runningBuild.buildLogger.warning("Kensa: malformed manifest.json (${e.message}), treating output dir as single sourceset")
            listOf(outputDir)
        }
    }

    private fun parseIndices(sourcesetDir: File, runningBuild: AgentRunningBuild): IndicesFile? {
        val indicesFile = File(sourcesetDir, "indices.json")
        if (!indicesFile.isFile) return null
        return try {
            IndicesFile.parse(indicesFile.readText())
        } catch (e: Exception) {
            runningBuild.buildLogger.warning("Kensa: malformed ${indicesFile.name} (${e.message}), skipping sourceset")
            null
        }
    }

    private fun loadResults(sourcesetDir: File, className: String, runningBuild: AgentRunningBuild): TestResultFile? {
        val resultsFile = File(sourcesetDir, "results/$className.json")
        if (!resultsFile.isFile) return null
        return try {
            TestResultFile.parse(resultsFile.readText())
        } catch (e: Exception) {
            runningBuild.buildLogger.warning("Kensa: malformed ${resultsFile.name} (${e.message}), skipping failure summary for this class")
            null
        }
    }
}

/**
 * Production sink: routes test events through TC's typed BuildProgressLogger API and publishes
 * artifacts via ArtifactsWatcher. These are the supported channels in `beforeBuildFinish`;
 * stdout-based service messages are NOT scanned in lifecycle hooks.
 */
private class BuildLoggerSink(
    private val log: BuildProgressLogger,
    private val artifactsWatcher: ArtifactsWatcher,
) : KensaServiceMessageSink {

    override fun testStarted(name: String) {
        log.logTestStarted(name)
    }

    override fun testFailed(name: String, message: String, details: String) {
        log.logTestFailed(name, message, details)
    }

    override fun testFinished(name: String) {
        log.logTestFinished(name)
    }

    override fun publishArtifact(sourceRelativeToCheckout: String, destination: String) {
        artifactsWatcher.addNewArtifactsPath("$sourceRelativeToCheckout => $destination")
    }
}
