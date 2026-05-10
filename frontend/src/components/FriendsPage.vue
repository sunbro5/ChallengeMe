<template>
  <div class="friends-page">
    <h2 class="page-title">{{ $t('friends.friends') }}</h2>

    <div class="tabs">
      <button :class="{ active: tab === 'friends' }" @click="tab = 'friends'">{{ $t('friends.friends') }}</button>
      <button :class="{ active: tab === 'requests' }" @click="tab = 'requests'; loadRequests()">
        {{ $t('friends.requests') }}
        <span v-if="requests.length" class="badge">{{ requests.length }}</span>
      </button>
      <button :class="{ active: tab === 'search' }" @click="tab = 'search'">{{ $t('friends.findPeople') }}</button>
      <button :class="{ active: tab === 'activity' }" @click="tab = 'activity'; loadActivity()">{{ $t('friends.activity') }}</button>
    </div>

    <!-- Friends tab -->
    <div v-if="tab === 'friends'" class="tab-content">
      <div v-if="friends.length === 0" class="empty-state">{{ $t('friends.noFriends') }}</div>
      <div v-for="friend in friends" :key="friend.id" class="friend-item">
        <div class="friend-avatar">{{ friend.username[0].toUpperCase() }}</div>
        <span class="friend-name">{{ friend.username }}</span>
        <div class="friend-actions">
          <router-link :to="`/player/${friend.username}`" class="profile-link">
            {{ $t('friends.viewProfile') }}
          </router-link>
          <button class="challenge-btn" @click="openChallenge(friend.username)">
            {{ $t('player.challenge') }}
          </button>
          <button class="unfriend-btn" @click="unfriend(friend)">{{ $t('friends.unfriend') }}</button>
        </div>
      </div>
    </div>

    <!-- Requests tab -->
    <div v-if="tab === 'requests'" class="tab-content">
      <div v-if="requests.length === 0" class="empty-state">{{ $t('friends.noRequests') }}</div>
      <div v-for="req in requests" :key="req.id" class="request-item">
        <div class="friend-avatar">{{ req.requesterUsername[0].toUpperCase() }}</div>
        <span class="friend-name">{{ req.requesterUsername }}</span>
        <div class="request-actions">
          <button class="accept-btn" @click="acceptRequest(req.id)">{{ $t('friends.accept') }}</button>
          <button class="decline-btn" @click="declineRequest(req.id)">{{ $t('friends.decline') }}</button>
        </div>
      </div>
    </div>

    <!-- Search / Find People tab -->
    <div v-if="tab === 'search'" class="tab-content">
      <div class="search-form">
        <input
          v-model="searchQuery"
          type="text"
          :placeholder="$t('friends.searchPlaceholder')"
          @input="onSearchInput"
          class="search-input"
        />
      </div>

      <div v-if="searchQuery.length > 0 && searchQuery.length < 2" class="hint">
        {{ $t('friends.typeToSearch') }}
      </div>
      <div v-else-if="searching" class="hint">{{ $t('friends.searching') }}</div>
      <div v-else-if="searchResults.length === 0 && searchQuery.length >= 2" class="empty-state">
        {{ $t('friends.noUsers') }}
      </div>

      <div v-for="result in searchResults" :key="result.id" class="search-result-item">
        <div class="friend-avatar">{{ result.username[0].toUpperCase() }}</div>
        <span class="friend-name">{{ result.username }}</span>
        <div class="result-actions">
          <router-link :to="`/player/${result.username}`" class="profile-link">
            {{ $t('friends.viewProfile') }}
          </router-link>
        </div>
      </div>
    </div>

    <!-- Activity tab -->
    <div v-if="tab === 'activity'" class="tab-content">
      <div v-if="activityLoading" class="hint">{{ $t('common.loading') }}</div>
      <div v-else-if="activity.length === 0" class="empty-state">{{ $t('friends.noActivity') }}</div>
      <div v-else class="activity-list">
        <router-link
          v-for="item in activity"
          :key="item.eventId"
          :to="`/event/${item.eventId}`"
          class="activity-item"
        >
          <span class="act-friend">{{ item.friendUsername }}</span>
          <span class="act-verb" :class="item.outcome">
            {{ item.outcome === 'won' ? $t('friends.activityWon')
             : item.outcome === 'lost' ? $t('friends.activityLost')
             : $t('friends.activityDrew') }}
          </span>
          <span class="act-opponent">{{ item.opponent }}</span>
          <span class="act-in">{{ $t('friends.activityIn') }}</span>
          <span class="act-game">{{ gameLabel(item.gameType) }}</span>
          <span class="act-date">{{ formatDate(item.playedAt) }}</span>
        </router-link>
      </div>
    </div>

    <!-- Challenge modal -->
    <ChallengeModal
      v-if="showChallengeModal"
      :targetUsername="challengeTarget"
      @close="showChallengeModal = false"
    />

    <!-- Report modal -->
    <div v-if="reportModal.open" class="modal-overlay" @click.self="closeReport">
      <div class="modal">
        <h3>{{ $t('friends.report', { username: reportModal.username }) }}</h3>
        <p class="modal-subtitle">{{ $t('friends.reportSubtitle') }}</p>
        <textarea
          v-model="reportModal.reason"
          :placeholder="$t('friends.reasonPlaceholder')"
          class="reason-textarea"
          rows="4"
        ></textarea>
        <p v-if="reportModal.message" :class="reportModal.success ? 'success-msg' : 'error-msg'">
          {{ reportModal.message }}
        </p>
        <div class="modal-actions">
          <button class="cancel-btn" @click="closeReport">{{ $t('common.cancel') }}</button>
          <button class="submit-report-btn" @click="submitReport">{{ $t('friends.submitReport') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import ChallengeModal from './ChallengeModal.vue'

export default {
  name: 'FriendsPage',
  components: { ChallengeModal },
  data() {
    return {
      tab: 'friends',
      friends: [],
      requests: [],
      searchQuery: '',
      searchResults: [],
      searching: false,
      searchTimer: null,
      reportModal: { open: false, username: '', reason: '', message: '', success: false },
      activity: [],
      activityLoading: false,
      gameMeta: {},
      showChallengeModal: false,
      challengeTarget: '',
    }
  },
  mounted() {
    this.loadFriends()
    this.loadRequests()
    this.loadGameMeta()
  },
  methods: {
    async loadFriends() {
      try {
        const res = await axios.get('/api/friends', { withCredentials: true })
        this.friends = res.data
      } catch (e) {
        console.error('Failed to load friends', e)
      }
    },
    async loadRequests() {
      try {
        const res = await axios.get('/api/friends/requests', { withCredentials: true })
        this.requests = res.data
      } catch (e) {
        console.error('Failed to load requests', e)
      }
    },

    async loadGameMeta() {
      try {
        const { data } = await axios.get('/api/games', { withCredentials: true })
        const meta = {}
        const locale = this.$i18n?.locale || 'en'
        data.forEach(g => {
          meta[g.key] = {
            icon: g.icon,
            label: (locale === 'cs' ? g.nameCs : g.nameEn) || g.nameEn || g.key,
          }
        })
        this.gameMeta = meta
      } catch { /* non-fatal */ }
    },

    async loadActivity() {
      this.activityLoading = true
      try {
        const { data } = await axios.get('/api/friends/activity', { withCredentials: true })
        this.activity = data
      } catch (e) {
        console.error('Failed to load activity', e)
      } finally {
        this.activityLoading = false
      }
    },

    gameLabel(key) {
      const m = this.gameMeta[key]
      return m ? `${m.icon} ${m.label}` : key
    },
    formatDate(iso) {
      return iso ? new Date(iso).toLocaleDateString() : '—'
    },

    // Search
    onSearchInput() {
      clearTimeout(this.searchTimer)
      if (this.searchQuery.length < 2) { this.searchResults = []; return }
      this.searching = true
      this.searchTimer = setTimeout(() => this.runSearch(), 350)
    },
    async runSearch() {
      try {
        const res = await axios.get(
          `/api/friends/search?q=${encodeURIComponent(this.searchQuery)}`,
          { withCredentials: true }
        )
        this.searchResults = res.data
      } catch (e) {
        console.error('Search failed', e)
      } finally {
        this.searching = false
      }
    },

    // Unfriend
    async unfriend(friend) {
      if (!confirm(this.$t('friends.unfriendConfirm', { username: friend.username }))) return
      try {
        await axios.delete(`/api/friends/with/${friend.id}`, { withCredentials: true })
        this.friends = this.friends.filter(f => f.id !== friend.id)
      } catch (e) {
        console.error('Failed to unfriend', e)
      }
    },

    // Accept request
    async acceptRequest(friendshipId) {
      try {
        await axios.post(`/api/friends/accept/${friendshipId}`, {}, { withCredentials: true })
        await this.loadRequests()
        await this.loadFriends()
      } catch (e) {
        console.error('Failed to accept request', e)
      }
    },

    // Decline request
    async declineRequest(friendshipId) {
      try {
        await axios.delete(`/api/friends/requests/${friendshipId}`, { withCredentials: true })
        this.requests = this.requests.filter(r => r.id !== friendshipId)
      } catch (e) {
        console.error('Failed to decline request', e)
      }
    },

    // Challenge
    openChallenge(username) {
      this.challengeTarget = username
      this.showChallengeModal = true
    },

    // Report modal
    openReport(username) {
      this.reportModal = { open: true, username, reason: '', message: '', success: false }
    },
    closeReport() {
      this.reportModal.open = false
    },
    async submitReport() {
      if (!this.reportModal.reason.trim()) {
        this.reportModal.message = this.$t('friends.enterReason')
        this.reportModal.success = false
        return
      }
      try {
        await axios.post('/api/reports', {
          reportedUsername: this.reportModal.username,
          reason: this.reportModal.reason
        }, { withCredentials: true })
        this.reportModal.message = this.$t('friends.reportSuccess')
        this.reportModal.success = true
        this.reportModal.reason = ''
        setTimeout(() => this.closeReport(), 1500)
      } catch (e) {
        this.reportModal.message = e.response?.data || this.$t('friends.reportFailed')
        this.reportModal.success = false
      }
    }
  }
}
</script>

<style scoped>
.friends-page {
  max-width: 960px;
  min-width: min(600px, 100%);
  margin: 0 auto;
  padding: 28px 20px 64px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 20px;
  letter-spacing: -.01em;
}

.tabs {
  display: flex;
  gap: 2px;
  border-bottom: 1px solid var(--border);
  margin-bottom: 20px;
}

.tabs button {
  padding: 8px 16px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  font-family: var(--font);
  transition: color var(--transition);
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
}
.tabs button:hover { color: var(--text-primary); }
.tabs button.active {
  color: var(--brand);
  border-bottom-color: var(--brand);
  font-weight: 600;
}

.badge {
  background: var(--red);
  color: #fff;
  border-radius: var(--r-full);
  font-size: 10px;
  font-weight: 700;
  padding: 1px 6px;
  margin-left: 5px;
  vertical-align: middle;
}

.tab-content {
  min-height: 120px;
}

.friend-item,
.request-item,
.search-result-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: var(--r-md);
  margin-bottom: 8px;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  transition: background var(--transition);
}
.friend-item:hover,
.request-item:hover,
.search-result-item:hover { background: var(--bg-elevated); }

.friend-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: var(--brand-muted);
  border: 1px solid rgba(66,184,131,.3);
  color: var(--brand);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
}

