# CameroonQuest... A clue of 237

An interactive, responsive web arcade showcasing Cameroonian culture through native games, rich visuals, player dashboards, and quizzes.

## 🛠️ Tech Stack
- **Frontend:** HTML5 Canvas, CSS3, Vanilla JavaScript (ES6+), running independently or served via live server.
- **Backend API:** Node.js, Express REST API running on **`http://localhost:8000`**.
- **Database:** SQLite3 (`cameroon_quest.db`) with relational schema for users, game scores, and trivia.

---

##  Quick Start Guide

### Step 1: Set up and Start the Backend Server
1. Navigate to your project folder:
   ```bash
   cd cameroon-quest

---

## Project Structure

```text
cameroon-quest/
│
├── index.html        # Single Page Application (SPA) structure
├── style.css         # Dark Emerald responsive styling & board visuals
├── app.js            # Game mechanics, Canvas loops, and backend API sync
├── server.js         # Node.js/Express REST API with SQLite integration
├── schema.sql        # Database table definitions for PostgreSQL/SQLite
├── REQUIREMENT.md    # System requirements & project specifications
└── README.md         # Documentation & setup instructions