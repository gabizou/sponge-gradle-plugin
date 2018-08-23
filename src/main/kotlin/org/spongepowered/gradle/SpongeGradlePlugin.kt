package org.spongepowered.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.spongepowered.gradle.plugin.SpongePluginExtension
import org.spongepowered.gradle.sort.SortClassFieldsTask
import org.spongepowered.gradle.sort.SortFieldsExtension
import java.io.File

open class SpongeGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val plugin = project.extensions.create("sponge", SpongePluginExtension::class.java)
        if (plugin.applySpongeDependencies) {
            project.pluginManager.apply {
                apply("java")
                apply("eclipse")
                apply("idea")
            }
            project.convention.getPlugin(JavaPluginConvention::class.java).apply {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }
            project.repositories.apply {
                mavenLocal()
                mavenCentral()
                maven {
                    it.name = "sponge"
                    it.setUrl("https://repo.spongepowered.org/maven")
                }
            }
            project.plugins.getPlugin(IdeaPlugin::class.java).apply {
                model.module.inheritOutputDirs = false
            }
            project.plugins.apply {
//                apply(MetadataPlugin::class.java)
//                apply(SpongeBasePlugin::class.java)
            }
        }
        val sortFields = project.extensions.create("sortFields", SortFieldsExtension::class.java)
        project.tasks.create("sortClassFields", SortClassFieldsTask::class.java) {
            sortFields.classGroups.forEach { group ->
                group.pqClasses.forEach { pqClass ->
                    it.add(group.fqpackage.replace(".", File.separator) + File.separator + pqClass.replace(".", File.separator))
                }
            }

        }

    }
}