
// src/composables/useToast.js
import { reactive } from "vue"
import { reactive } from "vue";

let _id = 0

export const toastState = reactive({
  items: [],
})

function addToast({ type = "info", title = "", message = "", duration = 2500 } = {}) {
  const id = ++_id
  toastState.items.push({ id, type, title, message })

  if (duration && duration > 0) {
    setTimeout(() => removeToast(id), duration)
  }
  return id
}

export function removeToast(id) {
  const idx = toastState.items.findIndex((t) => t.id === id)
  if (idx !== -1) toastState.items.splice(idx, 1)
}

export function useToast() {
  return {
    state: toastState,
    show: (message, opt = {}) => addToast({ message, ...opt }),
    success: (message, opt = {}) => addToast({ type: "success", title: "Thành công", message, ...opt }),
    error: (message, opt = {}) => addToast({ type: "error", title: "Lỗi", message, ...opt }),
    info: (message, opt = {}) => addToast({ type: "info", title: "Thông báo", message, ...opt }),
    warning: (message, opt = {}) => addToast({ type: "warning", title: "Cảnh báo", message, ...opt }),
    remove: removeToast,
    clear: () => (toastState.items.length = 0),
  }

const state = reactive({
  items: [],
});

let _id = 0;

function remove(id) {
  const i = state.items.findIndex((t) => t.id === id);
  if (i !== -1) state.items.splice(i, 1);
}

function show({ message, type = "info", title = "", timeout = 3000 } = {}) {
  const id = ++_id;
  state.items.push({ id, message, type, title });
  if (timeout && timeout > 0) setTimeout(() => remove(id), timeout);
  return id;
}


}
