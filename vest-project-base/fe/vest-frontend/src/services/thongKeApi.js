import http from "./http";

function unwrap(res) {
    const body = res?.data;
    // Backend trả ApiResponse { success, data, ... }
    if (body && typeof body === "object" && "success" in body) return body.data;
    return body;
}

export default {
    async getTongQuan() {
        const res = await http.get("/api/thong-ke/tong-quan");
        return unwrap(res) || null;
    },

    // Doanh thu: THANG | QUY | NAM
    async getDoanhThu(params) {
        // params:
        // - THANG: { type:'THANG', month:2, year:2026 }
        // - QUY  : { type:'QUY', quarter:3, year:2025 }
        // - NAM  : { type:'NAM', year:2025 }
        const res = await http.get("/api/thong-ke/doanh-thu", { params });
        return unwrap(res) || [];
    },

    // Đơn hàng theo range ngày (filter from/to)
    async getThongKeDonHangRange(from, to) {
        const res = await http.get("/api/thong-ke/don-hang-range", { params: { from, to } });
        return unwrap(res) || [];
    },

    // Top bán chạy
    async getTopSelling(from, to) {
        const res = await http.get("/api/thong-ke/top-ban-chay", { params: { from, to } });
        return unwrap(res) || [];
    },

    // Bán chậm / tồn kho
    async getSlowMoving(from, to) {
        const res = await http.get("/api/thong-ke/ban-cham", { params: { from, to } });
        return unwrap(res) || [];
    },

    // Khách hàng tiềm năng (VIP)
    async getTopCustomers(from, to) {
        const res = await http.get("/api/thong-ke/khach-hang-vip", { params: { from, to } });
        return unwrap(res) || [];
    },
};