rootProject.name = "wdio-testing-library-test"


includeBuild("../libraries")
includeBuild("../plugins")
includeBuild("../convention-plugins")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("rootLibs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
