<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '../components/layout/AppHeader.vue'
import AppFooter from '../components/layout/AppFooter.vue'
import { guideHeaderNavItems, labGuides } from '../data/labs'
import { getLabDetail, getLabDocument, getLabs } from '../api/labs'

const route = useRoute()
const labs = ref([])
const labDetail = ref(null)
const labDocument = ref(null)
const loading = ref(false)
const errorMessage = ref('')

const labIdNumber = computed(() => {
  const rawId = String(route.params.labId || 'lab1')
  const parsed = Number(rawId.replace(/^lab/i, ''))
  return Number.isFinite(parsed) && parsed > 0 ? parsed : 1
})

const labSlug = computed(() => `lab${labIdNumber.value}`)
const fallbackLab = computed(() => labGuides[labSlug.value] ?? labGuides.lab1)

function formatDuration(minutes) {
  if (!minutes || minutes <= 0) {
    return ''
  }

  if (minutes % 60 === 0) {
    return `${minutes / 60}小时`
  }

  return `${minutes}分钟`
}

function normalizeParagraphs(value) {
  if (!value) {
    return []
  }

  return String(value)
    .split(/\n+/)
    .map((item) => item.trim())
    .filter(Boolean)
}

function createSection(id, title, content, points = []) {
  return {
    id,
    title,
    paragraphs: normalizeParagraphs(content),
    points,
  }
}

const labList = computed(() => {
  if (labs.value.length > 0) {
    return labs.value.map((lab) => ({
      slug: String(lab.labCode || `Lab${lab.labId}`).toLowerCase(),
      title: `${lab.labCode || `Lab${lab.labId}`}：${lab.labName || ''}`.replace('：', lab.labName ? '：' : ''),
      difficulty: lab.difficulty || '未设置',
      duration: formatDuration(lab.estimatedTime) || '未设置',
    }))
  }

  return Object.entries(labGuides).map(([slug, item]) => ({
    slug,
    title: item.title,
    difficulty: item.difficulty,
    duration: item.duration,
  }))
})

const currentLab = computed(() => {
  const fallback = fallbackLab.value
  const detail = labDetail.value
  const document = labDocument.value || detail?.document || null

  if (!detail) {
    return {
      title: fallback.title,
      summary: fallback.summary,
      difficulty: fallback.difficulty,
      duration: fallback.duration,
      category: '',
      startTo: fallback.startTo,
      sections: fallback.sections,
      tasks: [],
      documentTitle: fallback.title,
    }
  }

  const sections = document
    ? [
        createSection('objective', '实验目的', document.docTarget),
        createSection('task', '实验任务', document.docTask),
        createSection('principle', '实验原理', document.docPrinciple),
        createSection('content', '实验内容', document.docContent),
      ]
    : fallback.sections

  return {
    title: `${detail.labCode || `Lab${labIdNumber.value}`}${detail.labName ? `：${detail.labName}` : ''}`,
    summary: detail.labDesc || fallback.summary,
    difficulty: detail.difficulty || fallback.difficulty,
    duration: formatDuration(detail.estimatedTime) || fallback.duration,
    category: detail.category || '',
    startTo: `/labs/${labSlug.value}/editor`,
    sections,
    tasks: Array.isArray(detail.tasks) ? detail.tasks : [],
    documentTitle: document?.docTitle || fallback.title,
  }
})

