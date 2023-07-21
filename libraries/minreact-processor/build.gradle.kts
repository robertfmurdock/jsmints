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
}
dependencies {
    "jvmMainImplementation"("com.squareup:kotlinpoet-ksp:1.14.2")
    "jvmMainImplementation"(libs.com.google.devtools.ksp.symbol.processing.api)
}