<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal">
      <h3>{{ $t('player.challengeModal.title') }}: <strong>{{ targetUsername }}</strong></h3>

      <!-- Location status -->
      <p v-if="locating" class="loc-status">{{ $t('player.challengeModal.locating') }}</p>
      <p v-else-if="!hasLocation" class="loc-warn">{{ $t('player.challengeModal.noLocation') }}</p>

      <!-- Game picker -->
      <div class="field">
        <label>{{ $t('player.challengeModal.pickGame') }}</label>
        <select v-model="form.gameType">
          <option v-for="g in games" :key="g.key" :value="g.key">
            {{ g.icon }} {{ currentLocale === 'cs' ? g.nameCs : g.nameEn }}
          </option>
        </select>
      </div>

      <!-- Date/time -->
      <div class="field">
        <label>{{ $t('player.challengeModal.when') }}</label>
        <input type="datetime-local" v-model="form.scheduledAt" :min="minDateTime" />
      </div>

      <!-- Location name -->
      <div class="field">
        <label>{{ $t('player.challengeModal.locationDesc') }} <span class="optional">({{ $t('common.optional') }})</span></label>
        <input
          v-model="form.locationName"
          type="text"
          maxlength="80"
          :placeholder="$t('map.locationNamePlaceholder')"
        />
      </div>

      <!-- Team mode toggle (only for team-eligible games) -->
      <label v-if="isTeamEligible" class="team-toggle-label">
        <input type="checkbox" v-model="form.teamMode" class="team-checkbox" />
        {{ $t('map.teamModeLabel') }}
      </label>

      <p v-if="error" class="msg-error">{{ error }}</p>
      <p v-if="success" class="msg-ok">{{ $t('player.challengeModal.sent') }}</p>

      <div class="modal-actions">
        <button class="btn-ghost" @click="$emit('close')">{{ $t('common.cancel') }}</button>
        <button class="btn-primary" :disabled="busy || !hasLocation || !form.gameType || !form.scheduledAt" @click="send">
          {{ busy ? $t('player.challengeModal.sending') : $t('player.challengeModal.send') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { useI18n } from 'vue-i18n'

const TEAM_GAME_KEYS = new Set([
  'PUB_QUIZ', 'BEER_PONG', 'FOOTBALL', 'BASKETBALL',
  'UNO', 'KNIFFEL', 'JENGA', 'DOMINO', 'MEXICO_DICE', 'LIAR_DICE',
])

export default {
  name: 'ChallengeModal',
  emits: ['close'],
  props: {
    targetUsername: { type: String, required: true },
  },
  setup() {
    const { locale } = useI18n()
    return { locale }
  },
  data() {
    const now = new Date()
    now.setMinutes(now.getMinutes() + 30)
    const pad = n => String(n).padStart(2, '0')
    const defaultDt = `${now.getFullYear()}-${pad(now.getMonth()+1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`

    return {
      games: [],
      locating: true,
      hasLocation: false,
      latitude: null,
      longitude: null,
      form: {
        gameType: '',
        scheduledAt: defaultDt,
        locationName: '',
        teamMode: false,
      },
      busy: false,
      error: '',
      success: false,
    }
  },
  computed: {
    currentLocale() { return this.locale },
    isTeamEligible() { return TEAM_GAME_KEYS.has(this.form.gameType) },
    minDateTime() {
      const now = new Date()
      const pad = n => String(n).padStart(2, '0')
      return `${now.getFullYear()}-${pad(now.getMonth()+1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`
    },
  },
  async mounted() {
    await this.loadGames()
    this.getLocation()
  },
  methods: {
    async loadGames() {
      try {
        const { data } = await axios.get('/api/games', { withCredentials: true })
        this.games = data
        if (data.length) this.form.gameType = data[0].key
      } catch { /* non-fatal */ }
    },

    getLocation() {
      if (!navigator.geolocation) {
        this.locating = false
        return
      }
      const timer = setTimeout(() => {
        this.locating = false
      }, 6000)
      navigator.geolocation.getCurrentPosition(
        pos => {
          clearTimeout(timer)
          this.latitude   = pos.coords.latitude
          this.longitude  = pos.coords.longitude
          this.hasLocation = true
          this.locating    = false
        },
        () => {
          clearTimeout(timer)
          this.locating = false
        }
      )
    },

    async send() {
      this.error   = ''
      this.success = false
      this.busy    = true
      try {
        await axios.post('/api/events', {
          gameType:       this.form.gameType,
          scheduledAt:    this.form.scheduledAt,
          locationName:    this.form.locationName || null,
          latitude:        this.latitude,
          longitude:       this.longitude,
          invitedUsername: this.targetUsername,
          teamMode:        this.form.teamMode && this.isTeamEligible,
        }, { withCredentials: true })
        this.success = true
        setTimeout(() => this.$emit('close'), 1400)
      } catch (err) {
        this.error = err.response?.data || 'Could not send challenge'
      } finally {
        this.busy = false
      }
    },
  },
}
</script>

<style scoped>
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,.7);
  z-index: 2000;
  display: flex; align-items: center; justify-content: center;
}
.modal {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--r-xl);
  padding: 26px 28px;
  width: 380px;
  max-width: 94vw;
  color: var(--text-primary);
  box-shadow: var(--shadow-lg);
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.modal h3 { margin: 0; font-size: 16px; font-weight: 700; }

.loc-status { font-size: 12px; color: var(--text-muted); margin: 0; }
.loc-warn   { font-size: 12px; color: var(--yellow);     margin: 0; }

.field { display: flex; flex-direction: column; gap: 5px; }
.field label {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: .04em;
}
.optional { text-transform: none; font-weight: 400; color: var(--text-muted); }

.field select,
.field input[type="datetime-local"],
.field input[type="text"] {
  background: var(--bg-elevated);
  color: var(--text-primary);
  border: 1px solid var(--border);
  border-radius: var(--r);
  padding: 8px 11px;
  font-size: 13px;
  font-family: var(--font);
  outline: none;
  transition: border-color var(--transition);
  width: 100%;
  box-sizing: border-box;
}
.field select:focus,
.field input:focus { border-color: var(--brand); }
.field input::placeholder { color: var(--text-muted); }

.modal-actions {
  display: flex; gap: 8px; justify-content: flex-end; margin-top: 4px;
}
.btn-ghost {
  background: var(--bg-elevated); color: var(--text-primary);
  border: 1px solid var(--border); border-radius: var(--r);
  padding: 7px 16px; cursor: pointer; font-family: var(--font); font-size: 13px;
  transition: background var(--transition);
}
.btn-ghost:hover { background: var(--bg-overlay); }
.btn-primary {
  background: var(--brand); color: #fff; border: none;
  border-radius: var(--r); padding: 7px 18px;
  font-weight: 600; cursor: pointer; font-family: var(--font); font-size: 13px;
  transition: background var(--transition);
}
.btn-primary:hover:not(:disabled) { background: var(--brand-hover); }
.btn-primary:disabled { opacity: .45; cursor: default; }

.msg-error { color: var(--red);   font-size: 12px; margin: 0; }
.msg-ok    { color: var(--brand); font-size: 12px; margin: 0; }

.team-toggle-label {
  display: flex; align-items: center; gap: 8px;
  font-size: 13px; color: var(--text-primary); cursor: pointer;
  background: var(--bg-elevated); border: 1px solid var(--border);
  border-radius: var(--r); padding: 9px 12px;
  transition: border-color var(--transition);
}
.team-toggle-label:hover { border-color: var(--brand); }
.team-checkbox { accent-color: var(--brand); width: 15px; height: 15px; cursor: pointer; }
</style>
