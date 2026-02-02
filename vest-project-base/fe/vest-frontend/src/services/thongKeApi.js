import http from "./http";

export default {
    // 1. So sánh doanh thu Quý
    compareQuarter() {
        return http.get("/api/thong-ke/so-sanh-quy");
    },

    // 2. Top sản phẩm bán chạy
    getTopSelling(from, to) {
        return http.get("/api/thong-ke/top-ban-chay", { params: { from, to } });
    },

    // 3. Sản phẩm bán chậm (tồn kho)
    getSlowMoving(from, to) {
        return http.get("/api/thong-ke/ban-cham", { params: { from, to } });
    },

    // 4. Khách hàng VIP
    getTopCustomers(from, to) {
        return http.get("/api/thong-ke/khach-hang-vip", { params: { from, to } });
    },
};