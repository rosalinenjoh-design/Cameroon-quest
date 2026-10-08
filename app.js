'use strict';

const Games = window.QuestGames;
const API_URL = '/api';
const GUEST_KEY = 'cameroonquest.guest.v1';
const $ = id => document.getElementById(id);
const AVATARS = [
    { icon: '🦁', region: 'Centre (Yaounde)' },
    { icon: '🐬', region: 'Littoral (Douala)' },
    { icon: '🐃', region: 'West (Bafoussam)' },
    { icon: '🐂', region: 'Adamawa (Ngaoundere)' },
    { icon: '🐘', region: 'North (Garoua)' },
    { icon: '🐪', region: 'Far North (Maroua)' },
    { icon: '🦍', region: 'East (Bertoua)' },
    { icon: '🐆', region: 'South (Ebolowa)' },
    { icon: '🐎', region: 'North-West (Bamenda)' },
    { icon: '🐒', region: 'South-West (Buea)' }
];
const GAME_INFO = {
    songo: { title: 'Songo Board', region: 'Centre / Beti-inspired arcade', description: 'A game of patience and strategy. Sow seeds and capture more than your opponent.', maxScore: 1000 },
    pirogue: { title: 'Pirogue Regatta', region: 'Littoral / Wouri River', description: 'Both boats paddle automatically. Steer around logs: each collision slows you down.', maxScore: 1000 },
    dochi: { title: 'Dochi Dodgeball', region: 'Grassfields-inspired agility', description: 'Dodge aimed throws from both sides. Survive the full timer to win.', maxScore: 450 },
    quiz: { title: 'Cultural Quiz', region: 'Discover Cameroon', description: 'Five questions. Two players answer the same questions, with answers hidden until both have chosen.', maxScore: 250 }
};
const RULES = {
    songo: `<p>This is a <strong>five-pit arcade adaptation</strong> inspired by Songo, not a complete traditional or tournament ruleset.</p>
        <ul><li>Each side starts with five pits of five seeds. Player 1 uses the bottom row; Player 2 or the AI uses the top row.</li>
        <li>Choose a highlighted pit on your side. Sow one seed per pit: along the bottom from left to right, then along the top from right to left. Skip the source pit on a full lap.</li>
        <li>If the last seed lands on the opposing side and makes 2 or 3 seeds, capture them. Continue backward through consecutive opposing pits containing 2 or 3.</li>
        <li>A capture that would empty the opposing row is cancelled. If that row is already empty, you must feed it when possible.</li>
        <li>The round ends when someone captures a majority, no legal move remains, a position repeats three times, or 200 turns are reached. Each player collects seeds left on their side; the higher total wins.</li></ul>
        <h3>Play together</h3><p>Take turns tapping or clicking your row. On a keyboard, Tab to a highlighted pit and press Enter. Solo difficulty changes the AI's planning strength.</p>`,
    pirogue: `<p>Steer your pirogue down the Wouri-inspired course. Boats paddle automatically at the same base speed.</p>
        <ul><li>Yellow is Player 1. Red is Player 2 or the AI. Each boat has its own lane and the same arrangement of logs.</li>
        <li>Hold the left/right touch buttons, or use A / D for Player 1. Player 2 uses the left/right arrow keys. In solo mode, either keyboard pair controls you.</li>
        <li>Hitting a log slows that boat for 1.35 seconds. Each log can hit a boat only once.</li>
        <li>The first boat to finish wins. An exact photo-finish is a draw. Harder courses are longer, faster, and have more obstacles.</li></ul>
        <h3>On a shared phone</h3><p>Each player holds their own pair of buttons. Simultaneous touches are supported. Space pauses on PC; the Pause button works on all devices.</p>`,
    dochi: `<p>Dochi remains a <strong>one-player</strong> survival challenge.</p>
        <ul><li>Move the green player with WASD, arrow keys, or the touch direction pad. Diagonal movement is supported and is not faster than straight movement.</li>
        <li>The red throwers alternate aimed shots. Keep moving; staying still is not safe.</li>
        <li>One ball hit ends the round. Survive 30 seconds on Easy, 40 on Medium, or 45 on Hard to win.</li>
        <li>Earn 10 score points per second survived. A win restores one heart, up to five.</li></ul>
        <p>Use Pause or Space for a break. Switching away from the page pauses the game automatically.</p>`,
    quiz: `<p>Learn something new with five cultural and regional questions.</p>
        <ul><li>Choose one answer per question. Correct answers earn 50 score points and 10 XP.</li>
        <li>Get at least three correct to restore one heart, up to five. Incorrect answers do not award XP or cost a heart.</li>
        <li>In two-player mode, pass the device when prompted. Both players answer the same question before the correct answer or updated scores are revealed.</li>
        <li>The starting player alternates each question. The most correct answers wins; equal scores draw.</li></ul>
        <p>Guest questions work without an account. Account quizzes use the server's question bank. Player 2 is a local guest; saved progress belongs to Player 1.</p>`
};

let currentUser = null;
let bestScores = emptyBests();
let selectedAvatar = 0;
let avatarChanged = false;
let authMode = 'login';
let setupGame = null;
let match = null;
let pendingConfirmation = null;
let progressVersion = 0;
let soundEnabled = false;
let audioContext = null;

function emptyBests() {
    return { songo: 0, pirogue: 0, dochi: 0, quiz: 0 };
}

function notice(message, error = false) {
    $('appNotice').textContent = message;
    $('appNotice').classList.toggle('error', error);
    $('appNotice').hidden = !message;
}

function reportError(message, error) {
    console.error(message, error);
    notice(`${message} ${error.message}`, true);
}

async function api(route, { method = 'GET', body, signal } = {}) {
    const controller = new AbortController();
    let timedOut = false;
    const abort = () => controller.abort();
    if (signal?.aborted) controller.abort();
    signal?.addEventListener('abort', abort, { once: true });
    const timeout = setTimeout(() => {
        timedOut = true;
        controller.abort();
    }, 12000);
    try {
        const response = await fetch(`${API_URL}${route}`, {
            method, signal: controller.signal,
            headers: body ? { 'Content-Type': 'application/json' } : undefined,
            body: body ? JSON.stringify(body) : undefined
        });
        if (!response.headers.get('content-type')?.includes('application/json')) {
            throw new Error('Open the app through the Node server (npm start), not a separate live server.');
        }
        const data = await response.json();
        if (!response.ok) throw new Error(data.error || `Request failed (${response.status}).`);
        return data;
    } catch (error) {
        if (timedOut) throw new Error('The server took too long to respond. Check your connection and try again.');
        throw error;
    } finally {
        clearTimeout(timeout);
        signal?.removeEventListener('abort', abort);
    }
}

