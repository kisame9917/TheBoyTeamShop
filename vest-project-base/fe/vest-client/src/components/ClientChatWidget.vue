<template>
  <!-- Icon chat nổi -->
  <button class="chat-fab" @click="toggle" aria-label="Chat hỗ trợ">
    💬
  </button>

  <!-- Popup chat -->
  <div v-if="open" class="chat-box">
    <div class="chat-header">
      <div class="title">Hỗ trợ VestShop</div>
      <button class="close-btn" @click="toggle" aria-label="Đóng">✕</button>
    </div>

    <div ref="msgBox" class="chat-messages">
      <div
        v-for="(m, idx) in messages"
        :key="m.id ?? `${m.senderType}-${m.createdAt}-${idx}`"
        class="msg-row"
        :class="m.senderType === 'CLIENT' ? 'me' : 'them'"
      >
        <div class="bubble">
          <div class="sender" v-if="m.senderType !== 'CLIENT'">
            {{ m.senderType === 'BOT' ? 'Bot' : 'CSKH' }}
          </div>

          <div class="text">{{ m.content }}</div>

          <div v-if="getQuickOptions(m).length" class="quick-options">
            <button
              v-for="(opt, i) in getQuickOptions(m)"
              :key="i"
              class="quick-btn"
              @click="sendQuickOption(opt)"
            >
              {{ opt }}
            </button>
          </div>

          <div class="time">{{ formatTime(m.createdAt) }}</div>
        </div>
      </div>
    </div>

    <div class="chat-input">
      <input
        v-model="input"
        @keyup.enter="send"
        placeholder="Nhập tin nhắn..."
      />
      <button @click="send">Gửi</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from "vue";
import axios from "axios";
import { Client } from "@stomp/stompjs";

const API = import.meta.env.VITE_API_BASE;

function getLoggedInUserId() {
  const raw = localStorage.getItem("vest_user");
  if (!raw) return null;
  try {
    const u = JSON.parse(raw);
    return u?.id ? String(u.id) : (u?.taiKhoan ? String(u.taiKhoan) : null);
  } catch {
    return null;
  }
}

function getGuestId() {
  let id = localStorage.getItem("guestId");
  if (!id) {
    id = crypto.randomUUID();
    localStorage.setItem("guestId", id);
  }
  return id;
}

function resolveCustomerId() {
  return getLoggedInUserId() ?? getGuestId();
}

function convKey(cid) {
  return `conversationId:${cid}`;
}

const open = ref(false);
const conversationId = ref(null);
const currentCustomerId = ref("");
const messages = ref([]);
const input = ref("");
const msgBox = ref(null);

let stomp = null;
let sub = null;

function toggle() {
  open.value = !open.value;
  if (open.value) {
    syncCustomerAndConversation().then(() => {
      nextTick(scrollBottom);
    });
  }
}

function scrollBottom() {
  if (!msgBox.value) return;
  msgBox.value.scrollTop = msgBox.value.scrollHeight;
}

