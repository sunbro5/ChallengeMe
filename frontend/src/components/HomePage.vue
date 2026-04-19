<template>
  <div class="home-layout">

    <!-- ── Left panel ─────────────────────────────────────────────────── -->
    <div class="side-panel">
      <div class="brand">
        <span class="brand-dot"></span>
        <span class="brand-name">{{ $t('appName') }}</span>
      </div>
      <p class="tagline">{{ $t('home.tagline') }}</p>

      <div class="stats-row" v-if="eventCount > 0">
        <span class="stat-pill">📍 {{ eventCount }} {{ $t('home.activeEvents') }}</span>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <h2>{{ $t('login.title') }}</h2>

        <input
          v-model="form.username"
          :placeholder="$t('login.username')"
          autocomplete="username"
          required
        />
        <input
          v-model="form.password"
          type="password"
          :placeholder="$t('login.password')"
          autocomplete="current-password"
          required
        />

        <button type="submit" :disabled="busy">
          {{ busy ? '…' : $t('login.loginBtn') }}
        </button>

        <p v-if="error" class="msg-error">{{ error }}</p>
      </form>

      <p class="register-link">
        {{ $t('login.noAccount') }}
        <router-link to="/register">{{ $t('login.registerHere') }}</router-link>
      </p>

      <p class="tos-link-row">
        <router-link to="/tos">{{ $t('home.tosLink') }}</router-link>
      </p>

      <div class="features">
        <div class="feature">
          <span class="feature-icon">📍</span>
          <span>{{ $t('home.feature1') }}</span>
        </div>
        <div class="feature">
          <span class="feature-icon">⚔️</span>
          <span>{{ $t('home.feature2') }}</span>
        </div>
        <div class="feature">
          <span class="feature-icon">🏆</span>
          <span>{{ $t('home.feature3') }}</span>
        </div>
      </div>
    </div>

    <!-- ── Map ────────────────────────────────────────────────────────── -->
    <div class="map-area">
      <div id="home-map" ref="mapEl"></div>
      <div class="map-overlay-hint">{{ $t('home.mapHint') }}</div>
    </div>

  </div>
</template>

<script>
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { markRaw } from 'vue'
import axios from 'axios'

const FALLBACK = { icon: '🎮', color: '#42b883' }
let gameMeta = {}

function makeMarker(gameType) {
  const meta = gameMeta[gameType] || FALLBACK
  return L.divIcon({
    className: '',
    html: `<div style="
      background:${meta.color};border:2px solid rgba(255,255,255,.25);border-radius:50%;
      width:38px;height:38px;display:flex;align-items:center;justify-content:center;
      font-size:18px;box-shadow:0 2px 10px rgba(0,0,0,.5);cursor:default;
    ">${meta.icon}</div>`,
    iconSize: [38, 38], iconAnchor: [19, 19],
  })
}

export default {
  name: 'HomePage',
  data() {
    return {
      map:        null,
      form:       { username: '', password: '' },
      busy:       false,
      error:      '',
      eventCount: 0,
    }
  },
  mounted() {
    if (localStorage.getItem('isLoggedIn') === 'true') {
      this.$router.replace('/map')
      return
    }
    this.initMap()
  },
  beforeUnmount() {
    if (this.map) this.map.remove()
  },
  methods: {
    async initMap() {
      await this.$nextTick()
      this.map = markRaw(L.map(this.$refs.mapEl, { zoomControl: true }).setView([50.0755, 14.4378], 6))
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors', maxZoom: 19,
      }).addTo(this.map)

      await this.loadGames()

      // Geolocation only for map centering — not sent to API
      const pos = await this.getPosition()
      if (pos) this.map.setView([pos.lat, pos.lng], 11)
      await this.loadEvents()
    },

    getPosition() {
      return new Promise(resolve => {
        if (!navigator.geolocation) { resolve(null); return }
        const timer = setTimeout(() => resolve(null), 5000)
        navigator.geolocation.getCurrentPosition(
          p  => { clearTimeout(timer); resolve({ lat: p.coords.latitude, lng: p.coords.longitude }) },
          () => { clearTimeout(timer); resolve(null) }
        )
      })
    },

    async loadGames() {
      try {
        const { data } = await axios.get('/api/games')
        const colours = ['#4f8ef7', '#f7864f', '#a6e3a1', '#cba6f7', '#f9e2af', '#89dceb',
                         '#42b883', '#f38ba8', '#fab387', '#94e2d5']
        data.forEach((g, i) => {
          gameMeta[g.key] = { icon: g.icon, color: colours[i % colours.length] }
        })
      } catch { /* non-fatal */ }
    },

    async loadEvents() {
      try {
        const { data } = await axios.get('/api/events/public')
        this.eventCount = data.length
        data.forEach(e => {
          L.marker([e.latitude, e.longitude], { icon: makeMarker(e.gameType) })
            .addTo(this.map)
        })
      } catch { /* non-fatal */ }
    },

    async handleLogin() {
      this.error = ''
      this.busy  = true
      try {
        await axios.post('/api/auth/login', {
          username: this.form.username,
          password: this.form.password,
        }, { withCredentials: true })
        localStorage.setItem('isLoggedIn', 'true')
        localStorage.setItem('username',   this.form.username)
        this.$router.push('/map')
      } catch (err) {
        this.error = err.response?.data || this.$t('login.failed')
      } finally {
        this.busy = false
      }
    },
  },
}
</script>

