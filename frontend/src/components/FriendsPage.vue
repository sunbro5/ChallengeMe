<template>
  <div class="friends-page">
    <div class="friends-container">
      <div class="tabs">
        <button :class="{ active: tab === 'friends' }" @click="tab = 'friends'">Friends</button>
        <button :class="{ active: tab === 'requests' }" @click="tab = 'requests'; loadRequests()">
          Requests
          <span v-if="requests.length" class="badge">{{ requests.length }}</span>
        </button>
        <button :class="{ active: tab === 'add' }" @click="tab = 'add'">Add Friend</button>
      </div>

      <!-- Friends tab -->
      <div v-if="tab === 'friends'" class="tab-content">
        <div v-if="friends.length === 0" class="empty-state">No friends yet. Add some!</div>
        <div v-for="friend in friends" :key="friend.id" class="friend-item">
          <div class="friend-avatar">{{ friend.username[0].toUpperCase() }}</div>
          <span class="friend-name">{{ friend.username }}</span>
          <button class="report-btn" @click="openReport(friend.username)" title="Report user">⚑ Report</button>
        </div>
      </div>

      <!-- Requests tab -->
      <div v-if="tab === 'requests'" class="tab-content">
        <div v-if="requests.length === 0" class="empty-state">No pending requests.</div>
        <div v-for="req in requests" :key="req.id" class="request-item">
          <div class="friend-avatar">{{ req.requesterUsername[0].toUpperCase() }}</div>
          <span class="friend-name">{{ req.requesterUsername }}</span>
          <button class="accept-btn" @click="acceptRequest(req.id)">Accept</button>
        </div>
      </div>

      <!-- Add Friend tab -->
      <div v-if="tab === 'add'" class="tab-content">
        <div class="add-form">
          <input
            v-model="searchUsername"
            type="text"
            placeholder="Enter username..."
            @keyup.enter="sendRequest"
            class="search-input"
          />
          <button @click="sendRequest" class="send-btn">Send Request</button>
        </div>
        <p v-if="addMessage" :class="addSuccess ? 'success-msg' : 'error-msg'">{{ addMessage }}</p>
      </div>
    </div>

    <!-- Report modal -->
    <div v-if="reportModal.open" class="modal-overlay" @click.self="closeReport">
      <div class="modal">
        <h3>Report {{ reportModal.username }}</h3>
        <p class="modal-subtitle">Describe why you're reporting this user:</p>
        <textarea
          v-model="reportModal.reason"
          placeholder="Enter reason..."
          class="reason-textarea"
          rows="4"
        ></textarea>
        <p v-if="reportModal.message" :class="reportModal.success ? 'success-msg' : 'error-msg'">
          {{ reportModal.message }}
        </p>
        <div class="modal-actions">
          <button class="cancel-btn" @click="closeReport">Cancel</button>
          <button class="submit-report-btn" @click="submitReport">Submit Report</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'FriendsPage',
  data() {
    return {
      tab: 'friends',
      friends: [],
      requests: [],
      searchUsername: '',
      addMessage: '',
      addSuccess: false,
      reportModal: {
        open: false,
        username: '',
        reason: '',
        message: '',
        success: false
      }
    }
  },
  mounted() {
    this.loadFriends()
    this.loadRequests()
  },
  methods: {
    async loadFriends() {
      try {
        const res = await axios.get('http://localhost:8080/friends', { withCredentials: true })
        this.friends = res.data
      } catch (e) {
        console.error('Failed to load friends', e)
      }
    },
    async loadRequests() {
      try {
        const res = await axios.get('http://localhost:8080/friends/requests', { withCredentials: true })
        this.requests = res.data
      } catch (e) {
        console.error('Failed to load requests', e)
      }
    },
    async sendRequest() {
      if (!this.searchUsername.trim()) return
      try {
        await axios.post(`http://localhost:8080/friends/request?username=${this.searchUsername}`, {}, { withCredentials: true })
        this.addMessage = 'Friend request sent!'
        this.addSuccess = true
        this.searchUsername = ''
      } catch (e) {
        this.addMessage = e.response?.data || 'Failed to send request'
        this.addSuccess = false
      }
    },
    async acceptRequest(friendshipId) {
      try {
        await axios.post(`http://localhost:8080/friends/accept/${friendshipId}`, {}, { withCredentials: true })
        await this.loadRequests()
        await this.loadFriends()
      } catch (e) {
        console.error('Failed to accept request', e)
      }
    },
    openReport(username) {
      this.reportModal = { open: true, username, reason: '', message: '', success: false }
    },
    closeReport() {
      this.reportModal.open = false
    },
    async submitReport() {
      if (!this.reportModal.reason.trim()) {
        this.reportModal.message = 'Please enter a reason'
        this.reportModal.success = false
        return
      }
      try {
        await axios.post('http://localhost:8080/reports', {
          reportedUsername: this.reportModal.username,
          reason: this.reportModal.reason
        }, { withCredentials: true })
        this.reportModal.message = 'Report submitted successfully'
        this.reportModal.success = true
        this.reportModal.reason = ''
        setTimeout(() => this.closeReport(), 1500)
      } catch (e) {
        this.reportModal.message = e.response?.data || 'Failed to submit report'
        this.reportModal.success = false
      }
    }
  }
}
</script>

