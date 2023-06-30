plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation("com.squareup:kotlinpoet-ksp:1.14.2")
                implementation(libs.com.google.devtools.ksp.symbol.processing.api)
            }
        }
    }
}
