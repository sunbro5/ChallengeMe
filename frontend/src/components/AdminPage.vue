<template>
  <div class="admin-page">
    <div class="admin-header">
      <h2>{{ $t('admin.title') }}</h2>
      <div class="filter-row">
        <label>
          <input type="checkbox" v-model="showResolved" @change="loadReports" />
          {{ $t('admin.showResolved') }}
        </label>
        <button class="refresh-btn" @click="reload">{{ $t('admin.refresh') }}</button>
      </div>
    </div>

    <div class="admin-tabs">
      <button :class="['admin-tab', { active: tab === 'reports' }]" @click="tab = 'reports'">
        {{ $t('admin.tabReports') }}
      </button>
      <button :class="['admin-tab', { active: tab === 'disputes' }]" @click="tab = 'disputes'">
        {{ $t('admin.tabDisputes') }}
        <span v-if="disputes.length" class="tab-badge">{{ disputes.length }}</span>
      </button>
      <button :class="['admin-tab', { active: tab === 'fraud' }]" @click="tab = 'fraud'; loadFraud()">
        {{ $t('admin.tabFraud') }}
        <span v-if="fraud && (fraud.suspiciousPairs.length + fraud.highDisputeUsers.length) > 0" class="tab-badge tab-badge-warn">
          {{ fraud.suspiciousPairs.length + fraud.highDisputeUsers.length }}
        </span>
      </button>
    </div>

    <!-- Reports tab -->
    <div v-if="tab === 'reports'">
      <div v-if="loading" class="loading">{{ $t('admin.loading') }}</div>
      <div v-else-if="filtered.length === 0" class="empty">{{ $t('admin.noReports') }}</div>
      <div v-else class="reports-list">
        <div
          v-for="report in filtered"
          :key="report.id"
          :class="['report-card', { resolved: report.status === 'RESOLVED' }]"
        >
          <div class="report-meta">
            <span class="badge" :class="report.status === 'PENDING' ? 'pending' : 'resolved-badge'">
              {{ report.status }}
            </span>
            <span class="date">{{ formatDate(report.createdAt) }}</span>
          </div>
          <div class="report-body">
            <div class="report-row">
              <span class="label">{{ $t('admin.reporter') }}</span>
              <span>{{ report.reporterUsername }}</span>
            </div>
            <div class="report-row">
              <span class="label">{{ $t('admin.reported') }}</span>
              <span>{{ report.reportedUsername }}</span>
              <span v-if="report.reportedBanned" class="banned-tag">{{ $t('admin.banned') }}</span>
            </div>
            <div class="report-row reason-row">
              <span class="label">{{ $t('admin.reason') }}</span>
              <span class="reason-text">{{ report.reason }}</span>
            </div>
          </div>
          <div class="report-actions">
            <button v-if="!report.reportedBanned" class="ban-btn" @click="banUser(report)">{{ $t('admin.banUser') }}</button>
            <button v-else class="unban-btn" @click="unbanUser(report)">{{ $t('admin.unbanUser') }}</button>
            <button v-if="report.status === 'PENDING'" class="resolve-btn" @click="resolveReport(report)">{{ $t('admin.markResolved') }}</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Fraud tab -->
    <div v-if="tab === 'fraud'">
      <div v-if="fraudLoading" class="loading">{{ $t('admin.loading') }}</div>
      <template v-else-if="fraud">
        <!-- Suspicious pairs -->
        <h3 class="fraud-section-title">{{ $t('admin.suspiciousPairs') }}</h3>
        <div v-if="fraud.suspiciousPairs.length === 0" class="empty" style="padding:20px 0">
          {{ $t('admin.noSuspiciousPairs') }}
        </div>
        <div v-else class="reports-list">
          <div v-for="p in fraud.suspiciousPairs" :key="p.userA + p.userB" class="report-card fraud-pair">
            <div class="fraud-pair-names">
              <router-link :to="`/player/${p.userA}`" class="fraud-player">{{ p.userA }}</router-link>
              <span class="fraud-vs">vs</span>
              <router-link :to="`/player/${p.userB}`" class="fraud-player">{{ p.userB }}</router-link>
            </div>
            <span class="fraud-count">{{ p.matchCount }} {{ $t('admin.matchesThisWeek') }}</span>
          </div>
        </div>

        <!-- High dispute rate users -->
        <h3 class="fraud-section-title" style="margin-top:24px">{{ $t('admin.highDisputeUsers') }}</h3>
        <div v-if="fraud.highDisputeUsers.length === 0" class="empty" style="padding:20px 0">
          {{ $t('admin.noHighDisputeUsers') }}
        </div>
        <div v-else class="reports-list">
          <div v-for="u in fraud.highDisputeUsers" :key="u.userId" class="report-card">
            <div class="report-body">
              <div class="report-row">
                <router-link :to="`/player/${u.username}`" class="fraud-player">{{ u.username }}</router-link>
                <span class="fraud-rate-badge">
                  {{ $t('admin.disputeRate') }}: <strong>{{ Math.round(u.disputeRate * 100) }}%</strong>
                </span>
                <span class="fraud-games">{{ u.totalGames }} {{ $t('admin.totalGames') }}</span>
              </div>
              <div class="report-row" style="font-size:11px; color:var(--text-muted)">
                {{ u.wins }}W · {{ u.losses }}L · {{ u.draws }}D · {{ u.disputes }} spory
              </div>
            </div>
            <div class="report-actions">
              <button class="ban-btn" @click="resetElo(u)" :disabled="u.eloReset">
                {{ u.eloReset ? $t('admin.resetEloOk') : $t('admin.resetElo') }}
              </button>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- Disputes tab -->
    <div v-if="tab === 'disputes'">
      <div v-if="disputesLoading" class="loading">{{ $t('admin.loading') }}</div>
      <div v-else-if="disputes.length === 0" class="empty">{{ $t('admin.noDisputes') }}</div>
      <div v-else class="reports-list">
        <div v-for="d in disputes" :key="d.id" class="report-card">
          <div class="report-meta">
            <span class="badge pending">DISPUTED</span>
            <span class="date">{{ d.scheduledAt ? formatDate(d.scheduledAt) : '' }}</span>
          </div>
          <div class="report-body">
            <div class="report-row">
              <span class="label">{{ $t('admin.game') }}</span>
              <span>{{ d.gameType }}</span>
            </div>
            <div class="report-row">
              <span class="label">{{ $t('admin.creator') }}</span>
              <span>{{ d.creatorUsername }}</span>
              <span class="claim">→ {{ d.creatorResult || 'draw' }}</span>
            </div>
            <div class="report-row">
              <span class="label">{{ $t('admin.challenger') }}</span>
              <span>{{ d.challengerUsername }}</span>
              <span class="claim">→ {{ d.challengerResult || 'draw' }}</span>
            </div>
          </div>
          <div class="report-actions">
            <button class="resolve-btn" @click="resolveDispute(d, d.creatorUsername)">
              ✅ {{ d.creatorUsername }}
            </button>
            <button class="resolve-btn" @click="resolveDispute(d, d.challengerUsername)">
              ✅ {{ d.challengerUsername }}
            </button>
            <button class="resolve-btn" @click="resolveDispute(d, null)">
              🤝 {{ $t('common.draw') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'AdminPage',
  data() {
    return {
      tab: 'reports',
      reports: [],
      loading: true,
      showResolved: false,
      disputes: [],
      disputesLoading: false,
      fraud: null,
      fraudLoading: false,
    }
  },
  computed: {
    filtered() {
      return this.showResolved ? this.reports : this.reports.filter(r => r.status === 'PENDING')
    }
  },
  mounted() {
    this.reload()
  },
  methods: {
    reload() {
      this.loadReports()
      this.loadDisputes()
    },
    async loadReports() {
      this.loading = true
      try {
        const res = await axios.get('/api/admin/reports', { withCredentials: true })
        this.reports = res.data
      } catch (e) {
        console.error('Failed to load reports', e)
      } finally {
        this.loading = false
      }
    },
    async loadDisputes() {
      this.disputesLoading = true
      try {
        const res = await axios.get('/api/admin/disputes', { withCredentials: true })
        this.disputes = res.data
      } catch (e) {
        console.error('Failed to load disputes', e)
      } finally {
        this.disputesLoading = false
      }
    },
    async resolveDispute(dispute, winnerUsername) {
      try {
        await axios.post(`/api/admin/disputes/${dispute.id}/resolve`,
          { winnerUsername: winnerUsername || '' },
          { withCredentials: true })
        this.disputes = this.disputes.filter(d => d.id !== dispute.id)
      } catch (e) {
        alert(e.response?.data || 'Failed to resolve dispute')
      }
    },
    async banUser(report) {
      try {
        await axios.post(`/api/admin/ban/${report.reportedId}`, {}, { withCredentials: true })
        report.reportedBanned = true
      } catch (e) {
        alert(e.response?.data || this.$t('admin.failedBan'))
      }
    },
    async unbanUser(report) {
      try {
        await axios.post(`/api/admin/unban/${report.reportedId}`, {}, { withCredentials: true })
        report.reportedBanned = false
      } catch (e) {
        alert(e.response?.data || this.$t('admin.failedUnban'))
      }
    },
    async resolveReport(report) {
      try {
        await axios.post(`/api/admin/resolve/${report.id}`, {}, { withCredentials: true })
        report.status = 'RESOLVED'
      } catch (e) {
        alert(e.response?.data || this.$t('admin.failedResolve'))
      }
    },
    async loadFraud() {
      if (this.fraud || this.fraudLoading) return  // already loaded
      this.fraudLoading = true
      try {
        const { data } = await axios.get('/api/admin/fraud', { withCredentials: true })
        this.fraud = data
      } catch (e) {
        console.error('Failed to load fraud panel', e)
      } finally {
        this.fraudLoading = false
      }
    },
    async resetElo(user) {
      if (!confirm(this.$t('admin.resetEloConfirm', { username: user.username }))) return
      try {
        await axios.post(`/api/admin/users/${user.userId}/reset-elo`, {}, { withCredentials: true })
        user.eloReset = true
      } catch (e) {
        alert(e.response?.data || this.$t('admin.resetEloFail'))
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      return new Date(dateStr).toLocaleString()
    }
  }
}
</script>

<style scoped>
.admin-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 20px 64px;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.admin-header h2 {
  color: var(--text-primary);
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -.01em;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: var(--text-secondary);
}

.filter-row label {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-secondary);
}

.refresh-btn {
  background: var(--bg-elevated);
  color: var(--text-primary);
  border: 1px solid var(--border);
  padding: 5px 12px;
  border-radius: var(--r);
  cursor: pointer;
  font-size: 12px;
  font-family: var(--font);
  transition: background var(--transition);
}
.refresh-btn:hover { background: var(--bg-overlay); }

.loading, .empty {
  text-align: center;
  color: var(--text-muted);
  padding: 60px 0;
  font-size: 14px;
}

.admin-tabs {
  display: flex; gap: 2px; border-bottom: 1px solid var(--border); margin-bottom: 20px;
}
.admin-tab {
  padding: 8px 16px; border: none; background: transparent; cursor: pointer;
  font-size: 13px; font-weight: 500; color: var(--text-secondary); font-family: var(--font);
  border-bottom: 2px solid transparent; margin-bottom: -1px;
  transition: color var(--transition);
}
.admin-tab:hover { color: var(--text-primary); }
.admin-tab.active { color: var(--brand); border-bottom-color: var(--brand); font-weight: 600; }
.tab-badge {
  background: var(--red); color: #fff; border-radius: var(--r-full);
  font-size: 10px; font-weight: 700; padding: 1px 6px; margin-left: 5px;
}
.claim {
  font-size: 12px; color: var(--text-secondary); margin-left: 4px;
  background: var(--bg-elevated); border-radius: var(--r); padding: 1px 7px;
}

.reports-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.report-card {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-left: 3px solid var(--red);
  border-radius: var(--r-md);
  padding: 16px;
  transition: border-color var(--transition);
}
.report-card.resolved {
  border-left-color: var(--border);
  opacity: .65;
}

.report-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.badge {
  display: inline-flex;
  align-items: center;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: var(--r-full);
  text-transform: uppercase;
  letter-spacing: .05em;
}
.badge.pending       { background: var(--red-muted);  color: var(--red); }
.badge.resolved-badge { background: rgba(139,148,158,.1); color: var(--text-muted); }

.date { font-size: 12px; color: var(--text-muted); }

.report-body {
  display: flex;
  flex-direction: column;
  gap: 5px;
  margin-bottom: 14px;
}

.report-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  color: var(--text-primary);
}

