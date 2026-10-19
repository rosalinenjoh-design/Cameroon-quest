package com.cameroonquest.app.ui.screens.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cameroonquest.app.R
import com.cameroonquest.app.domain.model.Badge
import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.PlayerProfile
import com.cameroonquest.app.domain.model.RankEntry
import com.cameroonquest.app.ui.components.Avatar
import com.cameroonquest.app.ui.components.BottomKente
import com.cameroonquest.app.ui.components.HeaderBar
import com.cameroonquest.app.ui.components.RegionBadge
import com.cameroonquest.app.ui.components.WhiteCard
import com.cameroonquest.app.ui.components.XpBar
import com.cameroonquest.app.ui.style.BadgeStyles
import com.cameroonquest.app.ui.style.CharacterStyles
import com.cameroonquest.app.ui.style.GameStyles
import com.cameroonquest.app.ui.style.RegionLabels
import com.cameroonquest.app.ui.style.rememberNumberFormat
import com.cameroonquest.app.ui.theme.Quest
import com.cameroonquest.app.ui.theme.darken
import com.cameroonquest.app.ui.theme.lighten
import com.cameroonquest.app.ui.viewmodel.LeaderboardViewModel
import com.cameroonquest.app.ui.viewmodel.PlayerUiState
import java.text.NumberFormat

@Composable
fun ProfileScreen(
    state: PlayerUiState,
    onBack: () -> Unit,
    onChangeCharacter: () -> Unit,
    onProgress: () -> Unit,
    onLeaderboard: () -> Unit,
    onSettings: () -> Unit,
    onHome: () -> Unit,
    onGames: () -> Unit,
) {
    val profile = state.profile
    val level = state.level
    val character = state.character
    val style = CharacterStyles.of(character)
    val numbers = rememberNumberFormat()
    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Quest.Teal, Quest.TealDark))),
    ) {
        HeaderBar(stringResource(R.string.my_profile), onBack, brush = null)
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = 58.dp)
                    .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                    .background(Quest.Cream)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 18.dp, end = 18.dp, top = 66.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(profile.name, color = Quest.Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.level_number, level.level),
                        color = Quest.InkSoft,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                XpBar(level.progress, Modifier.fillMaxWidth(), height = 16.dp, track = Color(0xFFD9CAB0))
                Text(
                    stringResource(R.string.xp_progress, profile.xp, level.nextLevelXp),
                    color = Quest.Ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        Icons.Rounded.Star, Quest.YellowDark, stringResource(R.string.total_score),
                        numbers.format(profile.totalScore), Modifier.weight(1f),
                    )
                    StatTile(
                        Icons.Rounded.SportsEsports, Quest.BlueDark, stringResource(R.string.games_played),
                        numbers.format(profile.gamesPlayed), Modifier.weight(1f),
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    WhiteCard(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RegionBadge(style, 42.dp)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(stringResource(R.string.region), color = Quest.InkSoft, fontSize = 13.sp)
                                Text(
                                    stringResource(RegionLabels.of(character.region)),
                                    color = Quest.Ink,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    WhiteCard(Modifier.weight(1f)) {
                        Text(stringResource(R.string.badges), color = Quest.InkSoft, fontSize = 13.sp)
                        Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Badge.entries.forEach { BadgeMedal(it, it in profile.badges, 32.dp) }
                        }
                    }
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.5.dp, Quest.Line, RoundedCornerShape(20.dp)),
                ) {
                    MenuRow(Icons.Rounded.BarChart, stringResource(R.string.my_progress), onProgress)
                    HorizontalDivider(color = Quest.Line)
                    MenuRow(Icons.Rounded.Leaderboard, stringResource(R.string.leaderboard), onLeaderboard)
                    HorizontalDivider(color = Quest.Line)
                    MenuRow(Icons.Rounded.Settings, stringResource(R.string.settings), onSettings)
                }
            }
            Box(Modifier.align(Alignment.TopCenter)) {
                Avatar(
                    style.avatar,
                    112.dp,
                    ringWidth = 4.dp,
                    contentDescription = stringResource(R.string.your_character, character.name),
                )
                val changeLabel = stringResource(R.string.change_character)
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Quest.Teal)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable(onClickLabel = changeLabel, role = Role.Button, onClick = onChangeCharacter),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = changeLabel, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .background(Quest.TealDeep)
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            NavItem(Icons.Rounded.Home, stringResource(R.string.home), onHome)
            NavItem(Icons.Rounded.SportsEsports, stringResource(R.string.games), onGames)
            NavItem(Icons.Rounded.BarChart, stringResource(R.string.progress), onProgress)
            NavItem(Icons.Rounded.MoreHoriz, stringResource(R.string.more), onSettings)
        }
        BottomKente(height = 12.dp)
    }
}

