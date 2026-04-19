<template>
  <div class="my-games-page">
    <h2 class="page-title">{{ $t('myGames.title') }}</h2>

    <div v-if="loading" class="state-msg">{{ $t('common.loading') }}</div>
    <div v-else-if="error" class="state-msg error">{{ error }}</div>

    <template v-else>
      <div v-if="!events.length" class="empty">
        <p>{{ $t('myGames.noGames') }}</p>
        <router-link to="/map" class="btn-go-map">{{ $t('myGames.goToMap') }}</router-link>
      </div>

      <div v-else class="events-list">
        <div
          v-for="ev in events"
          :key="ev.id"
          class="event-card"
          :class="ev.status.toLowerCase()"
        >
          <!-- Top row: game + status badge -->
          <div class="card-top">
            <span class="game-label">{{ gameLabel(ev.gameType) }}</span>
            <span class="status-badge" :class="ev.status.toLowerCase()">
              {{ statusLabel(ev.status) }}
            </span>
          </div>

          <!-- Details row -->
          <div class="card-meta">
            <span>📅 {{ formatDate(ev.scheduledAt) }}</span>
            <a :href="mapsLink(ev)" target="_blank" class="map-link">📍 {{ $t('common.openInMaps') }}</a>
          </div>
          <div v-if="ev.locationName" class="location-name">📍 {{ ev.locationName }}</div>

          <!-- Challenger section -->
          <div class="challenger-section">

            <div v-if="ev.status === 'OPEN'" class="waiting">
              {{ $t('myGames.waitingForAccept') }}
            </div>

            <template v-else-if="ev.status === 'PENDING_APPROVAL'">
              <!-- Creator sees approve/reject -->
              <template v-if="ev.isCreator">
                <div class="challenger-info">
                  <router-link :to="`/player/${ev.challengerUsername}`" class="challenger-link">
                    {{ $t('myGames.wantsToPlay', { username: ev.challengerUsername }) }}
                  </router-link>
                </div>
                <div class="approval-actions">
                  <button class="btn-approve" :disabled="busy[ev.id]" @click="approve(ev)">{{ $t('common.approve') }}</button>
                  <button class="btn-reject"  :disabled="busy[ev.id]" @click="reject(ev)">{{ $t('common.reject') }}</button>
                </div>
              </template>
              <!-- Challenger waits -->
              <div v-else class="waiting">
                {{ $t('myGames.waitingApproval', { username: ev.creatorUsername }) }}
              </div>
            </template>

            <template v-else-if="ev.status === 'IN_PROGRESS'">
              <div class="inprogress-info">
                {{ $t('myGames.playing', { username: opponentName(ev) }) }}
              </div>
              <div class="submit-status">
                <span :class="ev.iHaveSubmitted ? 'done' : 'pending'">
                  {{ ev.iHaveSubmitted ? $t('myGames.youSubmitted') : $t('myGames.youNotYet') }}
                </span>
                <span :class="opponentSubmitted(ev) ? 'done' : 'pending'">
                  {{ opponentName(ev) }}: {{ opponentSubmitted(ev) ? $t('myGames.submitted') : $t('myGames.notYet') }}
                </span>
              </div>
            </template>

            <div v-else-if="ev.status === 'FINISHED'" class="result-info finished">
              <span v-if="ev.winnerUsername">
                {{ $t('myGames.won', { username: ev.winnerUsername }) }}
              </span>
              <span v-else>{{ $t('common.draw') }}</span>
              <span v-if="ev.resultNote" class="result-note">"{{ ev.resultNote }}"</span>
            </div>

            <div v-else-if="ev.status === 'DISPUTED'" class="result-info disputed">
              {{ $t('common.disputedResult') }} — {{ $t('common.scoresNotCounted') }}
            </div>

            <div v-else-if="ev.status === 'CANCELLED'" class="result-info cancelled">
              ✕ {{ $t('status.cancelled') }}
            </div>

          </div>

          <button
            v-if="ev.isCreator && ['OPEN','PENDING_APPROVAL','IN_PROGRESS'].includes(ev.status)"
            class="btn-cancel"
            :disabled="busy[ev.id]"
            @click="cancel(ev)"
          >{{ $t('myGames.cancelEventBtn') }}</button>

          <router-link :to="`/event/${ev.id}`" class="btn-detail">{{ $t('common.fullDetail') }}</router-link>

          <p v-if="errors[ev.id]" class="card-error">{{ errors[ev.id] }}</p>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'MyGamesPage',
  data() {
    return {
      events:  [],
      loading: true,
      error:   '',
      busy:    {},
      errors:  {},
      gameMeta: {},
    }
  },
  async mounted() {
    await this.loadGames()
    await this.loadMyEvents()
  },
  methods: {
    async loadGames() {
      try {
        const { data } = await axios.get('/api/games', { withCredentials: true })
        data.forEach(g => { this.gameMeta[g.key] = { icon: g.icon, nameCs: g.nameCs, nameEn: g.nameEn } })
      } catch { /* non-fatal */ }
    },

    async loadMyEvents() {
      this.loading = true
      try {
        const { data } = await axios.get('/api/events/mine', { withCredentials: true })
        this.events = data
      } catch (e) {
        this.error = this.$t('myGames.couldNotLoad')
      } finally {
        this.loading = false
      }
    },

    async approve(ev) {
      this.busy  = { ...this.busy,  [ev.id]: true  }
      this.errors = { ...this.errors, [ev.id]: '' }
      try {
        const { data } = await axios.post(
          `/api/events/${ev.id}/approve`, {}, { withCredentials: true })
        this.replaceEvent(data)
      } catch (err) {
        this.errors = { ...this.errors, [ev.id]: err.response?.data || 'Failed.' }
      } finally {
        this.busy = { ...this.busy, [ev.id]: false }
      }
    },

    async reject(ev) {
      this.busy  = { ...this.busy,  [ev.id]: true  }
      this.errors = { ...this.errors, [ev.id]: '' }
      try {
        const { data } = await axios.post(
          `/api/events/${ev.id}/reject`, {}, { withCredentials: true })
        this.replaceEvent(data)
      } catch (err) {
        this.errors = { ...this.errors, [ev.id]: err.response?.data || 'Failed.' }
      } finally {
        this.busy = { ...this.busy, [ev.id]: false }
      }
    },

    async cancel(ev) {
      if (!confirm(this.$t('myGames.cancelConfirm'))) return
      this.busy  = { ...this.busy,  [ev.id]: true  }
      this.errors = { ...this.errors, [ev.id]: '' }
      try {
        await axios.delete(`/api/events/${ev.id}`, { withCredentials: true })
        this.replaceEvent({ ...ev, status: 'CANCELLED' })
      } catch (err) {
        this.errors = { ...this.errors, [ev.id]: err.response?.data || 'Failed.' }
      } finally {
        this.busy = { ...this.busy, [ev.id]: false }
      }
    },

    replaceEvent(updated) {
      this.events = this.events.map(e => e.id === updated.id ? updated : e)
    },

    gameLabel(type) {
      const m = this.gameMeta[type]
      if (!m) return type || ''
      const name = this.$i18n?.locale === 'cs' ? m.nameCs : m.nameEn
      return `${m.icon} ${name}`
    },
    opponentName(ev) {
      return ev.isCreator ? ev.challengerUsername : ev.creatorUsername
    },
    opponentSubmitted(ev) {
      return ev.isCreator ? ev.challengerResultSubmitted : ev.creatorResultSubmitted
    },
    statusLabel(s) { return this.$t('status.' + s.toLowerCase()) },
    formatDate(iso) { return iso ? new Date(iso).toLocaleString() : '—' },
    mapsLink(ev)    { return `https://www.google.com/maps?q=${ev.latitude},${ev.longitude}` },
  },
}
</script>

