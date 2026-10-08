const API_URL = 'http://localhost:8000/api';
let currentUser = null;
let currentActiveGame = null;
let currentGameDifficulty = 'easy';
let gameInterval = null;

// Avatar list representing 10 regions of Cameroon
const REGIONAL_AVATARS = [
    { name: 'Lion', region: 'Center (Yaoundé)', icon: '🦁' },
    { name: 'Sawa Dolphin', region: 'Littoral (Douala)', icon: '🐬' },
    { name: 'Grassfields Buffalo', region: 'West (Bafoussam)', icon: '🐃' },
    { name: 'Adamawa Zebu', region: 'Adamawa (Ngaoundéré)', icon: '🐂' },
    { name: 'North Elephant', region: 'North (Garoua)', icon: '🐘' },
    { name: 'Far North Camel', region: 'Far North (Maroua)', icon: '🐪' },
    { name: 'East Gorilla', region: 'East (Bertoua)', icon: '🦍' },
    { name: 'South Leopard', region: 'South (Ebolowa)', icon: '🐆' },
    { name: 'North-West Horse', region: 'North-West (Bamenda)', icon: '🐎' },
    { name: 'South-West Chimpanzee', region: 'South-West (Buea)', icon: '🐒' }
];

let selectedAvatarIndex = 0;

document.addEventListener('DOMContentLoaded', () => {
    initAuthScreen();
});

function showScreen(screenId) {
    document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
    document.getElementById(screenId).classList.add('active');
}

// --- AUTHENTICATION ---
function initAuthScreen() {
    const grid = document.getElementById('avatarGrid');
    if (!grid) return;
    grid.innerHTML = REGIONAL_AVATARS.map((av, idx) => `
        <div class="avatar-option ${idx === 0 ? 'selected' : ''}" onclick="selectAvatar(${idx})" title="${av.region}">
            ${av.icon}
        </div>
    `).join('');
}

function selectAvatar(idx) {
    selectedAvatarIndex = idx;
    document.querySelectorAll('.avatar-option').forEach((el, i) => {
        el.classList.toggle('selected', i === idx);
    });
}

async function handleLogin() {
    const username = document.getElementById('usernameInput').value.trim();
    const password = document.getElementById('passwordInput').value.trim();
    if (!username || !password) return alert('Please enter username and password.');

    try {
        const res = await fetch(`${API_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        const data = await res.json();
        if (res.ok) {
            currentUser = data;
            enterDashboard();
        } else {
            alert(data.error || 'Login failed.');
        }
    } catch (e) {
        alert('Server connection error. Ensure backend server is running on port 8000.');
    }
}

async function handleRegister() {
    const username = document.getElementById('usernameInput').value.trim();
    const password = document.getElementById('passwordInput').value.trim();
    if (!username || !password) return alert('Please enter username and password.');

    const av = REGIONAL_AVATARS[selectedAvatarIndex];
    try {
        const res = await fetch(`${API_URL}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password, avatar: av.icon, region: av.region })
        });
        const data = await res.json();
        if (res.ok) {
            currentUser = data;
            enterDashboard();
        } else {
            alert(data.error || 'Registration failed.');
        }
    } catch (e) {
        alert('Server connection error.');
    }
}

async function enterDashboard() {
    document.getElementById('userDisplayName').innerText = `${currentUser.avatar} ${currentUser.username}`;
    document.getElementById('userLives').innerText = currentUser.lives;
    document.getElementById('userXp').innerText = currentUser.xp;

    try {
        const res = await fetch(`${API_URL}/user/${currentUser.id}/progress`);
        const data = await res.json();
        if (res.ok) {
            currentUser.lives = data.user.lives;
            currentUser.xp = data.user.xp;
            updateDashboardScores(data.scores);
        }
    } catch (e) {}

    showScreen('dashboardScreen');
}

