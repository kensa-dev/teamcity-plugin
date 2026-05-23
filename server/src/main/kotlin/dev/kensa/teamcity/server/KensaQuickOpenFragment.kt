package dev.kensa.teamcity.server

import dev.kensa.teamcity.KensaConstants.REPORT_START_PAGE
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SBuildServer
import jetbrains.buildServer.web.openapi.BuildInfoFragmentTab
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.WebControllerManager
import jetbrains.buildServer.web.reportTabs.ReportTabUtil
import javax.servlet.http.HttpServletRequest

class KensaQuickOpenFragment(
    server: SBuildServer,
    manager: WebControllerManager,
    descriptor: PluginDescriptor,
) : BuildInfoFragmentTab(
    server,
    manager,
    "kensaQuickOpen",
    descriptor.getPluginResourcesPath("kensaQuickOpenFragment.jsp"),
) {
    override fun getDisplayName(): String = "Kensa Quick Open"

    override fun isAvailable(request: HttpServletRequest): Boolean {
        if (!super.isAvailable(request)) return false
        val build = getBuild(request) ?: return false
        return ReportTabUtil.isAvailable(build, REPORT_START_PAGE)
    }

    override fun fillModel(model: MutableMap<String, Any>, request: HttpServletRequest, build: SBuild?) {
        if (build == null) return
        model["buildData"] = build
        model["startPage"] = REPORT_START_PAGE
    }
}