@Composable
private fun NavItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
        Text(label, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
private fun MenuRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Quest.Teal),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(label, Modifier.weight(1f), color = Quest.Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = Quest.InkSoft)
    }
}

@Composable
private fun StatTile(icon: ImageVector, tint: Color, label: String, value: String, modifier: Modifier = Modifier) {
    WhiteCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(label, color = Quest.InkSoft, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(value, color = Quest.Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

@Composable
private fun BadgeMedal(badge: Badge, earned: Boolean, size: Dp) {
    val style = BadgeStyles.of(badge)
    val title = stringResource(style.title)
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (earned) {
                    Brush.verticalGradient(listOf(style.color.lighten(0.3f), style.color.darken(0.15f)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFFDDD6CA), Color(0xFFB9B1A4)))
                },
            )
            .border(2.dp, if (earned) Quest.Gold else Color(0xFFCFC6B8), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (earned) style.icon else Icons.Rounded.Lock,
            contentDescription = if (earned) title else stringResource(R.string.badge_locked_description, title),
            tint = Color.White,
            modifier = Modifier.size(size * 0.58f),
        )
    }
}

@Composable
fun LeaderboardScreen(viewModel: LeaderboardViewModel, onBack: () -> Unit) {
    val standings by viewModel.standings.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val numbers = rememberNumberFormat()
    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Quest.Teal, Quest.TealDark))),
    ) {
        HeaderBar(stringResource(R.string.leaderboard), onBack, brush = null)
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Quest.Cream),
        ) {
            Row(
                Modifier
                    .padding(14.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFEDE3D2))
                    .padding(4.dp),
            ) {
                listOf(R.string.champions, R.string.my_bests).forEachIndexed { i, label ->
                    val active = i == tab
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (active) Quest.Teal else Color.Transparent)
                            .clickable(role = Role.Tab) { tab = i }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            stringResource(label),
                            color = if (active) Color.White else Quest.Ink,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            if (tab == 0) {
                Text(
                    stringResource(R.string.champions_intro),
                    Modifier.padding(horizontal = 18.dp),
                    color = Quest.InkSoft,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
                LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(standings) { RankRow(it, numbers) }
                }
            } else {
                LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(GameKind.entries) { kind -> BestRow(kind, profile, numbers) }
                }
            }
        }
        BottomKente()
    }
}

