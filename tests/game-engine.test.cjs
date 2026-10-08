const test = require('node:test');
const assert = require('node:assert/strict');
const Games = require('../game-engine.js');

function seededRandom(seed = 237) {
    return () => {
        seed = (seed * 1664525 + 1013904223) >>> 0;
        return seed / 4294967296;
    };
}

function seedTotal(state) {
    return [...state.board, ...state.captured].reduce((sum, value) => sum + value, 0);
}

test('Songo starts with five pits each and rejects empty or opposing pits', () => {
    const state = Games.createSongo();
    assert.equal(seedTotal(state), 50);
    assert.deepEqual(Games.legalSongoMoves(state), [0, 1, 2, 3, 4]);
    assert.throws(() => Games.moveSongo(state, 5));
    assert.throws(() => Games.moveSongo(state, -1));
    const next = Games.moveSongo(state, 0);
    assert.equal(next.turn, 1);
    assert.equal(next.board[0], 0);
    assert.equal(state.board[0], 5);
    assert.equal(seedTotal(next), 50);
});

test('Songo captures consecutive twos/threes without capturing an entire opposing row', () => {
    const state = Games.createSongo();
    state.board = [0, 0, 0, 0, 2, 1, 2, 5, 0, 0];
    state.captured = [20, 20];
    const next = Games.moveSongo(state, 4);
    assert.equal(next.capturedLast, 5);
    assert.deepEqual(next.captured, [25, 20]);
    assert.equal(seedTotal(next), 50);
    const protectedState = Games.createSongo();
    protectedState.board = [5, 0, 0, 0, 1, 1, 0, 0, 0, 0];
    protectedState.captured = [20, 23];
    const protectedNext = Games.moveSongo(protectedState, 4);
    assert.equal(protectedNext.capturedLast, 0);
    assert.equal(protectedNext.board[5], 2);
    assert.equal(seedTotal(protectedNext), 50);
});

test('Songo requires feeding an empty side and finishes if no feed is possible', () => {
    const state = Games.createSongo();
    state.board = [1, 0, 0, 0, 2, 0, 0, 0, 0, 0];
    state.captured = [23, 24];
    assert.deepEqual(Games.legalSongoMoves(state), [4]);
    assert.throws(() => Games.moveSongo(state, 0));
    const end = Games.createSongo();
    end.board = [0, 0, 0, 0, 1, 1, 0, 0, 0, 0];
    end.captured = [24, 24];
    const b = Games.moveSongo(end, 4);
    assert.equal(b.over, true);
    assert.equal(b.winner, 1);
    assert.equal(seedTotal(b), 50);
    assert.throws(() => Games.moveSongo(b, 0));
});

test('Songo skips the source pit when sowing more than a lap', () => {
    const state = Games.createSongo();
    state.board = [12, 4, 4, 4, 4, 4, 4, 4, 5, 5];
    const next = Games.moveSongo(state, 0);
    assert.equal(next.board[0], 0);
    assert.equal(seedTotal(next), 50);
});

test('Songo AI games terminate, conserve all seeds, and return only legal moves', () => {
    const random = seededRandom();
    for (const difficulty of ['easy', 'medium', 'hard']) {
        let state = Games.createSongo();
        while (!state.over) {
            const move = Games.chooseSongoMove(state, difficulty, random);
            assert.ok(Games.legalSongoMoves(state).includes(move));
            state = Games.moveSongo(state, move);
            assert.equal(seedTotal(state), 50);
        }
        assert.ok(state.moves <= 200);
        assert.equal(state.board.every(seeds => seeds === 0), true);
    }
});

test('Songo settles a repeated position and the documented move limit', () => {
    const state = Games.createSongo();
    const expected = Games.moveSongo(state, 1);
    state.history = [expected.history.at(-1), expected.history.at(-1)];
    assert.equal(Games.moveSongo(state, 1).over, true);
    const capped = Games.createSongo();
    capped.moves = 199;
    assert.equal(Games.moveSongo(capped, 1).over, true);
});

