import org.gradle.plugins.signing.Sign

plugins {
    `java-platform`
    id("com.zegreatrob.jsmints.plugins.publish")
    id("org.jmailen.kotlinter")
    id("com.zegreatrob.jsmints.plugins.ncu")
}

kotlin { js { nodejs() } }

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform(libs.org.jetbrains.kotlin.kotlin.bom))
    api(platform(libs.org.jetbrains.kotlinx.kotlinx.coroutines.bom))
    api(platform(libs.org.jetbrains.kotlin.wrappers.kotlin.wrappers.bom))
    api(platform(libs.com.zegreatrob.testmints.testmints.bom))
    constraints {
        api(libs.com.squareup.kotlinpoet.ksp)
        api(libs.io.github.oshai.kotlin.logging)
        api(libs.com.fasterxml.jackson.core.jackson.databind)
    }
}

publishing {
    publications {
        val bomPublication = create<MavenPublication>("bom") {
            from(components["javaPlatform"])
        }
        tasks.withType<AbstractPublishToMaven> {
            enabled = this@withType.publication == bomPublication
        }
    }
}

// The BOM and both Kotlin publications sign the same Javadoc JAR.
val signingTasks = tasks.withType<Sign>()
val sonatypePublishTasks = tasks.withType<PublishToMavenRepository>()
sonatypePublishTasks.configureEach {
    if (name == "publishBomPublicationToSonatypeRepository") {
        dependsOn(signingTasks.matching { it.name == "signJsPublication" || it.name == "signKotlinMultiplatformPublication" })
    }
}
// Only the BOM publish task is enabled. Gradle still runs the disabled multiplatform
// publish task's prerequisites, so the BOM signer runs after the Kotlin signers.
signingTasks.configureEach {
    if (name == "signBomPublication") {
        dependsOn(sonatypePublishTasks.matching { it.name == "publishKotlinMultiplatformPublicationToSonatypeRepository" })
    }
}
