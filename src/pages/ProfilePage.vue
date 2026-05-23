<script setup>
import { computed } from 'vue'
import AppHeader from '../components/layout/AppHeader.vue'
import AppFooter from '../components/layout/AppFooter.vue'
import { getCurrentUser } from '../config/session'
import { dashboardHeaderNavItems } from '../data/labs'

const currentUser = computed(() => getCurrentUser())
</script>

<template>
  <div class="min-h-screen bg-slate-50 text-slate-900 flex flex-col">
    <AppHeader title="操作系统实验教学平台" :nav-items="dashboardHeaderNavItems" />

    <main class="flex-1 flex items-center justify-center px-4 py-12">
      <section class="w-full max-w-md rounded-2xl bg-white border border-slate-200 shadow-xl p-8 text-center">
        <h2 class="text-2xl font-bold text-slate-800 mb-2">个人中心</h2>
        <p class="text-sm text-slate-500 mb-6">查看当前登录用户信息。</p>

        <div v-if="currentUser" class="space-y-3 text-left">
          <div class="rounded-xl border border-slate-200 bg-slate-50 px-4 py-3">
            <div class="text-xs text-slate-500">姓名</div>
            <div class="font-semibold text-slate-800">{{ currentUser.name || '-' }}</div>
          </div>
          <div class="rounded-xl border border-slate-200 bg-slate-50 px-4 py-3">
            <div class="text-xs text-slate-500">用户名</div>
            <div class="font-semibold text-slate-800">{{ currentUser.username || '-' }}</div>
          </div>
          <div class="rounded-xl border border-slate-200 bg-slate-50 px-4 py-3">
            <div class="text-xs text-slate-500">用户ID</div>
            <div class="font-semibold text-slate-800">{{ currentUser.userId }}</div>
          </div>
        </div>
      </section>
    </main>

    <AppFooter />
  </div>
</template>