<style scoped>
.my-games-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 28px 20px 60px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 24px;
  letter-spacing: -.01em;
}

.state-msg       { text-align: center; padding: 60px; color: var(--text-muted); font-size: 14px; }
.state-msg.error { color: var(--red); }

.empty {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-muted);
}
.empty p { margin-bottom: 16px; font-size: 14px; }
.btn-go-map {
  display: inline-block;
  background: var(--brand);
  color: #fff;
  padding: 9px 22px;
  border-radius: var(--r);
  text-decoration: none;
  font-weight: 600;
  font-size: 13px;
  transition: background var(--transition);
}
.btn-go-map:hover { background: var(--brand-hover); }

.events-list { display: flex; flex-direction: column; gap: 10px; }

.event-card {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-left: 4px solid var(--border);
  border-radius: var(--r-md);
  padding: 16px 18px;
  transition: border-left-color var(--transition);
}
.event-card.open             { border-left-color: var(--brand); }
.event-card.pending_approval { border-left-color: var(--yellow); }
.event-card.in_progress      { border-left-color: var(--blue); }
.event-card.finished         { border-left-color: var(--text-secondary); }
.event-card.disputed         { border-left-color: var(--orange); }
.event-card.cancelled        { border-left-color: var(--border); opacity: .65; }

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.game-label { font-size: 14px; font-weight: 600; color: var(--text-primary); }

