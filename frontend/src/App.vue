<template>
  <div id="app">
    <div v-if="isLoggedIn" class="logged-in-container">
      <div class="header">
        <div class="header-left">
          <h1>{{ $t('appName') }}</h1>
          <nav class="nav-links">
            <router-link to="/map" class="nav-link">{{ $t('nav.map') }}</router-link>
            <router-link to="/leaderboard" class="nav-link">{{ $t('nav.leaderboard') }}</router-link>
            <router-link to="/friends" class="nav-link">{{ $t('nav.friends') }}</router-link>
            <router-link to="/games" class="nav-link">{{ $t('nav.games') }}</router-link>
            <router-link to="/tournaments" class="nav-link">{{ $t('nav.tournaments') }}</router-link>
            <router-link v-if="isAdmin" to="/admin" class="nav-link admin-link">{{ $t('nav.admin') }}</router-link>
          </nav>
        </div>
        <div class="header-right">
          <!-- Notification bell -->
          <div class="bell-wrap" ref="bellWrap">
            <button class="bell-btn" @click="toggleBell" title="Notifications">
              🔔
              <span v-if="unreadCount > 0" class="bell-badge">{{ unreadCount > 9 ? '9+' : unreadCount }}</span>
            </button>
            <transition name="menu-drop">
              <div v-if="bellOpen" class="notif-dropdown">
                <div class="notif-header">
                  <span>Notifications</span>
                  <button v-if="unreadCount > 0" class="notif-read-all" @click="markAllRead">Mark all read</button>
                </div>
                <div v-if="notifications.length === 0" class="notif-empty">No notifications</div>
                <div
                  v-for="n in notifications"
                  :key="n.id"
                  :class="['notif-item', { unread: !n.read }]"
                  @click="openNotification(n)"
                >
                  <span class="notif-text">{{ notifText(n) }}</span>
                  <span class="notif-time">{{ formatNotifTime(n.createdAt) }}</span>
                </div>
              </div>
            </transition>
          </div>

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
                <hr class="avatar-menu-divider" />
                <div class="avatar-menu-lang">
                  <button
                    v-for="loc in ['cs', 'en']"
                    :key="loc"
                    :class="['avatar-lang-btn', { active: currentLocale === loc }]"
                    @click="setLocale(loc)"
                  >{{ $t('lang.' + loc) }}</button>
                </div>
                <div class="avatar-menu-theme">
                  <button :class="['avatar-theme-btn', { active: !darkMode }]" @click="setTheme('light')">{{ $t('theme.light') }}</button>
                  <button :class="['avatar-theme-btn', { active: darkMode }]" @click="setTheme('dark')">{{ $t('theme.dark') }}</button>
                </div>
                <hr class="avatar-menu-divider" />
                <router-link to="/tos"     class="avatar-menu-item-link">{{ $t('footer.terms') }}</router-link>
                <router-link to="/privacy" class="avatar-menu-item-link">{{ $t('footer.privacy') }}</router-link>
                <button class="avatar-menu-item muted" @click="resetCookies">{{ $t('footer.cookieSettings') }}</button>
                <hr class="avatar-menu-divider" />
                <button class="avatar-menu-item danger" @click="logout">
                  {{ $t('user.logout') }}
                </button>
              </div>
            </transition>
          </div>
        </div>
      </div>
      <div class="page-content">
        <router-view />
      </div>
      <ChatPopup v-if="showChatPopup" @notification="addToast" />

      <!-- Mobile bottom navigation -->
      <nav class="bottom-nav">
        <router-link to="/map"         class="bnav-link">{{ $t('nav.mapShort') }}</router-link>
        <router-link to="/leaderboard" class="bnav-link">{{ $t('nav.leaderboardShort') }}</router-link>
        <router-link to="/friends"     class="bnav-link">{{ $t('nav.friendsShort') }}</router-link>
        <router-link to="/games"       class="bnav-link">{{ $t('nav.gamesShort') }}</router-link>
        <router-link to="/tournaments" class="bnav-link">{{ $t('nav.tournamentsShort') }}</router-link>
        <router-link v-if="isAdmin" to="/admin" class="bnav-link bnav-admin">{{ $t('nav.adminShort') }}</router-link>
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
      <h1 v-if="!isHomePage">{{ $t('appName') }}</h1>
      <router-view />
      <!-- Legal footer — visible on login/register/forgot-password etc. -->
      <footer class="legal-footer">
        <router-link to="/tos" class="footer-link">{{ $t('footer.terms') }}</router-link>
        <span class="footer-sep">·</span>
        <router-link to="/privacy" class="footer-link">{{ $t('footer.privacy') }}</router-link>
        <span class="footer-sep">·</span>
        <span class="footer-copy">{{ $t('footer.contact') }}</span>
      </footer>
    </div>

    <!-- Cookie consent banner — shown on first visit, above bottom nav -->
    <CookieBanner ref="cookieBanner" @consent="onCookieConsent" />

  </div>
