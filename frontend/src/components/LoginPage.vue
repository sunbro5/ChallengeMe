<template>
  <div class="auth-form">
    <h2>{{ $t('login.title') }}</h2>
    <form @submit.prevent="handleLogin">
      <input v-model="form.username" :placeholder="$t('login.username')" required>
      <input v-model="form.password" type="password" :placeholder="$t('login.password')" required>
      <button type="submit">{{ $t('login.loginBtn') }}</button>
    </form>
    <p v-if="message" class="message" :class="{ success: isSuccess, error: !isSuccess }">{{ message }}</p>
    <p class="toggle-text">
      {{ $t('login.noAccount') }}
      <router-link to="/register" class="link">{{ $t('login.registerHere') }}</router-link>
    </p>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'LoginPage',
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      message: '',
      isSuccess: false
    }
  },
  methods: {
    async handleLogin() {
      try {
        const response = await axios.post('http://localhost:8080/auth/login', this.form, { withCredentials: true })
        this.message = this.$t('login.success')
        this.isSuccess = true
        localStorage.setItem('isLoggedIn', 'true')
        localStorage.setItem('username', response.data.username)
        localStorage.setItem('role', response.data.role)
        setTimeout(() => {
          this.$router.push('/map')
        }, 800)
        this.form = { username: '', password: '' }
      } catch (error) {
        this.message = error.response?.data || this.$t('login.failed')
        this.isSuccess = false
      }
    }
  }
}
</script>

<style scoped>
.auth-form {
  width: 100%;
  max-width: 360px;
  display: flex;
  flex-direction: column;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--r-xl);
  padding: 32px 28px;
  box-shadow: var(--shadow);
}

h2 {
  color: var(--text-primary);
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 24px;
  text-align: center;
  letter-spacing: -.01em;
}

form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

input {
  background: var(--bg-elevated);
  color: var(--text-primary);
  border: 1px solid var(--border);
  border-radius: var(--r);
  padding: 9px 12px;
  font-size: 14px;
  font-family: var(--font);
  outline: none;
  transition: border-color var(--transition), box-shadow var(--transition);
  width: 100%;
}
input::placeholder { color: var(--text-muted); }
input:focus {
  border-color: var(--brand);
  box-shadow: 0 0 0 3px rgba(66,184,131,.15);
}

button {
  margin-top: 4px;
  padding: 9px;
  background: var(--brand);
  color: #fff;
  border: none;
  border-radius: var(--r);
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  font-family: var(--font);
  transition: background var(--transition);
  width: 100%;
}
button:hover:not(:disabled) { background: var(--brand-hover); }
button:disabled { opacity: .4; cursor: default; }

.message {
  font-size: 13px;
  margin-top: 4px;
  padding: 10px 12px;
  border-radius: var(--r);
  text-align: center;
  line-height: 1.4;
}
.message.error   { color: var(--red);   background: var(--red-muted);   border: 1px solid rgba(248,81,73,.25); }
.message.success { color: var(--brand); background: var(--brand-muted); border: 1px solid rgba(66,184,131,.25); }

.toggle-text {
  color: var(--text-secondary);
  margin-top: 16px;
  font-size: 13px;
  text-align: center;
}
.link { color: var(--brand); text-decoration: none; font-weight: 600; }
.link:hover { text-decoration: underline; }
</style>
