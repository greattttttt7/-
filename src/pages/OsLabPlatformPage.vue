<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppFooter from '../components/layout/AppFooter.vue'
import AppHeader from '../components/layout/AppHeader.vue'
import {
  dashboardAnnouncements,
  dashboardHeaderNavItems,
  dashboardHistoryItems,
  dashboardTodoItems,
  labGuides,
} from '../data/labs'
import { getCurrentUser, getCurrentUserId } from '../config/session'
import { getLabs } from '../api/labs'
import { getProgress } from '../api/progress'
import { getSubmissions } from '../api/submissions'

const router = useRouter()
const currentAnnouncementIndex = ref(0)
const labs = ref([])
const progressList = ref([])
const submissionList = ref([])
const loading = ref(false)
const errorMessage = ref('')
let timerId = null

const currentUser = computed(() => getCurrentUser())
const currentAnnouncement = computed(() => dashboardAnnouncements[currentAnnouncementIndex.value] ?? '')
const totalLabs = computed(() => labs.value.length || Object.keys(labGuides).length)
const completedLabs = computed(() => progressList.value.filter((item) => item.status === '已完成').length)
const completionRate = computed(() => {
  if (!totalLabs.value) return 0
  return Math.round((completedLabs.value / totalLabs.value) * 100)
})
const dashboardProgress = computed(() => ({
  completed: completedLabs.value,
  total: totalLabs.value,
}))
const progressCircleOffset = computed(() => {
  const total = dashboardProgress.value.total
  if (!total) return 439.6
  return 439.6 * (1 - dashboardProgress.value.completed / total)
})
const dashboardTodoItemsView = computed(() => {
  if (!labs.value.length) {
    return dashboardTodoItems.map((item, index) => ({
      ...item,
      labId: index + 1,
    }))
  }

  const progressMap = new Map(progressList.value.map((item) => [item.labId, item.status]))

  return labs.value.map((lab) => ({
    labId: lab.labId,
    label: `${lab.labCode || `Lab${lab.labId}`}: ${lab.labName}`,
    done: progressMap.get(lab.labId) === '已完成',
  }))
})
const dashboardHistoryItemsView = computed(() => {
  if (!submissionList.value.length) {
    return []
  }

  return submissionList.value.map((item) => ({
    subId: item.subId,
    name: `Lab${item.labId} 提交记录`,
    time: formatTime(item.submitTime),
    status: item.runResult,
    tone: item.runResult === 'Pass' ? 'success' : 'danger',
  }))
})

