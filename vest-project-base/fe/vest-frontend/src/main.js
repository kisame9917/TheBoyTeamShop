import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import './assets/base.css'

import { useAuthStore } from './stores/auth'

const app = createApp(App)

// 1) tạo pinia instance
const pinia = createPinia()

// 2) gắn pinia + router vào app (pinia trước router)
app.use(pinia)
app.use(router)

// 3) bây giờ mới được dùng store (truyền pinia cho chắc chắn)
const auth = useAuthStore(pinia)
auth.hydrate()

// 4) mount
app.mount('#app')
