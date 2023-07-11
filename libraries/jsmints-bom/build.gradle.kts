plugins {
    `java-platform`
    id("com.zegreatrob.jsmints.plugins.publish")
}

dependencies {
    constraints {
        api(project(":minenzyme"))
        api(project(":minjson"))
        api(project(":minreact"))
        api(project(":minreact-processor"))
        api(project(":react-data-loader"))
        api(project(":react-testing-library"))
        api(project(":user-event-testing-library"))
        api(project(":wdio"))
        api(project(":wdio-testing-library"))
        api(project(":wdiorunner"))
        api("com.zegreatrob.jsmints.plugins.wdiotest:com.zegreatrob.jsmints.plugins.wdiotest.gradle.plugin")
        api("com.zegreatrob.jsmints:plugins:${rootProject.version}")
    }
}

publishing {
    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])
        }
    }
}
