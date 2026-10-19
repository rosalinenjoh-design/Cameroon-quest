package com.cameroonquest.app.ui.screens.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cameroonquest.app.R
import com.cameroonquest.app.domain.model.PlayerProfile
import com.cameroonquest.app.domain.model.QuestCharacter
import com.cameroonquest.app.ui.components.BottomKente
import com.cameroonquest.app.ui.components.CircleIconButton
import com.cameroonquest.app.ui.components.KentePattern
import com.cameroonquest.app.ui.components.QuestButton
import com.cameroonquest.app.ui.components.QuestDialog
import com.cameroonquest.app.ui.components.RegionBadge
import com.cameroonquest.app.ui.components.TornPaperShape
import com.cameroonquest.app.ui.components.WaveBand
import com.cameroonquest.app.ui.components.WoodBanner
import com.cameroonquest.app.ui.components.fadeEdges
import com.cameroonquest.app.ui.components.rememberHaptics
import com.cameroonquest.app.ui.style.CharacterStyles
import com.cameroonquest.app.ui.style.RegionLabels
import com.cameroonquest.app.ui.style.SkillStyles
import com.cameroonquest.app.ui.style.localized
import com.cameroonquest.app.ui.theme.Quest
import com.cameroonquest.app.ui.theme.darken

@Composable
fun CharacterSelectScreen(characters: List<QuestCharacter>, onBack: () -> Unit, onPick: (QuestCharacter) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFFFFF5E4), Quest.Parchment, Color(0xFFEFD2A8)), radius = 1600f)),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0x40000000), Color.Transparent)))
                .statusBarsPadding()
                .padding(top = 6.dp),
        ) {
            CircleIconButton(
                Icons.AutoMirrored.Rounded.ArrowBack,
                stringResource(R.string.back),
                onBack,
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 10.dp),
                background = Quest.Wood,
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KentePattern(
                Modifier
                    .weight(1f)
                    .height(12.dp),
            )
            WoodBanner(
                stringResource(R.string.choose_character_title),
                stringResource(R.string.choose_character_subtitle),
                Modifier.padding(horizontal = 6.dp),
            )
            KentePattern(
                Modifier
                    .weight(1f)
                    .height(12.dp),
            )
        }
        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val gap = 10.dp
            val cardWidth = (maxWidth - 28.dp - gap * 2) / 3
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                characters.chunked(3).forEach { row ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
                    ) {
                        row.forEach { character ->
                            CharacterCard(character, { onPick(character) }, Modifier.width(cardWidth))
                        }
                    }
                }
            }
        }
        BottomKente()
    }
}

