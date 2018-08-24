import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
buildscript {

}
plugins {
    java
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "1.2.61"
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.10.0"
    id("com.github.hierynomus.license") version "0.14.0"

}

group = "org.spongepowered"
version = "0.9.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

gradlePlugin {
    plugins {
        create("sponge-gradle-plugin") {
            id = "sponge-gradle-plugin"
            implementationClass = "org.spongepowered.gradle.SpongeGradlePlugin"
        }
    }
}


repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven")
}

tasks.getByName<KotlinCompile>("compileKotlin").kotlinOptions.jvmTarget = "1.8"

tasks.getByName<KotlinCompile>("compileTestKotlin").kotlinOptions.jvmTarget = "1.8"

dependencies {
    compileOnly(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testCompile("junit", "junit", "4.12")
}

pluginBundle {
    website = "https://www.spongepowered.org/"
    vcsUrl = "https://github.com/SpongePowered/SpongeGradle/"
    tags = listOf("minecraft", "sponge")
    (plugins) {
        "sponge-gradle-plugin" {
            // id is captured from gradlePlugin extension block
            displayName = "spongegradle"
            description = "Sponge Plugin Gradle integration"
            version = project.version as String
        }
    }
}
