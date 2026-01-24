import http from './http'

export const getByProductId = (productId) => {
    return http.get(`/api/san-pham-chi-tiet/by-product/${productId}`)
}

export const getAllDetails = (page = 0, size = 10) => {
    return http.get('/api/san-pham-chi-tiet', { params: { page, size } })
}

export const createDetail = (payload) => {
    return http.post('/api/san-pham-chi-tiet', payload)
}

export const updateDetail = (id, payload) => {
    return http.put(`/api/san-pham-chi-tiet/${id}`, payload)
}

export const deleteDetail = (id) => {
    return http.delete(`/api/san-pham-chi-tiet/${id}`)
}


export const uploadImage = (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return http.post('/api/upload', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
}
