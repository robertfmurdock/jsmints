plugins {
    `java-platform`
    id("com.zegreatrob.testmints.plugins.publish")
}

dependencies {
    constraints {
        allprojects.forEach {
            api(project(it.path.also {path -> println("projectDep $path") } ))
        }
    }
}