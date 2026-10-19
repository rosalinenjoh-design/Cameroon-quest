package com.cameroonquest.app.domain.model

/** A bilingual quiz question. [characterId] links it to the region it is about (null for national questions). */
data class QuizQuestion(
    val id: String,
    val characterId: String?,
    val question: LocalizedText,
    val options: List<LocalizedText>,
    val correct: Int,
    val fact: LocalizedText,
    /** Key of an optional illustration; the user interface decides which picture it means. */
    val pictureKey: String? = null,
) {
    init {
        require(options.size == OPTION_COUNT) { "A question has exactly $OPTION_COUNT options." }
        require(correct in options.indices) { "The correct answer must be one of the options." }
    }

    private companion object {
        const val OPTION_COUNT = 4
    }
}

/** Illustration keys shared by the content and the user interface. */
object PictureKeys {
    const val GRASSFIELDS_HOUSE = "grassfields_house"
}
