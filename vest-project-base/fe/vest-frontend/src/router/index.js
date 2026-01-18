import { createRouter, createWebHistory } from 'vue-router'

import DefaultLayout from '../layouts/DefaultLayout.vue'
import BlankLayout from '../layouts/BlankLayout.vue'

import Dashboard from '../pages/Dashboard.vue'
import Login from '../pages/auth/Login.vue'

import ProductsList from '../pages/products/ProductsList.vue'
import ProductDetail from '../pages/products/ProductDetail.vue'
import OrdersList from '../pages/orders/OrdersList.vue'
import CustomersList from '../pages/customers/CustomersList.vue'
import StaffList from '../pages/staff/StaffList.vue'
import VouchersList from '../pages/vouchers/VouchersList.vue'
import VoucherCreate from '../pages/vouchers/VoucherCreate.vue'
import VoucherDetail from '../pages/vouchers/VoucherDetail.vue'
import VoucherUpdate from '../pages/vouchers/VoucherUpdate.vue'
import PaymentsList from '../pages/payments/PaymentsList.vue'
import NotFound from '../pages/notfound/NotFound.vue'

import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/login',
    component: BlankLayout,
    children: [
      { path: '', name: 'login', component: Login }
    ]
  },
  {
    path: '/',
    component: DefaultLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'dashboard', component: Dashboard },

      { path: 'products', name: 'products', component: ProductsList },
      { path: 'products/:id', name: 'product-detail', component: ProductDetail, props: true },

      { path: 'orders', name: 'orders', component: OrdersList },
      { path: 'customers', name: 'customers', component: CustomersList },
      { path: 'staff', name: 'staff', component: StaffList },

    
      { path: 'vouchers', name: 'vouchers', component: VouchersList },

   
      { path: 'vouchers/create', name: 'voucher-create', component: VoucherCreate },

      { path: 'vouchers/:id', name: 'voucher-detail', component: VoucherDetail, props: true },

    { path: 'vouchers/update/:id', name: 'voucher-update', component: VoucherUpdate, props: true },


     

      { path: 'payments', name: 'payments', component: PaymentsList }
    ]
  },
  { path: '/:pathMatch(.*)*', name: 'notfound', component: NotFound }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login' }
  }

  if (to.name === 'login' && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }
})

export default router
