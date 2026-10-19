package com.cameroonquest.app.ui

import com.cameroonquest.app.data.content.LocalCharacterCatalog
import com.cameroonquest.app.data.content.LocalQuestionBank
import com.cameroonquest.app.domain.model.Difficulty
import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.GameSetup
import com.cameroonquest.app.domain.model.Outcome
import com.cameroonquest.app.domain.model.RoundResult
import com.cameroonquest.app.domain.model.RoundReward
import com.cameroonquest.app.ui.style.CharacterStyles
import com.cameroonquest.app.ui.style.GameStyles
import com.cameroonquest.app.ui.style.QuizPictures
import com.cameroonquest.app.ui.viewmodel.RoundPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UiMappingTest {
    @Test
    fun everyCharacterHasArtwork() {
        LocalCharacterCatalog.characters.forEach { assertTrue(it.id, CharacterStyles.has(it.id)) }
    }

    @Test
    fun everyPictureKeyAndRivalExists() {
        LocalQuestionBank.questions.mapNotNull { it.pictureKey }.forEach { assertNotNull(it, QuizPictures.of(it)) }
        GameKind.entries.forEach { assertNotNull(it.name, LocalCharacterCatalog.find(GameStyles.of(it).rivalId)) }
    }

    @Test
    fun roundPhasesOnlyAllowValidTransitions() {
        assertEquals(RoundPhase.Playing, RoundPhase.Ready.start())
        assertEquals(RoundPhase.Ready, RoundPhase.Ready.pause())
        assertEquals(RoundPhase.Paused, RoundPhase.Playing.pause())
        assertEquals(RoundPhase.Playing, RoundPhase.Paused.resume())
        assertEquals(RoundPhase.Paused, RoundPhase.Paused.start())

        val finished = RoundPhase.Finished(
            RoundResult(GameSetup(GameKind.DOCHI, Difficulty.EASY), 120, Outcome.LOSS),
            RoundReward(xp = 10, score = 120, newLevel = null, newBest = true, newBadges = emptyList()),
        )
        assertEquals(finished, finished.resume())
        assertTrue(RoundPhase.Playing.acceptsMoves)
        listOf(RoundPhase.Ready, RoundPhase.Paused, finished).forEach { assertFalse(it.acceptsMoves) }
    }
}
