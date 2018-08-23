package org.spongepowered.gradle.sort

import org.spongepowered.gradle.util.TextConstants

/**
 * Field wrapper used to keep all of the component parts of a field
 * declaration together and allow them to be sorted based on name. Also
 * stores the field ordinal to preserve ordering in case natural ordering
 * fails.
 */
open class Field(
        /**
         * Comment lines, accumulated here until the field declaration is
         * located
         */
        var comment: Array<String> = arrayOf(),

        /**
         * Field modifiers, eg. public static final
         */
        var modifiers: String = "",
        /**
         * Field type, basically whatever is between the modifiers and the field
         * name
         */
        var type: String = "",
        /**
         * Field name
         */
        var name: String = "",
        /**
         * Field initialiser, basically whatever is between the field name and
         * the end of the line
         */
        var initializer: String = "",
        /**
         * Field ordinal
         */
        val index: Int = Indexes.index++
) : Comparable<Field> {

    fun isHasContent() : Boolean {
        return comment.isNotEmpty()
    }

    fun isValid() {
        !modifiers.isEmpty() && !type.isEmpty() && !name.isEmpty() && !initializer.isEmpty()
    }
    override fun compareTo(other: Field): Int {
        val diff : Int = this.name.compareTo(other.name)
        return if (diff == 0) index - other.index else diff
    }


    /**
     * Returns accumulated field comments as a String. In actual fact we
     * accumulate everything we don't recognise as a field in the "comments"
     * for the field, and this simply returns accumulated content
     *
     * @return
     */
    fun flush() : String
    {
        var commentBlock = ""
        for (commentLine in comment) {
            commentBlock = StringBuffer(commentBlock).append(commentLine).append(TextConstants.newLine).toString()
        }
        return commentBlock
    }

    override fun toString() : String
    {
        return StringBuffer(this.flush()).append(modifiers).append(type).append(name).append(initializer).toString()
    }


    object Indexes {
        /**
         * Next field ordinal
         */
        var index: Int = 0
    }
}