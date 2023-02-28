package com.zegreatrob.jsmints.plugins.wdiotest

import org.gradle.api.GradleException

object WdioTemplate {

    val wdioTemplateText
        get() = this::class.java
            .getResourceAsStream("/com/zegreatrob/jsmints/plugins/wdiotest/wdio.conf.mjs")
            ?.readAllBytes()
            ?.decodeToString()
            ?: throw GradleException("Could not load wdio.conf.js stub")
}