.friend-name {
  font-size: 14px;
  font-weight: 500;
  flex: 1;
  color: var(--text-primary);
}

.request-actions { display: flex; gap: 6px; }

.accept-btn {
  background: var(--brand);
  color: #fff;
  border: none;
  padding: 5px 13px;
  border-radius: var(--r);
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  font-family: var(--font);
  transition: background var(--transition);
}
.accept-btn:hover { background: var(--brand-hover); }

.decline-btn {
  background: transparent;
  color: var(--text-muted);
  border: 1px solid var(--border);
  padding: 4px 11px;
  border-radius: var(--r);
  cursor: pointer;
  font-size: 12px;
  font-family: var(--font);
  transition: color var(--transition), border-color var(--transition);
}
.decline-btn:hover { color: var(--red); border-color: var(--red); }

.friend-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.challenge-btn {
  background: var(--brand);
  color: #fff;
  border: none;
  padding: 5px 13px;
  border-radius: var(--r);
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  font-family: var(--font);
  transition: background var(--transition);
}
.challenge-btn:hover { background: var(--brand-hover); }

.unfriend-btn {
  background: transparent;
  color: var(--text-muted);
  border: 1px solid var(--border);
  padding: 4px 11px;
  border-radius: var(--r);
  cursor: pointer;
  font-size: 12px;
  font-family: var(--font);
  transition: color var(--transition), border-color var(--transition);
}
.unfriend-btn:hover {
  color: var(--red);
  border-color: var(--red);
}

