<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { basicSetup, EditorView } from 'codemirror'
import { python } from '@codemirror/lang-python'
import AppHeader from '../components/layout/AppHeader.vue'
import { editorFinalLine, guideHeaderNavItems, labGuides } from '../data/labs'
import { getLabDetail } from '../api/labs'
import { getCurrentUserId } from '../config/session'
import { getSubmissions, getTask, getTaskAnswer, getTaskSource, runSubmission, saveSubmission, getAnswerFile } from '../api/submissions'
import { saveProgress } from '../api/progress'

const route = useRoute()
const router = useRouter()
const editorRoot = ref(null)
const editorView = ref(null)
const code = ref('')
const compileLog = ref([])
const runResult = ref([])
const showCompileLog = ref(true)
const loading = ref(false)
const saving = ref(false)
const running = ref(false)
const errorMessage = ref('')
const currentTask = ref(null)
const taskList = ref([])
const currentSubmission = ref(null)
const copied = ref(false)
const ignoreRouteChanges = ref(false)
const lockLogUpdate = ref(false)
const compileLogFinalized = ref(false)
const runResultFinalized = ref(false)
const answerEnabled = ref(false)
const answerCode = ref('')
const showAnswer = ref(false)
let mounted = false
let copiedTimer = null

function setCompileLog(value) {
  if (compileLogFinalized.value) {
    console.warn('compileLog has been finalized, cannot modify')
    return
  }
  compileLog.value = value
}

function appendCompileLog(line) {
  if (compileLogFinalized.value) {
    console.warn('compileLog has been finalized, cannot append')
    return
  }
  compileLog.value.push(line)
}

function setRunResult(value) {
  if (runResultFinalized.value) {
    console.warn('runResult has been finalized, cannot modify')
    return
  }
  runResult.value = value
}

function appendRunResult(line) {
  if (runResultFinalized.value) {
    console.warn('runResult has been finalized, cannot append')
    return
  }
  runResult.value.push(line)
}

function finalizeLogs() {
  compileLogFinalized.value = true
  runResultFinalized.value = true
}

function resetLogFinalized() {
  compileLogFinalized.value = false
  runResultFinalized.value = false
}

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
  if (running.value || lockLogUpdate.value) {
    return
  }
  currentSubmission.value = submission || null
  setRunResult([
    submission?.resultDetail || '最近一次提交已从后端加载。',
    submission?.submitTime ? `提交时间：${formatTime(submission.submitTime)}` : '',
  ].filter(Boolean))
}

function getFallbackTaskId(preferredTaskId = 0) {
  const availableTaskIds = taskList.value.map((item) => item.taskId)
  if (availableTaskIds.includes(preferredTaskId)) {
    return preferredTaskId
  }

  return availableTaskIds[0] || 0
}

async function loadTaskContext(nextTaskId = null) {
  if (running.value || lockLogUpdate.value) {
    return
  }

  // 如果 judge 结果已定型（finalizeLogs 已调用），阻止 loadTaskContext
  // 覆盖当前显示的评测输出。调用方（previousTask/nextTask）会先 resetLogFinalized
  if (compileLogFinalized.value || runResultFinalized.value) {
    return
  }
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
    } else if (!running.value && !lockLogUpdate.value) {
      setRunResult(['请先编写代码，再点击运行或查看答案。'])
    }
  } catch (error) {
    if (!running.value && !lockLogUpdate.value) {
      errorMessage.value = error instanceof Error ? error.message : '加载任务失败'
      currentTask.value = null
      code.value = ''
      setCompileLog([])
      setRunResult([])
      editorView.value?.destroy()
      editorView.value = null
    }
  } finally {
    loading.value = false
  }
}

function previousTask() {
  if (!taskList.value.length || taskIndex.value < 0) return
  resetLogFinalized()
  const index = taskIndex.value <= 0 ? taskList.value.length - 1 : taskIndex.value - 1
  void loadTaskContext(taskList.value[index].taskId)
}

