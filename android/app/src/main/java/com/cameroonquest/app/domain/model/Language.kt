package com.cameroonquest.app.domain.model

/** The two official languages of Cameroon. */
enum class Language(val tag: String) {
    ENGLISH("en"),
    FRENCH("fr");

    companion object {
        fun fromTag(tag: String?): Language =
            entries.firstOrNull { tag?.startsWith(it.tag, ignoreCase = true) == true } ?: ENGLISH
    }
}

/** Value object: the same content text in English and French. */
data class LocalizedText(val english: String, val french: String) {
    init {
        require(english.isNotBlank() && french.isNotBlank()) { "Both translations are required." }
    }

    fun resolve(language: Language): String = when (language) {
        Language.ENGLISH -> english
        Language.FRENCH -> french
    }
}
