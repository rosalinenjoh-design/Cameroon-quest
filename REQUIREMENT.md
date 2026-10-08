# Cameroon Quest - Implemented scope

## Technology

- Responsive HTML/CSS and vanilla JavaScript.
- Accessible DOM buttons for the Songo board and quiz; canvas for real-time games.
- Node.js 22+, Express, bcrypt password hashing, and SQLite.
- Shared pure game rules with Node's built-in regression test runner.

## UI

- Dark screenshot-inspired shell with Cameroon green/red/yellow accents.
- Regional avatar, XP, level, and hearts; no nonfunctional currency or email field.
- Existing account login/registration plus browser-persisted guest play.
- Profile and ranking pages with explicit Games/Profile/Rankings navigation outside active games.
- Preview/save/cancel avatar editing for ten regional avatars; account changes persist in SQLite, guest changes in browser storage.
- Shared level rule: one level per 250 XP, starting at Level 1; exact progress/remaining XP and a saved-result level-up notice.
- Registered-player XP rankings with competition ties, stable pagination, own-player highlighting, and loading/empty/error states. Guests are unranked.
- Game setup, mode selection, local Player 2 name, rules, pause/resume, exit/restart confirmation, and results with explicit save status.
- Mobile layouts, multi-touch buttons, keyboard focus, labeled inputs, reduced motion, and no required external font/CDN services.

## Games

1. **Songo:** five pits per side, five seeds per pit; documented arcade capture/feeding/end rules. Solo AI or local two-player turns. Three AI difficulties.
2. **Pirogue:** equal-speed automatic paddling, matched obstacle courses, steering, one slowdown per collision, and first-finisher/draw detection. Solo AI or simultaneous two-player racing on one device.
3. **Dochi:** strictly single-player; two aimed throwers, bounded/normalized movement, swept collision detection, difficulty-based survival timers, and one terminal result.
4. **Quiz:** five questions; solo or local pass-and-play. Both players answer before feedback, starting players alternate, and rewards depend on correct answers.

## Progress and lifecycle

- Only Player 1 saves progress. Player 2 is a local guest.
- Completed rounds save once per client round; unfinished rounds do not save or lose hearts.
- Local multiplayer losses do not cost hearts. Hearts are a progress indicator, not a play restriction.
- Screen exit, restart, and player switching dispose of game timers and inputs.
- Real-time games pause on focus/visibility loss and resume explicitly.
- Quiz loading/answer requests are cancelled when their round is abandoned.
- Startup seeds missing quiz questions without adding duplicates; quiz requests also suppress legacy duplicate questions.
- Score insertion and XP/hearts updates are atomic and errors are surfaced.
- Seven-day HttpOnly, SameSite=Strict sessions restore accounts across reloads; logout invalidates the session. Profile/score changes and private progress access are restricted to the signed-in account.
- Levels are calculated from stored XP, never separately editable. Avatar edits do not alter XP, hearts, level, or scores.

## Non-goals and release requirements

- Multiplayer is not online and does not connect separate devices.
- The five-pit Songo adaptation does not claim traditional/tournament accuracy.
- The authenticated account API still needs production rate limiting, HTTPS/cookie deployment configuration, and server-validated/idempotent game results. Client-reported outcomes are not cheat-resistant.
- See [README.md](README.md) for setup, controls, rules, tests, and follow-up UI recommendations.
