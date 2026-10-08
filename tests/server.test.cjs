const { test, before, after } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { once } = require('node:events');
const { createApp } = require('../server.js');

let application;
let server;
let baseUrl;
let player;

async function request(route, body) {
    const response = await fetch(baseUrl + route, body === undefined ? {} : {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body)
    });
    return { status: response.status, data: await response.json() };
}

const query = (db, sql) => new Promise((resolve, reject) => db.get(sql, (error, row) => error ? reject(error) : resolve(row)));
const exec = (db, sql) => new Promise((resolve, reject) => db.exec(sql, error => error ? reject(error) : resolve()));

before(async () => {
    application = await createApp({ databaseFile: ':memory:' });
    server = application.app.listen(0, '127.0.0.1');
    await once(server, 'listening');
    baseUrl = `http://127.0.0.1:${server.address().port}`;
    const registered = await request('/api/register', { username: 'Quest Test', password: 'temporary-test-password', avatar: 'lion', region: 'Centre' });
    assert.equal(registered.status, 200);
    player = registered.data;
});

after(async () => {
    await new Promise((resolve, reject) => server.close(error => error ? reject(error) : resolve()));
    await application.close();
});

test('Existing login/progress contracts are preserved without exposing password hashes', async () => {
    const login = await request('/api/login', { username: 'Quest Test', password: 'temporary-test-password' });
    assert.equal(login.status, 200);
    assert.equal(login.data.id, player.id);
    assert.equal(Object.hasOwn(login.data, 'password_hash'), false);
    assert.equal((await request('/api/login', { username: 'Quest Test', password: 'incorrect' })).status, 401);
    const progress = await request(`/api/user/${player.id}/progress`);
    assert.equal(progress.status, 200);
    assert.ok(Array.isArray(progress.data.scores));
});

test('Only frontend assets are public and the alternate entry redirects to the working app', async () => {
    for (const route of ['/', '/style.css', '/app.js', '/game-engine.js']) {
        assert.equal((await fetch(baseUrl + route)).status, 200);
    }
    for (const route of ['/server.js', '/schema.sql', '/cameroon_quest.db', '/cameroonquest.db', '/package.json', '/tests/server.test.cjs']) {
        assert.equal((await fetch(baseUrl + route)).status, 404);
    }
    const legacy = await fetch(baseUrl + '/CameroonQuest/', { redirect: 'manual' });
    assert.equal(legacy.status, 302);
    assert.equal(legacy.headers.get('location'), '/');
});

test('Quiz gives five distinct questions, withholds answers, and handles bad answer input', async () => {
    const questions = await request('/api/quiz');
    assert.equal(questions.data.length, 5);
    assert.equal(new Set(questions.data.map(question => question.question)).size, 5);
    assert.equal(Object.hasOwn(questions.data[0], 'correct_answer'), false);
    assert.equal((await request('/api/quiz/verify', { question_id: questions.data[0].id, answer: 123 })).status, 400);
    const verified = await request('/api/quiz/verify', { question_id: questions.data[0].id, answer: 'a' });
    assert.equal(verified.status, 200);
    assert.ok(['A', 'B', 'C', 'D'].includes(verified.data.correctAnswer));
});

test('Scores use shared reward rules and local losses do not deduct hearts', async () => {
    const before = (await request(`/api/user/${player.id}/progress`)).data.user;
    const result = await request('/api/scores', {
        user_id: player.id, game_name: 'pirogue', difficulty: 'easy', mode: 'local',
        score: 800, status: 'loss', xp_gained: 999999, lives_delta: -5
    });
    assert.equal(result.status, 200);
    assert.equal(result.data.updatedUser.xp, before.xp + 10);
    assert.equal(result.data.updatedUser.lives, before.lives);
    const solo = await request('/api/scores', {
        user_id: player.id, game_name: 'dochi', difficulty: 'easy', mode: 'solo', score: 20, status: 'loss'
    });
    assert.equal(solo.data.updatedUser.lives, before.lives - 1);
    const quiz = await request('/api/scores', {
        user_id: player.id, game_name: 'quiz', difficulty: 'medium', mode: 'solo', score: 150, status: 'win'
    });
    assert.equal(quiz.data.updatedUser.xp, before.xp + 50);
    assert.equal(quiz.data.updatedUser.lives, before.lives);
});

test('Bad results and two-player Dochi are rejected without changing progress', async () => {
    const valid = { user_id: player.id, game_name: 'songo', difficulty: 'easy', mode: 'local', score: 400, status: 'win' };
    const before = (await request(`/api/user/${player.id}/progress`)).data;
    for (const invalid of [
        { ...valid, score: -1 }, { ...valid, score: 1001 }, { ...valid, difficulty: 'unknown' },
        { ...valid, game_name: 'unknown' }, { ...valid, game_name: 'dochi' },
        { ...valid, game_name: 'quiz', score: 51 }, { ...valid, status: 'unknown' }
    ]) assert.equal((await request('/api/scores', invalid)).status, 400);
    assert.equal((await request('/api/scores', { ...valid, user_id: 999999 })).status, 404);
    assert.deepEqual((await request(`/api/user/${player.id}/progress`)).data, before);
});

test('Failed score inserts roll back rewards and the next transaction still works', async () => {
    const before = (await request(`/api/user/${player.id}/progress`)).data;
    await exec(application.db, "CREATE TRIGGER test_score_failure BEFORE INSERT ON scores BEGIN SELECT RAISE(FAIL, 'test insert failure'); END;");
    const result = await request('/api/scores', { user_id: player.id, game_name: 'songo', difficulty: 'easy', score: 600, status: 'win' });
    assert.equal(result.status, 500);
    assert.deepEqual((await request(`/api/user/${player.id}/progress`)).data, before);
    await exec(application.db, 'DROP TRIGGER test_score_failure;');
    assert.equal((await request('/api/scores', { user_id: player.id, game_name: 'songo', difficulty: 'easy', score: 600, status: 'win' })).status, 200);
});

test('Concurrent completed rounds update XP without nested transactions or lost writes', async () => {
    const before = (await request(`/api/user/${player.id}/progress`)).data;
    const results = await Promise.all(Array.from({ length: 4 }, () => request('/api/scores', {
        user_id: player.id, game_name: 'songo', difficulty: 'easy', mode: 'local', score: 500, status: 'draw'
    })));
    assert.ok(results.every(result => result.status === 200));
    const after = (await request(`/api/user/${player.id}/progress`)).data;
    assert.equal(after.user.xp, before.user.xp + 100);
    assert.equal(after.scores.length, before.scores.length + 4);
});

test('Initialization is repeatable, preserves data, and enables foreign keys', async () => {
    const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'cameroonquest-test-'));
    const databaseFile = path.join(directory, 'test.db');
    try {
        const first = await createApp({ databaseFile });
        assert.equal((await query(first.db, 'PRAGMA foreign_keys')).foreign_keys, 1);
        const initial = await query(first.db, 'SELECT COUNT(*) AS count FROM quiz_questions');
        await first.close();
        const second = await createApp({ databaseFile });
        assert.deepEqual(await query(second.db, 'SELECT COUNT(*) AS count FROM quiz_questions'), initial);
        assert.equal(initial.count, 10);
        await second.close();
    } finally {
        fs.unlinkSync(databaseFile);
        fs.rmdirSync(directory);
    }
});
