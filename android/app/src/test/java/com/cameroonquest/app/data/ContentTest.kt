package com.cameroonquest.app.data

import com.cameroonquest.app.data.content.LocalCharacterCatalog
import com.cameroonquest.app.data.content.LocalQuestionBank
import com.cameroonquest.app.domain.model.Language
import com.cameroonquest.app.domain.model.LocalizedText
import com.cameroonquest.app.domain.model.PlayerProfile
import com.cameroonquest.app.domain.model.Region
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ContentTest {
    private fun LocalizedText.isTranslated() = english.isNotBlank() && french.isNotBlank()

    @Test
    fun tenCharactersCoverTheTenRegions() {
        val characters = LocalCharacterCatalog.characters
        assertEquals(10, characters.size)
        assertEquals(characters.size, characters.map { it.id }.toSet().size)
        assertEquals(Region.entries.toSet(), characters.map { it.region }.toSet())
        characters.forEach { assertTrue(it.id, it.quote.isTranslated() && it.about.isTranslated()) }
    }

    @Test
    fun everyQuestionIsWellFormedInBothLanguages() {
        val questions = LocalQuestionBank.questions
        assertEquals(questions.size, questions.map { it.id }.toSet().size)
        questions.forEach { question ->
            assertEquals(question.id, 4, question.options.map { it.english }.toSet().size)
            assertEquals(question.id, 4, question.options.map { it.french }.toSet().size)
            assertTrue(question.id, question.question.isTranslated() && question.fact.isTranslated())
            assertTrue(question.id, question.characterId == null || LocalCharacterCatalog.find(question.characterId) != null)
        }
    }

    @Test
    fun quizRoundsUseDistinctQuestions() {
        val round = LocalQuestionBank.drawRound(10, Random(7))
        assertEquals(10, round.size)
        assertEquals(10, round.map { it.id }.toSet().size)
    }

    @Test
    fun leaderboardSharesTiedRanksAndListsThePlayerOnce() {
        val profile = PlayerProfile(name = "Alex", characterId = "njoh", totalScore = 7450)
        val board = ChampionLeaderboard(LocalCharacterCatalog).standings(profile)
        assertEquals(10, board.size)
        assertFalse(board.any { !it.isPlayer && it.character.id == "njoh" })
        val me = board.single { it.isPlayer }
        val wouri = board.single { it.character.id == "wouri" }
        assertEquals(2, me.rank)
        assertEquals(wouri.rank, me.rank)
        assertTrue(board.indexOf(me) > board.indexOf(wouri))
        assertEquals(1, board.first().rank)
    }

    @Test
    fun theRivalIsNeverThePlayersOwnCharacter() {
        val njoh = LocalCharacterCatalog.findOrDefault("njoh")
        assertEquals("bakama", LocalCharacterCatalog.rivalOf(njoh, "bakama").id)
        assertFalse(LocalCharacterCatalog.rivalOf(njoh, "njoh").id == "njoh")
        assertNotNull(LocalCharacterCatalog.find("lea"))
    }

    @Test
    fun languagesAreReadFromLocaleTags() {
        assertEquals(Language.FRENCH, Language.fromTag("fr-CM"))
        assertEquals(Language.ENGLISH, Language.fromTag("en"))
        assertEquals(Language.ENGLISH, Language.fromTag("de"))
        assertEquals(Language.ENGLISH, Language.fromTag(null))
        assertEquals("Bonjour", LocalizedText("Hello", "Bonjour").resolve(Language.FRENCH))
    }
}
