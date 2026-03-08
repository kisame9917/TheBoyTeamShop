<template>
  <div class="chat-page">
    <div class="header">
      <div>
        <div class="title">Chat hỗ trợ</div>
      </div>

      <div class="join-box">
        <span class="ws-pill" :class="wsStatusClass">{{ wsStatus }}</span>
      </div>
    </div>

    <div class="body">
      <!-- Left: conversations -->
      <div class="side">
        <div class="card side-card">
          <div class="card-title">Inbox</div>

          <div v-if="conversations.length === 0" class="hint">
            Chưa có hội thoại nào. Khi khách gửi tin nhắn đầu tiên, inbox sẽ hiện ở đây.
          </div>

          <div v-else class="conv-list">
            <button
              v-for="c in conversations"
              :key="c.conversationId"
              class="conv-item"
              :class="{
                active: c.conversationId === conversationId,
                urgent: c.needsHuman
              }"
              @click="openConversation(c.conversationId)"
            >
              <div class="row1">
                <div class="customer-wrap">
                  <span class="customer-name">{{ c.customerName }}</span>

                  <span v-if="c.needsHuman" class="ping-wrap" aria-label="Cần tiếp nhận">
                    <span class="ping-ring"></span>
                    <span class="ping-dot"></span>
                  </span>
                </div>

                <span class="badge" v-if="c.unreadCount > 0">{{ c.unreadCount }}</span>
              </div>

              <div class="row2">
                <span class="preview">{{ c.lastMessage || "—" }}</span>
                <span class="time">{{ formatTime(c.lastAt) }}</span>
              </div>

              <div v-if="c.needsHuman" class="conv-actions">
                <button
                  class="take-btn"
                  @click.stop="takeConversation(c)"
                >
                  Tiếp nhận
                </button>
              </div>
            </button>
          </div>
        </div>
      </div>

      <!-- Right: chat -->
      <div class="chat">
        <div class="chat-top">
          <div class="chat-title">
            {{ activeConversationName || "Chưa chọn khách" }}
          </div>
        </div>

        <div ref="msgBox" class="messages">
          <div v-if="!conversationId" class="empty">
            Chọn một hội thoại bên trái để bắt đầu.
          </div>

          <div
            v-for="(m, idx) in messages"
            :key="m.id ?? `${m.senderType}-${m.createdAt}-${idx}`"
            class="msg-row"
            :class="['ADMIN', 'BOT'].includes(m.senderType) ? 'me' : 'them'"
          >
            <div
              class="bubble"
              :class="{
                'bubble-admin': m.senderType === 'ADMIN',
                'bubble-bot': m.senderType === 'BOT',
                'bubble-client': m.senderType === 'CLIENT'
              }"
            >
              <div class="meta">
                <span class="who">
                  {{
                    m.senderType === "BOT"
                      ? "BOT"
                      : m.senderType === "ADMIN"
                      ? "ADMIN"
                      : "KHÁCH"
                  }}
                </span>
                <span class="time">{{ formatTime(m.createdAt) }}</span>
              </div>
              <div class="text">{{ m.content }}</div>
            </div>
          </div>
        </div>

        <div class="input">
          <input
            v-model="input"
            :disabled="!canSend"
            placeholder="Nhập trả lời..."
            @keyup.enter="send"
          />
          <button class="btn" @click="send" :disabled="!canSend">
            Gửi
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, nextTick } from "vue";
import axios from "axios";
import { Client } from "@stomp/stompjs";
import { useAuthStore } from "@/stores/auth";

const API = "http://localhost:8080";
const auth = useAuthStore();

const adminId = computed(
  () =>
    auth.user?.id ||
    auth.username ||
    localStorage.getItem("adminId") ||
    "ADMIN_001"
);

const conversationId = ref(null);
const messages = ref([]);
const input = ref("");
const msgBox = ref(null);

const wsStatus = ref("DISCONNECTED");
const wsStatusClass = computed(() => {
  if (wsStatus.value === "CONNECTED") return "ok";
  if (wsStatus.value === "CONNECTING") return "warn";
  if (wsStatus.value === "ERROR") return "err";
  return "muted";
});

const conversations = ref([]);

const activeConversation = computed(
  () =>
    conversations.value.find(
      (x) => String(x.conversationId) === String(conversationId.value)
    ) || null
);

const activeConversationName = computed(
  () => activeConversation.value?.customerName || ""
);

let stomp = null;
let roomSub = null;
let adminSub = null;

const canSend = computed(() => !!conversationId.value && !!stomp?.connected);

function scrollBottom() {
  if (!msgBox.value) return;
  msgBox.value.scrollTop = msgBox.value.scrollHeight;
}

