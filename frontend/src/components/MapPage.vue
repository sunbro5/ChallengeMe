<template>
  <div class="map-wrapper">
    <div id="map" ref="mapEl"></div>

    <!-- ── Create-challenge panel ──────────────────────────────────────── -->
    <transition name="panel-slide">
      <div v-if="createPanel.visible" class="side-panel">
        <h3>{{ $t('map.postChallenge') }}</h3>
        <p class="coords">{{ createPanel.lat.toFixed(5) }}, {{ createPanel.lng.toFixed(5) }}</p>

        <label>
          {{ $t('map.game') }}
          <router-link to="/games" target="_blank" class="rules-link" :title="$t('map.rules')">ℹ️</router-link>
        </label>
        <select v-model="createPanel.gameType">
          <option v-for="g in games" :key="g.key" :value="g.key">
            {{ g.icon }} {{ g.name }}
          </option>
        </select>

        <label>{{ $t('map.whenToMeet') }}</label>
        <input type="datetime-local" v-model="createPanel.scheduledAt" />

        <label>{{ $t('map.description') }} <span style="font-size:.7rem;color:#6c7086">({{ $t('map.descriptionHint') }})</span></label>
        <textarea
          v-model="createPanel.description"
          :placeholder="$t('map.descriptionPlaceholder')"
          rows="3"
        ></textarea>

        <div class="safety-note">
          <span class="safety-icon">⚠️</span>
          <span>{{ $t('safety.createWarning') }}</span>
        </div>

        <div class="panel-actions">
          <button class="btn-primary" @click="submitCreate" :disabled="createPanel.busy">
            {{ createPanel.busy ? $t('map.posting') : $t('map.postChallengeBtn') }}
          </button>
          <button class="btn-ghost" @click="cancelCreate">{{ $t('common.cancel') }}</button>
        </div>
        <p v-if="createPanel.error" class="msg-error">{{ createPanel.error }}</p>
      </div>
    </transition>

    <!-- ── Event-detail panel ──────────────────────────────────────────── -->
    <transition name="panel-slide">
      <div v-if="detailPanel.visible && detailPanel.event" class="side-panel detail">
        <button class="close-btn" @click="detailPanel.visible = false">✕</button>

        <span class="status-badge" :class="detailPanel.event.status.toLowerCase()">
          {{ statusLabel(detailPanel.event.status) }}
        </span>

        <h3>
          {{ gameLabel(detailPanel.event.gameType) }}
          <router-link to="/games" target="_blank" class="rules-link" :title="$t('map.rules')">ℹ️</router-link>
        </h3>

        <p>
          <span class="lbl">{{ $t('nav.map').replace('🗺️ ', '') }}</span>
          <router-link :to="`/player/${detailPanel.event.creatorUsername}`" class="player-link">
            {{ detailPanel.event.creatorUsername }}
          </router-link>
        </p>
        <p><span class="lbl">{{ $t('map.whenToMeet').slice(0,4) }}</span> {{ formatDate(detailPanel.event.scheduledAt) }}</p>
        <p v-if="detailPanel.event.description" class="event-description">
          {{ detailPanel.event.description }}
        </p>
        <p>
          <span class="lbl"></span>
          <a :href="mapsLink(detailPanel.event)" target="_blank" class="map-link">{{ $t('common.openInMaps') }}</a>
        </p>

        <div class="participants">
          <router-link
            v-for="p in detailPanel.event.participants" :key="p"
            :to="`/player/${p}`"
            class="participant-tag"
          >👤 {{ p }}</router-link>
          <span v-if="detailPanel.event.participants.length === 1" class="participant-tag empty">
            {{ $t('common.waitingForChallenger') }}
          </span>
        </div>

        <!-- ── FINISHED ──────────────────────────────────────────────── -->
        <div v-if="detailPanel.event.status === 'FINISHED'" class="result-box">
          <div v-if="detailPanel.event.winnerUsername" class="result-win">
            🏆 <strong>{{ detailPanel.event.winnerUsername }}</strong> {{ $t('map.won', { username: '' }).replace(' ', '') }}
          </div>
          <div v-else class="result-draw">{{ $t('common.draw') }}</div>
          <div v-if="detailPanel.event.resultNote" class="result-note">
            "{{ detailPanel.event.resultNote }}"
          </div>
        </div>

        <!-- ── DISPUTED ──────────────────────────────────────────────── -->
        <div v-if="detailPanel.event.status === 'DISPUTED'" class="dispute-box">
          <p class="dispute-title">{{ $t('common.disputedResult') }}</p>
          <p class="dispute-row">
            <strong>{{ detailPanel.event.creatorUsername }}</strong> said:
            <span class="claim">{{ detailPanel.event.creatorResult || 'draw' }}</span>
          </p>
          <p class="dispute-row">
            <strong>{{ detailPanel.event.challengerUsername }}</strong> said:
            <span class="claim">{{ detailPanel.event.challengerResult || 'draw' }}</span>
          </p>
          <p class="dispute-note">{{ $t('common.scoresNotCounted') }}</p>
        </div>

        <!-- ── Actions (logged-in users) ────────────────────────────── -->
        <template v-if="isLoggedIn">

          <button
            v-if="detailPanel.event.status === 'OPEN' && !detailPanel.event.joined"
            class="btn-primary full"
            @click="applyChallenge"
            :disabled="detailPanel.busy"
          >{{ detailPanel.busy ? $t('map.applying') : $t('map.acceptChallenge') }}</button>

          <div v-if="detailPanel.event.status === 'OPEN' && detailPanel.event.isCreator"
               class="waiting-note">
            {{ $t('map.waitingApply') }}
          </div>

          <template v-if="detailPanel.event.status === 'PENDING_APPROVAL' && detailPanel.event.isCreator">
            <p class="approval-note">
              {{ $t('map.wantsToPlay', { username: detailPanel.event.challengerUsername }) }}
            </p>
            <div class="panel-actions">
              <button class="btn-success flex1" @click="approveChallenger" :disabled="detailPanel.busy">
                {{ $t('common.approve') }}
              </button>
              <button class="btn-danger flex1" @click="rejectChallenger" :disabled="detailPanel.busy">
                {{ $t('common.reject') }}
              </button>
            </div>
          </template>

          <div v-if="detailPanel.event.status === 'PENDING_APPROVAL' && detailPanel.event.isChallenger"
               class="waiting-note">
            {{ $t('map.waitingApproval', { username: detailPanel.event.creatorUsername }) }}
          </div>

          <div v-if="detailPanel.event.status === 'IN_PROGRESS' && detailPanel.event.joined"
               class="meetup-box">
            <p>{{ $t('map.headToPin') }}</p>
            <div class="safety-inline">⚠️ {{ $t('safety.meetupWarning') }}</div>

            <div class="submit-status">
              <span :class="detailPanel.event.creatorResultSubmitted ? 'submitted' : 'pending-submit'">
                {{ detailPanel.event.creatorUsername }}:
                {{ detailPanel.event.creatorResultSubmitted ? '✅ submitted' : '⏳ not yet' }}
              </span>
              <span :class="detailPanel.event.challengerResultSubmitted ? 'submitted' : 'pending-submit'">
                {{ detailPanel.event.challengerUsername }}:
                {{ detailPanel.event.challengerResultSubmitted ? '✅ submitted' : '⏳ not yet' }}
              </span>
            </div>

            <button
              v-if="!detailPanel.event.iHaveSubmitted"
              class="btn-success full"
              @click="openResultModal"
            >{{ $t('map.submitMyResult') }}</button>
            <div v-else class="my-submitted-note">
              {{ $t('map.resultIn') }}
            </div>
          </div>

          <button
            v-if="detailPanel.event.isCreator
                  && detailPanel.event.status !== 'FINISHED'
                  && detailPanel.event.status !== 'DISPUTED'
                  && detailPanel.event.status !== 'CANCELLED'"
            class="btn-danger-outline full"
            @click="cancelEvent"
          >{{ $t('map.cancelEvent') }}</button>

        </template>
        <p v-else-if="detailPanel.event.status === 'OPEN'" class="hint">
          {{ $t('map.loginToAccept') }}
        </p>

        <router-link
          :to="`/event/${detailPanel.event.id}`"
          class="btn-detail full"
        >{{ $t('common.fullDetail') }}</router-link>

        <p v-if="detailPanel.error" class="msg-error">{{ detailPanel.error }}</p>
      </div>
    </transition>

    <!-- ── Result modal ────────────────────────────────────────────────── -->
    <div v-if="resultModal.visible" class="modal-overlay" @click.self="resultModal.visible = false">
      <div class="modal">
        <h3>{{ $t('map.yourResult') }}</h3>
        <p class="modal-subtitle">
          {{ gameLabel(detailPanel.event?.gameType) }} —
          {{ $t('map.submitWhoWon') }}
        </p>

        <label>{{ $t('map.whoWon') }}</label>
        <div class="radio-group">
          <label
            v-for="p in detailPanel.event?.participants" :key="p"
            class="radio-opt"
          >
            <input type="radio" :value="p" v-model="resultModal.winner" />
            {{ p }}
          </label>
          <label class="radio-opt">
            <input type="radio" value="" v-model="resultModal.winner" />
            {{ $t('common.draw') }}
          </label>
        </div>

        <label>{{ $t('map.note') }} <span class="optional">({{ $t('common.optional') }})</span></label>
        <input
          type="text"
          v-model="resultModal.note"
          :placeholder="$t('map.notePlaceholder')"
          class="text-input"
        />

        <div class="modal-actions">
          <button class="btn-primary" @click="submitResult" :disabled="resultModal.busy">
            {{ resultModal.busy ? $t('common.saving') : $t('common.submit') }}
          </button>
          <button class="btn-ghost" @click="resultModal.visible = false">{{ $t('common.cancel') }}</button>
        </div>
        <p v-if="resultModal.error" class="msg-error">{{ resultModal.error }}</p>
      </div>
    </div>

    <div v-if="!isLoggedIn" class="map-hint">{{ $t('map.loginToPost') }}</div>
    <div v-else class="map-hint">{{ $t('map.clickToPost') }}</div>
  </div>
