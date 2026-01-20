import { reactive } from "vue";

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

export function useToast() {
  return { state, show, remove };
}
