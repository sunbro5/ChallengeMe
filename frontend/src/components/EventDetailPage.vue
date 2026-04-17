<template>
  <div class="event-detail-page">
    <div class="back-bar">
      <button class="btn-back" @click="$router.back()">{{ $t('eventDetail.back') }}</button>
    </div>

    <div v-if="loading" class="state-msg">{{ $t('common.loading') }}</div>
    <div v-else-if="error" class="state-msg error">{{ error }}</div>

    <template v-else-if="event">
      <!-- ── Main card ───────────────────────────────────────────────────── -->
      <div class="event-card" :class="event.status.toLowerCase()">
        <div class="card-top">
          <span class="game-label">{{ gameLabel(event.gameType) }}</span>
          <span class="status-badge" :class="event.status.toLowerCase()">{{ statusLabel(event.status) }}</span>
        </div>

        <div class="card-meta">
          <span>📅 {{ formatDate(event.scheduledAt) }}</span>
          <a :href="mapsLink(event)" target="_blank" class="link">{{ $t('eventDetail.openInMaps') }}</a>
        </div>

        <p v-if="event.description" class="event-description">{{ event.description }}</p>

        <div class="card-meta">
          <span>{{ $t('eventDetail.host') }}
            <router-link :to="`/player/${event.creatorUsername}`" class="link">{{ event.creatorUsername }}</router-link>
          </span>
        </div>

        <div class="participants">
          <router-link
            v-for="p in event.participants" :key="p"
            :to="`/player/${p}`"
            class="participant-tag"
          >👤 {{ p }}</router-link>
          <span v-if="event.participants.length === 1" class="participant-tag empty">
            {{ $t('eventDetail.waitingForChallenger') }}
          </span>
        </div>

        <!-- ── Actions ────────────────────────────────────────────────────── -->
        <template v-if="isLoggedIn">

          <button
            v-if="event.status === 'OPEN' && !event.isCreator && !event.joined"
            class="btn-primary"
            :disabled="busy"
            @click="applyChallenge"
          >{{ busy ? $t('eventDetail.applying') : $t('eventDetail.acceptChallenge') }}</button>

          <div v-if="event.status === 'OPEN' && event.isCreator" class="waiting-note">
            {{ $t('eventDetail.waitingApply') }}
          </div>

          <template v-if="event.status === 'PENDING_APPROVAL' && event.isCreator">
            <p class="approval-note">{{ $t('eventDetail.wantsToPlay', { username: event.challengerUsername }) }}</p>
            <div class="action-row">
              <button class="btn-success" :disabled="busy" @click="approve">{{ $t('common.approve') }}</button>
              <button class="btn-danger"  :disabled="busy" @click="reject">{{ $t('common.reject') }}</button>
            </div>
          </template>

          <div v-if="event.status === 'PENDING_APPROVAL' && event.isChallenger" class="waiting-note">
            {{ $t('eventDetail.waitingApproval', { username: event.creatorUsername }) }}
          </div>

          <div v-if="event.status === 'IN_PROGRESS' && event.joined" class="meetup-box">
            <p>{{ $t('eventDetail.headToPin') }}</p>
            <div class="safety-inline">⚠️ {{ $t('safety.meetupWarning') }}</div>
            <div class="submit-status">
              <span :class="event.creatorResultSubmitted ? 'done' : 'pending'">
                {{ event.creatorUsername }}: {{ event.creatorResultSubmitted ? $t('eventDetail.submitted') : $t('eventDetail.notYet') }}
              </span>
              <span :class="event.challengerResultSubmitted ? 'done' : 'pending'">
                {{ event.challengerUsername }}: {{ event.challengerResultSubmitted ? $t('eventDetail.submitted') : $t('eventDetail.notYet') }}
              </span>
            </div>
            <button
              v-if="!event.iHaveSubmitted"
              class="btn-success"
              @click="resultModal.visible = true"
            >{{ $t('eventDetail.submitResult') }}</button>
            <div v-else class="submitted-note">{{ $t('eventDetail.resultIn') }}</div>
          </div>

          <div v-if="event.status === 'FINISHED'" class="result-box">
            <span v-if="event.winnerUsername">{{ $t('eventDetail.won', { username: event.winnerUsername }) }}</span>
            <span v-else>{{ $t('common.draw') }}</span>
            <span v-if="event.resultNote" class="result-note">"{{ event.resultNote }}"</span>
          </div>

          <div v-if="event.status === 'DISPUTED'" class="dispute-box">
            <p class="dispute-title">{{ $t('common.disputedResult') }}</p>
            <p>{{ event.creatorUsername }} {{ $t('eventDetail.said') }}: <strong>{{ event.creatorResult || $t('common.draw') }}</strong></p>
            <p>{{ event.challengerUsername }} {{ $t('eventDetail.said') }}: <strong>{{ event.challengerResult || $t('common.draw') }}</strong></p>
            <p class="dispute-note">{{ $t('common.scoresNotCounted') }}</p>
          </div>

          <button
            v-if="event.isCreator && ['OPEN','PENDING_APPROVAL','IN_PROGRESS'].includes(event.status)"
            class="btn-cancel"
            :disabled="busy"
            @click="cancelEvent"
          >{{ $t('eventDetail.cancelEvent') }}</button>
        </template>

        <p v-if="actionError" class="error-msg">{{ actionError }}</p>
      </div>

      <!-- ── Chat ───────────────────────────────────────────────────────── -->
      <div v-if="otherUser" class="chat-section">
        <h3 class="chat-title">{{ $t('eventDetail.chatWith', { username: otherUser }) }}</h3>

        <div class="chat-messages" ref="messagesEl">
          <div
            v-for="(msg, i) in messages"
            :key="i"
            :class="['msg', msg.senderUsername === currentUser ? 'mine' : 'theirs']"
          >
            <div class="bubble">{{ msg.content }}</div>
            <div class="time">{{ formatTime(msg.sentAt) }}</div>
          </div>
          <div v-if="!messages.length" class="no-messages">{{ $t('eventDetail.noMessages') }}</div>
        </div>

        <div class="chat-input-row">
          <input
            v-model="chatInput"
            type="text"
            :placeholder="$t('eventDetail.messagePlaceholder')"
            class="chat-input"
            @keyup.enter="sendMessage"
          />
          <button class="btn-send" @click="sendMessage">➤</button>
        </div>
      </div>
    </template>

    <!-- ── Result modal ───────────────────────────────────────────────────── -->
    <div v-if="resultModal.visible" class="modal-overlay" @click.self="resultModal.visible = false">
      <div class="modal">
        <h3>{{ $t('eventDetail.yourResult') }}</h3>
        <p class="modal-sub">{{ $t('eventDetail.submitWhoWon') }}</p>

        <label>{{ $t('eventDetail.whoWon') }}</label>
        <div class="radio-group">
          <label v-for="p in event.participants" :key="p" class="radio-opt">
            <input type="radio" :value="p" v-model="resultModal.winner" /> {{ p }}
          </label>
          <label class="radio-opt">
            <input type="radio" value="" v-model="resultModal.winner" /> {{ $t('common.draw') }}
          </label>
        </div>

        <label>{{ $t('eventDetail.note') }} <span class="optional">({{ $t('common.optional') }})</span></label>
        <input type="text" v-model="resultModal.note" :placeholder="$t('eventDetail.notePlaceholder')" class="text-input" />

        <div class="modal-actions">
          <button class="btn-primary" :disabled="resultModal.busy" @click="submitResult">
            {{ resultModal.busy ? $t('common.saving') : $t('common.submit') }}
          </button>
          <button class="btn-ghost" @click="resultModal.visible = false">{{ $t('common.cancel') }}</button>
        </div>
        <p v-if="resultModal.error" class="error-msg">{{ resultModal.error }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'EventDetailPage',
  data() {
    return {
      event:       null,
      loading:     true,
      error:       '',
      busy:        false,
      actionError: '',
      gameMeta:    {},

      messages:   [],
      chatInput:  '',
      pollTimer:  null,

      currentUser: localStorage.getItem('username') || '',
      isLoggedIn:  localStorage.getItem('isLoggedIn') === 'true',

      resultModal: { visible: false, winner: '', note: '', busy: false, error: '' },
    }
  },
  computed: {
    otherUser() {
      if (!this.event) return null
      if (!this.event.challengerUsername) return null
      return this.event.creatorUsername === this.currentUser
        ? this.event.challengerUsername
        : this.event.creatorUsername
    },
  },
  async mounted() {
    await this.loadGames()
    await this.loadEvent()
    if (this.otherUser) {
      await this.loadMessages()
      this.pollTimer = setInterval(this.loadMessages, 4000)
    }
  },
  beforeUnmount() {
    clearInterval(this.pollTimer)
  },
  methods: {
    async loadGames() {
      try {
        const { data } = await axios.get('/api/games', { withCredentials: true })
        data.forEach(g => { this.gameMeta[g.key] = { icon: g.icon, name: g.name } })
      } catch { /* non-fatal */ }
    },

    async loadEvent() {
      this.loading = true
      try {
        const { data } = await axios.get(
          `/api/events/${this.$route.params.id}`,
          { withCredentials: true }
        )
        this.event = data
      } catch (e) {
        this.error = e.response?.status === 404
          ? this.$t('eventDetail.notFound')
          : this.$t('eventDetail.couldNotLoad')
      } finally {
        this.loading = false
      }
    },

    async loadMessages() {
      if (!this.otherUser) return
      try {
        const { data } = await axios.get(
          `/api/chat/history?friendUsername=${this.otherUser}`,
          { withCredentials: true }
        )
        this.messages = data
        this.$nextTick(this.scrollToBottom)
      } catch { /* non-fatal */ }
    },

    async sendMessage() {
      const content = this.chatInput.trim()
      if (!content || !this.otherUser) return
      this.chatInput = ''
      this.messages.push({
        senderUsername: this.currentUser,
        receiverUsername: this.otherUser,
        content,
        sentAt: new Date().toISOString(),
      })
      this.$nextTick(this.scrollToBottom)
      try {
        await axios.post('/api/chat/send',
          { receiverUsername: this.otherUser, content },
          { withCredentials: true }
        )
      } catch { /* message already shown optimistically */ }
    },

    scrollToBottom() {
      const el = this.$refs.messagesEl
      if (el) el.scrollTop = el.scrollHeight
    },

    async applyChallenge() {
      this.busy = true; this.actionError = ''
      try {
        const { data } = await axios.post(
          `/api/events/${this.event.id}/accept`, {},
          { withCredentials: true }
        )
        this.event = data
      } catch (e) {
        this.actionError = e.response?.data || this.$t('eventDetail.couldNotApply')
      } finally { this.busy = false }
    },

    async approve() {
      this.busy = true; this.actionError = ''
      try {
        const { data } = await axios.post(
          `/api/events/${this.event.id}/approve`, {},
          { withCredentials: true }
        )
        this.event = data
        if (this.otherUser && !this.pollTimer) {
          await this.loadMessages()
          this.pollTimer = setInterval(this.loadMessages, 4000)
        }
      } catch (e) {
        this.actionError = e.response?.data || this.$t('eventDetail.couldNotApprove')
      } finally { this.busy = false }
    },

    async reject() {
      this.busy = true; this.actionError = ''
      try {
        const { data } = await axios.post(
          `/api/events/${this.event.id}/reject`, {},
          { withCredentials: true }
        )
        this.event = data
      } catch (e) {
        this.actionError = e.response?.data || this.$t('eventDetail.couldNotReject')
      } finally { this.busy = false }
    },

    async cancelEvent() {
      if (!confirm(this.$t('eventDetail.cancelConfirm'))) return
      this.busy = true; this.actionError = ''
      try {
        await axios.delete(
          `/api/events/${this.event.id}`,
          { withCredentials: true }
        )
        this.event = { ...this.event, status: 'CANCELLED' }
      } catch (e) {
        this.actionError = e.response?.data || this.$t('eventDetail.couldNotCancel')
      } finally { this.busy = false }
    },

    async submitResult() {
      this.resultModal.busy = true; this.resultModal.error = ''
      try {
        const { data } = await axios.post(
          `/api/events/${this.event.id}/result`,
          { winnerUsername: this.resultModal.winner || null, resultNote: this.resultModal.note },
          { withCredentials: true }
        )
        this.event = data
        this.resultModal.visible = false
      } catch (e) {
        this.resultModal.error = e.response?.data || this.$t('eventDetail.couldNotSaveResult')
      } finally { this.resultModal.busy = false }
    },

    gameLabel(type) {
      const m = this.gameMeta[type]
      return m ? `${m.icon} ${m.name}` : (type || '')
    },
    statusLabel(s) { return this.$t('status.' + s.toLowerCase()) },
    formatDate(iso) { return iso ? new Date(iso).toLocaleString() : '—' },
    formatTime(iso) {
      if (!iso) return ''
      return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    },
    mapsLink(ev) { return `https://www.google.com/maps?q=${ev.latitude},${ev.longitude}` },
  },
}
</script>

