import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("js") version "1.3.72"
}

repositories {
    mavenCentral()
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    maven { url = uri("https://dl.bintray.com/kotlin/kotlin-js-wrappers") }
}

kotlin {
    target {
        nodejs()
    }
}

dependencies {
    api(npm("core-js", "^3.6.5"))
    api("org.jetbrains:kotlin-react:16.13.1-pre.110-kotlin-1.3.72")
    api("org.jetbrains:kotlin-react-dom:16.13.1-pre.110-kotlin-1.3.72")

    testImplementation(project(":minenzyme"))
    testImplementation(project(":standard"))
    testImplementation(project(":minassert"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-common")
    testImplementation("org.jetbrains.kotlin:kotlin-test-js")
}

tasks {
    val compileKotlinJs by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "commonjs"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }
    val compileTestKotlinJs by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "commonjs"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }
}
