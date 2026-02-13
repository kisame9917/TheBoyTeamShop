import http from "./http";

export default {
    // === 1. CA MẪU (TEMPLATES) ===
    getTemplates() {
        return http.get("/api/ca-lam-viec/mau");
    },
    createTemplate(data) {
        return http.post("/api/ca-lam-viec/mau", data);
    },
    updateTemplate(id, data) {
        return http.put(`/api/ca-lam-viec/mau/${id}`, data);
    },

    // === 2. LỊCH LÀM VIỆC (SCHEDULE) ===
    getSchedules(from, to) {
        return http.get("/api/ca-lam-viec/lich", { params: { from, to } });
    },
    assignSchedule(data) {
        // data gồm: idCaLamViec, idNhanVien, ngayLamViec, ghiChu
        return http.post("/api/ca-lam-viec/lich", data);
    },
    deleteSchedule(id) {
        return http.delete(`/api/ca-lam-viec/lich/${id}`);
    },
    // Lịch cá nhân (đúng theo CaLamViecController)
    getMySchedule(idNhanVien, from, to) {
        return http.get("/api/ca-lam-viec/lich-ca-nhan", {
            params: { idNhanVien, from, to }
        });
    },
    updateSchedule(id, data) {
        return http.put(`/api/ca-lam-viec/lich/${id}`, data);
    },
};