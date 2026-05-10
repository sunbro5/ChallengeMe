<template>
  <div class="player-page">
    <div v-if="loading" class="state-msg">{{ $t('common.loading') }}</div>
    <div v-else-if="error" class="state-msg error">{{ error }}</div>

    <template v-else>
      <!-- ── Profile header ──────────────────────────────────────────── -->
      <div class="profile-header">
        <div class="avatar">{{ profile.username[0].toUpperCase() }}</div>
        <div class="header-info">
          <div class="header-name-row">
            <h2>{{ profile.username }}</h2>
            <span class="rank-badge" :style="{ color: playerRank.color, borderColor: playerRank.color }">
              {{ $t(`rank.${playerRank.key}`) }}
            </span>
          </div>
          <p v-if="profile.isMe" class="me-badge">{{ $t('player.thatsYou') }}</p>
          <p v-if="profile.favoriteGameKey" class="fav-game">
            {{ gameIcon(profile.favoriteGameKey) }} {{ gameLabel(profile.favoriteGameKey) }}
          </p>
        </div>
      </div>

      <!-- ── Bio ──────────────────────────────────────────────────────── -->
      <div v-if="!profile.isMe && profile.bio" class="bio-box">
        <p>{{ profile.bio }}</p>
      </div>
      <div v-if="profile.isMe" class="bio-edit-section">
        <div v-if="!bioEditing">
          <p v-if="profile.bio" class="bio-box clickable" @click="startBioEdit">{{ profile.bio }}</p>
          <button v-else class="btn-edit-bio" @click="startBioEdit">{{ $t('player.addBio') }}</button>
        </div>
        <div v-else class="bio-edit-form">
          <textarea
            v-model="bioForm.bio"
            :placeholder="$t('player.bioPlaceholder')"
            maxlength="160"
            rows="3"
            class="bio-textarea"
          ></textarea>
          <div class="bio-game-row">
            <label>{{ $t('player.favoriteGame') }}</label>
            <select v-model="bioForm.favoriteGameKey" class="bio-game-select">
              <option value="">— {{ $t('player.noFavorite') }} —</option>
              <option v-for="g in games" :key="g.key" :value="g.key">{{ g.icon }} {{ gameLabel(g.key) }}</option>
            </select>
          </div>
          <div class="bio-actions">
            <button class="btn-save-bio" :disabled="bioSaving" @click="saveBio">
              {{ bioSaving ? $t('common.saving') : $t('common.submit') }}
            </button>
            <button class="btn-cancel-bio" @click="bioEditing = false">{{ $t('common.cancel') }}</button>
          </div>
        </div>
      </div>

      <!-- ── Stats row ───────────────────────────────────────────────── -->
      <div class="stats-row">
        <div class="stat">
          <span class="stat-value elo">{{ profile.rating || 1000 }}</span>
          <span class="stat-label">{{ $t('leaderboard.rating') }}</span>
        </div>
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
          <span class="stat-value disputes" :class="{ warning: disputeRate !== null && disputeRate > 25 }">
            {{ profile.disputes }}
          </span>
          <span class="stat-label">{{ $t('player.disputes') }}</span>
          <span v-if="disputeRate !== null && disputeRate > 0" class="dispute-rate-badge" :class="{ 'rate-warn': disputeRate > 25 }">
            {{ disputeRate }}%
          </span>
        </div>
      </div>

      <!-- ── Reputation score ──────────────────────────────────────── -->
      <div class="rep-section">
        <span class="rep-label">{{ $t('reputation.score') }}</span>
        <template v-if="profile.reputationScore != null">
          <span class="rep-stars">
            <span
              v-for="i in 5" :key="i"
              :class="['star', i <= profile.reputationScore ? 'filled' : 'empty']"
            >★</span>
          </span>
          <span class="rep-votes">({{ profile.thumbsUp }}👍 · {{ profile.thumbsDown }}👎)</span>
        </template>
        <span v-else class="rep-too-few">{{ $t('reputation.tooFew') }}</span>
      </div>

      <!-- ── ELO trend sparkline ────────────────────────────────────── -->
      <div v-if="ratingHistory.length >= 2" class="elo-trend-section">
        <h3 class="section-title">{{ $t('player.eloTrend') }}</h3>
        <div class="sparkline-wrap">
          <svg :viewBox="`0 0 ${sparkW} ${sparkH}`" class="sparkline" preserveAspectRatio="none">
            <polyline
              :points="sparkPoints"
              fill="none"
              stroke="var(--brand)"
              stroke-width="2"
              stroke-linejoin="round"
              stroke-linecap="round"
            />
          </svg>
          <div class="spark-labels">
            <span class="spark-start">{{ ratingHistory[0].rating }}</span>
            <span class="spark-end" :class="trendClass">{{ ratingHistory[ratingHistory.length - 1].rating }}</span>
          </div>
        </div>
      </div>

      <!-- ── Achievements ───────────────────────────────────────────── -->
      <div v-if="profile.achievements && profile.achievements.length" class="achievements-section">
        <h3 class="section-title">{{ $t('achievements.title') }}</h3>
        <div class="achievements-grid">
          <div
            v-for="a in profile.achievements"
            :key="a.type"
            class="achievement-badge"
            :title="formatDate(a.awardedAt)"
          >
            {{ $t('achievements.' + a.type) }}
          </div>
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

        <button
          v-if="!isBlocked"
          class="btn-challenge"
          @click="showChallengeModal = true"
        >{{ $t('player.challenge') }}</button>

        <button class="btn-report" @click="reportModal.visible = true">{{ $t('player.report') }}</button>

        <button
          v-if="!isBlocked"
          class="btn-block"
          @click="blockUser"
          :disabled="actionBusy"
        >{{ $t('player.block') }}</button>
        <button
          v-else
          class="btn-unblock"
          @click="unblockUser"
          :disabled="actionBusy"
        >{{ $t('player.unblock') }}</button>
      </div>

      <p v-if="actionError" class="msg-error">{{ actionError }}</p>

      <!-- ── Stats by game type ────────────────────────────────────────── -->
      <div v-if="profile.gameStats && profile.gameStats.length" class="game-stats-section">
        <h3 class="section-title">{{ $t('player.statsByGame') }}</h3>
        <div class="game-stats-list">
          <div v-for="s in profile.gameStats" :key="s.gameType" class="game-stat-row">
            <span class="gs-icon">{{ gameIcon(s.gameType) }}</span>
            <span class="gs-name">{{ gameLabel(s.gameType) }}</span>
            <span class="gs-badges">
              <span v-if="s.wins"     class="gs-badge win">{{ s.wins }}W</span>
              <span v-if="s.losses"   class="gs-badge loss">{{ s.losses }}L</span>
              <span v-if="s.draws"    class="gs-badge draw">{{ s.draws }}D</span>
              <span v-if="s.disputes" class="gs-badge disp">{{ s.disputes }}S</span>
            </span>
          </div>
        </div>
      </div>

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

    <!-- ── Challenge modal ──────────────────────────────────────────────── -->
    <ChallengeModal
      v-if="showChallengeModal && profile"
      :targetUsername="profile.username"
      @close="showChallengeModal = false"
    />

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
import { getRank } from '../utils/rank.js'
import ChallengeModal from './ChallengeModal.vue'

