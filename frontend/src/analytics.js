/**
 * Google Analytics integration — loaded only when user consents.
 *
 * Usage:
 *   1. Replace GA_MEASUREMENT_ID with your actual Google Analytics ID (e.g. "G-XXXXXXXXXX").
 *   2. Call initAnalytics('all') after the user accepts all cookies.
 *   3. Call initAnalytics('essential') to ensure GA is NOT loaded.
 */

const GA_MEASUREMENT_ID = 'G-XXXXXXXXXX' // TODO: replace with real ID when adding GA

let gaLoaded = false

/**
 * Initialise analytics based on stored or just-granted consent.
 * @param {'all'|'essential'} consent
 */
export function initAnalytics(consent) {
  if (consent !== 'all') return  // user chose essential-only → do nothing

  if (gaLoaded) return           // already loaded in this session
  gaLoaded = true

  // Inject the gtag script dynamically — never runs unless consent = 'all'
  const script = document.createElement('script')
  script.async = true
  script.src = `https://www.googletagmanager.com/gtag/js?id=${GA_MEASUREMENT_ID}`
  document.head.appendChild(script)

  window.dataLayer = window.dataLayer || []
  function gtag() { window.dataLayer.push(arguments) }
  window.gtag = gtag

  gtag('js', new Date())
  gtag('config', GA_MEASUREMENT_ID, {
    anonymize_ip: true,          // GDPR: mask last octet of IP
    cookie_flags: 'SameSite=None;Secure',
  })
}
