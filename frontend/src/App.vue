<template>
  <div id="app">
    <div v-if="isLoggedIn" class="logged-in-container">
      <div class="header">
        <div class="header-left">
          <h1>ChallengeMe</h1>
          <nav class="nav-links">
            <router-link to="/map" class="nav-link">Map</router-link>
            <router-link to="/friends" class="nav-link">Friends</router-link>
            <router-link v-if="isAdmin" to="/admin" class="nav-link admin-link">Admin</router-link>
          </nav>
        </div>
        <div class="header-right">
          <span class="welcome-text">Welcome, {{ username }}!</span>
          <button @click="logout" class="logout-btn">Logout</button>
        </div>
      </div>
      <router-view />
      <ChatPopup />
    </div>
    <div v-else class="auth-container">
      <h1>ChallengeMe</h1>
      <router-view />
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import ChatPopup from './components/ChatPopup.vue'

export default {
  name: 'App',
  components: { ChatPopup },
  data() {
    return {
      isLoggedIn: false,
      isAdmin: false,
      username: ''
    }
  },
  mounted() {
    this.syncAuth()
  },
  watch: {
    '$route'() {
      this.syncAuth()
    }
  },
  methods: {
    syncAuth() {
      this.isLoggedIn = localStorage.getItem('isLoggedIn') === 'true'
      this.isAdmin = localStorage.getItem('role') === 'ADMIN'
      this.username = localStorage.getItem('username') || ''
    },
    logout() {
      localStorage.removeItem('isLoggedIn')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      this.isLoggedIn = false
      this.isAdmin = false
      this.username = ''
      this.$router.push('/login')
      axios.post('http://localhost:8080/auth/logout', {}, { withCredentials: true })
        .catch(error => console.error('Logout API error:', error))
    }
  }
}
</script>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.auth-container {
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.logged-in-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

h1 {
  color: white;
  margin: 0;
  font-size: 20px;
}

.auth-container h1 {
  color: #42b883;
  font-size: 28px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background-color: #42b883;
  color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.nav-links {
  display: flex;
  gap: 4px;
}

.nav-link {
  color: rgba(255,255,255,0.85);
  text-decoration: none;
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 14px;
  transition: background 0.15s;
}

.nav-link:hover {
  background: rgba(255,255,255,0.15);
  color: white;
}

.nav-link.router-link-active {
  background: rgba(255,255,255,0.2);
  color: white;
  font-weight: 600;
}

.admin-link {
  background: rgba(231, 76, 60, 0.3);
}

.admin-link:hover {
  background: rgba(231, 76, 60, 0.5) !important;
}

.admin-link.router-link-active {
  background: rgba(231, 76, 60, 0.5) !important;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.welcome-text {
  font-size: 14px;
}

.logout-btn {
  background-color: #d63031;
  color: white;
  border: none;
  padding: 7px 14px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
}

.logout-btn:hover {
  background-color: #b82e2e;
}
</style>
