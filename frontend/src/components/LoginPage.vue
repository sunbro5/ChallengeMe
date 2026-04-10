<template>
  <div class="auth-form">
    <h2>Login</h2>
    <form @submit.prevent="handleLogin">
      <input v-model="form.username" placeholder="Username" required>
      <input v-model="form.password" type="password" placeholder="Password" required>
      <button type="submit">Login</button>
    </form>
    <p v-if="message" class="message" :class="{ success: isSuccess, error: !isSuccess }">{{ message }}</p>
    <p class="toggle-text">
      Don't have an account?
      <router-link to="/register" class="link">Register here</router-link>
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
        this.message = 'Login successful!'
        this.isSuccess = true
        localStorage.setItem('isLoggedIn', 'true')
        localStorage.setItem('username', response.data.username)
        localStorage.setItem('role', response.data.role)
        setTimeout(() => {
          this.$router.push('/map')
        }, 800)
        this.form = { username: '', password: '' }
      } catch (error) {
        this.message = error.response?.data || 'Login failed'
        this.isSuccess = false
      }
    }
  }
}
</script>

<style scoped>
.auth-form {
  display: flex;
  flex-direction: column;
  align-items: center;
}

h2 {
  color: #2c3e50;
  margin-bottom: 20px;
}

form {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
  width: 100%;
}

input {
  margin: 10px 0;
  padding: 10px;
  width: 80%;
  max-width: 300px;
  border: 1px solid #ccc;
  border-radius: 5px;
  font-size: 16px;
}

button {
  padding: 10px 20px;
  background-color: #42b883;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  margin-top: 10px;
}

button:hover {
  background-color: #369870;
}

.message {
  font-weight: bold;
  margin-top: 10px;
  padding: 12px;
  border-radius: 5px;
  width: 80%;
  max-width: 300px;
  text-align: center;
}

.message.error {
  color: #c0392b;
  background-color: #fadbd8;
  border: 1px solid #e74c3c;
}

.message.success {
  color: #27ae60;
  background-color: #d5f4e6;
  border: 1px solid #2ecc71;
}

.toggle-text {
  color: #2c3e50;
  margin-top: 20px;
}

.link {
  color: #42b883;
  text-decoration: none;
  font-weight: bold;
}

.link:hover {
  text-decoration: underline;
}
</style>