test('Pirogue uses identical obstacle courses and both local inputs independently', () => {
    const state = Games.createRace('easy', 'local', seededRandom());
    Games.stepRace(state, [-1, 1], 0.5);
    assert.ok(state.players[0].x < 0.5);
    assert.ok(state.players[1].x > 0.5);
    assert.equal(state.players[0].distance, state.players[1].distance);
    for (const obstacle of state.obstacles) assert.deepEqual(obstacle.checked, [false, false]);
});

test('Pirogue collisions slow a boat and count each obstacle only once', () => {
    const state = Games.createRace('easy', 'local', () => 0.5);
    state.obstacles = [{ distance: 1, x: 0.5, checked: [false, false] }];
    state.players[1].x = 0.9;
    Games.stepRace(state, [0, 0], 0.5);
    assert.equal(state.players[0].hits, 1);
    assert.equal(state.players[1].hits, 0);
    assert.ok(state.players[0].distance < state.players[1].distance);
    Games.stepRace(state, [0, 0], 0.5);
    assert.equal(state.players[0].hits, 1);
});

test('Pirogue resolves draws fairly and freezes after finishing', () => {
    const state = Games.createRace('easy', 'local', () => 0.5);
    state.obstacles = [];
    while (!state.over) Games.stepRace(state, [0, 0], 0.1);
    assert.equal(state.winner, null);
    const snapshot = JSON.stringify(state);
    Games.stepRace(state, [1, -1], 1);
    assert.equal(JSON.stringify(state), snapshot);
});

test('Pirogue identifies either winner and is independent of rendering frame rate', () => {
    for (const winner of [0, 1]) {
        const state = Games.createRace('hard', 'local', seededRandom());
        state.players[winner].distance = state.config.length - 1;
        Games.stepRace(state, [0, 0], 0.1);
        assert.equal(state.winner, winner);
    }
    const fast = Games.createRace('medium', 'local', seededRandom());
    const slow = Games.createRace('medium', 'local', seededRandom());
    for (let frame = 0; frame < 300; frame++) Games.stepRace(fast, [0, 0], 1 / 60);
    for (let frame = 0; frame < 150; frame++) Games.stepRace(slow, [0, 0], 1 / 30);
    assert.ok(Math.abs(fast.players[0].distance - slow.players[0].distance) < 1e-7);
    assert.equal(fast.players[0].hits, slow.players[0].hits);
});

test('Solo Pirogue AI can make mistakes, so a clean player can win at equal base speed', () => {
    const state = Games.createRace('easy', 'solo', () => 0.3);
    Games.stepRace(state, [1, 0], 0.5);
    while (!state.over) Games.stepRace(state, [0, 0], 0.1);
    assert.equal(state.winner, 0);
    assert.equal(state.players[0].hits, 0);
    assert.ok(state.players[1].hits > 0);
});

test('Dochi normalizes diagonal movement and keeps the player inside the court', () => {
    const straight = Games.createDochi();
    const diagonal = Games.createDochi();
    Games.stepDochi(straight, { x: 1, y: 0 }, 0.2);
    Games.stepDochi(diagonal, { x: 1, y: 1 }, 0.2);
    assert.ok(Math.abs(Math.hypot(diagonal.player.x - 320, diagonal.player.y - 230) - (straight.player.x - 320)) < 1e-7);
    for (let step = 0; step < 10; step++) {
        straight.nextThrow = Infinity;
        Games.stepDochi(straight, { x: 1, y: -1 }, 1);
    }
    assert.equal(straight.player.x, 557);
    assert.equal(straight.player.y, 53);
});

test('Dochi throws toward a stationary player instead of allowing an idle victory', () => {
    const state = Games.createDochi();
    while (!state.over) Games.stepDochi(state, { x: 0, y: 0 }, 0.1, () => 0.5);
    assert.equal(state.won, false);
    assert.ok(state.time < 5);
});

