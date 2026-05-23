package dev.kensa.teamcity.server

import dev.kensa.teamcity.KensaConstants.REPORT_START_PAGE
import jetbrains.buildServer.controllers.BuildDataExtensionUtil
import jetbrains.buildServer.serverSide.SBuildServer
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PlaceId
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.SimplePageExtension
import jetbrains.buildServer.web.reportTabs.ReportTabUtil
import javax.servlet.http.HttpServletRequest

class KensaBuildActionExtension(
    pagePlaces: PagePlaces,
    private val server: SBuildServer,
    descriptor: PluginDescriptor,
) : SimplePageExtension(
    pagePlaces,
    PlaceId.BUILD_ACTIONS,
    "kensaBuildAction",
    descriptor.getPluginResourcesPath("kensaBuildAction.jsp"),
) {
    init { register() }

    override fun isAvailable(request: HttpServletRequest): Boolean {
        val build = BuildDataExtensionUtil.retrieveBuild(request, server) ?: return false
        return ReportTabUtil.isAvailable(build, REPORT_START_PAGE)
    }

    override fun fillModel(model: MutableMap<String, Any>, request: HttpServletRequest) {
        val build = BuildDataExtensionUtil.retrieveBuild(request, server) ?: return
        model["buildData"] = build
        model["startPage"] = REPORT_START_PAGE
    }
}
