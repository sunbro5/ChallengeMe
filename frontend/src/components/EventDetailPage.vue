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
          <span>{{ formatDate(event.scheduledAt) }}</span>
          <a :href="mapsLink(event)" target="_blank" class="link">{{ $t('eventDetail.openInMaps') }}</a>

          <!-- Share button — native sheet on mobile, dropdown on desktop -->
          <div class="share-wrap" ref="shareWrap">
            <button class="share-btn" @click="doShare">
              <svg class="share-icon-svg" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="3"  r="1.5"/>
                <circle cx="12" cy="13" r="1.5"/>
                <circle cx="3"  cy="8"  r="1.5"/>
                <line x1="10.55" y1="4.05"  x2="4.45" y2="6.95"/>
                <line x1="10.55" y1="11.95" x2="4.45" y2="9.05"/>
              </svg>
              {{ $t('eventDetail.share') }}
            </button>

            <transition name="share-drop">
              <div v-if="shareMenu" class="share-dropdown">
                <a class="share-option" :href="whatsappUrl" target="_blank" rel="noopener" @click="shareMenu = false">
                  <img src="https://cdn.jsdelivr.net/gh/simple-icons/simple-icons/icons/whatsapp.svg" class="si-icon" alt="WhatsApp" /> WhatsApp
                </a>
                <a class="share-option" :href="telegramUrl" target="_blank" rel="noopener" @click="shareMenu = false">
                  <img src="https://cdn.jsdelivr.net/gh/simple-icons/simple-icons/icons/telegram.svg" class="si-icon" alt="Telegram" /> Telegram
                </a>
                <a class="share-option" :href="twitterUrl" target="_blank" rel="noopener" @click="shareMenu = false">
                  <img src="https://cdn.jsdelivr.net/gh/simple-icons/simple-icons/icons/x.svg" class="si-icon" alt="X" /> X / Twitter
                </a>
                <a class="share-option" :href="facebookUrl" target="_blank" rel="noopener" @click="shareMenu = false">
                  <img src="https://cdn.jsdelivr.net/gh/simple-icons/simple-icons/icons/facebook.svg" class="si-icon" alt="Facebook" /> Facebook
                </a>
                <hr class="share-divider" />
                <button class="share-option" @click="copyLink">
                  {{ shareCopied ? $t('eventDetail.copyLinkDone') : $t('eventDetail.copyLink') }}
                </button>
              </div>
            </transition>
          </div>
        </div>

        <p v-if="event.description" class="event-description">{{ event.description }}</p>
        <p v-if="event.locationName" class="location-name-badge">{{ event.locationName }}</p>
        <p v-if="event.invitedUsername" class="private-invite-badge">
          {{ $t('eventDetail.privateInvite', { username: event.invitedUsername }) }}
        </p>

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
          >{{ p }}</router-link>
          <span v-if="event.participants.length === 1" class="participant-tag empty">
            {{ $t('eventDetail.waitingForChallenger') }}
          </span>
        </div>

        <!-- ── Team panels (PUB_QUIZ only) ───────────────────────────────── -->
        <div v-if="event.isTeamEvent && event.status !== 'OPEN'" class="team-section">
          <div class="team-section-title">{{ $t('teamQuiz.title') }}</div>
          <div class="team-columns">

            <!-- Team A -->
            <div class="team-panel team-a">
              <div class="team-header">
                <span class="team-name">{{ $t('teamQuiz.teamA') }}</span>
                <span class="team-count">{{ (event.creatorTeam || []).length }}</span>
              </div>
              <div class="team-members-list">
                <div v-for="member in (event.creatorTeam || [])" :key="member" class="team-member-row">
                  <router-link :to="`/player/${member}`" class="team-member-link">{{ member }}</router-link>
                  <span v-if="member === event.creatorUsername" class="captain-tag">{{ $t('teamQuiz.captain') }}</span>
                </div>
              </div>
              <button
                v-if="isLoggedIn && !event.joined && !event.myTeamSide && event.status === 'IN_PROGRESS' && event.challengerUsername"
                class="btn-join-team"
                :disabled="teamBusy"
                @click="joinTeam('CREATOR')"
              >{{ teamBusy ? $t('teamQuiz.joining') : $t('teamQuiz.joinTeamA') }}</button>
              <div v-if="event.myTeamSide === 'CREATOR' && !event.joined" class="on-team-note">
                {{ $t('teamQuiz.onTeamA') }}
              </div>
            </div>

            <!-- Team B -->
            <div class="team-panel team-b" :class="{ 'team-empty': !event.challengerUsername }">
              <div class="team-header">
                <span class="team-name">{{ $t('teamQuiz.teamB') }}</span>
                <span class="team-count">{{ (event.challengerTeam || []).length }}</span>
              </div>
              <div class="team-members-list">
                <template v-if="event.challengerTeam">
                  <div v-for="member in event.challengerTeam" :key="member" class="team-member-row">
                    <router-link :to="`/player/${member}`" class="team-member-link">{{ member }}</router-link>
                    <span v-if="member === event.challengerUsername" class="captain-tag">{{ $t('teamQuiz.captain') }}</span>
                  </div>
                </template>
                <span v-else class="team-waiting">{{ $t('eventDetail.waitingForChallenger') }}</span>
              </div>
              <button
                v-if="isLoggedIn && !event.joined && !event.myTeamSide && event.status === 'IN_PROGRESS' && event.challengerUsername"
                class="btn-join-team"
                :disabled="teamBusy"
                @click="joinTeam('CHALLENGER')"
              >{{ teamBusy ? $t('teamQuiz.joining') : $t('teamQuiz.joinTeamB') }}</button>
              <div v-if="event.myTeamSide === 'CHALLENGER' && !event.joined" class="on-team-note">
                {{ $t('teamQuiz.onTeamB') }}
              </div>
            </div>

          </div>

          <!-- Leave button (non-captains only) -->
          <button
            v-if="isLoggedIn && event.myTeamSide && !event.joined && event.status === 'IN_PROGRESS'"
            class="btn-leave-team"
            :disabled="teamBusy"
            @click="leaveTeam"
          >{{ teamBusy ? $t('teamQuiz.leaving') : $t('teamQuiz.leaveTeam') }}</button>

          <p v-if="teamError" class="error-msg">{{ teamError }}</p>
        </div>

        <!-- ── Actions ────────────────────────────────────────────────────── -->
        <template v-if="isLoggedIn">

          <div v-if="event.status === 'OPEN' && !event.isCreator && !event.joined" class="accept-safety">
            <span>{{ $t('safety.acceptWarning') }} <router-link to="/tos" target="_blank" class="tos-link">{{ $t('safety.tosLink') }}</router-link></span>
          </div>

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
            <div class="safety-inline">{{ $t('safety.meetupWarning') }}</div>
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

          <!-- ── Sportsmanship voting ───────────────────────────────── -->
          <div v-if="event.status === 'FINISHED' && event.joined && otherUser" class="sport-vote-box">
            <span class="sport-vote-label">{{ $t('reputation.voteTitle') }}</span>
            <div v-if="!sportVoted" class="sport-vote-btns">
              <button class="btn-thumbs up"   :disabled="sportVoting" @click="submitVote(true)">👍</button>
              <button class="btn-thumbs down" :disabled="sportVoting" @click="submitVote(false)">👎</button>
            </div>
            <span v-else class="sport-voted-ok">{{ $t('reputation.voted') }}</span>
          </div>

          <button
            v-if="event.status === 'FINISHED' && event.joined"
            class="btn-rematch"
            @click="rematch"
          >{{ $t('eventDetail.rematch') }}</button>

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

      <!-- ── Match Chat ────────────────────────────────────────────────── -->
      <div v-if="otherUser" class="match-chat">

        <!-- Header: both players face to face -->
        <div class="mc-header">
          <div class="mc-avatars">
            <span class="mc-avatar mc-me">{{ currentUser?.[0]?.toUpperCase() }}</span>
            <span class="mc-vs">vs</span>
            <span class="mc-avatar mc-them">{{ otherUser[0]?.toUpperCase() }}</span>
          </div>
          <div class="mc-header-info">
            <span class="mc-opponent-name">{{ otherUser }}</span>
            <span class="mc-label">{{ $t('eventDetail.matchChat') }}</span>
          </div>
        </div>

        <!-- Messages -->
        <div class="mc-messages" ref="messagesEl">
          <div v-if="!messages.length" class="mc-empty">{{ $t('eventDetail.noMessages') }}</div>

          <div
            v-for="(msg, i) in messages"
            :key="i"
            :class="['mc-row', msg.senderUsername === currentUser ? 'mc-mine' : 'mc-theirs']"
          >
            <!-- Avatar circle -->
            <div class="mc-avatar-sm">{{ msg.senderUsername?.[0]?.toUpperCase() }}</div>

            <div class="mc-msg-body">
              <!-- Sender name only for "theirs" -->
              <span v-if="msg.senderUsername !== currentUser" class="mc-sender">{{ msg.senderUsername }}</span>
              <div class="mc-bubble">{{ msg.content }}</div>
              <div class="mc-time">{{ formatTime(msg.sentAt) }}</div>
            </div>
          </div>
        </div>

        <!-- Input -->
        <div class="mc-input-row">
          <input
            v-model="chatInput"
            type="text"
            :placeholder="$t('eventDetail.messagePlaceholder')"
            class="mc-input"
            @keyup.enter="sendMessage"
          />
          <button class="mc-send" @click="sendMessage">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>
            </svg>
          </button>
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

      messages:        [],
      chatInput:       '',
      pollTimer:       null,
      eventPollTimer:  null,

      currentUser: localStorage.getItem('username') || '',
      isLoggedIn:  localStorage.getItem('isLoggedIn') === 'true',

      resultModal: { visible: false, winner: '', note: '', busy: false, error: '' },
      shareMenu:   false,
      shareCopied: false,
      sportVoted:  false,
      sportVoting: false,
      teamBusy:    false,
      teamError:   '',
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
    shareUrl() {
      return window.location.href
    },
    shareText() {
      if (!this.event) return ''
      return this.$t('eventDetail.shareText', { game: this.gameLabel(this.event.gameType) })
    },
    whatsappUrl() {
      return `https://wa.me/?text=${encodeURIComponent(this.shareText + ' ' + this.shareUrl)}`
    },
    telegramUrl() {
      return `https://t.me/share/url?url=${encodeURIComponent(this.shareUrl)}&text=${encodeURIComponent(this.shareText)}`
    },
    twitterUrl() {
      return `https://twitter.com/intent/tweet?text=${encodeURIComponent(this.shareText)}&url=${encodeURIComponent(this.shareUrl)}`
    },
    facebookUrl() {
      return `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(this.shareUrl)}`
    },
  },
  async mounted() {
    await this.loadGames()
    await this.loadEvent()
    this.setupPolling()
    document.addEventListener('click', this.onShareDocClick)
    if (this.isLoggedIn && this.event?.status === 'FINISHED') {
      this.loadVoteStatus()
    }
  },
  beforeUnmount() {
    clearInterval(this.pollTimer)
    clearInterval(this.eventPollTimer)
    document.removeEventListener('click', this.onShareDocClick)
  },
  methods: {
    async loadGames() {
      try {
        const { data } = await axios.get('/api/games', { withCredentials: true })
        data.forEach(g => { this.gameMeta[g.key] = { icon: g.icon, nameCs: g.nameCs, nameEn: g.nameEn } })
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

    // ── Polling helpers ───────────────────────────────────────────────────────
    setupPolling() {
      // Start message polling whenever we have a chat partner
      if (this.otherUser && !this.pollTimer) {
        this.loadMessages()
        this.pollTimer = setInterval(this.loadMessages, 4000)
      }
      // Poll the event itself while waiting for state changes
      const awaitingUpdate = this.event &&
        ['OPEN', 'PENDING_APPROVAL'].includes(this.event.status)
      if (awaitingUpdate && !this.eventPollTimer) {
        this.eventPollTimer = setInterval(this.pollEvent, 5000)
      } else if (!awaitingUpdate) {
        clearInterval(this.eventPollTimer)
        this.eventPollTimer = null
      }
    },

    async pollEvent() {
      try {
        const { data } = await axios.get(
          `/api/events/${this.$route.params.id}`,
          { withCredentials: true }
        )
        this.event = data
        this.setupPolling()
      } catch { /* non-fatal */ }
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
        this.setupPolling()
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
        this.setupPolling()
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

    async loadVoteStatus() {
      try {
        const { data } = await axios.get(
          `/api/sportsmanship/check/${this.$route.params.id}`,
          { withCredentials: true }
        )
        this.sportVoted = data.voted
      } catch { /* non-fatal */ }
    },

    async submitVote(positive) {
      this.sportVoting = true
      try {
        await axios.post('/api/sportsmanship', {
          eventId:  this.event.id,
          positive,
        }, { withCredentials: true })
        this.sportVoted = true
      } catch { /* non-fatal */ } finally {
        this.sportVoting = false
      }
    },

    async joinTeam(side) {
      this.teamBusy = true; this.teamError = ''
      try {
        const { data } = await axios.post(
          `/api/events/${this.event.id}/team/join`,
          { side },
          { withCredentials: true }
        )
        this.event = data
      } catch (e) {
        this.teamError = e.response?.data || this.$t('teamQuiz.couldNotJoin')
      } finally { this.teamBusy = false }
    },

    async leaveTeam() {
      this.teamBusy = true; this.teamError = ''
      try {
        const { data } = await axios.delete(
          `/api/events/${this.event.id}/team/leave`,
          { withCredentials: true }
        )
        this.event = data
      } catch (e) {
        this.teamError = e.response?.data || this.$t('teamQuiz.couldNotLeave')
      } finally { this.teamBusy = false }
    },

    gameLabel(type) {
      const m = this.gameMeta[type]
      if (!m) return type || ''
      const name = this.$i18n?.locale === 'cs' ? m.nameCs : m.nameEn
      return `${m.icon} ${name}`
    },
    statusLabel(s) { return this.$t('status.' + s.toLowerCase()) },
    formatDate(iso) { return iso ? new Date(iso).toLocaleString() : '—' },
    formatTime(iso) {
      if (!iso) return ''
      return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    },
    mapsLink(ev) { return `https://www.google.com/maps?q=${ev.latitude},${ev.longitude}` },
    rematch() {
      const q = {
        rematch: '1',
        lat: this.event.latitude,
        lng: this.event.longitude,
        gameType: this.event.gameType,
      }
      if (this.event.description)     q.description     = this.event.description
      if (this.event.locationName)    q.locationName    = this.event.locationName
      if (this.event.invitedUsername) q.invitedUsername = this.event.invitedUsername
      if (this.event.visibility)      q.visibility      = this.event.visibility
      this.$router.push({ path: '/map', query: q })
    },
    // ── Share ─────────────────────────────────────────────────────────────────
    doShare() {
      // On mobile: delegate to the native OS share sheet (one-tap WhatsApp, SMS, etc.)
      if (navigator.share) {
        navigator.share({
          title: this.$t('eventDetail.shareTitle', { game: this.gameLabel(this.event.gameType) }),
          text:  this.shareText,
          url:   this.shareUrl,
        }).catch(() => {}) // AbortError = user dismissed — ignore
        return
      }
      // Desktop: toggle our own dropdown
      this.shareMenu = !this.shareMenu
    },
    onShareDocClick(e) {
      if (this.$refs.shareWrap && !this.$refs.shareWrap.contains(e.target)) {
        this.shareMenu = false
      }
    },
    async copyLink() {
      try {
        await navigator.clipboard.writeText(this.shareUrl)
        this.shareCopied = true
        setTimeout(() => { this.shareCopied = false; this.shareMenu = false }, 1800)
      } catch { /* clipboard blocked */ }
    },
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

.card-meta { font-size: 12px; color: var(--text-muted); margin-bottom: 8px; display: flex; gap: 16px; align-items: center; flex-wrap: wrap; }

/* ── Share button + dropdown ─────────────────────────────────────────────── */
.share-wrap { position: relative; }

.share-btn {
  display: inline-flex; align-items: center; gap: 5px;
  background: transparent; border: 1px solid var(--border); color: var(--text-muted);
  border-radius: var(--r); padding: 2px 9px; font-size: 11px; font-family: var(--font);
  cursor: pointer; transition: color var(--transition), border-color var(--transition);
  white-space: nowrap;
}
.share-btn:hover { color: var(--brand); border-color: var(--brand); }

.share-icon-svg { width: 12px; height: 12px; flex-shrink: 0; }

.share-dropdown {
  position: absolute; top: calc(100% + 6px); right: 0;
  background: var(--bg-overlay); border: 1px solid var(--border);
  border-radius: var(--r-lg); box-shadow: var(--shadow-lg);
  min-width: 170px; z-index: 1200; overflow: hidden;
}

.share-option {
  display: flex; align-items: center; gap: 9px;
  width: 100%; padding: 9px 14px; font-size: 13px;
  color: var(--text-primary); text-decoration: none;
  background: none; border: none; font-family: var(--font);
  cursor: pointer; transition: background var(--transition);
  white-space: nowrap; text-align: left;
}
.share-option:hover { background: var(--bg-elevated); }

/* Simple Icons are black SVGs — invert in dark mode, keep in light */
.si-icon {
  width: 16px; height: 16px; flex-shrink: 0;
  filter: invert(1) brightness(1.4);
}
html.light .si-icon { filter: none; }

.share-divider { border: none; border-top: 1px solid var(--border); margin: 2px 0; }

.share-drop-enter-active { transition: opacity .15s ease, transform .15s ease; }
.share-drop-leave-active { transition: opacity .1s  ease, transform .1s  ease; }
.share-drop-enter-from,
.share-drop-leave-to     { opacity: 0; transform: translateY(-4px); }
.event-description {
  font-size: 13px; color: var(--text-secondary); background: var(--bg-elevated);
  border-left: 3px solid var(--border); border-radius: 0 var(--r) var(--r) 0;
  padding: 8px 12px; margin-bottom: 12px; white-space: pre-wrap; line-height: 1.5;
}
.location-name-badge {
  font-size: 12px; color: var(--text-secondary); margin-bottom: 8px;
}
.private-invite-badge {
  font-size: 12px; font-weight: 600;
  background: rgba(203,166,247,.12); color: var(--brand);
  border: 1px solid rgba(203,166,247,.3);
  border-radius: var(--r); padding: 4px 10px;
  display: inline-block; margin-bottom: 10px;
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

/* ── Team quiz panels ────────────────────────────────────────────────────── */
.team-section {
  margin: 4px 0 16px;
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  overflow: hidden;
}
.team-section-title {
  background: var(--bg-elevated);
  border-bottom: 1px solid var(--border);
  padding: 8px 14px;
  font-size: 11px; font-weight: 700; text-transform: uppercase;
  letter-spacing: .06em; color: var(--text-muted);
}
.team-columns {
  display: grid; grid-template-columns: 1fr 1fr; gap: 0;
}
.team-panel {
  padding: 12px 14px;
}
.team-panel.team-b { border-left: 1px solid var(--border); }
.team-panel.team-empty { opacity: .6; }

.team-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 8px;
}
.team-name {
  font-size: 12px; font-weight: 700; color: var(--text-primary);
}
.team-panel.team-a .team-name { color: var(--brand); }
.team-panel.team-b .team-name { color: var(--blue); }
.team-count {
  font-size: 11px; color: var(--text-muted);
  background: var(--bg-elevated); border-radius: var(--r-full);
  padding: 1px 7px;
}
.team-members-list {
  display: flex; flex-direction: column; gap: 4px; min-height: 28px; margin-bottom: 10px;
}
.team-member-row {
  display: flex; align-items: center; gap: 6px; font-size: 12px;
}
.team-member-link {
  color: var(--text-primary); text-decoration: none;
}
.team-member-link:hover { text-decoration: underline; color: var(--brand); }
.captain-tag {
  font-size: 9px; font-weight: 700; text-transform: uppercase; letter-spacing: .04em;
  color: var(--text-muted); background: var(--bg-elevated);
  border: 1px solid var(--border); border-radius: var(--r-full); padding: 1px 5px;
}
.team-waiting { font-size: 12px; color: var(--text-muted); font-style: italic; }

.btn-join-team {
  width: 100%; background: var(--bg-elevated); border: 1px solid var(--border);
  color: var(--text-primary); border-radius: var(--r); padding: 6px 10px;
  font-size: 12px; font-family: var(--font); cursor: pointer;
  transition: background var(--transition), border-color var(--transition);
}
.team-panel.team-a .btn-join-team:hover:not(:disabled) { border-color: var(--brand); color: var(--brand); background: var(--brand-muted); }
.team-panel.team-b .btn-join-team:hover:not(:disabled) { border-color: var(--blue);  color: var(--blue);  background: var(--blue-muted); }
.btn-join-team:disabled { opacity: .5; cursor: default; }

.on-team-note {
  font-size: 11px; font-weight: 600; color: var(--brand);
  text-align: center; padding-top: 4px;
}
.team-panel.team-b .on-team-note { color: var(--blue); }

.btn-leave-team {
  display: block; width: calc(100% - 28px); margin: 0 14px 12px;
  background: transparent; border: 1px solid rgba(248,81,73,.3); color: var(--red);
  border-radius: var(--r); padding: 5px 10px; font-size: 12px; font-family: var(--font);
  cursor: pointer; transition: background var(--transition);
}
.btn-leave-team:hover:not(:disabled) { background: var(--red-muted); }
.btn-leave-team:disabled { opacity: .5; cursor: default; }

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

.sport-vote-box {
  display: flex; align-items: center; gap: 10px;
  background: var(--bg-elevated); border: 1px solid var(--border);
  border-radius: var(--r); padding: 10px 14px; margin-bottom: 10px;
}
.sport-vote-label { font-size: 12px; color: var(--text-secondary); flex: 1; }
.sport-vote-btns  { display: flex; gap: 6px; }
.btn-thumbs {
  background: var(--bg-surface); border: 1px solid var(--border);
  border-radius: var(--r); padding: 4px 12px; font-size: 18px;
  cursor: pointer; transition: background var(--transition);
}
.btn-thumbs.up:hover:not(:disabled)   { background: var(--brand-muted); border-color: var(--brand); }
.btn-thumbs.down:hover:not(:disabled) { background: var(--red-muted);   border-color: var(--red); }
.btn-thumbs:disabled { opacity: .5; cursor: default; }
.sport-voted-ok { font-size: 12px; color: var(--brand); font-weight: 600; }

.btn-rematch {
  background: var(--brand-muted); color: var(--brand);
  border: 1px solid rgba(66,184,131,.35); border-radius: var(--r);
  padding: 7px 16px; font-size: 13px; font-weight: 600; font-family: var(--font);
  cursor: pointer; display: inline-block; margin-bottom: 10px;
  transition: background var(--transition);
}
.btn-rematch:hover { background: rgba(66,184,131,.22); }

.btn-cancel {
  background: transparent; border: 1px solid rgba(248,81,73,.3); color: var(--red);
  border-radius: var(--r); padding: 5px 13px; font-size: 12px; cursor: pointer; margin-top: 6px;
  font-family: var(--font); transition: background var(--transition);
}
.btn-cancel:hover:not(:disabled) { background: var(--red-muted); }
.btn-cancel:disabled, .btn-success:disabled, .btn-danger:disabled { opacity: .5; cursor: default; }

.error-msg { color: var(--red); font-size: 12px; margin-top: 8px; }

/* ── Match Chat ────────────────────────────────────────────────────────── */
.match-chat {
  border: 1px solid var(--border);
  border-left: 3px solid var(--brand);
  border-radius: var(--r-md);
  overflow: hidden;
  background: var(--bg-surface);
}

.mc-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 16px;
  background: linear-gradient(135deg, var(--bg-elevated) 0%, var(--bg-surface) 100%);
  border-bottom: 1px solid var(--border);
}

.mc-avatars {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.mc-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; flex-shrink: 0;
}
.mc-me   { background: var(--brand-muted); color: var(--brand); border: 1px solid rgba(66,184,131,.4); }
.mc-them { background: var(--bg-overlay);  color: var(--text-secondary); border: 1px solid var(--border); }

.mc-vs {
  font-size: 10px; font-weight: 700; color: var(--text-muted);
  text-transform: uppercase; letter-spacing: .06em;
}

.mc-header-info {
  display: flex; flex-direction: column; gap: 1px;
}
.mc-opponent-name { font-size: 14px; font-weight: 600; color: var(--text-primary); }
.mc-label { font-size: 11px; color: var(--text-muted); letter-spacing: .02em; }

.mc-messages {
  height: 320px; overflow-y: auto; padding: 14px 16px;
  display: flex; flex-direction: column; gap: 12px;
}

.mc-empty { text-align: center; color: var(--text-muted); font-size: 13px; padding: 40px 0; }

.mc-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}
.mc-mine   { flex-direction: row-reverse; }
.mc-theirs { flex-direction: row; }

.mc-avatar-sm {
  width: 26px; height: 26px; border-radius: 50%;
  background: var(--bg-elevated); border: 1px solid var(--border);
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 700; color: var(--text-secondary);
  flex-shrink: 0;
}
.mc-mine .mc-avatar-sm   { background: var(--brand-muted); color: var(--brand); border-color: rgba(66,184,131,.3); }
.mc-theirs .mc-avatar-sm { background: var(--bg-elevated); }

.mc-msg-body {
  display: flex; flex-direction: column; gap: 2px;
  max-width: calc(100% - 80px);
}
.mc-mine   .mc-msg-body { align-items: flex-end; }
.mc-theirs .mc-msg-body { align-items: flex-start; }

.mc-sender {
  font-size: 10px; font-weight: 600; color: var(--text-muted);
  padding: 0 4px; text-transform: uppercase; letter-spacing: .04em;
}

.mc-bubble {
  padding: 8px 13px; border-radius: 16px;
  font-size: 13px; line-height: 1.45; word-break: break-word;
}
.mc-mine   .mc-bubble { background: var(--brand); color: #fff; border-bottom-right-radius: 4px; }
.mc-theirs .mc-bubble { background: var(--bg-elevated); color: var(--text-primary); border-bottom-left-radius: 4px; border: 1px solid var(--border); }

.mc-time { font-size: 10px; color: var(--text-muted); padding: 0 4px; }

.mc-input-row {
  display: flex; gap: 8px; padding: 10px 12px;
  border-top: 1px solid var(--border);
  background: var(--bg-surface);
}
.mc-input {
  flex: 1; padding: 8px 14px; background: var(--bg-elevated); color: var(--text-primary);
  border: 1px solid var(--border); border-radius: var(--r-full); font-size: 13px; outline: none;
  font-family: var(--font); transition: border-color var(--transition);
}
.mc-input::placeholder { color: var(--text-muted); }
.mc-input:focus { border-color: var(--brand); }
.mc-send {
  background: var(--brand); color: #fff; border: none; border-radius: 50%;
  width: 36px; height: 36px; cursor: pointer;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
  transition: background var(--transition);
}
.mc-send:hover { background: var(--brand-hover); }

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

@media (max-width: 640px) {
  .event-detail-page { padding: 16px 14px 76px; }
  .event-card        { padding: 14px 16px; }
  .mc-messages       { height: 50vh; }
  .modal             { width: calc(100vw - 32px); }
  /* On narrow screens the dropdown can't safely anchor right — keep it in view */
  .share-dropdown { right: auto; left: 0; }
}
</style>
