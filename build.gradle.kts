plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

allprojects {
    group = "dev.kensa.teamcity"
    version = "0.1.0"
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://download.jetbrains.com/teamcity-repository")
    }
}
