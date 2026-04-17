<template>
  <div id="app">
    <div v-if="isLoggedIn" class="logged-in-container">
      <div class="header">
        <div class="header-left">
          <h1>ChallengeMe</h1>
          <nav class="nav-links">
            <router-link to="/map" class="nav-link">{{ $t('nav.map') }}</router-link>
            <router-link to="/leaderboard" class="nav-link">{{ $t('nav.leaderboard') }}</router-link>
            <router-link to="/friends" class="nav-link">{{ $t('nav.friends') }}</router-link>
            <router-link to="/games" class="nav-link">{{ $t('nav.games') }}</router-link>
            <router-link v-if="isAdmin" to="/admin" class="nav-link admin-link">{{ $t('nav.admin') }}</router-link>
          </nav>
        </div>
        <div class="header-right">
          <div class="avatar-wrap" ref="avatarWrap">
            <button class="avatar-btn" @click="menuOpen = !menuOpen" :title="username">
              {{ username[0]?.toUpperCase() }}
            </button>
            <transition name="menu-drop">
              <div v-if="menuOpen" class="avatar-menu">
                <div class="avatar-menu-name">{{ username }}</div>
                <hr class="avatar-menu-divider" />
                <button class="avatar-menu-item" @click="goToProfile">
                  {{ $t('user.profile') }}
                </button>
                <button class="avatar-menu-item" @click="goToMyGames">
                  {{ $t('user.myGames') }}
                </button>
                <button class="avatar-menu-item danger" @click="logout">
                  {{ $t('user.logout') }}
                </button>
              </div>
            </transition>
          </div>
        </div>
      </div>
      <router-view />
      <ChatPopup @notification="addToast" />

      <!-- Mobile bottom navigation -->
      <nav class="bottom-nav">
        <router-link to="/map"         class="bnav-link"><span class="bnav-icon">🗺️</span><span class="bnav-label">{{ $t('nav.mapShort') }}</span></router-link>
        <router-link to="/leaderboard" class="bnav-link"><span class="bnav-icon">🏆</span><span class="bnav-label">{{ $t('nav.leaderboardShort') }}</span></router-link>
        <router-link to="/friends"     class="bnav-link"><span class="bnav-icon">👥</span><span class="bnav-label">{{ $t('nav.friendsShort') }}</span></router-link>
        <router-link to="/games"       class="bnav-link"><span class="bnav-icon">🎮</span><span class="bnav-label">{{ $t('nav.gamesShort') }}</span></router-link>
        <router-link v-if="isAdmin" to="/admin" class="bnav-link bnav-admin"><span class="bnav-icon">⚙️</span><span class="bnav-label">{{ $t('nav.adminShort') }}</span></router-link>
      </nav>

      <!-- Toast notifications -->
      <div class="toast-container">
        <transition-group name="toast-slide">
          <div
            v-for="t in toasts"
            :key="t.id"
            class="toast"
            @click="removeToast(t.id)"
          >
            {{ t.text }}
          </div>
        </transition-group>
      </div>
    </div>
    <div v-else :class="['auth-container', { 'auth-full': isHomePage }]">
      <h1 v-if="!isHomePage">ChallengeMe</h1>
      <router-view />
    </div>

    <!-- Language switcher -->
    <div class="lang-bar">
      <button
        v-for="loc in ['cs', 'en']"
        :key="loc"
        :class="['lang-btn', { active: currentLocale === loc }]"
        @click="setLocale(loc)"
      >{{ $t('lang.' + loc) }}</button>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { useI18n } from 'vue-i18n'
import ChatPopup from './components/ChatPopup.vue'
import i18n from './i18n'

