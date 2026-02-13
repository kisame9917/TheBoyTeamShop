import http from "@/services/http";

function unwrap(res) {
  const body = res?.data ?? res;

  if (
      body &&
      typeof body === "object" &&
      Object.prototype.hasOwnProperty.call(body, "success") &&
      Object.prototype.hasOwnProperty.call(body, "data")
  ) {
    if (body.success === false) {
      const err = new Error(body.message || "Request failed");
      err.payload = body; // ✅ để FE hiển thị hoặc debug
      throw err;
    }
    return body.data;
  }

  return body;
}

export default {
  async kiemTraCa() {
    const res = await http.get("/api/giao-ca/check-in");
    return unwrap(res);
  },

  async moCa(payload) {
    const res = await http.post("/api/giao-ca/mo", payload);
    return unwrap(res);
  },

  async dongCa(payload) {
    const res = await http.post("/api/giao-ca/dong", payload);
    return unwrap(res);
  },

  async hienTai() {
    const res = await http.get("/api/giao-ca/hien-tai");
    return unwrap(res);
  },

  async adminList(params = {}) {
    const res = await http.get("/api/giao-ca/admin/phien-ca", { params });
    return unwrap(res);
  },
};
