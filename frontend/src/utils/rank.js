export const RANKS = [
  { key: 'diamond',  min: 1500, color: '#89dceb' },
  { key: 'platinum', min: 1300, color: '#cba6f7' },
  { key: 'gold',     min: 1100, color: '#c9a227' },
  { key: 'silver',   min:  900, color: '#8a9ba8' },
  { key: 'bronze',   min:    0, color: '#a0673a' },
]

/**
 * Returns the rank object for a given ELO rating.
 * @param {number} rating
 * @returns {{ key: string, min: number, color: string }}
 */
export function getRank(rating) {
  return RANKS.find(r => rating >= r.min) ?? RANKS[RANKS.length - 1]
}
