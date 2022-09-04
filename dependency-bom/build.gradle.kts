plugins {
    `java-platform`
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("org.jlleitschuh.gradle.ktlint")
}

repositories {
    mavenCentral()
}

ktlint {
    version.set("0.45.2")
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform(libs.org.jetbrains.kotlin.kotlin.bom))
    api(platform(libs.org.jetbrains.kotlinx.kotlinx.coroutines.bom))
    api(platform(libs.org.jetbrains.kotlin.wrappers.kotlin.wrappers.bom))
    api(platform(libs.com.zegreatrob.testmints.testmints.bom))
    constraints {
        api(libs.com.soywiz.korlibs.klock)
        api(libs.io.github.microutils.kotlin.logging)
        api(libs.com.fasterxml.jackson.core.jackson.databind)
    }
}

publishing {
    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])
        }
    }
}
