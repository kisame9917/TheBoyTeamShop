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
      <!-- Left -->
      <div class="side">
        <div class="card side-card">
          <div class="side-header">
            <div class="side-title">Quản lý Chat</div>
            <span class="queue-badge">{{ waitingCount }} chờ</span>
          </div>

          <div class="sub-tabs">
            <button
              class="sub-tab"
              :class="{ active: subTab === 'ACTIVE' }"
              @click="subTab = 'ACTIVE'"
            >
              <span>Đang hoạt động</span>
            </button>

            <button
              class="sub-tab"
              :class="{ active: subTab === 'PENDING' }"
              @click="subTab = 'PENDING'"
            >
              <span>Chờ nhận</span>
            </button>
          </div>

          <div v-if="filteredConversations.length === 0" class="hint">
            Chưa có hội thoại nào. Khi khách gửi tin nhắn đầu tiên, inbox sẽ hiện ở đây.
          </div>

          <div v-else class="conv-list">
            <button
              v-for="c in filteredConversations"
              :key="c.conversationId"
              class="conv-item list-style"
              :class="{
                active: c.conversationId === conversationId,
                urgent: c.needsHuman
              }"
              @click="openConversation(c.conversationId)"
            >
              <div class="conv-top">
                <div class="customer-name">{{ c.customerName }}</div>

                <span
                  v-if="c.needsHuman"
                  class="status-chip waiting"
                >
                  Chờ tiếp nhận
                </span>

                <span
                  v-else-if="c.handledByAI && !c.isTaken"
                  class="status-chip bot-chip"
                >
                  AI đang xử lý
                </span>
              </div>

              <div class="preview-line">
                {{ c.lastMessage || "—" }}
              </div>

              <div v-if="c.isTaken && c.takenByName" class="handler-line">
                Người hỗ trợ: {{ c.takenByName }}
              </div>

              <div class="conv-bottom">
                <span class="time">{{ formatTime(c.lastAt) }}</span>
                <span v-if="c.unreadCount > 0" class="badge">
                  {{ c.unreadCount }}
                </span>
              </div>

              <div v-if="c.needsHuman" class="conv-actions">
                <button class="take-btn" @click.stop="takeConversation(c)">
                   Tiếp nhận
                </button>
              </div>
            </button>
          </div>
        </div>
      </div>

      <!-- Right -->
      <div class="chat">
        <div class="chat-top">
          <div class="chat-top-left">
            <div class="chat-title">
              {{ activeConversationName || "Chưa chọn khách" }}
            </div>

            <div
              v-if="activeConversation?.isTaken && activeConversation?.takenByName"
              class="assigned-admin"
            >
              Người đang hỗ trợ: {{ activeConversation.takenByName }}
            </div>
          </div>

          <div v-if="activeConversation" class="chat-top-right">
            <span
              v-if="activeConversation.needsHuman"
              class="status-chip waiting"
            >
              Chờ tiếp nhận
            </span>

            <span
              v-else-if="activeConversation.handledByAI && !activeConversation.isTaken"
              class="status-chip bot-chip"
            >
              AI đang xử lý
            </span>

            <span
              v-else-if="activeConversation.isTaken"
              class="status-chip success"
            >
              Đã tiếp nhận
            </span>
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
                      ? (activeConversation?.takenByName || adminLabel)
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
          <button
            class="btn"
            @click="send"
            :disabled="!canSend"
          >
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
  () => auth.user?.id || localStorage.getItem("adminId") || "1"
);

const adminCode = computed(() => {
  return (
    auth.user?.maNhanVien ||
    auth.user?.employeeCode ||
    auth.user?.staffCode ||
    auth.user?.code ||
    localStorage.getItem("adminCode") ||
    `NV${String(adminId.value).padStart(3, "0")}`
  );
});

const adminName = computed(() => {
  return (
    auth.user?.tenNhanVien ||
    auth.user?.fullName ||
    auth.user?.name ||
    auth.user?.username ||
    auth.user?.taiKhoan ||
    localStorage.getItem("adminName") ||
    "Chưa có tên"
  );
});

const adminLabel = computed(() => {
  return `${adminCode.value} - ${adminName.value}`;
});

const adminTakeMessageName = computed(() => {
  return adminName.value;
});

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

const mainTab = ref("CUSTOMER");
const subTab = ref("ACTIVE");

const activeConversation = computed(
  () =>
    conversations.value.find(
      (x) => String(x.conversationId) === String(conversationId.value)
    ) || null
);

const activeConversationName = computed(
  () => activeConversation.value?.customerName || ""
);

const waitingCount = computed(
  () =>
    conversations.value.filter(
      (c) => c.needsHuman && c.status !== "CLOSED"
    ).length
);