function updateDashboardScores(scores) {
    const games = ['songo', 'pirogue', 'dochi', 'quiz'];
    games.forEach(game => {
        const gameScores = scores.filter(s => s.game_name === game);
        const bestScore = gameScores.length ? Math.max(...gameScores.map(s => s.score)) : 0;
        const fillEl = document.getElementById(`${game}ScoreFill`);
        const textEl = document.getElementById(`${game}ScoreText`);
        if (fillEl && textEl) {
            const pct = Math.min(100, (bestScore / 500) * 100);
            fillEl.style.width = `${pct}%`;
            textEl.innerText = `Best Score: ${bestScore}`;
        }
    });
}

function logout() {
    currentUser = null;
    showScreen('authScreen');
}

// --- GAME ROUTING & HELP ---
function launchGame(gameName) {
    currentActiveGame = gameName;
    document.getElementById('gameTitleHeading').innerText = gameName.toUpperCase() + ' QUEST';
    showScreen('gameScreen');
    setDifficulty('easy');
}

function setDifficulty(diff) {
    currentGameDifficulty = diff;
    ['easy', 'medium', 'hard'].forEach(d => {
        const btn = document.getElementById(`btn-${d}`);
        if (btn) btn.classList.toggle('btn-yellow', d === diff);
    });
    startCurrentGameLoop();
}

