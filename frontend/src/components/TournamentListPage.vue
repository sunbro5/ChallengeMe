<template>
  <div class="tournament-list-page">
    <div class="page-header">
      <div>
        <h2>{{ $t('tournament.title') }}</h2>
        <p class="subtitle">{{ $t('tournament.subtitle') }}</p>
      </div>
      <button class="btn-create" @click="showCreate = true">{{ $t('tournament.createBtn') }}</button>
    </div>

    <div v-if="loading" class="state-msg">{{ $t('common.loading') }}</div>
    <div v-else-if="error" class="state-msg error">{{ error }}</div>
    <div v-else-if="!tournaments.length" class="state-msg">{{ $t('tournament.noActive') }}</div>

    <div v-else class="tournament-grid">
      <div
        v-for="t in tournaments" :key="t.id"
        class="tournament-card"
        :class="t.status.toLowerCase()"
        @click="$router.push(`/tournaments/${t.id}`)"
      >
        <div class="tc-top">
          <span class="tc-name">{{ t.name }}</span>
          <span class="tc-status" :class="t.status.toLowerCase()">
            {{ $t(`tournament.${t.status.toLowerCase()}`) }}
          </span>
        </div>
        <div class="tc-meta">
          <span>{{ gameIcon(t.gameType) }} {{ gameLabel(t.gameType) }}</span>
          <span class="tc-players">{{ $t('tournament.waitingForPlayers', { count: t.participantCount, capacity: t.capacity }) }}</span>
        </div>
        <div class="tc-footer">
          <span class="tc-creator">{{ $t('tournament.createdBy') }} {{ t.creatorUsername }}</span>
          <span class="tc-arrow">→</span>
        </div>
      </div>
    </div>

    <!-- ── Create modal ──────────────────────────────────────────────────── -->
    <div v-if="showCreate" class="modal-overlay" @click.self="showCreate = false">
      <div class="modal">
        <h3>{{ $t('tournament.createTitle') }}</h3>

        <div class="field">
          <label>{{ $t('tournament.nameLabel') }}</label>
          <input
            v-model="form.name"
            type="text"
            maxlength="80"
            :placeholder="$t('tournament.namePlaceholder')"
          />
        </div>

        <div class="field">
          <label>{{ $t('tournament.gameLabel') }}</label>
          <select v-model="form.gameType">
            <option v-for="g in games" :key="g.key" :value="g.key">
              {{ g.icon }} {{ currentLocale === 'cs' ? g.nameCs : g.nameEn }}
            </option>
          </select>
        </div>

        <div class="field">
          <label>{{ $t('tournament.capacityLabel') }}</label>
          <div class="cap-row">
            <button
              :class="['cap-btn', form.capacity === 4 ? 'active' : '']"
              @click="form.capacity = 4"
            >{{ $t('tournament.cap4') }}</button>
            <button
              :class="['cap-btn', form.capacity === 8 ? 'active' : '']"
              @click="form.capacity = 8"
            >{{ $t('tournament.cap8') }}</button>
          </div>
        </div>

        <p v-if="createError" class="msg-error">{{ createError }}</p>

        <div class="modal-actions">
          <button class="btn-ghost" @click="showCreate = false">{{ $t('common.cancel') }}</button>
          <button
            class="btn-primary"
            :disabled="creating || !form.name || !form.gameType"
            @click="createTournament"
          >{{ creating ? $t('tournament.creating') : $t('tournament.create') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { useI18n } from 'vue-i18n'

export default {
  name: 'TournamentListPage',
  setup() {
    const { locale } = useI18n()
    return { locale }
  },
  data() {
    return {
      tournaments: [],
      games: [],
      gameMeta: {},
      loading: true,
      error: '',
      showCreate: false,
      creating: false,
      createError: '',
      form: { name: '', gameType: '', capacity: 4 },
    }
  },
  computed: {
    currentLocale() { return this.locale },
  },
  async mounted() {
    await Promise.all([this.loadGames(), this.loadTournaments()])
  },
  methods: {
    async loadGames() {
      try {
        const { data } = await axios.get('/api/games', { withCredentials: true })
        this.games = data
        data.forEach(g => { this.gameMeta[g.key] = g })
        if (data.length) this.form.gameType = data[0].key
      } catch { /* non-fatal */ }
    },

    async loadTournaments() {
      this.loading = true; this.error = ''
      try {
        const { data } = await axios.get('/api/tournaments', { withCredentials: true })
        this.tournaments = data
      } catch {
        this.error = this.$t('tournament.couldNotLoad')
      } finally {
        this.loading = false
      }
    },

    async createTournament() {
      this.creating = true; this.createError = ''
      try {
        const { data } = await axios.post('/api/tournaments', {
          name:     this.form.name,
          gameType: this.form.gameType,
          capacity: this.form.capacity,
        }, { withCredentials: true })
        this.showCreate = false
        this.$router.push(`/tournaments/${data.id}`)
      } catch (err) {
        this.createError = err.response?.data || this.$t('tournament.couldNotCreate')
      } finally {
        this.creating = false
      }
    },

    gameIcon(key)  { return this.gameMeta[key]?.icon  || '' },
    gameLabel(key) {
      const g = this.gameMeta[key]
      if (!g) return key
      return this.currentLocale === 'cs' ? g.nameCs : g.nameEn
    },
  },
}
</script>

<style scoped>
.tournament-list-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 32px 20px 64px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 28px;
  flex-wrap: wrap;
}
h2 { font-size: 20px; font-weight: 700; margin: 0 0 4px; letter-spacing: -.01em; }
.subtitle { color: var(--text-muted); font-size: 13px; margin: 0; }

.btn-create {
  background: var(--brand); color: #fff; border: none; border-radius: var(--r);
  padding: 8px 18px; font-size: 13px; font-weight: 600; font-family: var(--font);
  cursor: pointer; white-space: nowrap; flex-shrink: 0;
  transition: background var(--transition);
}
.btn-create:hover { background: var(--brand-hover); }

.state-msg       { text-align: center; padding: 60px; color: var(--text-muted); }
.state-msg.error { color: var(--red); }

.tournament-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tournament-card {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-left: 4px solid var(--border);
  border-radius: var(--r-md);
  padding: 16px 20px;
  cursor: pointer;
  transition: background var(--transition), border-left-color var(--transition);
}
.tournament-card:hover { background: var(--bg-elevated); }
.tournament-card.open        { border-left-color: var(--brand); }
.tournament-card.in_progress { border-left-color: var(--blue); }
.tournament-card.finished    { border-left-color: var(--text-muted); opacity: .75; }

.tc-top {
  display: flex; align-items: center; justify-content: space-between; gap: 12px;
  margin-bottom: 6px;
}
.tc-name   { font-size: 15px; font-weight: 700; color: var(--text-primary); }
.tc-status {
  font-size: 10px; font-weight: 700; text-transform: uppercase;
  letter-spacing: .05em; padding: 2px 8px; border-radius: var(--r-full); flex-shrink: 0;
}
.tc-status.open        { background: var(--brand-muted);  color: var(--brand); }
.tc-status.in_progress { background: var(--blue-muted);   color: var(--blue); }
.tc-status.finished    { background: rgba(139,148,158,.1); color: var(--text-muted); }

.tc-meta {
  display: flex; gap: 16px; font-size: 12px; color: var(--text-secondary); margin-bottom: 10px;
}
.tc-players { color: var(--text-muted); }

.tc-footer {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 11px; color: var(--text-muted);
}
.tc-arrow { font-size: 14px; color: var(--text-muted); }

/* ── Modal ─────────────────────────────────────────────────────────── */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,.7);
  z-index: 2000; display: flex; align-items: center; justify-content: center;
}
.modal {
  background: var(--bg-surface); border: 1px solid var(--border);
  border-radius: var(--r-xl); padding: 26px 28px; width: 380px; max-width: 94vw;
  color: var(--text-primary); box-shadow: var(--shadow-lg);
  display: flex; flex-direction: column; gap: 14px;
}
.modal h3 { margin: 0; font-size: 16px; font-weight: 700; }

