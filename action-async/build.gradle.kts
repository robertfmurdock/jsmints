import com.zegreatrob.testmints.build.BuildConstants.coroutinesVersion
import com.zegreatrob.testmints.build.BuildConstants.kotlinVersion

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
    jcenter()
}

kotlin {

    targets {
        js { nodejs() }
        jvm()
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(project(":action"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(project(":standard"))
                implementation(project(":async"))
                implementation(project(":minassert"))
                implementation(project(":minspy"))
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect", kotlinVersion))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("reflect", "1.5.0"))
                implementation("org.slf4j:slf4j-simple:1.7.5")
                implementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.7.1")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:$kotlinVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.20.0-1.3.70-eap-274-2")
            }
        }
    }
}

tasks {

    val jvmTest by getting(Test::class) {
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")

        useJUnitPlatform()
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}