</template>

<script>
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { markRaw } from 'vue'
import axios from 'axios'

const GAME_META_FALLBACK = { icon: '🎮', color: '#42b883' }
let GAME_META_CACHE = {}

function makeIcon(gameType, status) {
  const meta = GAME_META_CACHE[gameType] || GAME_META_FALLBACK
  const dim  = status === 'IN_PROGRESS' || status === 'PENDING_APPROVAL'
  return L.divIcon({
    className: '',
    html: `<div style="
      background:${dim ? '#6c7086' : meta.color};
      border:2px solid #fff;border-radius:50%;
      width:40px;height:40px;
      display:flex;align-items:center;justify-content:center;
      font-size:20px;
      box-shadow:0 2px 8px rgba(0,0,0,.45);
      cursor:pointer;opacity:${dim ? .75 : 1};">${meta.icon}</div>`,
    iconSize: [40, 40], iconAnchor: [20, 20],
  })
}

export default {
  name: 'MapPage',
  data() {
    return {
      map: null,
      markers: {},
      tempMarker: null,
      isLoggedIn: !!localStorage.getItem('isLoggedIn'),

      games: [],
      createPanel: { visible: false, lat: 0, lng: 0, gameType: '', scheduledAt: '', description: '', busy: false, error: '' },
      detailPanel: { visible: false, event: null, busy: false, error: '' },
      resultModal:  { visible: false, winner: '', note: '', busy: false, error: '' },
    }
  },
  async mounted() {
    await this.loadGames()

    this.map = markRaw(L.map(this.$refs.mapEl).setView([51.505, -0.09], 13))
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors', maxZoom: 19,
    }).addTo(this.map)
    this.map.on('click', this.onMapClick)
    this.loadEvents()
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(pos =>
        this.map.setView([pos.coords.latitude, pos.coords.longitude], 13))
    }
  },
  beforeUnmount() { if (this.map) this.map.remove() },
  methods: {
    async loadGames() {
      try {
        const { data } = await axios.get('http://localhost:8080/games', { withCredentials: true })
        this.games = data
        const colours = ['#4f8ef7', '#f7864f', '#a6e3a1', '#cba6f7', '#f9e2af', '#89dceb']
        data.forEach((g, i) => {
          GAME_META_CACHE[g.key] = { icon: g.icon, label: `${g.icon} ${g.name}`, color: colours[i % colours.length] }
        })
        if (data.length && !this.createPanel.gameType) {
          this.createPanel.gameType = data[0].key
        }
      } catch (e) {
        console.error('Failed to load games', e)
      }
    },

    async loadEvents() {
      const { data } = await axios.get('http://localhost:8080/events', { withCredentials: true })
      Object.values(this.markers).forEach(m => m.remove())
      this.markers = {}
      data.forEach(e => this.addMarker(e))
    },

    addMarker(event) {
      const marker = L.marker([event.latitude, event.longitude],
          { icon: makeIcon(event.gameType, event.status) })
        .addTo(this.map)
        .on('click', () => {
          this.cancelCreate()
          this.detailPanel.event = event
          this.detailPanel.visible = true
          this.detailPanel.error = ''
        })
      this.markers[event.id] = markRaw(marker)
    },

    refreshMarker(event) {
      if (this.markers[event.id]) this.markers[event.id].remove()
      this.addMarker(event)
    },

    onMapClick(e) {
      if (!this.isLoggedIn) return
      this.detailPanel.visible = false
      this.cancelCreate()
      this.createPanel.lat = e.latlng.lat
      this.createPanel.lng = e.latlng.lng
      this.createPanel.visible = true
      this.createPanel.error = ''
      this.tempMarker = markRaw(L.marker([e.latlng.lat, e.latlng.lng], {
        icon: L.divIcon({ className: '', html: `<div style="font-size:30px;line-height:1;transform:translateY(-50%)">📌</div>`, iconSize: [30,30], iconAnchor: [15,30] }),
      }).addTo(this.map))
    },

    cancelCreate() {
      this.createPanel.visible     = false
      this.createPanel.description = ''
      if (this.tempMarker) { this.tempMarker.remove(); this.tempMarker = null }
    },

    async submitCreate() {
      if (!this.createPanel.scheduledAt) { this.createPanel.error = this.$t('map.pickDateTime'); return }
      this.createPanel.busy = true; this.createPanel.error = ''
      try {
        const { data } = await axios.post('http://localhost:8080/events', {
          latitude: this.createPanel.lat, longitude: this.createPanel.lng,
          gameType: this.createPanel.gameType, scheduledAt: this.createPanel.scheduledAt,
          description: this.createPanel.description || null,
        }, { withCredentials: true })
        this.addMarker(data)
        this.cancelCreate()
      } catch (err) {
        this.createPanel.error = err.response?.data || 'Failed to create event.'
      } finally { this.createPanel.busy = false }
    },

    async applyChallenge() {
      this.detailPanel.busy = true; this.detailPanel.error = ''
      try {
        const { data } = await axios.post(
          `http://localhost:8080/events/${this.detailPanel.event.id}/accept`, {}, { withCredentials: true })
        this.detailPanel.event = data
        this.refreshMarker(data)
      } catch (err) {
        this.detailPanel.error = err.response?.data || this.$t('map.couldNotApply')
      } finally { this.detailPanel.busy = false }
    },

    async approveChallenger() {
      this.detailPanel.busy = true; this.detailPanel.error = ''
      try {
        const { data } = await axios.post(
          `http://localhost:8080/events/${this.detailPanel.event.id}/approve`, {}, { withCredentials: true })
        this.detailPanel.event = data
        this.refreshMarker(data)
      } catch (err) {
        this.detailPanel.error = err.response?.data || this.$t('map.couldNotApprove')
      } finally { this.detailPanel.busy = false }
    },

    async rejectChallenger() {
      this.detailPanel.busy = true; this.detailPanel.error = ''
      try {
        const { data } = await axios.post(
          `http://localhost:8080/events/${this.detailPanel.event.id}/reject`, {}, { withCredentials: true })
        this.detailPanel.event = data
        this.refreshMarker(data)
      } catch (err) {
        this.detailPanel.error = err.response?.data || this.$t('map.couldNotReject')
      } finally { this.detailPanel.busy = false }
    },

    async cancelEvent() {
      if (!confirm(this.$t('map.cancelConfirm'))) return
      try {
        await axios.delete(`http://localhost:8080/events/${this.detailPanel.event.id}`, { withCredentials: true })
        if (this.markers[this.detailPanel.event.id]) {
          this.markers[this.detailPanel.event.id].remove()
          delete this.markers[this.detailPanel.event.id]
        }
        this.detailPanel.visible = false
      } catch (err) {
        this.detailPanel.error = err.response?.data || this.$t('map.couldNotCancel')
      }
    },

    openResultModal() {
      this.resultModal.winner = ''
      this.resultModal.note   = ''
      this.resultModal.error  = ''
      this.resultModal.visible = true
    },

    async submitResult() {
      this.resultModal.busy = true; this.resultModal.error = ''
      try {
        const { data } = await axios.post(
          `http://localhost:8080/events/${this.detailPanel.event.id}/result`,
          { winnerUsername: this.resultModal.winner || null, resultNote: this.resultModal.note },
          { withCredentials: true }
        )
        this.detailPanel.event = data
        this.resultModal.visible = false
        if (data.status === 'FINISHED' || data.status === 'DISPUTED') {
          if (this.markers[data.id]) { this.markers[data.id].remove(); delete this.markers[data.id] }
        }
      } catch (err) {
        this.resultModal.error = err.response?.data || 'Could not save result.'
      } finally { this.resultModal.busy = false }
    },

    gameLabel(type) { return GAME_META_CACHE[type]?.label || type },
    statusLabel(s)  { return this.$t('status.' + s.toLowerCase()) },
    formatDate(iso) { return iso ? new Date(iso).toLocaleString() : '—' },
    mapsLink(e)     { return `https://www.google.com/maps?q=${e.latitude},${e.longitude}` },
  },
}
</script>

