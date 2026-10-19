package com.cameroonquest.app.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Terrain
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.cameroonquest.app.R

enum class Skill(val label: String, val icon: ImageVector) {
    Speed("Speed", Icons.Rounded.DirectionsRun),
    Focus("Focus", Icons.Rounded.GpsFixed),
    Teamwork("Teamwork", Icons.Rounded.Groups),
    Strategy("Strategy", Icons.Rounded.Psychology),
    Wisdom("Wisdom", Icons.Rounded.Lightbulb),
    Patience("Patience", Icons.Rounded.Timer),
    Endurance("Endurance", Icons.Rounded.FitnessCenter),
    Creativity("Creativity", Icons.Rounded.Palette),
    Rhythm("Rhythm", Icons.Rounded.MusicNote),
    Courage("Courage", Icons.Rounded.Shield),
    Leadership("Leadership", Icons.Rounded.Flag),
    Agility("Agility", Icons.Rounded.Bolt),
}

data class QuestCharacter(
    val id: String,
    val name: String,
    val region: String,
    val color: Color,
    val portrait: Int,
    val avatar: Int,
    val backdrop: Int,
    val icon: ImageVector,
    val quote: String,
    val about: String,
    val skills: List<Skill>,
    /** Fixed score of this character on the offline champions leaderboard. */
    val championScore: Int,
    val darkText: Boolean = false,
    /** Full-height artwork; only Njoh has one in the mockup. */
    val detailArt: Int? = null,
) {
    val onColor: Color get() = if (darkText) Color(0xFF2A1E14) else Color.White
}

