# ChallengeMe

A social platform for real-life game meetups. Users pin challenge events on a map (Tic-Tac-Toe, Chess, …), meet at the location, play in person, and report the result.

---

## Stack

| Layer     | Technology                          |
|-----------|-------------------------------------|
| Backend   | Spring Boot 3.2 · Java 21 · JPA     |
| Database  | PostgreSQL 16                       |
| Frontend  | Vue 3 · Vite · Leaflet              |
| Server    | Nginx 1.27                          |
| Container | Docker · Docker Compose             |

---

## Quick start with Docker

### 1. Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (includes Docker Compose)

### 2. Set environment variables

The application reads secrets from the environment — nothing is hardcoded.  
Copy the example file and fill in your own values:

```bash
cp .env.example .env
```

Edit `.env`:

```dotenv
# Database credentials
DB_USER=challengeme
DB_PASS=your_strong_db_password

# Database name (optional, defaults to "challengeme")
DB_NAME=challengeme

# Admin panel login
ADMIN_USERNAME=admin
ADMIN_PASSWORD=your_strong_admin_password
```

> **Never commit `.env` to git** — it is already in `.gitignore`.

### 3. Build and start

```bash
docker compose up --build
```

First run takes a few minutes (Maven downloads dependencies, Node builds the frontend).  
Subsequent starts are fast because Docker caches the layers.

### 4. Open the app

| URL                        | What                          |
|----------------------------|-------------------------------|
| http://localhost           | Frontend (Vue app via Nginx)  |
| http://localhost:8080      | Backend API (Spring Boot)     |
| http://localhost:8080/h2-console | H2 console (dev profile only) |

The admin account is created automatically on first boot using the credentials from your `.env`.

### 5. Stop

```bash
docker compose down
```

Data in PostgreSQL is stored in a named Docker volume (`postgres_data`) and **survives restarts**.  
To wipe the database completely:

```bash
docker compose down -v
```

---

## Environment variables reference

| Variable         | Required | Default        | Description                        |
|------------------|----------|----------------|------------------------------------|
| `DB_USER`        | yes      | —              | PostgreSQL username                |
| `DB_PASS`        | yes      | —              | PostgreSQL password                |
| `DB_NAME`        | no       | `challengeme`  | PostgreSQL database name           |
| `ADMIN_USERNAME` | no       | `admin`        | Username of the built-in admin     |
| `ADMIN_PASSWORD` | yes      | —              | Password of the built-in admin     |

### Setting variables without a `.env` file

If you prefer to export variables directly in your shell session instead of using a file:

**Linux / macOS:**
```bash
export DB_USER=challengeme
export DB_PASS=secret
export DB_NAME=challengeme
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=secret
docker compose up --build
```

**Windows (PowerShell):**
```powershell
$env:DB_USER="challengeme"
$env:DB_PASS="secret"
$env:DB_NAME="challengeme"
$env:ADMIN_USERNAME="admin"
$env:ADMIN_PASSWORD="secret"
docker compose up --build
```

**Windows (CMD):**
```cmd
set DB_USER=challengeme
set DB_PASS=secret
set DB_NAME=challengeme
set ADMIN_USERNAME=admin
set ADMIN_PASSWORD=secret
docker compose up --build
```

Docker Compose automatically reads variables from the shell environment, so you don't need a `.env` file if they are already exported.

---

## Local development (without Docker)

### Backend

Requires Java 21 and Maven installed.

The default profile uses an **in-memory H2 database** — no setup needed:

```bash
mvn spring-boot:run
```

To use a real PostgreSQL (via Testcontainers — requires Docker running):

```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

### Frontend

Requires Node.js 18+.

```bash
cd frontend
npm install
npm run dev
```

The dev server starts on http://localhost:5173 and proxies API calls to `http://localhost:8080`.

---

## Architecture overview

```
Browser
  │
  ├─ http://localhost        → Nginx (port 80)
  │    └─ static files from /usr/share/nginx/html (Vue build output)
  │
  └─ http://localhost:8080   → Spring Boot (port 8080)
       └─ REST API · WebSocket /ws
            └─ PostgreSQL (port 5432, internal only)
```

