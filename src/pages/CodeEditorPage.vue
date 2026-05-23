<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { basicSetup, EditorView } from 'codemirror'
import { python } from '@codemirror/lang-python'
import AppHeader from '../components/layout/AppHeader.vue'
import { editorFinalLine, guideHeaderNavItems, labGuides } from '../data/labs'
import { getLabDetail } from '../api/labs'
import { getCurrentUserId } from '../config/session'
import { getSubmissions, getTask, getTaskAnswer, getTaskSource, runSubmission, saveSubmission } from '../api/submissions'
import { saveProgress } from '../api/progress'

const route = useRoute()
const router = useRouter()
const editorRoot = ref(null)
const editorView = ref(null)
const code = ref('')
const outputLines = ref([])
const loading = ref(false)
const saving = ref(false)
const running = ref(false)
const errorMessage = ref('')
const currentTask = ref(null)
const taskList = ref([])
const currentSubmission = ref(null)
const copied = ref(false)
let mounted = false
let copiedTimer = null

const labId = computed(() => Number(String(route.params.labId || 'lab1').replace(/^lab/i, '')) || 1)
const routeTaskId = computed(() => Number(route.query.taskId || 0) || 0)
const labKey = computed(() => `lab${labId.value}`)
const labGuide = computed(() => labGuides[labKey.value] ?? labGuides.lab1)
const editorTitle = computed(() => `${labGuide.value.title} 代码编辑器`)
const taskIndex = computed(() => taskList.value.findIndex((item) => item.taskId === currentTask.value?.taskId))
const taskFileName = computed(() => {
  const filePath = currentTask.value?.filePath || ''
  const normalized = String(filePath).replace(/\\/g, '/')
  return normalized ? normalized.split('/').pop() || '' : ''
})
const taskDisplayTitle = computed(() => {
  if (!currentTask.value) {
    return '请选择任务'
  }
  const taskName = currentTask.value.taskName || `Task ${currentTask.value.taskId}`
  const fileName = taskFileName.value || 'unknown.c'
  return `${taskName} (${fileName})`
})

