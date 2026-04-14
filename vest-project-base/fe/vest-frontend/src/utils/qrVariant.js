import QRCode from "qrcode";

export function buildVariantQrText(variant) {
  return String(
    variant?.maSanPhamChiTiet ||
      variant?.code ||
      variant?.ma_sp_chi_tiet ||
      ""
  ).trim();
}

export async function makeVariantQrDataUrl(variant, opts = {}) {
  const text = buildVariantQrText(variant);
  if (!text) throw new Error("Biến thể chưa có mã");

  return QRCode.toDataURL(text, {
    width: opts.width || 260,
    margin: opts.margin ?? 1,
  });
}

export async function downloadVariantQrPng(variant, opts = {}) {
  const text = buildVariantQrText(variant);
  if (!text) throw new Error("Biến thể chưa có mã");

  const url = await QRCode.toDataURL(text, {
    width: opts.width || 512,
    margin: opts.margin ?? 1,
  });

  const a = document.createElement("a");
  a.href = url;
  a.download = `${text}.png`;
  document.body.appendChild(a);
  a.click();
  a.remove();
}