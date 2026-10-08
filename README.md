# Cameroon Quest - A clue of 237

A screenshot-inspired cultural arcade with a charcoal interface, Cameroonian flag colors, regional avatars, and solo or shared-device games. Works with a mouse/keyboard on PC and touch controls on phones and tablets.

## Run locally

Use Node.js 22 or newer:

```powershell
npm ci
npm start
```

Open **http://localhost:8000**. Serve the interface through this Node server, not a separate Live Server: the frontend uses same-origin `/api` requests.

For development, run `npm run dev`. The old `/CameroonQuest/` URL redirects to the same working application; the nested prototype files are not a separate supported app.

To open it on your phone, connect the phone and PC to the same trusted Wi-Fi and visit `http://<your-PC-LAN-address>:8000`. Allow the Node server through the Windows firewall for private networks if prompted. Two-player mode is **local to one shared device**, not a networked match.

Optional environment variables:

- `PORT`: HTTP port (default `8000`).
- `HOST`: listen address (default `0.0.0.0`; use `127.0.0.1` for PC-only access).
- `DATABASE_FILE`: alternate SQLite file, or `:memory:` for disposable testing.

## Accounts and guest play

- Choose **Quick Guest Play** to play without an account. The name field is optional for guests; the default is Guest Warrior.
- Guest XP, hearts, avatar, and best scores are saved in that browser's local storage. Storage failures are shown explicitly; private browsing or clearing site data can remove progress.
- Existing username/password accounts still work. New passwords require at least eight characters. No email address is collected.
- Account login creates a seven-day, HttpOnly, SameSite=Strict session cookie. Reloading restores an active session; **Switch player** invalidates it on the server. Cookies use Secure when served directly over HTTPS.
- Local Player 2 chooses a display name and does not need an account. Only Player 1's profile receives XP, hearts, and saved scores.
- Hearts track solo outcomes; zero hearts do **not** lock players out. Correct quiz answers and Dochi victories can restore hearts, up to five. Local multiplayer losses do not deduct hearts.
- Every 250 XP advances the displayed level. There is no cosmetic coin balance or nonfunctional shop.

## Profile, levels, and rankings

Use the **Games / Profile / Rankings** navigation above the main content. The header avatar also opens your profile. Navigation is hidden during a round so changing pages cannot leave a game running in the background.

### Profile and avatars

- The profile shows total XP, current level, hearts, global rank for accounts, personal bests, and progress toward the next level.
- Preview any of the ten regional avatars, then select **Save avatar**. The matching region changes with the avatar; XP, level, and scores never change.
- Account avatar changes are stored in SQLite and appear in rankings and future sessions. Guest changes stay in the browser. Cancelling discards the preview.
- Failed loads/saves show an explicit message and retry guidance rather than claiming success. Only the signed-in account can edit its profile or submit its scores.

### Level thresholds

The same shared calculation is used by the header, profile, game-result notice, and ranking API:

| Total XP | Level | XP remaining until the next level |
| --- | --- | --- |
| 0 | 1 | 250 |
| 249 | 1 | 1 |
| 250 | 2 | 250 |
| 499 | 2 | 1 |
| 500 | 3 | 250 |

Formula: `level = 1 + floor(totalXP / 250)`. Levels are derived from XP, not separately stored or user-editable. A completed, saved round that crosses a threshold displays a level-up notice.

### Ranking rules

- Only registered accounts appear on the global board, ordered by total XP descending. Guest XP is not uploaded or merged into an account.
- Equal XP gives the same competition rank (for example `1, 1, 3`). Ties are displayed in stable account-ID order.
- Rankings show ten players per page, plus your own rank even when you are outside the displayed page. Your row is highlighted.
- Opening the page or selecting **Refresh** fetches current data. Empty rankings and connection failures have different states.
- The public board contains only display name, avatar, region, XP, level, and rank/ID, not passwords, session tokens, or private score history.

New endpoints: `GET /api/session`, `POST /api/logout`, `GET /api/profile`, `PATCH /api/profile/avatar` with `{ "avatar_id": "dolphin" }`, and `GET /api/rankings?limit=10&offset=0`. Profile, avatar, progress, and score mutations require the authenticated session; player IDs cannot be used to access another account.

## Games and controls