function showScreen(id) {
    document.querySelectorAll('.screen').forEach(screen => { screen.hidden = screen.id !== id; });
    $('mainContent').focus({ preventScroll: true });
    window.scrollTo({ top: 0, behavior: 'instant' });
}

function renderHeader() {
    $('playerAvatar').textContent = currentUser?.avatar || AVATARS[selectedAvatar].icon;
    $('userDisplayName').textContent = currentUser?.username || 'Guest Player';
    $('userXp').textContent = (currentUser?.xp || 0).toLocaleString();
    $('userLives').textContent = currentUser?.lives ?? 5;
    $('playerLevel').textContent = `Level ${1 + Math.floor((currentUser?.xp || 0) / 250)}`;
}

function renderDashboard() {
    for (const [game, info] of Object.entries(GAME_INFO)) {
        $(`${game}ScoreText`).textContent = `Best: ${bestScores[game].toLocaleString()}`;
        $(`${game}ScoreFill`).style.width = `${Math.min(100, bestScores[game] / info.maxScore * 100)}%`;
    }
    $('progressNote').textContent = currentUser?.isGuest
        ? 'Guest progress stays in this browser. Local Player 2 does not need an account.'
        : 'Completed rounds save to your profile. Local Player 2 joins as a guest.';
}

async function refreshProgress() {
    if (!currentUser || currentUser.isGuest) return;
    const user = currentUser;
    const version = ++progressVersion;
    try {
        const data = await api(`/user/${user.id}/progress`);
        if (currentUser !== user || version !== progressVersion) return;
        if (!data.user || !Array.isArray(data.scores)) throw new Error('The server returned invalid progress data.');
        currentUser.lives = data.user.lives;
        currentUser.xp = data.user.xp;
        bestScores = emptyBests();
        for (const score of data.scores) {
            if (Object.hasOwn(bestScores, score.game_name)) bestScores[score.game_name] = Math.max(bestScores[score.game_name], score.score);
        }
        renderHeader();
        renderDashboard();
    } catch (error) {
        if (currentUser === user && version === progressVersion) reportError('Could not load saved progress.', error);
    }
}

function saveGuest() {
    localStorage.setItem(GUEST_KEY, JSON.stringify({ version: 1, user: currentUser, bests: bestScores }));
}

function startGuest() {
    notice('');
    let saved = null;
    try {
        const raw = localStorage.getItem(GUEST_KEY);
        if (raw) {
            saved = JSON.parse(raw);
            if (saved.version !== 1 || typeof saved.user?.username !== 'string' ||
                typeof saved.user.avatar !== 'string' || typeof saved.user.region !== 'string' ||
                !Number.isSafeInteger(saved.user.xp) || saved.user.xp < 0 ||
                !Number.isInteger(saved.user.lives) || saved.user.lives < 0 || saved.user.lives > 5 ||
                !saved.bests || Object.keys(emptyBests()).some(game => !Number.isFinite(saved.bests[game]) || saved.bests[game] < 0)) {
                throw new Error('The saved guest profile has an unsupported format.');
            }
        }
    } catch (error) {
        saved = null;
        reportError('Guest progress could not be restored; this is a new session.', error);
    }
    const avatar = AVATARS[selectedAvatar];
    currentUser = saved ? { ...saved.user, isGuest: true, id: null } : {
        id: null, username: 'Guest Warrior', avatar: avatar.icon, region: avatar.region, lives: 5, xp: 0, isGuest: true
    };
    bestScores = saved ? { ...saved.bests } : emptyBests();
    const name = $('usernameInput').value.trim();
    if (name) currentUser.username = name;
    if (avatarChanged) {
        currentUser.avatar = avatar.icon;
        currentUser.region = avatar.region;
    }
    $('passwordInput').value = '';
    try { saveGuest(); } catch (error) { reportError('Browser storage is unavailable; progress will last only for this session.', error); }
    renderHeader();
    renderDashboard();
    showScreen('dashboardScreen');
}

async function authenticate(event) {
    event.preventDefault();
    $('authError').hidden = true;
    const buttons = ['authSubmit', 'authModeToggle', 'guestPlay'];
    buttons.forEach(id => { $(id).disabled = true; });
    $('authSubmit').textContent = 'Please wait...';
    try {
        const avatar = AVATARS[selectedAvatar];
        const data = await api(`/${authMode}`, {
            method: 'POST',
            body: { username: $('usernameInput').value.trim(), password: $('passwordInput').value, avatar: avatar.icon, region: avatar.region }
        });
        if (!Number.isInteger(data.id) || !Number.isFinite(data.xp) || !Number.isInteger(data.lives)) throw new Error('The server returned an invalid player profile.');
        currentUser = { ...data, isGuest: false };
        bestScores = emptyBests();
        $('passwordInput').value = '';
        notice('');
        renderHeader();
        renderDashboard();
        showScreen('dashboardScreen');
        await refreshProgress();
    } catch (error) {
        console.error('Account request failed', error);
        $('authError').textContent = `${error.message} Guest play is also available.`;
        $('authError').hidden = false;
    } finally {
        buttons.forEach(id => { $(id).disabled = false; });
        $('authSubmit').textContent = authMode === 'login' ? 'Log In' : 'Create Account';
    }
}

function stopMatch() {
    const previous = match;
    match = null;
    if (!previous) return;
    previous.controller.abort();
    clearTimeout(previous.aiTimer);
    if (previous.frame !== null) cancelAnimationFrame(previous.frame);
    clearInput(previous);
}

function returnToMenu() {
    stopMatch();
    for (const dialog of document.querySelectorAll('dialog[open]')) dialog.close();
    renderHeader();
    renderDashboard();
    showScreen('dashboardScreen');
}

function setupMode() {
    return setupGame === 'dochi' ? 'solo' : document.querySelector('input[name="playMode"]:checked').value;
}

