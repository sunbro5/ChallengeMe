<template>
  <div class="auth-form">
    <h2>Register</h2>
    <form @submit.prevent="handleRegister">
      <input v-model="form.username" placeholder="Username" required>
      <input v-model="form.email" type="email" placeholder="Email" required>
      <input v-model="form.password" type="password" placeholder="Password" required>
      <button type="submit">Register</button>
    </form>
    <p v-if="message" class="message" :class="{ success: isSuccess, error: !isSuccess }">{{ message }}</p>
    <p class="toggle-text">
      Already have an account?
      <router-link to="/login" class="link">Login here</router-link>
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
        username: '',
        email: '',
        password: ''
      },
      message: '',
      isSuccess: false
    }
  },
  methods: {
    async handleRegister() {
      try {
        const response = await axios.post('http://localhost:8080/auth/register', this.form, { withCredentials: false })
        this.message = 'Registration successful! Redirecting to login...'
        this.isSuccess = true

        setTimeout(() => {
          this.form = { username: '', email: '', password: '' }
          this.message = ''
          this.$router.push('/login')
        }, 1500)
      } catch (error) {
        this.message = error.response?.data || 'Registration failed'
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