<style scoped>
.map-wrapper { position: relative; width: 100%; height: 100vh; }
#map         { width: 100%; height: 100%; }

.side-panel {
  position: absolute; top: 16px; right: 16px; z-index: 1000;
  background: var(--bg-surface); color: var(--text-primary);
  border: 1px solid var(--border); border-radius: var(--r-lg);
  padding: 22px 24px; width: 290px;
  box-shadow: var(--shadow-lg);
}
.side-panel h3 { margin: 0 0 4px; color: var(--text-primary); font-size: 15px; font-weight: 700; }
.coords        { font-size: 11px; color: var(--text-muted); margin: 0 0 14px; }

label { display: block; font-size: 12px; color: var(--text-secondary); margin-bottom: 4px; font-weight: 500; }

select,
input[type="datetime-local"],
textarea,
.text-input {
  width: 100%; background: var(--bg-elevated); color: var(--text-primary);
  border: 1px solid var(--border); border-radius: var(--r);
  padding: 8px 10px; font-size: 13px; margin-bottom: 14px;
  box-sizing: border-box; font-family: var(--font);
  transition: border-color var(--transition);
}
select:focus, input[type="datetime-local"]:focus, textarea:focus, .text-input:focus {
  outline: none; border-color: var(--brand);
}
textarea { resize: vertical; line-height: 1.4; }

