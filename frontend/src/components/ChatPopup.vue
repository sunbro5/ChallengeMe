<template>
  <div class="chat-popup">
    <!-- Toggle button -->
    <button class="chat-toggle-btn" @click="togglePanel">
      💬
      <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }}</span>
    </button>

    <!-- Chat panel -->
    <div v-if="panelOpen" class="chat-panel">
      <div class="panel-header">
        <span v-if="!activeFriend">Chats</span>
        <span v-else>
          <button class="back-btn" @click="activeFriend = null">←</button>
          {{ activeFriend.username }}
        </span>
        <button class="close-btn" @click="panelOpen = false">✕</button>
      </div>

      <!-- Friends list view -->
      <div v-if="!activeFriend" class="friends-list">
        <div v-if="friends.length === 0" class="empty">No friends yet.</div>
        <div
          v-for="friend in friends"
          :key="friend.id"
          class="friend-row"
          @click="openChat(friend)"
        >
          <div class="avatar">{{ friend.username[0].toUpperCase() }}</div>
          <span class="fname">{{ friend.username }}</span>
        </div>
      </div>

      <!-- Chat view -->
      <div v-else class="chat-view">
        <div class="messages" ref="messagesContainer">
          <div
            v-for="(msg, i) in messages"
            :key="i"
            :class="['message', msg.senderUsername === currentUser ? 'mine' : 'theirs']"
          >
            <div class="bubble">{{ msg.content }}</div>
            <div class="time">{{ formatTime(msg.sentAt) }}</div>
          </div>
        </div>
        <div class="input-row">
          <input
            v-model="inputText"
            type="text"
            placeholder="Type a message..."
            @keyup.enter="sendMessage"
            class="msg-input"
          />
          <button @click="sendMessage" class="send-btn">➤</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { markRaw } from 'vue'
import { Client } from '@stomp/stompjs'

export default {
  name: 'ChatPopup',
  data() {
    return {
      panelOpen: false,
      friends: [],
      activeFriend: null,
      messages: [],
      inputText: '',
      currentUser: localStorage.getItem('username'),
      unreadCount: 0,
      stompClient: null
    }
  },
  mounted() {
    this.loadFriends()
    this.connectWebSocket()
  },
  beforeUnmount() {
    if (this.stompClient) this.stompClient.deactivate()
  },
  methods: {
    togglePanel() {
      this.panelOpen = !this.panelOpen
      if (this.panelOpen) {
        this.unreadCount = 0
        this.loadFriends()
      }
    },
    async loadFriends() {
      try {
        const res = await axios.get('http://localhost:8080/friends', { withCredentials: true })
        this.friends = res.data
      } catch (e) {
        console.error('Failed to load friends', e)
      }
    },
    async openChat(friend) {
      this.activeFriend = friend
      this.messages = []
      try {
        const res = await axios.get(`http://localhost:8080/chat/history?friendUsername=${friend.username}`, { withCredentials: true })
        this.messages = res.data
        this.$nextTick(() => this.scrollToBottom())
      } catch (e) {
        console.error('Failed to load history', e)
      }
    },
    connectWebSocket() {
      // markRaw prevents Vue from wrapping the Client in a reactive Proxy,
      // which would break @stomp/stompjs internal state and WebSocket callbacks.
      this.stompClient = markRaw(new Client({
        brokerURL: 'ws://localhost:8080/ws',
        reconnectDelay: 5000,
        onConnect: () => {
          this.stompClient.subscribe('/user/queue/messages', (frame) => {
            const msg = JSON.parse(frame.body)
            const isCurrentChat = this.activeFriend && msg.senderUsername === this.activeFriend.username
            if (isCurrentChat && this.panelOpen) {
              this.messages.push(msg)
              this.$nextTick(() => this.scrollToBottom())
            } else {
              this.unreadCount++
            }
          })
        },
        onStompError: (frame) => {
          console.error('WebSocket error', frame)
        }
      }))
      this.stompClient.activate()
    },
    sendMessage() {
      if (!this.inputText.trim() || !this.activeFriend || !this.stompClient?.connected) return
      const content = this.inputText.trim()
      // Optimistic update — sender sees message immediately without waiting for server echo
      this.messages.push({
        senderUsername: this.currentUser,
        receiverUsername: this.activeFriend.username,
        content,
        sentAt: new Date().toISOString()
      })
      this.stompClient.publish({
        destination: '/app/chat.send',
        body: JSON.stringify({ receiverUsername: this.activeFriend.username, content })
      })
      this.inputText = ''
      this.$nextTick(() => this.scrollToBottom())
    },
    scrollToBottom() {
      const el = this.$refs.messagesContainer
      if (el) el.scrollTop = el.scrollHeight
    },
    formatTime(sentAt) {
      if (!sentAt) return ''
      const d = new Date(sentAt)
      return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    }
  }
}
</script>

<style scoped>
.chat-popup {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.chat-toggle-btn {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: #42b883;
  color: white;
  border: none;
  font-size: 22px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-toggle-btn:hover {
  background: #369f6e;
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #e74c3c;
  color: white;
  font-size: 11px;
  border-radius: 50%;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-panel {
  position: absolute;
  bottom: 64px;
  right: 0;
  width: 300px;
  height: 420px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #42b883;
  color: white;
  font-weight: 600;
  font-size: 15px;
}

.close-btn {
  background: transparent;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
}

.back-btn {
  background: transparent;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 18px;
  margin-right: 6px;
  line-height: 1;
}

.friends-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.friend-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
}

.friend-row:hover {
  background: #f0faf5;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #42b883;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}

.fname {
  font-size: 14px;
}

.empty {
  text-align: center;
  color: #999;
  padding: 40px 0;
  font-size: 14px;
}

.chat-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message {
  display: flex;
  flex-direction: column;
}

.message.mine {
  align-items: flex-end;
}

.message.theirs {
  align-items: flex-start;
}

.bubble {
  max-width: 200px;
  padding: 8px 12px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.4;
  word-break: break-word;
}

.message.mine .bubble {
  background: #42b883;
  color: white;
  border-bottom-right-radius: 4px;
}

.message.theirs .bubble {
  background: #f0f0f0;
  color: #333;
  border-bottom-left-radius: 4px;
}

.time {
  font-size: 11px;
  color: #aaa;
  margin-top: 2px;
  padding: 0 4px;
}

.input-row {
  display: flex;
  gap: 8px;
  padding: 10px;
  border-top: 1px solid #eee;
}

.msg-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 14px;
  outline: none;
}

.msg-input:focus {
  border-color: #42b883;
}

.send-btn {
  background: #42b883;
  color: white;
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-btn:hover {
  background: #369f6e;
}
</style>
