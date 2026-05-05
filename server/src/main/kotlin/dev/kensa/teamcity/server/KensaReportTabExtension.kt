package dev.kensa.teamcity.server

import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SBuildServer
import jetbrains.buildServer.web.openapi.ArtifactsViewTab
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.ReportTabsIsolationProtection
import jetbrains.buildServer.web.reportTabs.ReportTabUtil
import javax.servlet.http.HttpServletRequest

private const val START_PAGE = "kensa-site/index.html"

/**
 * Extends ArtifactsViewTab so TeamCity treats the Kensa report as a registered HTML report
 * artifact. When the server has artifact domain isolation configured (or disabled), TC's
 * standard JSP `<bs:iframe>` tag will render the report inline. When isolation blocks
 * embedding, the JSP falls back to a download link with a hint to the admin.
 */
class KensaReportTabExtension(
    pagePlaces: PagePlaces,
    server: SBuildServer,
    descriptor: PluginDescriptor,
    isolation: ReportTabsIsolationProtection,
) : ArtifactsViewTab("Kensa", "kensaReport", pagePlaces, server, isolation) {

    init {
        setIncludeUrl(descriptor.getPluginResourcesPath("kensaReportTab.jsp"))
    }

    override fun fillModel(model: MutableMap<String, Any>, request: HttpServletRequest, build: SBuild) {
        super.fillModel(model, request, build)
        model["startPage"] = START_PAGE
        model["buildData"] = build
    }

    override fun isAvailable(request: HttpServletRequest, build: SBuild): Boolean =
        ReportTabUtil.isAvailable(build, START_PAGE)
}