<style scoped>
.event-detail-page {
  max-width: 680px;
  margin: 0 auto;
  padding: 24px 20px 60px;
}

.back-bar { margin-bottom: 20px; }
.btn-back {
  background: var(--bg-elevated); border: 1px solid var(--border); border-radius: var(--r);
  padding: 6px 14px; font-size: 12px; cursor: pointer; color: var(--text-secondary);
  font-family: var(--font); transition: background var(--transition);
}
.btn-back:hover { background: var(--bg-overlay); color: var(--text-primary); }

.state-msg       { text-align: center; padding: 60px; color: var(--text-muted); font-size: 14px; }
.state-msg.error { color: var(--red); }

.event-card {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-left: 4px solid var(--border);
  border-radius: var(--r-md);
  padding: 20px 22px;
  margin-bottom: 20px;
  transition: border-left-color var(--transition);
}
.event-card.open             { border-left-color: var(--brand); }
.event-card.pending_approval { border-left-color: var(--yellow); }
.event-card.in_progress      { border-left-color: var(--blue); }
.event-card.finished         { border-left-color: var(--text-secondary); }
.event-card.disputed         { border-left-color: var(--orange); }
.event-card.cancelled        { border-left-color: var(--border); opacity: .65; }

.card-top {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px;
}
.game-label { font-size: 16px; font-weight: 700; color: var(--text-primary); }

