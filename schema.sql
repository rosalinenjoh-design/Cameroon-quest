-- CameroonQuest SQLite schema. Existing profiles and scores are preserved.

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    avatar TEXT NOT NULL,
    region TEXT NOT NULL,
    lives INTEGER DEFAULT 5,
    xp INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS scores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    game_name TEXT NOT NULL, -- 'songo', 'pirogue', 'dochi', 'quiz'
    difficulty TEXT NOT NULL, -- 'easy', 'medium', 'hard'
    score INTEGER NOT NULL,
    status TEXT NOT NULL, -- 'win', 'loss', 'draw'
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS quiz_questions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    region TEXT NOT NULL,
    question TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_answer TEXT NOT NULL -- 'A', 'B', 'C', 'D'
);

-- server.js seeds the shared question bank from game-engine.js without duplicating questions.