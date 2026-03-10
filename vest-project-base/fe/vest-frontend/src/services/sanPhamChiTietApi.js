import http from './http'
import { uploadVariantPrimaryImage } from './mediaApi'

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

export const decreaseStock = (id, qty = 1) => {
    return http.patch(`/api/san-pham-chi-tiet/${id}/decrease-stock`, null, { params: { qty } })
}

export const increaseStock = (id, qty = 1) => {
    return http.patch(`/api/san-pham-chi-tiet/${id}/increase-stock`, null, { params: { qty } });
};


export const uploadImage = (file) => uploadVariantPrimaryImage(file)
