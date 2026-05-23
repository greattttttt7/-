import { createRouter, createWebHistory } from 'vue-router'
import OsLabPlatformPage from '../pages/OsLabPlatformPage.vue'
import LabPlatformPage from '../pages/LabPlatformPage.vue'
import CodeEditorPage from '../pages/CodeEditorPage.vue'
import LoginPage from '../pages/LoginPage.vue'
import RegisterPage from '../pages/RegisterPage.vue'
import ProfilePage from '../pages/ProfilePage.vue'
import { getCurrentUser } from '../config/session'

const routes = [
  { path: '/login', name: 'login', component: LoginPage },
  { path: '/register', name: 'register', component: RegisterPage },
  { path: '/profile', name: 'profile', component: ProfilePage },
  { path: '/', name: 'home', component: OsLabPlatformPage },
  { path: '/labs/:labId(lab[1-5])', name: 'lab-guide', component: LabPlatformPage },
  { path: '/labs/:labId(lab[1-5])/editor', name: 'lab-editor', component: CodeEditorPage },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to) => {
  const currentUser = getCurrentUser()
  const publicRoutes = new Set(['login', 'register'])

  if (!currentUser && !publicRoutes.has(to.name)) {
    return { name: 'login' }
  }

  if (currentUser && publicRoutes.has(to.name)) {
    return { name: 'home' }
  }

  return true
})

export default router
