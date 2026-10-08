const express = require('express');
const sqlite3 = require('sqlite3').verbose();
const bodyParser = require('body-parser');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const path = require('path');

const app = express();
const PORT = 8000;

app.use(cors());
app.use(bodyParser.json());
app.use(express.static(path.join(__dirname)));

const dbFile = path.join(__dirname, 'cameroon_quest.db');
const db = new sqlite3.Database(dbFile, (err) => {
    if (err) {
        console.error('Error opening database', err.message);
    } else {
        console.log('Connected to SQLite database on port 8000.');
        
        // Create tables & seed quiz questions
        db.run(`CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE NOT NULL,
            password_hash TEXT NOT NULL,
            avatar TEXT NOT NULL,
            region TEXT NOT NULL,
            lives INTEGER DEFAULT 5,
            xp INTEGER DEFAULT 0,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )`);

        db.run(`CREATE TABLE IF NOT EXISTS scores (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            game_name TEXT NOT NULL,
            difficulty TEXT NOT NULL,
            score INTEGER NOT NULL,
            status TEXT NOT NULL,
            played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
        )`);

        db.run(`CREATE TABLE IF NOT EXISTS quiz_questions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            region TEXT NOT NULL,
            question TEXT NOT NULL,
            option_a TEXT NOT NULL,
            option_b TEXT NOT NULL,
            option_c TEXT NOT NULL,
            option_d TEXT NOT NULL,
            correct_answer TEXT NOT NULL
        )`, () => {
            // Seed quiz data
            const quizzes = [
                ['Center', 'Which traditional board game originated mainly from the Beti culture in Cameroon?', 'Ludo', 'Songo', 'Checkers', 'Scrabble', 'B'],
                ['Littoral', 'The famous annual Duala canoe race on the Wouri river is known as:', 'Ngondo Festival', 'Nguon Festival', 'Foumban Carnival', 'Mount Cameroon Race', 'A'],
                ['West', 'Dochi is a traditional agility game deeply rooted in which cultural region of Cameroon?', 'Far North', 'East', 'Grassfields (West)', 'South', 'C'],
                ['Adamawa', 'What is the traditional capital and historic emirate located in the Adamawa region?', 'Maroua', 'Ngaoundéré', 'Garoua', 'Bamenda', 'B'],
                ['South-West', 'Mount Cameroon, the highest peak in West/Central Africa, is located in which region?', 'South-West', 'North-West', 'Littoral', 'Center', 'A']
            ];
            quizzes.forEach(q => {
                db.run(`INSERT OR IGNORE INTO quiz_questions (region, question, option_a, option_b, option_c, option_d, correct_answer) VALUES (?, ?, ?, ?, ?, ?, ?)`, q);
            });
        });
    }
});

// --- API ENDPOINTS ---

app.post('/api/register', async (req, res) => {
    const { username, password, avatar, region } = req.body;
    if (!username || !password || !avatar || !region) {
        return res.status(400).json({ error: 'All fields are required.' });
    }
    try {
        const hashedPassword = await bcrypt.hash(password, 10);
        db.run(
            `INSERT INTO users (username, password_hash, avatar, region, lives, xp) VALUES (?, ?, ?, ?, 5, 0)`,
            [username, hashedPassword, avatar, region],
            function (err) {
                if (err) {
                    if (err.message.includes('UNIQUE constraint failed')) {
                        return res.status(400).json({ error: 'Username already exists.' });
                    }
                    return res.status(500).json({ error: err.message });
                }
                res.json({ id: this.lastID, username, avatar, region, lives: 5, xp: 0 });
            }
        );
    } catch (e) {
        res.status(500).json({ error: 'Server error during registration.' });
    }
});

app.post('/api/login', (req, res) => {
    const { username, password } = req.body;
    db.get(`SELECT * FROM users WHERE username = ?`, [username], async (err, user) => {
        if (err) return res.status(500).json({ error: err.message });
        if (!user) return res.status(401).json({ error: 'Invalid username or password.' });

        const validPassword = await bcrypt.compare(password, user.password_hash);
        if (!validPassword) return res.status(401).json({ error: 'Invalid username or password.' });

        res.json({
            id: user.id,
            username: user.username,
            avatar: user.avatar,
            region: user.region,
            lives: user.lives,
            xp: user.xp
        });
    });
});

app.get('/api/user/:id/progress', (req, res) => {
    const userId = req.params.id;
    db.get(`SELECT id, username, avatar, region, lives, xp FROM users WHERE id = ?`, [userId], (err, user) => {
        if (err || !user) return res.status(404).json({ error: 'User not found.' });

        db.all(`SELECT game_name, difficulty, score, status, played_at FROM scores WHERE user_id = ? ORDER BY played_at DESC`, [userId], (err, scores) => {
            if (err) scores = [];
            res.json({ user, scores });
        });
    });
});

app.post('/api/scores', (req, res) => {
    const { user_id, game_name, difficulty, score, status, xp_gained, lives_delta } = req.body;
    db.serialize(() => {
        db.run(
            `INSERT INTO scores (user_id, game_name, difficulty, score, status) VALUES (?, ?, ?, ?, ?)`,
            [user_id, game_name, difficulty, score, status]
        );
        db.run(
            `UPDATE users SET xp = xp + ?, lives = MAX(0, MIN(5, lives + ?)) WHERE id = ?`,
            [xp_gained || 0, lives_delta || 0, user_id],
            function(err) {
                if (err) return res.status(500).json({ error: err.message });
                db.get(`SELECT lives, xp FROM users WHERE id = ?`, [user_id], (err, row) => {
                    res.json({ success: true, updatedUser: row });
                });
            }
        );
    });
});

app.get('/api/quiz', (req, res) => {
    db.all(`SELECT id, region, question, option_a, option_b, option_c, option_d FROM quiz_questions ORDER BY RANDOM() LIMIT 5`, [], (err, rows) => {
        if (err) return res.status(500).json({ error: err.message });
        res.json(rows);
    });
});

app.post('/api/quiz/verify', (req, res) => {
    const { question_id, answer } = req.body;
    db.get(`SELECT correct_answer FROM quiz_questions WHERE id = ?`, [question_id], (err, row) => {
        if (err || !row) return res.status(404).json({ error: 'Question not found.' });
        const isCorrect = row.correct_answer.toUpperCase() === answer.toUpperCase();
        res.json({ correct: isCorrect, correctAnswer: row.correct_answer });
    });
});

app.listen(PORT, () => {
    console.log(`CameroonQuest backend running on http://localhost:${PORT}`);
});