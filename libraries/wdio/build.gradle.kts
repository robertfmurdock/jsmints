plugins {
    id("com.zegreatrob.jsmints.plugins.versioning")
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
}
dependencies {
    jsMainImplementation(kotlin("stdlib"))
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    jsMainImplementation("com.soywiz.korlibs.klock:klock")
    jsMainImplementation("io.github.oshai:kotlin-logging")
    jsMainImplementation(jsconstraint("@wdio/cli"))
    jsMainImplementation(jsconstraint("webdriverio"))
}
