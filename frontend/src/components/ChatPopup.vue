<template>
  <div class="chat-popup">
    <button class="chat-toggle-btn" @click="togglePanel">
      💬
      <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }}</span>
    </button>

    <div v-if="panelOpen" class="chat-panel">
      <div class="panel-header">
        <span v-if="!activeFriend">{{ $t('chat.title') }}</span>
        <span v-else>
          <button class="back-btn" @click="activeFriend = null">←</button>
          {{ activeFriend.username }}
        </span>
        <button class="close-btn" @click="panelOpen = false">✕</button>
      </div>

      <!-- Friends list view -->
      <div v-if="!activeFriend" class="friends-list">
        <div v-if="friends.length === 0" class="empty">{{ $t('chat.noFriends') }}</div>
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
            :placeholder="$t('chat.messagePlaceholder')"
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
  emits: ['notification'],
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
      this.stompClient = markRaw(new Client({
        brokerURL: 'ws://localhost:8080/ws',
        reconnectDelay: 5000,
        connectHeaders: {
          login: localStorage.getItem('username') || ''
        },
        onConnect: () => {
          this.stompClient.subscribe(`/topic/chat/${this.currentUser}`, (frame) => {
            const msg = JSON.parse(frame.body)
            const isCurrentChat = this.activeFriend && msg.senderUsername === this.activeFriend.username
            if (isCurrentChat && this.panelOpen) {
              this.messages.push(msg)
              this.$nextTick(() => this.scrollToBottom())
            } else {
              this.unreadCount++
            }
          })
          this.stompClient.subscribe(`/topic/notifications/${this.currentUser}`, (frame) => {
            const notif = JSON.parse(frame.body)
            this.$emit('notification', notif)
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
      this.messages.push({
        senderUsername: this.currentUser,
        receiverUsername: this.activeFriend.username,
        content,
        sentAt: new Date().toISOString()
      })
      this.stompClient.publish({
        destination: '/app/chat.send',
        body: JSON.stringify({
          senderUsername:   this.currentUser,
          receiverUsername: this.activeFriend.username,
          content,
        })
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
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--brand);
  color: #fff;
  border: none;
  font-size: 20px;
  cursor: pointer;
  box-shadow: var(--shadow-lg);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background var(--transition);
}
.chat-toggle-btn:hover { background: var(--brand-hover); }

.unread-badge {
  position: absolute;
  top: -3px;
  right: -3px;
  background: var(--red);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  border-radius: 50%;
  width: 17px;
  height: 17px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-panel {
  position: absolute;
  bottom: 60px;
  right: 0;
  width: 300px;
  height: 420px;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-lg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 11px 14px;
  background: var(--bg-elevated);
  border-bottom: 1px solid var(--border);
  color: var(--text-primary);
  font-weight: 600;
  font-size: 14px;
}

.close-btn {
  background: transparent;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
  transition: color var(--transition);
}
.close-btn:hover { color: var(--text-primary); }

.back-btn {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 16px;
  margin-right: 6px;
  line-height: 1;
  transition: color var(--transition);
}
.back-btn:hover { color: var(--text-primary); }

.friends-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.friend-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 10px;
  border-radius: var(--r);
  cursor: pointer;
  transition: background var(--transition);
}
.friend-row:hover { background: var(--bg-elevated); }

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--brand-muted);
  border: 1px solid rgba(66,184,131,.3);
  color: var(--brand);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 13px;
  flex-shrink: 0;
}

.fname { font-size: 13px; color: var(--text-primary); font-weight: 500; }

.empty {
  text-align: center;
  color: var(--text-muted);
  padding: 40px 0;
  font-size: 13px;
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

.message { display: flex; flex-direction: column; }
.message.mine   { align-items: flex-end; }
.message.theirs { align-items: flex-start; }

.bubble {
  max-width: 200px;
  padding: 7px 12px;
  border-radius: 14px;
  font-size: 13px;
  line-height: 1.4;
  word-break: break-word;
}
.message.mine .bubble   { background: var(--brand); color: #fff; border-bottom-right-radius: 4px; }
.message.theirs .bubble { background: var(--bg-elevated); color: var(--text-primary); border-bottom-left-radius: 4px; }

.time { font-size: 10px; color: var(--text-muted); margin-top: 2px; padding: 0 4px; }

.input-row {
  display: flex;
  gap: 8px;
  padding: 10px;
  border-top: 1px solid var(--border);
}

.msg-input {
  flex: 1;
  padding: 7px 12px;
  background: var(--bg-elevated);
  color: var(--text-primary);
  border: 1px solid var(--border);
  border-radius: var(--r-full);
  font-size: 13px;
  font-family: var(--font);
  outline: none;
  transition: border-color var(--transition);
}
.msg-input::placeholder { color: var(--text-muted); }
.msg-input:focus { border-color: var(--brand); }

.send-btn {
  background: var(--brand);
  color: #fff;
  border: none;
  border-radius: 50%;
  width: 34px;
  height: 34px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background var(--transition);
  flex-shrink: 0;
}
.send-btn:hover { background: var(--brand-hover); }
</style>
