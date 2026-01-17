import http from "./http";

export const hoaDonApi = {
  // OrdersList dùng: hoaDonApi.search(params)
  // params: { page, size, keyword?, trangThaiDon? }
  search: (params) => http.get("/api/hoa-don", { params }),

  // OrdersList dùng: hoaDonApi.detail(id)
  detail: (id) => http.get(`/api/hoa-don/${id}`),

  // QR scan (nếu bạn dùng)
  byMa: (ma) => http.get(`/api/hoa-don/by-ma/${encodeURIComponent(ma)}`),

  // (nếu bạn dùng nơi khác)
  changeStatus: (id, body) => http.patch(`/api/hoa-don/${id}/trang-thai`, body),
  returnOrder: (id, body) => http.patch(`/api/hoa-don/${id}/hoan-hang`, body),
};
