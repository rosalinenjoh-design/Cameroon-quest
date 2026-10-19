package com.cameroonquest.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cameroonquest.app.R
import com.cameroonquest.app.ui.components.Avatar
import com.cameroonquest.app.ui.components.BottomKente
import com.cameroonquest.app.ui.components.CircleIconButton
import com.cameroonquest.app.ui.components.QuestButton
import com.cameroonquest.app.ui.components.SkyClouds
import com.cameroonquest.app.ui.components.SquareIconButton
import com.cameroonquest.app.ui.components.XpBar
import com.cameroonquest.app.ui.style.CharacterStyles
import com.cameroonquest.app.ui.theme.Quest
import com.cameroonquest.app.ui.theme.ShadowedText
import com.cameroonquest.app.ui.viewmodel.PlayerUiState

@Composable
fun HomeScreen(
    state: PlayerUiState,
    onPlay: () -> Unit,
    onProgress: () -> Unit,
    onLeaderboard: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
) {
    val profile = state.profile
    val level = state.level
    val style = CharacterStyles.of(state.character)
    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Quest.Jungle, Quest.JungleDark))),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Quest.Sky, Quest.SkyLight))),
        ) {
            SkyClouds(Modifier.matchParentSize())
            Row(
                Modifier
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 10.dp, top = 10.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(
                    style.avatar,
                    64.dp,
                    Modifier
                        .clip(CircleShape)
                        .clickable(onClickLabel = stringResource(R.string.open_profile), role = Role.Button, onClick = onProfile),
                    contentDescription = stringResource(R.string.your_character, state.character.name),
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.hello_name, profile.name),
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        style = ShadowedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        stringResource(R.string.level_number, level.level),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        style = ShadowedText,
                    )
                    Spacer(Modifier.height(5.dp))
                    XpBar(
                        level.progress,
                        Modifier.fillMaxWidth(0.88f),
                        height = 17.dp,
                        label = stringResource(R.string.xp_progress, profile.xp, level.nextLevelXp),
                    )
                }
                CircleIconButton(
                    Icons.Rounded.Settings,
                    stringResource(R.string.settings),
                    onSettings,
                    background = Color.Transparent,
                    border = null,
                    size = 46.dp,
                )
            }
        }
        Box {
            Image(
                painterResource(R.drawable.home_hero),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(636f / 330f),
                contentScale = ContentScale.Crop,
            )
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Quest.SkyLight,
                            0.1f to Color.Transparent,
                            0.8f to Color.Transparent,
                            1f to Quest.Jungle,
                        ),
                    ),
            )
        }
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 30.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            QuestButton(stringResource(R.string.play), onPlay, Modifier.fillMaxWidth(), icon = Icons.Rounded.SportsEsports)
            QuestButton(
                stringResource(R.string.my_progress), onProgress, Modifier.fillMaxWidth(),
                top = Quest.BlueTop, bottom = Quest.BlueDark, icon = Icons.Rounded.BarChart, fontSize = 22.sp,
            )
            QuestButton(
                stringResource(R.string.leaderboard), onLeaderboard, Modifier.fillMaxWidth(),
                top = Quest.PurpleTop, bottom = Quest.PurpleDark, icon = Icons.Rounded.EmojiEvents, fontSize = 22.sp,
            )
            Row(
                Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                SquareIconButton(Icons.Rounded.Person, stringResource(R.string.profile), onProfile)
                SquareIconButton(Icons.Rounded.Settings, stringResource(R.string.settings), onSettings)
                SquareIconButton(Icons.AutoMirrored.Rounded.HelpOutline, stringResource(R.string.help), onHelp)
            }
        }
        BottomKente()
    }
}