.search-form { margin-bottom: 12px; }

.search-input {
  width: 100%;
  background: var(--bg-elevated);
  color: var(--text-primary);
  border: 1px solid var(--border);
  border-radius: var(--r);
  padding: 9px 12px;
  font-size: 14px;
  font-family: var(--font);
  outline: none;
  box-sizing: border-box;
  transition: border-color var(--transition), box-shadow var(--transition);
}
.search-input::placeholder { color: var(--text-muted); }
.search-input:focus {
  border-color: var(--brand);
  box-shadow: 0 0 0 3px rgba(66,184,131,.15);
}

.result-actions { display: flex; align-items: center; gap: 8px; }

.profile-link {
  background: var(--brand);
  color: #fff;
  text-decoration: none;
  padding: 5px 13px;
  border-radius: var(--r);
  font-size: 12px;
  font-weight: 600;
  transition: background var(--transition);
}
.profile-link:hover { background: var(--brand-hover); }

.activity-list { display: flex; flex-direction: column; gap: 6px; }
.activity-item {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 5px;
  padding: 10px 14px;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  font-size: 13px;
  color: var(--text-primary);
  text-decoration: none;
  transition: background var(--transition);
}
.activity-item:hover { background: var(--bg-elevated); }
.act-friend   { font-weight: 700; color: var(--brand); }
.act-verb     { font-weight: 600; }
.act-verb.won  { color: var(--brand); }
.act-verb.lost { color: var(--red); }
.act-verb.draw { color: var(--yellow); }
.act-opponent { font-weight: 600; color: var(--text-secondary); }
.act-in  { color: var(--text-muted); }
.act-game { color: var(--text-secondary); }
.act-date { margin-left: auto; font-size: 11px; color: var(--text-muted); }

