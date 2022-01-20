plugins {
    `java-platform`
    id("com.zegreatrob.testmints.plugins.publish")
}

dependencies {
    constraints {
        api(project(":minjson"))
        api(project(":minreact"))
        api(project(":minenzyme"))
        api(project(":react-data-loader"))
        api(project(":wdio"))
    }
}

publishing {
    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])
        }
    }
}