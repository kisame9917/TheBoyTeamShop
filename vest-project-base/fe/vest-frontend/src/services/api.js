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
