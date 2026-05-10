import './assets/global.css'
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'
import i18n from './i18n'

// Global 401 handler — clear local auth state and redirect via router (no hard reload).
let _401handling = false
axios.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401 && localStorage.getItem('isLoggedIn') === 'true' && !_401handling) {
      _401handling = true
      localStorage.removeItem('isLoggedIn')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      router.push('/login').finally(() => { _401handling = false })
    }
    return Promise.reject(err)
  }
)

const app = createApp(App).use(router).use(i18n)

// Verify session against backend before mounting — catches expired sessions
// (e.g. after backend restart) before the user navigates to a protected page.
axios.get('/api/auth/me', { withCredentials: true })
  .then(res => {
    // Session is alive — make sure localStorage reflects it
    localStorage.setItem('isLoggedIn', 'true')
    localStorage.setItem('username', res.data.username)
    localStorage.setItem('role', res.data.role)
  })
  .catch(() => {
    // Session gone — clear stale localStorage silently
    localStorage.removeItem('isLoggedIn')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
  })
  .finally(() => app.mount('#app'))

// Register service worker for PWA support
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').catch(() => {/* non-fatal */})
  })
}
