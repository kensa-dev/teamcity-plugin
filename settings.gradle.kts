pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://download.jetbrains.com/teamcity-repository")
    }
}

rootProject.name = "kensa-teamcity-plugin"

include("common")
include("server")
include("agent")
