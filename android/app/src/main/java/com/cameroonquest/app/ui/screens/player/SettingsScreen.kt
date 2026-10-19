package com.cameroonquest.app.ui.screens.player

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
import androidx.compose.material.icons.rounded.Translate
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cameroonquest.app.R
import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.Language
import com.cameroonquest.app.ui.components.Avatar
import com.cameroonquest.app.ui.components.BottomKente
import com.cameroonquest.app.ui.components.ChoiceChip
import com.cameroonquest.app.ui.components.HeaderBar
import com.cameroonquest.app.ui.components.QuestButton
import com.cameroonquest.app.ui.components.QuestDialog
import com.cameroonquest.app.ui.components.WhiteCard
import com.cameroonquest.app.ui.screens.onboarding.NameDialog
import com.cameroonquest.app.ui.style.CharacterStyles
import com.cameroonquest.app.ui.style.GameStyles
import com.cameroonquest.app.ui.style.LanguageLabels
import com.cameroonquest.app.ui.style.RegionLabels
import com.cameroonquest.app.ui.theme.Quest
import com.cameroonquest.app.ui.viewmodel.PlayerUiState

@Composable
fun SettingsScreen(
    state: PlayerUiState,
    language: Language,
    onBack: () -> Unit,
    onRename: (String) -> Unit,
    onChangeCharacter: () -> Unit,
    onHaptics: (Boolean) -> Unit,
    onLanguage: (Language) -> Unit,
    onHelp: () -> Unit,
    onReset: () -> Unit,
) {
    var renaming by rememberSaveable { mutableStateOf(false) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    val profile = state.profile
    val character = state.character
    val style = CharacterStyles.of(character)
    Column(
        Modifier
            .fillMaxSize()
            .background(Quest.Parchment),
    ) {
        HeaderBar(stringResource(R.string.settings), onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionTitle(stringResource(R.string.player_section))
            WhiteCard(Modifier.fillMaxWidth()) {
                SettingRow(Icons.Rounded.Person, stringResource(R.string.name), profile.name, onClick = { renaming = true })
                HorizontalDivider(Modifier.padding(vertical = 4.dp), color = Quest.Line)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(role = Role.Button, onClick = onChangeCharacter)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(style.avatar, 40.dp, ring = style.color, ringWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.character), color = Quest.Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            stringResource(R.string.character_line, character.name, stringResource(RegionLabels.of(character.region))),
                            color = Quest.InkSoft,
                            fontSize = 13.sp,
                        )
                    }
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = Quest.InkSoft)
                }
            }
            SectionTitle(stringResource(R.string.preferences))
            WhiteCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RoundIcon(Icons.Rounded.Vibration, Quest.Teal)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.vibration), color = Quest.Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(R.string.vibration_detail), color = Quest.InkSoft, fontSize = 13.sp)
                    }
                    Switch(
                        checked = state.haptics,
                        onCheckedChange = onHaptics,
                        colors = SwitchDefaults.colors(checkedTrackColor = Quest.Green),
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Quest.Line)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RoundIcon(Icons.Rounded.Translate, Quest.Teal)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.language), color = Quest.Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(R.string.language_detail), color = Quest.InkSoft, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Language.entries.forEach { option ->
                        ChoiceChip(
                            stringResource(LanguageLabels.of(option)),
                            option == language,
                            { if (option != language) onLanguage(option) },
                            Modifier.weight(1f),
                        )
                    }
                }
            }
            SectionTitle(stringResource(R.string.help))
            WhiteCard(Modifier.fillMaxWidth()) {
                SettingRow(
                    Icons.AutoMirrored.Rounded.HelpOutline,
                    stringResource(R.string.how_to_play),
                    stringResource(R.string.how_to_play_detail),
                    onClick = onHelp,
                )
            }
            SectionTitle(stringResource(R.string.data_section))
            WhiteCard(Modifier.fillMaxWidth()) {
                SettingRow(
                    Icons.Rounded.Delete,
                    stringResource(R.string.reset_progress),
                    stringResource(R.string.reset_detail),
                    onClick = { confirmReset = true },
                    tint = Quest.Wrong,
                )
            }
            SectionTitle(stringResource(R.string.about))
            WhiteCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.about_version), color = Quest.Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.about_text), color = Quest.InkSoft, fontSize = 13.sp, lineHeight = 18.sp)
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
            Text(stringResource(R.string.reset_title), color = Quest.Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(stringResource(R.string.reset_message), color = Quest.InkSoft, fontSize = 15.sp, textAlign = TextAlign.Center)
            QuestButton(
                stringResource(R.string.reset),
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
            TextButton(onClick = { confirmReset = false }) {
                Text(stringResource(R.string.cancel), color = Quest.InkSoft, fontSize = 16.sp)
            }
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
fun HelpScreen(xpPerLevel: Int, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Quest.Parchment),
    ) {
        HeaderBar(stringResource(R.string.help), onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WhiteCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.help_welcome_title), color = Quest.Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.help_welcome_text, xpPerLevel), color = Quest.Ink, fontSize = 14.sp, lineHeight = 20.sp)
            }
            GameKind.entries.forEach { kind ->
                val style = GameStyles.of(kind)
                WhiteCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painterResource(style.image),
                            contentDescription = null,
                            modifier = Modifier
                                .size(width = 84.dp, height = 50.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(stringResource(style.title), color = Quest.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(stringResource(style.origin), color = Quest.InkSoft, fontSize = 13.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(style.rules), color = Quest.Ink, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
            WhiteCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.rewards), color = Quest.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.rewards_text), color = Quest.Ink, fontSize = 14.sp, lineHeight = 20.sp)
            }
            WhiteCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.two_players_title), color = Quest.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.two_players_text), color = Quest.Ink, fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
        BottomKente()
    }
}
