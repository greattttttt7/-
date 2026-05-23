<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearCurrentUser, getCurrentUser } from '../../config/session'

defineProps({
  title: { type: String, default: '' },
  navItems: { type: Array, required: true },
})

const route = useRoute()
const router = useRouter()
const openDropdown = ref('')
let closeTimer = null

const currentPath = computed(() => route.path)
const currentUser = computed(() => getCurrentUser())

function handleProfileClick() {
  if (currentUser.value) {
    router.push('/profile')
    return
  }
  router.push('/login')
}

function handleLogout() {
  clearCurrentUser()
  router.push('/login')
}

function openNavDropdown(label) {
  if (closeTimer) {
    window.clearTimeout(closeTimer)
    closeTimer = null
  }
  openDropdown.value = label
}

function closeNavDropdownSoon(label) {
  if (closeTimer) {
    window.clearTimeout(closeTimer)
  }
  closeTimer = window.setTimeout(() => {
    if (openDropdown.value === label) {
      openDropdown.value = ''
    }
    closeTimer = null
  }, 160)
}

function cancelNavDropdownClose() {
  if (closeTimer) {
    window.clearTimeout(closeTimer)
    closeTimer = null
  }
}

onBeforeUnmount(() => {
  cancelNavDropdownClose()
})
</script>

<template>
  <header class="bg-white border-b border-slate-200 pt-8 pb-4 sticky top-0 z-50 shadow-sm">
    <div class="max-w-6xl mx-auto px-4 flex flex-col items-center">
      <h1 class="text-3xl font-bold text-blue-700 tracking-tight mb-6">{{ title }}</h1>
      <nav class="flex items-center gap-12 flex-wrap justify-center">
        <template v-for="item in navItems" :key="item.label">
          <button
            v-if="item.label === '个人中心'"
            type="button"
            class="flex items-center font-medium text-slate-500 hover:text-blue-600 transition-colors pb-2 border-b-2 border-transparent hover:border-blue-600"
            @click="handleProfileClick"
          >
            <Icon v-if="item.icon" :icon="item.icon" class="mr-2" />
            {{ currentUser ? currentUser.name || currentUser.username : item.label }}
          </button>

          <RouterLink
            v-else-if="item.to"
            :to="item.to"
            class="flex items-center font-semibold pb-2 border-b-2 transition-colors"
            :class="
              item.active || currentPath === item.to
                ? 'text-blue-600 border-blue-600'
                : 'text-slate-500 border-transparent hover:text-blue-600'
            "
          >
            <Icon v-if="item.icon" :icon="item.icon" class="mr-2" />
            {{ item.label }}
          </RouterLink>

          <div
            v-else
            class="relative pb-2"
            @mouseenter="openNavDropdown(item.label)"
            @mouseleave="closeNavDropdownSoon(item.label)"
          >
            <button
              type="button"
              class="flex items-center font-medium text-slate-500 hover:text-blue-600 transition-colors"
              :aria-expanded="openDropdown === item.label"
            >
              <Icon v-if="item.icon" :icon="item.icon" class="mr-2" />
              {{ item.label }}
              <Icon v-if="item.children?.length" icon="lucide:chevron-down" class="ml-1 text-xs" />
            </button>

            <div
              v-if="item.children?.length"
              v-show="openDropdown === item.label"
              class="absolute left-0 top-full z-10 w-56 pt-2"
              @mouseenter="openNavDropdown(item.label)"
              @mouseleave="closeNavDropdownSoon(item.label)"
            >
              <div class="overflow-hidden rounded-lg border border-slate-100 bg-white py-2 shadow-xl">
                <RouterLink
                  v-for="child in item.children"
                  :key="child.label"
                  :to="child.to"
                  class="block px-4 py-2 text-sm text-slate-600 hover:bg-blue-50 hover:text-blue-600"
                  @click="openDropdown = ''"
                >
                  {{ child.label }}
                </RouterLink>
              </div>
            </div>
          </div>
        </template>

        <button
          v-if="currentUser"
          type="button"
          class="flex items-center font-semibold pb-2 border-b-2 text-slate-500 border-transparent hover:text-blue-600 hover:border-blue-600 transition-colors"
          @click="handleLogout"
        >
          退出登录
        </button>
      </nav>
    </div>
  </header>
</template>
