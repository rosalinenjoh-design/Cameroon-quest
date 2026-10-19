package com.cameroonquest.app.data.content

import com.cameroonquest.app.domain.model.LocalizedText
import com.cameroonquest.app.domain.model.QuestCharacter
import com.cameroonquest.app.domain.model.Region
import com.cameroonquest.app.domain.model.Skill
import com.cameroonquest.app.domain.repository.CharacterCatalog

/** Singleton catalog of the ten regional characters, written in English and French. */
object LocalCharacterCatalog : CharacterCatalog {
    override val characters: List<QuestCharacter> = listOf(
        QuestCharacter(
            id = "njoh",
            name = "Njoh",
            region = Region.LITTORAL,
            quote = LocalizedText(
                "The ocean is my home, and my culture my strength.",
                "L’océan est ma maison, et ma culture ma force.",
            ),
            about = LocalizedText(
                "Njoh comes from the Littoral region. He loves the sea, traditional music and the Sawa culture. " +
                    "He is curious, brave and always ready for new adventures!",
                "Njoh vient de la région du Littoral. Il aime la mer, la musique traditionnelle et la culture sawa. " +
                    "Il est curieux, courageux et toujours prêt pour de nouvelles aventures\u00A0!",
            ),
            skills = listOf(Skill.SPEED, Skill.FOCUS, Skill.TEAMWORK),
            championScore = 7890,
        ),
        QuestCharacter(
            id = "bakama",
            name = "Bakama",
            region = Region.CENTRE,
            quote = LocalizedText(
                "Patience and strategy win every Songo match.",
                "La patience et la stratégie gagnent chaque partie de Songo.",
            ),
            about = LocalizedText(
                "Bakama grew up near Yaoundé in the Centre region. She learned Songo from her grandmother " +
                    "and loves Bikutsi rhythms. She is clever, calm and never gives up.",
                "Bakama a grandi près de Yaoundé, dans la région du Centre. Sa grand-mère lui a appris le Songo " +
                    "et elle adore les rythmes du bikutsi. Elle est maligne, calme et n’abandonne jamais.",
            ),
            skills = listOf(Skill.STRATEGY, Skill.WISDOM, Skill.PATIENCE),
            championScore = 8560,
        ),
        QuestCharacter(
            id = "mbororo",
            name = "Mbororo",
            region = Region.FAR_NORTH,
            quote = LocalizedText(
                "The savanna wind carries my stories.",
                "Le vent de la savane porte mes histoires.",
            ),
            about = LocalizedText(
                "Mbororo comes from the Far North, land of wide savannas and Waza National Park. " +
                    "He knows the herds, the stars and the long roads of the Sahel. He is strong, patient and wise.",
                "Mbororo vient de l’Extrême-Nord, terre de grandes savanes et du parc national de Waza. " +
                    "Il connaît les troupeaux, les étoiles et les longues routes du Sahel. Il est fort, patient et sage.",
            ),
            skills = listOf(Skill.ENDURANCE, Skill.WISDOM, Skill.FOCUS),
            championScore = 6310,
        ),
        QuestCharacter(
            id = "wouri",
            name = "Wouri",
            region = Region.NORTH_WEST,
            quote = LocalizedText(
                "The hills of the Grassfields keep our traditions alive.",
                "Les collines des Grassfields gardent nos traditions vivantes.",
            ),
            about = LocalizedText(
                "Wouri lives in the North West highlands near Bamenda. She loves the bright toghu cloth, " +
                    "traditional dances and the cool mountain air. She is creative, joyful and a great storyteller.",
                "Wouri vit sur les hauts plateaux du Nord-Ouest, près de Bamenda. Elle aime le toghu aux couleurs vives, " +
                    "les danses traditionnelles et l’air frais des montagnes. Elle est créative, joyeuse et grande conteuse.",
            ),
            skills = listOf(Skill.CREATIVITY, Skill.RHYTHM, Skill.TEAMWORK),
            championScore = 7450,
        ),
        QuestCharacter(
            id = "nkem",
            name = "Nkem",
            region = Region.WEST,
            quote = LocalizedText(
                "Respect the elders, honour the chiefdom.",
                "Respecte les anciens, honore la chefferie.",
            ),
            about = LocalizedText(
                "Nkem comes from the West region, home of the Bamileke chiefdoms and their tall thatched houses. " +
                    "He loves masks, markets and the indigo Ndop cloth. He is respectful, hard-working and proud of his roots.",
                "Nkem vient de la région de l’Ouest, terre des chefferies bamiléké et de leurs grandes cases au toit de chaume. " +
                    "Il aime les masques, les marchés et le tissu ndop indigo. Il est respectueux, travailleur et fier de ses racines.",
            ),
            skills = listOf(Skill.STRATEGY, Skill.WISDOM, Skill.TEAMWORK),
            championScore = 5780,
        ),
        QuestCharacter(
            id = "yeme",
            name = "Yeme",
            region = Region.SOUTH_WEST,
            quote = LocalizedText(
                "I climb Mount Cameroon with a smile.",
                "Je grimpe le mont Cameroun avec le sourire.",
            ),
            about = LocalizedText(
                "Yeme lives in Buea, at the foot of Mount Cameroon, and cheers the runners of the Race of Hope every year. " +
                    "She loves the green tea fields and the black sand beaches of Limbe. She is energetic, kind and brave.",
                "Yeme vit à Buéa, au pied du mont Cameroun, et encourage chaque année les coureurs de la Course de l’espoir. " +
                    "Elle aime les champs de thé verts et les plages de sable noir de Limbé. Elle est énergique, gentille et courageuse.",
            ),
            skills = listOf(Skill.SPEED, Skill.ENDURANCE, Skill.COURAGE),
            championScore = 5410,
        ),
        QuestCharacter(
            id = "zanga",
            name = "Zanga",
            region = Region.ADAMAWA,
            quote = LocalizedText(
                "From the plateau, I can see the whole country.",
                "Du haut du plateau, je vois tout le pays.",
            ),
            about = LocalizedText(
                "Zanga comes from the Adamawa plateau around Ngaoundéré, known as Cameroon’s water tower. " +
                    "He loves horse riding, cattle markets and the Lamido’s palace. He is calm, fair and a natural leader.",
                "Zanga vient du plateau de l’Adamaoua, autour de Ngaoundéré, qu’on appelle le château d’eau du Cameroun. " +
                    "Il aime l’équitation, les marchés de bétail et le palais du lamido. Il est calme, juste et un vrai meneur.",
            ),
            skills = listOf(Skill.LEADERSHIP, Skill.FOCUS, Skill.ENDURANCE),
            championScore = 5120,
        ),
        QuestCharacter(
            id = "tenda",
            name = "Ténda",
            region = Region.EAST,
            quote = LocalizedText(
                "The forest is alive, and it teaches me every day.",
                "La forêt est vivante, et elle m’apprend chaque jour.",
            ),
            about = LocalizedText(
                "Ténda lives in the East, the largest region of Cameroon, covered by deep rainforest. " +
                    "She knows the plants and the songs of the forest. She is gentle, observant and very brave.",
                "Ténda vit dans l’Est, la plus grande région du Cameroun, couverte d’une forêt dense. " +
                    "Elle connaît les plantes et les chants de la forêt. Elle est douce, attentive et très courageuse.",
            ),
            skills = listOf(Skill.FOCUS, Skill.WISDOM, Skill.AGILITY),
            championScore = 6020,
        ),
        QuestCharacter(
            id = "foufou",
            name = "Foufou",
            region = Region.NORTH,
            quote = LocalizedText(
                "Like the Bénoué river, I never stop moving.",
                "Comme la Bénoué, je ne m’arrête jamais.",
            ),
            about = LocalizedText(
                "Foufou comes from Garoua in the North region, on the banks of the Bénoué river. " +
                    "He loves football, fishing and the colourful markets of the Sahel. He is fast, funny and full of energy.",
                "Foufou vient de Garoua, dans la région du Nord, au bord de la Bénoué. " +
                    "Il aime le football, la pêche et les marchés colorés du Sahel. Il est rapide, drôle et plein d’énergie.",
            ),
            skills = listOf(Skill.SPEED, Skill.AGILITY, Skill.TEAMWORK),
            championScore = 6720,
        ),
        // The mockup labels Léa "Grand Nord", which is not one of the ten regions; South is the missing one.
        QuestCharacter(
            id = "lea",
            name = "Léa",
            region = Region.SOUTH,
            quote = LocalizedText(
                "Between forest and sea, I find my rhythm.",
                "Entre forêt et mer, je trouve mon rythme.",
            ),
            about = LocalizedText(
                "Léa comes from the South region, between the rainforest and the beaches of Kribi, where the Lobé Falls " +
                    "meet the ocean. She loves cocoa farms, music and dancing. She is joyful, curious and loves to learn.",
                "Léa vient de la région du Sud, entre la forêt tropicale et les plages de Kribi, où les chutes de la Lobé " +
                    "se jettent dans l’océan. Elle aime les plantations de cacao, la musique et la danse. " +
                    "Elle est joyeuse, curieuse et adore apprendre.",
            ),
            skills = listOf(Skill.RHYTHM, Skill.CREATIVITY, Skill.AGILITY),
            championScore = 6980,
        ),
    )
}
