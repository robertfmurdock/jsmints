package com.zegreatrob.jsmints.plugins.wdiotest

import org.gradle.api.GradleException

object WdioTemplate {

    val wdioTemplateText get() = loadResouce("wdio.conf.mjs")
    val htmlReporterPluginText get() = loadResouce("html-reporter.mjs")

    private fun loadResouce(resourceName: String) = (
        this::class.java
            .getResource("/com/zegreatrob/jsmints/plugins/wdiotest/$resourceName")
            ?: throw GradleException("Could not load $resourceName")
        )
}
