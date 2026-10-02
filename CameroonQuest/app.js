/* STATE MANAGEMENT */
const state = {
    soundEnabled: true,
    player: {
        name: "Alex",
        level: 3,
        xp: 350,
        nextLevelXp: 500,
        score: 1250,
        gamesPlayed: 12,
        selectedCharId: 'njoh'
    },
    songo: {
        board: {
            aiPits: [5, 5, 5, 5, 5, 5],
            playerPits: [5, 5, 5, 5, 5, 5]
        },
        playerScore: 0,
        aiScore: 0,
        currentTurn: 'player'
    },
    quiz: {
        currentIndex: 0,
        score: 0,
        answered: false
    }
};

/* DATA: 10 Cameroonian Character Profiles */
const characters = [
    { id: 'njoh', name: 'Njoh', region: 'Littoral', color: 'bg-cyan-600', seed: 'Njoh', quote: '"The ocean is my home, and my culture my strength."', bio: 'Njoh comes from the Littoral region. He loves the sea, traditional music and Sawa culture.' },
    { id: 'bakama', name: 'Bakama', region: 'Centre', color: 'bg-amber-600', seed: 'Bakama', quote: '"In the heart of the forest, wisdom grows."', bio: 'Bakama represents the Beti cultural zone. She excels at strategic games like Songo.' },
    { id: 'mbororo', name: 'Mbororo', region: 'Far North', color: 'bg-blue-600', seed: 'Mbororo', quote: '"Endurance takes us across vast lands."', bio: 'Hailing from the Far North, Mbororo brings unmatched endurance.' },
    { id: 'wouri', name: 'Wouri', region: 'North West', color: 'bg-purple-600', seed: 'Wouri', quote: '"Unity and honor guide every path."', bio: 'Wouri is proud of the majestic North West grasslands.' },
    { id: 'nkem', name: 'Nkem', region: 'West', color: 'bg-emerald-600', seed: 'Nkem', quote: '"Respect for traditions leads to prosperity."', bio: 'Nkem comes from the West region, famous for royal chiefdoms.' },
    { id: 'yeme', name: 'Yeme', region: 'South West', color: 'bg-rose-600', seed: 'Yeme', quote: '"Nature speaks to those who listen."', bio: 'Yeme represents the lush South West around Mount Cameroon.' },
    { id: 'zanga', name: 'Zanga', region: 'Adamawa', color: 'bg-orange-600', seed: 'Zanga', quote: '"Highlands teach us to aim high."', bio: 'Zanga comes from the high plateaus of Adamawa.' },
    { id: 'tenda', name: 'Ténda', region: 'East', color: 'bg-teal-600', seed: 'Tenda', quote: '"The rainforest holds centuries of history."', bio: 'Ténda is deeply connected to the rainforests of Eastern Cameroon.' },
    { id: 'foufou', name: 'Foufou', region: 'North', color: 'bg-red-600', seed: 'Foufou', quote: '"Warmth and hospitality in every smile."', bio: 'Foufou represents Northern Cameroon with vibrant culture.' },
    { id: 'lea', name: 'Léa', region: 'Grand Nord', color: 'bg-indigo-600', seed: 'Lea', quote: '"Exploring the horizons with confidence."', bio: 'Léa represents the dynamic youth of Grand Nord.' }
];

/* DATA: Cultural Quiz */
const quizQuestions = [
    {
        image: 'https://images.unsplash.com/photo-1590523741831-ab7e8b8f9c7f?auto=format&fit=crop&w=600&q=80',
        question: 'What is the name of this traditional royal house architecture from the Grassfields region?',
        options: ['Fon Palace', 'Bale', 'Mbou', 'Ekom'],
        correct: 0
    },
    {
        image: 'https://images.unsplash.com/photo-1516026672322-bc52d61a55d5?auto=format&fit=crop&w=600&q=80',
        question: 'Which traditional mancala board game is widely popular in the Centre region of Cameroon?',
        options: ['Ludo', 'Songo', 'Dama', 'Awaele'],
        correct: 1
    }
];

/* AUDIO SYNTHESIZER */
function playSoundEffect(type) {
    if (!state.soundEnabled) return;
    try {
        const ctx = new (window.AudioContext || window.webkitAudioContext)();
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.connect(gain);
        gain.connect(ctx.destination);

        if (type === 'click') {
            osc.frequency.setValueAtTime(450, ctx.currentTime);
            osc.frequency.exponentialRampToValueAtTime(850, ctx.currentTime + 0.05);
            gain.gain.setValueAtTime(0.12, ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.05);
            osc.start();
            osc.stop(ctx.currentTime + 0.05);
        }
    } catch(e) {}
}

function switchView(viewId) {
    playSoundEffect('click');
    document.querySelectorAll('.view-screen').forEach(el => el.classList.add('hidden'));
    const target = document.getElementById(viewId);
    if (target) target.classList.remove('hidden');
}

/* INITIALIZATION */
window.onload = function() {
    console.log("Cameroon Quest Engine initialized.");
};
