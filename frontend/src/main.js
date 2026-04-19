import './assets/global.css'
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'
import i18n from './i18n'

// Global 401 handler — if the server invalidates the session (e.g. user is banned)
// while the user thinks they are logged in, clear local state and redirect to login.
axios.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401 && localStorage.getItem('isLoggedIn') === 'true') {
      localStorage.removeItem('isLoggedIn')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

createApp(App).use(router).use(i18n).mount('#app')

// Register service worker for PWA support
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').catch(() => {/* non-fatal */})
  })
}
