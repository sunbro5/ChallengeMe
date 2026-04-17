<template>
  <div class="player-page">
    <div v-if="loading" class="state-msg">{{ $t('common.loading') }}</div>
    <div v-else-if="error" class="state-msg error">{{ error }}</div>

    <template v-else>
      <!-- ── Profile header ──────────────────────────────────────────── -->
      <div class="profile-header">
        <div class="avatar">{{ profile.username[0].toUpperCase() }}</div>
        <div class="header-info">
          <h2>{{ profile.username }}</h2>
          <p v-if="profile.isMe" class="me-badge">{{ $t('player.thatsYou') }}</p>
        </div>
      </div>

      <!-- ── Stats row ───────────────────────────────────────────────── -->
      <div class="stats-row">
        <div class="stat">
          <span class="stat-value wins">{{ profile.wins }}</span>
          <span class="stat-label">{{ $t('player.wins') }}</span>
        </div>
        <div class="stat">
          <span class="stat-value losses">{{ profile.losses }}</span>
          <span class="stat-label">{{ $t('player.losses') }}</span>
        </div>
        <div class="stat">
          <span class="stat-value draws">{{ profile.draws }}</span>
          <span class="stat-label">{{ $t('player.draws') }}</span>
        </div>
        <div class="stat">
          <span class="stat-value disputes" :class="{ warning: profile.disputes > 0 }">
            {{ profile.disputes }}
          </span>
          <span class="stat-label">{{ $t('player.disputes') }}</span>
        </div>
      </div>

      <!-- ── Actions (not shown on own profile) ──────────────────────── -->
      <div v-if="!profile.isMe && isLoggedIn" class="actions-row">
        <button
          v-if="profile.friendStatus === 'NONE'"
          class="btn-friend"
          @click="sendFriendRequest"
          :disabled="actionBusy"
        >{{ $t('player.addFriend') }}</button>

        <span v-else-if="profile.friendStatus === 'PENDING'" class="tag-pending">
          {{ $t('player.requestPending') }}
        </span>

        <button
          v-else-if="profile.friendStatus === 'ACCEPTED'"
          class="btn-unfriend"
          @click="unfriend"
          :disabled="actionBusy"
        >{{ $t('player.unfriend') }}</button>

        <button class="btn-report" @click="reportModal.visible = true">{{ $t('player.report') }}</button>
      </div>

      <p v-if="actionError" class="msg-error">{{ actionError }}</p>

      <!-- ── Game history ─────────────────────────────────────────────── -->
      <h3 class="section-title">{{ $t('player.gameHistory') }}</h3>

      <div v-if="!profile.games.length" class="empty-history">
        {{ $t('player.noGames') }}
      </div>

      <div v-else class="games-list">
        <div
          v-for="game in profile.games"
          :key="game.id"
          class="game-row"
          :class="game.result"
        >
          <div class="game-icon">{{ gameIcon(game.gameType) }}</div>

          <div class="game-info">
            <div class="game-top">
              <span class="game-type">{{ gameLabel(game.gameType) }}</span>
              <span class="result-tag" :class="game.result">{{ resultLabel(game.result) }}</span>
            </div>
            <div class="game-bottom">
              <span class="opponent">vs <strong>{{ game.opponentUsername }}</strong></span>
              <span class="game-date">{{ formatDate(game.scheduledAt) }}</span>
            </div>
            <div v-if="game.resultNote" class="game-note">"{{ game.resultNote }}"</div>

            <div v-if="game.result === 'disputed'" class="dispute-detail">
              <span>{{ game.creatorUsername }} said: <strong>{{ game.creatorResult || 'draw' }}</strong></span>
              —
              <span>{{ $t('player.opponentSaid') }}: <strong>{{ game.challengerResult || 'draw' }}</strong></span>
            </div>
          </div>
        </div>
      </div>

      <!-- ── Danger zone (own profile only) ───────────────────────────── -->
      <div v-if="profile.isMe" class="danger-zone">
        <h3 class="danger-title">{{ $t('player.dangerZone') }}</h3>
        <div class="danger-row">
          <div class="danger-desc">
            <strong>{{ $t('player.deleteAccountTitle') }}</strong>
            <p>{{ $t('player.deleteAccountDesc') }}</p>
          </div>
          <button class="btn-delete-account" @click="deleteAccountModal = true">
            {{ $t('player.deleteAccountBtn') }}
          </button>
        </div>
      </div>

    </template>

    <!-- ── Delete account confirm modal ────────────────────────────────── -->
    <div v-if="deleteAccountModal" class="modal-overlay" @click.self="deleteAccountModal = false">
      <div class="modal">
        <h3 class="modal-danger-title">{{ $t('player.deleteModal.title') }}</h3>
        <p class="modal-subtitle">{{ $t('player.deleteModal.subtitle') }}</p>
        <p class="modal-subtitle" style="margin-top:8px">
          {{ $t('player.deleteModal.typeConfirmPre') }}<strong>{{ $t('player.deleteModal.confirmWord') }}</strong>{{ $t('player.deleteModal.typeConfirmPost') }}
        </p>
        <input
          v-model="deleteConfirmText"
          type="text"
          :placeholder="$t('player.deleteModal.confirmWord')"
          class="delete-confirm-input"
        />
        <p v-if="deleteAccountError" class="msg-error">{{ deleteAccountError }}</p>
        <div class="modal-actions">
          <button class="btn-ghost" @click="deleteAccountModal = false; deleteConfirmText = ''">{{ $t('common.cancel') }}</button>
          <button
            class="btn-danger"
            :disabled="deleteConfirmText !== $t('player.deleteModal.confirmWord') || deleteAccountBusy"
            @click="deleteAccount"
          >
            {{ deleteAccountBusy ? $t('player.deleteModal.deleting') : $t('player.deleteModal.deleteForever') }}
          </button>
        </div>
      </div>
    </div>

    <!-- ── Report modal ────────────────────────────────────────────────── -->
    <div v-if="reportModal.visible" class="modal-overlay" @click.self="reportModal.visible = false">
      <div class="modal">
        <h3>{{ $t('friends.report', { username: profile?.username }) }}</h3>
        <p class="modal-subtitle">{{ $t('player.reportModal.subtitle') }}</p>
        <textarea
          v-model="reportModal.reason"
          rows="4"
          :placeholder="$t('player.reportModal.reasonPlaceholder')"
          class="reason-textarea"
        ></textarea>
        <p v-if="reportModal.message" :class="reportModal.success ? 'msg-ok' : 'msg-error'">
          {{ reportModal.message }}
        </p>
        <div class="modal-actions">
          <button class="btn-ghost" @click="reportModal.visible = false">{{ $t('common.cancel') }}</button>
          <button class="btn-danger" @click="submitReport" :disabled="reportModal.busy">
            {{ reportModal.busy ? $t('player.reportModal.sending') : $t('player.reportModal.submitReport') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

const GAME_META = {
  TIC_TAC_TOE:         { icon: '❌', label: '❌⭕ Tic-Tac-Toe' },
  ROCK_PAPER_SCISSORS: { icon: '✂️', label: '✂️ Rock Paper Scissors' },
}

export default {
  name: 'PlayerPage',
  data() {
    return {
      loading: true,
      error: '',
      profile: null,
      isLoggedIn: !!localStorage.getItem('isLoggedIn'),
      actionBusy: false,
      actionError: '',
      reportModal: { visible: false, reason: '', message: '', success: false, busy: false },
      deleteAccountModal: false,
      deleteConfirmText: '',
      deleteAccountBusy: false,
      deleteAccountError: '',
    }
  },
  async mounted() {
    await this.loadProfile()
  },
  watch: {
    '$route.params.username'() { this.loadProfile() },
  },
  methods: {
    async loadProfile() {
      this.loading = true; this.error = ''
      try {
        const { data } = await axios.get(
          `/api/players/${this.$route.params.username}`,
          { withCredentials: true })
        this.profile = data
      } catch {
        this.error = this.$t('player.notFound')
      } finally {
        this.loading = false
      }
    },

    async sendFriendRequest() {
      this.actionBusy = true; this.actionError = ''
      try {
        await axios.post(
          `/api/friends/request?username=${this.profile.username}`,
          {}, { withCredentials: true })
        this.profile.friendStatus = 'PENDING'
      } catch (err) {
        this.actionError = err.response?.data || this.$t('player.couldNotSendRequest')
      } finally { this.actionBusy = false }
    },

    async unfriend() {
      if (!confirm(this.$t('friends.unfriendConfirm', { username: this.profile.username }))) return
      this.actionBusy = true; this.actionError = ''
      try {
        await axios.delete(
          `/api/friends/with/${this.profile.id}`,
          { withCredentials: true })
        this.profile.friendStatus = 'NONE'
      } catch (err) {
        this.actionError = err.response?.data || this.$t('player.couldNotUnfriend')
      } finally { this.actionBusy = false }
    },

    async submitReport() {
      if (!this.reportModal.reason.trim()) {
        this.reportModal.message = this.$t('player.reportModal.enterReason')
        this.reportModal.success = false
        return
      }
      this.reportModal.busy = true
      try {
        await axios.post('/api/reports', {
          reportedUsername: this.profile.username,
          reason: this.reportModal.reason,
        }, { withCredentials: true })
        this.reportModal.message = this.$t('player.reportModal.submitted')
        this.reportModal.success = true
        this.reportModal.reason = ''
        setTimeout(() => { this.reportModal.visible = false; this.reportModal.message = '' }, 1500)
      } catch (err) {
        this.reportModal.message = err.response?.data || this.$t('player.reportModal.failed')
        this.reportModal.success = false
      } finally { this.reportModal.busy = false }
    },

    async deleteAccount() {
      this.deleteAccountBusy = true; this.deleteAccountError = ''
      try {
        await axios.delete('/api/auth/account', { withCredentials: true })
        localStorage.removeItem('isLoggedIn')
        localStorage.removeItem('username')
        localStorage.removeItem('role')
        this.$router.push('/login')
      } catch (err) {
        this.deleteAccountError = err.response?.data || this.$t('player.couldNotDelete')
        this.deleteAccountBusy = false
      }
    },

    gameIcon(t)    { return GAME_META[t]?.icon  || '🎮' },
    gameLabel(t)   { return GAME_META[t]?.label || t },
    formatDate(iso){ return iso ? new Date(iso).toLocaleDateString() : '—' },
    resultLabel(r) { return this.$t('result.' + r) || r },
  },
}
</script>

<style scoped>
.player-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 32px 20px 64px;
}

.state-msg       { text-align:center; padding:60px; color:var(--text-muted); }
.state-msg.error { color:var(--red); }

.profile-header {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 24px;
}
.avatar {
  width: 64px; height: 64px;
  border-radius: 50%;
  background: var(--brand-muted);
  border: 2px solid rgba(66,184,131,.35);
  color: var(--brand);
  display: flex; align-items: center; justify-content: center;
  font-size: 1.6rem; font-weight: 700;
  flex-shrink: 0;
}
.header-info h2 { margin: 0; font-size: 20px; font-weight: 700; letter-spacing: -.01em; }
.me-badge {
  display: inline-flex;
  margin-top: 4px;
  background: var(--brand-muted);
  color: var(--brand);
  border-radius: var(--r-full);
  padding: 1px 10px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: .04em;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-bottom: 24px;
}
.stat {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--r-lg);
  padding: 14px 10px;
  text-align: center;
  display: flex; flex-direction: column; gap: 5px;
}
.stat-value         { font-size: 24px; font-weight: 700; line-height: 1; }
.stat-value.wins    { color: var(--brand); }
.stat-value.losses  { color: var(--red); }
.stat-value.draws   { color: var(--yellow); }
.stat-value.disputes { color: var(--text-muted); }
.stat-value.disputes.warning { color: var(--red); }
.stat-label         { font-size: 11px; color: var(--text-muted); font-weight: 500; text-transform: uppercase; letter-spacing: .04em; }