.label {
  font-weight: 600;
  color: var(--text-secondary);
  min-width: 72px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: .04em;
}

.reason-row { align-items: flex-start; }
.reason-text { color: var(--text-primary); line-height: 1.5; }

.banned-tag {
  background: var(--red);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  padding: 1px 7px;
  border-radius: var(--r-full);
  margin-left: 4px;
  text-transform: uppercase;
  letter-spacing: .04em;
}

.report-actions { display: flex; gap: 8px; }

.ban-btn {
  background: var(--red);
  color: #fff;
  border: none;
  padding: 5px 12px;
  border-radius: var(--r);
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  font-family: var(--font);
  transition: filter var(--transition);
}
.ban-btn:hover { filter: brightness(1.1); }

.unban-btn {
  background: var(--yellow-muted);
  color: var(--yellow);
  border: 1px solid rgba(210,153,34,.3);
  padding: 5px 12px;
  border-radius: var(--r);
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  font-family: var(--font);
  transition: background var(--transition);
}
.unban-btn:hover { background: rgba(210,153,34,.2); }

.resolve-btn {
  background: var(--bg-elevated);
  color: var(--text-secondary);
  border: 1px solid var(--border);
  padding: 5px 12px;
  border-radius: var(--r);
  cursor: pointer;
  font-size: 12px;
  font-family: var(--font);
  transition: background var(--transition);
}
.resolve-btn:hover { background: var(--bg-overlay); }

