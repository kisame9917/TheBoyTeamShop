import { reactive, computed } from "vue";
import { Client } from "@stomp/stompjs";

const state = reactive({
  items: [],
  stompClient: null,
  connected: false,
  inited: false,
});

function getToken() {
  return (
    localStorage.getItem("USER_ACCESS_TOKEN") ||
    sessionStorage.getItem("USER_ACCESS_TOKEN") ||
    localStorage.getItem("vest_token") ||
    sessionStorage.getItem("vest_token") ||
    ""
  );
}

function getUserId() {
  const directId =
    localStorage.getItem("USER_ID") ||
    sessionStorage.getItem("USER_ID") ||
    "";

  if (directId) return directId;

  const raw =
    localStorage.getItem("vest_user") ||
    sessionStorage.getItem("vest_user") ||
    localStorage.getItem("user") ||
    sessionStorage.getItem("user") ||
    "";

  if (!raw) return null;

  try {
    const u = JSON.parse(raw);

    return (
      u?.id ||
      u?.idKhachHang ||
      u?.khachHangId ||
      u?.maKhachHang ||
      u?.userId ||
      null
    );
  } catch {
    return null;
  }
}

function storageKey() {
  const userId = getUserId();
  return userId ? `client_notifications_${userId}` : "client_notifications";
}

function baseUrl() {
  return (
    import.meta.env.VITE_API_URL ||
    import.meta.env.VITE_API_BASE ||
    import.meta.env.VITE_API_BASE_URL ||
    "http://localhost:8080"
  ).replace(/\/+$/, "");
}

function normalizeNoti(raw) {
  return {
    id: raw?.id || `${Date.now()}-${Math.random()}`,
    title:
      raw?.title ||
      raw?.tieuDe ||
      raw?.noiDung ||
      raw?.message ||
      "Thông báo mới",
    time: raw?.time || raw?.thoiGian || "Vừa xong",
    read: !!raw?.read,
    link: raw?.link || raw?.url || "",
    type: raw?.type || "INFO",
    createdAt: raw?.createdAt || new Date().toISOString(),
    hoaDonId: raw?.hoaDonId || raw?.idHoaDon || null,
    maHoaDon: raw?.maHoaDon || raw?.code || "",
    trangThai: raw?.trangThai || raw?.status || "",
  };
}

function saveLocal() {
  localStorage.setItem(storageKey(), JSON.stringify(state.items));
}

function loadLocal() {
  try {
    state.items = JSON.parse(localStorage.getItem(storageKey()) || "[]");
  } catch {
    state.items = [];
  }
}

function add(item) {
  const n = normalizeNoti(item);

  const existed = state.items.some((x) => String(x.id) === String(n.id));
  if (existed) return;

  state.items.unshift(n);
  state.items = state.items.slice(0, 50);
  saveLocal();
}

export function useNotificationStore() {
  const unreadCount = computed(() => {
    return state.items.filter((item) => !item.read).length;
  });

  function connect() {
    if (state.stompClient) return;

    const token = getToken();
    const userId = getUserId();

    if (!token || !userId) return;

    const wsUrl = baseUrl().replace(/^http/, "ws") + "/ws";

    const client = new Client({
      brokerURL: wsUrl,
      reconnectDelay: 3000,
      debug: () => {},
    });

    client.onConnect = () => {
      state.connected = true;

      client.subscribe(`/topic/users/${userId}/notifications`, (message) => {
        try {
          add(JSON.parse(message.body));
        } catch {}
      });
    };

    client.onStompError = () => {
      state.connected = false;
    };

    client.onWebSocketClose = () => {
      state.connected = false;
    };

    client.activate();
    state.stompClient = client;
  }

  function init() {
    if (state.inited) return;

    loadLocal();
    connect();
    state.inited = true;
  }

  function disconnect() {
    if (state.stompClient) {
      state.stompClient.deactivate();
      state.stompClient = null;
    }

    state.connected = false;
    state.inited = false;
  }

  function markRead(id) {
    state.items = state.items.map((item) => {
      if (String(item.id) === String(id)) {
        return {
          ...item,
          read: true,
        };
      }

      return item;
    });

    saveLocal();
  }

  function markAllRead() {
    state.items = state.items.map((item) => ({
      ...item,
      read: true,
    }));

    saveLocal();
  }

  return {
    get items() {
      return state.items;
    },
    get unreadCount() {
      return unreadCount.value;
    },
    init,
    disconnect,
    markRead,
    markAllRead,
    add,
  };
}