const CART_KEY = "cart_items";

function readCart() {
  try {
    const raw = localStorage.getItem(CART_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch (error) {
    console.error("readCart error:", error);
    return [];
  }
}

function writeCart(items) {
  localStorage.setItem(CART_KEY, JSON.stringify(items));
}

function getCartItems() {
  return readCart();
}

function clearCart() {
  writeCart([]);
}

function removeCartItem(idSanPhamChiTiet) {
  const items = readCart().filter(
    (item) => item.idSanPhamChiTiet !== idSanPhamChiTiet
  );
  writeCart(items);
  return items;
}

function updateCartItemQty(idSanPhamChiTiet, qty) {
  const nextQty = Math.max(1, Number(qty || 1));

  const items = readCart().map((item) => {
    if (item.idSanPhamChiTiet === idSanPhamChiTiet) {
      return {
        ...item,
        qty: nextQty,
      };
    }
    return item;
  });

  writeCart(items);
  return items;
}

function addToCart(product, qty = 1) {
  if (!product?.idSanPhamChiTiet) {
    throw new Error("Sản phẩm chưa có idSanPhamChiTiet");
  }

  const items = readCart();
  const nextQty = Math.max(1, Number(qty || 1));

  const index = items.findIndex(
    (item) => item.idSanPhamChiTiet === product.idSanPhamChiTiet
  );

  if (index >= 0) {
    items[index] = {
      ...items[index],
      qty: Number(items[index].qty || 0) + nextQty,
      price: Number(product.price ?? items[index].price ?? 0),
      image: product.image ?? items[index].image ?? "",
      color: product.color ?? items[index].color ?? "",
      size: product.size ?? items[index].size ?? "",
      stock: Number(product.stock ?? items[index].stock ?? 0),
      code: product.code ?? items[index].code ?? "",
      name: product.name ?? items[index].name ?? "",
      productId: product.productId ?? items[index].productId ?? null,
    };
  } else {
    items.push({
      idSanPhamChiTiet: product.idSanPhamChiTiet,
      productId: product.productId ?? null,
      name: product.name ?? "",
      image: product.image ?? "",
      color: product.color ?? "",
      size: product.size ?? "",
      price: Number(product.price ?? 0),
      qty: nextQty,
      stock: Number(product.stock ?? 0),
      code: product.code ?? "",
    });
  }

  writeCart(items);
  return items;
}

export default {
  getCartItems,
  addToCart,
  updateCartItemQty,
  removeCartItem,
  clearCart,
};