.status-badge {
  font-size: 10px; font-weight: 700; text-transform: uppercase;
  letter-spacing: .05em; padding: 2px 9px; border-radius: var(--r-full);
}
.status-badge.open             { background: var(--brand-muted);  color: var(--brand); }
.status-badge.pending_approval { background: var(--yellow-muted); color: var(--yellow); }
.status-badge.in_progress      { background: var(--blue-muted);   color: var(--blue); }
.status-badge.finished         { background: rgba(139,148,158,.1); color: var(--text-secondary); }
.status-badge.disputed         { background: var(--orange-muted); color: var(--orange); }
.status-badge.cancelled        { background: rgba(139,148,158,.08); color: var(--text-muted); }

.card-meta { font-size: 12px; color: var(--text-muted); margin-bottom: 8px; display: flex; gap: 16px; }
.event-description {
  font-size: 13px; color: var(--text-secondary); background: var(--bg-elevated);
  border-left: 3px solid var(--border); border-radius: 0 var(--r) var(--r) 0;
  padding: 8px 12px; margin-bottom: 12px; white-space: pre-wrap; line-height: 1.5;
}
.link { color: var(--blue); text-decoration: none; }
.link:hover { text-decoration: underline; }

.participants { display: flex; flex-wrap: wrap; gap: 8px; margin: 12px 0 16px; }
.participant-tag {
  background: var(--bg-elevated); border: 1px solid var(--border); border-radius: var(--r-full);
  padding: 4px 14px; font-size: 12px; color: var(--text-primary); text-decoration: none;
  transition: background var(--transition);
}
.participant-tag:hover { background: var(--bg-overlay); }
.participant-tag.empty { color: var(--text-muted); font-style: italic; }

