plugins {
    alias(libs.plugins.com.github.sghill.distribution.sha)
    id("com.zegreatrob.jsmints.plugins.versioning")
    alias(libs.plugins.com.zegreatrob.tools.tagger)
    alias(libs.plugins.com.zegreatrob.tools.digger)
    alias(libs.plugins.com.zegreatrob.tools.fingerprint)
    base
}

group = "com.zegreatrob.jsmints"


tagger {
    releaseBranch = "master"
    githubReleaseEnabled.set(true)
    System.getenv("DISABLE_DETACHED")?.let { value ->
        disableDetached.set(value.lowercase() == "true")
    }
}

fingerprintConfig {
    includedBuilds = listOf("libraries", "plugins")
}

tasks {
    val publishableBuilds = listOf(
        gradle.includedBuild("libraries"),
        gradle.includedBuild("plugins"),
    )
    val testBuilds = listOf(
        gradle.includedBuild("libraries"),
        gradle.includedBuild("plugins"),
    ) + gradle.includedBuild("wdio-testing-library-test")
    val includedBuilds = testBuilds + gradle.includedBuild("convention-plugins")

    val publish by registering {
        mustRunAfter(check)
        dependsOn(provider { publishableBuilds.map { it.task(":publish") } })
    }
    "versionCatalogUpdate" {
        dependsOn(provider { includedBuilds.map { it.task(":versionCatalogUpdate") } })
    }
    register("kotlinUpgradeYarnLock") {
        dependsOn(
            provider {
                listOf(
                    gradle.includedBuild("libraries"),
                    gradle.includedBuild("wdio-testing-library-test")
                ).map { it.task(":kotlinUpgradeYarnLock") }
            }
        )
    }
    register<Copy>("collectResults") {
        dependsOn(provider { (getTasksByName("collectResults", true) - this).toList() })
        dependsOn(provider { testBuilds.map { it.task(":collectResults") } })
        from(testBuilds.map { it.projectDir.resolve("build/test-output") })
        into(rootProject.layout.buildDirectory.dir("test-output/${project.path}".replace(":", "/")))
    }

    register("formatKotlin") {
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
        finalizedBy(publish, currentContributionData)
    }
    currentContributionData {
        mustRunAfter(tag)
    }
}
