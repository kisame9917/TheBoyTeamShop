import http from './http'
import { normalizeUploadResponse } from '../utils/media'

async function postMultipart(url, file) {
    const formData = new FormData()
    formData.append('file', file)
    const response = await http.post(url, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    })
    return normalizeUploadResponse(response)
}

async function tryUpload(primaryUrl, fallbackUrl, file) {
    try {
        return await postMultipart(primaryUrl, file)
    } catch (err) {
        const status = err?.response?.status
        if (fallbackUrl && (status === 404 || status === 405)) {
            return postMultipart(fallbackUrl, file)
        }
        throw err
    }
}

export function uploadVariantPrimaryImage(file) {
    return tryUpload('/api/upload/san-pham-chi-tiet-primary', '/api/upload', file)
}

export function uploadStaffAvatar(file) {
    return tryUpload('/api/upload/nhan-vien-avatar', null, file)
}

export function uploadCustomerAvatar(file) {
    return tryUpload('/api/upload/khach-hang-avatar', '/api/khach-hang/upload-avatar', file)
}