const filteredConversations = computed(() => {
  let list = [...conversations.value];

  if (mainTab.value === "CUSTOMER") {
    list = list.filter((c) => !c.isInternal);
  } else {
    list = list.filter((c) => c.isInternal);
  }

  if (subTab.value === "PENDING") {
    list = list.filter((c) => c.needsHuman && c.status !== "CLOSED");
  } else {
    list = list.filter((c) => c.status !== "CLOSED");
  }

  return list.sort((a, b) => new Date(b.lastAt || 0) - new Date(a.lastAt || 0));
});

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
    status: c.status ?? "ACTIVE",
    isInternal: Boolean(c.isInternal ?? false),
    handledByAI: Boolean(c.handledByAI ?? false),
    isTaken: Boolean(c.isTaken ?? false),
    takenByName: c.takenByName ?? "",
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
    status: msg.status ?? "ACTIVE",
    isInternal: Boolean(msg.isInternal ?? false),
    handledByAI: Boolean(msg.senderType === "BOT" || msg.handledByAI),
    isTaken: Boolean(msg.isTaken ?? false),
    takenByName: msg.takenByName ?? "",
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
    status: msg.status ?? current.status,
    handledByAI: msg.senderType === "BOT" ? true : current.handledByAI,
    isTaken:
      msg.senderType === "ADMIN"
        ? true
        : current.isTaken,
    takenByName:
      msg.senderType === "ADMIN"
        ? current.takenByName || adminLabel.value
        : current.takenByName,
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
      const current = conversations.value[idx];
      current.lastMessage = msg.content;
      current.lastAt = msg.createdAt;
      current.customerName =
        current.customerName || msg.customerName || "Khách vãng lai";
      current.status = msg.status ?? current.status;
      current.needsHuman = isHumanSupportRequest(msg.content);

      if (msg.senderType === "BOT") {
        current.handledByAI = true;
      }

      if (msg.senderType === "ADMIN") {
        current.isTaken = true;
        current.takenByName = current.takenByName || adminLabel.value;
        current.needsHuman = false;
      }

      current.unreadCount =
        msg.senderType === "CLIENT" && String(conversationId.value) !== String(id)
          ? Number(current.unreadCount || 0) + 1
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

  const idx = conversations.value.findIndex(
    (x) => String(x.conversationId) === String(conversationId.value)
  );
  if (idx !== -1) {
    conversations.value[idx].isTaken = true;
    conversations.value[idx].takenByName = adminLabel.value;
    conversations.value[idx].needsHuman = false;
  }

  input.value = "";
}

async function takeConversation(c) {
  await openConversation(c.conversationId);

  if (!stomp?.connected) return;

  const message = `${adminTakeMessageName.value} sẽ hỗ trợ anh/chị từ bây giờ ạ. Anh/chị cần em hỗ trợ gì thêm không?`;

  stomp.publish({
    destination: "/app/chat.send",
    body: JSON.stringify({
      conversationId: c.conversationId,
      senderType: "ADMIN",
      senderId: adminId.value,
      content: message,
    }),
  });

  const idx = conversations.value.findIndex(
    (x) => String(x.conversationId) === String(c.conversationId)
  );

  if (idx !== -1) {
    conversations.value[idx].needsHuman = false;
    conversations.value[idx].isTaken = true;
    conversations.value[idx].takenByName = adminLabel.value;
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
  background: #f5f7fb;
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
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  font-weight: 800;
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
  grid-template-columns: 360px 1fr;
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
  padding: 0;
  overflow: hidden;
}

.card {
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 16px;
  box-shadow: 0 10px 22px rgba(2, 6, 23, 0.06);
}

.side-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 14px 10px;
  gap: 10px;
}

.side-title {
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
}

.queue-badge {
  background: #e0f2fe;
  color: #075985;
  font-size: 11px;
  font-weight: 800;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid #bae6fd;
  white-space: nowrap;
}

.main-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin: 0 14px 10px;
}

.main-tab {
  min-height: 42px;
  border: 1px solid #dbeafe;
  background: #f8fbff;
  color: #475569;
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
  border-radius: 12px;
  padding: 0 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-tab span {
  display: inline-block;
  white-space: nowrap;
}

.main-tab.active {
  background: #2954b8;
  border-color: #2954b8;
  color: #fff;
}

.sub-tabs {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  margin: 0 14px;
  padding-bottom: 6px;
  border-bottom: 1px solid #e2e8f0;
}

.sub-tab {
  min-height: 44px;
  padding: 0 6px;
  border: none;
  background: transparent;
  color: #64748b;
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  line-height: 1.2;
}

.sub-tab span {
  display: inline-block;
  max-width: 100%;
}

.sub-tab.active {
  color: #2954b8;
}

.sub-tab.active::after {
  content: "";
  position: absolute;
  left: 14px;
  right: 14px;
  bottom: -7px;
  height: 3px;
  border-radius: 999px;
  background: #2954b8;
}

.hint {
  margin: 14px;
  font-size: 12px;
  color: #64748b;
  line-height: 1.4;
}

.conv-list {
  display: flex;
  flex-direction: column;
  margin-top: 6px;
  overflow: auto;
  min-height: 0;
  padding: 0 14px 14px;
}

.conv-item.list-style {
  text-align: left;
  border: none;
  border-bottom: 1px solid #eef2f7;
  border-radius: 12px;
  padding: 12px 8px;
  background: #fff;
  cursor: pointer;
}

.conv-item.list-style.active {
  background: #eff6ff;
}

.conv-item.list-style.urgent {
  background: #f8fbff;
}

.conv-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}

