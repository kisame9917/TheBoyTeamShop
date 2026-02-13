import { reactive } from "vue";

// Toast state dùng chung toàn app
let _id = 0;

const toastState = reactive({
  items: [], // {id, type, title, message}
});

function removeToast(id) {
  const idx = toastState.items.findIndex((t) => t.id === id);
  if (idx !== -1) toastState.items.splice(idx, 1);
}

function addToast({ type = "info", title = "", message = "", duration = 2500 } = {}) {
  const id = ++_id;
  toastState.items.push({ id, type, title, message });
  if (duration && duration > 0) setTimeout(() => removeToast(id), duration);
  return id;
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
  };
}
