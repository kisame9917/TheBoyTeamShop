import { computed, ref } from "vue";
import cartService from "../services/cartService";

const cartItems = ref(cartService.getCartItems());

function syncCart() {
  cartItems.value = cartService.getCartItems();
}

export function useCart() {
  function addToCart(product, qty = 1) {
    cartService.addToCart(product, qty);
    syncCart();
  }

  function removeFromCart(idSanPhamChiTiet) {
    cartService.removeCartItem(idSanPhamChiTiet);
    syncCart();
  }

  function updateQty(idSanPhamChiTiet, qty) {
    cartService.updateCartItemQty(idSanPhamChiTiet, qty);
    syncCart();
  }

  function clearCart() {
    cartService.clearCart();
    syncCart();
  }

  const totalQty = computed(() =>
    cartItems.value.reduce((sum, item) => sum + Number(item.qty || 0), 0)
  );

  const totalAmount = computed(() =>
    cartItems.value.reduce(
      (sum, item) =>
        sum + Number(item.price || 0) * Number(item.qty || 0),
      0
    )
  );

  return {
    cartItems,
    totalQty,
    totalAmount,
    addToCart,
    removeFromCart,
    updateQty,
    clearCart,
    syncCart,
  };
}