package org.spongepowered.gradle.sort

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

open class SortFieldsExtension(val group: NamedDomainObjectContainer<SortGroup>) {

    fun invoke(name: String, apply: Action<SortGroup>) {
        group.create(name).apply {
            apply.execute(this)
        }
    }
}