import { createI18n } from 'vue-i18n'
import en from './locales/en.json'
import cs from './locales/cs.json'

const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: localStorage.getItem('locale') || 'cs',
  fallbackLocale: 'en',
  messages: { en, cs },
})

export default i18n