.waiting-note {
  background: var(--yellow-muted); border: 1px solid rgba(210,153,34,.2); border-radius: var(--r);
  padding: 10px 14px; font-size: 13px; color: var(--yellow); margin-bottom: 10px;
}
.approval-note { font-size: 13px; color: var(--text-primary); margin-bottom: 10px; }
.action-row { display: flex; gap: 8px; margin-bottom: 10px; }

.meetup-box {
  background: var(--brand-muted); border: 1px solid rgba(66,184,131,.2); border-radius: var(--r-md);
  padding: 14px; margin-bottom: 12px;
}
.meetup-box p { margin: 0 0 10px; font-size: 13px; color: var(--brand); }
.safety-inline {
  font-size: 11px; color: var(--yellow); background: var(--yellow-muted);
  border-radius: var(--r); padding: 5px 9px; margin-bottom: 8px;
}

.submit-status { display: flex; flex-direction: column; gap: 4px; margin-bottom: 10px; font-size: 12px; }
.done    { color: var(--brand); }
.pending { color: var(--text-muted); }
.submitted-note { font-size: 13px; color: var(--brand); padding: 4px 0 10px; }

.result-box {
  background: var(--bg-elevated); border: 1px solid var(--border); border-radius: var(--r-md);
  padding: 14px 16px; margin-bottom: 12px; font-size: 13px;
  display: flex; align-items: center; gap: 10px; flex-wrap: wrap; color: var(--text-primary);
}
.result-note { font-style: italic; color: var(--text-muted); font-size: 12px; }

