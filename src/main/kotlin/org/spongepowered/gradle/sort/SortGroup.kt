package org.spongepowered.gradle.sort

open class SortGroup(val name: String) {

    var files: MutableList<String> = mutableListOf()


    fun add(className: String) {
        files + className
    }

}