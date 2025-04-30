# jsmints ![Build](https://github.com/robertfmurdock/jsmints/actions/workflows/main.yml/badge.svg?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.zegreatrob.jsmints/jsmints-bom/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.zegreatrob/jsmints)

[![Gradle Lib Updates](https://github.com/robertfmurdock/jsmints/actions/workflows/gradle-update.yml/badge.svg?branch=master)](https://github.com/robertfmurdock/jsmints/actions/workflows/gradle-update.yml)
[![Gradle Wrapper Update](https://github.com/robertfmurdock/jsmints/actions/workflows/update-gradle-wrapper.yml/badge.svg?branch=master)](https://github.com/robertfmurdock/jsmints/actions/workflows/update-gradle-wrapper.yml)

![Latest Release](https://img.shields.io/github/v/release/robertfmurdock/jsmints)
![GitHub commit activity](https://img.shields.io/github/commit-activity/m/robertfmurdock/jsmints)

Jsmints is a suite of libraries and gradle plugins for working with Kotlin JS, with a focus on testing and version updating.

Included:

### react-testing-library

A kotlin wrapper for [react testing library](https://testing-library.com/docs/react-testing-library/intro/). Coroutine sugar included to help avoid common errors, like forgetting to "await" a returned promise.

Most functions are available from the "TestingLibraryReact" singleton object. Import and play.

    import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact

- [examples](libraries/react-testing-library/src/jsTest/kotlin/com/zegreatrob/wrapper/testinglibrary/react/ByLabelTextTest.kt)


### user-event-testing-library

A kotlin wrapper for [user event](https://testing-library.com/docs/user-event/intro). Similar to react-testing-library, coroutine sugar included to help avoid common errors, like forgetting to "await" a returned promise.

### wdio

Kotlin wrapper + convenience functions for the wdio js project, to allow node js kotlin code to operate a web browser via wdio. This allows for writing webdriver tests with Kotlin Test.

### wdio-testing-library

Kotlin wrapper + convenience functions for [wdio testing library](https://testing-library.com/docs/webdriverio-testing-library/intro/).

### WDIO Gradle Plugin

This is a one-stop-shop for setting up WDIO for your kotlin project.

Intended to be usable without configuration, but allow the full flexibility of the tool, it comes bundled with defaults that can be changed, including use of the ["nice html reporter"](https://github.com/rpii/wdio-html-reporter), screenshot capture, and chrome operation.

### Jspackage Gradle Plugin

A gradle plugin that allows you use a package.json file to define your Javascript dependencies.

### NCU Gradle Plugin

A gradle plugin that will automatically include [npm-check-updates](https://github.com/raineorshine/npm-check-updates) and tasks that take advantage of it.

Requires use of the Jspackage plugin.

### React Data Loader

A Kotlin-based react component for handling simple data-loading state. For people who want extremely simple and clear data-loading behavior, and find other React solutions too *quirky*.

### [minreact](minreact/README.md)

Sugar for React functional components that allows them to use Kotlin data classes as props. This helps avoid the 'forgotten mandatory prop' issue that can happen when using the base Kotlin DSL.

If you choose to use this library, please share feedback! The API is strange and could use some love regarding what's good and what stinks.

## Installation

All of these are either regular libraries, or gradle plugins.

They're listed at the [com.zegreatrob.jsmints maven repository](https://mvnrepository.com/artifact/com.zegreatrob.jsmints)

### Jspackage Gradle Plugin
* Add the jspackage gradle plugin
```kotlin
plugins {
    id("com.zegreatrob.jsmints.plugins.jspackage") version "6.4.19" // Use latest release from above
}
```
### react-testing-library and user-event-testing-library-js
Use the Jspackage plugin to install the npm dependencies specified in the [com.zegreatrob.jsmints dependency-bom](https://mvnrepository.com/artifact/com.zegreatrob.jsmints/dependency-bom) artifact.
The specific packages provided are listed in [libraries/dependency-bom/package.json](libraries/dependency-bom/package.json)
```kotlin
dependencies {
    jsTestImplementation(project.dependencies.enforcedPlatform("com.zegreatrob.jsmints:jsmints-bom:6.4.19"))
    jsTestImplementation("com.zegreatrob.jsmints:react-testing-library")
    jsTestImplementation("com.zegreatrob.jsmints:user-event-testing-library-js")
}
```
Due to a dependency bug in `kotest-framework-engine-js`, you may have to exclude kotlinx-coroutines-debug (jvm only) from the jsTest dependencies brought in through `jsmints-bom`
```kotlin
configurations.named("jsTestImplementation") {
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-debug")
}
```

Sorry for the abbreviated instructions, but I wanted to get something here to help anyone who might get stuck!

## Using jsNodeTest instead of jsBrowserTest
You can set up react component test suites using `node.js` and `jsdom` with `react-testing-library` instead of using 
`Chrome` with `Karma`. This is more analogous to what the JS community is accustomed to than the tools Kotlin JS provides
out-of-the-box.  This provides a better developer experience as you can set breakpoints and see console output while 
running tests.  `react-testing-library` and `user-event-testing-library-js` have been preconfigured to work in this
environment once you add dependencies for `jsdom` and `global-jsdom`
```kotlin
    jsTestImplementation(npm("jsdom", "26.1.0"))
    jsTestImplementation(npm("global-jsdom", "26.0.0"))
```