@Composable
private fun RankRow(entry: RankEntry, numbers: NumberFormat) {
    val style = CharacterStyles.of(entry.character)
    val shape = RoundedCornerShape(16.dp)
    val name = if (entry.isPlayer) {
        stringResource(R.string.you_suffix, entry.name.ifBlank { stringResource(R.string.you) })
    } else {
        entry.name
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (entry.isPlayer) Color(0xFFDFF3E4) else Color.White)
            .border(if (entry.isPlayer) 2.dp else 1.dp, if (entry.isPlayer) Quest.Correct else Quest.Line, shape)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RankBadge(entry.rank)
        Spacer(Modifier.width(10.dp))
        Avatar(style.avatar, 44.dp, ring = style.color, ringWidth = 2.5.dp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Star, contentDescription = null, tint = Quest.Yellow, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    name,
                    color = Quest.Ink,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RegionBadge(style, 16.dp, ring = Color.Transparent)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(RegionLabels.of(entry.character.region)), color = Quest.InkSoft, fontSize = 13.sp)
            }
        }
        Text(numbers.format(entry.score), color = Quest.Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RankBadge(rank: Int) {
    val medal = when (rank) {
        1 -> Quest.Gold
        2 -> Quest.Silver
        3 -> Quest.Bronze
        else -> null
    }
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                if (medal != null) {
                    Brush.verticalGradient(listOf(medal.lighten(0.25f), medal.darken(0.15f)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFFF1EADF), Color(0xFFE4D9C7)))
                },
            )
            .border(2.dp, if (medal != null) Color.White else Quest.Line, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text("$rank", color = if (medal != null) Color.White else Quest.Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BestRow(kind: GameKind, profile: PlayerProfile, numbers: NumberFormat) {
    val style = GameStyles.of(kind)
    val played = profile.timesPlayed(kind)
    WhiteCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(style.image),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 92.dp, height = 54.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(style.title), color = Quest.Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(pluralStringResource(R.plurals.played_times, played, played), color = Quest.InkSoft, fontSize = 13.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(stringResource(R.string.best), color = Quest.InkSoft, fontSize = 12.sp)
                Text(numbers.format(profile.bestScore(kind)), color = Quest.Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProgressScreen(state: PlayerUiState, onBack: () -> Unit) {
    val profile = state.profile
    val level = state.level
    val numbers = rememberNumberFormat()
    Column(
        Modifier
            .fillMaxSize()
            .background(Quest.Parchment),
    ) {
        HeaderBar(stringResource(R.string.my_progress), onBack, brush = Brush.verticalGradient(listOf(Quest.BlueTop, Quest.BlueDark)))
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WhiteCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LevelRing(level.level, level.progress, 96.dp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            stringResource(R.string.level_number, level.level),
                            color = Quest.Ink,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            stringResource(R.string.xp_in_level, level.earned, level.required),
                            color = Quest.InkSoft,
                            fontSize = 14.sp,
                        )
                        Text(
                            stringResource(R.string.xp_to_next, level.remaining, level.level + 1),
                            color = Quest.Teal,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    Icons.Rounded.Star, Quest.YellowDark, stringResource(R.string.total_score),
                    numbers.format(profile.totalScore), Modifier.weight(1f),
                )
                StatTile(
                    Icons.Rounded.EmojiEvents, Quest.PurpleDark, stringResource(R.string.wins),
                    numbers.format(profile.wins), Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    Icons.Rounded.SportsEsports, Quest.BlueDark, stringResource(R.string.games_played),
                    numbers.format(profile.gamesPlayed), Modifier.weight(1f),
                )
                StatTile(
                    Icons.Rounded.BarChart, Quest.GreenDark, stringResource(R.string.total_xp),
                    numbers.format(profile.xp), Modifier.weight(1f),
                )
            }
            Text(stringResource(R.string.games), color = Quest.Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            GameKind.entries.forEach { BestRow(it, profile, numbers) }
            Text(stringResource(R.string.badges), color = Quest.Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Badge.entries.forEach { badge ->
                val earned = badge in profile.badges
                val style = BadgeStyles.of(badge)
                WhiteCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BadgeMedal(badge, earned, 44.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(style.title), color = Quest.Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text(stringResource(style.description), color = Quest.InkSoft, fontSize = 13.sp)
                        }
                        Text(
                            stringResource(if (earned) R.string.earned else R.string.locked),
                            color = if (earned) Quest.GreenDark else Quest.InkSoft,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
        BottomKente()
    }
}

@Composable
private fun LevelRing(level: Int, progress: Float, diameter: Dp) {
    Box(Modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 10.dp.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)
            drawArc(Color(0xFFE9E0CF), 0f, 360f, false, topLeft = topLeft, size = arcSize, style = Stroke(stroke))
            drawArc(
                Brush.sweepGradient(listOf(Quest.GreenTop, Quest.Green, Quest.GreenTop)),
                -90f,
                360f * progress.coerceIn(0f, 1f),
                false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.level_word), color = Quest.InkSoft, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("$level", color = Quest.Ink, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }
    }
}