function formatTime(value) {
  if (!value) return '--'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '--'
  const pad = (num) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function previousAnnouncement() {
  currentAnnouncementIndex.value =
    (currentAnnouncementIndex.value - 1 + dashboardAnnouncements.length) % dashboardAnnouncements.length
}

function nextAnnouncement() {
  currentAnnouncementIndex.value =
    (currentAnnouncementIndex.value + 1) % dashboardAnnouncements.length
}

function openLabGuide(labId) {
  router.push(`/labs/lab${labId}`)
}

function openLabEditor(labId) {
  router.push(`/labs/lab${labId}/editor`)
}
function getHistoryClass(item) {
  return item.tone === 'success' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
}

async function loadDashboard() {
  const userId = getCurrentUserId()
  if (!userId) {
    router.push('/login')
    return
  }

  loading.value = true
  errorMessage.value = ''
  labs.value = []
  progressList.value = []
  submissionList.value = []

  try {
    const [labResult, progressResult] = await Promise.allSettled([
      getLabs(),
      getProgress(userId),
    ])

    if (labResult.status === 'fulfilled' && Array.isArray(labResult.value)) {
      labs.value = labResult.value
    }

    if (progressResult.status === 'fulfilled' && Array.isArray(progressResult.value)) {
      progressList.value = progressResult.value
    }

    const submissionResults = await Promise.allSettled(
      labs.value.map((lab) => getSubmissions(userId, lab.labId)),
    )

    const allSubmissions = []
    for (const result of submissionResults) {
      if (result.status === 'fulfilled' && Array.isArray(result.value)) {
        allSubmissions.push(...result.value)
      }
    }

    submissionList.value = allSubmissions
      .slice()
      .sort((left, right) => {
        const leftTime = Date.parse(left.submitTime || '') || 0
        const rightTime = Date.parse(right.submitTime || '') || 0
        return rightTime - leftTime
      })
      .slice(0, 4)

    const firstFailed = [labResult, progressResult, ...submissionResults].find(
      (result) => result.status === 'rejected',
    )

    if (firstFailed?.status === 'rejected') {
      errorMessage.value = firstFailed.reason instanceof Error ? firstFailed.reason.message : '加载失败'
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  timerId = window.setInterval(() => {
    currentAnnouncementIndex.value =
      (currentAnnouncementIndex.value + 1) % dashboardAnnouncements.length
  }, 5000)

  void loadDashboard()
})

onBeforeUnmount(() => {
  if (timerId) {
    window.clearInterval(timerId)
    timerId = null
  }
})
</script>


<template>
  <div class="min-h-screen bg-slate-50 text-slate-900 flex flex-col">
    <AppHeader title="操作系统实验教学平台" :nav-items="dashboardHeaderNavItems" />

    <main class="flex-1 max-w-6xl mx-auto px-4 py-8 w-full">
      <section class="mb-10">
        <div
          class="relative w-full h-56 bg-gradient-to-r from-blue-600 to-indigo-700 rounded-2xl shadow-lg overflow-hidden flex items-center justify-center group"
        >
          <div class="absolute inset-0 opacity-10">
            <svg class="w-full h-full" preserveAspectRatio="none" viewBox="0 0 100 100">
              <path d="M0 100 C 20 0 50 0 100 100 Z" fill="white"></path>
            </svg>
          </div>

          <div class="relative z-10 text-center px-8 transition-opacity duration-300">
            <div
              class="inline-flex items-center bg-blue-500/30 text-white text-xs font-bold px-3 py-1 rounded-full mb-4 uppercase tracking-wider"
            >
              最新公告
            </div>
            <h2 class="text-2xl md:text-3xl font-bold text-white mb-2">
              {{ currentAnnouncement }}
            </h2>
            <p class="text-blue-100 text-lg opacity-90">
              {{ currentUser ? `欢迎回来，${currentUser.name || currentUser.username}` : '请先登录后查看你的实验进度与提交记录' }}
            </p>
          </div>

          <div class="absolute bottom-6 flex space-x-2">
            <button
              v-for="(_, index) in dashboardAnnouncements"
              :key="index"
              type="button"
              class="w-2.5 h-2.5 rounded-full transition-colors"
              :class="index === currentAnnouncementIndex ? 'bg-white shadow-inner' : 'bg-white/40 hover:bg-white/60'"
              @click="currentAnnouncementIndex = index"
            ></button>
          </div>

          <button
            class="absolute left-4 w-10 h-10 rounded-full bg-white/10 text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity hover:bg-white/20"
            type="button"
            @click="previousAnnouncement"
          >
            <Icon icon="lucide:chevron-left" width="24" />
          </button>
          <button
            class="absolute right-4 w-10 h-10 rounded-full bg-white/10 text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity hover:bg-white/20"
            type="button"
            @click="nextAnnouncement"
          >
            <Icon icon="lucide:chevron-right" width="24" />
          </button>
        </div>
      </section>

      <div v-if="errorMessage" class="mb-6 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-red-700">
        {{ errorMessage }}
      </div>

      <div v-if="loading" class="mb-6 rounded-xl border border-blue-200 bg-blue-50 px-4 py-3 text-blue-700">
        正在加载后端数据...
      </div>

      <div class="grid grid-cols-1 gap-8 md:grid-cols-3">
        <section class="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow flex flex-col items-center">
          <div class="flex items-center justify-between w-full mb-8">
            <h3 class="text-lg font-bold text-slate-800 flex items-center">
              <Icon class="mr-2 text-blue-600" icon="lucide:pie-chart" />
              实验进度
            </h3>
            <span class="text-xs font-medium text-slate-400 uppercase tracking-widest">Progress</span>
          </div>

          <div class="relative flex items-center justify-center mb-6">
            <svg class="w-40 h-40">
              <circle
                class="text-slate-100"
                cx="80"
                cy="80"
                fill="transparent"
                r="70"
                stroke="currentColor"
                stroke-width="8"
              ></circle>
              <circle
                class="text-blue-600 progress-ring__circle"
                cx="80"
                cy="80"
                fill="transparent"
                r="70"
                stroke="currentColor"
                stroke-dasharray="439.6"
                :stroke-dashoffset="progressCircleOffset"
                stroke-linecap="round"
                stroke-width="8"
              ></circle>
            </svg>
            <div class="absolute flex flex-col items-center">
              <span class="text-4xl font-extrabold text-slate-800">{{ completionRate }}%</span>
              <span class="text-xs text-slate-500 font-medium">完成率</span>
            </div>
          </div>

          <div class="text-center">
            <p class="text-slate-600 font-medium">
              已完成 <span class="text-blue-600 font-bold">{{ dashboardProgress.completed }}</span
              >/{{ dashboardProgress.total }} 个实验
            </p>
            <div class="mt-4 flex space-x-1">
              <div
                v-for="index in dashboardProgress.total"
                :key="index"
                class="h-1.5 w-8 rounded-full"
                :class="index <= dashboardProgress.completed ? 'bg-blue-600' : 'bg-slate-100'"
              ></div>
            </div>
          </div>
        </section>

        <section class="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
          <div class="flex items-center justify-between w-full mb-8">
            <h3 class="text-lg font-bold text-slate-800 flex items-center">
              <Icon class="mr-2 text-orange-500" icon="lucide:list-todo" />
              待完成实验
            </h3>
            <span class="text-xs font-medium text-slate-400 uppercase tracking-widest">To-Do List</span>
          </div>

          <ul class="space-y-4">
            <li
              v-for="item in dashboardTodoItemsView"
              :key="item.labId || item.label"
              class="flex items-center justify-between p-3 rounded-xl border transition-colors group"
              :class="
                item.done
                  ? 'bg-slate-50 border-slate-100'
                  : 'bg-white border-slate-200 hover:border-blue-300 cursor-pointer'
              "
              @click="!item.done && openLabGuide(item.labId)"
            >
              <div class="flex items-center">
                <div
                  class="w-6 h-6 rounded-full flex items-center justify-center mr-3"
                  :class="item.done ? 'bg-green-100 text-green-600' : 'border-2 border-slate-200 text-transparent group-hover:border-blue-500'"
                >
                  <Icon :icon="item.done ? 'lucide:check' : 'lucide:circle'" width="14" />
                </div>
                <div>
                  <p class="font-medium text-slate-800">{{ item.label }}</p>
                  <p class="text-xs text-slate-500">{{ item.done ? '已完成' : '待完成' }}</p>
                </div>
              </div>
              <span class="text-xs font-medium" :class="item.done ? 'text-green-600' : 'text-slate-400'">
                {{ item.done ? '完成' : '未完成' }}
              </span>
            </li>
          </ul>
        </section>

        <section class="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
          <div class="flex items-center justify-between w-full mb-8">
            <h3 class="text-lg font-bold text-slate-800 flex items-center">
              <Icon class="mr-2 text-emerald-500" icon="lucide:history" />
              最近提交
            </h3>
            <span class="text-xs font-medium text-slate-400 uppercase tracking-widest">History</span>
          </div>

          <div v-if="dashboardHistoryItemsView.length" class="space-y-3">
            <article
              v-for="item in dashboardHistoryItemsView"
              :key="item.subId || `${item.name}-${item.time}`"
              class="rounded-xl border border-slate-200 bg-slate-50/70 p-4"
            >
              <div class="flex items-start justify-between gap-3">
                <div>
                  <p class="font-semibold text-slate-800">{{ item.name }}</p>
                  <p class="mt-1 text-xs text-slate-500">{{ item.time || '暂无提交时间' }}</p>
                </div>
                <span class="rounded-full px-2.5 py-1 text-xs font-semibold" :class="getHistoryClass(item)">
                  {{ item.status }}
                </span>
              </div>
            </article>
          </div>

          <p v-else class="rounded-xl border border-dashed border-slate-200 bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
            暂无提交记录
          </p>
        </section>
      </div>
    </main>

    <AppFooter />
  </div>
</template>
