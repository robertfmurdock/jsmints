plugins {
    id("com.zegreatrob.jsmints.plugins.publish")
    kotlin("multiplatform")
}

kotlin {
    jvm()
}
dependencies {
    "jvmMainImplementation"(platform(project(":dependency-bom")))
    "jvmMainImplementation"("com.squareup:kotlinpoet-ksp")
    "jvmMainImplementation"(libs.com.google.devtools.ksp.symbol.processing.api)
}
