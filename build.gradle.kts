plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.teamcity.server) apply false
    alias(libs.plugins.teamcity.agent) apply false
}

allprojects {
    group = "dev.kensa.teamcity"
    version = "0.1.0"
}

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
