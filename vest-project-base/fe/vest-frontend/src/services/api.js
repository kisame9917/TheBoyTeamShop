import axios from 'axios'

const http = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 15000
})

// unwrap ApiResponse<T>
http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body === 'object' && 'success' in body) {
      if (body.success) return body.data
      return Promise.reject(new Error(body.message || 'API error'))
    }
    return body
  },
  (err) => Promise.reject(err)
)

export const sanPhamApi = {
  list: () => http.get('/api/san-pham'),
  get: (id) => http.get(`/api/san-pham/${id}`),
  create: (payload) => http.post('/api/san-pham', payload)
}

export default http
