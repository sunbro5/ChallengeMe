<template>
  <div class="admin-page">
    <div class="admin-header">
      <h2>Admin Panel — Reports</h2>
      <div class="filter-row">
        <label>
          <input type="checkbox" v-model="showResolved" @change="loadReports" />
          Show resolved
        </label>
        <button class="refresh-btn" @click="loadReports">Refresh</button>
      </div>
    </div>

    <div v-if="loading" class="loading">Loading...</div>
    <div v-else-if="filtered.length === 0" class="empty">No reports found.</div>

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
            <span class="label">Reporter:</span>
            <span>{{ report.reporterUsername }}</span>
          </div>
          <div class="report-row">
            <span class="label">Reported:</span>
            <span>{{ report.reportedUsername }}</span>
            <span v-if="report.reportedBanned" class="banned-tag">BANNED</span>
          </div>
          <div class="report-row reason-row">
            <span class="label">Reason:</span>
            <span class="reason-text">{{ report.reason }}</span>
          </div>
        </div>
        <div class="report-actions">
          <button
            v-if="!report.reportedBanned"
            class="ban-btn"
            @click="banUser(report)"
          >Ban user</button>
          <button
            v-else
            class="unban-btn"
            @click="unbanUser(report)"
          >Unban user</button>
          <button
            v-if="report.status === 'PENDING'"
            class="resolve-btn"
            @click="resolveReport(report)"
          >Mark resolved</button>
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
        const res = await axios.get('http://localhost:8080/admin/reports', { withCredentials: true })
        this.reports = res.data
      } catch (e) {
        console.error('Failed to load reports', e)
      } finally {
        this.loading = false
      }
    },
    async banUser(report) {
      try {
        await axios.post(`http://localhost:8080/admin/ban/${report.reportedId}`, {}, { withCredentials: true })
        report.reportedBanned = true
      } catch (e) {
        alert(e.response?.data || 'Failed to ban user')
      }
    },
    async unbanUser(report) {
      try {
        await axios.post(`http://localhost:8080/admin/unban/${report.reportedId}`, {}, { withCredentials: true })
        report.reportedBanned = false
      } catch (e) {
        alert(e.response?.data || 'Failed to unban user')
      }
    },
    async resolveReport(report) {
      try {
        await axios.post(`http://localhost:8080/admin/resolve/${report.id}`, {}, { withCredentials: true })
        report.status = 'RESOLVED'
      } catch (e) {
        alert(e.response?.data || 'Failed to resolve report')
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
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.admin-header h2 {
  color: #2c3e50;
  font-size: 20px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: #555;
}

.filter-row label {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.refresh-btn {
  background: #42b883;
  color: white;
  border: none;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.refresh-btn:hover {
  background: #369f6e;
}

.loading, .empty {
  text-align: center;
  color: #999;
  padding: 60px 0;
  font-size: 15px;
}

.reports-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.report-card {
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  padding: 16px;
  border-left: 4px solid #e74c3c;
}

.report-card.resolved {
  border-left-color: #bdc3c7;
  opacity: 0.75;
}

.report-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.badge {
  font-size: 11px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.badge.pending {
  background: #fdecea;
  color: #e74c3c;
}

.badge.resolved-badge {
  background: #eaecee;
  color: #7f8c8d;
}

.date {
  font-size: 12px;
  color: #999;
}

.report-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 14px;
}

.report-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 14px;
}

.label {
  font-weight: 600;
  color: #555;
  min-width: 72px;
}

.reason-row {
  align-items: flex-start;
}

.reason-text {
  color: #2c3e50;
  line-height: 1.5;
}

.banned-tag {
  background: #e74c3c;
  color: white;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 10px;
  margin-left: 4px;
}

.report-actions {
  display: flex;
  gap: 8px;
}

.ban-btn {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.ban-btn:hover {
  background: #c0392b;
}

.unban-btn {
  background: #f39c12;
  color: white;
  border: none;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.unban-btn:hover {
  background: #d68910;
}

.resolve-btn {
  background: #ecf0f1;
  color: #555;
  border: none;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.resolve-btn:hover {
  background: #dde1e2;
}
</style>
