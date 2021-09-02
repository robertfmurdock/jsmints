# <img src="test-mint-2.png" alt="Logo" width="100"> testmints [![CircleCI](https://circleci.com/gh/robertfmurdock/testmints.svg?style=svg)](https://circleci.com/gh/robertfmurdock/testmints) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.zegreatrob.testmints/standard/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.zegreatrob/testmints)

Do your tests have a great personality but have bad breath? Just looking for a way to freshen up your tests after a big meal of refactoring?

Testmints is here for you.

Testmints is a low-cal solution to bring a tiny bit of sugar to your Kotlin tests, hopefully improving readability and just generally making things less smelly.

Don't like the flavor? Submit a PR, or just don't use em. :-D

## Requirements

All sugar here is multiplatform compliant and works great in combination with the [standard kotlin.test library](https://kotlinlang.org/api/latest/kotlin.test/index.html). The intention is to also minimize its dependencies as much as possible, to maximize portability.

## Installation
Just want to dive right in? Here's the gradle snippets to get going. Basically, make sure you've loaded maven central and then reference the library. I don't recommend using the gradle plus setting unless you like to *live on the edge*.

    repositories {
        mavenCentral()
    }
    
    kotlin {
        sourceSets {
            val commonTest by getting {
                dependencies {
                    implementation("com.zegreatrob.testmints:standard:+")
                }
            }
        }
     }


## Standard

Lots of people attempt to organize their tests in a setup, exercise, verify style, sometimes using the language given, when, then. You'll frequently see these tests written as such:

    @Test
    fun plusOne() {
        // Setup, or Given
        val input: Int = Random.nextInt()
        val expected = input + 1
        // Exercise, or When
        val result = input.plusOne()
        // Verify, or Then
        assertEquals(expected, result)
    }

This works alright, but the comments are a little smelly. Some people might simply delete these comments, and use blank lines to indicate a section break. The trouble is though, that the comments are actually communicating something valuable to the reader - the name and intent of the section! And if you use blank lines to divide using these meaningful sections, then you can't use them again to break things up for any other reason.

As tests get more complex, this can be confusing, and programmers reading the test may find it difficult to identify what is exactly being tested. And if they can't figure the structure of the test out correctly, it'll be more difficult for them to maintain and understand the original intent behind the test as things change.

Let's chew on the standard mints and see what we can do!

    @Test
    fun plusOne() = setup(object {
        val input: Int = Random.nextInt()
        val expected = input + 1
    }) exercise {
        input.plusOne()
    } verify { result ->
        assertEquals(expected, result)
    }

Whoa whoa whoa. So, we've replaced the comments with functions! This forces us to organize the test into sections. We store the data required for the setup on an anonymous kotlin object, to which the exercise and verify sections have "this" access. The return value from the exercise section is passed onto the verify section as a parameter.

By using this style, we've retained all the benefits of the comments, and formalized them, and, *hopefully*, made the test more expressive as a result in about the same amount of space (about 9 - 10 lines).

If you're an IntelliJ user (like me), you can download live templates for setting up these tests faster [here](https://github.com/robertfmurdock/testmints/raw/master/templates/IdeaLiveTemplates.zip).
After you download the file, you can import it using File | Import Settings. If you don't see import settings, you're probably sharing your IDE settings and you'll have to disable sync for a moment in order to do the import.

## Async

For tests that use Kotlin's coroutine system, the regular mints won't work - for those you'll need the alternate module: com.zegreatrob.testmints:async

    @Test
    fun plusOne() = asyncSetup(object {
        val input: Int = Random.nextInt()
        val expected = input + 1
    }) exercise {
        input.suspendPlusOne() // suspend functions can be called in the all closures
    } verify { result ->
        assertEquals(expected, result)
    }

In all test closures, suspend functionality can be used. All jobs started in the exercise lambda will end before the verify lambda starts.

If you want access to any of the involved scopes (there is a test scope, a setup scope, and an exercise scope), you can add a ScopeMint to your context object:

    @Test
    fun plusOne() = asyncSetup(object : ScopeMint() {
        val input: Int = Random.nextInt()
        val testTarget = TestTarget(exerciseScope)
    }) exercise {
        testTarget.spawnWorkOnExerciseScope()
    } verify { 
        assertEquals(true, testTarget.workComplete)
    }

You'll probably have to play with it a bit to get the hang of it, but give it a shot. So long as you can inject the "exercise scope" into your test target, you won't have to add waits for the work to be done.

Be warned: when using the async mints, you *MUST* use the form:

    @Test
    fun plusOne() = asyncSetup ...
    
and not 
    
    @Test
    fun plusOne() { 
       asyncSetup ...
    } 

In order to write tests safely on all platforms... especially KotlinJs.


## I Still Don't Get It...

For more on the thinking, read these two essays:

[The Quality of Test](https://medium.com/@robert.f.murdock/the-quality-of-test-52a641cfe0f3)

An essay about what the right priorities should be when writing tests. 

[The Flavor of Mint](https://medium.com/@robert.f.murdock/the-flavor-of-mint-fe3f27dbfd53)

An essay about an attempt to embed those priorities in the test code itself.

## Acknowledgements

Logo courtesy of Abs King!
