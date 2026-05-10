# ChallengeMe — Přehled funkcí a API endpointů

> Poslední aktualizace: 2026-04-25  
> Backend: Spring Boot 3.2 · Java 21 · PostgreSQL / H2  
> Frontend: Vue 3 · vue-i18n · Leaflet · Axios  

---

## Obsah

1. [REST API endpointy](#1-rest-api-endpointy)
2. [WebSocket (STOMP)](#2-websocket-stomp)
3. [JPA entity (databázový model)](#3-jpa-entity-databázový-model)
4. [Vue stránky a komponenty](#4-vue-stránky-a-komponenty)
5. [Infrastruktura a konfigurace](#5-infrastruktura-a-konfigurace)

---

## 1. REST API endpointy

### Auth — `/api/auth`

| Metoda | Cesta | Popis | Auth |
|--------|-------|-------|------|
| GET | `/api/auth/captcha` | Vygeneruje matematický CAPTCHA challenge | veřejný |
| POST | `/api/auth/register` | Registrace nového uživatele (ověření CAPTCHA) | veřejný |
| POST | `/api/auth/login` | Přihlášení, vytvoří HTTP session | veřejný |
| GET | `/api/auth/me` | Vrátí username + role přihlášeného uživatele | veřejný |
| POST | `/api/auth/logout` | Zneplatní session | přihlášen |
| POST | `/api/auth/forgot-password` | Krok 1 resetu hesla — odešle e-mail s tokenem | veřejný |
| POST | `/api/auth/reset-password` | Krok 2 resetu hesla — nastaví nové heslo podle tokenu | veřejný |
| DELETE | `/api/auth/account` | Smaže vlastní účet | přihlášen |

---

### Hráči — `/api/players`

| Metoda | Cesta | Popis | Auth |
|--------|-------|-------|------|
| GET | `/api/players/{username}` | Profil hráče: statistiky, skóre reputace, achievementy, historie her | veřejný |
| GET | `/api/players/{username}/rating-history` | Vývoj ELO ratingu v čase | veřejný |
| PUT | `/api/players/me/profile` | Aktualizace vlastního bio a oblíbené hry | přihlášen |

---

### Katalog her — `/api/games`

| Metoda | Cesta | Popis | Auth |
|--------|-------|-------|------|
| GET | `/api/games` | Seznam všech dostupných her (cached) | veřejný |
| GET | `/api/games/{key}` | Detail hry — název, pravidla, tipy, jak vyhrát (CS/EN) | veřejný |

---

### Výzvy (GameEvent) — `/api/events`

| Metoda | Cesta | Popis | Auth |
|--------|-------|-------|------|
| GET | `/api/events/public` | Aktivní výzvy pro mapu bez přihlášení | veřejný |
| GET | `/api/events` | Aktivní výzvy s filtrováním viditelnosti (PUBLIC / FRIENDS / PRIVATE / TOURNAMENT) | přihlášen |
| GET | `/api/events/mine` | Vlastní výzvy přihlášeného uživatele | přihlášen |
| GET | `/api/events/{id}` | Detail jedné výzvy | přihlášen |
| POST | `/api/events` | Vytvoření nové výzvy (GPS, hra, čas, viditelnost, volitelně pozvaný hráč) | přihlášen |
| POST | `/api/events/{id}/accept` | Přijetí výzvy — přejde do PENDING_APPROVAL | přihlášen |
| POST | `/api/events/{id}/approve` | Schválení uchazeče (creator) | přihlášen |
| POST | `/api/events/{id}/reject` | Odmítnutí uchazeče (creator) | přihlášen |
| DELETE | `/api/events/{id}` | Zrušení výzvy | přihlášen |
| POST | `/api/events/{id}/result` | Nahlášení výsledku (spustí přepočet ELO, cache evict) | přihlášen |
| POST | `/api/events/{id}/team/join` | Připojení se k týmu výzvy (strana CREATOR / CHALLENGER) | přihlášen |
| DELETE | `/api/events/{id}/team/leave` | Opuštění týmu před koncem výzvy | přihlášen |

---

### Žebříček — `/api/leaderboard`

| Metoda | Cesta | Popis | Auth |
|--------|-------|-------|------|
| GET | `/api/leaderboard` | Top 50 hráčů podle ELO (cached 2 min) | veřejný |
| GET | `/api/leaderboard/game/{gameType}` | Žebříček filtrovaný podle typu hry | veřejný |

---

### 1v1 Turnaje — `/api/tournaments`

| Metoda | Cesta | Popis | Auth |
|--------|-------|-------|------|
| GET | `/api/tournaments` | Seznam aktivních turnajů (OPEN + IN_PROGRESS) | přihlášen |
| GET | `/api/tournaments/{id}` | Detail turnaje — bracket, účastníci, zápasy | přihlášen |
| POST | `/api/tournaments` | Vytvoření turnaje (formát ELIMINATION nebo ROUND_ROBIN, kapacita 2–32) | přihlášen |
| POST | `/api/tournaments/{id}/join` | Přihlášení se do turnaje | přihlášen |
| POST | `/api/tournaments/{id}/start` | Spuštění turnaje — creator (ROUND_ROBIN min. 3 hráčů, ELIMINATION full) | přihlášen |
| POST | `/api/tournaments/{id}/matches/{matchId}/result` | Záznam výsledku zápasu | přihlášen |

---

### Týmové turnaje — `/api/team-tournaments`

| Metoda | Cesta | Popis | Auth |
|--------|-------|-------|------|
| GET | `/api/team-tournaments` | Seznam aktivních týmových turnajů | přihlášen |
| GET | `/api/team-tournaments/{id}` | Detail týmového turnaje + bracket | přihlášen |
| POST | `/api/team-tournaments` | Vytvoření týmového turnaje | přihlášen |
| POST | `/api/team-tournaments/{id}/teams` | Vytvoření týmu (kapitán) | přihlášen |
| POST | `/api/team-tournaments/{id}/teams/{teamId}/join` | Připojení se k existujícímu týmu | přihlášen |
| DELETE | `/api/team-tournaments/{id}/teams/{teamId}/leave` | Opuštění týmu | přihlášen |
| POST | `/api/team-tournaments/{id}/start` | Spuštění turnaje (creator) | přihlášen |
| POST | `/api/team-tournaments/{id}/matches/{matchId}/result` | Záznam výsledku týmového zápasu | přihlášen |

---

### Přátelé — `/api/friends`

| Metoda | Cesta | Popis | Auth |
|--------|-------|-------|------|
| GET | `/api/friends/search?q=` | Hledání uživatelů podle jména (top 20) | přihlášen |
| POST | `/api/friends/request?username=` | Odeslání žádosti o přátelství | přihlášen |
| POST | `/api/friends/accept/{friendshipId}` | Přijetí žádosti o přátelství | přihlášen |
| DELETE | `/api/friends/requests/{friendshipId}` | Odmítnutí žádosti o přátelství | přihlášen |
| DELETE | `/api/friends/with/{friendId}` | Zrušení přátelství | přihlášen |
| GET | `/api/friends` | Seznam přijatých přátel | přihlášen |
| GET | `/api/friends/activity` | Posledních 20 dokončených her přátel | přihlášen |
| GET | `/api/friends/requests` | Čekající žádosti o přátelství | přihlášen |

---

### Chat — `/api/chat`

| Metoda | Cesta | Popis | Auth |
|--------|-------|-------|------|
| POST | `/api/chat/send` | Odeslání zprávy (uložení do DB + push přes WebSocket) | přihlášen |
| GET | `/api/chat/history?friendUsername=` | Historie konverzace s přítelem | přihlášen |

---

### Notifikace — `/api/notifications`

| Metoda | Cesta | Popis | Auth |
|--------|-------|-------|------|
| GET | `/api/notifications` | Všechny notifikace přihlášeného uživatele | přihlášen |
| GET | `/api/notifications/unread-count` | Počet nepřečtených notifikací | přihlášen |
| POST | `/api/notifications/read-all` | Označení všech jako přečtené | přihlášen |
| POST | `/api/notifications/{id}/read` | Označení jedné notifikace jako přečtené | přihlášen |

---

### Sportsmanship — `/api/sportsmanship`

| Metoda | Cesta | Popis | Auth |
|--------|-------|-------|------|
| POST | `/api/sportsmanship` | Hlasování 👍 / 👎 za soupeře po dokončené hře (1× per zápas) | přihlášen |
| GET | `/api/sportsmanship/{username}` | Počty thumbsUp / thumbsDown pro hráče | přihlášen |
| GET | `/api/sportsmanship/check/{eventId}` | Zda přihlášený uživatel již hlasoval v daném zápasu | přihlášen |

---

### Hlášení — `/api/reports`

| Metoda | Cesta | Popis | Auth |
|--------|-------|-------|------|
| POST | `/api/reports` | Nahlášení hráče (důvod, popis) | přihlášen |

---

### Blokování — `/api/blocks`

| Metoda | Cesta | Popis | Auth |
|--------|-------|-------|------|
| POST | `/api/blocks/{username}` | Zablokování hráče | přihlášen |
| DELETE | `/api/blocks/{username}` | Odblokování hráče | přihlášen |
| GET | `/api/blocks/{username}` | Zjištění, zda je hráč zablokován | přihlášen |

---

### Admin panel — `/api/admin/**` _(pouze ROLE_ADMIN)_

| Metoda | Cesta | Popis |
|--------|-------|-------|
| GET | `/api/admin/reports` | Seznam všech nahlášení |
| POST | `/api/admin/ban/{userId}` | Zablokování uživatele |
| POST | `/api/admin/unban/{userId}` | Odblokování uživatele |
| POST | `/api/admin/resolve/{reportId}` | Uzavření nahlášení |
| GET | `/api/admin/disputes` | Sporné výsledky čekající na rozhodnutí |
| POST | `/api/admin/disputes/{eventId}/resolve` | Rozhodnutí sporu — admin vybere vítěze nebo remízu |
| GET | `/api/admin/fraud` | Fraud panel — podezřelé páry (3+ her za 7 dní) + hráči s vysokou mírou sporů |
| POST | `/api/admin/users/{userId}/reset-elo` | Reset ELO hráče na 1000 |

---

## 2. WebSocket (STOMP)

| Endpoint | Typ | Popis |
|----------|-----|-------|
| `/api/ws` | Handshake | HTTP → WebSocket upgrade |
| `/app/api/chat.send` | MessageMapping | Odeslání zprávy přes WebSocket |
| `/topic/chat/{username}` | Subscribe | Příjem zpráv v reálném čase |
| `/topic/notifications/{username}` | Subscribe | Příjem notifikací v reálném čase |

Autentizace: primárně HTTP session, fallback STOMP CONNECT login header.  
Každý uživatel se může přihlásit jen ke svým vlastním topic kanálům.

---

## 3. JPA entity (databázový model)

| Entita | Tabulka | Popis |
|--------|---------|-------|
| `User` | `users` | Uživatelský účet — username, role, ELO, statistiky, bio |
| `Game` | `games` | Katalog her s i18n popisy (CS/EN) |
| `GameEvent` | `game_events` | Výzva/hra — GPS, stav, výsledek, viditelnost, týmový mód |
| `TeamMember` | `team_members` | Člen týmu ve výzvě (strana CREATOR / CHALLENGER) |
| `Tournament` | `tournaments` | 1v1 turnaj (ELIMINATION / ROUND_ROBIN, kapacita 2–32) |
| `TournamentParticipant` | `tournament_participants` | Registrovaný hráč v turnaji |
| `TournamentMatch` | `tournament_matches` | Zápas v turnajovém bracketu |
| `TeamTournament` | `team_tournaments` | Týmový turnaj |
| `TournamentTeam` | `tournament_teams` | Tým v týmovém turnaji |
| `TournamentTeamMember` | `tournament_team_members` | Člen týmu |
| `TeamTournamentMatch` | `team_tournament_matches` | Zápas mezi týmy |
| `Friendship` | `friendships` | Přátelský vztah (PENDING / ACCEPTED) |
| `Message` | `messages` | Chatová zpráva |
| `Notification` | `notifications` | In-app notifikace (s příznakem přečtení) |
| `UserAchievement` | `user_achievements` | Získaný odznak hráče |
| `Sportsmanship` | `sportsmanship` | Hlasování fair play (voter, target, event, positive) |
| `Report` | `reports` | Nahlášení hráče pro moderaci |
| `PasswordResetToken` | `password_reset_tokens` | Token pro reset hesla (platnost 1 h) |
| `RatingHistory` | `rating_history` | Snímek ELO po každé hře |
| `Block` | `blocks` | Seznam zablokovaných hráčů |

---

## 4. Vue stránky a komponenty

| Soubor | Cesta | Popis |
|--------|-------|-------|
| `App.vue` | `/` (root) | Root komponenta — header, navigace, router-view |
| `HomePage.vue` | `/` | Úvodní stránka |
| `LoginPage.vue` | `/login` | Přihlašovací formulář |
| `RegisterPage.vue` | `/register` | Registrace s CAPTCHA |
| `ForgotPasswordPage.vue` | `/forgot-password` | Žádost o reset hesla |
| `ResetPasswordPage.vue` | `/reset-password` | Dokončení resetu hesla (token z e-mailu) |
| `PlayerPage.vue` | `/players/:username` | Profil hráče — bio, statistiky, reputace, achievementy |
| `MapPage.vue` | `/map` | Interaktivní mapa výzev (Leaflet.js) |
| `MyGamesPage.vue` | `/my-games` | Vlastní výzvy a jejich stavový přehled |
| `EventDetailPage.vue` | `/events/:id` | Detail výzvy — přijetí, schválení, výsledek, chat |
| `ChallengeModal.vue` | (modal) | Formulář pro přímé vyzvání hráče z jeho profilu |
| `LeaderboardPage.vue` | `/leaderboard` | Top 50 žebříček s ELO a ranky |
| `GameRulesPage.vue` | `/games` | Pravidla a popis všech her (CS/EN) |
| `TournamentListPage.vue` | `/tournaments` | Seznam turnajů + vytvoření nového |
| `TournamentDetailPage.vue` | `/tournaments/:id` | Bracket / standings 1v1 turnaje |
| `TeamTournamentDetailPage.vue` | `/team-tournaments/:id` | Bracket / standings týmového turnaje |
| `FriendsPage.vue` | `/friends` | Přátelé, žádosti, aktivita přátel |
| `ChatPopup.vue` | (overlay) | Real-time chat panel s přítelem |
| `AdminPage.vue` | `/admin` | Admin panel — reporty, bany, spory, fraud |
| `CookieBanner.vue` | (overlay) | Cookie souhlas |
| `PrivacyPage.vue` | `/privacy` | Zásady ochrany osobních údajů |
| `TermsPage.vue` | `/terms` | Obchodní podmínky |

---

## 5. Infrastruktura a konfigurace

### Zabezpečení

- Session-based autentizace přes `HttpSession` (Tomcat in-memory)
- Vlastní `SessionAuthFilter` — čte `user` atribut ze session před každým requestem
- CORS konfigurovatelné přes env proměnnou `CORS_ALLOWED_ORIGINS`
- CSRF vypnuto (session cookie + SameSite postačí)
- JSON chybové odpovědi místo přesměrování na login (401 / 403)
- Admin sekce chráněna rolí `ROLE_ADMIN`

### Cachování (Caffeine)

| Cache | TTL | Invalidace |
|-------|-----|------------|
| `publicEvents` | bez TTL | při změně stavu výzvy |
| `leaderboard` | 2 minuty | po nahlášení výsledku |
| `games` | bez TTL (statická data) | nikdy |

### E-mail (Spring Mail)

- SMTP konfigurace přes env proměnné: `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`
- Výchozí: `smtp.gmail.com:587` + STARTTLS
- Lokální dev: pokud není SMTP nakonfigurováno, loguje místo odesílání
- Odesílá: reset hesla s tokenem platným 1 hodinu

### Internacionalizace

- Frontend: `vue-i18n` — češtna (`cs.json`) a angličtina (`en.json`), uloženo v localStorage
- Backend: game entity mají dvojici polí `*Cs` / `*En` pro každý textový obsah

### Hodnocení hráčů

- ELO systém (výchozí 1000, min. floor 100)
- Každý výsledek hry aktualizuje rating obou hráčů
- Snímky ELO ukládány v `RatingHistory` → graf vývoje na profilu
- Ranky podle ELO:

| Rank | ELO |
|------|-----|
| Bronze | < 900 |
| Silver | 900–1099 |
| Gold | 1100–1299 |
| Platinum | 1300–1499 |
| Diamond | 1500+ |

### Achievementy (odznaky)

Systém odznaků udělovaných automaticky při dosažení milníků (první výhra, série výher, počet her v různých hrách atd.). Zobrazeny na profilu hráče.

### Anti-fraud opatření (Admin)

- **Podezřelé páry**: hráči, kteří sehráli ≥ 3 hry v posledních 7 dnech (riziko farmaření ELO)
- **Vysoká míra sporů**: uživatelé s > 25 % sporných výsledků a ≥ 5 her celkem
- Admin může resetovat ELO nebo zablokovat účet

### PWA (Progressive Web App)

- `manifest.json`: standalone mód, ikony 192 × 512 px, téma `#42b883`
- `sw.js`: network-first pro `/api/*`, cache-first pro statické assety
- Service worker registrován při načtení aplikace

### Databáze

| Profil | DB | DDL |
|--------|----|-----|
| dev | PostgreSQL přes Testcontainers (`withReuse(true)`) | `update` |
| výchozí / test | H2 in-memory | `create-drop` |

---

_Soubor vygenerován z průzkumu zdrojového kódu projektu ChallengeMe._
