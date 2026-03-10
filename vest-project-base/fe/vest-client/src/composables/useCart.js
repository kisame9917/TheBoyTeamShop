import { computed, onMounted, onUnmounted, ref } from "vue";
import cartService, { CART_EVENT } from "../services/cartService";

const cartItems = ref(cartService.getCartItems());

function syncCart() {
  cartItems.value = cartService.getCartItems();
}

if (typeof window !== "undefined") {
  window.addEventListener(CART_EVENT, syncCart);
}

export function useCart() {
  onMounted(() => {
    syncCart();
    if (typeof window !== "undefined") {
      window.addEventListener("storage", syncCart);
    }
  });

  onUnmounted(() => {
    if (typeof window !== "undefined") {
      window.removeEventListener("storage", syncCart);
    }
  });

  function addToCart(product, qty = 1) {
    cartService.addToCart(product, qty);
    syncCart();
  }

  function removeFromCart(keyOrId) {
    cartService.removeCartItem(keyOrId);
    syncCart();
  }

  function removeItem(keyOrId) {
    cartService.removeItem(keyOrId);
    syncCart();
  }

  function updateQty(keyOrId, qty) {
    cartService.updateQty(keyOrId, qty);
    syncCart();
  }

  function clearCart() {
    cartService.clearCart();
    syncCart();
  }

  const items = computed(() => cartItems.value);
  const totalQty = computed(() =>
      cartItems.value.reduce((sum, item) => sum + Number(item.qty || 0), 0),
  );
  const subtotal = computed(() =>
      cartItems.value.reduce(
          (sum, item) => sum + Number(item.price || 0) * Number(item.qty || 0),
          0,
      ),
  );
  const totalAmount = computed(() => subtotal.value);

  return {
    items,
    cartItems,
    totalQty,
    subtotal,
    totalAmount,
    addToCart,
    removeFromCart,
    removeItem,
    updateQty,
    clearCart,
    syncCart,
  };
}
