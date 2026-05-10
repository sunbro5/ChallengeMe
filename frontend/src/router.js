import { createRouter, createWebHistory } from 'vue-router'
import HomePage            from './components/HomePage.vue'
import LoginPage           from './components/LoginPage.vue'
import RegisterPage        from './components/RegisterPage.vue'
import TermsPage           from './components/TermsPage.vue'
import PrivacyPage         from './components/PrivacyPage.vue'
import MapPage             from './components/MapPage.vue'
import FriendsPage         from './components/FriendsPage.vue'
import AdminPage           from './components/AdminPage.vue'
import LeaderboardPage     from './components/LeaderboardPage.vue'
import PlayerPage          from './components/PlayerPage.vue'
import GameRulesPage       from './components/GameRulesPage.vue'
import MyGamesPage         from './components/MyGamesPage.vue'
import EventDetailPage     from './components/EventDetailPage.vue'
import ForgotPasswordPage    from './components/ForgotPasswordPage.vue'
import ResetPasswordPage     from './components/ResetPasswordPage.vue'
import TournamentListPage         from './components/TournamentListPage.vue'
import TournamentDetailPage        from './components/TournamentDetailPage.vue'
import TeamTournamentDetailPage    from './components/TeamTournamentDetailPage.vue'

const routes = [
  { path: '/',                    component: HomePage },
  { path: '/login',               component: LoginPage },
  { path: '/register',            component: RegisterPage },
  { path: '/tos',                 component: TermsPage },
  { path: '/privacy',             component: PrivacyPage },
  { path: '/forgot-password',     component: ForgotPasswordPage },
  { path: '/reset-password',      component: ResetPasswordPage },
  { path: '/map',                 component: MapPage,         meta: { requiresAuth: true } },
  { path: '/friends',             component: FriendsPage,     meta: { requiresAuth: true } },
  { path: '/leaderboard',         component: LeaderboardPage, meta: { requiresAuth: true } },
  { path: '/player/:username',    component: PlayerPage,      meta: { requiresAuth: true } },
  { path: '/games',               component: GameRulesPage,   meta: { requiresAuth: true } },
  { path: '/my-games',            component: MyGamesPage,     meta: { requiresAuth: true } },
  { path: '/event/:id',           component: EventDetailPage,     meta: { requiresAuth: true } },
  { path: '/tournaments',         component: TournamentListPage,      meta: { requiresAuth: true } },
  { path: '/tournaments/:id',     component: TournamentDetailPage,    meta: { requiresAuth: true } },
  { path: '/team-tournaments/:id', component: TeamTournamentDetailPage, meta: { requiresAuth: true } },
  {
    path: '/admin',
    component: AdminPage,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true'
  const isAdmin    = localStorage.getItem('role') === 'ADMIN'

  if (to.meta.requiresAuth && !isLoggedIn)       next('/login')
  else if (to.meta.requiresAdmin && !isAdmin)    next('/map')
  else                                            next()
})

export default router