.status-badge {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .05em;
  padding: 2px 9px;
  border-radius: var(--r-full);
}
.status-badge.open             { background: var(--brand-muted);  color: var(--brand); }
.status-badge.pending_approval { background: var(--yellow-muted); color: var(--yellow); }
.status-badge.in_progress      { background: var(--blue-muted);   color: var(--blue); }
.status-badge.finished         { background: rgba(139,148,158,.1); color: var(--text-secondary); }
.status-badge.disputed         { background: var(--orange-muted); color: var(--orange); }
.status-badge.cancelled        { background: rgba(139,148,158,.08); color: var(--text-muted); }

.card-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 12px;
}
.map-link { color: var(--blue); text-decoration: none; }
.location-name { font-size: 12px; color: var(--text-secondary); margin-bottom: 8px; }
.map-link:hover { text-decoration: underline; }

.challenger-section { margin-bottom: 12px; }

.waiting { font-size: 12px; color: var(--yellow); background: var(--yellow-muted); border-radius: var(--r); padding: 7px 12px; }

.challenger-info { margin-bottom: 8px; font-size: 13px; color: var(--text-primary); }
.challenger-link { color: var(--blue); text-decoration: none; font-size: 13px; }
.challenger-link:hover { text-decoration: underline; }

.approval-actions { display: flex; gap: 8px; }
.btn-approve {
  background: var(--brand); color: #fff; border: none;
  border-radius: var(--r); padding: 6px 16px; font-weight: 600; cursor: pointer; font-size: 12px;
  font-family: var(--font); transition: background var(--transition);
}
.btn-approve:hover:not(:disabled) { background: var(--brand-hover); }
.btn-reject {
  background: transparent; color: var(--red); border: 1px solid rgba(248,81,73,.4);
  border-radius: var(--r); padding: 6px 16px; cursor: pointer; font-size: 12px;
  font-family: var(--font); transition: background var(--transition);
}
.btn-reject:hover:not(:disabled) { background: var(--red-muted); }

.inprogress-info { font-size: 12px; color: var(--blue); margin-bottom: 6px; }
.submit-status { display: flex; gap: 14px; font-size: 12px; }
.submit-status .done    { color: var(--brand); }
.submit-status .pending { color: var(--text-muted); }

.result-info { font-size: 13px; display: flex; align-items: center; gap: 10px; flex-wrap: wrap; color: var(--text-primary); }
.result-info.disputed  { color: var(--orange); }
.result-info.cancelled { color: var(--text-muted); }
.result-note { color: var(--text-muted); font-style: italic; font-size: 12px; }

.btn-cancel {
  margin-top: 4px;
  background: transparent;
  border: 1px solid rgba(248,81,73,.3);
  color: var(--red);
  border-radius: var(--r);
  padding: 4px 12px;
  font-size: 12px;
  cursor: pointer;
  font-family: var(--font);
  transition: background var(--transition);
}
.btn-cancel:hover:not(:disabled) { background: var(--red-muted); }
.btn-cancel:disabled { opacity: .5; cursor: default; }
.btn-approve:disabled, .btn-reject:disabled { opacity: .5; cursor: default; }

.btn-detail {
  display: inline-block;
  margin-top: 8px;
  font-size: 12px;
  color: var(--blue);
  text-decoration: none;
}
.btn-detail:hover { text-decoration: underline; }

.card-error { color: var(--red); font-size: 12px; margin-top: 6px; }

@media (max-width: 640px) {
  .my-games-page { padding: 20px 14px 76px; }
  .event-card    { padding: 14px; }
}
</style>