.dispute-box {
  background: var(--red-muted); border: 1px solid rgba(248,81,73,.2); border-radius: var(--r-md);
  padding: 14px 16px; margin-bottom: 12px;
}
.dispute-title { color: var(--red); font-weight: 700; margin-bottom: 6px; font-size: 13px; }
.dispute-box p { font-size: 13px; color: var(--text-primary); margin: 4px 0; }
.dispute-note  { color: var(--text-muted); font-size: 12px; margin-top: 8px; }

.btn-primary {
  background: var(--brand); color: #fff; border: none; border-radius: var(--r);
  padding: 9px 20px; font-weight: 600; cursor: pointer; font-size: 13px; font-family: var(--font);
  display: inline-block; margin-bottom: 10px; transition: background var(--transition);
}
.btn-primary:hover:not(:disabled) { background: var(--brand-hover); }
.btn-primary:disabled { opacity: .5; cursor: default; }

.btn-success {
  flex: 1; background: var(--brand); color: #fff; border: none;
  border-radius: var(--r); padding: 8px 16px; font-weight: 600; cursor: pointer; font-size: 13px;
  font-family: var(--font); transition: background var(--transition);
}
.btn-success:hover:not(:disabled) { background: var(--brand-hover); }

.btn-danger {
  flex: 1; background: var(--red-muted); color: var(--red); border: 1px solid rgba(248,81,73,.3);
  border-radius: var(--r); padding: 8px 16px; cursor: pointer; font-size: 13px;
  font-family: var(--font); transition: background var(--transition);
}
.btn-danger:hover:not(:disabled) { background: rgba(248,81,73,.2); }

.btn-cancel {
  background: transparent; border: 1px solid rgba(248,81,73,.3); color: var(--red);
  border-radius: var(--r); padding: 5px 13px; font-size: 12px; cursor: pointer; margin-top: 6px;
  font-family: var(--font); transition: background var(--transition);
}
.btn-cancel:hover:not(:disabled) { background: var(--red-muted); }
.btn-cancel:disabled, .btn-success:disabled, .btn-danger:disabled { opacity: .5; cursor: default; }

