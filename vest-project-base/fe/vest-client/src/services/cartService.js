import { computed, ref } from "vue";

// UI trước: lưu localStorage (sau này nối API thì thay phần persist)
const STORAGE_KEY = "VEST_CLIENT_CART";

const items = ref(loadFromStorage());

function loadFromStorage() {
    try {
        const raw = localStorage.getItem(STORAGE_KEY);
        const parsed = raw ? JSON.parse(raw) : [];
        return Array.isArray(parsed) ? parsed : [];
    } catch {
        return [];
    }
}

function saveToStorage() {
    try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(items.value));
    } catch {
        // ignore
    }
}

function makeKey(payload) {
    const { id, color = "", size = "" } = payload || {};
    return `${id}__${color}__${size}`;
}

function notify() {
    window.dispatchEvent(new Event("cart-changed"));
}

function addToCart(payload, qty = 1) {
    if (!payload || payload.id == null) return;
    const q = Number(qty) > 0 ? Number(qty) : 1;

    const key = makeKey(payload);
    const idx = items.value.findIndex((x) => x.key === key);

    if (idx >= 0) {
        items.value[idx].qty += q;
    } else {
        items.value.unshift({
            key,
            id: payload.id,
            name: payload.name || "Sản phẩm",
            image: payload.image || "",
            price: Number(payload.price) || 0,
            color: payload.color || "",
            size: payload.size || "",
            qty: q,
        });
    }

    saveToStorage();
    notify();
}

function removeItem(key) {
    items.value = items.value.filter((x) => x.key !== key);
    saveToStorage();
    notify();
}

function updateQty(key, qty) {
    const q = Number(qty);
    const idx = items.value.findIndex((x) => x.key === key);
    if (idx < 0) return;

    // ✅ UI yêu cầu: số lượng tối thiểu là 1 (không tự xóa khi bấm trừ)
    items.value[idx].qty = Number.isFinite(q) && q >= 1 ? q : 1;
    saveToStorage();
    notify();
}

function clearCart() {
    items.value = [];
    saveToStorage();
    notify();
}

const totalQty = computed(() =>
    items.value.reduce((sum, x) => sum + (Number(x.qty) || 0), 0)
);

const subtotal = computed(() =>
    items.value.reduce(
        (sum, x) => sum + (Number(x.price) || 0) * (Number(x.qty) || 0),
        0
    )
);

export const cartService = {
    items,
    totalQty,
    subtotal,
    addToCart,
    removeItem,
    updateQty,
    clearCart,
};
