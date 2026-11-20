plugins {
    id("com.zegreatrob.jsmints.plugins.publish")
    id("com.zegreatrob.jsmints.plugins.js")
    id("com.zegreatrob.jsmints.plugins.playwrighttest")
}

group = "com.zegreatrob.jsmints"

playwrightTest {
    profiles {
        register("chromium")
        register("firefox")
    }
}

dependencies {
    jsMainImplementation(jsconstraint("playwright"))
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    jsMainApi("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    jsMainApi("com.zegreatrob.testmints:async")

    "jsPlaywrightTestImplementation"("org.jetbrains.kotlin-wrappers:kotlin-node")
    "jsPlaywrightTestImplementation"(jsconstraint("jszip"))
}

val sharedFixture = layout.projectDirectory.dir(
    "../../browser-testing-fixture/src/jsE2eTest/resources/com/zegreatrob/wrapper/wdio/testing/library",
)

kotlin.sourceSets.named("jsPlaywrightTest") {
    resources.srcDir(sharedFixture)
}