.error-msg { color: var(--red); font-size: 12px; margin-top: 8px; }

.chat-section {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  overflow: hidden;
}

.chat-title {
  background: var(--bg-elevated); color: var(--text-primary); margin: 0;
  padding: 12px 16px; font-size: 14px; font-weight: 600;
  border-bottom: 1px solid var(--border);
}

.chat-messages {
  height: 280px; overflow-y: auto; padding: 12px;
  display: flex; flex-direction: column; gap: 8px;
}

.msg { display: flex; flex-direction: column; }
.msg.mine   { align-items: flex-end; }
.msg.theirs { align-items: flex-start; }

.bubble {
  max-width: 260px; padding: 7px 12px; border-radius: 14px;
  font-size: 13px; line-height: 1.4; word-break: break-word;
}
.msg.mine   .bubble { background: var(--brand); color: #fff; border-bottom-right-radius: 4px; }
.msg.theirs .bubble { background: var(--bg-elevated); color: var(--text-primary); border-bottom-left-radius: 4px; }

.time { font-size: 10px; color: var(--text-muted); margin-top: 2px; padding: 0 4px; }
.no-messages { text-align: center; color: var(--text-muted); font-size: 13px; padding: 20px 0; }

.chat-input-row {
  display: flex; gap: 8px; padding: 10px;
  border-top: 1px solid var(--border);
}
.chat-input {
  flex: 1; padding: 7px 12px; background: var(--bg-elevated); color: var(--text-primary);
  border: 1px solid var(--border); border-radius: var(--r-full); font-size: 13px; outline: none;
  font-family: var(--font); transition: border-color var(--transition);
}
.chat-input::placeholder { color: var(--text-muted); }
.chat-input:focus { border-color: var(--brand); }
.btn-send {
  background: var(--brand); color: #fff; border: none; border-radius: 50%;
  width: 34px; height: 34px; cursor: pointer; font-size: 14px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
  transition: background var(--transition);
}
.btn-send:hover { background: var(--brand-hover); }

.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,.7);
  z-index: 2000; display: flex; align-items: center; justify-content: center;
}
.modal {
  background: var(--bg-surface); border: 1px solid var(--border); border-radius: var(--r-lg);
  padding: 26px 28px; width: 340px; color: var(--text-primary); box-shadow: var(--shadow-lg);
}
.modal h3 { margin: 0 0 4px; font-size: 16px; font-weight: 700; }
.modal-sub { color: var(--text-muted); font-size: 12px; margin: 0 0 16px; line-height: 1.4; }

label { display: block; font-size: 12px; color: var(--text-secondary); margin-bottom: 4px; font-weight: 500; }
.optional { color: var(--text-muted); font-size: 11px; }

.radio-group { display: flex; flex-direction: column; gap: 8px; margin-bottom: 16px; }
.radio-opt {
  display: flex; align-items: center; gap: 8px;
  background: var(--bg-elevated); border: 1px solid var(--border); border-radius: var(--r);
  padding: 9px 14px; cursor: pointer; font-size: 13px; transition: background var(--transition);
}
.radio-opt:hover { background: var(--bg-overlay); }
.radio-opt input { accent-color: var(--brand); }

.text-input {
  width: 100%; padding: 8px 10px; background: var(--bg-elevated); color: var(--text-primary);
  border: 1px solid var(--border); border-radius: var(--r); font-size: 13px; font-family: var(--font);
  margin-bottom: 14px; box-sizing: border-box; outline: none; transition: border-color var(--transition);
}
.text-input:focus { border-color: var(--brand); }
.text-input::placeholder { color: var(--text-muted); }

.modal-actions { display: flex; gap: 8px; margin-top: 4px; }
.btn-ghost {
  background: var(--bg-elevated); color: var(--text-secondary); border: 1px solid var(--border);
  border-radius: var(--r); padding: 8px 16px; cursor: pointer; font-size: 13px; font-family: var(--font);
  transition: background var(--transition);
}
.btn-ghost:hover { background: var(--bg-overlay); }
</style>