| Game | Solo | Two players on one device | Controls |
| --- | --- | --- | --- |
| Songo Board | Against AI | Alternating turns | Tap/click a highlighted pit, or Tab then Enter |
| Pirogue Regatta | Against AI | Simultaneous racing | P1: A/D; P2: left/right arrows; separate multi-touch buttons |
| Dochi Dodgeball | Survival only | Not available | WASD, arrow keys, or touch direction pad |
| Cultural Quiz | Five questions | Pass-and-play | Tap/click an answer; both answer before feedback |

Real-time games wait for **Start**, offer Pause/Resume, and pause on loss of focus or when the page is hidden. Space pauses when the game canvas has keyboard focus. Leaving or restarting a round cancels its animation, AI timers, inputs, and pending quiz requests; unfinished rounds are not scored.

### Songo arcade rules

This is the requested **five-pit-per-side arcade adaptation**, not a traditional seven-pit or tournament implementation.

1. Ten pits start with five seeds each: 50 seeds total.
2. Player 1 moves along the bottom row left to right, followed by the top row right to left. Sowing skips the source pit on subsequent laps.
3. Landing in an opposing pit with 2 or 3 seeds captures them, continuing backward through consecutive opposing pits containing 2 or 3.
4. A capture cannot empty the entire opposing row. When the opposing row is empty, feeding it is compulsory if possible.
5. A captured majority, absence of legal moves, threefold repetition, or the 200-turn arcade limit ends the round. Each side collects its remaining seeds; equal totals draw.
6. Solo Easy chooses random legal moves; Medium searches two plies and Hard four. Difficulty does not change rules in local play.

### Pirogue Regatta

Boats paddle automatically at equal base speed. Each player sees an independent view of the same obstacle course. Hold a direction to avoid logs; a collision applies a 1.35-second slowdown once per obstacle. The first boat to finish wins, with precise finish-time comparison for draws. Difficulty changes speed, course length, obstacle spacing, and AI anticipation. The AI can miss a log, so a clean human run can win rather than only tie a perfect opponent.

### Dochi

Two throwers alternate aimed shots at the single player. Diagonal movement is normalized; the player cannot leave the court. Balls crossing the player between frames still collide. A collision ends the round once and takes precedence over a simultaneous survival deadline.

Survive 30/40/45 seconds on Easy/Medium/Hard. Score is elapsed survival time multiplied by ten, independent of screen refresh rate.

### Cultural quiz

Guest play uses a built-in regional question bank. Account play loads five distinct questions from SQLite. Each correct answer earns 50 score points and 10 XP; three or more correct restores one heart. Two-player rounds hide answers and current-round score changes until both players answer, and alternate the starting player.

## Tests

```powershell
npm test
```

Tests use Node's built-in test runner, deterministic game simulations, in-memory SQLite, and disposable databases. They cover rule invariants, game endings, AI moves, fair local racing, frame-rate independence, quiz turn-taking, score transactions, invalid inputs, seeding, and public asset routing. Existing project databases are not used by tests.

Profile tests cover exact 250-XP boundaries, shared avatar data, avatar ownership/persistence, session rotation/expiry/logout, ranking order, ties across page boundaries, current-player placement, and rejection of XP edits.

## Project structure

- [index.html](index.html): accessible screens, dialogs, board, and game controls.
- [style.css](style.css): responsive screenshot-inspired styling.
- [app.js](app.js): screen lifecycle, rendering, input, guest storage, and API integration.
- [game-engine.js](game-engine.js): independently testable game rules and shared rewards/question bank.
- [player-profile.js](player-profile.js): shared regional avatar catalog and 250-XP level progression.
- [server.js](server.js): Express API and public asset routes.
- [schema.sql](schema.sql): SQLite table definitions; existing profiles/scores are preserved.
- [tests](tests): game and API regression tests.

## Release limitations and UI recommendations

This is still a **local/demo application**, not a cheat-resistant competitive service. Passwords are hashed, authenticated sessions enforce account ownership, rewards are calculated consistently, and private project files are not public static assets. However, game outcomes are still client-reported. Before public deployment, add server-validated/idempotent game results, authentication rate limits, HTTPS with correctly configured secure cookies behind any proxy, and operational monitoring. Cookie tokens are random and only their hashes are stored in SQLite.

The interface incorporates large touch targets, visible keyboard focus, readable contrast, labeled controls, responsive header/cards, honest save/error messages, in-page quizzes/results, and reduced-motion support. Useful next product improvements are English/French localization, community review of cultural content and game variants, more region-specific questions, and a short first-play tutorial. Canvas action games are not a complete screen-reader-accessible gameplay experience.
