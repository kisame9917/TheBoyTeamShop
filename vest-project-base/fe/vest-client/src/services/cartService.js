import { resolveMediaUrl } from '../utils/media';

const CART_KEY = 'cart_items';
const LEGACY_CART_KEY = 'VEST_CLIENT_CART';
const CART_EVENT = 'cart-changed';
const CART_ADDED_EVENT = 'cart-added';

function safeParse(raw) {
  try {
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

function readRawCart() {
  const current = safeParse(localStorage.getItem(CART_KEY));
  if (Array.isArray(current) && current.length) return current;

  const legacy = safeParse(localStorage.getItem(LEGACY_CART_KEY));
  if (Array.isArray(legacy) && legacy.length) {
    const migrated = legacy.map(normalizeItem).filter(Boolean);
    writeRawCart(migrated);
    try {
      localStorage.removeItem(LEGACY_CART_KEY);
    } catch {}
    return migrated;
  }

  return Array.isArray(current) ? current : [];
}

function writeRawCart(items) {
  const normalized = (Array.isArray(items) ? items : [])
      .map(normalizeItem)
      .filter(Boolean);

  localStorage.setItem(CART_KEY, JSON.stringify(normalized));
  localStorage.setItem(LEGACY_CART_KEY, JSON.stringify(normalized));
  window.dispatchEvent(new Event(CART_EVENT));
}

function makeKey(item = {}) {
  if (item.key) return String(item.key);
  if (item.idSanPhamChiTiet != null) return `spct__${item.idSanPhamChiTiet}`;

  const id = item.productId ?? item.id ?? 'unknown';
  const color = item.color ?? '';
  const size = item.size ?? '';
  return `${id}__${color}__${size}`;
}

function normalizeItem(item) {
  if (!item || typeof item !== 'object') return null;

  const normalized = {
    key: makeKey(item),
    idSanPhamChiTiet:
        item.idSanPhamChiTiet != null
            ? item.idSanPhamChiTiet
            : item.productDetailId != null
                ? item.productDetailId
                : item.id != null
                    ? item.id
                    : null,
    productId:
        item.productId != null
            ? item.productId
            : item.id != null && item.idSanPhamChiTiet == null
                ? item.id
                : null,
    id: item.id ?? item.productId ?? item.idSanPhamChiTiet ?? null,
    name: item.name || item.tenSanPham || 'Sản phẩm',
    image: resolveMediaUrl(
        item.image || item.imageUrl || item.primaryImageUrl || item.hinhAnh || item.anh || '',
    ),
    color: item.color || item.mauSac || '',
    size: item.size || item.kichCo || '',
    price: Number(item.price ?? item.giaBan ?? 0),
    qty: Math.max(1, Number(item.qty ?? item.soLuong ?? 1) || 1),
    stock: Math.max(0, Number(item.stock ?? item.soLuongTon ?? 0) || 0),
    code: item.code || item.ma || '',
  };

  if (normalized.idSanPhamChiTiet == null && normalized.productId == null && !normalized.key) {
    return null;
  }

  return normalized;
}

function readCart() {
  return readRawCart().map(normalizeItem).filter(Boolean);
}

function getCartItems() {
  return readCart();
}

function findIndex(items, keyOrId) {
  return items.findIndex(
      (item) => item.key === keyOrId || item.idSanPhamChiTiet === keyOrId,
  );
}

function addToCart(product, qty = 1) {
  const nextItem = normalizeItem({ ...product, qty });
  if (!nextItem) {
    throw new Error('Dữ liệu sản phẩm thêm vào giỏ hàng không hợp lệ');
  }

  const items = readCart();
  const index = items.findIndex(
      (item) =>
          (nextItem.idSanPhamChiTiet != null && item.idSanPhamChiTiet === nextItem.idSanPhamChiTiet) ||
          item.key === nextItem.key,
  );

  if (index >= 0) {
    const current = items[index];
    let mergedQty = Number(current.qty || 0) + Number(nextItem.qty || 1);
    const stock = Number(nextItem.stock || current.stock || 0);
    if (stock > 0) mergedQty = Math.min(mergedQty, stock);

    items[index] = normalizeItem({
      ...current,
      ...nextItem,
      qty: mergedQty,
    });
  } else {
    items.unshift(nextItem);
  }

  writeRawCart(items);

  const updatedItems = readCart();
  const addedItem = updatedItems.find(
      (item) =>
          item.key === nextItem.key ||
          (nextItem.idSanPhamChiTiet != null && item.idSanPhamChiTiet === nextItem.idSanPhamChiTiet),
  ) || nextItem;

  window.dispatchEvent(
      new CustomEvent(CART_ADDED_EVENT, {
        detail: {
          item: addedItem,
          qtyAdded: Math.max(1, Number(qty || 1) || 1),
        },
      }),
  );

  return updatedItems;
}

function removeCartItem(keyOrId) {
  const items = readCart().filter(
      (item) => item.key !== keyOrId && item.idSanPhamChiTiet !== keyOrId,
  );
  writeRawCart(items);
  return items;
}

function removeItem(keyOrId) {
  return removeCartItem(keyOrId);
}

function updateCartItemQty(keyOrId, qty) {
  const items = readCart();
  const index = findIndex(items, keyOrId);
  if (index < 0) return items;

  let nextQty = Math.max(1, Number(qty || 1));
  const stock = Number(items[index].stock || 0);
  if (stock > 0) nextQty = Math.min(nextQty, stock);

  items[index] = normalizeItem({
    ...items[index],
    qty: nextQty,
  });

  writeRawCart(items);
  return readCart();
}

function updateQty(keyOrId, qty) {
  return updateCartItemQty(keyOrId, qty);
}

function clearCart() {
  writeRawCart([]);
}

export { CART_EVENT, CART_ADDED_EVENT };

export default {
  getCartItems,
  addToCart,
  removeCartItem,
  removeItem,
  updateCartItemQty,
  updateQty,
  clearCart,
};
