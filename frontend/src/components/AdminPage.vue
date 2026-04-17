<template>
  <div class="admin-page">
    <div class="admin-header">
      <h2>{{ $t('admin.title') }}</h2>
      <div class="filter-row">
        <label>
          <input type="checkbox" v-model="showResolved" @change="loadReports" />
          {{ $t('admin.showResolved') }}
        </label>
        <button class="refresh-btn" @click="loadReports">{{ $t('admin.refresh') }}</button>
      </div>
    </div>

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
          <button
            v-if="!report.reportedBanned"
            class="ban-btn"
            @click="banUser(report)"
          >{{ $t('admin.banUser') }}</button>
          <button
            v-else
            class="unban-btn"
            @click="unbanUser(report)"
          >{{ $t('admin.unbanUser') }}</button>
          <button
            v-if="report.status === 'PENDING'"
            class="resolve-btn"
            @click="resolveReport(report)"
          >{{ $t('admin.markResolved') }}</button>
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
      reports: [],
      loading: true,
      showResolved: false
    }
  },
  computed: {
    filtered() {
      return this.showResolved ? this.reports : this.reports.filter(r => r.status === 'PENDING')
    }
  },
  mounted() {
    this.loadReports()
  },
  methods: {
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
</style>
