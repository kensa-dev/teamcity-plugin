package dev.kensa.teamcity.server

import dev.kensa.teamcity.KensaConstants
import jetbrains.buildServer.serverSide.BuildFeature
import jetbrains.buildServer.web.openapi.PluginDescriptor

class KensaBuildFeature(descriptor: PluginDescriptor) : BuildFeature() {

    private val editUrl = descriptor.getPluginResourcesPath("editKensaFeature.jsp")

    override fun getType(): String = KensaConstants.FEATURE_TYPE
    override fun getDisplayName(): String = "Kensa Integration"
    override fun getEditParametersUrl(): String = editUrl
    override fun isMultipleFeaturesPerBuildTypeAllowed(): Boolean = false

    override fun getDefaultParameters(): Map<String, String> = mapOf(
        KensaConstants.PARAM_REPORT_TAB to "true",
        KensaConstants.PARAM_TEST_REPORTER to "true",
        KensaConstants.PARAM_FAILURE_SUMMARY to "true",
    )

    override fun describeParameters(params: Map<String, String>): String {
        val parts = mutableListOf<String>()
        if (params[KensaConstants.PARAM_REPORT_TAB]?.toBoolean() != false) parts += "Report tab"
        if (params[KensaConstants.PARAM_TEST_REPORTER]?.toBoolean() != false) parts += "Test reporter"
        if (params[KensaConstants.PARAM_FAILURE_SUMMARY]?.toBoolean() != false) parts += "Failure summary"
        val pathNote = params[KensaConstants.PARAM_OUTPUT_PATH]?.takeIf { it.isNotBlank() }
            ?.let { " (path: $it)" } ?: ""
        return parts.joinToString(", ").ifEmpty { "Disabled" } + pathNote
    }
}
