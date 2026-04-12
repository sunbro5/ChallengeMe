<template>
  <div class="rules-page">
    <div class="rules-header">
      <h2>{{ $t('gameRules.title') }}</h2>
      <p class="subtitle">{{ $t('gameRules.subtitle') }}</p>
    </div>

    <!-- How ChallengeMe works -->
    <section class="how-it-works">
      <h3>{{ $t('gameRules.howItWorks') }}</h3>
      <ol class="steps">
        <li v-for="(step, i) in steps" :key="i">
          <span class="step-icon"></span>{{ step }}
        </li>
      </ol>
    </section>

    <div v-if="loading" class="state-msg">{{ $t('gameRules.loading') }}</div>
    <div v-else-if="error" class="state-msg error">{{ error }}</div>

    <section v-else class="game-cards">
      <div
        v-for="game in games"
        :key="game.key"
        class="game-card"
        :class="{ active: activeKey === game.key }"
        @click="activeKey = activeKey === game.key ? null : game.key"
      >
        <div class="card-header">
          <span class="card-icon">{{ game.icon }}</span>
          <div class="card-title-block">
            <h3>{{ game.name }}</h3>
            <p class="card-tagline">{{ game.tagline }}</p>
          </div>
          <span class="expand-arrow">{{ activeKey === game.key ? '▲' : '▼' }}</span>
        </div>

        <transition name="expand">
          <div v-if="activeKey === game.key" class="card-body">
            <p class="card-desc">{{ game.description }}</p>

            <div class="rule-section">
              <h4>{{ $t('gameRules.rulesSection') }}</h4>
              <ul>
                <li v-for="(rule, i) in game.rules" :key="i">{{ rule }}</li>
              </ul>
            </div>

            <div class="rule-section">
              <h4>{{ $t('gameRules.howToWin') }}</h4>
              <p>{{ game.howToWin }}</p>
            </div>

            <div v-if="game.tips && game.tips.length" class="rule-section tips">
              <h4>{{ $t('gameRules.tips') }}</h4>
              <ul>
                <li v-for="(tip, i) in game.tips" :key="i">{{ tip }}</li>
              </ul>
            </div>

            <div class="report-note">
              <span class="report-icon">📝</span>
              {{ $t('gameRules.reportNote') }}
            </div>
          </div>
        </transition>
      </div>
    </section>

    <section class="result-states">
      <h3>{{ $t('gameRules.resultStates') }}</h3>
      <div class="states-grid">
        <div class="state-card">
          <span class="state-badge finished">FINISHED</span>
          <p>{{ $t('gameRules.finishedDesc') }}</p>
        </div>
        <div class="state-card">
          <span class="state-badge disputed">DISPUTED</span>
          <p>{{ $t('gameRules.disputedDesc') }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'GameRulesPage',
  data() {
    return {
      games:     [],
      loading:   true,
      error:     null,
      activeKey: null,
    }
  },
  computed: {
    steps() {
      return this.$tm('gameRules.steps')
    }
  },
  async mounted() {
    try {
      const { data } = await axios.get('http://localhost:8080/games', { withCredentials: true })
      this.games = data
    } catch (e) {
      this.error = this.$t('gameRules.couldNotLoad')
    } finally {
      this.loading = false
    }
  },
}
</script>

<style scoped>
.rules-page {
  max-width: 780px;
  margin: 0 auto;
  padding: 28px 20px 48px;
}

.rules-header {
  text-align: center;
  margin-bottom: 32px;
}
.rules-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
  letter-spacing: -.01em;
}
.subtitle {
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1.6;
}

.how-it-works {
  background: var(--brand-muted);
  border: 1px solid rgba(66,184,131,.2);
  border-radius: var(--r-md);
  padding: 18px 22px;
  margin-bottom: 28px;
}
.how-it-works h3 {
  color: var(--brand);
  margin-bottom: 14px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .06em;
}
.steps {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.steps li {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 13px;
  color: var(--text-primary);
  line-height: 1.5;
}
.step-icon {
  font-size: 16px;
  flex-shrink: 0;
  margin-top: 1px;
}

.state-msg {
  text-align: center;
  padding: 32px;
  color: var(--text-muted);
  font-size: 14px;
}
.state-msg.error { color: var(--red); }

.game-cards {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 28px;
}

.game-card {
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  overflow: hidden;
  background: var(--bg-surface);
  cursor: pointer;
  transition: border-color var(--transition), box-shadow var(--transition);
}
.game-card:hover { border-color: var(--brand); }
.game-card.active {
  border-color: var(--brand);
  box-shadow: 0 0 0 1px var(--brand);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px;
  border-left: 3px solid var(--brand);
  user-select: none;
}
.card-icon { font-size: 24px; flex-shrink: 0; }
.card-title-block { flex: 1; }
.card-title-block h3 { font-size: 15px; font-weight: 600; color: var(--text-primary); margin-bottom: 2px; }
.card-tagline { font-size: 12px; color: var(--text-muted); }
.expand-arrow { font-size: 11px; color: var(--text-muted); }

.card-body {
  padding: 0 18px 18px;
  border-top: 1px solid var(--border-muted);
}
.card-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin: 14px 0 16px;
}
.rule-section { margin-bottom: 16px; }
.rule-section h4 {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-secondary);
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: .05em;
}
.rule-section ul {
  padding-left: 18px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.rule-section ul li { font-size: 13px; color: var(--text-primary); line-height: 1.5; }
.rule-section p { font-size: 13px; color: var(--text-primary); line-height: 1.6; }
.tips {
  background: var(--yellow-muted);
  border: 1px solid rgba(210,153,34,.2);
  border-radius: var(--r);
  padding: 12px 16px;
}
.tips h4 { color: var(--yellow); }

.report-note {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: var(--r);
  padding: 10px 14px;
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
  margin-top: 16px;
}
.report-icon { font-size: 14px; flex-shrink: 0; }

.expand-enter-active,
.expand-leave-active { transition: opacity 0.2s ease; }
.expand-enter-from,
.expand-leave-to     { opacity: 0; }

.result-states {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  padding: 18px 22px;
}
.result-states h3 {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-muted);
  margin-bottom: 14px;
  text-transform: uppercase;
  letter-spacing: .06em;
}
.states-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
@media (max-width: 500px) { .states-grid { grid-template-columns: 1fr; } }
.state-card {
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: var(--r);
  padding: 14px 16px;
}
.state-card p { font-size: 12px; color: var(--text-secondary); line-height: 1.6; margin-top: 8px; }

.state-badge {
  display: inline-block;
  padding: 2px 9px;
  border-radius: var(--r-full);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: .05em;
  text-transform: uppercase;
}
.state-badge.finished { background: var(--brand-muted); color: var(--brand); }
.state-badge.disputed { background: var(--orange-muted); color: var(--orange); }
</style>