function renderSetup() {
    const mode = setupMode();
    $('modePicker').hidden = setupGame === 'dochi';
    $('soloOnlyNote').hidden = setupGame !== 'dochi';
    $('secondPlayerField').hidden = mode !== 'local';
    $('difficultyField').hidden = setupGame === 'quiz';
    $('difficultySelect').disabled = setupGame === 'songo' && mode === 'local';
    $('difficultyHelp').textContent = setupGame === 'songo'
        ? (mode === 'local' ? 'Both players use the same arcade rules. Difficulty applies only to the AI.' : 'Easy makes random legal moves; Medium and Hard plan ahead.')
        : 'Difficulty changes the course or the survival challenge equally for everyone.';
    const controls = {
        songo: mode === 'local' ? 'Take turns: Player 1 taps the bottom row; Player 2 taps the top row.' : 'Tap your bottom-row pits. The AI plays the top row.',
        pirogue: mode === 'local' ? 'PC: Player 1 uses A / D; Player 2 uses left / right arrows. Phone: each player holds their own touch buttons.' : 'PC: A / D or arrow keys. Phone: hold the left/right buttons. Boats paddle automatically.',
        dochi: 'PC: WASD or arrow keys. Phone: hold the direction pad. Dochi is always one player.',
        quiz: mode === 'local' ? 'Pass the phone or share the mouse. Correct answers stay hidden until both players have answered.' : 'Choose an answer. Three or more correct answers restore one heart.'
    };
    $('setupControls').textContent = controls[setupGame];
}

function openSetup(game) {
    setupGame = game;
    $('setupTitle').textContent = GAME_INFO[game].title;
    $('setupDescription').textContent = GAME_INFO[game].description;
    if (game === 'dochi') document.querySelector('input[name="playMode"][value="solo"]').checked = true;
    renderSetup();
    $('setupDialog').showModal();
}

function clearInput(round) {
    round.keys.clear();
    round.pointers.clear();
    document.querySelectorAll('.control-button.pressed').forEach(button => button.classList.remove('pressed'));
}

function held(round, control, ...keys) {
    return [...round.pointers.values()].includes(control) || keys.some(key => round.keys.has(key));
}

function controlsFor(round) {
    if (round.game === 'dochi') {
        return {
            x: Number(held(round, 'right', 'KeyD', 'ArrowRight')) - Number(held(round, 'left', 'KeyA', 'ArrowLeft')),
            y: Number(held(round, 'down', 'KeyS', 'ArrowDown')) - Number(held(round, 'up', 'KeyW', 'ArrowUp'))
        };
    }
    const left = round.mode === 'solo' ? ['KeyA', 'ArrowLeft'] : ['KeyA'];
    const right = round.mode === 'solo' ? ['KeyD', 'ArrowRight'] : ['KeyD'];
    return [
        Number(held(round, 'p1-right', ...right)) - Number(held(round, 'p1-left', ...left)),
        Number(held(round, 'p2-right', 'ArrowRight')) - Number(held(round, 'p2-left', 'ArrowLeft'))
    ];
}

async function startMatch(config) {
    stopMatch();
    notice('');
    const round = {
        ...config, controller: new AbortController(), keys: new Set(), pointers: new Map(),
        phase: 'playing', engine: null, frame: null, aiTimer: null, lastTimestamp: null,
        quizBusy: false, quizError: '', correctIndex: null, selections: [], saving: false
    };
    match = round;
    $('gameTitleHeading').textContent = GAME_INFO[round.game].title;
    $('gameRegion').textContent = GAME_INFO[round.game].region;
    $('modeBadge').textContent = round.mode === 'local' ? '2 players / shared device' : 'Solo adventure';
    $('difficultyBadge').textContent = round.game === 'quiz' ? '5 questions' : (round.game === 'songo' && round.mode === 'local' ? 'Arcade rules' : `${round.difficulty[0].toUpperCase()}${round.difficulty.slice(1)}`);
    $('songoArena').hidden = round.game !== 'songo';
    $('canvasArena').hidden = !['pirogue', 'dochi'].includes(round.game);
    $('quizArena').hidden = round.game !== 'quiz';
    $('pauseGame').hidden = !['pirogue', 'dochi'].includes(round.game);
    showScreen('gameScreen');
    if (round.game === 'songo') {
        round.engine = Games.createSongo();
        buildSongoBoard();
        renderSongo(round);
    } else if (round.game === 'quiz') {
        await loadQuiz(round);
    } else {
        round.engine = round.game === 'pirogue' ? Games.createRace(round.difficulty, round.mode) : Games.createDochi(round.difficulty);
        round.context = $('gameCanvas').getContext('2d');
        if (!round.context) {
            notice('Canvas is unavailable in this browser. Try a current browser to play this game.', true);
            returnToMenu();
            return;
        }
        round.phase = 'ready';
        $('raceControls').hidden = round.game !== 'pirogue';
        $('dochiControls').hidden = round.game !== 'dochi';
        $('raceControlsTwoWrap').hidden = round.mode !== 'local';
        $('raceControlsOne').textContent = `${round.names[0]} / P1`;
        $('raceControlsTwo').textContent = `${round.names[1]} / P2`;
        $('meterTwoWrap').hidden = round.game !== 'pirogue';
        $('meterOneLabel').textContent = round.game === 'pirogue' ? round.names[0] : 'Survival time';
        $('meterTwoLabel').textContent = round.names[1];
        $('controlsHint').textContent = round.game === 'pirogue'
            ? 'Avoid the brown logs. Both boats follow the same course. Hold a direction to steer.'
            : 'Yellow balls are aimed at you. Stay inside the court and keep moving.';
        renderCanvas(round);
        renderPause(round);
    }
}

function buildSongoBoard() {
    for (const [id, pits] of [['topPits', [9, 8, 7, 6, 5]], ['bottomPits', [0, 1, 2, 3, 4]]]) {
        $(id).replaceChildren(...pits.map(pit => {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'pit';
            button.dataset.pit = pit;
            button.innerHTML = `<span class="pit-label">Pit ${pit + 1}</span><span class="pit-count"><span class="seed" aria-hidden="true"></span><span class="seed-count">5</span></span>`;
            return button;
        }));
    }
}

