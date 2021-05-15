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
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
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
                implementation("org.slf4j:slf4j-simple:2.0.0-alpha1")
                implementation("org.jetbrains.kotlin:kotlin-test")

                implementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.7.2")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:$kotlinVersion")
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