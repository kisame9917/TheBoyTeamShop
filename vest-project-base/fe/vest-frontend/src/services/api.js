import axios from 'axios'

const http = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 15000
})

// ✅ gắn Bearer token cho mọi request
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// unwrap ApiResponse<T> + handle 401/403
http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body === 'object' && 'success' in body) {
      if (body.success) return body.data
      return Promise.reject(new Error(body.message || 'API error'))
    }
    // login thường trả {token, role} => không có success => return thẳng
    return body
  },
  (err) => {
    const status = err?.response?.status
    if (status === 401 || status === 403) {
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('username')
      // tuỳ bạn dùng router hay reload
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default http


export async function adminChangeOrderStatus(orderId, payload) {
  const token =
    localStorage.getItem("ADMIN_ACCESS_TOKEN") ||
    sessionStorage.getItem("ADMIN_ACCESS_TOKEN") ||
    localStorage.getItem("vest_token");

  const res = await fetch(`http://localhost:8080/api/hoa-don/${orderId}/change-status`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(payload),
  });

  const data = await res.json().catch(() => ({}));

  if (!res.ok) {
    throw new Error(data?.message || "Cập nhật trạng thái thất bại");
  }

  return data;
}

export async function adminConfirmRefund(orderId, payload) {
  const token =
    localStorage.getItem("ADMIN_ACCESS_TOKEN") ||
    sessionStorage.getItem("ADMIN_ACCESS_TOKEN") ||
    localStorage.getItem("vest_token");

  const res = await fetch(`http://localhost:8080/api/hoa-don/${orderId}/xac-nhan-hoan-tien`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(payload || {}),
  });

  const data = await res.json().catch(() => ({}));

  if (!res.ok) {
    throw new Error(data?.message || "Xác nhận hoàn tiền thất bại");
  }

  return data;
}