<template>
  <transition name="cookie-slide">
    <div v-if="visible" class="cookie-banner" role="region" aria-label="Cookie consent">
      <div class="cookie-content">
        <p class="cookie-text">
          <template v-if="$i18n.locale === 'cs'">
            Používáme <strong>nezbytné cookies</strong> pro přihlášení a volitelně
            <strong>analytické cookies</strong> (Google Analytics) pro zlepšování služby.
            <router-link to="/privacy#cookies" class="cookie-link">Více informací</router-link>
          </template>
          <template v-else>
            We use <strong>essential cookies</strong> for login and optionally
            <strong>analytics cookies</strong> (Google Analytics) to improve the service.
            <router-link to="/privacy#cookies" class="cookie-link">More info</router-link>
          </template>
        </p>
        <div class="cookie-actions">
          <button class="btn-essential" @click="acceptEssential">
            {{ $i18n.locale === 'cs' ? 'Pouze nutné' : 'Essential only' }}
          </button>
          <button class="btn-accept" @click="acceptAll">
            {{ $i18n.locale === 'cs' ? 'Přijmout vše' : 'Accept all' }}
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'CookieBanner',
  emits: ['consent'],
  data() {
    return {
      visible: false,
    }
  },
  mounted() {
    const saved = localStorage.getItem('cookieConsent')
    if (!saved) {
      // Small delay so the page renders first
      setTimeout(() => { this.visible = true }, 600)
    } else {
      this.$emit('consent', saved)
    }
  },
  methods: {
    acceptAll() {
      localStorage.setItem('cookieConsent', 'all')
      this.$emit('consent', 'all')
      this.visible = false
    },
    acceptEssential() {
      localStorage.setItem('cookieConsent', 'essential')
      this.$emit('consent', 'essential')
      this.visible = false
    },
    /** Called from App.vue when user clicks "Cookie settings" in footer */
    reset() {
      localStorage.removeItem('cookieConsent')
      this.visible = true
    },
  },
}
</script>

<style scoped>
.cookie-banner {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 3000;
  background: var(--bg-surface);
  border-top: 1px solid var(--border);
  box-shadow: 0 -4px 24px rgba(0,0,0,.25);
  padding: 16px 20px;
  /* sit above mobile bottom nav (z-index 100) */
}

.cookie-content {
  max-width: 860px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.cookie-text {
  flex: 1;
  font-size: 13px;
  color: var(--text-primary);
  line-height: 1.5;
  margin: 0;
}

.cookie-link {
  color: var(--brand);
  text-decoration: none;
  white-space: nowrap;
}
.cookie-link:hover { text-decoration: underline; }

.cookie-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.btn-essential {
  background: var(--bg-elevated);
  color: var(--text-primary);
  border: 1px solid var(--border);
  border-radius: var(--r);
  padding: 8px 16px;
  font-size: 13px;
  font-family: var(--font);
  cursor: pointer;
  transition: background var(--transition);
}
.btn-essential:hover { background: var(--bg-overlay); }

.btn-accept {
  background: var(--brand);
  color: #fff;
  border: none;
  border-radius: var(--r);
  padding: 8px 18px;
  font-size: 13px;
  font-weight: 600;
  font-family: var(--font);
  cursor: pointer;
  transition: background var(--transition);
}
.btn-accept:hover { background: var(--brand-hover); }

/* Slide up animation */
.cookie-slide-enter-active { transition: transform .3s ease, opacity .3s ease; }
.cookie-slide-leave-active { transition: transform .25s ease, opacity .25s ease; }
.cookie-slide-enter-from  { transform: translateY(100%); opacity: 0; }
.cookie-slide-leave-to    { transform: translateY(100%); opacity: 0; }

@media (max-width: 600px) {
  .cookie-content { flex-direction: column; align-items: stretch; }
  .cookie-actions { justify-content: flex-end; }
}
</style>
