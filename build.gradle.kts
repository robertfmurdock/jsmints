plugins {
    alias(libs.plugins.com.github.sghill.distribution.sha)
    id("com.zegreatrob.jsmints.plugins.versioning")
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    alias(libs.plugins.com.zegreatrob.tools.tagger)
    base
}

group = "com.zegreatrob.jsmints"


tagger {
    releaseBranch = "master"
    githubReleaseEnabled.set(true)
}

tasks {
    val publishableBuilds = listOf(
        gradle.includedBuild("libraries"),
        gradle.includedBuild("plugins"),
    )
    val includedBuilds = publishableBuilds + gradle.includedBuild("convention-plugins")

    val publish by creating {
        mustRunAfter(check)
        dependsOn(provider { publishableBuilds.map { it.task(":publish") } })
    }

    "versionCatalogUpdate" {
        dependsOn(provider { includedBuilds.map { it.task(":versionCatalogUpdate") } })
    }

    create<Copy>("collectResults") {
        dependsOn(provider { (getTasksByName("collectResults", true) - this).toList() })
        dependsOn(provider { includedBuilds.map { it.task(":collectResults") } })
        from(includedBuilds.map { it.projectDir.resolve("build/test-output") })
        into("${rootProject.buildDir.path}/test-output/${project.path}".replace(":", "/"))
    }

    create("formatKotlin") {
        dependsOn(provider { (getTasksByName("formatKotlin", true) - this).toList() })
        dependsOn(provider { includedBuilds.map { it.task(":formatKotlin") } })
    }
    check {
        dependsOn(provider { (getTasksByName("check", true) - this).toList() })
        dependsOn(provider { includedBuilds.map { it.task(":check") } })

    }
    clean {
        dependsOn(provider { includedBuilds.map { it.task(":clean") } })
    }

    release {
        mustRunAfter(check)
        finalizedBy(publish)
    }
}
