plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.teamcity.agent)
}

kotlin {
    jvmToolchain(21)
}

teamcity {
    version = libs.versions.teamcity.get()
    agent {
        descriptor {
            pluginDeployment {
                useSeparateClassloader = true
            }
        }
        archiveName = "kensa-teamcity-agent.zip"
    }
}

dependencies {
    implementation(project(":common"))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
