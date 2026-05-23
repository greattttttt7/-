import { createApp } from 'vue'
import { Icon } from '@iconify/vue'
import router from './router'
import './style.css'
import App from './App.vue'

createApp(App).component('Icon', Icon).use(router).mount('#app')
