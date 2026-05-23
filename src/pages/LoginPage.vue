<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '../components/layout/AppHeader.vue'
import AppFooter from '../components/layout/AppFooter.vue'
import { login } from '../api/auth'
import { setCurrentUser } from '../config/session'
import { dashboardHeaderNavItems } from '../data/labs'

const router = useRouter()
const form = reactive({ username: '', password: '' })
const loading = ref(false)
const errorMessage = ref('')

async function handleSubmit() {
  loading.value = true
  errorMessage.value = ''
  try {
    const user = await login(form)
    setCurrentUser(user)
    router.push('/')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-slate-50 text-slate-900 flex flex-col">
    <AppHeader title="操作系统实验教学平台" :nav-items="dashboardHeaderNavItems" />

    <main class="flex-1 flex items-center justify-center px-4 py-12">
      <section class="w-full max-w-md rounded-2xl bg-white border border-slate-200 shadow-xl p-8">
        <h2 class="text-2xl font-bold text-slate-800 mb-2">登录</h2>
        <p class="text-sm text-slate-500 mb-6">请先登录后进入实验平台。</p>

        <div v-if="errorMessage" class="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {{ errorMessage }}
        </div>

        <form class="space-y-4" @submit.prevent="handleSubmit">
          <label class="block">
            <span class="mb-1 block text-sm font-medium text-slate-600">用户名</span>
            <input v-model="form.username" class="w-full rounded-lg border border-slate-300 px-4 py-3 outline-none focus:border-blue-500" type="text" />
          </label>
          <label class="block">
            <span class="mb-1 block text-sm font-medium text-slate-600">密码</span>
            <input v-model="form.password" class="w-full rounded-lg border border-slate-300 px-4 py-3 outline-none focus:border-blue-500" type="password" />
          </label>

          <button class="w-full rounded-lg bg-blue-600 px-4 py-3 font-semibold text-white hover:bg-blue-700 disabled:opacity-60" :disabled="loading" type="submit">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>

        <button class="mt-4 w-full rounded-lg border border-blue-200 px-4 py-3 font-semibold text-blue-600 hover:bg-blue-50" type="button" @click="router.push('/register')">
          去注册
        </button>
      </section>
    </main>

    <AppFooter />
  </div>
</template>