export default {
  name: 'PlayerPage',
  components: { ChallengeModal },
  data() {
    return {
      loading: true,
      error: '',
      profile: null,
      gameMeta: {},
      games: [],
      isLoggedIn: !!localStorage.getItem('isLoggedIn'),
      actionBusy: false,
      actionError: '',
      isBlocked: false,
      bioEditing: false,
      bioSaving: false,
      bioForm: { bio: '', favoriteGameKey: '' },
      reportModal: { visible: false, reason: '', message: '', success: false, busy: false },
      deleteAccountModal: false,
      deleteConfirmText: '',
      deleteAccountBusy: false,
      deleteAccountError: '',
      showChallengeModal: false,
      ratingHistory: [],
      sparkW: 300,
      sparkH: 60,
    }
  },
  async mounted() {
    await Promise.all([this.loadProfile(), this.loadGames()])
    if (this.isLoggedIn && this.profile && !this.profile.isMe) {
      this.loadBlockStatus()
    }
    this.loadRatingHistory()
  },
  computed: {
    sparkPoints() {
      if (this.ratingHistory.length < 2) return ''
      const vals = this.ratingHistory.map(p => p.rating)
      const minV = Math.min(...vals)
      const maxV = Math.max(...vals)
      const range = maxV - minV || 1
      const pad = 4
      return vals.map((v, i) => {
        const x = pad + (i / (vals.length - 1)) * (this.sparkW - pad * 2)
        const y = pad + (1 - (v - minV) / range) * (this.sparkH - pad * 2)
        return `${x.toFixed(1)},${y.toFixed(1)}`
      }).join(' ')
    },
    trendClass() {
      if (this.ratingHistory.length < 2) return ''
      const first = this.ratingHistory[0].rating
      const last  = this.ratingHistory[this.ratingHistory.length - 1].rating
      return last > first ? 'trend-up' : last < first ? 'trend-down' : 'trend-flat'
    },
    playerRank() {
      return getRank(this.profile?.rating || 1000)
    },
    // Dispute rate as percentage (0–100). null when no games played yet.
    disputeRate() {
      if (!this.profile) return null
      const total = this.profile.wins + this.profile.losses + this.profile.draws
      if (!total) return null
      return Math.round((this.profile.disputes / total) * 100)
    },
  },
  watch: {
    '$route.params.username'() { this.loadProfile(); this.loadRatingHistory() },
  },
  methods: {
    async loadRatingHistory() {
      try {
        const username = this.$route.params.username
        const { data } = await axios.get(
          `/api/players/${username}/rating-history`,
          { withCredentials: true })
        this.ratingHistory = data
      } catch { /* non-fatal */ }
    },

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

    async loadGames() {
      try {
        const { data } = await axios.get('/api/games', { withCredentials: true })
        this.games = data
        const meta = {}
        data.forEach(g => {
          const locale = this.$i18n.locale
          meta[g.key] = {
            icon: g.icon,
            label: (locale === 'cs' ? g.nameCs : g.nameEn) || g.nameEn || g.nameCs || g.key,
          }
        })
        this.gameMeta = meta
      } catch { /* non-fatal */ }
    },

    async loadBlockStatus() {
      try {
        const { data } = await axios.get(`/api/blocks/${this.profile.username}`, { withCredentials: true })
        this.isBlocked = data
      } catch { /* non-fatal */ }
    },
    async blockUser() {
      this.actionBusy = true
      try {
        await axios.post(`/api/blocks/${this.profile.username}`, {}, { withCredentials: true })
        this.isBlocked = true
      } catch (e) {
        this.actionError = e.response?.data || 'Could not block user'
      } finally { this.actionBusy = false }
    },
    async unblockUser() {
      this.actionBusy = true
      try {
        await axios.delete(`/api/blocks/${this.profile.username}`, { withCredentials: true })
        this.isBlocked = false
      } catch (e) {
        this.actionError = e.response?.data || 'Could not unblock user'
      } finally { this.actionBusy = false }
    },
    startBioEdit() {
      this.bioForm.bio = this.profile.bio || ''
      this.bioForm.favoriteGameKey = this.profile.favoriteGameKey || ''
      this.bioEditing = true
    },
    async saveBio() {
      this.bioSaving = true
      try {
        await axios.put('/api/players/me/profile', {
          bio: this.bioForm.bio,
          favoriteGameKey: this.bioForm.favoriteGameKey,
        }, { withCredentials: true })
        this.profile.bio = this.bioForm.bio || null
        this.profile.favoriteGameKey = this.bioForm.favoriteGameKey || null
        this.bioEditing = false
      } catch { /* non-fatal */ } finally {
        this.bioSaving = false
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

    gameIcon(t)    { return this.gameMeta[t]?.icon  || '🎮' },
    gameLabel(t)   { return this.gameMeta[t]?.label || t },
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
.header-name-row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.header-info h2  { margin: 0; font-size: 20px; font-weight: 700; letter-spacing: -.01em; }
.rank-badge {
  border: 1px solid;
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: .04em;
  opacity: .9;
  flex-shrink: 0;
}
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

.btn-block {
  background: transparent; border: 1px solid rgba(248,81,73,.35); color: var(--red);
  border-radius: var(--r); padding: 5px 13px; font-size: 12px; font-family: var(--font);
  cursor: pointer; transition: background var(--transition);
}
.btn-block:hover { background: var(--red-muted); }
.btn-unblock {
  background: var(--bg-elevated); border: 1px solid var(--border); color: var(--text-muted);
  border-radius: var(--r); padding: 5px 13px; font-size: 12px; font-family: var(--font);
  cursor: pointer; transition: background var(--transition);
}
.btn-unblock:hover { background: var(--bg-overlay); }

.fav-game { margin: 2px 0 0; font-size: 12px; color: var(--text-secondary); }

.bio-box {
  background: var(--bg-surface); border: 1px solid var(--border); border-radius: var(--r-md);
  padding: 12px 16px; margin-bottom: 16px; font-size: 13px; color: var(--text-secondary);
  line-height: 1.6; white-space: pre-wrap;
}
.bio-box.clickable { cursor: pointer; }
.bio-box.clickable:hover { background: var(--bg-elevated); }

.bio-edit-section { margin-bottom: 16px; }
.btn-edit-bio {
  background: transparent; border: 1px dashed var(--border); color: var(--text-muted);
  border-radius: var(--r); padding: 7px 14px; font-size: 12px; font-family: var(--font);
  cursor: pointer; width: 100%; transition: border-color var(--transition), color var(--transition);
}
.btn-edit-bio:hover { border-color: var(--brand); color: var(--brand); }

.bio-edit-form { display: flex; flex-direction: column; gap: 8px; }
.bio-textarea {
  width: 100%; padding: 9px 12px; background: var(--bg-elevated); color: var(--text-primary);
  border: 1px solid var(--border); border-radius: var(--r); font-size: 13px; resize: vertical;
  font-family: var(--font); box-sizing: border-box; outline: none;
  transition: border-color var(--transition);
}
.bio-textarea::placeholder { color: var(--text-muted); }
.bio-textarea:focus { border-color: var(--brand); }

.bio-game-row { display: flex; flex-direction: column; gap: 4px; }
.bio-game-row label { font-size: 11px; color: var(--text-secondary); text-transform: uppercase; letter-spacing: .04em; }
.bio-game-select {
  background: var(--bg-elevated); color: var(--text-primary); border: 1px solid var(--border);
  border-radius: var(--r); padding: 7px 10px; font-size: 13px; font-family: var(--font); outline: none;
}
.bio-game-select:focus { border-color: var(--brand); }

.bio-actions { display: flex; gap: 8px; }
.btn-save-bio {
  background: var(--brand); color: #fff; border: none; border-radius: var(--r);
  padding: 7px 16px; font-size: 13px; font-weight: 600; font-family: var(--font);
  cursor: pointer; transition: background var(--transition);
}
.btn-save-bio:hover:not(:disabled) { background: var(--brand-hover); }
.btn-save-bio:disabled { opacity: .5; cursor: default; }
.btn-cancel-bio {
  background: var(--bg-elevated); color: var(--text-secondary); border: 1px solid var(--border);
  border-radius: var(--r); padding: 7px 14px; font-size: 13px; font-family: var(--font);
  cursor: pointer; transition: background var(--transition);
}
.btn-cancel-bio:hover { background: var(--bg-overlay); }

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
.stats-row         { grid-template-columns: repeat(5, 1fr); }
.stat-value         { font-size: 24px; font-weight: 700; line-height: 1; }
.stat-value.elo     { color: var(--blue, #4f8ef7); }
.stat-value.wins    { color: var(--brand); }
.stat-value.losses  { color: var(--red); }
.stat-value.draws   { color: var(--yellow); }
.stat-value.disputes { color: var(--text-muted); }
.stat-value.disputes.warning { color: var(--red); }

.dispute-rate-badge {
  display: block;
  font-size: 10px;
  font-weight: 600;
  color: var(--text-muted);
  margin-top: 1px;
}
.dispute-rate-badge.rate-warn { color: var(--red); }
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
.btn-challenge {
  background: var(--brand);
  color: #fff;
  border: none;
  border-radius: var(--r);
  padding: 7px 16px;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  font-family: var(--font);
  transition: background var(--transition);
}
.btn-challenge:hover { background: var(--brand-hover); }

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

.rep-section {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 13px;
}
.rep-label    { color: var(--text-secondary); font-size: 11px; text-transform: uppercase; letter-spacing: .04em; font-weight: 600; }
.rep-stars    { display: flex; gap: 1px; }
.star         { font-size: 16px; line-height: 1; }
.star.filled  { color: #c9a227; }
.star.empty   { color: var(--border); }
.rep-votes    { font-size: 12px; color: var(--text-muted); }
.rep-too-few  { font-size: 12px; color: var(--text-muted); font-style: italic; }

.elo-trend-section { margin-bottom: 28px; }
.sparkline-wrap {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--r-lg);
  padding: 12px 16px;
}
.sparkline {
  display: block;
  width: 100%;
  height: 60px;
}
.spark-labels {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
}
.spark-end.trend-up   { color: var(--brand); font-weight: 700; }
.spark-end.trend-down { color: var(--red);   font-weight: 700; }
.spark-end.trend-flat { color: var(--text-muted); }

.achievements-section { margin-bottom: 28px; }
.achievements-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.achievement-badge {
  background: var(--brand-muted);
  color: var(--brand);
  border: 1px solid rgba(66,184,131,.3);
  border-radius: var(--r-full);
  padding: 5px 14px;
  font-size: 13px;
  font-weight: 600;
  cursor: default;
  transition: background var(--transition);
}
.achievement-badge:hover { background: rgba(66,184,131,.2); }

.game-stats-section { margin-bottom: 28px; }
.game-stats-list { display: flex; flex-direction: column; gap: 6px; }
.game-stat-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 14px;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--r);
}
.gs-icon  { font-size: 18px; flex-shrink: 0; }
.gs-name  { font-size: 13px; font-weight: 500; color: var(--text-primary); flex: 1; }
.gs-badges { display: flex; gap: 5px; flex-wrap: wrap; }
.gs-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: var(--r-full);
  letter-spacing: .03em;
}
.gs-badge.win  { background: var(--brand-muted);  color: var(--brand); }
.gs-badge.loss { background: var(--red-muted);    color: var(--red); }
.gs-badge.draw { background: var(--yellow-muted); color: var(--yellow); }
.gs-badge.disp { background: var(--orange-muted); color: var(--orange); }

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

@media (max-width: 640px) {
  .player-page { padding: 20px 14px 76px; }
  .stats-grid  { grid-template-columns: repeat(2, 1fr); }
  .profile-header { gap: 12px; }
  .avatar { width: 52px; height: 52px; font-size: 1.3rem; }
  .header-info h2 { font-size: 17px; }
}
</style>
