plugins {
    `java-platform`
    id("com.zegreatrob.jsmints.plugins.publish")
}

dependencies {
    constraints {
        api(project(":minjson"))
        api(project(":minreact"))
        api(project(":minenzyme"))
        api(project(":react-data-loader"))
        api(project(":wdio"))
        api(project(":wdio-testing-library"))
        api(project(":js-package-plugin"))
        api("com.zegreatrob.jsmints.plugins.wdiotest:com.zegreatrob.jsmints.plugins.wdiotest.gradle.plugin")
    }
}

publishing {
    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])
        }
    }
}
