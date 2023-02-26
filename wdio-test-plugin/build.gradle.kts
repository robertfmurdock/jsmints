plugins {
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    alias(libs.plugins.io.github.gradle.nexus.publish.plugin)
    `maven-publish`
    signing
    base
    id("com.zegreatrob.jsmints.plugins.lint")
    id("com.zegreatrob.jsmints.plugins.versioning")
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            stagingProfileId.set("59331990bed4c")
        }
    }
}

tasks {
    clean { delete(rootProject.buildDir) }
    check { dependsOn(provider { (getTasksByName("check", true) - this).toList() }) }
    assemble { dependsOn(provider { (getTasksByName("assemble", true) - this).toList() }) }
    create("formatKotlin") { dependsOn(provider { (getTasksByName("formatKotlin", true) - this).toList() }) }
    publish { dependsOn(provider { (getTasksByName("publish", true) - this).toList() }) }
}
