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
    api(platform("org.jetbrains.kotlin:kotlin-bom:1.7.10"))
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4"))
    api(platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:1.0.0-pre.382"))
    api(platform("com.zegreatrob.testmints:testmints-bom:8.1.13"))
    constraints {
        api("com.soywiz.korlibs.klock:klock:3.0.1")
        api("io.github.microutils:kotlin-logging:2.1.23")
    }
}

publishing {
    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])
        }
    }
}