/* ── Fraud panel ─────────────────────────────────────────────────────────── */
.tab-badge-warn { background: var(--orange); }

.fraud-section-title {
  font-size: 13px; font-weight: 600; color: var(--text-secondary);
  text-transform: uppercase; letter-spacing: .05em; margin-bottom: 10px;
}

.fraud-pair {
  display: flex; align-items: center; justify-content: space-between;
  border-left-color: var(--orange); gap: 12px;
}

.fraud-pair-names {
  display: flex; align-items: center; gap: 10px;
}

.fraud-player {
  color: var(--blue); font-size: 13px; font-weight: 600; text-decoration: none;
}
.fraud-player:hover { text-decoration: underline; }

.fraud-vs {
  font-size: 11px; color: var(--text-muted); font-weight: 500;
}

.fraud-count {
  font-size: 12px; font-weight: 700; color: var(--orange);
  background: var(--orange-muted); border-radius: var(--r-full);
  padding: 2px 10px; white-space: nowrap;
}

.fraud-rate-badge {
  font-size: 12px; color: var(--red);
  background: var(--red-muted); border-radius: var(--r-full);
  padding: 2px 10px; margin-left: 8px;
}

.fraud-games {
  font-size: 12px; color: var(--text-muted); margin-left: 8px;
}
</style>