.actions-row {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.btn-friend {
  background: var(--blue-muted);
  color: var(--blue);
  border: 1px solid rgba(88,166,255,.3);
  border-radius: var(--r);
  padding: 7px 16px;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  font-family: var(--font);
  transition: background var(--transition);
}
.btn-friend:hover { background: rgba(88,166,255,.2); }
.btn-unfriend {
  background: transparent;
  border: 1px solid var(--red);
  color: var(--red);
  border-radius: var(--r);
  padding: 7px 16px;
  cursor: pointer;
  font-size: 13px;
  font-family: var(--font);
  transition: background var(--transition);
}
.btn-unfriend:hover { background: var(--red-muted); }
.btn-report {
  background: transparent;
  border: 1px solid var(--border);
  color: var(--text-secondary);
  border-radius: var(--r);
  padding: 7px 16px;
  cursor: pointer;
  font-size: 13px;
  font-family: var(--font);
  transition: color var(--transition), border-color var(--transition);
}
.btn-report:hover { border-color: var(--red); color: var(--red); }
.tag-pending {
  background: var(--yellow-muted);
  color: var(--yellow);
  border: 1px solid rgba(210,153,34,.3);
  border-radius: var(--r);
  padding: 7px 16px;
  font-size: 13px;
}

.section-title { font-size: 13px; font-weight: 600; color: var(--text-secondary); text-transform: uppercase; letter-spacing: .05em; margin: 0 0 14px; }
.empty-history { color: var(--text-muted); text-align: center; padding: 32px 0; font-size: 13px; }

.games-list { display:flex; flex-direction:column; gap:8px; }
.game-row {
  display: flex;
  gap: 12px;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-left: 3px solid var(--border);
  border-radius: var(--r-md);
  padding: 12px 16px;
  align-items: flex-start;
  transition: background var(--transition);
}
.game-row:hover { background: var(--bg-elevated); }
.game-row.won      { border-left-color: var(--brand); }
.game-row.lost     { border-left-color: var(--red); }
.game-row.draw     { border-left-color: var(--yellow); }
.game-row.disputed { border-left-color: var(--orange); }

.game-icon { font-size: 22px; flex-shrink: 0; line-height: 1; margin-top: 1px; }
.game-info { flex: 1; }

.game-top    { display:flex; align-items:center; gap:8px; margin-bottom:3px; }
.game-type   { font-size:13px; font-weight:600; color:var(--text-primary); }
.result-tag  { font-size:10px; font-weight:700; border-radius:var(--r-full); padding:2px 8px; text-transform:uppercase; letter-spacing:.05em; }
.result-tag.won      { background:var(--brand-muted);  color:var(--brand); }
.result-tag.lost     { background:var(--red-muted);    color:var(--red); }
.result-tag.draw     { background:var(--yellow-muted); color:var(--yellow); }
.result-tag.disputed { background:var(--orange-muted); color:var(--orange); }

.game-bottom { display:flex; gap:14px; font-size:12px; color:var(--text-secondary); }
.game-date   { color:var(--text-muted); }
.game-note   { font-size:12px; color:var(--text-muted); font-style:italic; margin-top:3px; }
.dispute-detail { font-size:12px; color:var(--orange); margin-top:4px; }

.danger-zone {
  margin-top: 36px;
  border: 1px solid rgba(248,81,73,.2);
  border-radius: var(--r-lg);
  padding: 18px 20px;
  background: rgba(248,81,73,.04);
}
.danger-title {
  font-size: 11px;
  color: var(--red);
  text-transform: uppercase;
  letter-spacing: .06em;
  font-weight: 700;
  margin: 0 0 14px;
}
.danger-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.danger-desc { flex: 1; min-width: 200px; }
.danger-desc strong { color: var(--text-primary); font-size: 13px; }
.danger-desc p { color: var(--text-muted); font-size: 12px; margin: 4px 0 0; line-height: 1.5; }
.btn-delete-account {
  background: transparent;
  border: 1px solid var(--red);
  color: var(--red);
  border-radius: var(--r);
  padding: 7px 14px;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
  font-family: var(--font);
  transition: background var(--transition);
}
.btn-delete-account:hover { background: var(--red-muted); }

.modal-danger-title { margin: 0 0 10px; color: var(--red) !important; }
.delete-confirm-input {
  width: 100%;
  background: var(--bg-elevated);
  color: var(--text-primary);
  border: 1px solid rgba(248,81,73,.4);
  border-radius: var(--r);
  padding: 9px 12px;
  font-size: 13px;
  margin-top: 8px;
  box-sizing: border-box;
  font-family: var(--font);
  outline: none;
  transition: border-color var(--transition);
}
.delete-confirm-input:focus { border-color: var(--red); }

.modal-overlay { position:fixed; inset:0; background:rgba(0,0,0,.7); z-index:2000; display:flex; align-items:center; justify-content:center; }
.modal { background:var(--bg-surface); border:1px solid var(--border); border-radius:var(--r-xl); padding:26px 28px; width:360px; max-width:92vw; color:var(--text-primary); box-shadow:var(--shadow-lg); }
.modal h3 { margin:0 0 6px; font-size:16px; font-weight:700; }
.modal-subtitle { color:var(--text-secondary); font-size:13px; margin:0 0 14px; line-height:1.5; }
.reason-textarea {
  width:100%; background:var(--bg-elevated); color:var(--text-primary);
  border:1px solid var(--border); border-radius:var(--r); padding:9px 12px;
  font-size:13px; resize:vertical; font-family:var(--font); box-sizing:border-box; outline:none;
  transition:border-color var(--transition);
}
.reason-textarea::placeholder { color:var(--text-muted); }
.reason-textarea:focus { border-color:var(--brand); }
.modal-actions { display:flex; gap:8px; margin-top:14px; justify-content:flex-end; }
.btn-ghost   { background:var(--bg-elevated); color:var(--text-primary); border:1px solid var(--border); border-radius:var(--r); padding:7px 16px; cursor:pointer; font-family:var(--font); font-size:13px; transition:background var(--transition); }
.btn-ghost:hover { background:var(--bg-overlay); }
.btn-danger  { background:var(--red); color:#fff; border:none; border-radius:var(--r); padding:7px 16px; font-weight:600; cursor:pointer; font-family:var(--font); font-size:13px; transition:filter var(--transition); }
.btn-danger:hover:not(:disabled) { filter:brightness(1.1); }
.btn-danger:disabled { opacity:.4; cursor:default; }

.msg-error { color:var(--red);   font-size:12px; margin-top:8px; }
.msg-ok    { color:var(--brand); font-size:12px; margin-top:8px; }
</style>
