import { createRouter, createWebHistory } from 'vue-router';

// Layout
import ClientLayout from '../layouts/ClientLayout.vue';

// Pages (client)
import HomePage from '../pages/client/HomePage.vue';
import SearchPage from '../pages/client/SearchPage.vue';
import ProductDetail from '../pages/client/ProductDetail.vue';

// Pages (auth)
import Login from '../pages/login/Login.vue';

const routes = [
    {
        path: '/',
        component: ClientLayout,
        children: [
            { path: '', name: 'Home', component: HomePage },

            // chưa có shop riêng -> trỏ tạm sang SearchPage
            { path: 'shop', name: 'Shop', component: SearchPage },

            { path: 'search', name: 'Search', component: SearchPage },
            { path: 'product/:id', name: 'ProductDetail', component: ProductDetail, props: true },
        ],
    },

    // Auth routes (không bọc layout nếu bạn muốn login page "trống" header/footer)
    { path: '/login', name: 'Login', component: Login },

    // fallback
    { path: '/:pathMatch(.*)*', redirect: '/' },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior() {
        return { top: 0 };
    },
});

export default router;