function renderSongo(round) {
    const state = round.engine;
    const playable = Games.legalSongoMoves(state);
    document.querySelectorAll('.pit').forEach(button => {
        const pit = Number(button.dataset.pit);
        const player = pit < 5 ? 0 : 1;
        button.querySelector('.seed-count').textContent = state.board[pit];
        button.disabled = round.phase !== 'playing' || !playable.includes(pit) || (round.mode === 'solo' && state.turn === 1);
        button.classList.toggle('last-move', state.lastPit === pit);
        button.setAttribute('aria-label', `${round.names[player]}, pit ${pit + 1}, ${state.board[pit]} seeds`);
    });
    $('topSideLabel').textContent = `${round.names[1]} / top row`;
    $('bottomSideLabel').textContent = `${round.names[0]} / bottom row`;
    $('songoPlayerOne').textContent = round.names[0];
    $('songoPlayerTwo').textContent = round.names[1];
    $('songoScoreOne').textContent = `${state.captured[0]} seeds`;
    $('songoScoreTwo').textContent = `${state.captured[1]} seeds`;
    $('gameStatus').textContent = state.over ? 'Round complete' : (round.mode === 'solo' && state.turn === 1
        ? 'The AI is thinking...' : `${round.names[state.turn]}, your turn!`);
}

function playSongo(pit) {
    const round = match;
    if (!round || round.game !== 'songo' || round.phase !== 'playing' ||
        (round.mode === 'solo' && round.engine.turn === 1)) return;
    round.engine = Games.moveSongo(round.engine, pit);
    sound('move');
    renderSongo(round);
    if (round.engine.over) finishMatch(round);
    else scheduleSongoAI(round);
}

function scheduleSongoAI(round) {
    if (match !== round || round.game !== 'songo' || round.mode !== 'solo' || round.phase !== 'playing' ||
        round.engine.turn !== 1 || round.aiTimer !== null || document.hidden || document.querySelector('dialog[open]')) return;
    round.aiTimer = setTimeout(() => {
        round.aiTimer = null;
        if (match !== round || round.phase !== 'playing') return;
        round.engine = Games.moveSongo(round.engine, Games.chooseSongoMove(round.engine, round.difficulty));
        renderSongo(round);
        if (round.engine.over) finishMatch(round);
    }, 650);
}

function suspendMatch() {
    if (!match) return;
    clearTimeout(match.aiTimer);
    match.aiTimer = null;
    if (['pirogue', 'dochi'].includes(match.game) && match.phase === 'playing') {
        match.phase = 'paused';
        if (match.frame !== null) cancelAnimationFrame(match.frame);
        match.frame = null;
        match.lastTimestamp = null;
        clearInput(match);
        renderPause(match);
    }
}

function renderPause(round) {
    const ready = round.phase === 'ready';
    $('pauseOverlay').hidden = round.phase === 'playing' || round.phase === 'finished';
    $('pauseEyebrow').textContent = ready ? 'Ready when you are' : 'Take a breath';
    $('pauseTitle').textContent = ready ? (round.mode === 'local' ? 'Both players ready?' : 'Ready to play?') : 'Game paused';
    $('pauseDescription').textContent = ready
        ? (round.game === 'pirogue' ? 'Boats paddle automatically. Find your steering buttons below.' : `Dodge the yellow balls for ${round.engine.config.duration} seconds.`)
        : 'Your round is frozen. Resume when you are ready.';
    $('resumeGame').textContent = ready ? (round.game === 'pirogue' ? 'Start race' : 'Start dodging') : 'Resume game';
    $('pauseGame').textContent = round.phase === 'paused' ? 'Resume' : 'Pause';
    $('pauseGame').disabled = ready;
    $('gameStatus').textContent = ready ? 'Get comfortable. Then press Start.' : (round.phase === 'paused' ? 'Paused - no time or points lost' : '');
}

function resumeMatch() {
    const round = match;
    if (!round || !['pirogue', 'dochi'].includes(round.game) || !['ready', 'paused'].includes(round.phase)) return;
    clearInput(round);
    round.phase = 'playing';
    round.lastTimestamp = null;
    renderPause(round);
    $('gameCanvas').focus({ preventScroll: true });
    function frame(timestamp) {
        round.frame = null;
        if (match !== round || round.phase !== 'playing') return;
        const seconds = round.lastTimestamp === null ? 0 : Math.min(0.05, (timestamp - round.lastTimestamp) / 1000);
        round.lastTimestamp = timestamp;
        if (round.game === 'pirogue') Games.stepRace(round.engine, controlsFor(round), seconds);
        else Games.stepDochi(round.engine, controlsFor(round), seconds);
        renderCanvas(round);
        if (round.engine.over) finishMatch(round);
        else round.frame = requestAnimationFrame(frame);
    }
    round.frame = requestAnimationFrame(frame);
}

function roundedRect(ctx, x, y, width, height, radius, fill) {
    ctx.fillStyle = fill;
    ctx.beginPath();
    ctx.roundRect(x, y, width, height, radius);
    ctx.fill();
}

function drawBoat(ctx, x, y, color, label, slowed, time) {
    ctx.save();
    ctx.translate(x, y);
    ctx.fillStyle = '#032b4670';
    ctx.beginPath();
    ctx.ellipse(4, 12, 22, 43, 0, 0, Math.PI * 2);
    ctx.fill();
    ctx.fillStyle = color;
    ctx.beginPath();
    ctx.moveTo(0, -35); ctx.quadraticCurveTo(24, -8, 15, 34);
    ctx.quadraticCurveTo(0, 44, -15, 34); ctx.quadraticCurveTo(-24, -8, 0, -35); ctx.fill();
    roundedRect(ctx, -9, -3, 18, 25, 6, '#24313b');
    ctx.strokeStyle = '#f2d8a5';
    ctx.lineWidth = 4;
    const paddle = Math.sin(time * 9) * 10;
    ctx.beginPath(); ctx.moveTo(-25, -5 + paddle); ctx.lineTo(25, 13 - paddle); ctx.stroke();
    ctx.fillStyle = '#fff';
    ctx.font = 'bold 14px Segoe UI, sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText(label, 0, -48);
    if (slowed) {
        ctx.fillStyle = '#fff1bc';
        ctx.font = 'bold 12px Segoe UI, sans-serif';
        ctx.fillText('Slowed!', 0, 58);
    }
    ctx.restore();
}