.field { display: flex; flex-direction: column; gap: 5px; }
.field label {
  font-size: 11px; font-weight: 600; color: var(--text-secondary);
  text-transform: uppercase; letter-spacing: .04em;
}
.field input, .field select {
  background: var(--bg-elevated); color: var(--text-primary);
  border: 1px solid var(--border); border-radius: var(--r);
  padding: 8px 11px; font-size: 13px; font-family: var(--font); outline: none;
  transition: border-color var(--transition); width: 100%; box-sizing: border-box;
}
.field input:focus, .field select:focus { border-color: var(--brand); }
.field input::placeholder { color: var(--text-muted); }

.cap-row { display: flex; gap: 8px; }
.cap-btn {
  flex: 1; background: var(--bg-elevated); border: 1px solid var(--border);
  border-radius: var(--r); padding: 8px; font-size: 13px; font-family: var(--font);
  color: var(--text-secondary); cursor: pointer; transition: all var(--transition);
}
.cap-btn.active {
  background: var(--brand-muted); border-color: rgba(66,184,131,.4); color: var(--brand); font-weight: 600;
}
.cap-btn:hover:not(.active) { border-color: var(--brand); color: var(--text-primary); }

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
  .tournament-list-page { padding: 20px 14px 76px; }
  .page-header { flex-direction: column; }
}
</style>