function runCode(source) {
  const lines = source.split(/\r?\n/)
  const results = []

  for (const rawLine of lines) {
    const line = rawLine.trim()
    if (!line || line.startsWith('#')) continue

    if (line.startsWith('print(') && line.endsWith(')')) {
      const content = line.slice(6, -1).trim()
      const quoted = content.match(/^(['"])(.*)\1$/)
      results.push(quoted ? quoted[2] : content)
    }
  }

  return results
}

function formatTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : ''
}

function applyTaskList(detail) {
  taskList.value = Array.isArray(detail?.tasks) ? detail.tasks : []
}

function replaceRouteTask(nextTaskId) {
  router.replace({
    path: route.path,
    query: { ...route.query, taskId: String(nextTaskId) },
  })
}

function applyEditorTask(task, source) {
  currentTask.value = task
  code.value = source || ''
  editorView.value?.destroy()
  editorView.value = new EditorView({
    doc: code.value,
    extensions: [
      basicSetup,
      python(),
      EditorView.updateListener.of((update) => {
        if (update.docChanged) {
          code.value = update.state.doc.toString()
        }
      }),
    ],
    parent: editorRoot.value,
  })
}

function applySubmission(submission) {
  currentSubmission.value = submission || null
  outputLines.value = [
    submission?.resultDetail || '最近一次提交已从后端加载。',
    submission?.submitTime ? `提交时间：${formatTime(submission.submitTime)}` : '',
    editorFinalLine,
  ].filter(Boolean)
}

function getFallbackTaskId(preferredTaskId = 0) {
  const availableTaskIds = taskList.value.map((item) => item.taskId)
  if (availableTaskIds.includes(preferredTaskId)) {
    return preferredTaskId
  }

  return availableTaskIds[0] || 0
}

async function loadTaskContext(nextTaskId = null) {
  const userId = getCurrentUserId()
  if (!userId) {
    router.push('/login')
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const detail = await getLabDetail(labId.value)
    applyTaskList(detail)

    const targetTaskId = getFallbackTaskId(nextTaskId || routeTaskId.value)
    if (!targetTaskId) {
      throw new Error('实验暂无任务')
    }

    if (routeTaskId.value !== targetTaskId) {
      replaceRouteTask(targetTaskId)
    }

    let taskDetail = null
    let source = ''
    try {
      ;[taskDetail, source] = await Promise.all([
        getTask(targetTaskId),
        getTaskSource(targetTaskId, userId),
      ])
    } catch (taskError) {
      const fallbackTaskId = getFallbackTaskId()
      if (!fallbackTaskId || fallbackTaskId === targetTaskId) {
        throw taskError
      }
      if (routeTaskId.value !== fallbackTaskId) {
        replaceRouteTask(fallbackTaskId)
      }
      ;[taskDetail, source] = await Promise.all([
        getTask(fallbackTaskId),
        getTaskSource(fallbackTaskId, userId),
      ])
    }

    const submissions = await getSubmissions(userId, labId.value)

    applyEditorTask(taskDetail, source)

    const latestSubmission = Array.isArray(submissions) && submissions.length > 0 ? submissions[0] : null
    if (latestSubmission?.codeContent && !code.value) {
      code.value = latestSubmission.codeContent
    }

    if (latestSubmission) {
      applySubmission(latestSubmission)
    } else {
      outputLines.value = ['请先编写代码，再点击运行或查看答案。']
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载任务失败'
    currentTask.value = null
    code.value = ''
    outputLines.value = []
    editorView.value?.destroy()
    editorView.value = null
  } finally {
    loading.value = false
  }
}

function previousTask() {
  if (!taskList.value.length || taskIndex.value < 0) return
  const index = taskIndex.value <= 0 ? taskList.value.length - 1 : taskIndex.value - 1
  void loadTaskContext(taskList.value[index].taskId)
}

function nextTask() {
  if (!taskList.value.length || taskIndex.value < 0) return
  const index = taskIndex.value >= taskList.value.length - 1 ? 0 : taskIndex.value + 1
  void loadTaskContext(taskList.value[index].taskId)
}

async function handleRun() {
  const userId = getCurrentUserId()
  if (!userId || running.value || !currentTask.value) {
    return
  }

  running.value = true
  errorMessage.value = ''

  try {
    outputLines.value = ['正在提交代码到评测系统...', '']

    const judgeResult = await runSubmission({
      userId,
      labId: labId.value,
      taskId: currentTask.value.taskId,
      fileName: taskFileName.value,
      codeContent: code.value,
    })

    const output = []

    output.push(`=== 评测结果 ===`)
    output.push(`状态: ${judgeResult?.success ? '✓ 通过' : '✗ 失败'}`)

    if (judgeResult?.message) {
      output.push(`消息: ${judgeResult.message}`)
    }

    if (judgeResult?.durationMillis != null) {
      output.push(`耗时: ${(judgeResult.durationMillis / 1000).toFixed(2)} 秒`)
    }

    if (judgeResult?.exitCode != null) {
      output.push(`退出码: ${judgeResult.exitCode}`)
    }

    if (judgeResult?.timeout) {
      output.push(`警告: 程序执行超时`)
    }

    if (judgeResult?.stdout) {
      output.push('')
      output.push(`=== 标准输出 (stdout) ===`)
      output.push(judgeResult.stdout)
    }

    if (judgeResult?.stderr) {
      output.push('')
      output.push(`=== 错误输出 (stderr) ===`)
      output.push(judgeResult.stderr)
    }

    if (judgeResult?.combinedLog) {
      output.push('')
      output.push(`=== 完整日志 ===`)
      output.push(judgeResult.combinedLog)
    }

    output.push('')
    output.push(editorFinalLine)

    outputLines.value = output

    await saveProgress({
      userId,
      labId: labId.value,
      status: judgeResult?.success ? '已完成' : '进行中',
      score: judgeResult?.success ? 100 : 0,
    })
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '运行提交失败'
    outputLines.value = ['错误: ' + (error instanceof Error ? error.message : '运行提交失败'), editorFinalLine]
  } finally {
    running.value = false
  }
}

async function handleSave() {
  const userId = getCurrentUserId()
  if (!userId || saving.value || !currentTask.value) {
    return
  }

  saving.value = true
  errorMessage.value = ''

  try {
    await saveSubmission({
      userId,
      labId: labId.value,
      taskId: currentTask.value.taskId,
      fileName: taskFileName.value,
      codeContent: code.value,
      runResult: 'Fail',
      runTime: 0,
      passCount: 0,
      failCount: 0,
      totalCount: 0,
      resultDetail: '已保存到提交记录',
    })

    outputLines.value = ['代码已保存到后端提交记录。', editorFinalLine]
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '保存失败'
  } finally {
    saving.value = false
  }
}

async function handleViewAnswer() {
  if (!currentTask.value) {
    return
  }

  try {
    const answer = await getTaskAnswer(currentTask.value.taskId)
    outputLines.value = [answer || '暂无答案内容', editorFinalLine]
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '查看答案失败'
  }
}

function copyResult() {
  if (copiedTimer) {
    clearTimeout(copiedTimer)
  }
  
  const resultText = outputLines.value.join('\n')
  
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(resultText).then(() => {
      copied.value = true
      copiedTimer = setTimeout(() => {
        copied.value = false
      }, 2000)
    }).catch(() => {
      copied.value = false
      errorMessage.value = '复制到剪贴板失败，请手动选择内容复制'
      setTimeout(() => {
        errorMessage.value = ''
      }, 3000)
    })
  } else {
    const textArea = document.createElement('textarea')
    textArea.value = resultText
    textArea.style.position = 'fixed'
    textArea.style.left = '-9999px'
    document.body.appendChild(textArea)
    textArea.select()
    try {
      document.execCommand('copy')
      copied.value = true
      copiedTimer = setTimeout(() => {
        copied.value = false
      }, 2000)
    } catch (err) {
      copied.value = false
      errorMessage.value = '复制到剪贴板失败，请手动选择内容复制'
      setTimeout(() => {
        errorMessage.value = ''
      }, 3000)
    } finally {
      document.body.removeChild(textArea)
    }
  }
}

function handleDocs() {
  router.push(`/labs/${labKey.value}`)
}

onMounted(() => {
  mounted = true
  void loadTaskContext(routeTaskId.value)
})

watch(labKey, () => {
  if (!mounted) return
  void loadTaskContext(routeTaskId.value)
})

watch(routeTaskId, (nextTaskId) => {
  if (!mounted || !nextTaskId) return
  void loadTaskContext(nextTaskId)
})

onBeforeUnmount(() => {
  editorView.value?.destroy()
})
</script>

<template>
  <div class="min-h-screen bg-[#FEF8E7] flex flex-col text-slate-900">
    <AppHeader :title="editorTitle" :nav-items="guideHeaderNavItems" />

    <main class="flex-1 p-4 flex flex-col">
      <div v-if="errorMessage" class="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
        {{ errorMessage }}
      </div>

      <div v-if="loading" class="mb-4 rounded-xl border border-blue-200 bg-blue-50 px-4 py-3 text-sm text-blue-700">
        正在加载任务内容...
      </div>

      <div class="flex-1 border-4 border-blue-300 rounded-xl flex flex-col overflow-hidden shadow-2xl relative bg-[#FEF8E7]">
        <div class="flex-1 flex flex-col lg:flex-row">
          <section class="lg:w-[60%] flex flex-col border-b-4 lg:border-b-0 lg:border-r-4 border-blue-300">
            <div class="p-3 border-b-4 border-blue-300 flex items-center justify-between bg-blue-50/50">
              <div class="flex items-center gap-2">
                <Icon class="text-blue-500 text-xl" icon="ph:code-bold" />
                <span class="font-bold text-blue-600">代码编辑区</span>
              </div>
              <div class="flex items-center gap-2 text-right">
                <div class="rounded-lg bg-white px-3 py-1.5 border border-blue-200 shadow-sm">
                  <div class="text-xs font-semibold text-blue-500 uppercase tracking-wide">当前任务</div>
                  <div class="text-sm font-bold text-slate-800">{{ taskDisplayTitle }}</div>
                </div>
                <div class="flex items-center gap-2">
                  <button class="w-9 h-9 rounded-full bg-white border border-blue-200 text-blue-600 hover:bg-blue-50" type="button" @click="previousTask">
                    <Icon icon="ph:caret-left-bold" />
                  </button>
                  <button class="w-9 h-9 rounded-full bg-white border border-blue-200 text-blue-600 hover:bg-blue-50" type="button" @click="nextTask">
                    <Icon icon="ph:caret-right-bold" />
                  </button>
                </div>
              </div>
            </div>
            <div class="flex-1 p-6 text-sm overflow-auto">
              <div ref="editorRoot" class="h-full min-h-[420px]"></div>
            </div>
          </section>

          <section class="lg:w-[40%] flex flex-col relative">
            <div class="p-3 border-b-4 border-blue-300 flex items-center justify-between bg-blue-50/50">
              <div class="flex items-center gap-2">
                <Icon class="text-blue-500 text-xl" icon="ph:terminal-window-bold" />
                <span class="font-bold text-blue-600">运行结果</span>
              </div>
              <button
                class="px-3 py-1.5 bg-white border border-blue-200 rounded-md text-blue-600 text-xs font-medium hover:bg-blue-50 transition-colors shadow-sm flex items-center gap-1 disabled:opacity-60 disabled:cursor-not-allowed"
                type="button"
                :disabled="outputLines.length === 0"
                @click="copyResult"
              >
                <Icon :icon="copied ? 'ph:check-bold' : 'ph:copy-bold'" />
                {{ copied ? '已复制' : '复制结果' }}
              </button>
            </div>
            <div class="flex-1 p-6 text-sm bg-slate-900/5 m-4 rounded-lg border border-blue-200/50">
              <p v-for="(line, index) in outputLines" :key="`${line}-${index}`" class="text-slate-800 mb-1 whitespace-pre-wrap">
                {{ line }}
              </p>
            </div>
            <button
              class="absolute bottom-6 right-6 px-4 py-2 bg-white border-2 border-blue-300 rounded-md text-blue-600 text-sm font-medium hover:bg-blue-50 transition-colors shadow-sm flex items-center gap-1"
              type="button"
              @click="handleViewAnswer"
            >
              <Icon icon="ph:lightbulb-bold" />
              查看答案
            </button>
          </section>
        </div>

        <div class="h-20 border-t-4 border-blue-300 bg-blue-50/30 flex items-center justify-center gap-6 px-8 flex-wrap">
          <button
            class="px-8 py-3 bg-white border-2 border-blue-400 rounded-xl text-blue-600 font-bold hover:bg-blue-100 transition-all flex items-center gap-2 shadow-sm"
            type="button"
            @click="handleDocs"
          >
            <Icon icon="ph:file-text-bold" />
            查看文档
          </button>
          <button
            class="px-8 py-3 bg-white border-2 border-blue-400 rounded-xl text-blue-600 font-bold hover:bg-blue-100 transition-all flex items-center gap-2 shadow-sm disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="saving"
            type="button"
            @click="handleSave"
          >
            <Icon icon="ph:floppy-disk-bold" />
            {{ saving ? '保存中...' : '保存' }}
          </button>
          <button
            class="px-8 py-3 bg-blue-500 border-2 border-blue-600 rounded-xl text-white font-bold hover:bg-blue-600 transition-all flex items-center gap-2 shadow-md transform active:scale-95 disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="running"
            type="button"
            @click="handleRun"
          >
            <Icon icon="ph:play-fill" />
            {{ running ? '运行中...' : '运行' }}
          </button>
        </div>
      </div>
    </main>
  </div>
</template>
