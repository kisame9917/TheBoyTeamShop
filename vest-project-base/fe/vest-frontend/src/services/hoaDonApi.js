import http from "./http";

const BASE = "/api/hoa-don";

export default {
  // params: page,size,keyword,trangThaiDon,phanLoai,loaiDon,from,to,minTotal,maxTotal,hasVoucher,idNhanVien,active,sortBy,sortDir
  search(params) {
    return http.get(BASE, { params });
  },

  detail(id) {
    return http.get(`${BASE}/${id}`);
  },

  byMa(maHoaDon) {
    return http.get(`${BASE}/by-ma/${encodeURIComponent(maHoaDon)}`);
  },

  changeStatus(id, body) {
    return http.patch(`${BASE}/${id}/trang-thai`, body);
  },

  // ✅ chỉ để 1 cái returnOrder
  returnOrder(id, body) {
    return http.patch(`${BASE}/${id}/hoan-hang`, body);
  },

  lichSu(id) {
    return http.get(`${BASE}/${id}/lich-su`);
  },

  thanhToan(id) {
    return http.get(`${BASE}/${id}/thanh-toan`);
  },

  giaoDich(id) {
    return http.get(`${BASE}/${id}/giao-dich`);
  },

  // =========================
  // ✅ POS DRAFT FLOW
  // =========================

  // tạo hóa đơn nháp (trạng thái 0)
  createDraft(payload) {
    // payload: { maHoaDon }
    return http.post(`${BASE}/taohoadon`, payload);
  },

  // checkout hóa đơn nháp (update trạng thái 0 -> hoàn thành)
  checkoutDraft(id, payload) {
    // payload chính là BanHangRequest (items, paid, idPhieuGiamGia,...)
    return http.post(`${BASE}/draft/${id}/checkout`, payload);
  },

  // hủy hóa đơn nháp + trả kho
  cancelDraft(id, payload) {
    // payload: { reason, items:[{idSanPhamChiTiet, soLuong}] }
    return http.post(`${BASE}/draft/${id}/cancel`, payload);
  },

  // (tuỳ chọn) nếu bạn vẫn muốn giữ createPos
  // createPos(payload) {
  //   return http.post(`${BASE}/pos`, payload);
  // },
};