object Characters {
    val all = listOf(
        QuestCharacter(
            id = "njoh", name = "Njoh", region = "Littoral", color = Color(0xFF026CC2),
            portrait = R.drawable.char_njoh, avatar = R.drawable.avatar_njoh, backdrop = R.drawable.charbg_njoh,
            icon = Icons.Rounded.Waves,
            quote = "The ocean is my home, and my culture my strength.",
            about = "Njoh comes from the Littoral region. He loves the sea, traditional music and the Sawa culture. " +
                "He is curious, brave and always ready for new adventures!",
            skills = listOf(Skill.Speed, Skill.Focus, Skill.Teamwork),
            championScore = 7890, detailArt = R.drawable.detail_njoh,
        ),
        QuestCharacter(
            id = "bakama", name = "Bakama", region = "Centre", color = Color(0xFFFBC024),
            portrait = R.drawable.char_bakama, avatar = R.drawable.avatar_bakama, backdrop = R.drawable.charbg_bakama,
            icon = Icons.Rounded.AccountBalance,
            quote = "Patience and strategy win every Songo match.",
            about = "Bakama grew up near Yaoundé in the Centre region. She learned Songo from her grandmother " +
                "and loves Bikutsi rhythms. She is clever, calm and never gives up.",
            skills = listOf(Skill.Strategy, Skill.Wisdom, Skill.Patience),
            championScore = 8560, darkText = true,
        ),
        QuestCharacter(
            id = "mbororo", name = "Mbororo", region = "Far North", color = Color(0xFF9AD4F9),
            portrait = R.drawable.char_mbororo, avatar = R.drawable.avatar_mbororo, backdrop = R.drawable.charbg_mbororo,
            icon = Icons.Rounded.WbSunny,
            quote = "The savanna wind carries my stories.",
            about = "Mbororo comes from the Far North, land of wide savannas and Waza National Park. " +
                "He knows the herds, the stars and the long roads of the Sahel. He is strong, patient and wise.",
            skills = listOf(Skill.Endurance, Skill.Wisdom, Skill.Focus),
            championScore = 6310, darkText = true,
        ),
        QuestCharacter(
            id = "wouri", name = "Wouri", region = "North West", color = Color(0xFF8A23C8),
            portrait = R.drawable.char_wouri, avatar = R.drawable.avatar_wouri, backdrop = R.drawable.charbg_wouri,
            icon = Icons.Rounded.Landscape,
            quote = "The hills of the Grassfields keep our traditions alive.",
            about = "Wouri lives in the North West highlands near Bamenda. She loves the bright toghu cloth, " +
                "traditional dances and the cool mountain air. She is creative, joyful and a great storyteller.",
            skills = listOf(Skill.Creativity, Skill.Rhythm, Skill.Teamwork),
            championScore = 7450,
        ),
        QuestCharacter(
            id = "nkem", name = "Nkem", region = "West", color = Color(0xFF4C9B2E),
            portrait = R.drawable.char_nkem, avatar = R.drawable.avatar_nkem, backdrop = R.drawable.charbg_nkem,
            icon = Icons.Rounded.Park,
            quote = "Respect the elders, honour the chiefdom.",
            about = "Nkem comes from the West region, home of the Bamileke chiefdoms and their tall thatched houses. " +
                "He loves masks, markets and the indigo Ndop cloth. He is respectful, hard-working and proud of his roots.",
            skills = listOf(Skill.Strategy, Skill.Wisdom, Skill.Teamwork),
            championScore = 5780,
        ),
        QuestCharacter(
            id = "yeme", name = "Yeme", region = "South West", color = Color(0xFFC72A5F),
            portrait = R.drawable.char_yeme, avatar = R.drawable.avatar_yeme, backdrop = R.drawable.charbg_yeme,
            icon = Icons.Rounded.Terrain,
            quote = "I climb Mount Cameroon with a smile.",
            about = "Yeme lives in Buea, at the foot of Mount Cameroon, and cheers the runners of the Race of Hope every year. " +
                "She loves the green tea fields and the black sand beaches of Limbe. She is energetic, kind and brave.",
            skills = listOf(Skill.Speed, Skill.Endurance, Skill.Courage),
            championScore = 5410,
        ),
        QuestCharacter(
            id = "zanga", name = "Zanga", region = "Adamawa", color = Color(0xFFED621D),
            portrait = R.drawable.char_zanga, avatar = R.drawable.avatar_zanga, backdrop = R.drawable.charbg_zanga,
            icon = Icons.Rounded.Grass,
            quote = "From the plateau, I can see the whole country.",
            about = "Zanga comes from the Adamawa plateau around Ngaoundéré, known as Cameroon's water tower. " +
                "He loves horse riding, cattle markets and the Lamido's palace. He is calm, fair and a natural leader.",
            skills = listOf(Skill.Leadership, Skill.Focus, Skill.Endurance),
            championScore = 5120,
        ),
        QuestCharacter(
            id = "tenda", name = "Ténda", region = "East", color = Color(0xFF05BA8D),
            portrait = R.drawable.char_tenda, avatar = R.drawable.avatar_tenda, backdrop = R.drawable.charbg_tenda,
            icon = Icons.Rounded.Eco,
            quote = "The forest is alive, and it teaches me every day.",
            about = "Ténda lives in the East, the largest region of Cameroon, covered by deep rainforest. " +
                "She knows the plants and the songs of the forest. She is gentle, observant and very brave.",
            skills = listOf(Skill.Focus, Skill.Wisdom, Skill.Agility),
            championScore = 6020,
        ),
        QuestCharacter(
            id = "foufou", name = "Foufou", region = "North", color = Color(0xFFDC1B20),
            portrait = R.drawable.char_foufou, avatar = R.drawable.avatar_foufou, backdrop = R.drawable.charbg_foufou,
            icon = Icons.Rounded.Pets,
            quote = "Like the Bénoué river, I never stop moving.",
            about = "Foufou comes from Garoua in the North region, on the banks of the Bénoué river. " +
                "He loves football, fishing and the colourful markets of the Sahel. He is fast, funny and full of energy.",
            skills = listOf(Skill.Speed, Skill.Agility, Skill.Teamwork),
            championScore = 6720,
        ),
        // The mockup labels Léa "Grand Nord", which is not one of the ten regions; South is the missing one.
        QuestCharacter(
            id = "lea", name = "Léa", region = "South", color = Color(0xFF395FE8),
            portrait = R.drawable.char_lea, avatar = R.drawable.avatar_lea, backdrop = R.drawable.charbg_lea,
            icon = Icons.Rounded.BeachAccess,
            quote = "Between forest and sea, I find my rhythm.",
            about = "Léa comes from the South region, between the rainforest and the beaches of Kribi, where the Lobé Falls " +
                "meet the ocean. She loves cocoa farms, music and dancing. She is joyful, curious and loves to learn.",
            skills = listOf(Skill.Rhythm, Skill.Creativity, Skill.Agility),
            championScore = 6980,
        ),
    )

    fun byId(id: String?): QuestCharacter? = all.firstOrNull { it.id == id }

    fun orDefault(id: String?): QuestCharacter = byId(id) ?: all.first()

    /** A computer or second-player opponent who is never the player's own character. */
    fun rivalOf(player: QuestCharacter, preferred: String = "bakama"): QuestCharacter =
        byId(preferred)?.takeIf { it.id != player.id } ?: all.first { it.id != player.id }
}

data class RankEntry(
    val rank: Int,
    val name: String,
    val character: QuestCharacter,
    val score: Int,
    val isPlayer: Boolean,
)

/** Ranks the player against the nine other regional champions, with shared ranks for ties. */
fun championBoard(playerName: String, playerCharacterId: String?, playerScore: Int): List<RankEntry> {
    val mine = Characters.orDefault(playerCharacterId)
    val entries = Characters.all.filter { it.id != mine.id }.map { RankEntry(0, it.name, it, it.championScore, false) } +
        RankEntry(0, playerName.ifBlank { "You" }, mine, playerScore, true)
    val sorted = entries.sortedWith(compareByDescending<RankEntry> { it.score }.thenBy { it.isPlayer })
    return sorted.map { entry -> entry.copy(rank = 1 + sorted.count { it.score > entry.score }) }
}
