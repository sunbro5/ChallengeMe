<template>
  <div class="tournament-detail-page">
    <div class="back-bar">
      <button class="btn-back" @click="$router.push('/tournaments')">{{ $t('tournament.back') }}</button>
    </div>

    <div v-if="loading" class="state-msg">{{ $t('common.loading') }}</div>
    <div v-else-if="error" class="state-msg error">{{ error }}</div>

    <template v-else-if="tournament">
      <!-- ── Header ──────────────────────────────────────────────────────── -->
      <div class="t-header">
        <div class="t-title-row">
          <h2>{{ tournament.name }}</h2>
          <div class="badge-row">
            <span v-if="tournament.format" class="t-format">
              {{ tournament.format === 'ELIMINATION'
                ? $t('tournament.formatElimination')
                : $t('tournament.formatRoundRobin') }}
            </span>
            <span class="t-status" :class="tournament.status.toLowerCase()">
              {{ $t(`tournament.${tournament.status.toLowerCase()}`) }}
            </span>
          </div>
        </div>
        <p class="t-meta">
          {{ gameIcon(tournament.gameType) }} {{ gameLabel(tournament.gameType) }}
          &nbsp;·&nbsp;
          {{ $t('tournament.createdBy') }} {{ tournament.creatorUsername }}
        </p>

        <!-- Winner banner -->
        <div v-if="tournament.winnerUsername" class="winner-banner">
          {{ $t('tournament.winner', { username: tournament.winnerUsername }) }}
        </div>
      </div>

      <!-- ── Participants ────────────────────────────────────────────────── -->
      <div class="section">
        <h3 class="section-title">
          {{ $t('tournament.participants') }}
          <span class="player-count">{{ tournament.participantCount }} / {{ tournament.capacity }}</span>
        </h3>
        <div class="participants-list">
          <div
            v-for="p in tournament.participants" :key="p.userId"
            class="participant-row"
            :class="{ eliminated: p.eliminated }"
          >
            <router-link :to="`/player/${p.username}`" class="p-name">
              {{ p.username }}
            </router-link>
            <span v-if="p.seed > 0" class="p-seed">#{{ p.seed }}</span>
            <span v-if="p.eliminated" class="p-elim">{{ $t('tournament.eliminated') }}</span>
          </div>
        </div>

        <!-- Join / waiting for players -->
        <div v-if="tournament.status === 'OPEN'" class="join-area">
          <p class="capacity-hint">
            {{ $t('tournament.waitingForPlayers', { count: tournament.participantCount, capacity: tournament.capacity }) }}
          </p>
          <button
            v-if="!isParticipant && isLoggedIn"
            class="btn-join"
            :disabled="actionBusy || tournament.participantCount >= tournament.capacity"
            @click="joinTournament"
          >{{ $t('tournament.joinBtn') }}</button>
          <span v-else-if="isParticipant" class="joined-tag">{{ $t('tournament.joined') }}</span>

          <!-- Start button (creator only):
               ELIMINATION requires full capacity; ROUND_ROBIN requires ≥ 3 players -->
          <button
            v-if="isCreator && canStart"
            class="btn-start"
            :disabled="actionBusy"
            @click="startTournament"
          >{{ actionBusy ? $t('tournament.starting') : $t('tournament.startBtn') }}</button>
        </div>

        <p v-if="actionError" class="msg-error">{{ actionError }}</p>
      </div>

      <!-- ── Standings table (ROUND_ROBIN only) ────────────────────────── -->
      <div v-if="tournament.format === 'ROUND_ROBIN' && tournament.matches.length" class="section">
        <h3 class="section-title">{{ $t('tournament.standings') }}</h3>
        <table class="standings-table">
          <thead>
            <tr>
              <th>#</th>
              <th>{{ $t('tournament.participants') }}</th>
              <th>W</th>
              <th>L</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(p, idx) in sortedParticipants"
              :key="p.userId"
              :class="{
                'my-row': p.username === currentUser,
                'winner-row': idx === 0 && tournament.status === 'FINISHED'
              }"
            >
              <td class="rank-col">{{ idx + 1 }}</td>
              <td class="name-col">
                <router-link :to="`/player/${p.username}`" class="p-name">{{ p.username }}</router-link>
                <span v-if="tournament.winnerUsername === p.username" class="winner-crown">★</span>
              </td>
              <td>{{ p.wins }}</td>
              <td>{{ p.losses }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- ── Bracket / Matches ───────────────────────────────────────────── -->
      <div v-if="tournament.matches.length" class="section">
        <h3 class="section-title">{{ $t('tournament.bracket') }}</h3>

        <div
          v-for="group in matchesByRound" :key="group.round"
          class="round-block"
        >
          <h4 class="round-label">
            {{ tournament.format !== 'ROUND_ROBIN' && group.matches.length === 1
              ? $t('tournament.final')
              : $t('tournament.round', { n: group.round }) }}
          </h4>

          <div class="matches-list">
            <div
              v-for="match in group.matches" :key="match.id"
              class="match-card"
              :class="{ done: !!match.winnerUsername }"
            >
              <div class="match-players">
                <span :class="['mp', match.winnerUsername === match.player1Username ? 'winner' : match.winnerUsername ? 'loser' : '']">
                  {{ match.player1Username || '?' }}
                </span>
                <span class="vs">{{ $t('tournament.vs') }}</span>
                <span :class="['mp', match.winnerUsername === match.player2Username ? 'winner' : match.winnerUsername ? 'loser' : '']">
                  {{ match.player2Username || '?' }}
                </span>
              </div>

              <div class="match-actions">
                <router-link
                  v-if="match.gameEventId"
                  :to="`/event/${match.gameEventId}`"
                  class="link-event"
                >Detail →</router-link>

                <button
                  v-if="isCreator && !match.winnerUsername && tournament.status === 'IN_PROGRESS'"
                  class="btn-record"
                  @click="openRecordModal(match)"
                >{{ $t('tournament.recordResult') }}</button>

                <span v-if="match.winnerUsername" class="match-winner-tag">
                  {{ match.winnerUsername }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- ── Record result modal ───────────────────────────────────────────── -->
    <div v-if="recordModal.visible" class="modal-overlay" @click.self="recordModal.visible = false">
      <div class="modal">
        <h3>{{ $t('tournament.selectWinner') }}</h3>
        <div class="radio-group">
          <label class="radio-opt">
            <input type="radio" :value="recordModal.match.player1Username" v-model="recordModal.winner" />
            {{ recordModal.match.player1Username }}
          </label>
          <label class="radio-opt">
            <input type="radio" :value="recordModal.match.player2Username" v-model="recordModal.winner" />
            {{ recordModal.match.player2Username }}
          </label>
        </div>
        <p v-if="recordModal.error" class="msg-error">{{ recordModal.error }}</p>
        <div class="modal-actions">
          <button class="btn-ghost" @click="recordModal.visible = false">{{ $t('common.cancel') }}</button>
          <button
            class="btn-primary"
            :disabled="!recordModal.winner || recordModal.busy"
            @click="submitResult"
          >{{ recordModal.busy ? $t('common.saving') : $t('common.submit') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { useI18n } from 'vue-i18n'

export default {
  name: 'TournamentDetailPage',
  setup() {
    const { locale } = useI18n()
    return { locale }
  },
  data() {
    return {
      tournament: null,
      games: [],
      gameMeta: {},
      loading: true,
      error: '',
      actionBusy: false,
      actionError: '',
      currentUser: localStorage.getItem('username') || '',
      isLoggedIn: localStorage.getItem('isLoggedIn') === 'true',
      recordModal: { visible: false, match: null, winner: '', busy: false, error: '' },
    }
  },
  computed: {
    currentLocale() { return this.locale },

    isCreator() {
      return this.tournament?.creatorUsername === this.currentUser
    },
    isParticipant() {
      return this.tournament?.participants?.some(p => p.username === this.currentUser)
    },

    canStart() {
      if (!this.tournament) return false
      const count = this.tournament.participantCount
      if (this.tournament.format === 'ROUND_ROBIN') return count >= 3
      return count === this.tournament.capacity
    },

    /** Participants sorted by wins desc, losses asc — for round-robin standings. */
    sortedParticipants() {
      if (!this.tournament?.participants) return []
      return [...this.tournament.participants].sort((a, b) => {
        if (b.wins !== a.wins) return b.wins - a.wins
        return a.losses - b.losses
      })
    },

    /** Returns [ { round: N, matches: [...] }, ... ] sorted by round number. */
    matchesByRound() {
      if (!this.tournament?.matches?.length) return []
      const map = {}
      for (const m of this.tournament.matches) {
        if (!map[m.round]) map[m.round] = []
        map[m.round].push(m)
      }
      return Object.keys(map)
        .map(Number)
        .sort((a, b) => a - b)
        .map(r => ({ round: r, matches: map[r] }))
    },
  },
  async mounted() {
    await Promise.all([this.loadGames(), this.loadTournament()])
  },
  methods: {
    async loadGames() {
      try {
        const { data } = await axios.get('/api/games', { withCredentials: true })
        data.forEach(g => { this.gameMeta[g.key] = g })
      } catch { /* non-fatal */ }
    },

    async loadTournament() {
      this.loading = true; this.error = ''
      try {
        const { data } = await axios.get(
          `/api/tournaments/${this.$route.params.id}`,
          { withCredentials: true }
        )
        this.tournament = data
      } catch {
        this.error = this.$t('tournament.couldNotLoad')
      } finally {
        this.loading = false
      }
    },

    async joinTournament() {
      this.actionBusy = true; this.actionError = ''
      try {
        const { data } = await axios.post(
          `/api/tournaments/${this.tournament.id}/join`, {},
          { withCredentials: true }
        )
        this.tournament = data
      } catch (err) {
        this.actionError = err.response?.data || this.$t('tournament.couldNotJoin')
      } finally { this.actionBusy = false }
    },

    async startTournament() {
      this.actionBusy = true; this.actionError = ''
      // Location is required — every match must appear on the map
      const pos = await this.getPosition()
      if (!pos) {
        this.actionError = this.$t('tournament.locationRequired')
        this.actionBusy = false
        return
      }
      try {
        const { data } = await axios.post(
          `/api/tournaments/${this.tournament.id}/start`,
          { latitude: pos.lat, longitude: pos.lng },
          { withCredentials: true }
        )
        this.tournament = data
      } catch (err) {
        this.actionError = err.response?.data || this.$t('tournament.couldNotStart')
      } finally { this.actionBusy = false }
    },

    openRecordModal(match) {
      this.recordModal = { visible: true, match, winner: '', busy: false, error: '' }
    },

    async submitResult() {
      const match = this.recordModal.match
      // Find userId of selected winner
      const winnerParticipant = this.tournament.participants.find(
        p => p.username === this.recordModal.winner
      )
      if (!winnerParticipant) { this.recordModal.error = this.$t('tournament.couldNotRecord'); return }
      this.recordModal.busy = true; this.recordModal.error = ''
      try {
        const { data } = await axios.post(
          `/api/tournaments/${this.tournament.id}/matches/${match.id}/result`,
          { winnerUserId: winnerParticipant.userId },
          { withCredentials: true }
        )
        this.tournament = data
        this.recordModal.visible = false
      } catch (err) {
        this.recordModal.error = err.response?.data || this.$t('tournament.couldNotRecord')
      } finally { this.recordModal.busy = false }
    },

    getPosition() {
      return new Promise(resolve => {
        if (!navigator.geolocation) { resolve(null); return }
        const timer = setTimeout(() => resolve(null), 5000)
        navigator.geolocation.getCurrentPosition(
          p  => { clearTimeout(timer); resolve({ lat: p.coords.latitude, lng: p.coords.longitude }) },
          () => { clearTimeout(timer); resolve(null) }
        )
      })
    },

    gameIcon(key)  { return this.gameMeta[key]?.icon || '' },
    gameLabel(key) {
      const g = this.gameMeta[key]
      if (!g) return key
      return this.currentLocale === 'cs' ? g.nameCs : g.nameEn
    },
  },
}
</script>

<style scoped>
.tournament-detail-page {
  max-width: 680px;
  margin: 0 auto;
  padding: 24px 20px 64px;
}

.back-bar { margin-bottom: 20px; }
.btn-back {
  background: var(--bg-elevated); border: 1px solid var(--border); border-radius: var(--r);
  padding: 6px 14px; font-size: 12px; cursor: pointer; color: var(--text-secondary);
  font-family: var(--font); transition: background var(--transition);
}
.btn-back:hover { background: var(--bg-overlay); color: var(--text-primary); }

.state-msg       { text-align: center; padding: 60px; color: var(--text-muted); }
.state-msg.error { color: var(--red); }

.t-header { margin-bottom: 28px; }
.t-title-row {
  display: flex; align-items: flex-start; gap: 12px; flex-wrap: wrap; margin-bottom: 6px;
}
h2 { font-size: 20px; font-weight: 700; margin: 0; letter-spacing: -.01em; }

.badge-row { display: flex; gap: 6px; align-items: center; flex-wrap: wrap; flex-shrink: 0; }

.t-format {
  font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: .04em;
  padding: 2px 9px; border-radius: var(--r-full);
  background: var(--bg-overlay); color: var(--text-secondary);
}

.t-status {
  font-size: 10px; font-weight: 700; text-transform: uppercase;
  letter-spacing: .05em; padding: 2px 9px; border-radius: var(--r-full);
}
.t-status.open        { background: var(--brand-muted);  color: var(--brand); }
.t-status.in_progress { background: var(--blue-muted);   color: var(--blue); }
.t-status.finished    { background: rgba(139,148,158,.1); color: var(--text-muted); }

.t-meta { font-size: 12px; color: var(--text-muted); margin: 0 0 12px; }

.winner-banner {
  background: linear-gradient(135deg, rgba(201,162,39,.2), rgba(201,162,39,.08));
  border: 1px solid rgba(201,162,39,.4);
  border-radius: var(--r-md);
  padding: 12px 18px;
  font-size: 15px;
  font-weight: 700;
  color: #c9a227;
  text-align: center;
}

.section { margin-bottom: 28px; }
.section-title {
  font-size: 13px; font-weight: 600; color: var(--text-secondary);
  text-transform: uppercase; letter-spacing: .05em;
  margin: 0 0 12px; display: flex; align-items: center; gap: 8px;
}
.player-count {
  font-size: 11px; background: var(--bg-elevated); border: 1px solid var(--border);
  border-radius: var(--r-full); padding: 1px 8px; color: var(--text-muted);
  font-weight: 400; text-transform: none; letter-spacing: 0;
}

/* ── Participants ──────────────────────────────────────────────── */
.participants-list { display: flex; flex-direction: column; gap: 6px; margin-bottom: 12px; }
.participant-row {
  display: flex; align-items: center; gap: 10px;
  background: var(--bg-surface); border: 1px solid var(--border);
  border-radius: var(--r); padding: 8px 14px; font-size: 13px;
}
.participant-row.eliminated { opacity: .5; }
.p-name { color: var(--text-primary); text-decoration: none; font-weight: 500; flex: 1; }
.p-name:hover { text-decoration: underline; color: var(--brand); }
.p-seed { font-size: 11px; color: var(--text-muted); background: var(--bg-elevated); border-radius: var(--r-full); padding: 1px 7px; }
.p-elim { font-size: 11px; color: var(--red); }

.join-area { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.capacity-hint { font-size: 12px; color: var(--text-muted); margin: 0; flex: 1; }
.btn-join {
  background: var(--brand); color: #fff; border: none; border-radius: var(--r);
  padding: 7px 16px; font-size: 13px; font-weight: 600; font-family: var(--font);
  cursor: pointer; transition: background var(--transition);
}
.btn-join:hover:not(:disabled) { background: var(--brand-hover); }
.btn-join:disabled { opacity: .45; cursor: default; }
.joined-tag {
  background: var(--brand-muted); color: var(--brand); border: 1px solid rgba(66,184,131,.3);
  border-radius: var(--r-full); padding: 4px 12px; font-size: 12px; font-weight: 600;
}
.btn-start {
  background: var(--blue); color: #fff; border: none; border-radius: var(--r);
  padding: 7px 16px; font-size: 13px; font-weight: 600; font-family: var(--font);
  cursor: pointer; transition: background var(--transition);
}
.btn-start:hover:not(:disabled) { filter: brightness(1.1); }
.btn-start:disabled { opacity: .45; cursor: default; }

/* ── Standings table (ROUND_ROBIN) ───────────────────────────── */
.standings-table {
  width: 100%; border-collapse: collapse; font-size: 13px; margin-bottom: 4px;
}
.standings-table th {
  text-align: left; font-size: 10px; font-weight: 700; text-transform: uppercase;
  letter-spacing: .04em; color: var(--text-muted); padding: 5px 10px;
  border-bottom: 1px solid var(--border);
}
.standings-table td {
  padding: 8px 10px; border-bottom: 1px solid var(--border); color: var(--text-primary);
}
.standings-table tbody tr:hover { background: var(--bg-elevated); }
.my-row td { color: var(--brand); }
.winner-row .name-col { font-weight: 700; }
.rank-col { color: var(--text-muted); width: 28px; }
.name-col { font-weight: 500; }
.winner-crown { color: #c9a227; margin-left: 4px; }

/* ── Bracket ──────────────────────────────────────────────────── */
.round-block { margin-bottom: 20px; }
.round-label {
  font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: .05em;
  color: var(--text-muted); margin: 0 0 8px;
}

.matches-list { display: flex; flex-direction: column; gap: 8px; }
.match-card {
  display: flex; align-items: center; justify-content: space-between; gap: 12px;
  background: var(--bg-surface); border: 1px solid var(--border); border-radius: var(--r-md);
  padding: 12px 16px; flex-wrap: wrap;
}
.match-card.done { opacity: .85; }

.match-players { display: flex; align-items: center; gap: 10px; font-size: 14px; }
.mp { font-weight: 600; color: var(--text-primary); }
.mp.winner { color: var(--brand); }
.mp.loser  { color: var(--text-muted); text-decoration: line-through; }
.vs { font-size: 11px; color: var(--text-muted); font-weight: 400; }

.match-actions { display: flex; align-items: center; gap: 8px; }
.link-event {
  font-size: 12px; color: var(--blue); text-decoration: none;
}
.link-event:hover { text-decoration: underline; }
.btn-record {
  background: var(--bg-elevated); border: 1px solid var(--border); border-radius: var(--r);
  padding: 4px 12px; font-size: 12px; cursor: pointer; font-family: var(--font);
  color: var(--text-secondary); transition: background var(--transition), border-color var(--transition);
}
.btn-record:hover { border-color: var(--brand); color: var(--brand); }
.match-winner-tag {
  font-size: 12px; font-weight: 700; color: var(--brand);
  background: var(--brand-muted); border-radius: var(--r-full); padding: 2px 10px;
}

/* ── Modal ─────────────────────────────────────────────────────── */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,.7);
  z-index: 2000; display: flex; align-items: center; justify-content: center;
}
.modal {
  background: var(--bg-surface); border: 1px solid var(--border);
  border-radius: var(--r-xl); padding: 26px 28px; width: 340px; max-width: 94vw;
  color: var(--text-primary); box-shadow: var(--shadow-lg);
  display: flex; flex-direction: column; gap: 14px;
}
.modal h3 { margin: 0; font-size: 16px; font-weight: 700; }

.radio-group { display: flex; flex-direction: column; gap: 8px; }
.radio-opt {
  display: flex; align-items: center; gap: 8px;
  background: var(--bg-elevated); border: 1px solid var(--border); border-radius: var(--r);
  padding: 9px 14px; cursor: pointer; font-size: 13px; transition: background var(--transition);
}
.radio-opt:hover { background: var(--bg-overlay); }
.radio-opt input { accent-color: var(--brand); }

.modal-actions { display: flex; gap: 8px; justify-content: flex-end; }
.btn-ghost {
  background: var(--bg-elevated); color: var(--text-primary); border: 1px solid var(--border);
  border-radius: var(--r); padding: 7px 16px; cursor: pointer; font-family: var(--font); font-size: 13px;
  transition: background var(--transition);
}
.btn-ghost:hover { background: var(--bg-overlay); }
.btn-primary {
  background: var(--brand); color: #fff; border: none; border-radius: var(--r);
  padding: 7px 18px; font-weight: 600; cursor: pointer; font-family: var(--font); font-size: 13px;
  transition: background var(--transition);
}
.btn-primary:hover:not(:disabled) { background: var(--brand-hover); }
.btn-primary:disabled { opacity: .45; cursor: default; }

.msg-error { color: var(--red); font-size: 12px; margin: 0; }

@media (max-width: 640px) {
  .tournament-detail-page { padding: 16px 14px 76px; }
  .match-card { flex-direction: column; align-items: flex-start; }
}
</style>
