
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
};