<style scoped>
.home-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* ── Side panel ──────────────────────────────────────────────────────── */
.side-panel {
  width: 340px;
  flex-shrink: 0;
  background: var(--bg-surface);
  border-right: 1px solid var(--border);
  padding: 28px 28px 24px;
  display: flex;
  flex-direction: column;
  gap: 0;
  overflow-y: auto;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.brand-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--brand);
  box-shadow: 0 0 8px rgba(66,184,131,.6);
  flex-shrink: 0;
}
.brand-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -.02em;
}

.tagline {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.5;
  margin-bottom: 14px;
}

.stats-row {
  margin-bottom: 18px;
}
.stat-pill {
  display: inline-block;
  background: var(--brand-muted);
  color: var(--brand);
  border: 1px solid rgba(66,184,131,.2);
  border-radius: var(--r-full);
  padding: 3px 12px;
  font-size: 12px;
  font-weight: 600;
}

/* Login form */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 14px;
}
.login-form h2 {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 2px;
}
.login-form input {
  background: var(--bg-elevated);
  color: var(--text-primary);
  border: 1px solid var(--border);
  border-radius: var(--r);
  padding: 9px 12px;
  font-size: 13px;
  font-family: var(--font);
  outline: none;
  transition: border-color var(--transition), box-shadow var(--transition);
}
.login-form input::placeholder { color: var(--text-muted); }
.login-form input:focus {
  border-color: var(--brand);
  box-shadow: 0 0 0 3px rgba(66,184,131,.12);
}
.login-form button[type="submit"] {
  background: var(--brand);
  color: #fff;
  border: none;
  border-radius: var(--r);
  padding: 9px;
  font-size: 13px;
  font-weight: 600;
  font-family: var(--font);
  cursor: pointer;
  transition: background var(--transition);
  margin-top: 2px;
}
.login-form button[type="submit"]:hover:not(:disabled) { background: var(--brand-hover); }
.login-form button[type="submit"]:disabled { opacity: .45; cursor: default; }

.msg-error {
  color: var(--red);
  font-size: 12px;
  background: var(--red-muted);
  border: 1px solid rgba(248,81,73,.2);
  border-radius: var(--r);
  padding: 7px 10px;
  line-height: 1.4;
}

.register-link {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 6px;
}
.register-link a { color: var(--brand); text-decoration: none; font-weight: 600; }
.register-link a:hover { text-decoration: underline; }

.tos-link-row {
  font-size: 11px;
  color: var(--text-muted);
  margin-bottom: 22px;
}
.tos-link-row a { color: var(--blue); text-decoration: underline; }

/* Feature teasers */
.features {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: auto;
  padding-top: 10px;
  border-top: 1px solid var(--border-muted);
}
.feature {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.4;
}
.feature-icon { font-size: 16px; flex-shrink: 0; }

/* ── Map ─────────────────────────────────────────────────────────────── */
.map-area {
  flex: 1;
  position: relative;
  overflow: hidden;
}
#home-map {
  width: 100%;
  height: 100%;
}

.map-overlay-hint {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(13,17,23,.85);
  color: var(--text-secondary);
  border: 1px solid var(--border);
  border-radius: var(--r-full);
  padding: 7px 18px;
  font-size: 12px;
  backdrop-filter: blur(4px);
  pointer-events: none;
  white-space: nowrap;
}

/* ── Mobile ──────────────────────────────────────────────────────────── */
@media (max-width: 680px) {
  .home-layout { flex-direction: column; }
  .side-panel  { width: 100%; height: auto; border-right: none; border-bottom: 1px solid var(--border); overflow: visible; }
  .map-area    { min-height: 50vh; }
  .features    { display: none; }
}
</style>