function renderRace(round) {
    const ctx = round.context;
    const state = round.engine;
    ctx.fillStyle = '#057db9'; ctx.fillRect(0, 0, 640, 460);
    ctx.fillStyle = '#087d46'; ctx.fillRect(0, 0, 60, 460); ctx.fillRect(580, 0, 60, 460);
    ctx.fillStyle = '#086f3e'; ctx.fillRect(0, 0, 47, 460); ctx.fillRect(593, 0, 47, 460);
    ctx.strokeStyle = '#ffffff3d'; ctx.lineWidth = 2; ctx.setLineDash([12, 15]);
    ctx.beginPath(); ctx.moveTo(320, 0); ctx.lineTo(320, 460); ctx.stroke(); ctx.setLineDash([]);
    state.players.forEach((player, index) => {
        const left = index === 0 ? 82 : 340;
        const laneWidth = 218;
        const camera = player.distance;
        ctx.save();
        ctx.beginPath(); ctx.rect(left - 12, 0, laneWidth + 24, 460); ctx.clip();
        ctx.strokeStyle = '#77d4ef60'; ctx.lineWidth = 2;
        for (let row = 0; row < 8; row++) {
            const y = ((row * 75 + camera * 1.1) % 520) - 35;
            for (let wave = 0; wave < 3; wave++) {
                const x = left + wave * 76 + 8;
                ctx.beginPath(); ctx.moveTo(x, y); ctx.quadraticCurveTo(x + 10, y + 5, x + 22, y); ctx.stroke();
            }
        }
        const finishY = 355 - (state.config.length - camera) * 1.1;
        if (finishY > -25) {
            for (let square = 0; square < 12; square++) {
                ctx.fillStyle = square % 2 ? '#141b24' : '#fff';
                ctx.fillRect(left - 10 + square * 20, finishY, 20, 14);
                ctx.fillStyle = square % 2 ? '#fff' : '#141b24';
                ctx.fillRect(left - 10 + square * 20, finishY + 14, 20, 14);
            }
        }
        for (const obstacle of state.obstacles) {
            const y = 355 - (obstacle.distance - camera) * 1.1;
            if (y < -35 || y > 495) continue;
            const x = left + obstacle.x * laneWidth;
            roundedRect(ctx, x - 29, y - 12, 58, 24, 7, '#69422b');
            ctx.strokeStyle = '#a67c50'; ctx.lineWidth = 2;
            ctx.beginPath(); ctx.moveTo(x - 18, y - 4); ctx.lineTo(x + 17, y - 4); ctx.moveTo(x - 20, y + 5); ctx.lineTo(x + 12, y + 5); ctx.stroke();
        }
        drawBoat(ctx, left + player.x * laneWidth, 355, index === 0 ? '#ffda19' : '#e2193e',
            index === 0 ? 'P1' : (round.mode === 'local' ? 'P2' : 'AI'), player.slowFor > 0, state.time);
        ctx.restore();
        const number = index === 0 ? 'One' : 'Two';
        $(`meter${number}`).value = player.distance / state.config.length * 100;
        $(`meter${number}Value`).textContent = `${Math.floor(player.distance)} / ${state.config.length}m`;
    });
}

function renderDochi(round) {
    const ctx = round.context;
    const state = round.engine;
    ctx.fillStyle = '#1c232a'; ctx.fillRect(0, 0, 640, 460);
    ctx.fillStyle = '#20282f'; ctx.fillRect(70, 40, 500, 380);
    ctx.strokeStyle = '#3c4751'; ctx.lineWidth = 3; ctx.strokeRect(70, 40, 500, 380);
    ctx.strokeStyle = '#ffffff09'; ctx.lineWidth = 1;
    for (let y = 100; y < 420; y += 60) {
        ctx.beginPath(); ctx.moveTo(70, y); ctx.lineTo(570, y); ctx.stroke();
    }
    for (const x of [32, 608]) {
        ctx.fillStyle = '#ce1233'; ctx.beginPath(); ctx.arc(x, 230, 20, 0, Math.PI * 2); ctx.fill();
        ctx.fillStyle = '#ffadbb'; ctx.beginPath(); ctx.arc(x - 4, 224, 5, 0, Math.PI * 2); ctx.fill();
    }
    for (const ball of state.balls) {
        ctx.shadowColor = '#ffda1970'; ctx.shadowBlur = 8;
        ctx.fillStyle = '#ffda19'; ctx.beginPath(); ctx.arc(ball.x, ball.y, 8, 0, Math.PI * 2); ctx.fill();
    }
    ctx.shadowBlur = 0;
    ctx.fillStyle = '#009b59'; ctx.strokeStyle = '#ffda19'; ctx.lineWidth = 3;
    ctx.beginPath(); ctx.arc(state.player.x, state.player.y, state.player.radius, 0, Math.PI * 2); ctx.fill(); ctx.stroke();
    ctx.fillStyle = '#c3ced9'; ctx.font = '15px Segoe UI, sans-serif'; ctx.textAlign = 'left';
    ctx.fillText(`Score: ${state.score}`, 87, 67);
    ctx.fillStyle = '#ffda19'; ctx.textAlign = 'right';
    ctx.fillText(`${Math.max(0, Math.ceil(state.config.duration - state.time))}s left`, 553, 67);
    $('meterOne').value = state.time / state.config.duration * 100;
    $('meterOneValue').textContent = `${Math.floor(state.time)} / ${state.config.duration}s`;
}

function renderCanvas(round) {
    if (round.game === 'pirogue') renderRace(round);
    else renderDochi(round);
}

function shuffled(items) {
    const result = items.slice();
    for (let i = result.length - 1; i > 0; i--) {
        const other = Math.floor(Math.random() * (i + 1));
        [result[i], result[other]] = [result[other], result[i]];
    }
    return result;
}