async function loadLabPage() {
  loading.value = true
  errorMessage.value = ''
  labDetail.value = null
  labDocument.value = null

  try {
    const [labsResult, detailResult, documentResult] = await Promise.allSettled([
      getLabs(),
      getLabDetail(labIdNumber.value),
      getLabDocument(labIdNumber.value),
    ])

    if (labsResult.status === 'fulfilled' && Array.isArray(labsResult.value)) {
      labs.value = labsResult.value
    }

    if (detailResult.status === 'fulfilled' && detailResult.value) {
      labDetail.value = detailResult.value
    }

    if (documentResult.status === 'fulfilled' && documentResult.value) {
      labDocument.value = documentResult.value
    }

    const failedResult = [labsResult, detailResult, documentResult].find((result) => result.status === 'rejected')
    if (failedResult?.status === 'rejected') {
      errorMessage.value = failedResult.reason instanceof Error ? failedResult.reason.message : '加载失败'
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载失败'
  } finally {
    loading.value = false
  }
}

watch(labSlug, () => {
  void loadLabPage()
}, { immediate: true })
</script>

<template>
  <div class="min-h-screen bg-[#FEF8E7] flex flex-col text-slate-900">
    <AppHeader :title="'操作系统实验教学平台'" :nav-items="guideHeaderNavItems" />

    <main class="flex-1 mx-auto w-full max-w-6xl px-4 py-8">
      <div v-if="errorMessage" class="mb-6 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-red-700">
        {{ errorMessage }}
      </div>

      <div v-if="loading" class="mb-6 rounded-xl border border-blue-200 bg-blue-50 px-4 py-3 text-blue-700">
        正在加载实验内容...
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-[260px_minmax(0,1fr)] gap-8">
        <aside class="bg-white/85 border-4 border-blue-300 rounded-2xl shadow-lg p-5 h-fit lg:sticky lg:top-32">
          <div class="flex items-center gap-2 mb-5 pb-4 border-b border-blue-100">
            <Icon class="text-blue-500" icon="ph:books-bold" />
            <h2 class="text-lg font-bold text-blue-700">实验目录</h2>
          </div>

          <div class="space-y-3">
            <RouterLink
              v-for="item in labList"
              :key="item.slug"
              :to="`/labs/${item.slug}`"
              class="block rounded-xl px-4 py-3 border transition-all"
              :class="item.slug === labSlug ? 'bg-blue-600 text-white border-blue-600 shadow-md' : 'bg-blue-50/60 text-slate-700 border-blue-100 hover:bg-blue-100 hover:border-blue-200'"
            >
              <div class="font-semibold">{{ item.title }}</div>
              <div class="text-xs mt-1" :class="item.slug === labSlug ? 'text-blue-100' : 'text-slate-500'">
                {{ item.difficulty }} · {{ item.duration }}
              </div>
            </RouterLink>
          </div>
        </aside>

        <section class="w-full space-y-8">
          <div class="w-full bg-[#FEF8E7] border-4 border-blue-300 rounded-2xl shadow-2xl p-8 lg:p-10 relative overflow-hidden">
            <div class="absolute top-0 left-0 w-full h-2 bg-blue-400/20"></div>
            <div class="absolute -top-10 -right-10 w-32 h-32 bg-blue-100 rounded-full opacity-50"></div>

            <div class="relative z-10 flex flex-col items-center text-center">
              <Icon class="text-7xl text-blue-400 mb-6" icon="ph:notebook-bold" />
              <h2 class="text-4xl lg:text-5xl font-bold text-[#1E40AF] mb-4 tracking-tight">
                {{ currentLab.title }}
              </h2>
              <p class="max-w-3xl text-slate-600 leading-relaxed mb-8">{{ currentLab.summary }}</p>

              <div class="flex gap-4 mb-8 flex-wrap justify-center">
                <span class="px-3 py-1 bg-blue-50 text-blue-600 text-xs font-semibold rounded-full border border-blue-100">
                  难度: {{ currentLab.difficulty }}
                </span>
                <span class="px-3 py-1 bg-green-50 text-green-600 text-xs font-semibold rounded-full border border-green-100">
                  预计耗时: {{ currentLab.duration }}
                </span>
                <span v-if="currentLab.category" class="px-3 py-1 bg-amber-50 text-amber-700 text-xs font-semibold rounded-full border border-amber-100">
                  分类: {{ currentLab.category }}
                </span>
              </div>

              <div class="w-full text-left space-y-6">
                <section
                  v-for="section in currentLab.sections"
                  :key="section.id"
                  class="border-l-4 border-blue-400 pl-5 py-2"
                >
                  <div class="flex items-center gap-2 mb-4">
                    <div class="w-2.5 h-2.5 rounded-full bg-blue-500"></div>
                    <h3 class="text-xl font-bold text-[#1E40AF]">{{ section.title }}</h3>
                  </div>
                  <div class="space-y-4 text-slate-600 leading-relaxed">
                    <p v-for="paragraph in section.paragraphs" :key="paragraph">{{ paragraph }}</p>
                  </div>
                  <ul v-if="section.points?.length" class="mt-4 space-y-2 text-sm text-slate-700">
                    <li v-for="point in section.points" :key="point" class="flex items-start gap-2">
                      <Icon class="mt-0.5 text-blue-500 shrink-0" icon="ph:check-circle-bold" />
                      <span>{{ point }}</span>
                    </li>
                  </ul>
                </section>
              </div>

              <div class="mt-10 flex justify-end w-full">
                <RouterLink
                  :to="currentLab.startTo"
                  class="group px-8 py-4 bg-[#1E40AF] text-white rounded-xl font-bold text-lg shadow-lg hover:bg-blue-800 hover:shadow-blue-200 hover:-translate-y-1 transition-all flex items-center gap-3"
                >
                  开始实验
                  <Icon class="group-hover:animate-bounce" icon="ph:rocket-launch-fill" />
                </RouterLink>
              </div>
            </div>
          </div>
        </section>

      </div>
    </main>

    <AppFooter />
  </div>
</template>
