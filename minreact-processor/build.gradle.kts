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
                implementation("com.squareup:javapoet:1.12.1")
                implementation(libs.com.google.devtools.ksp.symbol.processing.api)
            }
        }
    }
}
