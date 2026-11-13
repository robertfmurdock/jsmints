includeBuild("../libraries")
includeBuild("../plugins")
includeBuild("../convention-plugins")

dependencyResolutionManagement {
    versionCatalogs {
        create("rootLibs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
