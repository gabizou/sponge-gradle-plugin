package org.spongepowered.gradle.sort

import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.impldep.com.google.common.io.Files
import org.spongepowered.gradle.util.TextConstants
import java.io.File
import java.nio.charset.Charset
import java.util.*
import javax.inject.Inject

open class SortClassFieldsTask @Inject constructor() : DefaultTask() {

    @Input
    fun getFiles() : List<File> {
        val groups = project.extensions.getByType(SortFieldsExtension::class.java).group.toList()
        groups.forEach { group ->
            group.files.forEach { pqClass ->
                add(group.name.replace(".", File.separator) + File.separator + pqClass.replace(".", File.separator))
            }
        }
        return targetFiles
    }

    @Internal
    val targetFiles : MutableList<File> = mutableListOf()


    fun add(file : File) {
        this.targetFiles + file
    }

    /**
     * Add a resource for processing, the resource name should be a
     * fully-qualified class name.
     *
     * @param resourceName Resource to add
     */
    fun add(resourceName: String) {
        this.add("main", resourceName)
    }

    /**
     * Add a resource for processing, the resource name should be a
     * fully-qualified class name.
     *
     * @param sourceSetName Sourceset to use
     * @param resourceName Resource to add
     */
    fun add(sourceSetName : String, resourceName : String) {
        val java = project.convention.getPlugin(JavaPluginConvention::class.java)
        val sourceSet : SourceSet? = java.sourceSets.findByName(sourceSetName)
        when (sourceSet) {
            is SourceSet -> this.add(sourceSet, resourceName)
            else -> throw InvalidUserDataException("Could not find specified sourceSet '${sourceSetName} for task")
        }
    }

    /**
     * Add a resource for processing, the resource name should be a
     * fully-qualified class name.
     *
     * @param sourceSet Sourceset to use
     * @param resourceName Resource to add
     */
    fun add(sourceSet : SourceSet, resourceName : String) {

        if (resourceName.isEmpty()) {
            throw InvalidUserDataException("$resourceName is not a valid resource name")
        }

        var foundResource = false
        val resourceFileName = String.format("%s.java", resourceName.replace(".", File.separator))

        sourceSet.allJava.srcDirs.forEach {
            srcDir ->
            val sourceFile = File(resourceFileName)
            sourceFile.resolve(srcDir)
            if (sourceFile.exists()) {
                foundResource = true
                targetFiles + sourceFile
            }
        }

        if (!foundResource) {
            throw InvalidUserDataException("$resourceName could not be found")
        }
    }

    /**
     * Main task action, sort added files
     */
    @TaskAction
    fun sortFiles() {
        for (file in targetFiles) {
            this.sortFile(file)
        }
    }

    /**
     * Sort a class file
     *
     * @param file File to sort
     */
    private fun sortFile(file : File) {
        if (!file.exists()) {
            return
        }

        project.logger.lifecycle("Sorting fields in: {}", file)

        // Flag switched by the processing semaphore
        var active = false

        // File content for output
        var output = ""

        // Sorted field set
        val fields = TreeSet<Field>()

        // Current field being accumulated
        var current = Field()
        Files.readLines(file, Charset.defaultCharset()).forEach { line ->
            if (!current.initializer.isEmpty()) {
                if (current.initializer.contains(";")) {
                    if (!current.type.isEmpty()) {
                        fields + current
                    } else {
                        output = StringBuffer(output).append(current.flush()).toString()
                    }
                    current = Field()
                } else {
                    current.initializer = StringBuffer(current.initializer).append(TextConstants.newLine).append(line).toString()
                }
            }
            val semaphore = TextConstants.semaphores.matchEntire(line)
            when (semaphore) {
                is MatchResult ->  {
                    if ("OFF" == semaphore.groups[1]!!.value) {
                        fields.forEach { field ->
                            output = StringBuffer(output).append(TextConstants.newLine).append(field).append(TextConstants.newLine).toString()
                        }
                        if (fields.isNotEmpty()) {
                            output = StringBuffer(output).append(TextConstants.newLine).toString()
                        }
                        fields.clear()
                    }
                    active = "ON" == semaphore.groups[1]!!.value
                    output = StringBuffer(output).append(TextConstants.newLine).append(line).append(TextConstants.newLine).toString()
                    return@forEach
                }
            }
            if (!active) {
                output = StringBuffer(output).append(TextConstants.newLine).append(line).append(TextConstants.newLine).toString()
                return@forEach
            } else if (line.isNotEmpty()) {
                return@forEach
            }

            val matched = TextConstants.modifiers.matchEntire(line)
            when (matched) {
                is MatchResult -> { // found a field declaration
                    current.modifiers = matched.groups[0]!!.value
                    val assignedPos = line.indexOf("=")
                    val typeAndName = line.substring(current.modifiers.length, assignedPos)
                    current.initializer = line.substring(assignedPos)
                    val idMatch = TextConstants.identifier.matchEntire(typeAndName)
                    when (idMatch) {
                        is MatchResult -> {
                            current.type = idMatch.groups[1]!!.value
                            current.name = idMatch.groups[2]!!.value
                        }
                    }
                }
                else -> {
                    current.comment += line
                }
            }



        }
        // Flush any remaining accumulated content
        if (current.isHasContent()) {
            output = StringBuffer(output).append(current.flush()).toString()
        }

        Files.write(output, file, Charset.defaultCharset())
    }
}

