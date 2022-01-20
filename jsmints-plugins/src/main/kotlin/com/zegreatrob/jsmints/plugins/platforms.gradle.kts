package com.zegreatrob.jsmints.plugins

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
    targets {
        jvm {

        }
        js { nodejs {} }
        macosX64()
        iosX64()
        linuxX64()
        mingwX64()
    }

    sourceSets {
        getByName("jvmTest") {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
            }
        }
    }
}

tasks {
    val jvmTest by getting(Test::class) {
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
        useJUnitPlatform()
    }

}