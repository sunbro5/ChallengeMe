<template>
  <div class="auth-form">
    <h2>{{ $t('register.title') }}</h2>
    <form @submit.prevent="handleRegister">
      <input v-model="form.username" :placeholder="$t('register.username')" required />
      <input v-model="form.email" type="email" :placeholder="$t('register.email')" required />
      <input v-model="form.password" type="password" :placeholder="$t('register.password')" required />
      <input v-model="form.passwordConfirm" type="password" :placeholder="$t('register.confirmPassword')" required />
      <input
        v-model.number="form.birthYear"
        type="number"
        :placeholder="$t('register.birthYear')"
        min="1900"
        :max="new Date().getFullYear()"
        required
      />

      <!-- Math CAPTCHA -->
      <div class="captcha-row">
        <span class="captcha-question">{{ captchaQuestion }}</span>
        <button type="button" class="btn-refresh" @click="loadCaptcha" :title="$t('register.newQuestion')">🔄</button>
      </div>
      <input
        v-model="form.captchaAnswer"
        :placeholder="$t('register.captchaAnswer')"
        inputmode="numeric"
        autocomplete="off"
        required
      />

      <div class="tos-row">
        <input type="checkbox" id="tos-cb" v-model="form.tosAccepted" />
        <label for="tos-cb" class="tos-label">
          {{ $t('register.tosCheckboxPre') }}<router-link to="/tos" target="_blank" class="tos-link">{{ $t('register.tosLink') }}</router-link>{{ $t('register.tosCheckboxPost') }}
        </label>
      </div>

      <button type="submit" :disabled="submitting">
        {{ submitting ? $t('register.registering') : $t('register.register') }}
      </button>
    </form>

    <p v-if="message" class="message" :class="{ success: isSuccess, error: !isSuccess }">
      {{ message }}
    </p>
    <p class="toggle-text">
      {{ $t('register.alreadyHave') }}
      <router-link to="/login" class="link">{{ $t('register.loginHere') }}</router-link>
    </p>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'RegisterPage',
  data() {
    return {
      form: {
        username:        '',
        email:           '',
        password:        '',
        passwordConfirm: '',
        birthYear:       '',
        captchaId:       '',
        captchaAnswer:   '',
        tosAccepted:     false,
      },
      captchaQuestion: '…',
      message:    '',
      isSuccess:  false,
      submitting: false,
    }
  },
  mounted() {
    this.loadCaptcha()
  },
  methods: {
    async loadCaptcha() {
      this.form.captchaId     = ''
      this.form.captchaAnswer = ''
      this.captchaQuestion    = '…'
      try {
        const { data } = await axios.get('/api/auth/captcha')
        this.form.captchaId  = data.id
        this.captchaQuestion = data.question
      } catch {
        this.captchaQuestion = this.$t('register.couldNotLoad')
      }
    },

    async handleRegister() {
      this.message = ''

      if (this.form.password !== this.form.passwordConfirm) {
        this.message   = this.$t('register.passwordsMismatch')
        this.isSuccess = false
        return
      }
      if (!this.form.tosAccepted) {
        this.message   = this.$t('register.tosRequired')
        this.isSuccess = false
        return
      }
      const age = new Date().getFullYear() - this.form.birthYear
      if (!this.form.birthYear || age < 18) {
        this.$router.push('/')
        return
      }
      if (!this.form.captchaAnswer.trim()) {
        this.message   = this.$t('register.answerCaptcha')
        this.isSuccess = false
        return
      }

      this.submitting = true
      try {
        await axios.post('/api/auth/register', {
          username:      this.form.username,
          email:         this.form.email,
          password:      this.form.password,
          birthYear:     this.form.birthYear,
          captchaId:     this.form.captchaId,
          captchaAnswer: this.form.captchaAnswer,
        })
        this.message   = this.$t('register.success')
        this.isSuccess = true
        setTimeout(() => this.$router.push('/login'), 1500)
      } catch (error) {
        this.message   = error.response?.data || this.$t('register.failed')
        this.isSuccess = false
        await this.loadCaptcha()
      } finally {
        this.submitting = false
      }
    },
  },
}
</script>

<style scoped>
.auth-form {
  width: 100%;
  max-width: 380px;
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
  gap: 10px;
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

.captcha-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 4px;
  padding: 10px 12px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: var(--r);
}

.captcha-question {
  flex: 1;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: .04em;
}

.btn-refresh {
  background: none;
  border: none;
  font-size: 16px;
  cursor: pointer;
  padding: 0;
  line-height: 1;
  color: var(--brand);
  opacity: .8;
  transition: opacity var(--transition);
}
.btn-refresh:hover { opacity: 1; }

button[type="submit"] {
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
button[type="submit"]:hover:not(:disabled) { background: var(--brand-hover); }
button[type="submit"]:disabled { opacity: .4; cursor: default; }

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

.tos-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin: 2px 0 4px;
}
.tos-row input[type="checkbox"] {
  margin-top: 3px;
  accent-color: var(--brand);
  flex-shrink: 0;
  width: 14px;
  height: 14px;
  cursor: pointer;
}
.tos-label {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
  cursor: pointer;
}
.tos-link {
  color: var(--blue);
  text-decoration: underline;
}
.tos-link:hover { color: var(--text-primary); }

.toggle-text {
  color: var(--text-secondary);
  margin-top: 16px;
  font-size: 13px;
  text-align: center;
}
.link { color: var(--brand); text-decoration: none; font-weight: 600; }
.link:hover { text-decoration: underline; }

@media (max-width: 420px) {
  .auth-form { padding: 24px 18px; border-radius: var(--r-lg); }
}
</style>
