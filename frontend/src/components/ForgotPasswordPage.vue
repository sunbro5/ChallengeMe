<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2>{{ $t('auth.forgotTitle') }}</h2>
      <p class="subtitle">{{ $t('auth.forgotSubtitle') }}</p>

      <div v-if="sent" class="sent-msg">{{ $t('auth.linkSent') }}</div>

      <template v-else>
        <label>{{ $t('auth.emailLabel') }}</label>
        <input
          v-model="email"
          type="email"
          :placeholder="$t('auth.emailPlaceholder')"
          @keyup.enter="submit"
          class="auth-input"
          autofocus
        />

        <p v-if="error" class="msg-error">{{ error }}</p>

        <button class="btn-primary" :disabled="busy" @click="submit">
          {{ busy ? $t('auth.sending') : $t('auth.sendLink') }}
        </button>
      </template>

      <router-link to="/login" class="back-link">{{ $t('auth.backToLogin') }}</router-link>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ForgotPasswordPage',
  data() {
    return { email: '', busy: false, error: '', sent: false }
  },
  methods: {
    async submit() {
      this.error = ''
      if (!this.email.trim()) return
      this.busy = true
      try {
        await axios.post('/api/auth/forgot-password', { email: this.email.trim() })
        this.sent = true
      } catch (e) {
        this.error = e.response?.data || 'An error occurred.'
      } finally {
        this.busy = false
      }
    },
  },
}
</script>

<style scoped>
.auth-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 20px;
}
.auth-card {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--r-xl);
  padding: 36px 32px;
  width: 360px;
  max-width: 100%;
  box-shadow: var(--shadow-lg);
  display: flex;
  flex-direction: column;
  gap: 14px;
}
h2 { margin: 0; font-size: 20px; font-weight: 700; color: var(--text-primary); }
.subtitle { margin: 0; font-size: 13px; color: var(--text-secondary); line-height: 1.5; }

label { font-size: 12px; color: var(--text-secondary); font-weight: 500; }
.auth-input {
  width: 100%; background: var(--bg-elevated); color: var(--text-primary);
  border: 1px solid var(--border); border-radius: var(--r); padding: 9px 12px;
  font-size: 14px; font-family: var(--font); box-sizing: border-box; outline: none;
  transition: border-color var(--transition);
}
.auth-input:focus { border-color: var(--brand); }
.auth-input::placeholder { color: var(--text-muted); }

.btn-primary {
  background: var(--brand); color: #fff; border: none; border-radius: var(--r);
  padding: 10px 20px; font-size: 14px; font-weight: 600; font-family: var(--font);
  cursor: pointer; width: 100%; transition: background var(--transition);
}
.btn-primary:hover:not(:disabled) { background: var(--brand-hover); }
.btn-primary:disabled { opacity: .5; cursor: default; }

.sent-msg { color: var(--brand); font-size: 14px; text-align: center; padding: 12px 0; }
.msg-error { color: var(--red); font-size: 12px; }

.back-link { font-size: 13px; color: var(--text-muted); text-align: center; text-decoration: none; }
.back-link:hover { color: var(--brand); }
</style>
