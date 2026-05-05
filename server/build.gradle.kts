plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.teamcity.server)
}

kotlin {
    jvmToolchain(21)
}

teamcity {
    version = libs.versions.teamcity.get()
    server {
        descriptor = file("${rootDir}/teamcity-plugin.xml")
        archiveName = "kensa-teamcity-plugin.zip"
        tokens = mapOf("Plugin_Version" to project.version.toString())
    }
}

dependencies {
    implementation(project(":common"))
    agent(project(path = ":agent", configuration = "plugin"))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