<style scoped>
.friends-page {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.friends-container {
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  overflow: hidden;
}

.tabs {
  display: flex;
  background: #f5f5f5;
  border-bottom: 1px solid #e0e0e0;
}

.tabs button {
  flex: 1;
  padding: 12px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  position: relative;
}

.tabs button.active {
  background: white;
  color: #42b883;
  font-weight: 600;
  border-bottom: 2px solid #42b883;
}

.badge {
  background: #e74c3c;
  color: white;
  border-radius: 50%;
  font-size: 11px;
  padding: 2px 6px;
  margin-left: 4px;
}

.tab-content {
  padding: 16px;
  min-height: 200px;
}

.friend-item, .request-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border-radius: 8px;
  margin-bottom: 8px;
  background: #f9f9f9;
}

.friend-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #42b883;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  flex-shrink: 0;
}

.friend-name {
  font-size: 15px;
  flex: 1;
}

.accept-btn {
  background: #42b883;
  color: white;
  border: none;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.accept-btn:hover {
  background: #369f6e;
}

.report-btn {
  background: transparent;
  color: #e74c3c;
  border: 1px solid #e74c3c;
  padding: 5px 10px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  opacity: 0.7;
  transition: opacity 0.15s;
}

.report-btn:hover {
  opacity: 1;
}

.add-form {
  display: flex;
  gap: 10px;
}

.search-input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.send-btn {
  background: #42b883;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 6px;
  cursor: pointer;
}

.send-btn:hover {
  background: #369f6e;
}

.empty-state {
  text-align: center;
  color: #999;
  padding: 40px 0;
}

.success-msg {
  color: #27ae60;
  margin-top: 10px;
  font-size: 14px;
}

.error-msg {
  color: #e74c3c;
  margin-top: 10px;
  font-size: 14px;
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal {
  background: white;
  border-radius: 12px;
  padding: 28px;
  width: 420px;
  max-width: 90vw;
  box-shadow: 0 8px 32px rgba(0,0,0,0.2);
}

.modal h3 {
  margin: 0 0 6px;
  color: #2c3e50;
  font-size: 18px;
}

.modal-subtitle {
  color: #777;
  font-size: 14px;
  margin-bottom: 14px;
}

.reason-textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
  font-family: inherit;
  box-sizing: border-box;
}

.reason-textarea:focus {
  outline: none;
  border-color: #42b883;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

.cancel-btn {
  background: #ecf0f1;
  color: #555;
  border: none;
  padding: 8px 18px;
  border-radius: 7px;
  cursor: pointer;
  font-size: 14px;
}

.cancel-btn:hover {
  background: #dde1e2;
}

.submit-report-btn {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 8px 18px;
  border-radius: 7px;
  cursor: pointer;
  font-size: 14px;
}

.submit-report-btn:hover {
  background: #c0392b;
}
</style>