test('Dochi resolves a collision once, including at the survival deadline', () => {
    const state = Games.createDochi();
    state.time = state.config.duration - 0.001;
    state.balls = [{ x: state.player.x, y: state.player.y, vx: 0, vy: 0 }];
    Games.stepDochi(state, { x: 0, y: 0 }, 1 / 60);
    assert.equal(state.over, true);
    assert.equal(state.won, false);
    const snapshot = JSON.stringify(state);
    Games.stepDochi(state, { x: 1, y: 1 }, 1);
    assert.equal(JSON.stringify(state), snapshot);
});

test('Dochi ends with one win and the exact target score when the player survives', () => {
    const state = Games.createDochi('hard');
    state.nextThrow = Infinity;
    while (!state.over) Games.stepDochi(state, { x: 0, y: 0 }, 0.1);
    assert.equal(state.won, true);
    assert.equal(state.time, 45);
    assert.equal(state.score, 450);
});

test('Dochi detects fast balls crossing the player and uses time-based scoring', () => {
    const state = Games.createDochi();
    state.balls = [{ x: 280, y: 230, vx: 10000, vy: 0 }];
    Games.stepDochi(state, { x: 0, y: 0 }, 1 / 60);
    assert.equal(state.over, true);
    const a = Games.createDochi();
    const b = Games.createDochi();
    for (let tick = 0; tick < 60; tick++) Games.stepDochi(a, { x: 0, y: 0 }, 1 / 60);
    for (let tick = 0; tick < 30; tick++) Games.stepDochi(b, { x: 0, y: 0 }, 1 / 30);
    assert.equal(a.score, 10);
    assert.equal(a.score, b.score);
});

test('Local quiz hides feedback until both players answer and alternates who starts', () => {
    const quiz = Games.createQuiz(2, 'local');
    Games.answerQuiz(quiz, true);
    assert.equal(quiz.phase, 'handoff');
    assert.equal(quiz.player, 1);
    assert.throws(() => Games.answerQuiz(quiz, true));
    Games.continueQuiz(quiz);
    Games.answerQuiz(quiz, false);
    assert.equal(quiz.phase, 'feedback');
    Games.continueQuiz(quiz);
    assert.equal(quiz.round, 1);
    assert.equal(quiz.player, 1);
    Games.answerQuiz(quiz, true);
    Games.continueQuiz(quiz);
    Games.answerQuiz(quiz, false);
    Games.continueQuiz(quiz);
    assert.equal(quiz.over, true);
    assert.equal(quiz.winner, null);
    assert.deepEqual(quiz.scores, [1, 1]);
    assert.throws(() => Games.continueQuiz(quiz));
});

test('Solo quiz finishes at the actual question count and rewards only correct answers', () => {
    const quiz = Games.createQuiz(5);
    for (const correct of [true, false, true, false, true]) {
        Games.answerQuiz(quiz, correct);
        assert.throws(() => Games.answerQuiz(quiz, true));
        Games.continueQuiz(quiz);
    }
    assert.equal(quiz.winner, 0);
    assert.deepEqual(Games.rewards('quiz', 150, 'win'), { xp: 30, lives: 1 });
    assert.deepEqual(Games.rewards('quiz', 0, 'loss'), { xp: 0, lives: 0 });
    assert.equal(Games.rewards('pirogue', 200, 'loss', 'local').lives, 0);
    assert.equal(Games.rewards('pirogue', 200, 'loss', 'solo').lives, -1);
});

test('Invalid game settings are reported instead of silently defaulted', () => {
    assert.throws(() => Games.createRace('unknown'));
    assert.throws(() => Games.createRace('easy', 'online'));
    assert.throws(() => Games.createDochi('unknown'));
    assert.throws(() => Games.createQuiz(0));
    assert.throws(() => Games.stepRace(Games.createRace(), [0, 0], -1));
    assert.throws(() => Games.stepDochi(Games.createDochi(), { x: NaN, y: 0 }, 0.1));
});
