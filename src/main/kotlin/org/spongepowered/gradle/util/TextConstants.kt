package org.spongepowered.gradle.util

object TextConstants {
    /**
     * Platform-specific newline
     */
    val newLine = System.lineSeparator()
    /**
     * Regex for matching modifiers, used to identify actual field declarations
     */
    val modifiers = "^\\s*((public|protected|private|static|abstract|final|synchronized|transient|native|volatile)\\s+)+".toRegex()
    /**
     * Regex for matching identifiers, used to find the field name in the
     * declaration
     */
    val identifier = "^(.*?\\s)(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\s*)\$".toRegex()
    /**
     * Regex for matching the processing semaphores
     */
    val semaphores = "\\/\\/\\s*SORTFIELDS\\s*:\\s*(ON|OFF)".toRegex()
}