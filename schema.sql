-- CameroonQuest Database Schema (SQLite / PostgreSQL compatible)

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

-- Seed Initial Quiz Data
INSERT OR IGNORE INTO quiz_questions (id, region, question, option_a, option_b, option_c, option_d, correct_answer) VALUES
(1, 'Center', 'Which traditional board game originated mainly from the Beti culture in Cameroon?', 'Ludo', 'Songo', 'Checkers', 'Scrabble', 'B'),
(2, 'Littoral', 'The famous annual Duala canoe race on the Wouri river is known as:', 'Ngondo Festival', 'Nguon Festival', 'Foumban Carnival', 'Mount Cameroon Race', 'A'),
(3, 'West', 'Dochi is a traditional agility game deeply rooted in which cultural region of Cameroon?', 'Far North', 'East', 'Grassfields (West)', 'South', 'C'),
(4, 'Adamawa', 'What is the traditional capital and historic emirate located in the Adamawa region?', 'Maroua', 'Ngaoundéré', 'Garoua', 'Bamenda', 'B'),
(5, 'South-West', 'Mount Cameroon, the highest peak in West/Central Africa, is located in which region?', 'South-West', 'North-West', 'Littoral', 'Center', 'A');