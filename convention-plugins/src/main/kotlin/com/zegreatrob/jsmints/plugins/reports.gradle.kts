package com.zegreatrob.jsmints.plugins

repositories {
    mavenCentral()
}

tasks {
    val projectResultPath = rootProject.layout.buildDirectory.dir(
        "test-output/${project.path}/results".replace(":", "/"),
    )
    val check = named("check")
    register<Copy>("collectResults") {
        group = "verification"
        description = "Collects this project's reports and test results."
        mustRunAfter(check)
        from(layout.buildDirectory.dir("reports"))
        from(layout.buildDirectory.dir("test-results"))
        into(projectResultPath)
    }
}
