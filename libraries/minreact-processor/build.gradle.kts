plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    sourceSets {
        named("jvmMain") {
            dependencies {
                implementation("com.squareup:kotlinpoet-ksp:1.14.2")
                implementation(libs.com.google.devtools.ksp.symbol.processing.api)
            }
        }
    }
}
