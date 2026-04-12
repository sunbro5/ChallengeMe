<template>
  <div class="lb-page">
    <h2>{{ $t('leaderboard.title') }}</h2>
    <p class="subtitle">{{ $t('leaderboard.subtitle') }}</p>

    <div v-if="loading" class="state-msg">{{ $t('common.loading') }}</div>
    <div v-else-if="error"   class="state-msg error">{{ error }}</div>
    <div v-else-if="!rows.length" class="state-msg">{{ $t('leaderboard.noResults') }}</div>

    <table v-else>
      <thead>
        <tr>
          <th>{{ $t('leaderboard.rank') }}</th>
          <th>{{ $t('leaderboard.player') }}</th>
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
            <span v-if="i === 0">🥇</span>
            <span v-else-if="i === 1">🥈</span>
            <span v-else-if="i === 2">🥉</span>
            <span v-else>{{ i + 1 }}</span>
          </td>
          <td class="username">
            {{ row.username }}
            <span v-if="row.username === currentUser" class="you-tag">{{ $t('leaderboard.you') }}</span>
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

export default {
  name: 'LeaderboardPage',
  data() {
    return {
      rows: [],
      loading: true,
      error: '',
      currentUser: localStorage.getItem('username') || '',
    }
  },
  async mounted() {
    try {
      const { data } = await axios.get('http://localhost:8080/leaderboard', { withCredentials: true })
      this.rows = data
    } catch {
      this.error = this.$t('leaderboard.couldNotLoad')
    } finally {
      this.loading = false
    }
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

.rank { font-size: 15px; width: 48px; text-align: center; }
.username { font-weight: 600; }
.wins { color: var(--brand); font-weight: 700; }
.total { color: var(--text-muted); font-size: 12px; }

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
</style>