.event-description {
  font-size: 13px; color: var(--text-secondary); background: var(--bg-elevated);
  border-radius: var(--r); padding: 8px 10px; margin-bottom: 10px;
  line-height: 1.5; white-space: pre-wrap;
}

.panel-actions { display: flex; gap: 8px; margin-top: 4px; }

.btn-primary  { flex:1; background:var(--brand); color:#fff; border:none; border-radius:var(--r); padding:9px 0; font-weight:600; cursor:pointer; font-family:var(--font); font-size:13px; transition:background var(--transition); }
.btn-primary:hover:not(:disabled) { background:var(--brand-hover); }
.btn-primary:disabled { opacity:.5; cursor:default; }

.btn-ghost    { background:var(--bg-elevated); color:var(--text-primary); border:1px solid var(--border); border-radius:var(--r); padding:9px 14px; cursor:pointer; font-family:var(--font); font-size:13px; transition:background var(--transition); }
.btn-ghost:hover { background:var(--bg-overlay); }

.btn-success  { background:var(--brand); color:#fff; border:none; border-radius:var(--r); padding:9px 0; font-weight:600; cursor:pointer; font-family:var(--font); font-size:13px; transition:background var(--transition); }
.btn-success:hover { background:var(--brand-hover); }

.btn-danger   { background:var(--red-muted); color:var(--red); border:1px solid rgba(248,81,73,.3); border-radius:var(--r); padding:9px 0; font-weight:600; cursor:pointer; font-family:var(--font); font-size:13px; transition:background var(--transition); }
.btn-danger:hover { background:rgba(248,81,73,.2); }

.btn-danger-outline {
  background:transparent; border:1px solid var(--red); color:var(--red);
  border-radius:var(--r); padding:8px 0; cursor:pointer; font-size:13px; font-family:var(--font);
  transition:background var(--transition);
}
.btn-danger-outline:hover { background:var(--red-muted); }

.full  { width:100%; display:block; box-sizing:border-box; }
.flex1 { flex:1; }

.detail { padding-top: 28px; }
.close-btn { position:absolute; top:12px; right:14px; background:none; border:none; color:var(--text-muted); font-size:14px; cursor:pointer; transition:color var(--transition); }
.close-btn:hover { color:var(--text-primary); }

.status-badge { display:inline-block; border-radius:var(--r-full); padding:2px 10px; font-size:10px; font-weight:700; text-transform:uppercase; letter-spacing:.05em; margin-bottom:8px; }
.status-badge.open             { background:var(--brand-muted);  color:var(--brand); }
.status-badge.pending_approval { background:var(--yellow-muted); color:var(--yellow); }
.status-badge.in_progress      { background:var(--blue-muted);   color:var(--blue); }
.status-badge.finished         { background:rgba(139,148,158,.1); color:var(--text-secondary); }
.status-badge.disputed         { background:var(--orange-muted); color:var(--orange); }
.status-badge.cancelled        { background:rgba(139,148,158,.08); color:var(--text-muted); }

.rules-link { font-size:11px; color:var(--blue); text-decoration:none; margin-left:8px; opacity:.8; vertical-align:middle; }
.rules-link:hover { opacity:1; text-decoration:underline; }

.lbl { display:inline-block; font-size:11px; text-transform:uppercase; letter-spacing:.05em; color:var(--text-muted); margin-right:6px; }
.player-link { color:var(--blue); font-size:13px; text-decoration:none; }
.player-link:hover { text-decoration:underline; }
.map-link    { color:var(--blue); font-size:13px; }

.participants { display:flex; flex-wrap:wrap; gap:6px; margin:10px 0 14px; }
.participant-tag { background:var(--bg-elevated); border:1px solid var(--border); border-radius:var(--r-full); padding:4px 12px; font-size:12px; color:var(--text-primary); text-decoration:none; transition:background var(--transition); }
.participant-tag:hover { background:var(--bg-overlay); }
.participant-tag.empty { color:var(--text-muted); font-style:italic; }

.waiting-note { background:var(--yellow-muted); border:1px solid rgba(210,153,34,.2); border-radius:var(--r); padding:10px 14px; font-size:13px; color:var(--yellow); margin-bottom:8px; }
.approval-note { color:var(--text-primary); font-size:13px; margin-bottom:10px; text-align:center; }

.meetup-box { background:var(--brand-muted); border:1px solid rgba(66,184,131,.2); border-radius:var(--r-md); padding:14px; margin-bottom:10px; }
.meetup-box p { margin:0 0 10px; font-size:13px; color:var(--brand); }

.submit-status { display:flex; flex-direction:column; gap:4px; margin-bottom:10px; font-size:12px; }
.submitted     { color:var(--brand); }
.pending-submit { color:var(--text-muted); }

.my-submitted-note { color:var(--brand); font-size:13px; padding:6px 0; }

.result-box { background:var(--bg-elevated); border:1px solid var(--border); border-radius:var(--r-md); padding:12px 16px; margin:10px 0; text-align:center; }
.result-win  { font-size:14px; color:var(--brand); margin-bottom:4px; }
.result-draw { font-size:14px; color:var(--yellow); margin-bottom:4px; }
.result-note { font-size:12px; color:var(--text-muted); font-style:italic; }

.dispute-box { background:var(--red-muted); border:1px solid rgba(248,81,73,.2); border-radius:var(--r-md); padding:12px 16px; margin:10px 0; }
.dispute-title { color:var(--red); font-weight:700; margin-bottom:8px; font-size:13px; }
.dispute-row  { font-size:13px; margin-bottom:4px; color:var(--text-primary); }
.claim        { color:var(--yellow); font-weight:600; margin-left:4px; }
.dispute-note { font-size:12px; color:var(--text-muted); margin-top:6px; }

.modal-overlay { position:fixed; inset:0; background:rgba(0,0,0,.7); z-index:2000; display:flex; align-items:center; justify-content:center; }
.modal { background:var(--bg-surface); border:1px solid var(--border); border-radius:var(--r-lg); padding:28px 30px; width:340px; color:var(--text-primary); box-shadow:var(--shadow-lg); }
.modal h3 { margin:0 0 4px; color:var(--text-primary); font-size:16px; font-weight:700; }
.modal-subtitle { color:var(--text-muted); font-size:12px; margin:0 0 16px; line-height:1.4; }
.modal-actions  { display:flex; gap:8px; margin-top:12px; }

.radio-group { display:flex; flex-direction:column; gap:8px; margin-bottom:16px; }
.radio-opt { display:flex; align-items:center; gap:8px; background:var(--bg-elevated); border:1px solid var(--border); border-radius:var(--r); padding:9px 14px; cursor:pointer; font-size:13px; transition:background var(--transition); }
.radio-opt:hover { background:var(--bg-overlay); }
.radio-opt input { accent-color:var(--brand); }
.optional { color:var(--text-muted); font-size:11px; }

.map-hint { position:absolute; bottom:20px; left:50%; transform:translateX(-50%); z-index:1000; background:rgba(13,17,23,.85); color:var(--text-secondary); border:1px solid var(--border); border-radius:var(--r-full); padding:8px 20px; font-size:13px; backdrop-filter:blur(4px); pointer-events:none; }

.msg-error { color:var(--red); font-size:12px; margin-top:8px; }
.hint      { color:var(--text-muted); font-size:12px; }

.btn-detail {
  display:block; width:100%; box-sizing:border-box; text-align:center;
  background:var(--bg-elevated); color:var(--blue); border:1px solid var(--border);
  border-radius:var(--r); padding:8px 0; font-size:13px; text-decoration:none;
  margin-top:8px; cursor:pointer; font-family:var(--font); transition:background var(--transition);
}
.btn-detail:hover { background:var(--bg-overlay); }

.safety-note {
  display:flex; align-items:flex-start; gap:8px;
  background:var(--yellow-muted); border:1px solid rgba(210,153,34,.25);
  border-radius:var(--r); padding:9px 12px; margin-bottom:12px;
  font-size:12px; color:var(--yellow); line-height:1.5;
}
.safety-icon { flex-shrink:0; }
.safety-inline {
  font-size:11px; color:var(--yellow); background:var(--yellow-muted);
  border-radius:var(--r); padding:5px 9px; margin-bottom:8px;
}

.panel-slide-enter-active,.panel-slide-leave-active { transition:transform .2s ease,opacity .2s ease; }
.panel-slide-enter-from,.panel-slide-leave-to       { transform:translateX(30px); opacity:0; }
</style>
