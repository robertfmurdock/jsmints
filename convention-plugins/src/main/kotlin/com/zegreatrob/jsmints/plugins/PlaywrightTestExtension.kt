package com.zegreatrob.jsmints.plugins

import org.gradle.api.provider.Property

abstract class PlaywrightTestExtension {
    abstract val baseUrl: Property<String>
    abstract val browser: Property<String>
    abstract val headless: Property<Boolean>
    abstract val timeoutMillis: Property<Int>
}
