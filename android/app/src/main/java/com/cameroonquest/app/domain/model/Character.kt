package com.cameroonquest.app.domain.model

enum class Region { LITTORAL, CENTRE, FAR_NORTH, NORTH_WEST, WEST, SOUTH_WEST, ADAMAWA, EAST, NORTH, SOUTH }

enum class Skill { SPEED, FOCUS, TEAMWORK, STRATEGY, WISDOM, PATIENCE, ENDURANCE, CREATIVITY, RHYTHM, COURAGE, LEADERSHIP, AGILITY }

/** A playable character; each one represents one region of Cameroon. */
data class QuestCharacter(
    val id: String,
    val name: String,
    val region: Region,
    val quote: LocalizedText,
    val about: LocalizedText,
    val skills: List<Skill>,
    /** Fixed score of this character on the offline champions leaderboard. */
    val championScore: Int,
) {
    init {
        require(id.isNotBlank() && name.isNotBlank()) { "A character needs an id and a name." }
        require(skills.size == SKILL_COUNT) { "Each character has exactly $SKILL_COUNT special skills." }
        require(championScore >= 0) { "A champion score cannot be negative." }
    }

    private companion object {
        const val SKILL_COUNT = 3
    }
}