async function loadQuiz(round) {
    round.phase = 'loading';
    $('gameStatus').textContent = 'Preparing your questions...';
    $('quizQuestionText').textContent = 'Loading the cultural quiz';
    $('quizOptions').replaceChildren();
    $('quizQuestionPanel').hidden = false;
    $('quizHandoff').hidden = true;
    $('quizContinue').hidden = true;
    $('quizFeedback').textContent = '';
    $('quizRegion').textContent = 'Cameroon';
    $('quizProgress').textContent = '';
    $('quizNameOne').textContent = round.names[0];
    $('quizNameTwo').textContent = round.names[1];
    $('quizScoreOne').textContent = '0 correct';
    $('quizScoreTwo').textContent = '0 correct';
    $('quizScoreTwoWrap').hidden = round.mode !== 'local';
    try {
        if (currentUser.isGuest) {
            round.questions = shuffled(Games.PRACTICE_QUESTIONS).slice(0, 5);
        } else {
            const questions = await api('/quiz', { signal: round.controller.signal });
            if (match !== round) return;
            if (!Array.isArray(questions) || questions.length !== 5 || new Set(questions.map(question => question.question)).size !== 5) {
                throw new Error('The question bank must return five different questions.');
            }
            round.questions = questions.map(question => ({
                id: question.id, region: question.region, question: question.question,
                options: [question.option_a, question.option_b, question.option_c, question.option_d]
            }));
        }
        round.engine = Games.createQuiz(round.questions.length, round.mode);
        round.selections = round.questions.map(() => [null, null]);
        round.phase = 'playing';
        renderQuiz(round);
    } catch (error) {
        if (round.controller.signal.aborted || match !== round) return;
        round.phase = 'error';
        $('gameStatus').textContent = 'The quiz could not be loaded';
        $('quizQuestionText').textContent = 'Use Restart to try again, or return to the menu.';
        reportError('Could not load the quiz.', error);
    }
}

function renderQuiz(round) {
    const state = round.engine;
    const question = round.questions[state.round];
    const feedback = state.phase === 'feedback';
    const handoff = state.phase === 'handoff';
    $('quizRegion').textContent = question.region;
    $('quizProgress').textContent = `Question ${state.round + 1} / ${state.count}`;
    $('quizQuestionText').textContent = question.question;
    $('quizQuestionPanel').hidden = handoff;
    $('quizHandoff').hidden = !handoff;
    $('handoffTitle').textContent = `Pass to ${round.names[state.player]}`;
    $('gameStatus').textContent = handoff ? 'Your answer is locked in'
        : (feedback ? 'The answers are in!' : `${round.names[state.player]}, choose your answer`);
    $('quizOptions').replaceChildren(...question.options.map((option, index) => {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'quiz-option';
        button.dataset.answer = index;
        button.disabled = round.quizBusy || state.phase !== 'question';
        const letter = document.createElement('span');
        letter.className = 'option-letter';
        letter.textContent = String.fromCharCode(65 + index);
        const text = document.createElement('span');
        text.textContent = option;
        button.append(letter, text);
        if (feedback && index === round.correctIndex) button.classList.add('correct');
        if (feedback && index !== round.correctIndex && round.selections[state.round].includes(index)) button.classList.add('incorrect');
        return button;
    }));
    $('quizFeedback').classList.toggle('error', Boolean(round.quizError));
    $('quizFeedback').textContent = round.quizError || (feedback
        ? `Correct answer: ${String.fromCharCode(65 + round.correctIndex)}. ${question.options[round.correctIndex]}. ${round.names[0]}: ${state.answers[0] ? 'correct' : 'not this time'}${round.mode === 'local' ? `; ${round.names[1]}: ${state.answers[1] ? 'correct' : 'not this time'}` : ''}.`
        : '');
    $('quizContinue').hidden = !['handoff', 'feedback'].includes(state.phase);
    $('quizContinue').textContent = handoff ? `I'm ready - ${round.names[state.player]}` : (state.round + 1 === state.count ? 'See results' : 'Next question');
    [0, 1].forEach(player => {
        const visibleScore = state.scores[player] - (!feedback && state.answers[player] === true ? 1 : 0);
        $(player === 0 ? 'quizScoreOne' : 'quizScoreTwo').textContent = `${visibleScore} correct`;
    });
}

async function submitQuizAnswer(index) {
    const round = match;
    if (!round || round.game !== 'quiz' || round.phase !== 'playing' || round.quizBusy || round.engine.phase !== 'question') return;
    round.quizBusy = true;
    round.quizError = '';
    renderQuiz(round);
    try {
        const question = round.questions[round.engine.round];
        let correctIndex = question.correct;
        if (question.id !== undefined) {
            const data = await api('/quiz/verify', {
                method: 'POST', signal: round.controller.signal,
                body: { question_id: question.id, answer: String.fromCharCode(65 + index) }
            });
            if (match !== round) return;
            correctIndex = ['A', 'B', 'C', 'D'].indexOf(data.correctAnswer);
            if (correctIndex < 0) throw new Error('The server returned an invalid quiz answer.');
        }
        round.correctIndex = correctIndex;
        round.selections[round.engine.round][round.engine.player] = index;
        Games.answerQuiz(round.engine, index === correctIndex);
        sound('move');
    } catch (error) {
        if (round.controller.signal.aborted || match !== round) return;
        console.error('Could not verify quiz answer', error);
        round.quizError = `${error.message} Your answer has not been recorded. Please choose again.`;
    } finally {
        round.quizBusy = false;
        if (match === round && round.phase === 'playing') renderQuiz(round);
    }
}

function nextQuizStep() {
    const round = match;
    if (!round || round.game !== 'quiz' || round.quizBusy || round.phase !== 'playing') return;
    const previousRound = round.engine.round;
    Games.continueQuiz(round.engine);
    if (round.engine.over) finishMatch(round);
    else {
        if (previousRound !== round.engine.round) round.correctIndex = null;
        renderQuiz(round);
        $('quizQuestionText').scrollIntoView({ block: 'nearest' });
    }
}

function resultFor(round) {
    const state = round.engine;
    let winner;
    let scores;
    let description;
    let displays;
    if (round.game === 'songo') {
        winner = state.winner;
        scores = state.captured.map(total => total * 20);
        displays = state.captured.map(total => `${total} seeds`);
        description = state.reason;
    } else if (round.game === 'pirogue') {
        winner = state.winner;
        scores = state.players.map(player => Math.floor(player.distance / state.config.length * 1000));
        displays = state.players.map(player => `${Math.floor(player.distance)}m / ${player.hits} hits`);
        description = 'First across the line wins. Both boats faced the same river course.';
    } else if (round.game === 'dochi') {
        winner = state.won ? 0 : 1;
        scores = [state.score];
        displays = [`${state.score} points`];
        description = state.won ? `You survived all ${state.config.duration} seconds!` : `A thrower got you after ${state.time.toFixed(1)} seconds. Keep moving and try again.`;
    } else {
        winner = state.winner;
        scores = state.scores.map(score => score * 50);
        displays = state.scores.map(score => `${score} / ${state.count} correct`);
        description = round.mode === 'local' ? 'Same questions, fair turns. The most correct answers wins.' : 'Every correct answer teaches you something new and earns 10 XP.';
    }
    return { status: winner === null ? 'draw' : (winner === 0 ? 'win' : 'loss'), winner, scores, displays, description };
}

