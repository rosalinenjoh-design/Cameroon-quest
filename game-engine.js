(function (root, factory) {
    const games = factory();
    if (typeof module === 'object' && module.exports) module.exports = games;
    else root.QuestGames = games;
}(typeof globalThis === 'object' ? globalThis : this, function () {
    'use strict';

    const DIFFICULTIES = ['easy', 'medium', 'hard'];
    const SONGO_PITS = 5;
    const SONGO_SEEDS = 50;
    const PHYSICS_STEP = 1 / 120;
    const RACE_CONFIG = {
        easy: { length: 1800, speed: 82, spacing: 230, aiLookahead: 90, aiMissChance: 0.35 },
        medium: { length: 2400, speed: 98, spacing: 210, aiLookahead: 140, aiMissChance: 0.18 },
        hard: { length: 3000, speed: 112, spacing: 180, aiLookahead: 200, aiMissChance: 0.08 }
    };
    const DOCHI_CONFIG = {
        easy: { duration: 30, ballSpeed: 170, interval: 1.05 },
        medium: { duration: 40, ballSpeed: 210, interval: 0.8 },
        hard: { duration: 45, ballSpeed: 250, interval: 0.62 }
    };
    const PRACTICE_QUESTIONS = [
        { region: 'Centre', question: 'Which seed-board game is associated with Beti culture?', options: ['Ludo', 'Songo', 'Chess', 'Scrabble'], correct: 1 },
        { region: 'Littoral', question: 'Which Sawa cultural festival is celebrated around the Wouri River?', options: ['Ngondo', 'Nguon', 'Nyem-Nyem', 'Medumba'], correct: 0 },
        { region: 'West', question: 'Which city is the capital of the West Region?', options: ['Garoua', 'Bafoussam', 'Buea', 'Ebolowa'], correct: 1 },
        { region: 'Adamawa', question: 'Which city is the capital of the Adamawa Region?', options: ['Maroua', 'Ngaoundere', 'Douala', 'Bamenda'], correct: 1 },
        { region: 'South-West', question: 'In which region is Mount Cameroon located?', options: ['South-West', 'North-West', 'Littoral', 'Centre'], correct: 0 },
        { region: 'North', question: 'Which city is the capital of the North Region?', options: ['Bertoua', 'Buea', 'Garoua', 'Yaounde'], correct: 2 },
        { region: 'Far North', question: 'Which city is the capital of the Far North Region?', options: ['Maroua', 'Bafoussam', 'Ebolowa', 'Douala'], correct: 0 },
        { region: 'East', question: 'Which city is the capital of the East Region?', options: ['Bamenda', 'Garoua', 'Bertoua', 'Buea'], correct: 2 },
        { region: 'South', question: 'Which city is the capital of the South Region?', options: ['Ngaoundere', 'Ebolowa', 'Maroua', 'Douala'], correct: 1 },
        { region: 'North-West', question: 'Which city is the capital of the North-West Region?', options: ['Yaounde', 'Bertoua', 'Bafoussam', 'Bamenda'], correct: 3 }
    ];

    function checkDifficulty(difficulty) {
        if (!DIFFICULTIES.includes(difficulty)) throw new RangeError('Unknown difficulty.');
    }

    function checkMode(mode) {
        if (!['solo', 'local'].includes(mode)) throw new RangeError('Unknown player mode.');
    }

    function clamp(value, min, max) {
        return Math.max(min, Math.min(max, value));
    }

    function winnerOf(scores) {
        return scores[0] === scores[1] ? null : (scores[0] > scores[1] ? 0 : 1);
    }

    function sumSide(board, player) {
        return board.slice(player * SONGO_PITS, (player + 1) * SONGO_PITS).reduce((sum, value) => sum + value, 0);
    }

    function sow(board, pit) {
        const next = board.slice();
        let seeds = next[pit];
        let last = pit;
        next[pit] = 0;
        while (seeds > 0) {
            last = (last + 1) % next.length;
            if (last !== pit) {
                next[last]++;
                seeds--;
            }
        }
        return { board: next, last };
    }

    function songoKey(state) {
        return `${state.turn}:${state.board.join(',')}:${state.captured.join(',')}`;
    }

    function createSongo() {
        const state = {
            board: Array(10).fill(5), captured: [0, 0], turn: 0, moves: 0,
            over: false, winner: null, history: [], lastPit: null, capturedLast: 0, reason: ''
        };
        state.history.push(songoKey(state));
        return state;
    }

    function legalSongoMoves(state) {
        if (state.over) return [];
        const moves = [];
        const opponent = 1 - state.turn;
        for (let pit = state.turn * SONGO_PITS; pit < (state.turn + 1) * SONGO_PITS; pit++) {
            if (state.board[pit] === 0) continue;
            if (sumSide(state.board, opponent) === 0 && sumSide(sow(state.board, pit).board, opponent) === 0) continue;
            moves.push(pit);
        }
        return moves;
    }

    function finishSongo(state, reason) {
        for (let player = 0; player < 2; player++) state.captured[player] += sumSide(state.board, player);
        state.board.fill(0);
        state.over = true;
        state.reason = reason;
        state.winner = winnerOf(state.captured);
        return state;
    }

    function moveSongo(state, pit) {
        if (!Number.isInteger(pit) || !legalSongoMoves(state).includes(pit)) {
            throw new RangeError('Choose a non-empty pit on the active side; feed an empty opposing side if possible.');
        }
        const result = sow(state.board, pit);
        const next = {
            ...state, board: result.board, captured: state.captured.slice(),
            history: state.history.slice(), moves: state.moves + 1, lastPit: pit, capturedLast: 0
        };
        const opponent = 1 - state.turn;
        const capturedPits = [];
        let cursor = result.last;
        while (Math.floor(cursor / SONGO_PITS) === opponent && [2, 3].includes(next.board[cursor])) {
            capturedPits.push(cursor);
            cursor--;
        }
        const captured = capturedPits.reduce((total, index) => total + next.board[index], 0);
        // A capture may not empty the opponent's entire row.
        if (captured > 0 && captured < sumSide(next.board, opponent)) {
            for (const index of capturedPits) next.board[index] = 0;
            next.captured[state.turn] += captured;
            next.capturedLast = captured;
        }
        next.turn = opponent;
        next.history.push(songoKey(next));
        if (next.captured.some(total => total > SONGO_SEEDS / 2)) return finishSongo(next, 'A player captured a majority.');
        if (legalSongoMoves(next).length === 0) return finishSongo(next, 'No legal move remains. Each side collects its remaining seeds.');
        if (next.history.filter(key => key === songoKey(next)).length >= 3) {
            return finishSongo(next, 'The position repeated three times. Remaining seeds are shared by side.');
        }
        if (next.moves >= 200) return finishSongo(next, 'The 200-turn arcade limit was reached. Remaining seeds are shared by side.');
        return next;
    }

    function chooseSongoMove(state, difficulty, random = Math.random) {
        checkDifficulty(difficulty);
        const moves = legalSongoMoves(state);
        if (!moves.length) throw new Error('No legal Songo move is available.');
        if (difficulty === 'easy') return moves[Math.min(moves.length - 1, Math.floor(random() * moves.length))];
        const player = state.turn;
        const depth = difficulty === 'hard' ? 4 : 2;
        function evaluate(position, remaining, alpha, beta) {
            const difference = position.captured[player] - position.captured[1 - player];
            if (position.over) return position.winner === null ? 0 : (position.winner === player ? 10000 + difference : -10000 + difference);
            if (remaining === 0) return difference * 12 + sumSide(position.board, player) - sumSide(position.board, 1 - player);
            const maximizing = position.turn === player;
            let best = maximizing ? -Infinity : Infinity;
            for (const move of legalSongoMoves(position)) {
                const score = evaluate(moveSongo(position, move), remaining - 1, alpha, beta);
                best = maximizing ? Math.max(best, score) : Math.min(best, score);
                if (maximizing) alpha = Math.max(alpha, best);
                else beta = Math.min(beta, best);
                if (alpha >= beta) break;
            }
            return best;
        }
        let bestMove = moves[0];
        let bestScore = -Infinity;
        for (const move of moves) {
            const score = evaluate(moveSongo(state, move), depth - 1, -Infinity, Infinity);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return bestMove;
    }

    function slices(seconds, update) {
        if (!Number.isFinite(seconds) || seconds < 0 || seconds > 1) throw new RangeError('A simulation step must be between zero and one second.');
        let remaining = seconds;
        while (remaining > 1e-9) {
            const step = Math.min(PHYSICS_STEP, remaining);
            if (update(step) === false) break;
            remaining -= step;
        }
    }

    function createRace(difficulty = 'easy', mode = 'solo', random = Math.random) {
        checkDifficulty(difficulty);
        checkMode(mode);
        const config = RACE_CONFIG[difficulty];
        const obstacles = [];
        for (let distance = 300; distance < config.length - 100; distance += config.spacing) {
            obstacles.push({ distance, x: 0.2 + random() * 0.6, aiDistracted: random() < config.aiMissChance, checked: [false, false] });
        }
        return {
            difficulty, mode, config, obstacles, time: 0, over: false, winner: null,
            players: [0, 1].map(() => ({ x: 0.5, distance: 0, hits: 0, slowFor: 0, finishAt: null }))
        };
    }

    function raceAiInput(state) {
        const player = state.players[1];
        const next = state.obstacles.find(obstacle => obstacle.distance > player.distance &&
            obstacle.distance - player.distance < state.config.aiLookahead);
        if (next?.aiDistracted) return 0;
        const target = next ? (next.x < 0.5 ? 0.85 : 0.15) : 0.5;
        return Math.abs(target - player.x) < 0.025 ? 0 : Math.sign(target - player.x);
    }

    function stepRace(state, inputs, seconds) {
        if (state.over) return state;
        if (!Array.isArray(inputs) || inputs.length !== 2 || inputs.some(value => !Number.isFinite(value))) {
            throw new TypeError('Both racers need a numeric steering input.');
        }
        slices(seconds, dt => {
            const controls = [clamp(inputs[0], -1, 1), state.mode === 'solo' ? raceAiInput(state) : clamp(inputs[1], -1, 1)];
            state.players.forEach((player, index) => {
                const previousDistance = player.distance;
                player.x = clamp(player.x + controls[index] * dt * 0.85, 0.1, 0.9);
                const speed = state.config.speed * (player.slowFor > 0 ? 0.35 : 1);
                player.slowFor = Math.max(0, player.slowFor - dt);
                player.distance += speed * dt;
                for (const obstacle of state.obstacles) {
                    if (!obstacle.checked[index] && player.distance >= obstacle.distance) {
                        obstacle.checked[index] = true;
                        if (Math.abs(player.x - obstacle.x) < 0.2) {
                            player.hits++;
                            player.slowFor = 1.35;
                        }
                    }
                }
                if (player.distance >= state.config.length) {
                    player.finishAt = state.time + (state.config.length - previousDistance) / speed;
                    player.distance = state.config.length;
                }
            });
            state.time += dt;
            const finishes = state.players.map(player => player.finishAt);
            if (finishes.some(time => time !== null)) {
                state.over = true;
                if (finishes.every(time => time !== null)) {
                    state.winner = Math.abs(finishes[0] - finishes[1]) < 1e-7 ? null : (finishes[0] < finishes[1] ? 0 : 1);
                } else state.winner = finishes[0] === null ? 1 : 0;
                return false;
            }
            return true;
        });
        return state;
    }

    function createDochi(difficulty = 'easy') {
        checkDifficulty(difficulty);
        return {
            difficulty, config: DOCHI_CONFIG[difficulty], time: 0, nextThrow: 1.2, nextSide: 0,
            player: { x: 320, y: 230, radius: 13 }, balls: [], score: 0, over: false, won: false
        };
    }

    function segmentDistance(x1, y1, x2, y2) {
        const dx = x2 - x1;
        const dy = y2 - y1;
        const length = dx * dx + dy * dy;
        const t = length === 0 ? 0 : clamp(-(x1 * dx + y1 * dy) / length, 0, 1);
        return Math.hypot(x1 + t * dx, y1 + t * dy);
    }

    function stepDochi(state, input, seconds, random = Math.random) {
        if (state.over) return state;
        if (!input || !Number.isFinite(input.x) || !Number.isFinite(input.y)) throw new TypeError('Dochi requires numeric movement.');
        slices(seconds, step => {
            const dt = Math.min(step, Math.max(0, state.config.duration - state.time));
            const oldPlayer = { ...state.player };
            const dx = clamp(input.x, -1, 1);
            const dy = clamp(input.y, -1, 1);
            const magnitude = Math.max(1, Math.hypot(dx, dy));
            state.player.x = clamp(state.player.x + dx / magnitude * 230 * dt, 83, 557);
            state.player.y = clamp(state.player.y + dy / magnitude * 230 * dt, 53, 407);
            state.time += dt;
            state.score = Math.floor((state.time + 1e-8) * 10);
            if (state.time >= state.nextThrow) {
                const x = state.nextSide === 0 ? 42 : 598;
                const y = 230;
                const angle = Math.atan2(state.player.y - y + (random() - 0.5) * 40, state.player.x - x);
                state.balls.push({ x, y, vx: Math.cos(angle) * state.config.ballSpeed, vy: Math.sin(angle) * state.config.ballSpeed });
                state.nextSide = 1 - state.nextSide;
                state.nextThrow += state.config.interval;
            }
            for (const ball of state.balls) {
                const oldX = ball.x;
                const oldY = ball.y;
                ball.x += ball.vx * dt;
                ball.y += ball.vy * dt;
                if (segmentDistance(oldX - oldPlayer.x, oldY - oldPlayer.y, ball.x - state.player.x, ball.y - state.player.y) <= state.player.radius + 8) {
                    state.over = true;
                    state.won = false;
                    return false;
                }
            }
            state.balls = state.balls.filter(ball => ball.x > -20 && ball.x < 660 && ball.y > -20 && ball.y < 480);
            if (state.time >= state.config.duration - 1e-8) {
                state.time = state.config.duration;
                state.score = state.config.duration * 10;
                state.over = true;
                state.won = true;
                return false;
            }
            return true;
        });
        return state;
    }

    function createQuiz(count, mode = 'solo') {
        checkMode(mode);
        if (!Number.isInteger(count) || count < 1) throw new RangeError('A quiz needs at least one question.');
        return { count, mode, round: 0, player: 0, scores: [0, 0], answers: [null, null], phase: 'question', over: false, winner: null };
    }

    function answerQuiz(state, correct) {
        if (state.over || state.phase !== 'question' || typeof correct !== 'boolean') throw new Error('This quiz is not accepting an answer.');
        state.answers[state.player] = correct;
        if (correct) state.scores[state.player]++;
        if (state.mode === 'local' && state.answers[1 - state.player] === null) {
            state.player = 1 - state.player;
            state.phase = 'handoff';
        } else state.phase = 'feedback';
        return state;
    }

    function continueQuiz(state) {
        if (state.over || !['handoff', 'feedback'].includes(state.phase)) throw new Error('Finish the current answer before continuing.');
        if (state.phase === 'handoff') {
            state.phase = 'question';
            return state;
        }
        state.round++;
        if (state.round === state.count) {
            state.over = true;
            state.phase = 'finished';
            state.winner = state.mode === 'local' ? winnerOf(state.scores) : (state.scores[0] >= Math.ceil(state.count * 0.6) ? 0 : 1);
        } else {
            state.answers = [null, null];
            state.player = state.mode === 'local' ? state.round % 2 : 0;
            state.phase = 'question';
        }
        return state;
    }

    function rewards(game, score, status, mode = 'solo') {
        if (!['songo', 'pirogue', 'dochi', 'quiz'].includes(game)) throw new RangeError('Unknown game.');
        if (!Number.isInteger(score) || score < 0 || !['win', 'loss', 'draw'].includes(status)) throw new RangeError('Invalid game result.');
        checkMode(mode);
        if (game === 'quiz') return { xp: Math.floor(score / 50) * 10, lives: score >= 150 ? 1 : 0 };
        return {
            xp: status === 'win' ? 50 : (status === 'draw' ? 25 : 10),
            lives: game === 'dochi' && status === 'win' ? 1 : (mode === 'solo' && status === 'loss' ? -1 : 0)
        };
    }

    return {
        createSongo, legalSongoMoves, moveSongo, chooseSongoMove,
        createRace, stepRace, createDochi, stepDochi,
        createQuiz, answerQuiz, continueQuiz, rewards, PRACTICE_QUESTIONS
    };
}));