export default {
  name: 'App',
  components: { ChatPopup },
  setup() {
    const { locale } = useI18n()
    return { locale }
  },
  data() {
    return {
      isLoggedIn: false,
      isAdmin: false,
      username: '',
      menuOpen: false,
      toasts: [],
      _toastSeq: 0,
    }
  },
  computed: {
    currentLocale() {
      return this.locale
    },
    isHomePage() {
      return this.$route.path === '/'
    },
  },
  mounted() {
    this.syncAuth()
    document.addEventListener('click', this.onDocClick)
  },
  beforeUnmount() {
    document.removeEventListener('click', this.onDocClick)
  },
  watch: {
    '$route'() {
      this.syncAuth()
      this.menuOpen = false
    }
  },
  methods: {
    syncAuth() {
      this.isLoggedIn = localStorage.getItem('isLoggedIn') === 'true'
      this.isAdmin    = localStorage.getItem('role') === 'ADMIN'
      this.username   = localStorage.getItem('username') || ''
    },
    onDocClick(e) {
      if (this.$refs.avatarWrap && !this.$refs.avatarWrap.contains(e.target)) {
        this.menuOpen = false
      }
    },
    goToProfile() {
      this.menuOpen = false
      this.$router.push(`/player/${this.username}`)
    },
    goToMyGames() {
      this.menuOpen = false
      this.$router.push('/my-games')
    },
    setLocale(loc) {
      i18n.global.locale.value = loc
      localStorage.setItem('locale', loc)
    },
    addToast(notif) {
      let text = '🔔 New notification'
      if (notif.type === 'CHALLENGE_ACCEPTED') {
        text = this.$t('toast.challengeAccepted', { username: notif.challengerUsername })
      }
      const id = ++this._toastSeq
      this.toasts.push({ id, text })
      setTimeout(() => this.removeToast(id), 6000)
    },
    removeToast(id) {
      this.toasts = this.toasts.filter(t => t.id !== id)
    },
    logout() {
      this.menuOpen = false
      localStorage.removeItem('isLoggedIn')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      this.isLoggedIn = false
      this.isAdmin    = false
      this.username   = ''
      this.$router.push('/login')
      axios.post('/api/auth/logout', {}, { withCredentials: true })
        .catch(err => console.error('Logout error:', err))
    },
  }
}
</script>

<style>
#app {
  font-family: var(--font);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* ── Auth layout ─────────────────────────────────────────────────────────── */
.auth-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

/* Full-screen variant — used by HomePage so the map can fill the viewport */
.auth-container.auth-full {
  padding: 0;
  align-items: stretch;
  justify-content: flex-start;
  height: 100vh;
  overflow: hidden;
}

.auth-container h1 {
  color: var(--brand);
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 28px;
  letter-spacing: -.02em;
}

/* ── App shell ───────────────────────────────────────────────────────────── */
.logged-in-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* ── Header ──────────────────────────────────────────────────────────────── */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 52px;
  background: var(--bg-surface);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 1010;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

h1 {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -.01em;
  display: flex;
  align-items: center;
  gap: 7px;
}
h1::before {
  content: '';
  display: inline-block;
  width: 7px;
  height: 7px;
  background: var(--brand);
  border-radius: 50%;
  flex-shrink: 0;
}

.nav-links {
  display: flex;
  gap: 2px;
}

.nav-link {
  color: var(--text-secondary);
  text-decoration: none;
  padding: 5px 11px;
  border-radius: var(--r);
  font-size: 13px;
  font-weight: 500;
  transition: color var(--transition), background var(--transition);
}
.nav-link:hover {
  color: var(--text-primary);
  background: var(--bg-elevated);
}
.nav-link.router-link-active {
  color: var(--text-primary);
  background: var(--bg-elevated);
  font-weight: 600;
}

.admin-link { color: var(--red); }
.admin-link:hover { color: var(--red); background: var(--red-muted); }
.admin-link.router-link-active { color: var(--red); background: var(--red-muted); }

.header-right { display: flex; align-items: center; }

/* ── Avatar + dropdown ───────────────────────────────────────────────────── */
.avatar-wrap { position: relative; }

.avatar-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--brand-muted);
  border: 1px solid rgba(66,184,131,.4);
  color: var(--brand);
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background var(--transition);
  line-height: 1;
}
.avatar-btn:hover { background: rgba(66,184,131,.22); }

.avatar-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  background: var(--bg-overlay);
  border: 1px solid var(--border);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-lg);
  min-width: 156px;
  z-index: 9999;
  overflow: hidden;
}