</template>

<script>
import axios from 'axios'
import { useI18n } from 'vue-i18n'
import ChatPopup from './components/ChatPopup.vue'
import CookieBanner from './components/CookieBanner.vue'
import i18n from './i18n'
import { initAnalytics } from './analytics.js'

export default {
  name: 'App',
  components: { ChatPopup, CookieBanner },
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
      darkMode: true,
      bellOpen: false,
      notifications: [],
      unreadCount: 0,
      _notifPoll: null,
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
    // Hide the global chat popup on event detail — it shares the same message store
    // and would duplicate every conversation the user is already having inline.
    showChatPopup() {
      return !this.$route.path.startsWith('/event/')
    },
  },
  mounted() {
    this.syncAuth()
    document.addEventListener('click', this.onDocClick)
    if (this.isLoggedIn) this.startNotifPoll()
    // Restore saved theme
    const savedTheme = localStorage.getItem('theme')
    this.darkMode = savedTheme !== 'light'
    this.applyTheme(this.darkMode)
  },
  beforeUnmount() {
    document.removeEventListener('click', this.onDocClick)
    clearInterval(this._notifPoll)
  },
  watch: {
    '$route'() {
      this.syncAuth()
      this.menuOpen = false
      this.bellOpen = false
    },
    isLoggedIn(val) {
      if (val) this.startNotifPoll()
      else { clearInterval(this._notifPoll); this.unreadCount = 0; this.notifications = [] }
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
      if (this.$refs.bellWrap && !this.$refs.bellWrap.contains(e.target)) {
        this.bellOpen = false
      }
    },
    startNotifPoll() {
      this.fetchUnreadCount()
      clearInterval(this._notifPoll)
      this._notifPoll = setInterval(this.fetchUnreadCount, 30000)
    },
    async fetchUnreadCount() {
      try {
        const { data } = await axios.get('/api/notifications/unread-count', { withCredentials: true })
        this.unreadCount = data
      } catch { /* not logged in or failed */ }
    },
    async toggleBell() {
      this.bellOpen = !this.bellOpen
      this.menuOpen = false
      if (this.bellOpen) {
        try {
          const { data } = await axios.get('/api/notifications', { withCredentials: true })
          this.notifications = data
        } catch { /* non-fatal */ }
      }
    },
    async markAllRead() {
      try {
        await axios.post('/api/notifications/read-all', {}, { withCredentials: true })
        this.notifications.forEach(n => { n.read = true })
        this.unreadCount = 0
      } catch { /* non-fatal */ }
    },
    openNotification(n) {
      if (!n.read) {
        axios.post(`/api/notifications/${n.id}/read`, {}, { withCredentials: true }).catch(() => {})
        n.read = true
        if (this.unreadCount > 0) this.unreadCount--
      }
      this.bellOpen = false
      if (n.eventId) this.$router.push(`/event/${n.eventId}`)
    },
    notifText(n) {
      if (n.type && n.actorUsername) {
        const key = `notif.${n.type}`
        const t = this.$t(key, { actor: n.actorUsername })
        if (t !== key) return t
      }
      return n.text
    },
    formatNotifTime(iso) {
      if (!iso) return ''
      const d = new Date(iso)
      const now = new Date()
      const diffH = Math.floor((now - d) / 3600000)
      if (diffH < 1) return 'Just now'
      if (diffH < 24) return `${diffH}h ago`
      return d.toLocaleDateString()
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
    setTheme(mode) {
      this.darkMode = mode === 'dark'
      this.applyTheme(this.darkMode)
    },
    applyTheme(dark) {
      if (dark) {
        document.documentElement.classList.remove('light')
      } else {
        document.documentElement.classList.add('light')
      }
      localStorage.setItem('theme', dark ? 'dark' : 'light')
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
    onCookieConsent(consent) {
      initAnalytics(consent)
    },
    resetCookies() {
      this.$refs.cookieBanner?.reset()
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

/* Wrapper that sits between the header and the bottom-nav.
   On mobile it gets padding-top so content starts below the fixed header. */
.page-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
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
.avatar-menu-item.muted { color: var(--text-muted); font-size: 11px; }
.avatar-menu-item.muted:hover { color: var(--text-secondary); background: var(--bg-elevated); }

/* Legal links in avatar menu */
.avatar-menu-item-link {
  display: block;
  padding: 8px 14px;
  font-size: 12px;
  color: var(--text-muted);
  text-decoration: none;
  transition: background var(--transition), color var(--transition);
  font-family: var(--font);
}
.avatar-menu-item-link:hover {
  background: var(--bg-elevated);
  color: var(--text-primary);
}

/* Legal footer on auth pages */
.legal-footer {
  margin-top: 32px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}
.footer-link {
  font-size: 11px;
  color: var(--text-muted);
  text-decoration: none;
  transition: color var(--transition);
}
.footer-link:hover { color: var(--brand); }
.footer-sep { font-size: 11px; color: var(--border); }
.footer-copy { font-size: 11px; color: var(--text-muted); }

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
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  text-decoration: none;
  font-size: 12px;
  font-weight: 500;
  min-height: 44px;
  transition: color var(--transition);
}
.bnav-link.router-link-active {
  color: var(--brand);
  font-weight: 600;
}
.bnav-link.bnav-admin { color: var(--red); }
.bnav-link.bnav-admin.router-link-active { color: var(--red); }

/* ── Mobile breakpoint ───────────────────────────────────────────────────── */
@media (max-width: 640px) {
  .nav-links  { display: none; }
  .bottom-nav { display: flex; }

  /* ── Fixed header: always visible, cannot scroll away ────────────────── */
  .header {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    padding: 0 12px;
    /* iPhone notch / Dynamic Island — extend header into the safe area */
    padding-top: env(safe-area-inset-top, 0px);
    height: var(--shell-top);
  }
  .header-left { gap: 10px; }

  /* Push page content below the now-fixed header */
  .page-content { padding-top: var(--shell-top); }

  /* ── Fixed bottom nav: extend into iPhone home-indicator safe area ────── */
  .bottom-nav {
    padding-bottom: env(safe-area-inset-bottom, 0px);
    height: var(--shell-bot);
  }

  /* Body bottom clearance = nav + home indicator */
  body { padding-bottom: var(--shell-bot); }

  /* Push toasts above the bottom nav + safe area */
  .toast-container { bottom: calc(var(--shell-bot) + 68px); right: 12px; }
  .toast { max-width: calc(100vw - 24px); font-size: 12px; }
}

/* ── Notification bell ───────────────────────────────────────────────────── */
.bell-wrap { position: relative; margin-right: 8px; }

.bell-btn {
  background: none; border: none; cursor: pointer; font-size: 18px;
  position: relative; width: 32px; height: 32px; display: flex;
  align-items: center; justify-content: center; border-radius: 50%;
  transition: background var(--transition);
}
.bell-btn:hover { background: var(--bg-elevated); }

.bell-badge {
  position: absolute; top: 1px; right: 1px;
  background: var(--red); color: #fff;
  font-size: 9px; font-weight: 700; line-height: 1;
  min-width: 16px; height: 16px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  padding: 0 3px; pointer-events: none;
}

.notif-dropdown {
  position: absolute; top: calc(100% + 8px); right: 0;
  background: var(--bg-overlay); border: 1px solid var(--border);
  border-radius: var(--r-lg); box-shadow: var(--shadow-lg);
  width: 300px; max-height: 400px; overflow-y: auto;
  z-index: 9999;
}

.notif-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 14px 8px; border-bottom: 1px solid var(--border);
  font-size: 12px; font-weight: 600; color: var(--text-secondary);
  text-transform: uppercase; letter-spacing: .04em;
}
.notif-read-all {
  background: none; border: none; color: var(--brand); font-size: 11px;
  font-weight: 600; cursor: pointer; font-family: var(--font); padding: 0;
}
.notif-read-all:hover { text-decoration: underline; }

.notif-empty {
  padding: 24px 14px; text-align: center;
  color: var(--text-muted); font-size: 13px;
}

.notif-item {
  display: flex; flex-direction: column; gap: 2px;
  padding: 10px 14px; cursor: pointer;
  border-bottom: 1px solid var(--border);
  transition: background var(--transition);
}
.notif-item:last-child { border-bottom: none; }
.notif-item:hover { background: var(--bg-elevated); }
.notif-item.unread { background: var(--brand-muted); }
.notif-item.unread:hover { background: rgba(66,184,131,.15); }

.notif-text { font-size: 13px; color: var(--text-primary); line-height: 1.4; }
.notif-time { font-size: 11px; color: var(--text-muted); }

@media (max-width: 640px) {
  /* Dropdown starts at right edge of bell, extends leftward across full viewport */
  .notif-dropdown { width: calc(100vw - 24px); right: -60px; }
  .bell-wrap { margin-right: 4px; }
}

/* ── Language switcher (in avatar dropdown) ──────────────────────────────── */
.avatar-menu-lang {
  display: flex;
  gap: 4px;
  padding: 7px 10px;
}

.avatar-lang-btn {
  flex: 1;
  background: transparent;
  border: 1px solid var(--border);
  color: var(--text-muted);
  font-size: 11px;
  font-weight: 600;
  padding: 4px 0;
  border-radius: var(--r);
  cursor: pointer;
  transition: background var(--transition), color var(--transition), border-color var(--transition);
  font-family: var(--font);
}
.avatar-lang-btn:hover { color: var(--text-primary); background: var(--bg-elevated); }
.avatar-lang-btn.active {
  background: var(--brand-muted);
  border-color: rgba(66,184,131,.4);
  color: var(--brand);
}

/* ── Theme switcher (in avatar dropdown) ─────────────────────────────────── */
.avatar-menu-theme {
  display: flex;
  gap: 4px;
  padding: 4px 10px 7px;
}

.avatar-theme-btn {
  flex: 1;
  background: transparent;
  border: 1px solid var(--border);
  color: var(--text-muted);
  font-size: 11px;
  font-weight: 600;
  padding: 4px 0;
  border-radius: var(--r);
  cursor: pointer;
  transition: background var(--transition), color var(--transition), border-color var(--transition);
  font-family: var(--font);
}
.avatar-theme-btn:hover { color: var(--text-primary); background: var(--bg-elevated); }
.avatar-theme-btn.active {
  background: var(--brand-muted);
  border-color: rgba(66,184,131,.4);
  color: var(--brand);
}
</style>
