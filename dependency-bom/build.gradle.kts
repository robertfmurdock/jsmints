plugins {
    `java-platform`
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("org.jmailen.kotlinter")
    id("com.zegreatrob.jsmints.plugins.ncu")
}

repositories {
    mavenCentral()
}

kotlin { js { nodejs() } }

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
        tasks.withType<AbstractPublishToMaven> {
            enabled = named<MavenPublication>("js").map { this@withType.publication != it }.get()
        }
    }
}

afterEvaluate {
    tasks {
        "publishBomPublicationToSonatypeRepository" {
            dependsOn("signJsPublication", "signKotlinMultiplatformPublication")
        }
        "signBomPublication" {
            dependsOn("publishKotlinMultiplatformPublicationToSonatypeRepository")
        }
    }
}