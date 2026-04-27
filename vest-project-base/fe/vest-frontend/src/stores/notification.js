import { defineStore } from "pinia";
import { Client } from "@stomp/stompjs";

function getRole() {
  return String(
    localStorage.getItem("vest_role") || localStorage.getItem("role") || ""
  ).toUpperCase();
}

function getUserId() {
  try {
    const raw = localStorage.getItem("vest_user");
    if (!raw) return null;

    const u = JSON.parse(raw);
    return u?.id || u?.idNhanVien || u?.userId || null;
  } catch {
    return null;
  }
}

function baseUrl() {
  return (
    import.meta.env.VITE_API_URL ||
    import.meta.env.VITE_API_BASE ||
    "http://localhost:8080"
  ).replace(/\/+$/, "");
}

function normalizeNoti(raw) {
  return {
    id: raw?.id || `${Date.now()}-${Math.random()}`,
    title: raw?.title || "Thông báo mới",
    time: raw?.time || "Vừa xong",
    read: !!raw?.read,
    link: raw?.link || "",
    type: raw?.type || "INFO",
    createdAt: raw?.createdAt || new Date().toISOString(),
  };
}

export const useNotificationStore = defineStore("notification", {
  state: () => ({
    items: [],
    stompClient: null,
    connected: false,
    inited: false,
  }),

  getters: {
    unreadCount: (state) => state.items.filter((x) => !x.read).length,
  },

  actions: {
    saveLocal() {
      localStorage.setItem("vest_notifications", JSON.stringify(this.items));
    },

    loadLocal() {
      try {
        this.items = JSON.parse(
          localStorage.getItem("vest_notifications") || "[]"
        );
      } catch {
        this.items = [];
      }
    },

    add(item) {
      const n = normalizeNoti(item);

      const existed = this.items.some((x) => String(x.id) === String(n.id));
      if (existed) return;

      this.items.unshift(n);
      this.items = this.items.slice(0, 50);
      this.saveLocal();
    },

    markRead(id) {
      const n = this.items.find((x) => String(x.id) === String(id));
      if (!n) return;

      n.read = true;
      this.saveLocal();
    },

    markAllRead() {
      this.items = this.items.map((x) => ({ ...x, read: true }));
      this.saveLocal();
    },

    connect() {
      if (this.stompClient) return;

      const wsUrl = baseUrl().replace(/^http/, "ws") + "/ws";

      const client = new Client({
        brokerURL: wsUrl,
        reconnectDelay: 3000,
        debug: () => {},
      });

      client.onConnect = () => {
        this.connected = true;

        const role = getRole();
        const userId = getUserId();

        if (role) {
          client.subscribe(`/topic/roles/${role}/notifications`, (message) => {
            try {
              this.add(JSON.parse(message.body));
            } catch {}
          });
        }

        if (userId) {
          client.subscribe(`/topic/users/${userId}/notifications`, (message) => {
            try {
              this.add(JSON.parse(message.body));
            } catch {}
          });
        }
      };

      client.onStompError = () => {
        this.connected = false;
      };

      client.onWebSocketClose = () => {
        this.connected = false;
      };

      client.activate();
      this.stompClient = client;
    },

    disconnect() {
      if (this.stompClient) {
        this.stompClient.deactivate();
        this.stompClient = null;
      }

      this.connected = false;
      this.inited = false;
    },

    init() {
      if (this.inited) return;

      this.loadLocal();
      this.connect();
      this.inited = true;
    },
  },
});