function formatTime(v) {
  if (!v) return "";
  const d = new Date(v);
  if (Number.isNaN(d.getTime())) return "";

  return d.toLocaleTimeString("vi-VN", {
    timeZone: "Asia/Ho_Chi_Minh",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
}

function normalizeText(text) {
  return String(text || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d")
    .replace(/Đ/g, "D")
    .toLowerCase()
    .trim()
    .replace(/\s+/g, " ");
}

function isHumanSupportRequest(text) {
  const t = normalizeText(text);

  return (
    t.includes("gap nhan vien") ||
    t.includes("gap cskh") ||
    t.includes("ket noi") ||
    t.includes("nhan vien tu van") ||
    t.includes("vui long cho trong giay lat") ||
    t.includes("se ket noi") ||
    t.includes("tu van cua vestshop ngay")
  );
}

function isSameMessage(a, b) {
  if (!a || !b) return false;

  if (a.id != null && b.id != null) {
    return String(a.id) === String(b.id);
  }

  return (
    String(a.conversationId) === String(b.conversationId) &&
    String(a.senderType) === String(b.senderType) &&
    String(a.content) === String(b.content) &&
    String(a.createdAt) === String(b.createdAt)
  );
}

function dedupeMessages(list) {
  const unique = [];
  for (const msg of list || []) {
    const exists = unique.some((m) => isSameMessage(m, msg));
    if (!exists) unique.push(msg);
  }
  return unique;
}

function normalizeConversationItem(c) {
  const rawName =
    c.customerName ??
    c.customerFullName ??
    c.fullName ??
    c.name ??
    c.username ??
    c.customerUsername ??
    c.guestName ??
    "";

  const lastMessage = c.lastMessage ?? "";

  return {
    conversationId: c.id ?? c.conversationId,
    customerName: String(rawName).trim() || "Khách vãng lai",
    lastMessage,
    lastAt: c.lastAt ?? c.createdAt ?? c.updatedAt ?? null,
    unreadCount: Number(c.unreadCount || 0),
    needsHuman: isHumanSupportRequest(lastMessage),
  };
}

function hasRealMessage(c) {
  return !!(c && c.conversationId && c.lastMessage && String(c.lastMessage).trim());
}

function upsertConversationFromMessage(msg) {
  if (!msg?.conversationId || !msg?.content?.trim()) return;

  const id = msg.conversationId;
  const idx = conversations.value.findIndex(
    (x) => String(x.conversationId) === String(id)
  );

  const isActive = String(conversationId.value) === String(id);

  const rawName =
    msg.customerName ??
    msg.customerFullName ??
    msg.fullName ??
    msg.name ??
    msg.username ??
    msg.customerUsername ??
    msg.guestName ??
    "";

  const resolvedName = String(rawName).trim() || "Khách vãng lai";

  const item = {
    conversationId: id,
    customerName: resolvedName,
    lastMessage: msg.content,
    lastAt: msg.createdAt,
    unreadCount: isActive ? 0 : 1,
    needsHuman: isHumanSupportRequest(msg.content),
  };

  if (idx === -1) {
    conversations.value.unshift(item);
    return;
  }

  const current = conversations.value[idx];
  const unread = Number(current.unreadCount || 0);

  conversations.value.splice(idx, 1);
  conversations.value.unshift({
    ...current,
    customerName: current.customerName || resolvedName,
    lastMessage: msg.content,
    lastAt: msg.createdAt,
    unreadCount: isActive ? 0 : unread + 1,
    needsHuman: isHumanSupportRequest(msg.content),
  });
}

async function loadHistory(id) {
  const res = await axios.get(`${API}/api/chat/messages`, {
    params: { conversationId: id },
  });

  messages.value = dedupeMessages(Array.isArray(res.data) ? res.data : []);
  nextTick(scrollBottom);
}

async function refreshList() {
  try {
    const res = await axios.get(`${API}/api/chat/conversations/open`);

    const list = (Array.isArray(res.data) ? res.data : [])
      .map(normalizeConversationItem)
      .filter(hasRealMessage)
      .sort((a, b) => new Date(b.lastAt || 0) - new Date(a.lastAt || 0));

    conversations.value = list;
  } catch (e) {
    console.warn("Load conversations failed:", e?.message || e);
  }
}

function connectWsIfNeeded() {
  if (stomp) return;

  const wsUrl = API.replace(/^http/, "ws") + "/ws";

  wsStatus.value = "CONNECTING";

  stomp = new Client({
    brokerURL: wsUrl,
    reconnectDelay: 2000,
    debug: () => {},
  });

  stomp.onConnect = () => {
    wsStatus.value = "CONNECTED";

    try {
      adminSub?.unsubscribe();
    } catch (e) {}

    adminSub = stomp.subscribe("/topic/admin/conversations", (frame) => {
      const msg = JSON.parse(frame.body);
      upsertConversationFromMessage(msg);
    });

    if (conversationId.value) {
      subscribeRoom(conversationId.value);
    }
  };

  stomp.onStompError = () => {
    wsStatus.value = "ERROR";
  };

  stomp.onWebSocketClose = () => {
    if (wsStatus.value !== "ERROR") {
      wsStatus.value = "DISCONNECTED";
    }
  };

  stomp.activate();
}

function subscribeRoom(id) {
  if (!stomp?.connected) return;

  try {
    roomSub?.unsubscribe();
  } catch (e) {}

  roomSub = stomp.subscribe(`/topic/conversations/${id}`, (frame) => {
    const msg = JSON.parse(frame.body);

    const exists = messages.value.some((m) => isSameMessage(m, msg));
    if (!exists) {
      messages.value.push(msg);
      nextTick(scrollBottom);
    }

    const idx = conversations.value.findIndex(
      (x) => String(x.conversationId) === String(id)
    );
    if (idx !== -1) {
      conversations.value[idx].lastMessage = msg.content;
      conversations.value[idx].lastAt = msg.createdAt;
      conversations.value[idx].customerName =
        conversations.value[idx].customerName ||
        msg.customerName ||
        "Khách vãng lai";
      conversations.value[idx].needsHuman = isHumanSupportRequest(msg.content);
      conversations.value[idx].unreadCount =
        msg.senderType === "CLIENT" && String(conversationId.value) !== String(id)
          ? Number(conversations.value[idx].unreadCount || 0) + 1
          : 0;
    }
  });
}

async function openConversation(id) {
  connectWsIfNeeded();

  conversationId.value = id;

  const idx = conversations.value.findIndex(
    (x) => String(x.conversationId) === String(id)
  );
  if (idx !== -1) {
    conversations.value[idx].unreadCount = 0;
  }

  await loadHistory(id);

  if (stomp?.connected) {
    subscribeRoom(id);
  }
}

function send() {
  const content = input.value.trim();
  if (!content) return;
  if (!conversationId.value) return;
  if (!stomp?.connected) return;

  stomp.publish({
    destination: "/app/chat.send",
    body: JSON.stringify({
      conversationId: conversationId.value,
      senderType: "ADMIN",
      senderId: adminId.value,
      content,
    }),
  });

  input.value = "";
}

async function takeConversation(c) {
  await openConversation(c.conversationId);

  if (!stomp?.connected) return;

  stomp.publish({
    destination: "/app/chat.send",
    body: JSON.stringify({
      conversationId: c.conversationId,
      senderType: "ADMIN",
      senderId: adminId.value,
      content: "Em đã tiếp nhận cuộc trò chuyện này. Anh/chị cần hỗ trợ gì thêm ạ?",
    }),
  });

  const idx = conversations.value.findIndex(
    (x) => String(x.conversationId) === String(c.conversationId)
  );
  if (idx !== -1) {
    conversations.value[idx].needsHuman = false;
    conversations.value[idx].unreadCount = 0;
  }
}

onMounted(async () => {
  connectWsIfNeeded();
  await refreshList();
});

onBeforeUnmount(() => {
  try {
    roomSub?.unsubscribe();
  } catch (e) {}
  try {
    adminSub?.unsubscribe();
  } catch (e) {}
  try {
    stomp?.deactivate();
  } catch (e) {}
});
</script>

<style scoped>
.chat-page {
  padding: 18px;
  height: calc(100vh - 70px);
  overflow: hidden;
  box-sizing: border-box;
}

.header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 14px;
}

.title {
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
}

.join-box {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ws-pill {
  height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  font-weight: 700;
}

.ws-pill.ok {
  background: rgba(34, 197, 94, 0.12);
  color: #15803d;
}

.ws-pill.warn {
  background: rgba(245, 158, 11, 0.14);
  color: #92400e;
}

.ws-pill.err {
  background: rgba(239, 68, 68, 0.12);
  color: #b91c1c;
}

.ws-pill.muted {
  background: rgba(100, 116, 139, 0.12);
  color: #334155;
}

.body {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 14px;
  height: calc(100vh - 140px);
  min-height: 0;
}

.side {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.side-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.card {
  background: #fff;
  border: 1px solid rgba(2, 6, 23, 0.08);
  border-radius: 14px;
  padding: 12px;
  box-shadow: 0 10px 22px rgba(2, 6, 23, 0.06);
}

.card-title {
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 8px;
  flex-shrink: 0;
}

.hint {
  margin-top: 10px;
  font-size: 12px;
  color: #64748b;
  line-height: 1.35;
}

.conv-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 10px;
  overflow: auto;
  min-height: 0;
}

.conv-item {
  text-align: left;
  border: 1px solid rgba(2, 6, 23, 0.08);
  background: #fff;
  border-radius: 12px;
  padding: 10px;
  cursor: pointer;
}

.conv-item.active {
  border-color: rgba(41, 84, 184, 0.35);
  background: rgba(41, 84, 184, 0.06);
}

.conv-item.urgent {
  border-color: rgba(245, 158, 11, 0.45);
  background: rgba(255, 247, 237, 0.9);
}

.row1 {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #0f172a;
  gap: 10px;
}

.customer-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.customer-name {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 190px;
}

.row2 {
  margin-top: 6px;
  display: flex;
  justify-content: space-between;
  gap: 10px;
  color: #64748b;
  font-size: 12px;
}

.preview {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 210px;
}

.time {
  flex-shrink: 0;
}

.badge {
  min-width: 22px;
  height: 20px;
  padding: 0 8px;
  border-radius: 999px;
  background: rgba(239, 68, 68, 0.12);
  color: #b91c1c;
  font-weight: 900;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.conv-actions {
  margin-top: 10px;
}

.take-btn {
  height: 34px;
  padding: 0 12px;
  border-radius: 10px;
  border: none;
  background: #f59e0b;
  color: #fff;
  font-weight: 800;
  cursor: pointer;
}

.ping-wrap {
  position: relative;
  width: 12px;
  height: 12px;
  flex-shrink: 0;
}

.ping-dot {
  position: absolute;
  inset: 0;
  border-radius: 999px;
  background: #ef4444;
}

.ping-ring {
  position: absolute;
  inset: 0;
  border-radius: 999px;
  background: rgba(239, 68, 68, 0.45);
  animation: ping 1.4s infinite;
}

@keyframes ping {
  0% {
    transform: scale(0.9);
    opacity: 0.9;
  }
  70% {
    transform: scale(2);
    opacity: 0;
  }
  100% {
    transform: scale(2);
    opacity: 0;
  }
}

.chat {
  background: #fff;
  border: 1px solid rgba(2, 6, 23, 0.08);
  border-radius: 14px;
  box-shadow: 0 10px 22px rgba(2, 6, 23, 0.06);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
  height: 100%;
}

.chat-top {
  height: 54px;
  padding: 0 12px;
  border-bottom: 1px solid rgba(2, 6, 23, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f8fafc;
  flex-shrink: 0;
}

.chat-title {
  font-weight: 700;
  color: #0f172a;
}

.messages {
  flex: 1;
  padding: 12px;
  overflow: auto;
  background: #f6f8fb;
  min-height: 0;
}

.empty {
  color: #64748b;
  padding: 16px;
  border: 1px dashed rgba(2, 6, 23, 0.16);
  border-radius: 12px;
  background: #fff;
}

.msg-row {
  display: flex;
  margin: 10px 0;
}

.msg-row.me {
  justify-content: flex-end;
}

.msg-row.them {
  justify-content: flex-start;
}

.bubble {
  max-width: 70%;
  padding: 10px;
  border-radius: 12px;
  border: 1px solid rgba(2, 6, 23, 0.06);
}

.bubble-admin {
  background: #e9f3ff;
  border-color: rgba(10, 133, 237, 0.25);
}

.bubble-bot {
  background: #ede9fe;
  border-color: rgba(124, 58, 237, 0.25);
}

.bubble-client {
  background: #fff;
  border-color: rgba(2, 6, 23, 0.06);
}

.meta {
  display: flex;
  gap: 10px;
  justify-content: space-between;
  font-size: 11px;
  color: #64748b;
  margin-bottom: 6px;
}

.who {
  font-weight: 800;
}

.text {
  white-space: pre-wrap;
  word-break: break-word;
  color: #0f172a;
}

.input {
  display: flex;
  gap: 10px;
  padding: 12px;
  border-top: 1px solid rgba(2, 6, 23, 0.06);
  background: #fff;
  flex-shrink: 0;
}

.input input {
  flex: 1;
  height: 42px;
  padding: 0 12px;
  border-radius: 12px;
  border: 1px solid rgba(2, 6, 23, 0.14);
  outline: none;
}

.btn {
  height: 42px;
  padding: 0 14px;
  border-radius: 12px;
  border: none;
  background: #2954b8;
  color: #fff;
  font-weight: 800;
  cursor: pointer;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 980px) {
  .body {
    grid-template-columns: 1fr;
  }

  .preview,
  .customer-name {
    max-width: 100%;
  }
}
</style>