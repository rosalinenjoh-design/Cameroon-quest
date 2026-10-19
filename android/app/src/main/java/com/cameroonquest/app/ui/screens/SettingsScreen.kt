package com.cameroonquest.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cameroonquest.app.data.Characters
import com.cameroonquest.app.data.GameKind
import com.cameroonquest.app.data.PlayerProfile
import com.cameroonquest.app.data.XP_PER_LEVEL
import com.cameroonquest.app.ui.components.Avatar
import com.cameroonquest.app.ui.components.BottomKente
import com.cameroonquest.app.ui.components.HeaderBar
import com.cameroonquest.app.ui.components.QuestButton
import com.cameroonquest.app.ui.components.QuestDialog
import com.cameroonquest.app.ui.components.WhiteCard
import com.cameroonquest.app.ui.theme.Quest

@Composable
fun SettingsScreen(
    profile: PlayerProfile,
    onBack: () -> Unit,
    onRename: (String) -> Unit,
    onChangeCharacter: () -> Unit,
    onHaptics: (Boolean) -> Unit,
    onHelp: () -> Unit,
    onReset: () -> Unit,
) {
    var renaming by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    val character = Characters.orDefault(profile.characterId)
    Column(
        Modifier
            .fillMaxSize()
            .background(Quest.Parchment),
    ) {
        HeaderBar("Settings", onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionTitle("Player")
            WhiteCard(Modifier.fillMaxWidth()) {
                SettingRow(Icons.Rounded.Person, "Name", profile.name, onClick = { renaming = true })
                HorizontalDivider(Modifier.padding(vertical = 4.dp), color = Quest.Line)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(role = Role.Button, onClick = onChangeCharacter)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(character.avatar, 40.dp, ring = character.color, ringWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Character", color = Quest.Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text("${character.name} \u00B7 ${character.region}", color = Quest.InkSoft, fontSize = 13.sp)
                    }
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = Quest.InkSoft)
                }
            }
            SectionTitle("Preferences")
            WhiteCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RoundIcon(Icons.Rounded.Vibration, Quest.Teal)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Vibration", color = Quest.Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text("Short vibrations on taps and game events.", color = Quest.InkSoft, fontSize = 13.sp)
                    }
                    Switch(
                        checked = profile.haptics,
                        onCheckedChange = onHaptics,
                        colors = SwitchDefaults.colors(checkedTrackColor = Quest.Green),
                    )
                }
            }
            SectionTitle("Help")
            WhiteCard(Modifier.fillMaxWidth()) {
                SettingRow(Icons.AutoMirrored.Rounded.HelpOutline, "How to play", "Rules of every game and how rewards work.", onClick = onHelp)
            }
            SectionTitle("Data")
            WhiteCard(Modifier.fillMaxWidth()) {
                SettingRow(
                    Icons.Rounded.Delete,
                    "Reset progress",
                    "Deletes your name, character, XP and scores from this phone.",
                    onClick = { confirmReset = true },
                    tint = Quest.Wrong,
                )
            }
            SectionTitle("About")
            WhiteCard(Modifier.fillMaxWidth()) {
                Text("Cameroon Quest 1.0", color = Quest.Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "Progress is saved only on this phone. Artwork comes from the Cameroon Quest design mockup. " +
                        "Font: Fredoka, SIL Open Font License 1.1.",
                    color = Quest.InkSoft,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }
        }
        BottomKente()
    }

    if (renaming) {
        NameDialog(profile.name, onDismiss = { renaming = false }) {
            onRename(it)
            renaming = false
        }
    }
    if (confirmReset) {
        QuestDialog({ confirmReset = false }) {
            Text("Reset all progress?", color = Quest.Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(
                "Your name, character, XP, scores and badges will be deleted from this phone. This cannot be undone.",
                color = Quest.InkSoft,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
            QuestButton(
                "Reset",
                {
                    confirmReset = false
                    onReset()
                },
                Modifier.fillMaxWidth(),
                top = Color(0xFFE5564B),
                bottom = Color(0xFFA7231B),
                height = 52.dp,
                fontSize = 21.sp,
            )
            TextButton(onClick = { confirmReset = false }) { Text("Cancel", color = Quest.InkSoft, fontSize = 16.sp) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, Modifier.padding(start = 4.dp, top = 6.dp), color = Quest.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun RoundIcon(icon: ImageVector, tint: Color) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, detail: String?, onClick: () -> Unit, tint: Color = Quest.Teal) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundIcon(icon, tint)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = if (tint == Quest.Wrong) Quest.Wrong else Quest.Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (detail != null) Text(detail, color = Quest.InkSoft, fontSize = 13.sp)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = Quest.InkSoft)
    }
}

@Composable
fun HelpScreen(onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Quest.Parchment),
    ) {
        HeaderBar("Help", onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WhiteCard(Modifier.fillMaxWidth()) {
                Text("Welcome to Cameroon Quest!", color = Quest.Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Choose a character, play traditional games and answer cultural questions. Every finished game gives " +
                        "you XP ($XP_PER_LEVEL XP = 1 level) and adds to your total score on the leaderboard.",
                    color = Quest.Ink,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            }
            GameKind.entries.forEach { game ->
                WhiteCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painterResource(gameImage(game)),
                            contentDescription = null,
                            modifier = Modifier
                                .size(width = 84.dp, height = 50.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(game.title, color = Quest.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(game.origin, color = Quest.InkSoft, fontSize = 13.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(GameTexts.rules(game), color = Quest.Ink, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
            WhiteCard(Modifier.fillMaxWidth()) {
                Text("Rewards", color = Quest.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Win: 50 XP \u00B7 Draw: 25 XP \u00B7 Loss: 10 XP. In the quiz, each correct answer gives 10 XP and 50 points.",
                    color = Quest.Ink,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            }
            WhiteCard(Modifier.fillMaxWidth()) {
                Text("Two players", color = Quest.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Songo and Pirogue Racing can be played by two people on the same phone. Only your own progress is saved.",
                    color = Quest.Ink,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            }
        }
        BottomKente()
    }
}
