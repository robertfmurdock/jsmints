package com.zegreatrob.jsmints.plugins.wdiotest

import org.gradle.api.GradleException

object WdioTemplate {

    val wdioTemplate get() = loadResource("wdio.conf.mjs")
    val htmlReporterPlugin get() = loadResource("html-reporter.mjs")
    val chromePlugin get() = loadResource("chrome.mjs")
    val screenshotsOnFailurePlugin get() = loadResource("screenshots-on-failure.mjs")

    private fun loadResource(resourceName: String) = (
        this::class.java
            .getResource("/com/zegreatrob/jsmints/plugins/wdiotest/$resourceName")
            ?: throw GradleException("Could not load $resourceName")
        )
}