function startCurrentGameLoop() {
    if (gameInterval) clearInterval(gameInterval);
    const canvas = document.getElementById('gameCanvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    canvas.width = 700;
    canvas.height = 400;

    if (currentActiveGame === 'songo') initSongoGame(ctx, canvas);
    else if (currentActiveGame === 'pirogue') initPirogueGame(ctx, canvas);
    else if (currentActiveGame === 'dochi') initDochiGame(ctx, canvas);
}

function openHelpModal() {
    const helps = {
        songo: "Songo (Center Region): Traditional Beti seed board game. Click your pits to sow seeds. Visually watch small seed balls fill and empty across the wooden board to win, lose, or draw against AI!",
        pirogue: "Pirogue Racing (Littoral Region): Race along the Wouri river with blue water, vivid landscape banks, and finish lines across Easy, Medium, and Hard track lengths.",
        dochi: "Dochi (West Grassfields): Agility test! Two throwers stand at opposite ends of the arena throwing balls toward you in the middle. Use Arrow keys or WASD to move in all directions to survive."
    };
    document.getElementById('helpTextContent').innerText = helps[currentActiveGame] || "Explore and enjoy Cameroonian culture!";
    document.getElementById('helpModal').classList.remove('hidden');
}

function closeHelpModal() {
    document.getElementById('helpModal').classList.add('hidden');
}

// --- MINI GAME 1: SONGO (Realistic Wood Board & Small Balls) ---
function initSongoGame(ctx, canvas) {
    let board = [4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4];
    let turn = 'player';
    let statusMsg = "Your Turn! Click a pit (bottom row)";

    function drawBoard() {
        // Wood Board Background
        ctx.fillStyle = '#78350F';
        ctx.fillRect(40, 60, 620, 280);
        ctx.strokeStyle = '#FCD116';
        ctx.lineWidth = 4;
        ctx.strokeRect(40, 60, 620, 280);

        ctx.fillStyle = '#FCD116';
        ctx.font = '15px sans-serif';
        ctx.fillText(`SONGO (${currentGameDifficulty.toUpperCase()}) - ${statusMsg}`, 55, 90);

        // Draw 12 Pits and Small Balls Inside
        for (let i = 0; i < 12; i++) {
            let x = 95 + (i % 6) * 95;
            let y = i < 6 ? 260 : 150; // Bottom row vs Top row

            // Pit depression circle
            ctx.beginPath();
            ctx.arc(x, y, 36, 0, Math.PI * 2);
            ctx.fillStyle = '#451A03';
            ctx.fill();
            ctx.lineWidth = 3;
            ctx.strokeStyle = '#F59E0B';
            ctx.stroke();

            // Draw individual small seed balls inside the pit
            let seedCount = board[i];
            let radius = 5;
            for (let s = 0; s < seedCount; s++) {
                let angle = (s / Math.max(1, seedCount)) * Math.PI * 2;
                let rOffset = seedCount > 6 ? 16 : 10;
                let sx = x + Math.cos(angle) * rOffset;
                let sy = y + Math.sin(angle) * rOffset;

                ctx.beginPath();
                ctx.arc(sx, sy, radius, 0, Math.PI * 2);
                ctx.fillStyle = '#10B981'; // Emerald seed color
                ctx.fill();
                ctx.strokeStyle = '#FFFFFF';
                ctx.lineWidth = 1;
                ctx.stroke();
            }

            // Total number text
            ctx.fillStyle = '#FFFFFF';
            ctx.font = 'bold 13px sans-serif';
            ctx.fillText(seedCount, x - 6, y + 4);
        }
    }

    drawBoard();

    canvas.onclick = (e) => {
        if (turn !== 'player') return;
        const rect = canvas.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;

        // Player clicks bottom pits (indices 0 to 5)
        for (let i = 0; i < 6; i++) {
            let cx = 95 + (i % 6) * 95;
            let cy = 260;
            if (Math.hypot(x - cx, y - cy) < 36 && board[i] > 0) {
                executeSongoMove(i);
                break;
            }
        }
    };

    function executeSongoMove(idx) {
        let seeds = board[idx];
        board[idx] = 0;
        let curr = idx;
        while (seeds > 0) {
            curr = (curr + 1) % 12;
            if (curr !== idx) {
                board[curr]++;
                seeds--;
            }
        }
        drawBoard();

        let playerTotal = board.slice(0, 6).reduce((a,b)=>a+b, 0);
        let aiTotal = board.slice(6, 12).reduce((a,b)=>a+b, 0);

        if (playerTotal === 0 || aiTotal === 0) {
            let outcome = playerTotal > aiTotal ? 'win' : (playerTotal < aiTotal ? 'loss' : 'draw');
            statusMsg = `Game Over! You ${outcome.toUpperCase()}`;
            drawBoard();
            submitGameScore('songo', currentGameDifficulty, playerTotal * 15, outcome, outcome === 'win' ? 50 : 10, outcome === 'loss' ? -1 : 0);
            return;
        }

        turn = 'ai';
        statusMsg = "Beti AI Thinking...";
        drawBoard();

        setTimeout(() => {
            let aiIdx = 6 + Math.floor(Math.random() * 6);
            while(board[aiIdx] === 0) {
                aiIdx = 6 + Math.floor(Math.random() * 6);
            }
            let aSeeds = board[aiIdx];
            board[aiIdx] = 0;
            let c = aiIdx;
            while(aSeeds > 0) {
                c = (c + 1) % 12;
                if(c !== aiIdx) {
                    board[c]++;
                    aSeeds--;
                }
            }
            turn = 'player';
            statusMsg = "Your Turn!";
            drawBoard();
        }, 900);
    }
}

// --- MINI GAME 2: PIROGUE RACING ---
function initPirogueGame(ctx, canvas) {
    let trackLength = currentGameDifficulty === 'easy' ? 2500 : (currentGameDifficulty === 'medium' ? 5000 : 8500);
    let playerDist = 0;
    let aiDist = 0;
    let speed = currentGameDifficulty === 'easy' ? 5 : (currentGameDifficulty === 'medium' ? 7 : 9);
    let keys = {};

    window.onkeydown = (e) => keys[e.key] = true;
    window.onkeyup = (e) => keys[e.key] = false;

    gameInterval = setInterval(() => {
        if (keys['ArrowUp'] || keys['w']) playerDist += speed;
        aiDist += speed * (0.88 + Math.random() * 0.22);

        // Vibrant Blue Water
        ctx.fillStyle = '#0284C7';
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        // Landscapes & Mangroves on banks
        ctx.fillStyle = '#166534';
        ctx.fillRect(0, 0, canvas.width, 55);
        ctx.fillRect(0, 345, canvas.width, 55);

        // Finish Line
        ctx.fillStyle = '#FCD116';
        ctx.fillRect(620, 55, 8, 290);

        // Player Pirogue
        let playerX = 80 + Math.min(520, (playerDist / trackLength) * 520);
        ctx.fillStyle = '#CE1126';
        ctx.fillRect(playerX, 150, 65, 26);
        ctx.fillStyle = '#FFFFFF';
        ctx.font = '12px sans-serif';
        ctx.fillText("🛶 You", playerX, 142);

        // AI Pirogue
        let aiX = 80 + Math.min(520, (aiDist / trackLength) * 520);
        ctx.fillStyle = '#007A3D';
        ctx.fillRect(aiX, 230, 65, 26);
        ctx.fillStyle = '#FFFFFF';
        ctx.fillText("🛶 Rival", aiX, 222);

        // HUD
        ctx.fillStyle = '#FFFFFF';
        ctx.fillText(`WOURI RIVER RACE (${currentGameDifficulty.toUpperCase()}) - ${Math.floor(playerDist)}m / ${trackLength}m`, 25, 30);

        if (playerDist >= trackLength || aiDist >= trackLength) {
            clearInterval(gameInterval);
            let won = playerDist >= aiDist;
            alert(won ? "Victory! You won the Wouri River Pirogue Race!" : "Defeat! Rival pirogue crossed first.");
            submitGameScore('pirogue', currentGameDifficulty, Math.floor(playerDist / 10), won ? 'win' : 'loss', won ? 70 : 15, won ? 0 : -1);
        }
    }, 30);
}

// --- MINI GAME 3: DOCHI (Two Throwers at Both Ends & Middle Player) ---
function initDochiGame(ctx, canvas) {
    let player = { x: 350, y: 200, radius: 14, speed: 6 };
    let balls = [
        { x: 50, y: 200, vx: 5, vy: 2 },
        { x: 650, y: 200, vx: -5, vy: -2 }
    ];
    let score = 0;
    let keys = {};

    window.onkeydown = (e) => keys[e.key] = true;
    window.onkeyup = (e) => keys[e.key] = false;

    gameInterval = setInterval(() => {
        score++;
        if (keys['ArrowUp'] || keys['w']) player.y = Math.max(65, player.y - player.speed);
        if (keys['ArrowDown'] || keys['s']) player.y = Math.min(335, player.y + player.speed);
        if (keys['ArrowLeft'] || keys['a']) player.x = Math.max(70, player.x - player.speed);
        if (keys['ArrowRight'] || keys['d']) player.x = Math.min(630, player.x + player.speed);

        // Arena Background & Floor
        ctx.fillStyle = '#0F172A';
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        ctx.strokeStyle = '#334155';
        ctx.lineWidth = 2;
        ctx.strokeRect(40, 50, 620, 300);

        // --- DRAW TWO THROWERS AT BOTH ENDS (Left & Right) ---
        // Left Thrower
        ctx.fillStyle = '#007A3D';
        ctx.beginPath();
        ctx.arc(30, 200, 18, 0, Math.PI * 2);
        ctx.fill();
        ctx.fillStyle = '#FFFFFF';
        ctx.font = '10px sans-serif';
        ctx.fillText("Thrower 1", 5, 230);

        // Right Thrower
        ctx.fillStyle = '#CE1126';
        ctx.beginPath();
        ctx.arc(670, 200, 18, 0, Math.PI * 2);
        ctx.fill();
        ctx.fillStyle = '#FFFFFF';
        ctx.fillText("Thrower 2", 645, 230);

        // Update and draw balls thrown across
        balls.forEach(b => {
            b.x += b.vx * (currentGameDifficulty === 'hard' ? 1.4 : (currentGameDifficulty === 'medium' ? 1.2 : 1));
            b.y += b.vy * (currentGameDifficulty === 'hard' ? 1.4 : (currentGameDifficulty === 'medium' ? 1.2 : 1));

            if (b.x < 45 || b.x > 655) b.vx *= -1;
            if (b.y < 55 || b.y > 345) b.vy *= -1;

            ctx.beginPath();
            ctx.arc(b.x, b.y, 10, 0, Math.PI * 2);
            ctx.fillStyle = '#FCD116';
            ctx.fill();

            // Collision check
            let dist = Math.hypot(player.x - b.x, player.y - b.y);
            if (dist < player.radius + 10) {
                clearInterval(gameInterval);
                alert(`Hit by thrower ball! Dochi Game Over. Score: ${score}`);
                submitGameScore('dochi', currentGameDifficulty, score, 'loss', 25, -1);
            }
        });

        // --- DRAW PLAYER IN THE MIDDLE ---
        ctx.beginPath();
        ctx.arc(player.x, player.y, player.radius, 0, Math.PI * 2);
        ctx.fillStyle = '#FCD116';
        ctx.fill();
        ctx.lineWidth = 2;
        ctx.strokeStyle = '#007A3D';
        ctx.stroke();

        ctx.fillStyle = '#FFFFFF';
        ctx.fillText(`DOCHI SURVIVAL (${currentGameDifficulty.toUpperCase()}) - Score: ${score}`, 50, 32);

        if (score > 700) {
            clearInterval(gameInterval);
            alert(`Amazing Agility! You survived Dochi! Score: ${score}`);
            submitGameScore('dochi', currentGameDifficulty, score, 'win', 100, 1);
        }
    }, 30);
}

// --- QUIZ SYSTEM (Fixed Backend Fetch) ---
async function startQuiz() {
    try {
        const res = await fetch(`${API_URL}/quiz`);
        const questions = await res.json();
        if (!questions || !questions.length) return alert('No quiz questions found on server.');
        
        let qIdx = 0;
        let correctCount = 0;

        function askNext() {
            if (qIdx >= questions.length) {
                alert(`Quiz Finished! You got ${correctCount}/${questions.length} correct. Earned +1 Life and +50 XP!`);
                submitGameScore('quiz', 'medium', correctCount * 50, correctCount >= 3 ? 'win' : 'loss', 50, 1);
                enterDashboard();
                return;
            }

            let q = questions[qIdx];
            let ans = prompt(`[Cameroon Cultural Quiz - Region: ${q.region}]\n\n${q.question}\nA) ${q.option_a}\nB) ${q.option_b}\nC) ${q.option_c}\nD) ${q.option_d}\n\nEnter your answer letter (A, B, C, or D):`);
            
            if (!ans) { enterDashboard(); return; }

            fetch(`${API_URL}/quiz/verify`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ question_id: q.id, answer: ans.trim() })
            }).then(r => r.json()).then(data => {
                if (data.correct) {
                    correctCount++;
                    alert('Correct! 🎉');
                } else {
                    alert(`Incorrect. Correct answer was: ${data.correctAnswer}`);
                }
                qIdx++;
                askNext();
            });
        }

        askNext();
    } catch (e) {
        alert('Could not connect to backend server for quiz questions. Make sure npm start is running on port 8000.');
    }
}

// --- SUBMIT SCORE TO BACKEND ---
async function submitGameScore(gameName, difficulty, score, status, xpGained, livesDelta) {
    try {
        const res = await fetch(`${API_URL}/scores`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                user_id: currentUser.id,
                game_name: gameName,
                difficulty,
                score,
                status,
                xp_gained: xpGained,
                lives_delta: livesDelta
            })
        });
        const data = await res.json();
        if (res.ok) {
            currentUser.lives = data.updatedUser.lives;
            currentUser.xp = data.updatedUser.xp;
            enterDashboard();
        }
    } catch (e) {}
}