PostgreSQL is not exposed to the host — only the backend container can reach it.

---

## Database persistence

Data is stored in a named Docker volume:

```bash
# List volumes
docker volume ls

# Inspect where data lives on disk
docker volume inspect challengeme_postgres_data
```

To back up the database:

```bash
docker compose exec db pg_dump -U $DB_USER $DB_NAME > backup.sql
```

To restore:

```bash
docker compose exec -T db psql -U $DB_USER $DB_NAME < backup.sql
```

---

## Debugging

### Start in debug mode

Use the `docker-compose.debug.yml` override on top of the main file:

```bash
docker compose -f docker-compose.yml -f docker-compose.debug.yml up --build
```

This enables everything listed below compared to a normal start.

### What debug mode adds

| Feature | URL / port | Description |
|---------|-----------|-------------|
| **Adminer** (DB web UI) | http://localhost:8888 | Browse tables, run SQL queries in the browser |
| **PostgreSQL** direct access | `localhost:5432` | Connect with DBeaver, pgAdmin, DataGrip, … |
| **JVM remote debug** | `localhost:5005` | Attach IntelliJ / VS Code debugger |
| **Spring Boot Actuator** | http://localhost:8080/actuator | Health, beans, env, mappings, … |
| **Verbose SQL logs** | `docker compose logs -f backend` | Every SQL query printed to stdout |

---

### Adminer (DB in browser)

Open http://localhost:8888 and fill in:

| Field | Value |
|-------|-------|
| System | PostgreSQL |
| Server | `db` |
| Username | value of `DB_USER` from your `.env` |
| Password | value of `DB_PASS` from your `.env` |
| Database | value of `DB_NAME` (default: `challengeme`) |

---

### Connect with an external DB client (DBeaver, pgAdmin, …)

In debug mode port 5432 is mapped to your host. Create a connection with:

```
Host:     localhost
Port:     5432
Database: challengeme   (or your DB_NAME)
User:     <DB_USER>
Password: <DB_PASS>
```

---

### JVM remote debug (IntelliJ IDEA)

1. Run the stack in debug mode (see above).
2. In IntelliJ: **Run → Edit Configurations → + → Remote JVM Debug**
3. Set:
   - Host: `localhost`
   - Port: `5005`
   - Command line arguments: `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005`
4. Click **Debug** — IntelliJ connects and breakpoints work normally.

> The JVM starts with `suspend=n`, so the app boots immediately even without a debugger attached.

---

### Logs

```bash
# Follow all services
docker compose logs -f

# Follow only the backend
docker compose logs -f backend

# Last 100 lines of the database
docker compose logs --tail=100 db
```

---

### Shell inside a container

```bash
# Shell into the backend (Alpine — use sh, not bash)
docker compose exec backend sh

# PostgreSQL interactive console
docker compose exec db psql -U $DB_USER $DB_NAME
```

Useful psql commands once inside:

```sql
-- List all tables
\dt

-- Inspect a table
\d users

-- Quick check of registered users
SELECT id, username, email, role, banned FROM users;

-- Check game events
SELECT id, status, game_type, scheduled_at FROM game_events ORDER BY id DESC LIMIT 20;
```

---

### Spring Boot Actuator endpoints (debug mode)

| Endpoint | What it shows |
|----------|---------------|
| `/actuator/health` | DB connection, disk, app status |
| `/actuator/env` | All env variables and Spring properties |
| `/actuator/beans` | Every Spring bean loaded |
| `/actuator/mappings` | All HTTP routes |
| `/actuator/loggers` | Logger levels (can be changed at runtime via POST) |

Change a log level at runtime without restart:

```bash
# Set org.jan to TRACE
curl -X POST http://localhost:8080/actuator/loggers/org.jan \
     -H "Content-Type: application/json" \
     -d '{"configuredLevel":"TRACE"}'
```