function formatTime(iso) {
  if (!iso) return "";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "";
  return d.toLocaleTimeString("vi-VN", {
    timeZone: "Asia/Ho_Chi_Minh",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
}

function getQuickOptions(message) {
  if (message.senderType !== "BOT") return [];

  const content = (message.content || "").toLowerCase();

  if (content.includes("shop có thể hỗ trợ gì")) {
    return ["Kiểm tra đơn hàng", "Phí ship", "Tư vấn size", "Gặp CSKH"];
  }
  if (content.includes("kiểm tra phí ship khu vực nào")) {
    return ["Nội thành TP.HCM", "Ngoại thành TP.HCM", "Tỉnh khác", "Gặp CSKH"];
  }
  if (content.includes("gửi mã đơn hàng")) {
    return ["Đơn của tôi", "Gặp CSKH"];
  }
  if (content.includes("thanh toán theo cách nào")) {
    return ["COD", "Chuyển khoản", "Gặp CSKH"];
  }
  if (content.includes("tư vấn size phù hợp")) {
    return ["Nam 1m70 65kg", "Nam 1m75 70kg", "Nữ 1m55 45kg", "Gặp CSKH"];
  }

  return [];
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

function publishMessage(content) {
  if (!content?.trim()) return;
  if (!conversationId.value) return;
  if (!stomp?.connected) return;

  stomp.publish({
    destination: "/app/chat.send",
    body: JSON.stringify({
      conversationId: conversationId.value,
      senderType: "CLIENT",
      senderId: currentCustomerId.value,
      content: content.trim(),
    }),
  });
}

async function createConversationAndSendFirstMessage(content) {
  if (!content?.trim()) return;

  const cv = await axios.post(`${API}/api/chat/conversation`, null, {
    params: { customerId: currentCustomerId.value },
  });

  conversationId.value = cv.data.id;
  localStorage.setItem(
    convKey(currentCustomerId.value),
    String(conversationId.value)
  );

  if (stomp?.connected && conversationId.value) {
    subscribeRoom(conversationId.value);
  }

  stomp.publish({
    destination: "/app/chat.send",
    body: JSON.stringify({
      conversationId: conversationId.value,
      senderType: "CLIENT",
      senderId: currentCustomerId.value,
      content: content.trim(),
    }),
  });
}

async function sendQuickOption(text) {
  const content = text?.trim();
  if (!content) return;

  try {
    if (!conversationId.value) {
      await createConversationAndSendFirstMessage(content);
    } else {
      publishMessage(content);
    }
    nextTick(scrollBottom);
  } catch (e) {
    console.error("Send quick option failed:", e);
  }
}

async function loadConversationAndHistory(cid) {
  const cached = localStorage.getItem(convKey(cid));

  if (!cached) {
    conversationId.value = null;
    messages.value = [];
    return;
  }

  conversationId.value = Number(cached);

  const hist = await axios.get(`${API}/api/chat/messages`, {
    params: { conversationId: conversationId.value },
  });

  messages.value = dedupeMessages(Array.isArray(hist.data) ? hist.data : []);
  nextTick(scrollBottom);
}

function connectWsIfNeeded() {
  if (stomp) return;

  const wsUrl = API.replace(/^http/, "ws") + "/ws";

  stomp = new Client({
    brokerURL: wsUrl,
    reconnectDelay: 2000,
    debug: () => {},
  });

  stomp.onConnect = () => {
    if (conversationId.value) subscribeRoom(conversationId.value);
  };

  stomp.activate();
}

function subscribeRoom(convId) {
  if (!stomp?.connected) return;

  try {
    sub?.unsubscribe();
  } catch (e) {}

  sub = stomp.subscribe(`/topic/conversations/${convId}`, (frame) => {
    const msg = JSON.parse(frame.body);

    const exists = messages.value.some((m) => isSameMessage(m, msg));
    if (!exists) {
      messages.value.push(msg);
      nextTick(scrollBottom);
    }
  });
}

async function reInitForCustomer(cid) {
  try {
    sub?.unsubscribe();
  } catch (e) {}
  sub = null;

  messages.value = [];
  conversationId.value = null;
  input.value = "";

  await loadConversationAndHistory(cid);

  if (stomp?.connected && conversationId.value) {
    subscribeRoom(conversationId.value);
  }
}

async function syncCustomerAndConversation() {
  const cid = resolveCustomerId();

  if (cid === currentCustomerId.value && conversationId.value) return;
  if (cid === currentCustomerId.value && !conversationId.value) return;

  currentCustomerId.value = cid;
  await reInitForCustomer(cid);
}

async function handleAuthChanged() {
  const oldGuestId = localStorage.getItem("guestId");
  const loggedInId = getLoggedInUserId();

  try {
    sub?.unsubscribe();
  } catch (e) {}
  sub = null;

  messages.value = [];
  conversationId.value = null;
  input.value = "";

  // Nếu từ guest -> login user, chuyển conversation cũ sang user mới
  if (loggedInId && oldGuestId) {
    const guestConvKey = convKey(oldGuestId);
    const userConvKey = convKey(loggedInId);

    const guestConversationId = localStorage.getItem(guestConvKey);
    const userConversationId = localStorage.getItem(userConvKey);

    if (guestConversationId && !userConversationId) {
      localStorage.setItem(userConvKey, guestConversationId);
    }

    // chỉ xóa sau khi đã migrate
    localStorage.removeItem(guestConvKey);
    localStorage.removeItem("guestId");
  }

  currentCustomerId.value = "";
  await syncCustomerAndConversation();
}
async function send() {
  const content = input.value.trim();
  if (!content) return;

  try {
    if (!conversationId.value) {
      await createConversationAndSendFirstMessage(content);
    } else {
      publishMessage(content);
    }

    input.value = "";
    nextTick(scrollBottom);
  } catch (e) {
    console.error("Send message failed:", e);
  }
}

async function handleFocusOrVisible() {
  if (document.visibilityState === "visible") {
    await syncCustomerAndConversation();
  }
}

onMounted(async () => {
  connectWsIfNeeded();
  await syncCustomerAndConversation();

  window.addEventListener("focus", handleFocusOrVisible);
  document.addEventListener("visibilitychange", handleFocusOrVisible);
  window.addEventListener("auth-changed", handleAuthChanged);
});

onBeforeUnmount(() => {
  try {
    sub?.unsubscribe();
  } catch (e) {}
  try {
    stomp?.deactivate();
  } catch (e) {}

  window.removeEventListener("focus", handleFocusOrVisible);
  document.removeEventListener("visibilitychange", handleFocusOrVisible);
  window.removeEventListener("auth-changed", handleAuthChanged);
});
</script>

<style scoped>
.chat-fab {
  position: fixed;
  right: 18px;
  bottom: 18px;
  width: 56px;
  height: 56px;
  border-radius: 999px;
  border: none;
  background: #0a85ed;
  color: #fff;
  font-size: 22px;
  cursor: pointer;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.22);
  z-index: 2147483647;
}

.chat-box {
  position: fixed;
  right: 18px;
  bottom: 86px;
  width: 340px;
  height: 460px;
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.25);
  z-index: 2147483646;
  display: flex;
  flex-direction: column;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.chat-header {
  padding: 12px 14px;
  background: #0a85ed;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-weight: 700;
}

.close-btn {
  border: none;
  background: transparent;
  color: #fff;
  font-size: 18px;
  cursor: pointer;
}

.chat-messages {
  flex: 1;
  padding: 12px;
  overflow: auto;
  background: #f6f8fb;
}

.msg-row {
  display: flex;
  margin: 8px 0;
}

.msg-row.me {
  justify-content: flex-end;
}

.msg-row.them {
  justify-content: flex-start;
}

.bubble {
  max-width: 78%;
  padding: 10px 10px 6px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.msg-row.me .bubble {
  background: #e9f3ff;
  border-color: rgba(10, 133, 237, 0.25);
}

.sender {
  font-size: 11px;
  font-weight: 700;
  margin-bottom: 4px;
  opacity: 0.7;
}

.text {
  white-space: pre-wrap;
  word-break: break-word;
}

.quick-options {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.quick-btn {
  border: 1px solid rgba(10, 133, 237, 0.18);
  background: #eef4ff;
  color: #0a85ed;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  cursor: pointer;
}

.quick-btn:hover {
  background: #e4efff;
}

.time {
  margin-top: 6px;
  font-size: 11px;
  opacity: 0.6;
  text-align: right;
}

.chat-input {
  display: flex;
  gap: 8px;
  padding: 10px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  background: #fff;
}

.chat-input input {
  flex: 1;
  padding: 10px 10px;
  border: 1px solid rgba(0, 0, 0, 0.14);
  border-radius: 10px;
  outline: none;
}

.chat-input button {
  padding: 10px 12px;
  border: none;
  border-radius: 10px;
  background: #0a85ed;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}

@media (max-width: 420px) {
  .chat-box {
    right: 10px;
    left: 10px;
    width: auto;
  }

  .chat-fab {
    right: 12px;
    bottom: 12px;
  }
}
</style>