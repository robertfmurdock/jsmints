plugins {
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
