plugins {
    `java-platform`
    id("com.zegreatrob.testmints.plugins.publish")
}

dependencies {
    constraints {
        api(project(":minassert"))
        api(project(":standard"))
        api(project(":async"))
        api(project(":action"))
        api(project(":action-async"))
        api(project(":minspy"))
        api(project(":mindiff"))
        api(project(":minjson"))
        api(project(":report"))
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