.avatar-menu-name {
  padding: 10px 14px 6px;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: .05em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.avatar-menu-divider {
  border: none;
  border-top: 1px solid var(--border);
  margin: 0;
}

.avatar-menu-item {
  display: block;
  width: 100%;
  padding: 9px 14px;
  font-size: 13px;
  color: var(--text-primary);
  background: none;
  border: none;
  text-align: left;
  cursor: pointer;
  transition: background var(--transition);
  font-family: var(--font);
}
.avatar-menu-item:hover { background: var(--bg-elevated); }
.avatar-menu-item.danger { color: var(--red); }
.avatar-menu-item.danger:hover { background: var(--red-muted); }

.menu-drop-enter-active { transition: opacity .15s ease, transform .15s ease; }
.menu-drop-leave-active { transition: opacity .1s ease, transform .1s ease; }
.menu-drop-enter-from,
.menu-drop-leave-to     { opacity: 0; transform: translateY(-6px); }

/* ── Toast ───────────────────────────────────────────────────────────────── */
.toast-container {
  position: fixed;
  bottom: 88px;
  right: 20px;
  z-index: 9000;
  display: flex;
  flex-direction: column-reverse;
  gap: 8px;
  pointer-events: none;
}

.toast {
  background: var(--bg-overlay);
  color: var(--text-primary);
  padding: 11px 16px;
  border-radius: var(--r-md);
  font-size: 13px;
  max-width: 300px;
  box-shadow: var(--shadow);
  cursor: pointer;
  pointer-events: all;
  border-left: 3px solid var(--brand);
  line-height: 1.5;
}

.toast-slide-enter-active { transition: opacity .25s ease, transform .25s ease; }
.toast-slide-leave-active { transition: opacity .2s ease, transform .2s ease; }
.toast-slide-enter-from,
.toast-slide-leave-to     { opacity: 0; transform: translateX(16px); }

/* ── Bottom navigation (mobile only) ────────────────────────────────────── */
.bottom-nav {
  display: none;
  position: fixed;
  bottom: 0; left: 0; right: 0;
  height: 56px;
  background: var(--bg-surface);
  border-top: 1px solid var(--border);
  z-index: 1010;
}

.bnav-link {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  color: var(--text-muted);
  text-decoration: none;
  min-height: 44px;
  transition: color var(--transition);
}
.bnav-link.router-link-active { color: var(--brand); }
.bnav-link.bnav-admin { color: var(--red); }
.bnav-link.bnav-admin.router-link-active { color: var(--red); }
.bnav-icon  { font-size: 20px; line-height: 1; }
.bnav-label { font-size: 10px; font-weight: 500; }

/* ── Mobile breakpoint ───────────────────────────────────────────────────── */
@media (max-width: 640px) {
  .nav-links  { display: none; }
  .bottom-nav { display: flex; }

  /* push lang-bar and toasts above the bottom nav */
  .lang-bar        { bottom: 64px; }
  .toast-container { bottom: 124px; right: 12px; }
  .toast           { max-width: calc(100vw - 24px); font-size: 12px; }

  /* add bottom breathing room on all scrollable pages */
  body { padding-bottom: 56px; }

  /* tighten header padding on narrow screens */
  .header { padding: 0 12px; }
  .header-left { gap: 10px; }
}

/* ── Language switcher ───────────────────────────────────────────────────── */
.lang-bar {
  position: fixed;
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 8000;
  display: flex;
  gap: 2px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: var(--r-full);
  padding: 3px;
}

.lang-btn {
  background: transparent;
  border: none;
  color: var(--text-muted);
  font-size: 11px;
  font-weight: 600;
  padding: 3px 12px;
  border-radius: var(--r-full);
  cursor: pointer;
  transition: background var(--transition), color var(--transition);
  font-family: var(--font);
}
.lang-btn:hover { color: var(--text-primary); background: var(--bg-overlay); }
.lang-btn.active { background: var(--bg-overlay); color: var(--text-primary); }
</style>
