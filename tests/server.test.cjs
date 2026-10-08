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
let sessionCookie = '';

async function request(route, body, options = {}) {
    const cookie = options.cookie ?? sessionCookie;
    const response = await fetch(baseUrl + route, {
        method: options.method || (body === undefined ? 'GET' : 'POST'),
        headers: { ...(body === undefined ? {} : { 'Content-Type': 'application/json' }), ...(cookie ? { Cookie: cookie } : {}) },
        body: body === undefined ? undefined : JSON.stringify(body)
    });
    const setCookie = response.headers.get('set-cookie');
    if (setCookie && options.cookie === undefined) sessionCookie = setCookie.split(';')[0];
    return { status: response.status, data: await response.json(), cookie: setCookie?.split(';')[0], setCookie };
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
    for (const route of ['/', '/style.css', '/app.js', '/game-engine.js', '/player-profile.js']) {
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
    assert.equal((await request('/api/scores', { ...valid, user_id: 999999 })).status, 403);
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

test('A session is required for profiles, avatar changes, and score writes', async () => {
    assert.equal((await request('/api/profile', undefined, { cookie: '' })).status, 401);
    assert.equal((await request(`/api/user/${player.id}/progress`, undefined, { cookie: '' })).status, 401);
    assert.equal((await request('/api/scores', {
        user_id: player.id, game_name: 'songo', difficulty: 'easy', score: 500, status: 'win'
    }, { cookie: '' })).status, 401);
    assert.equal((await request('/api/profile/avatar', { avatar_id: 'dolphin' }, { cookie: '', method: 'PATCH' })).status, 401);
    assert.deepEqual((await request('/api/session', undefined, { cookie: '' })).data, { user: null });
});

test('Avatar edits persist, update the matching region, and cannot change XP or another player', async () => {
    const before = (await request('/api/profile')).data;
    const peer = await request('/api/register', { username: 'Avatar Peer', password: 'temporary-peer-password', avatar: 'lion', region: 'Centre' }, { cookie: '' });
    const updated = await request('/api/profile/avatar', { avatar_id: 'dolphin' }, { method: 'PATCH' });
    assert.equal(updated.status, 200);
    assert.equal(updated.data.user.avatar, '\u{1F42C}');
    assert.equal(updated.data.user.region, 'Littoral (Douala)');
    assert.equal(updated.data.user.xp, before.user.xp);
    assert.equal(updated.data.user.level, before.user.level);
    assert.equal((await request('/api/profile')).data.user.avatar, '\u{1F42C}');
    assert.equal((await request(`/api/user/${peer.data.id}/progress`)).status, 403);
    assert.equal((await request('/api/profile/avatar', { avatar_id: 'lion', user_id: peer.data.id }, { method: 'PATCH' })).status, 400);
    assert.equal((await request('/api/profile/avatar', { avatar_id: 'lion', xp: 99999 }, { method: 'PATCH' })).status, 400);
    assert.equal((await request('/api/profile/avatar', { avatar_id: 'not-an-avatar' }, { method: 'PATCH' })).status, 400);
    const peerProfile = await request('/api/profile', undefined, { cookie: peer.cookie });
    assert.equal(peerProfile.data.user.avatar, 'lion');
    const relogin = await request('/api/login', { username: 'Quest Test', password: 'temporary-test-password' });
    assert.equal(relogin.data.avatar, '\u{1F42C}');
    assert.equal((await request('/api/session')).data.user.id, player.id);
});

test('Real score awards increase the same level in score responses, profiles, and rankings', async () => {
    const account = await request('/api/register', { username: 'Level Check', password: 'temporary-level-password', avatar: 'lion', region: 'Centre' }, { cookie: '' });
    assert.equal(account.data.level, 1);
    for (let game = 1; game <= 5; game++) {
        const score = await request('/api/scores', {
            game_name: 'songo', difficulty: 'easy', score: 600, status: 'win'
        }, { cookie: account.cookie });
        assert.equal(score.status, 200);
        assert.equal(score.data.updatedUser.xp, game * 50);
        assert.equal(score.data.updatedUser.level, game === 5 ? 2 : 1);
    }
    const profile = (await request('/api/profile', undefined, { cookie: account.cookie })).data;
    assert.equal(profile.user.level, 2);
    assert.deepEqual(profile.progress, { level: 2, earned: 0, required: 250, remaining: 250, nextLevelXp: 500, percent: 0 });
    const rankings = (await request('/api/rankings', undefined, { cookie: account.cookie })).data;
    assert.equal(rankings.currentPlayer.level, 2);
    assert.equal(rankings.currentPlayer.xp, 250);
    assert.equal(rankings.currentPlayer.rank, profile.rank);
});

test('Rankings sort by XP with shared ranks for ties, stable pagination, and public fields only', async () => {
    await exec(application.db, `INSERT INTO users (username, password_hash, avatar, region, xp) VALUES
        ('Rank Alpha', 'test-only', 'lion', 'Centre', 2000),
        ('Rank Beta', 'test-only', 'dolphin', 'Littoral', 2000),
        ('Rank Gamma', 'test-only', 'horse', 'North-West', 1500);`);
    const first = (await request('/api/rankings?limit=1&offset=0', undefined, { cookie: '' })).data;
    const second = (await request('/api/rankings?limit=1&offset=1', undefined, { cookie: '' })).data;
    const third = (await request('/api/rankings?limit=1&offset=2', undefined, { cookie: '' })).data;
    assert.equal(first.players[0].username, 'Rank Alpha');
    assert.equal(second.players[0].username, 'Rank Beta');
    assert.equal(third.players[0].username, 'Rank Gamma');
    assert.deepEqual([first.players[0].rank, second.players[0].rank, third.players[0].rank], [1, 1, 3]);
    assert.equal(first.players[0].level, 9);
    assert.equal(third.players[0].level, 7);
    assert.equal(first.currentPlayer, null);
    assert.equal(first.total, second.total);
    assert.deepEqual(Object.keys(first.players[0]).sort(), ['avatar', 'id', 'level', 'rank', 'region', 'username', 'xp']);
    const all = (await request('/api/rankings?limit=50')).data.players;
    assert.ok(all.every((row, index) => index === 0 || row.xp <= all[index - 1].xp));
    const outsidePage = (await request('/api/rankings?limit=1')).data.currentPlayer;
    assert.equal(outsidePage.id, player.id);
    assert.ok(outsidePage.rank > 3);
    for (const query of ['limit=0', 'limit=51', 'offset=-1', 'offset=0.5', 'limit=abc', 'limit=1&limit=2', 'offset=1000001']) {
        assert.equal((await request(`/api/rankings?${query}`)).status, 400);
    }
});

test('Sessions use HttpOnly cookies, expire, rotate on login, and are invalidated by logout', async () => {
    const account = await request('/api/register', { username: 'Session Check', password: 'temporary-session-password', avatar: 'lion', region: 'Centre' }, { cookie: '' });
    assert.match(account.setCookie, /HttpOnly/i);
    assert.match(account.setCookie, /SameSite=Strict/i);
    const rawToken = account.cookie.split('=')[1];
    const stored = await query(application.db, `SELECT token_hash FROM sessions WHERE user_id = ${account.data.id}`);
    assert.notEqual(stored.token_hash, rawToken);
    const loggedIn = await request('/api/login', { username: 'Session Check', password: 'temporary-session-password' }, { cookie: account.cookie });
    assert.notEqual(loggedIn.cookie, account.cookie);
    assert.equal((await request('/api/profile', undefined, { cookie: account.cookie })).status, 401);
    assert.equal((await request('/api/logout', {}, { cookie: loggedIn.cookie })).status, 200);
    assert.equal((await request('/api/profile', undefined, { cookie: loggedIn.cookie })).status, 401);
    const expired = await request('/api/login', { username: 'Session Check', password: 'temporary-session-password' }, { cookie: '' });
    await exec(application.db, `UPDATE sessions SET expires_at = 0 WHERE user_id = ${account.data.id};`);
    assert.equal((await request('/api/profile', undefined, { cookie: expired.cookie })).status, 401);
});

test('Initialization is repeatable, preserves data, and enables foreign keys', async () => {
    const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'cameroonquest-test-'));
    const databaseFile = path.join(directory, 'test.db');
    try {
        const first = await createApp({ databaseFile });
        assert.equal((await query(first.db, 'PRAGMA foreign_keys')).foreign_keys, 1);
        const initial = await query(first.db, 'SELECT COUNT(*) AS count FROM quiz_questions');
        await exec(first.db, `INSERT INTO users (username, password_hash, avatar, region, xp)
            VALUES ('Persistent Player', 'test-only', '\u{1F42C}', 'Littoral (Douala)', 499);`);
        await first.close();
        const second = await createApp({ databaseFile });
        assert.deepEqual(await query(second.db, 'SELECT COUNT(*) AS count FROM quiz_questions'), initial);
        assert.equal(initial.count, 10);
        assert.deepEqual(await query(second.db, "SELECT avatar, region, xp FROM users WHERE username = 'Persistent Player'"), {
            avatar: '\u{1F42C}', region: 'Littoral (Douala)', xp: 499
        });
        await second.close();
    } finally {
        fs.unlinkSync(databaseFile);
        fs.rmdirSync(directory);
    }
});

test('A fresh ranking board is genuinely empty, not populated with placeholder players', async () => {
    const empty = await createApp({ databaseFile: ':memory:' });
    const listener = empty.app.listen(0, '127.0.0.1');
    await once(listener, 'listening');
    try {
        const response = await fetch(`http://127.0.0.1:${listener.address().port}/api/rankings`);
        assert.equal(response.status, 200);
        assert.deepEqual(await response.json(), { players: [], total: 0, limit: 10, offset: 0, currentPlayer: null });
    } finally {
        await new Promise((resolve, reject) => listener.close(error => error ? reject(error) : resolve()));
        await empty.close();
    }
});
