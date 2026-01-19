import { createApp } from "vue";
import { createPinia } from "pinia";

import App from "./App.vue";
import router from "./router";

// CSS (giữ cả 2)
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";
import "./assets/base.css"; // hoặc "./assets/base.css" tuỳ bạn đang dùng đường dẫn nào
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import "flatpickr/dist/flatpickr.css";

createApp(App).use(createPinia()).use(router).mount("#app");