.customer-name {
  font-size: 14px;
  font-weight: 800;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 170px;
}

.preview-line {
  font-size: 13px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conv-bottom {
  margin-top: 6px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.time {
  font-size: 12px;
  color: #94a3b8;
}

.badge {
  min-width: 22px;
  height: 20px;
  padding: 0 8px;
  border-radius: 999px;
  background: rgba(41, 84, 184, 0.12);
  color: #2954b8;
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
  padding: 0 14px;
  border-radius: 999px;
  border: none;
  background: #2954b8;
  color: #fff;
  font-weight: 800;
  cursor: pointer;
  font-size: 12px;
}

.take-btn:hover {
  background: #1e429f;
}

.status-chip {
  flex-shrink: 0;
  font-size: 11px;
  font-weight: 800;
  padding: 4px 8px;
  border-radius: 999px;
  border: 1px solid transparent;
}

.status-chip.waiting {
  background: #e0f2fe;
  color: #075985;
  border-color: #bae6fd;
}

.status-chip.bot-chip {
  background: #ecfeff;
  color: #0f766e;
  border-color: #a5f3fc;
}

.status-chip.success {
  background: #dbeafe;
  color: #1d4ed8;
  border-color: #bfdbfe;
}

.handler-line {
  margin-top: 4px;
  font-size: 12px;
  color: #2954b8;
  font-weight: 700;
}

.chat {
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 16px;
  box-shadow: 0 10px 22px rgba(2, 6, 23, 0.06);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
  height: 100%;
}

.chat-top {
  min-height: 64px;
  padding: 10px 14px;
  border-bottom: 1px solid rgba(2, 6, 23, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f8fbff;
  gap: 12px;
  flex-shrink: 0;
}

.chat-top-left {
  min-width: 0;
}

.chat-top-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-title {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
}

.assigned-admin {
  margin-top: 4px;
  font-size: 12px;
  color: #2954b8;
  font-weight: 700;
}

.messages {
  flex: 1;
  padding: 14px;
  overflow: auto;
  background: #f5f7fb;
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
  margin: 12px 0;
}

.msg-row.me {
  justify-content: flex-end;
}

.msg-row.them {
  justify-content: flex-start;
}

.bubble {
  max-width: 72%;
  padding: 12px;
  border-radius: 14px;
  border: 1px solid rgba(2, 6, 23, 0.06);
}

.bubble-admin {
  background: #eaf2ff;
  border-color: rgba(41, 84, 184, 0.28);
}

.bubble-bot {
  background: #f0fdfa;
  border-color: rgba(13, 148, 136, 0.2);
}

.bubble-client {
  background: #ffffff;
  border-color: rgba(148, 163, 184, 0.24);
}

.meta {
  display: flex;
  gap: 10px;
  justify-content: space-between;
  font-size: 11px;
  color: #64748b;
  margin-bottom: 8px;
}

.who {
  font-weight: 900;
}

.text {
  white-space: pre-wrap;
  word-break: break-word;
  color: #0f172a;
  line-height: 1.5;
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
  height: 44px;
  padding: 0 14px;
  border-radius: 12px;
  border: 1px solid rgba(2, 6, 23, 0.14);
  outline: none;
  background: #fff;
}

.input input:focus {
  border-color: #2954b8;
  box-shadow: 0 0 0 3px rgba(41, 84, 184, 0.12);
}

.btn {
  height: 44px;
  padding: 0 16px;
  border-radius: 12px;
  border: none;
  background: #2954b8;
  color: #fff;
  font-weight: 800;
  cursor: pointer;
}

.btn:hover {
  background: #1e429f;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 1200px) {
  .main-tab,
  .sub-tab {
    font-size: 12px;
  }
}

@media (max-width: 980px) {
  .body {
    grid-template-columns: 1fr;
  }

  .customer-name,
  .preview-line {
    max-width: 100%;
  }

  .chat-top {
    flex-direction: column;
    align-items: flex-start;
  }

  .chat-top-right {
    flex-wrap: wrap;
  }
}
</style>