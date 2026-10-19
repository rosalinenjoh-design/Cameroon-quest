package com.cameroonquest.app.data

import com.cameroonquest.app.R
import kotlin.random.Random

data class QuizQuestion(
    /** Character whose region the question is about; null for national questions. */
    val characterId: String?,
    val question: String,
    val options: List<String>,
    val correct: Int,
    val fact: String,
    val picture: Int? = null,
)

data class RoundQuestion(val source: QuizQuestion, val options: List<String>, val correct: Int)

object Questions {
    const val PER_ROUND = 10

    val bank = listOf(
        QuizQuestion(
            "nkem", "Houses with a square base and a tall cone-shaped thatched roof are typical of which area?",
            listOf("The Grassfields (West & North West)", "The Sahel of the Far North", "The Littoral coast", "The East rainforest"), 0,
            "Grassfields chiefdoms such as Bandjoun are famous for tall thatched houses with carved pillars.",
            R.drawable.quiz_grassfields,
        ),
        QuizQuestion(
            "bakama", "Which seed-board game is associated with Beti culture?",
            listOf("Ludo", "Songo", "Chess", "Scrabble"), 1,
            "Songo, also called Mbang, is played by the Beti peoples of the Centre and South.",
        ),
        QuizQuestion(
            "njoh", "Which Sawa festival is celebrated each year around the Wouri River?",
            listOf("Ngondo", "Nguon", "Lela", "Medumba"), 0,
            "The Ngondo is the great festival of the Sawa coastal peoples, held in Douala.",
        ),
        QuizQuestion(
            "nkem", "Which city is the capital of the West Region?",
            listOf("Garoua", "Bafoussam", "Buea", "Ebolowa"), 1,
            "Bafoussam is the capital of the West Region, in the heart of the Bamileke country.",
        ),
        QuizQuestion(
            "zanga", "Which city is the capital of the Adamawa Region?",
            listOf("Maroua", "Ngaoundéré", "Douala", "Bamenda"), 1,
            "Ngaoundéré sits on the Adamawa plateau, often called Cameroon's water tower.",
        ),
        QuizQuestion(
            "yeme", "In which region is Mount Cameroon?",
            listOf("South West", "North West", "Littoral", "Centre"), 0,
            "At about 4,000 m, Mount Cameroon is the highest peak of West and Central Africa.",
        ),
        QuizQuestion(
            "foufou", "Which city is the capital of the North Region?",
            listOf("Bertoua", "Buea", "Garoua", "Yaoundé"), 2,
            "Garoua lies on the banks of the Bénoué river.",
        ),
        QuizQuestion(
            "mbororo", "Which city is the capital of the Far North Region?",
            listOf("Maroua", "Bafoussam", "Ebolowa", "Douala"), 0,
            "Maroua is the capital of the Far North, the region closest to Lake Chad.",
        ),
        QuizQuestion(
            "tenda", "Which city is the capital of the East Region?",
            listOf("Bamenda", "Garoua", "Bertoua", "Buea"), 2,
            "Bertoua is the capital of the East, Cameroon's largest region.",
        ),
        QuizQuestion(
            "lea", "Which city is the capital of the South Region?",
            listOf("Ngaoundéré", "Ebolowa", "Maroua", "Douala"), 1,
            "Ebolowa is the capital of the South, a region known for its cocoa farms.",
        ),
        QuizQuestion(
            "wouri", "Which city is the capital of the North West Region?",
            listOf("Yaoundé", "Bertoua", "Bafoussam", "Bamenda"), 3,
            "Bamenda is the capital of the North West, in the Grassfields highlands.",
        ),
        QuizQuestion(
            "bakama", "Which city is the political capital of Cameroon?",
            listOf("Douala", "Yaoundé", "Garoua", "Kribi"), 1,
            "Yaoundé, the capital of the Centre Region, is also the national capital.",
        ),
        QuizQuestion(
            "njoh", "Which city is Cameroon's economic capital and busiest port?",
            listOf("Douala", "Limbe", "Kribi", "Bertoua"), 0,
            "Douala, capital of the Littoral Region, is the country's economic capital.",
        ),
        QuizQuestion(
            "wouri", "In the North West Grassfields, what title is given to a traditional ruler?",
            listOf("Fon", "Lamido", "Mayor", "Governor"), 0,
            "Fons lead traditional kingdoms of the Grassfields, such as Bafut and Nso.",
        ),
        QuizQuestion(
            "zanga", "In Ngaoundéré and Garoua, which title is used for Fulani traditional rulers?",
            listOf("Fon", "Lamido", "Pharaoh", "Emperor"), 1,
            "Lamidos lead lamidates such as Ngaoundéré, Garoua and Rey Bouba.",
        ),
        QuizQuestion(
            "njoh", "Ndolé, a famous Cameroonian dish, is made mainly with...",
            listOf("Bitter leaves", "Rice and beans", "Corn only", "Potatoes"), 0,
            "Ndolé is a stew of bitter leaves, groundnuts and fish or meat, loved in Douala and beyond.",
        ),
        QuizQuestion(
            "mbororo", "Waza National Park, famous for giraffes and elephants, is in which region?",
            listOf("Far North", "South", "Littoral", "West"), 0,
            "Waza National Park in the Far North is one of Cameroon's best-known parks.",
        ),
        QuizQuestion(
            "lea", "Near Kribi, the Lobé Falls are special because they...",
            listOf("Fall directly into the sea", "Freeze every year", "Flow uphill", "Are inside a cave"), 0,
            "The Lobé Falls are one of the rare waterfalls in the world that flow straight into the ocean.",
        ),
        QuizQuestion(
            "mbororo", "Which lake is shared by Cameroon, Chad, Niger and Nigeria?",
            listOf("Lake Chad", "Lake Nyos", "Lake Barombi", "Lake Victoria"), 0,
            "Lake Chad touches the very north of Cameroon's Far North Region.",
        ),
        QuizQuestion(
            "njoh", "Makossa music, made famous by Manu Dibango, was born in which city?",
            listOf("Douala", "Maroua", "Bertoua", "Ebolowa"), 0,
            "Manu Dibango's \"Soul Makossa\" (1972) carried this Douala rhythm around the world.",
        ),
        QuizQuestion(
            "bakama", "Bikutsi, a fast dance rhythm, comes from which people?",
            listOf("Beti", "Sawa", "Fulani", "Bamileke"), 0,
            "Bikutsi comes from the Beti peoples of the Centre region.",
        ),
        QuizQuestion(
            "tenda", "Which is the largest region of Cameroon by area?",
            listOf("East", "Littoral", "West", "North West"), 0,
            "The East Region covers about 109,000 km², mostly rainforest.",
        ),
        QuizQuestion(
            "tenda", "The Dja Faunal Reserve, a UNESCO World Heritage site, mainly protects...",
            listOf("Rainforest", "Desert dunes", "Glaciers", "Coral reefs"), 0,
            "The Dja reserve protects a huge area of rainforest home to gorillas and chimpanzees.",
        ),
        QuizQuestion(
            "yeme", "Every year, runners race up and down Mount Cameroon in the...",
            listOf("Race of Hope", "Tour de France", "Boston Marathon", "Great Trek"), 0,
            "The Mount Cameroon Race of Hope starts and finishes in Buea.",
        ),
        QuizQuestion(
            null, "What colours are on Cameroon's flag, from left to right?",
            listOf("Green, red, yellow", "Red, white, blue", "Yellow, green, red", "Green, white, green"), 0,
            "A yellow star shines in the middle of the red stripe.",
        ),
        QuizQuestion(
            null, "What are the two official languages of Cameroon?",
            listOf("French and English", "French and German", "English and Spanish", "Arabic and French"), 0,
            "Cameroon is bilingual, and more than 200 local languages are also spoken.",
        ),
    )

    /** Picks distinct questions and shuffles each one's answers. */
    fun pickRound(count: Int = PER_ROUND, random: Random = Random.Default): List<RoundQuestion> {
        require(count in 1..bank.size) { "A quiz round needs between 1 and ${bank.size} questions." }
        return bank.shuffled(random).take(count).map { question ->
            val order = question.options.indices.shuffled(random)
            RoundQuestion(question, order.map { question.options[it] }, order.indexOf(question.correct))
        }
    }
}
