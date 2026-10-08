const express = require('express');
const sqlite3 = require('sqlite3').verbose();
const bcrypt = require('bcryptjs');
const path = require('path');
const fs = require('fs');
const Games = require('./game-engine');

async function createApp({ databaseFile = path.join(__dirname, 'cameroon_quest.db') } = {}) {
    const db = await new Promise((resolve, reject) => {
        const connection = new sqlite3.Database(databaseFile, error => error ? reject(error) : resolve(connection));
    });
    const run = (sql, params = []) => new Promise((resolve, reject) => {
        db.run(sql, params, function (error) {
            if (error) reject(error);
            else resolve({ lastID: this.lastID, changes: this.changes });
        });
    });
    const get = (sql, params = []) => new Promise((resolve, reject) => {
        db.get(sql, params, (error, row) => error ? reject(error) : resolve(row));
    });
    const all = (sql, params = []) => new Promise((resolve, reject) => {
        db.all(sql, params, (error, rows) => error ? reject(error) : resolve(rows));
    });
    const exec = sql => new Promise((resolve, reject) => db.exec(sql, error => error ? reject(error) : resolve()));
    const close = () => new Promise((resolve, reject) => db.close(error => error ? reject(error) : resolve()));
    let writeQueue = Promise.resolve();

    async function transaction(work) {
        const previous = writeQueue;
        let release;
        writeQueue = new Promise(resolve => { release = resolve; });
        await previous;
        let begun = false;
        try {
            await exec('BEGIN IMMEDIATE');
            begun = true;
            const result = await work();
            await exec('COMMIT');
            begun = false;
            return result;
        } catch (error) {
            if (begun) await exec('ROLLBACK');
            throw error;
        } finally {
            release();
        }
    }

    try {
        await exec('PRAGMA foreign_keys = ON; PRAGMA busy_timeout = 5000;');
        await exec(fs.readFileSync(path.join(__dirname, 'schema.sql'), 'utf8'));
        await transaction(async () => {
            for (const question of Games.PRACTICE_QUESTIONS) {
                await run(`INSERT INTO quiz_questions (region, question, option_a, option_b, option_c, option_d, correct_answer)
                    SELECT ?, ?, ?, ?, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM quiz_questions WHERE question = ?)`,
                [question.region, question.question, ...question.options, String.fromCharCode(65 + question.correct), question.question]);
            }
        });
    } catch (error) {
        await close();
        throw error;
    }

    const app = express();
    app.disable('x-powered-by');
    app.use(express.json({ limit: '16kb' }));
    const asyncRoute = handler => (req, res, next) => Promise.resolve(handler(req, res)).catch(next);
    const validText = (value, min, max) => typeof value === 'string' && value.length >= min && value.length <= max;
    const publicUser = user => ({
        id: user.id, username: user.username, avatar: user.avatar, region: user.region, lives: user.lives, xp: user.xp
    });

    app.get(['/', '/index.html'], (_req, res) => res.sendFile(path.join(__dirname, 'index.html')));
    for (const file of ['style.css', 'app.js', 'game-engine.js']) {
        app.get(`/${file}`, (_req, res) => res.sendFile(path.join(__dirname, file)));
    }
    app.get(['/CameroonQuest', '/CameroonQuest/', '/CameroonQuest/index.html'], (_req, res) => res.redirect(302, '/'));

    app.post('/api/register', asyncRoute(async (req, res) => {
        const { username, password, avatar, region } = req.body || {};
        if (!validText(username, 1, 40) || !username.trim() || !validText(password, 8, 128) ||
            !validText(avatar, 1, 20) || !validText(region, 1, 80)) {
            return res.status(400).json({ error: 'Use a player name (1-40 characters), password (8-128 characters), and a regional avatar.' });
        }
        const normalizedName = username.trim();
        const hash = await bcrypt.hash(password, 10);
        try {
            const result = await transaction(() => run(
                'INSERT INTO users (username, password_hash, avatar, region, lives, xp) VALUES (?, ?, ?, ?, 5, 0)',
                [normalizedName, hash, avatar, region]
            ));
            res.json({ id: result.lastID, username: normalizedName, avatar, region, lives: 5, xp: 0 });
        } catch (error) {
            if (error.code === 'SQLITE_CONSTRAINT' && error.message.includes('users.username')) {
                return res.status(400).json({ error: 'That player name is already in use.' });
            }
            throw error;
        }
    }));

    app.post('/api/login', asyncRoute(async (req, res) => {
        const { username, password } = req.body || {};
        if (!validText(username, 1, 40) || !validText(password, 1, 128)) {
            return res.status(400).json({ error: 'Enter your player name and password.' });
        }
        const user = await get('SELECT * FROM users WHERE username = ?', [username.trim()]);
        if (!user || !await bcrypt.compare(password, user.password_hash)) {
            return res.status(401).json({ error: 'Invalid player name or password.' });
        }
        res.json(publicUser(user));
    }));

    app.get('/api/user/:id/progress', asyncRoute(async (req, res) => {
        const id = Number(req.params.id);
        if (!Number.isSafeInteger(id) || id <= 0) return res.status(400).json({ error: 'Invalid player ID.' });
        const user = await get('SELECT id, username, avatar, region, lives, xp FROM users WHERE id = ?', [id]);
        if (!user) return res.status(404).json({ error: 'Player not found.' });
        const scores = await all('SELECT game_name, difficulty, score, status, played_at FROM scores WHERE user_id = ? ORDER BY played_at DESC', [id]);
        res.json({ user, scores });
    }));

    app.post('/api/scores', asyncRoute(async (req, res) => {
        const { user_id, game_name, difficulty, score, status, mode = 'solo' } = req.body || {};
        const maximum = { songo: 1000, pirogue: 1000, dochi: 450, quiz: 250 };
        if (!Number.isSafeInteger(user_id) || user_id <= 0 || !Object.hasOwn(maximum, game_name) ||
            !['easy', 'medium', 'hard'].includes(difficulty) || !Number.isInteger(score) || score < 0 || score > maximum[game_name] ||
            !['win', 'loss', 'draw'].includes(status) || !['solo', 'local'].includes(mode) ||
            (game_name === 'dochi' && (mode !== 'solo' || status === 'draw')) ||
            (game_name === 'quiz' && score % 50 !== 0)) {
            return res.status(400).json({ error: 'Invalid game result. Check the game, mode, difficulty, score, and outcome.' });
        }
        const reward = Games.rewards(game_name, score, status, mode);
        const updatedUser = await transaction(async () => {
            const user = await get('SELECT id FROM users WHERE id = ?', [user_id]);
            if (!user) return null;
            await run('INSERT INTO scores (user_id, game_name, difficulty, score, status) VALUES (?, ?, ?, ?, ?)',
                [user_id, game_name, difficulty, score, status]);
            await run('UPDATE users SET xp = xp + ?, lives = MAX(0, MIN(5, lives + ?)) WHERE id = ?',
                [reward.xp, reward.lives, user_id]);
            return get('SELECT lives, xp FROM users WHERE id = ?', [user_id]);
        });
        if (!updatedUser) return res.status(404).json({ error: 'Player not found; no score was saved.' });
        res.json({ success: true, updatedUser });
    }));

    app.get('/api/quiz', asyncRoute(async (_req, res) => {
        const questions = await all(`SELECT id, region, question, option_a, option_b, option_c, option_d
            FROM quiz_questions WHERE id IN (SELECT MIN(id) FROM quiz_questions GROUP BY question)
            ORDER BY RANDOM() LIMIT 5`);
        res.json(questions);
    }));

    app.post('/api/quiz/verify', asyncRoute(async (req, res) => {
        const { question_id, answer } = req.body || {};
        if (!Number.isSafeInteger(question_id) || !validText(answer, 1, 1) || !/^[ABCD]$/i.test(answer)) {
            return res.status(400).json({ error: 'Choose one answer: A, B, C, or D.' });
        }
        const row = await get('SELECT correct_answer FROM quiz_questions WHERE id = ?', [question_id]);
        if (!row) return res.status(404).json({ error: 'Question not found.' });
        res.json({ correct: row.correct_answer === answer.toUpperCase(), correctAnswer: row.correct_answer });
    }));

    app.use('/api', (_req, res) => res.status(404).json({ error: 'API route not found.' }));
    app.use((error, _req, res, _next) => {
        console.error('CameroonQuest request failed:', error);
        const invalidJson = error.type === 'entity.parse.failed' || error.type === 'entity.too.large';
        res.status(invalidJson ? 400 : 500).json({ error: invalidJson ? 'The request body is invalid or too large.' : 'The server could not complete this request. Please try again.' });
    });
    return { app, close, db };
}

if (require.main === module) {
    createApp({ databaseFile: process.env.DATABASE_FILE || path.join(__dirname, 'cameroon_quest.db') }).then(({ app, close }) => {
        const port = Number(process.env.PORT || 8000);
        const server = app.listen(port, process.env.HOST || '0.0.0.0', () => {
            console.log(`CameroonQuest is ready at http://localhost:${server.address().port}`);
        });
        server.on('error', async error => {
            console.error('Unable to start CameroonQuest:', error);
            await close();
            process.exitCode = 1;
        });
    }).catch(error => {
        console.error('Unable to initialize CameroonQuest:', error);
        process.exitCode = 1;
    });
}

module.exports = { createApp };