.hint { color: var(--text-muted); font-size: 13px; padding: 8px 0; }
.empty-state { text-align: center; color: var(--text-muted); padding: 40px 0; font-size: 13px; }

.success-msg { color: var(--brand); margin-top: 10px; font-size: 13px; }
.error-msg   { color: var(--red);   margin-top: 10px; font-size: 13px; }

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}
.modal {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--r-xl);
  padding: 26px 28px;
  width: 400px;
  max-width: 92vw;
  box-shadow: var(--shadow-lg);
}
.modal h3 { margin: 0 0 6px; color: var(--text-primary); font-size: 16px; font-weight: 700; }
.modal-subtitle { color: var(--text-secondary); font-size: 13px; margin-bottom: 14px; }

.reason-textarea {
  width: 100%;
  background: var(--bg-elevated);
  color: var(--text-primary);
  border: 1px solid var(--border);
  border-radius: var(--r);
  padding: 9px 12px;
  font-size: 14px;
  resize: vertical;
  font-family: var(--font);
  box-sizing: border-box;
  outline: none;
  transition: border-color var(--transition);
}
.reason-textarea::placeholder { color: var(--text-muted); }
.reason-textarea:focus { border-color: var(--brand); }

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 14px;
}

.cancel-btn {
  background: var(--bg-elevated);
  color: var(--text-primary);
  border: 1px solid var(--border);
  padding: 7px 16px;
  border-radius: var(--r);
  cursor: pointer;
  font-size: 13px;
  font-family: var(--font);
  transition: background var(--transition);
}
.cancel-btn:hover { background: var(--bg-overlay); }

.submit-report-btn {
  background: var(--red);
  color: #fff;
  border: none;
  padding: 7px 16px;
  border-radius: var(--r);
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  font-family: var(--font);
  transition: filter var(--transition);
}
.submit-report-btn:hover { filter: brightness(1.1); }

@media (max-width: 640px) {
  .friends-page { padding: 20px 14px 76px; }
  .tabs button  { padding: 8px 10px; font-size: 12px; }
  .modal        { width: calc(100vw - 32px); }
}
</style>
