import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import com.zegreatrob.testmints.build.BuildConstants
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "1.3.21"
    id("maven-publish")
    id("com.jfrog.bintray") version ("1.8.4")
}

group = "com.zegreatrob.testmints"
version = "0.0.3"

repositories {
    mavenCentral()
}

kotlin {
    targets {
        jvm()
        add(presets["js"].createTarget("js"))
        macosX64()
        linuxX64()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-test-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common:${BuildConstants.kotlinVersion}")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-junit:${BuildConstants.kotlinVersion}")
            }
        }

        val nativeCommonMain by creating {
            dependsOn(commonMain)
        }

        val macosX64Main by getting { dependsOn(nativeCommonMain) }

        val linuxX64Main by getting { dependsOn(nativeCommonMain) }

        getByName("jsMain") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
            }
        }
    }
}

tasks {
    getByName<Kotlin2JsCompile>("compileKotlinJs") {
        kotlinOptions.moduleKind = "umd"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }

    val bintrayUpload by getting(BintrayUploadTask::class) {
        doFirst {
            val publications = project.publishing.publications
                    .filterNot {
                        it.name.contains("-test")
                    }
                    .map {
                        it.name.also(::println)
                    }
            setPublications(*publications.toTypedArray())
        }

        dependsOn("publishToMavenLocal")
    }
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_KEY")
    override = true

    pkg(closureOf<BintrayExtension.PackageConfig> {
        repo = "zegreatrob"
        name = "testmints"

        version(closureOf<BintrayExtension.VersionConfig> {
        })
    })
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.zegreatrob.testmints"
            artifactId = "standard"
            version = "${project.version}"
        }
    }
}

