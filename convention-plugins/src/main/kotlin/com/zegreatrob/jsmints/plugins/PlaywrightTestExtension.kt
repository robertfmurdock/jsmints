package com.zegreatrob.jsmints.plugins

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class PlaywrightTestExtension @Inject constructor(objects: ObjectFactory) {
    abstract val baseUrl: Property<String>
    abstract val browser: Property<String>
    abstract val headless: Property<Boolean>
    abstract val reuseBrowser: Property<Boolean>
    abstract val timeoutMillis: Property<Int>
    abstract val configDirectory: DirectoryProperty

    val profiles: NamedDomainObjectContainer<PlaywrightProfile> = objects.domainObjectContainer(PlaywrightProfile::class.java)
}

abstract class PlaywrightProfile @Inject constructor(private val profileName: String) : Named {
    override fun getName() = profileName

    abstract val browser: Property<String>
    abstract val baseUrl: Property<String>
    abstract val headless: Property<Boolean>
    abstract val reuseBrowser: Property<Boolean>
    abstract val timeoutMillis: Property<Int>
}
