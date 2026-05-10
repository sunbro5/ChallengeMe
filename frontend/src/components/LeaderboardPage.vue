<template>
  <div class="lb-page">
    <h2>{{ $t('leaderboard.title') }}</h2>
    <p class="subtitle">{{ $t('leaderboard.subtitle') }}</p>

    <!-- ── Game filter ───────────────────────────────────────────────── -->
    <div class="filter-bar">
      <button
        :class="['filter-btn', { active: !selectedGame }]"
        @click="selectGame(null)"
      >{{ $t('leaderboard.allGames') }}</button>
      <button
        v-for="g in games" :key="g.key"
        :class="['filter-btn', { active: selectedGame === g.key }]"
        @click="selectGame(g.key)"
      ><span class="btn-icon">{{ g.icon }}</span>{{ currentLocale === 'cs' ? g.nameCs : g.nameEn }}</button>
    </div>

    <div v-if="loading" class="state-msg">{{ $t('common.loading') }}</div>
    <div v-else-if="error"   class="state-msg error">{{ error }}</div>
    <div v-else-if="!rows.length" class="state-msg">{{ $t('leaderboard.noResults') }}</div>

    <table v-else>
      <thead>
        <tr>
          <th>{{ $t('leaderboard.rank') }}</th>
          <th>{{ $t('leaderboard.player') }}</th>
          <th v-if="!selectedGame" class="elo-col">{{ $t('leaderboard.rating') }}</th>
          <th>{{ $t('leaderboard.wins') }}</th>
          <th>{{ $t('leaderboard.losses') }}</th>
          <th>{{ $t('leaderboard.draws') }}</th>
          <th>{{ $t('leaderboard.games') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="(row, i) in rows" :key="row.username"
          :class="{ me: row.username === currentUser, podium: i < 3 }"
        >
          <td class="rank">
            <span :class="i < 3 ? `top${i+1}` : ''">{{ i + 1 }}</span>
          </td>
          <td class="username">
            {{ row.username }}
            <span v-if="row.username === currentUser" class="you-tag">{{ $t('leaderboard.you') }}</span>
            <span class="rank-badge" :style="{ color: rowRank(row).color, borderColor: rowRank(row).color }">
              {{ $t(`rank.${rowRank(row).key}`) }}
            </span>
          </td>
          <td v-if="!selectedGame" class="elo-col">
            <span class="elo-badge">{{ row.rating || 1000 }}</span>
          </td>
          <td class="wins">{{ row.wins }}</td>
          <td>{{ row.losses }}</td>
          <td>{{ row.draws }}</td>
          <td class="total">{{ row.wins + row.losses + row.draws }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import axios from 'axios'
import { useI18n } from 'vue-i18n'
import { getRank } from '../utils/rank.js'

export default {
  name: 'LeaderboardPage',
  setup() {
    const { locale } = useI18n()
    return { locale }
  },
  data() {
    return {
      rows: [],
      games: [],
      selectedGame: null,
      loading: true,
      error: '',
      currentUser: localStorage.getItem('username') || '',
    }
  },
  computed: {
    currentLocale() { return this.locale },
  },
  async mounted() {
    await Promise.all([this.loadGames(), this.loadLeaderboard()])
  },
  methods: {
    async loadGames() {
      try {
        const { data } = await axios.get('/api/games', { withCredentials: true })
        this.games = data
      } catch { /* non-fatal */ }
    },

    async loadLeaderboard() {
      this.loading = true
      this.error = ''
      try {
        const url = this.selectedGame
          ? `/api/leaderboard/game/${this.selectedGame}`
          : '/api/leaderboard'
        const { data } = await axios.get(url, { withCredentials: true })
        this.rows = data
      } catch {
        this.error = this.$t('leaderboard.couldNotLoad')
      } finally {
        this.loading = false
      }
    },

    async selectGame(key) {
      this.selectedGame = key
      await this.loadLeaderboard()
    },

    rowRank(row) { return getRank(row.rating || 1000) },
  },
}
</script>

<style scoped>
.lb-page {
  max-width: 680px;
  margin: 0 auto;
  padding: 32px 20px 64px;
}

h2 {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 4px;
  color: var(--text-primary);
  letter-spacing: -.01em;
}
.subtitle { color: var(--text-muted); margin-bottom: 28px; font-size: 13px; }

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 20px;
}
.filter-btn {
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: var(--r-full);
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 500;
  padding: 4px 12px;
  cursor: pointer;
  font-family: var(--font);
  transition: background var(--transition), color var(--transition), border-color var(--transition);
  display: inline-flex;
  align-items: center;
  gap: 5px;
}
.btn-icon {
  font-size: 14px;
  line-height: 1;
}
.filter-btn:hover { border-color: var(--brand); color: var(--text-primary); }
.filter-btn.active {
  background: var(--brand-muted);
  border-color: rgba(66,184,131,.4);
  color: var(--brand);
  font-weight: 600;
}

.state-msg { text-align: center; padding: 60px; color: var(--text-muted); }
.state-msg.error { color: var(--red); }

table {
  width: 100%;
  border-collapse: collapse;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--r-lg);
  overflow: hidden;
}

thead tr { background: var(--bg-elevated); }
th {
  padding: 10px 14px;
  text-align: left;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: .06em;
  color: var(--text-muted);
  font-weight: 600;
}

tbody tr {
  border-top: 1px solid var(--border-muted);
  transition: background var(--transition);
}
tbody tr:hover { background: var(--bg-elevated); }
tbody tr.me    { background: var(--brand-muted); }

td {
  padding: 12px 14px;
  font-size: 13px;
  color: var(--text-primary);
}

.rank { font-size: 13px; width: 48px; text-align: center; font-weight: 600; color: var(--text-muted); }
.rank .top1 { color: #c9a227; font-weight: 700; }
.rank .top2 { color: #8a9ba8; font-weight: 700; }
.rank .top3 { color: #a0673a; font-weight: 700; }
.username { font-weight: 600; }
.wins { color: var(--brand); font-weight: 700; }
.total { color: var(--text-muted); font-size: 12px; }

.elo-col { text-align: center; }
.elo-badge {
  display: inline-block;
  background: var(--blue-muted, rgba(79,142,247,.12));
  color: var(--blue, #4f8ef7);
  border-radius: var(--r-full);
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: .01em;
}

.rank-badge {
  border: 1px solid;
  border-radius: 4px;
  padding: 1px 7px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: .04em;
  margin-left: 6px;
  vertical-align: middle;
  opacity: .85;
}

.you-tag {
  background: var(--brand-muted);
  color: var(--brand);
  border-radius: var(--r-full);
  padding: 1px 8px;
  font-size: 10px;
  font-weight: 700;
  margin-left: 6px;
  vertical-align: middle;
  text-transform: uppercase;
  letter-spacing: .04em;
}

.podium td { color: var(--yellow); }
.podium td.username { font-weight: 700; }

@media (max-width: 640px) {
  .lb-page { padding: 20px 14px 76px; }
  /* wrap table so it can scroll horizontally if needed */
  .lb-page { overflow-x: hidden; }
  table    { font-size: 12px; }
  th, td   { padding: 9px 10px; }
  .rank    { width: 36px; }
}

@media (max-width: 420px) {
  /* hide Draws and Games columns on very narrow screens */
  th:nth-child(5), td:nth-child(5),
  th:nth-child(6), td:nth-child(6) { display: none; }
}
</style>
