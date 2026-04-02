export const ORDER_STATUS = {
  CHO_XAC_NHAN: 0,
  DANG_XU_LY: 1,
  DANG_GIAO: 2,
  DA_GIAO: 3,
  HOAN_THANH: 4,
  DA_HUY: 5,
  YEU_CAU_HOAN: 6,
  DA_HOAN_TIEN: 7,
  DA_XAC_NHAN: 8,
  YEU_CAU_HUY: 9,
};

function normalizePaymentMethod(method) {
  return String(method || "").trim().toUpperCase();
}

export function isChoXacNhan(order) {
  return Number(order?.trangThaiDon) === ORDER_STATUS.CHO_XAC_NHAN;
}

export function isCod(order) {
  return normalizePaymentMethod(order?.paymentMethod) === "COD";
}

export function isBankTransfer(order) {
  const method = normalizePaymentMethod(order?.paymentMethod);
  return method === "QR" || method === "BANK" || method === "CK";
}

export function canCancelOrder(order) {
  if (!order) return false;
  return Number(order?.trangThaiDon) === ORDER_STATUS.CHO_XAC_NHAN;
}

export function canEditShipping(order) {
  if (!order) return false;
  if (!isChoXacNhan(order)) return false;

  // FE trước mắt: COD và QR/CK đều cho mở form shipping
  return isCod(order) || isBankTransfer(order);
}

export function canEditItems(order) {
  if (!order) return false;
  if (!isChoXacNhan(order)) return false;

  // Chỉ COD mới cho sửa sản phẩm trong đơn
  return isCod(order);
}