@Composable
private fun CharacterCard(character: QuestCharacter, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val style = CharacterStyles.of(character)
    val region = stringResource(RegionLabels.of(character.region))
    val shape = RoundedCornerShape(16.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, label = "card")
    val haptics = rememberHaptics()
    Column(
        modifier
            .scale(scale)
            .shadow(6.dp, shape)
            .clip(shape)
            .background(style.color)
            .border(2.dp, Color.White.copy(alpha = 0.75f), shape)
            .clickable(
                interaction,
                indication = null,
                onClickLabel = stringResource(R.string.choose_named, character.name),
                role = Role.Button,
            ) {
                haptics()
                onClick()
            },
    ) {
        Image(
            painterResource(style.portrait),
            contentDescription = "${character.name}, $region",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.12f),
            contentScale = ContentScale.Crop,
        )
        Row(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(style.color, style.color.darken(0.14f))))
                .padding(horizontal = 5.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RegionBadge(style, 26.dp)
            Spacer(Modifier.width(5.dp))
            Column {
                Text(
                    character.name,
                    color = style.onColor,
                    fontSize = 16.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    region,
                    color = style.onColor.copy(alpha = 0.88f),
                    fontSize = 11.sp,
                    lineHeight = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun CharacterDetailScreen(
    character: QuestCharacter,
    currentName: String,
    onBack: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val style = CharacterStyles.of(character)
    var askName by rememberSaveable { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxSize()
            .background(style.color.darken(0.4f)),
    ) {
        Image(
            painterResource(style.backdrop),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.1f)),
        )
        val art = style.detailArt
        if (art != null) {
            Image(
                painterResource(art),
                contentDescription = character.name,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.6f)
                    .fadeEdges(toEnd = true, from = 0.8f),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
            )
        } else {
            WaveBand(
                style.color,
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.6f)
                    .height(190.dp),
            )
            Image(
                painterResource(style.portrait),
                contentDescription = character.name,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 14.dp, top = 70.dp)
                    .fillMaxWidth(0.42f)
                    .aspectRatio(0.92f)
                    .rotate(-3f)
                    .shadow(10.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .border(4.dp, Color.White, RoundedCornerShape(22.dp)),
                contentScale = ContentScale.Crop,
            )
        }
        CircleIconButton(
            Icons.AutoMirrored.Rounded.ArrowBack,
            stringResource(R.string.back),
            onBack,
            Modifier
                .statusBarsPadding()
                .padding(12.dp),
            background = Color.White.copy(alpha = 0.92f),
            tint = Quest.Ink,
            border = null,
        )
        DetailCard(
            character,
            onSelect = { if (currentName.isBlank()) askName = true else onSelect(currentName) },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.56f)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(end = 10.dp, top = 58.dp, bottom = 20.dp),
        )
    }
    if (askName) {
        NameDialog(initial = currentName, onDismiss = { askName = false }) {
            askName = false
            onSelect(it)
        }
    }
}

@Composable
private fun DetailCard(character: QuestCharacter, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    val style = CharacterStyles.of(character)
    val shape = TornPaperShape()
    Column(
        modifier
            .shadow(12.dp, shape)
            .clip(shape)
            .background(Quest.Cream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RegionBadge(style, 46.dp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    character.name,
                    color = Quest.Navy,
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    lineHeight = 38.sp,
                    maxLines = 1,
                )
                Text(stringResource(RegionLabels.of(character.region)), color = Quest.Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.quote_format, character.quote.localized()),
            color = Quest.InkSoft,
            fontStyle = FontStyle.Italic,
            fontSize = 14.sp,
            lineHeight = 19.sp,
        )
        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Quest.Line)
        Text(stringResource(R.string.about_character, character.name), color = Quest.Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Text(character.about.localized(), color = Quest.Ink, fontSize = 13.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(14.dp))
        Text(stringResource(R.string.special_skills), color = Quest.Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            character.skills.forEach { skill ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(style.color.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(SkillStyles.icon(skill), contentDescription = null, tint = style.color.darken(0.3f), modifier = Modifier.size(26.dp))
                    }
                    Text(stringResource(SkillStyles.label(skill)), color = Quest.Ink, fontSize = 11.sp, maxLines = 1)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        QuestButton(
            stringResource(R.string.select),
            onSelect,
            Modifier.fillMaxWidth(),
            top = Quest.YellowTop,
            bottom = Quest.YellowDark,
            textColor = Quest.YellowInk,
            height = 50.dp,
            fontSize = 22.sp,
        )
    }
}

@Composable
fun NameDialog(initial: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf(initial) }
    val valid = name.isNotBlank()
    QuestDialog(onDismiss) {
        Text(stringResource(R.string.name_title), color = Quest.Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(
            stringResource(R.string.name_hint),
            color = Quest.InkSoft,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(PlayerProfile.MAX_NAME_LENGTH) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.name_label)) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (valid) onConfirm(name.trim()) }),
        )
        QuestButton(
            stringResource(R.string.lets_go),
            { onConfirm(name.trim()) },
            Modifier.fillMaxWidth(),
            height = 52.dp,
            fontSize = 21.sp,
            enabled = valid,
        )
        TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = Quest.InkSoft) }
    }
}