function finishMatch(round) {
    if (match !== round || round.phase === 'finished') return;
    round.phase = 'finished';
    clearInput(round);
    clearTimeout(round.aiTimer);
    if (round.frame !== null) cancelAnimationFrame(round.frame);
    round.frame = null;
    const result = resultFor(round);
    const earned = Games.rewards(round.game, result.scores[0], result.status, round.mode);
    round.result = result;
    $('resultTitle').textContent = round.mode === 'local'
        ? (result.winner === null ? 'A well-matched draw!' : `${round.names[result.winner]} wins!`)
        : (result.status === 'win' ? 'Well played!' : (result.status === 'draw' ? 'An even match!' : 'Keep exploring!'));
    $('resultIcon').textContent = result.status === 'win' ? '🏆' : (result.status === 'draw' ? '🤝' : '🌱');
    $('resultDescription').textContent = result.description;
    const shownPlayers = round.game === 'dochi' || (round.game === 'quiz' && round.mode === 'solo') ? 1 : 2;
    $('resultScores').replaceChildren(...Array.from({ length: shownPlayers }, (_, index) => {
        const panel = document.createElement('div');
        panel.className = `score-panel ${index === 0 ? 'player-one' : 'player-two'}`;
        const name = document.createElement('span');
        name.textContent = round.names[index];
        const score = document.createElement('strong');
        score.textContent = result.displays[index];
        panel.append(name, score);
        return panel;
    }));
    const heartDelta = Math.max(0, Math.min(5, currentUser.lives + earned.lives)) - currentUser.lives;
    $('resultRewards').textContent = `Player 1: +${earned.xp} XP${heartDelta ? ` / ${heartDelta > 0 ? '+' : ''}${heartDelta} heart` : ''}`;
    $('saveStatus').classList.remove('error');
    $('saveStatus').textContent = 'Saving your progress...';
    $('resultMenu').disabled = true;
    $('playAgain').disabled = true;
    round.saving = true;
    $('resultDialog').showModal();
    sound(result.status === 'win' ? 'win' : 'move');
    saveResult(round, earned);
}

async function saveResult(round, earned) {
    const user = currentUser;
    ++progressVersion;
    try {
        if (user.isGuest) {
            user.xp += earned.xp;
            user.lives = Math.max(0, Math.min(5, user.lives + earned.lives));
            bestScores[round.game] = Math.max(bestScores[round.game], round.result.scores[0]);
            saveGuest();
        } else {
            const data = await api('/scores', {
                method: 'POST',
                body: {
                    user_id: user.id, game_name: round.game, difficulty: round.difficulty,
                    score: round.result.scores[0], status: round.result.status, mode: round.mode
                }
            });
            if (!data.success || !Number.isFinite(data.updatedUser?.xp) || !Number.isInteger(data.updatedUser?.lives)) {
                throw new Error('The server did not confirm that this result was saved.');
            }
            user.xp = data.updatedUser.xp;
            user.lives = data.updatedUser.lives;
            bestScores[round.game] = Math.max(bestScores[round.game], round.result.scores[0]);
        }
        if (match === round) $('saveStatus').textContent = user.isGuest ? 'Saved in this browser. Player 2 remains a local guest.' : 'Saved to your player profile.';
    } catch (error) {
        console.error('Score save failed', error);
        if (match === round) {
            $('saveStatus').classList.add('error');
            $('saveStatus').textContent = `This result could not be confirmed as saved. ${error.message}${user.isGuest ? ' Your progress is still available for this session.' : ''}`;
        }
    } finally {
        round.saving = false;
        renderHeader();
        if (match === round) {
            $('resultMenu').disabled = false;
            $('playAgain').disabled = false;
        }
    }
}

function requestLeave(restart = false) {
    if (!match) return;
    if (match.phase === 'finished') {
        if (!match.saving) restart ? replay() : returnToMenu();
        return;
    }
    suspendMatch();
    pendingConfirmation = restart ? 'restart' : 'menu';
    $('confirmTitle').textContent = restart ? 'Restart this round?' : 'Leave this round?';
    $('confirmDescription').textContent = 'This unfinished round will not cost a heart or add a score.';
    $('confirmAction').textContent = restart ? 'Restart round' : 'Leave round';
    $('confirmDialog').showModal();
}

function replay() {
    const config = { game: match.game, mode: match.mode, difficulty: match.difficulty, names: match.names.slice() };
    $('resultDialog').close();
    startMatch(config);
}

async function sound(type) {
    if (!soundEnabled) return;
    try {
        if (!audioContext) audioContext = new window.AudioContext();
        if (audioContext.state === 'suspended') await audioContext.resume();
        if (!soundEnabled) return;
        const oscillator = audioContext.createOscillator();
        const gain = audioContext.createGain();
        oscillator.connect(gain);
        gain.connect(audioContext.destination);
        const time = audioContext.currentTime;
        oscillator.frequency.setValueAtTime(type === 'win' ? 520 : 360, time);
        oscillator.frequency.exponentialRampToValueAtTime(type === 'win' ? 880 : 480, time + .12);
        gain.gain.setValueAtTime(.04, time);
        gain.gain.exponentialRampToValueAtTime(.001, time + .2);
        oscillator.start(time);
        oscillator.stop(time + .22);
        oscillator.onended = () => { oscillator.disconnect(); gain.disconnect(); };
    } catch (error) {
        soundEnabled = false;
        renderSoundToggle();
        reportError('Sound is unavailable in this browser.', error);
    }
}

function renderSoundToggle() {
    $('soundToggle').setAttribute('aria-pressed', String(soundEnabled));
    $('soundToggle').setAttribute('aria-label', soundEnabled ? 'Turn sound off' : 'Turn sound on');
    $('soundToggle').firstElementChild.textContent = soundEnabled ? '🔊' : '🔇';
}

