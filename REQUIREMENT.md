# CameroonQuest - System Requirements Specification

# System Requirements & Specifications

## Tech Stack
- **Frontend:** HTML5 Canvas, CSS3 Grid/Flexbox, Vanilla JavaScript (ES6+).
- **Backend:** Node.js, Express REST API.
- **Database:** SQLite3 with relational tables for users, game scores, and quiz questions.
- **Security:** Password hashing via `bcryptjs`, CORS middleware protection.

## Core Features
1. **Authentication System:** Secure registration and login supporting 10 regional animal avatars representing Cameroon's regions.
2. **Dashboard & Score Tracking:** Visual progress score bars for each mini-game linked directly to the database.
3. **Three Cultural Mini-Games:**
   - **Songo:** Mancala seed board game with AI and 2-player support.
   - **Pirogue Racing:** Extended long-track Wouri river race with vivid blue water and landscapes.
   - **Dochi:** Grassfields 3-person dodgeball with omnidirectional movement.
4. **Trivia Quiz:** Cultural questionnaire to replenish player lives (+1 Heart) and earn XP.
5. **Mobile & Responsive UI:** Designed with touch-ready viewports and green-yellow-red Cameroonian color tokens.