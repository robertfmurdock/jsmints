plugins {
    `maven-publish`
    alias(libs.plugins.com.github.ben.manes.versions)
    alias(libs.plugins.io.github.gradle.nexus.publish.plugin)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
//    alias(libs.plugins.org.jmailen.kotlinter)
    base
    signing
}

//tasks {
//    withType<DependencyUpdatesTask> {
//        checkForGradleUpdate = true
//        outputFormatter = "json"
//        outputDir = "build/dependencyUpdates"
//        reportfileName = "report"
//        revision = "release"
//
//        rejectVersionIf {
//            "^[0-9.]+[0-9](-RC|-M[0-9]+|-RC[0-9]+|-beta.*|-alpha.*|-dev.*|-RC.*)\$"
//                .toRegex(RegexOption.IGNORE_CASE)
//                .matches(candidate.version)
//        }
//    }
//}

nexusPublishing {
    this@nexusPublishing.repositories {
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
    clean {
        delete(rootProject.buildDir)
        dependsOn(provider { (getTasksByName("clean", true) - this).toList() })
    }
    check { dependsOn(provider { (getTasksByName("check", true) - this).toList() }) }
    assemble { dependsOn(provider { (getTasksByName("assemble", true) - this).toList() }) }
    create("formatKotlin") { dependsOn(provider { (getTasksByName("formatKotlin", true) - this).toList() }) }
    publish { dependsOn(provider { (getTasksByName("publish", true) - this).toList() }) }
}
