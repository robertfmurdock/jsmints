# jsmints ![Build](https://github.com/robertfmurdock/jsmints/actions/workflows/main.yml/badge.svg?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.zegreatrob.jsmints/jsmints-bom/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.zegreatrob/jsmints)

[![Gradle Lib Updates](https://github.com/robertfmurdock/jsmints/actions/workflows/gradle-update.yml/badge.svg?branch=master)](https://github.com/robertfmurdock/jsmints/actions/workflows/gradle-update.yml)
[![Gradle Wrapper Update](https://github.com/robertfmurdock/jsmints/actions/workflows/update-gradle-wrapper.yml/badge.svg?branch=master)](https://github.com/robertfmurdock/jsmints/actions/workflows/update-gradle-wrapper.yml)

![Latest Release](https://img.shields.io/github/v/release/robertfmurdock/jsmints)
![GitHub commit activity](https://img.shields.io/github/commit-activity/m/robertfmurdock/jsmints)
![Testspace tests](https://img.shields.io/testspace/passed/robertfmurdock/robertfmurdock:jsmints/master)

These are libraries full of sugar for working with Kotlin JS.

Included:

### React Data Loader

A Kotlin-based react component for handling simple data-loading state. For people who want extremely simple and clear data-loading behavior, and find other React solutions too *quirky*.

### [minreact](minreact/README.md)

Sugar for React functional components that allows them to use Kotlin data classes as props. This helps avoid the 'forgotten mandatory prop' issue that can happen when using the base Kotlin DSL.

### wdio

Kotlin wrapper + convenience functions for the wdio js project, to allow node js kotlin code to operate a web browser via wdio. This allows for writing webdriver tests with Kotlin Test.

### wdio-testing-library

Kotlin wrapper + convenience functions for [wdio testing library](https://testing-library.com/docs/webdriverio-testing-library/intro/) 

### WDIO Gradle Plugin

This is a one-stop-shop for setting up WDIO for your kotlin project.

Intended to be usable without configuration, but allow the full flexibility of the tool, it comes bundled with defaults that can be changed, including use of the ["nice html reporter"](https://github.com/rpii/wdio-html-reporter), screenshot capture, and chrome operation.

### Jspackage Gradle Plugin

A gradle plugin that allows you use a package.json file to define your Javascript dependencies.

### NCU Gradle Plugin

A gradle plugin that will automatically include [npm-check-updates](https://github.com/raineorshine/npm-check-updates) and tasks that take advantage of it.

Requires use of the Jspackage plugin.
