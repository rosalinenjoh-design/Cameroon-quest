package com.cameroonquest.app.data.content

import com.cameroonquest.app.domain.model.LocalizedText
import com.cameroonquest.app.domain.model.PictureKeys
import com.cameroonquest.app.domain.model.QuizQuestion
import com.cameroonquest.app.domain.repository.QuestionBank

/** Singleton bank of bilingual quiz questions about Cameroon's regions. */
object LocalQuestionBank : QuestionBank {
    override val questions: List<QuizQuestion> = listOf(
        QuizQuestion(
            id = "grassfields_house", characterId = "nkem",
            question = t(
                "Houses with a square base and a tall cone-shaped thatched roof are typical of which area?",
                "Les cases à base carrée et au grand toit de chaume en forme de cône sont typiques de quelle zone\u00A0?",
            ),
            options = listOf(
                t("The Grassfields (West & North West)", "Les Grassfields (Ouest et Nord-Ouest)"),
                t("The Sahel of the Far North", "Le Sahel de l’Extrême-Nord"),
                t("The Littoral coast", "La côte du Littoral"),
                t("The East rainforest", "La forêt de l’Est"),
            ),
            correct = 0,
            fact = t(
                "Grassfields chiefdoms such as Bandjoun are famous for tall thatched houses with carved pillars.",
                "Les chefferies des Grassfields, comme Bandjoun, sont célèbres pour leurs grandes cases au toit de chaume et aux piliers sculptés.",
            ),
            pictureKey = PictureKeys.GRASSFIELDS_HOUSE,
        ),
        QuizQuestion(
            id = "songo_beti", characterId = "bakama",
            question = t("Which seed-board game is associated with Beti culture?", "Quel jeu de graines est associé à la culture beti\u00A0?"),
            options = listOf(same("Ludo"), same("Songo"), t("Chess", "Les échecs"), same("Scrabble")),
            correct = 1,
            fact = t(
                "Songo, also called Mbang, is played by the Beti peoples of the Centre and South.",
                "Le Songo, aussi appelé mbang, se joue chez les peuples beti du Centre et du Sud.",
            ),
        ),
        QuizQuestion(
            id = "ngondo", characterId = "njoh",
            question = t(
                "Which Sawa festival is celebrated each year around the Wouri River?",
                "Quelle fête sawa est célébrée chaque année autour du fleuve Wouri\u00A0?",
            ),
            options = listOf(same("Ngondo"), same("Nguon"), same("Lela"), same("Medumba")),
            correct = 0,
            fact = t(
                "The Ngondo is the great festival of the Sawa coastal peoples, held in Douala.",
                "Le Ngondo est la grande fête des peuples sawa de la côte, célébrée à Douala.",
            ),
        ),
        QuizQuestion(
            id = "capital_west", characterId = "nkem",
            question = t("Which city is the capital of the West Region?", "Quelle ville est le chef-lieu de la région de l’Ouest\u00A0?"),
            options = listOf(same("Garoua"), same("Bafoussam"), t("Buea", "Buéa"), same("Ebolowa")),
            correct = 1,
            fact = t(
                "Bafoussam is the capital of the West Region, in the heart of the Bamileke country.",
                "Bafoussam est le chef-lieu de la région de l’Ouest, au cœur du pays bamiléké.",
            ),
        ),
        QuizQuestion(
            id = "capital_adamawa", characterId = "zanga",
            question = t("Which city is the capital of the Adamawa Region?", "Quelle ville est le chef-lieu de la région de l’Adamaoua\u00A0?"),
            options = listOf(same("Maroua"), same("Ngaoundéré"), same("Douala"), same("Bamenda")),
            correct = 1,
            fact = t(
                "Ngaoundéré sits on the Adamawa plateau, often called Cameroon’s water tower.",
                "Ngaoundéré se trouve sur le plateau de l’Adamaoua, souvent appelé le château d’eau du Cameroun.",
            ),
        ),
        QuizQuestion(
            id = "mount_cameroon", characterId = "yeme",
            question = t("In which region is Mount Cameroon?", "Dans quelle région se trouve le mont Cameroun\u00A0?"),
            options = listOf(t("South West", "Sud-Ouest"), t("North West", "Nord-Ouest"), same("Littoral"), same("Centre")),
            correct = 0,
            fact = t(
                "At about 4,000 m, Mount Cameroon is the highest peak of West and Central Africa.",
                "Avec environ 4\u00A0000\u00A0m, le mont Cameroun est le plus haut sommet d’Afrique de l’Ouest et centrale.",
            ),
        ),
        QuizQuestion(
            id = "capital_north", characterId = "foufou",
            question = t("Which city is the capital of the North Region?", "Quelle ville est le chef-lieu de la région du Nord\u00A0?"),
            options = listOf(same("Bertoua"), t("Buea", "Buéa"), same("Garoua"), same("Yaoundé")),
            correct = 2,
            fact = t("Garoua lies on the banks of the Bénoué river.", "Garoua s’étend au bord de la Bénoué."),
        ),
        QuizQuestion(
            id = "capital_far_north", characterId = "mbororo",
            question = t(
                "Which city is the capital of the Far North Region?",
                "Quelle ville est le chef-lieu de la région de l’Extrême-Nord\u00A0?",
            ),
            options = listOf(same("Maroua"), same("Bafoussam"), same("Ebolowa"), same("Douala")),
            correct = 0,
            fact = t(
                "Maroua is the capital of the Far North, the region closest to Lake Chad.",
                "Maroua est le chef-lieu de l’Extrême-Nord, la région la plus proche du lac Tchad.",
            ),
        ),
        QuizQuestion(
            id = "capital_east", characterId = "tenda",
            question = t("Which city is the capital of the East Region?", "Quelle ville est le chef-lieu de la région de l’Est\u00A0?"),
            options = listOf(same("Bamenda"), same("Garoua"), same("Bertoua"), t("Buea", "Buéa")),
            correct = 2,
            fact = t(
                "Bertoua is the capital of the East, Cameroon’s largest region.",
                "Bertoua est le chef-lieu de l’Est, la plus grande région du Cameroun.",
            ),
        ),
        QuizQuestion(
            id = "capital_south", characterId = "lea",
            question = t("Which city is the capital of the South Region?", "Quelle ville est le chef-lieu de la région du Sud\u00A0?"),
            options = listOf(same("Ngaoundéré"), same("Ebolowa"), same("Maroua"), same("Douala")),
            correct = 1,
            fact = t(
                "Ebolowa is the capital of the South, a region known for its cocoa farms.",
                "Ebolowa est le chef-lieu du Sud, une région connue pour ses plantations de cacao.",
            ),
        ),
        QuizQuestion(
            id = "capital_north_west", characterId = "wouri",
            question = t(
                "Which city is the capital of the North West Region?",
                "Quelle ville est le chef-lieu de la région du Nord-Ouest\u00A0?",
            ),
            options = listOf(same("Yaoundé"), same("Bertoua"), same("Bafoussam"), same("Bamenda")),
            correct = 3,
            fact = t(
                "Bamenda is the capital of the North West, in the Grassfields highlands.",
                "Bamenda est le chef-lieu du Nord-Ouest, sur les hauts plateaux des Grassfields.",
            ),
        ),
        QuizQuestion(
            id = "political_capital", characterId = "bakama",
            question = t("Which city is the political capital of Cameroon?", "Quelle ville est la capitale politique du Cameroun\u00A0?"),
            options = listOf(same("Douala"), same("Yaoundé"), same("Garoua"), same("Kribi")),
            correct = 1,
            fact = t(
                "Yaoundé, the capital of the Centre Region, is also the national capital.",
                "Yaoundé, chef-lieu de la région du Centre, est aussi la capitale du pays.",
            ),
        ),
        QuizQuestion(
            id = "economic_capital", characterId = "njoh",
            question = t(
                "Which city is Cameroon’s economic capital and busiest port?",
                "Quelle ville est la capitale économique et le plus grand port du Cameroun\u00A0?",
            ),
            options = listOf(same("Douala"), t("Limbe", "Limbé"), same("Kribi"), same("Bertoua")),
            correct = 0,
            fact = t(
                "Douala, capital of the Littoral Region, is the country’s economic capital.",
                "Douala, chef-lieu de la région du Littoral, est la capitale économique du pays.",
            ),
        ),
        QuizQuestion(
            id = "fon_title", characterId = "wouri",
            question = t(
                "In the North West Grassfields, what title is given to a traditional ruler?",
                "Dans les Grassfields du Nord-Ouest, quel titre porte un chef traditionnel\u00A0?",
            ),
            options = listOf(same("Fon"), same("Lamido"), t("Mayor", "Maire"), t("Governor", "Gouverneur")),
            correct = 0,
            fact = t(
                "Fons lead traditional kingdoms of the Grassfields, such as Bafut and Nso.",
                "Les fons dirigent des royaumes traditionnels des Grassfields, comme Bafut et Nso.",
            ),
        ),
        QuizQuestion(
            id = "lamido_title", characterId = "zanga",
            question = t(
                "In Ngaoundéré and Garoua, which title is used for Fulani traditional rulers?",
                "À Ngaoundéré et à Garoua, quel titre portent les chefs traditionnels peuls\u00A0?",
            ),
            options = listOf(same("Fon"), same("Lamido"), t("Pharaoh", "Pharaon"), t("Emperor", "Empereur")),
            correct = 1,
            fact = t(
                "Lamidos lead lamidates such as Ngaoundéré, Garoua and Rey Bouba.",
                "Les lamidos dirigent des lamidats comme Ngaoundéré, Garoua et Rey Bouba.",
            ),
        ),
        QuizQuestion(
            id = "ndole", characterId = "njoh",
            question = t(
                "Ndolé, a famous Cameroonian dish, is made mainly with…",
                "Le ndolé, plat célèbre du Cameroun, est surtout préparé avec…",
            ),
            options = listOf(
                t("Bitter leaves", "Des feuilles amères"),
                t("Rice and beans", "Du riz et des haricots"),
                t("Corn only", "Du maïs seulement"),
                t("Potatoes", "Des pommes de terre"),
            ),
            correct = 0,
            fact = t(
                "Ndolé is a stew of bitter leaves, groundnuts and fish or meat, loved in Douala and beyond.",
                "Le ndolé est un plat de feuilles amères, d’arachides et de poisson ou de viande, très aimé à Douala et ailleurs.",
            ),
        ),
        QuizQuestion(
            id = "waza", characterId = "mbororo",
            question = t(
                "Waza National Park, famous for giraffes and elephants, is in which region?",
                "Le parc national de Waza, célèbre pour ses girafes et ses éléphants, est dans quelle région\u00A0?",
            ),
            options = listOf(t("Far North", "Extrême-Nord"), t("South", "Sud"), same("Littoral"), t("West", "Ouest")),
            correct = 0,
            fact = t(
                "Waza National Park in the Far North is one of Cameroon’s best-known parks.",
                "Le parc de Waza, dans l’Extrême-Nord, est l’un des parcs les plus connus du Cameroun.",
            ),
        ),
        QuizQuestion(
            id = "lobe_falls", characterId = "lea",
            question = t(
                "Near Kribi, the Lobé Falls are special because they…",
                "Près de Kribi, les chutes de la Lobé sont spéciales car elles…",
            ),
            options = listOf(
                t("Fall directly into the sea", "Se jettent directement dans la mer"),
                t("Freeze every year", "Gèlent chaque année"),
                t("Flow uphill", "Coulent vers le haut"),
                t("Are inside a cave", "Sont dans une grotte"),
            ),
            correct = 0,
            fact = t(
                "The Lobé Falls are one of the rare waterfalls in the world that flow straight into the ocean.",
                "Les chutes de la Lobé font partie des rares chutes au monde qui se jettent directement dans l’océan.",
            ),
        ),
        QuizQuestion(
            id = "lake_chad", characterId = "mbororo",
            question = t(
                "Which lake is shared by Cameroon, Chad, Niger and Nigeria?",
                "Quel lac est partagé par le Cameroun, le Tchad, le Niger et le Nigeria\u00A0?",
            ),
            options = listOf(
                t("Lake Chad", "Le lac Tchad"),
                t("Lake Nyos", "Le lac Nyos"),
                t("Lake Barombi", "Le lac Barombi"),
                t("Lake Victoria", "Le lac Victoria"),
            ),
            correct = 0,
            fact = t(
                "Lake Chad touches the very north of Cameroon’s Far North Region.",
                "Le lac Tchad touche l’extrémité nord de la région de l’Extrême-Nord.",
            ),
        ),
        QuizQuestion(
            id = "makossa", characterId = "njoh",
            question = t(
                "Makossa music, made famous by Manu Dibango, was born in which city?",
                "Le makossa, rendu célèbre par Manu Dibango, est né dans quelle ville\u00A0?",
            ),
            options = listOf(same("Douala"), same("Maroua"), same("Bertoua"), same("Ebolowa")),
            correct = 0,
            fact = t(
                "Manu Dibango’s “Soul Makossa” (1972) carried this Douala rhythm around the world.",
                "«\u00A0Soul Makossa\u00A0» de Manu Dibango (1972) a fait connaître ce rythme de Douala dans le monde entier.",
            ),
        ),
        QuizQuestion(
            id = "bikutsi", characterId = "bakama",
            question = t(
                "Bikutsi, a fast dance rhythm, comes from which people?",
                "Le bikutsi, un rythme de danse rapide, vient de quel peuple\u00A0?",
            ),
            options = listOf(same("Beti"), same("Sawa"), t("Fulani", "Peuls"), t("Bamileke", "Bamiléké")),
            correct = 0,
            fact = t(
                "Bikutsi comes from the Beti peoples of the Centre region.",
                "Le bikutsi vient des peuples beti de la région du Centre.",
            ),
        ),
        QuizQuestion(
            id = "largest_region", characterId = "tenda",
            question = t(
                "Which is the largest region of Cameroon by area?",
                "Quelle est la plus grande région du Cameroun par la superficie\u00A0?",
            ),
            options = listOf(t("East", "Est"), same("Littoral"), t("West", "Ouest"), t("North West", "Nord-Ouest")),
            correct = 0,
            fact = t(
                "The East Region covers about 109,000 km², mostly rainforest.",
                "La région de l’Est couvre environ 109\u00A0000\u00A0km², surtout de la forêt tropicale.",
            ),
        ),
        QuizQuestion(
            id = "dja_reserve", characterId = "tenda",
            question = t(
                "The Dja Faunal Reserve, a UNESCO World Heritage site, mainly protects…",
                "La réserve de faune du Dja, inscrite au patrimoine mondial de l’UNESCO, protège surtout…",
            ),
            options = listOf(
                t("Rainforest", "La forêt tropicale"),
                t("Desert dunes", "Des dunes de sable"),
                t("Glaciers", "Des glaciers"),
                t("Coral reefs", "Des récifs de corail"),
            ),
            correct = 0,
            fact = t(
                "The Dja reserve protects a huge area of rainforest home to gorillas and chimpanzees.",
                "La réserve du Dja protège une immense forêt tropicale où vivent gorilles et chimpanzés.",
            ),
        ),
        QuizQuestion(
            id = "race_of_hope", characterId = "yeme",
            question = t(
                "Every year, runners race up and down Mount Cameroon in the…",
                "Chaque année, des coureurs montent et descendent le mont Cameroun lors de…",
            ),
            options = listOf(
                t("Race of Hope", "La Course de l’espoir"),
                t("Tour de France", "Le Tour de France"),
                t("Boston Marathon", "Le marathon de Boston"),
                t("Great Trek", "Le Grand Trek"),
            ),
            correct = 0,
            fact = t(
                "The Mount Cameroon Race of Hope starts and finishes in Buea.",
                "La Course de l’espoir du mont Cameroun part de Buéa et y revient.",
            ),
        ),
        QuizQuestion(
            id = "flag_colours", characterId = null,
            question = t(
                "What colours are on Cameroon’s flag, from left to right?",
                "Quelles sont les couleurs du drapeau du Cameroun, de gauche à droite\u00A0?",
            ),
            options = listOf(
                t("Green, red, yellow", "Vert, rouge, jaune"),
                t("Red, white, blue", "Rouge, blanc, bleu"),
                t("Yellow, green, red", "Jaune, vert, rouge"),
                t("Green, white, green", "Vert, blanc, vert"),
            ),
            correct = 0,
            fact = t("A yellow star shines in the middle of the red stripe.", "Une étoile jaune brille au milieu de la bande rouge."),
        ),
        QuizQuestion(
            id = "official_languages", characterId = null,
            question = t("What are the two official languages of Cameroon?", "Quelles sont les deux langues officielles du Cameroun\u00A0?"),
            options = listOf(
                t("French and English", "Le français et l’anglais"),
                t("French and German", "Le français et l’allemand"),
                t("English and Spanish", "L’anglais et l’espagnol"),
                t("Arabic and French", "L’arabe et le français"),
            ),
            correct = 0,
            fact = t(
                "Cameroon is bilingual, and more than 200 local languages are also spoken.",
                "Le Cameroun est bilingue, et on y parle aussi plus de 200 langues locales.",
            ),
        ),
    )

    private fun t(english: String, french: String) = LocalizedText(english, french)

    private fun same(text: String) = LocalizedText(text, text)
}