function nextTask() {
  if (!taskList.value.length || taskIndex.value < 0) return
  resetLogFinalized()
  const index = taskIndex.value >= taskList.value.length - 1 ? 0 : taskIndex.value + 1
  void loadTaskContext(taskList.value[index].taskId)
}

async function handleRun() {
  const userId = getCurrentUserId()
  if (!userId || running.value || !currentTask.value) {
    return
  }

  running.value = true
  ignoreRouteChanges.value = true
  lockLogUpdate.value = true
  errorMessage.value = ''
  
  resetLogFinalized()
  
  const compileLogBuffer = []
  const runResultBuffer = []

  compileLogBuffer.push(`=== 正在提交代码到评测系统 ===`)
  compileLogBuffer.push(`用户ID: ${userId}`)
  compileLogBuffer.push(`实验ID: lab${labId.value}`)
  compileLogBuffer.push(`任务ID: ${currentTask.value.taskId}`)
  compileLogBuffer.push(`文件名: ${taskFileName.value}`)
  compileLogBuffer.push('')
  compileLogBuffer.push(`正在编译代码...`)

  compileLog.value = [...compileLogBuffer]
  runResult.value = []

  try {
    const judgeResult = await runSubmission({
      userId,
      labId: labId.value,
      taskId: currentTask.value.taskId,
      fileName: taskFileName.value,
      codeContent: code.value,
    })

    if (judgeResult?.combinedLog) {
      const logs = judgeResult.combinedLog.split(/\r?\n/)
      let inBuildPhase = true

      for (const line of logs) {
        const trimmedLine = line.trim()
        
        if (!trimmedLine) {
          continue
        }

        if (trimmedLine.startsWith('提交时间') || trimmedLine.startsWith('>>>') || trimmedLine.includes('程序运行结束')) {
          continue
        }

        const isBuildLog =
          // 编译器工具链
          trimmedLine.startsWith('riscv64-unknown-elf-gcc') ||
          trimmedLine.startsWith('riscv64-unknown-elf-ld') ||
          trimmedLine.startsWith('riscv64-unknown-elf-objdump') ||
          trimmedLine.startsWith('riscv64-unknown-elf-objcopy') ||
          trimmedLine.startsWith('riscv64-unknown-elf-ar') ||
          // 构建基础设施命令（容器内 bash 拼接的命令回显）
          trimmedLine.startsWith('mkdir') ||
          trimmedLine.startsWith('rm') ||
          trimmedLine.startsWith('touch') ||
          trimmedLine.startsWith('cp') ||
          trimmedLine.startsWith('printf') ||
          trimmedLine.startsWith('sed') ||
          trimmedLine.startsWith('cd ') ||
          trimmedLine.startsWith('make ') ||
          trimmedLine.startsWith('make[') ||
          trimmedLine.startsWith('make:') ||
          trimmedLine.startsWith('Entering directory') ||
          trimmedLine.startsWith('Leaving directory') ||
          trimmedLine.startsWith('/bin/sh:') ||
          trimmedLine.startsWith('*** ') ||
          trimmedLine.startsWith('if ') ||
          trimmedLine.startsWith('then') ||
          trimmedLine.startsWith('else') ||
          trimmedLine.startsWith('fi') ||
          trimmedLine === ':' ||
          trimmedLine.includes('Build kernel done') ||
          // QEMU 启动
          trimmedLine.startsWith('qemu-system-riscv64') ||
          // RustSBI 固件启动信息
          trimmedLine.includes('[rustsbi]') ||
          trimmedLine.includes('Platform Memory') ||
          trimmedLine.includes('pmp01') ||
          trimmedLine.includes('pmp02') ||
          trimmedLine.includes('pmp03') ||
          trimmedLine.includes('pmp04') ||
          trimmedLine.includes('Hart') ||
          trimmedLine.startsWith('RUSTSBI') ||
          trimmedLine.match(/^====*$/) ||
          trimmedLine.match(/^\s*\.______/) ||
          trimmedLine.match(/^\s*\|.*\|$/) ||
          (trimmedLine.includes('Implementation') && trimmedLine.includes('RustSBI')) ||
          (trimmedLine.includes('Platform Name') && trimmedLine.includes('riscv')) ||
          trimmedLine.includes('Platform SMP') ||
          trimmedLine.includes('Boot HART') ||
          trimmedLine.includes('Device Tree') ||
          trimmedLine.includes('Firmware Address') ||
          trimmedLine.includes('Supervisor Address') ||
          trimmedLine.startsWith('#') ||
          trimmedLine.startsWith('  ') ||
          trimmedLine.match(/^\s*\d+\s*$/)
        
        if (inBuildPhase && isBuildLog) {
          compileLogBuffer.push(trimmedLine)
        } else if (inBuildPhase && !isBuildLog) {
          inBuildPhase = false
          runResultBuffer.push(trimmedLine)
        } else {
          runResultBuffer.push(trimmedLine)
        }
      }
    }

    runResultBuffer.push('')
    runResultBuffer.push('=== 运行状态 ===')
    runResultBuffer.push(`【运行状态】: ${judgeResult?.success ? '✓ 通过' : '✗ 失败'}`)
    
    if (judgeResult?.message) {
      runResultBuffer.push(`消息: ${judgeResult.message}`)
    }

    if (judgeResult?.durationMillis != null) {
      runResultBuffer.push(`耗时: ${(judgeResult.durationMillis / 1000).toFixed(2)} 秒`)
    }

    if (judgeResult?.exitCode != null) {
      runResultBuffer.push(`退出码: ${judgeResult.exitCode}`)
    }

    if (judgeResult?.timeout) {
      runResultBuffer.push(`警告: 程序执行超时`)
    }

    runResultBuffer.push('')
    runResultBuffer.push(`提交时间：${new Date().toLocaleString('zh-CN')}`)
    runResultBuffer.push(`>>> 程序运行结束 (耗时: ${(judgeResult?.durationMillis ? judgeResult.durationMillis / 1000 : 0).toFixed(2)}s)`)

    compileLog.value = [...compileLogBuffer]
    runResult.value = [...runResultBuffer]

    finalizeLogs()
    answerEnabled.value = true

    try {
      await saveProgress({
        userId,
        labId: labId.value,
        status: judgeResult?.success ? '已完成' : '进行中',
        score: judgeResult?.success ? 100 : 0,
      })
    } catch (e) {
      console.warn('保存进度失败:', e)
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '运行提交失败'
    runResult.value = ['错误: ' + (error instanceof Error ? error.message : '运行提交失败')]
    finalizeLogs()
  } finally {
    ignoreRouteChanges.value = false
    lockLogUpdate.value = false
    running.value = false
  }
}

async function handleSave() {
  const userId = getCurrentUserId()
  if (!userId || saving.value || !currentTask.value || running.value || lockLogUpdate.value) {
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

    setRunResult(['代码已保存到后端提交记录。'])
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '保存失败'
  } finally {
    saving.value = false
  }
}

async function handleViewAnswer() {
  if (!currentTask.value || running.value || lockLogUpdate.value || !answerEnabled.value) {
    return
  }

  try {
    const answer = await getAnswerFile(labId.value, taskFileName.value)
    answerCode.value = answer || '暂无答案内容'
    showAnswer.value = true
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '查看答案失败'
  }
}

function copyResult() {
  if (copiedTimer) {
    clearTimeout(copiedTimer)
  }
  
  const allContent = [...compileLog.value, '', '=== 运行结果 ===', ...runResult.value].join('\n')
  
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(allContent).then(() => {
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
    textArea.value = allContent
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
  if (!mounted || running.value || ignoreRouteChanges.value || lockLogUpdate.value) return
  void loadTaskContext(routeTaskId.value)
})

watch(routeTaskId, (nextTaskId) => {
  if (!mounted || !nextTaskId || running.value || ignoreRouteChanges.value || lockLogUpdate.value) return
  void loadTaskContext(nextTaskId)
})

onBeforeRouteLeave((to, from, next) => {
  if (running.value) {
    next(false)
  } else {
    next()
  }
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
                :disabled="!compileLog.length && !runResult.length"
                @click="copyResult"
              >
                <Icon :icon="copied ? 'ph:check-bold' : 'ph:copy-bold'" />
                {{ copied ? '已复制' : '复制结果' }}
              </button>
            </div>
            
            <div class="flex-1 p-4 overflow-auto">
              <div class="mb-4">
                <button
                  class="w-full flex items-center justify-between p-3 bg-blue-50 border border-blue-200 rounded-lg hover:bg-blue-100 transition-colors"
                  @click="showCompileLog = !showCompileLog"
                >
                  <div class="flex items-center gap-2">
                    <Icon :icon="showCompileLog ? 'ph:chevron-down-bold' : 'ph:chevron-right-bold'" class="text-blue-500" />
                    <span class="font-semibold text-blue-700">构建日志</span>
                    <span class="text-xs text-blue-500">({{ compileLog.length }} 行)</span>
                  </div>
                </button>
                <div v-show="showCompileLog" class="mt-2 p-4 bg-slate-900/5 rounded-lg border border-blue-200/50 max-h-64 overflow-auto">
                  <p v-for="(line, index) in compileLog" :key="`compile-${line}-${index}`" class="text-slate-800 text-sm mb-1 whitespace-pre-wrap">
                    {{ line }}
                  </p>
                </div>
              </div>
              
              <div>
                <button
                  class="w-full flex items-center justify-between p-3 bg-green-50 border border-green-200 rounded-lg hover:bg-green-100 transition-colors"
                  style="cursor:default"
                >
                  <div class="flex items-center gap-2">
                    <Icon icon="ph:chevron-down-bold" class="text-green-500" />
                    <span class="font-semibold text-green-700">运行结果</span>
                    <span class="text-xs text-green-500">({{ runResult.length }} 行)</span>
                  </div>
                </button>
                <div class="mt-2 p-4 bg-slate-900/5 rounded-lg border border-green-200/50 max-h-80 overflow-auto">
                  <p v-for="(line, index) in runResult" :key="`result-${line}-${index}`" class="text-slate-800 text-sm mb-1 whitespace-pre-wrap">
                    {{ line }}
                  </p>
                  <p v-if="!runResult.length" class="text-slate-500 text-sm italic">运行结果将显示在这里...</p>
                </div>
              </div>

              <div v-if="answerCode" class="mt-4 flex flex-col flex-1">
                <button
                  class="w-full flex items-center justify-between p-3 bg-yellow-50 border border-yellow-200 rounded-lg hover:bg-yellow-100 transition-colors"
                  @click="showAnswer = !showAnswer"
                >
                  <div class="flex items-center gap-2">
                    <Icon :icon="showAnswer ? 'ph:chevron-down-bold' : 'ph:chevron-right-bold'" class="text-yellow-600" />
                    <span class="font-semibold text-yellow-700">参考答案</span>
                    <span class="text-xs text-yellow-500">({{ answerCode.split('\n').length }} 行)</span>
                  </div>
                </button>
                <div v-show="showAnswer" class="mt-2 p-4 bg-slate-900/5 rounded-lg border border-yellow-200/50 flex-1 overflow-auto">
                  <pre class="text-slate-800 text-sm whitespace-pre-wrap font-mono">{{ answerCode }}</pre>
                </div>
              </div>
            </div>

            <button
              class="absolute bottom-6 right-6 px-4 py-2 bg-white border-2 border-blue-300 rounded-md text-blue-600 text-sm font-medium hover:bg-blue-50 transition-colors shadow-sm flex items-center gap-1 disabled:opacity-50 disabled:cursor-not-allowed"
              type="button"
              :disabled="!answerEnabled"
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