function initialize() {
    $('avatarGrid').replaceChildren(...AVATARS.map((avatar, index) => {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'avatar-option';
        button.textContent = avatar.icon;
        button.title = avatar.region;
        button.setAttribute('aria-label', avatar.region);
        button.setAttribute('aria-pressed', String(index === selectedAvatar));
        button.addEventListener('click', () => {
            selectedAvatar = index;
            avatarChanged = true;
            $('avatarGrid').querySelectorAll('button').forEach((item, i) => item.setAttribute('aria-pressed', String(i === index)));
            $('selectedRegion').textContent = avatar.region;
            renderHeader();
        });
        return button;
    }));
    $('authForm').addEventListener('submit', authenticate);
    $('guestPlay').addEventListener('click', startGuest);
    $('authModeToggle').addEventListener('click', () => {
        authMode = authMode === 'login' ? 'register' : 'login';
        $('authSubmit').textContent = authMode === 'login' ? 'Log In' : 'Create Account';
        $('authModeToggle').textContent = authMode === 'login' ? 'Create Account' : 'I already have an account';
        $('authTitle').textContent = authMode === 'login' ? 'Welcome, explorer' : 'Start your story';
        $('passwordInput').autocomplete = authMode === 'login' ? 'current-password' : 'new-password';
        $('passwordInput').minLength = authMode === 'login' ? 1 : 8;
        $('passwordInput').placeholder = authMode === 'login' ? 'Your password' : 'At least 8 characters';
        $('avatarDetails').open = authMode === 'register';
        $('authError').hidden = true;
    });
    $('logoutButton').addEventListener('click', () => {
        stopMatch();
        ++progressVersion;
        currentUser = null;
        bestScores = emptyBests();
        $('passwordInput').value = '';
        notice('');
        renderHeader();
        showScreen('authScreen');
    });
    document.querySelectorAll('[data-game]').forEach(button => button.addEventListener('click', () => openSetup(button.dataset.game)));
    document.querySelectorAll('input[name="playMode"]').forEach(input => input.addEventListener('change', renderSetup));
    $('setupForm').addEventListener('submit', event => {
        event.preventDefault();
        const mode = setupMode();
        const names = [currentUser.username, mode === 'local' ? ($('secondPlayerName').value.trim() || 'Player 2') : 'AI'];
        const difficulty = setupGame === 'quiz' ? 'medium' : (setupGame === 'songo' && mode === 'local' ? 'easy' : $('difficultySelect').value);
        $('setupDialog').close();
        startMatch({ game: setupGame, mode, difficulty, names });
    });
    document.querySelectorAll('[data-close]').forEach(button => button.addEventListener('click', () => $(button.dataset.close).close()));
    $('songoArena').addEventListener('click', event => {
        const button = event.target.closest('[data-pit]');
        if (button && !button.disabled) playSongo(Number(button.dataset.pit));
    });
    $('quizOptions').addEventListener('click', event => {
        const button = event.target.closest('[data-answer]');
        if (button && !button.disabled) submitQuizAnswer(Number(button.dataset.answer));
    });
    $('quizContinue').addEventListener('click', nextQuizStep);
    $('resumeGame').addEventListener('click', resumeMatch);
    $('pauseGame').addEventListener('click', () => match?.phase === 'paused' ? resumeMatch() : suspendMatch());
    $('exitGame').addEventListener('click', () => requestLeave());
    $('restartGame').addEventListener('click', () => requestLeave(true));
    $('helpButton').addEventListener('click', () => {
        if (!match) return;
        suspendMatch();
        $('helpTitle').textContent = `${GAME_INFO[match.game].title}: rules`;
        $('helpContent').innerHTML = RULES[match.game];
        $('helpDialog').showModal();
    });
    $('confirmAction').addEventListener('click', () => {
        const action = pendingConfirmation;
        $('confirmDialog').close();
        if (action === 'restart') replay();
        else returnToMenu();
    });
    for (const id of ['helpDialog', 'confirmDialog']) {
        $(id).addEventListener('close', () => {
            pendingConfirmation = null;
            if (match?.game === 'songo') scheduleSongoAI(match);
        });
    }
    $('resultMenu').addEventListener('click', returnToMenu);
    $('playAgain').addEventListener('click', replay);
    $('resultDialog').addEventListener('cancel', event => {
        event.preventDefault();
        if (!match?.saving) returnToMenu();
    });
    $('soundToggle').addEventListener('click', () => {
        soundEnabled = !soundEnabled;
        renderSoundToggle();
        sound('move');
    });
    document.querySelectorAll('[data-control]').forEach(button => {
        button.addEventListener('pointerdown', event => {
            if (!match || match.phase !== 'playing' || !['pirogue', 'dochi'].includes(match.game)) return;
            event.preventDefault();
            button.setPointerCapture(event.pointerId);
            match.pointers.set(event.pointerId, button.dataset.control);
            button.classList.add('pressed');
        });
        const release = event => {
            if (!match) return;
            match.pointers.delete(event.pointerId);
            button.classList.toggle('pressed', [...match.pointers.values()].includes(button.dataset.control));
        };
        button.addEventListener('pointerup', release);
        button.addEventListener('pointercancel', release);
        button.addEventListener('lostpointercapture', release);
        button.addEventListener('contextmenu', event => event.preventDefault());
    });
    const movementKeys = new Set(['KeyW', 'KeyA', 'KeyS', 'KeyD', 'ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight']);
    document.addEventListener('keydown', event => {
        if (!match || !['pirogue', 'dochi'].includes(match.game) || document.querySelector('dialog[open]') ||
            ['INPUT', 'SELECT', 'TEXTAREA', 'BUTTON'].includes(event.target.tagName)) return;
        if (event.code === 'Space' && ['playing', 'paused'].includes(match.phase)) {
            event.preventDefault();
            if (!event.repeat) match.phase === 'playing' ? suspendMatch() : resumeMatch();
        } else if (movementKeys.has(event.code) && match.phase === 'playing') {
            event.preventDefault();
            match.keys.add(event.code);
        }
    });
    document.addEventListener('keyup', event => { match?.keys.delete(event.code); });
    window.addEventListener('blur', suspendMatch);
    window.addEventListener('focus', () => { if (match?.game === 'songo') scheduleSongoAI(match); });
    document.addEventListener('visibilitychange', () => {
        if (document.hidden) suspendMatch();
        else if (match?.game === 'songo') scheduleSongoAI(match);
    });
    renderHeader();
}

initialize();
