<template>
  <!-- Icon chat nổi -->
  <button class="chat-fab" @click="toggle" aria-label="Chat hỗ trợ">
  <i class="bi bi-chat-dots-fill"></i>
  <span v-if="unreadCount > 0" class="chat-badge">{{ unreadCount }}</span>
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

          <div class="text" :class="{ typing: m.localTyping }">
            {{ m.content }}
          </div>

          <div v-if="shouldShowProducts(m)" class="product-list">
            <div
              v-for="p in m.products"
              :key="p.sanPhamChiTietId"
              class="product-card clickable"
              @click="goToProduct(p)"
            >
              <img
                v-if="p.anh"
                :src="buildImageUrl(p.anh)"
                :alt="p.tenSanPham"
                class="product-image"
                @error="onImageError"
              />

              <div class="product-info">
                <div class="product-name">{{ p.tenSanPham }}</div>

                <div class="product-meta">
                  <span v-if="p.loaiSanPham">{{ p.loaiSanPham }}</span>
                  <span v-if="p.mauSac">• {{ p.mauSac }}</span>
                  <span v-if="p.kichCo">• Size {{ p.kichCo }}</span>
                </div>

                <div class="product-price">{{ formatPrice(p.donGia) }}</div>
              </div>
            </div>
          </div>

          <div v-if="showBotSupportPanel(m)" class="bot-support-panel">
           

            <button
              class="support-btn staff-btn"
              @click="sendQuickOption('Gặp nhân viên')"
            >
              <span class="support-icon">⌁</span>
              <span>Gặp nhân viên</span>
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
        placeholder="Nhập câu hỏi của bạn..."
      />
      <button @click="send" aria-label="Gửi">➤</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from "vue";
import axios from "axios";
import { Client } from "@stomp/stompjs";
import { useRouter } from "vue-router";

const router = useRouter();
const API = import.meta.env.VITE_API_BASE;

function getLoggedInUser() {
  const raw = localStorage.getItem("vest_user");
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

function getLoggedInUserId() {
  const u = getLoggedInUser();
  const id =
    u?.khachHangId ??
    u?.customerId ??
    u?.id ??
    u?.userId ??
    null;

  if (id == null) return null;

  const n = Number(id);
  return Number.isNaN(n) ? null : n;
}

function getGuestName() {
  return "Khách vãng lai";
}

function getSenderId() {
  const userId = getLoggedInUserId();
  return userId != null ? String(userId) : "GUEST";
}

function getIdentityKey() {
  const userId = getLoggedInUserId();
  return userId != null ? `user:${userId}` : "guest";
}

function convKey(identityKey) {
  return `conversationId:${identityKey}`;
}

function buildWelcomeMessage() {
  return {
    id: `local-bot-${Date.now()}`,
    conversationId: null,
    senderType: "BOT",
    senderId: "LOCAL_BOT",
    content: "Chào bạn, shop có thể hỗ trợ gì cho bạn?",
    createdAt: new Date().toISOString(),
    localOnly: true,
    products: [],
  };
}

function buildLocalClientMessage(content) {
  return {
    id: `local-client-${Date.now()}-${Math.random()}`,
    conversationId: conversationId.value,
    senderType: "CLIENT",
    senderId: getSenderId(),
    content: content.trim(),
    createdAt: new Date().toISOString(),
    localOnly: true,
    products: [],
  };
}

function buildTypingMessage() {
  return {
    id: `local-typing-${Date.now()}-${Math.random()}`,
    conversationId: conversationId.value,
    senderType: "BOT",
    senderId: "BOT_TYPING",
    content: "Bot đang trả lời...",
    createdAt: new Date().toISOString(),
    localTyping: true,
    products: [],
  };
}

const open = ref(false);
const conversationId = ref(null);
const currentIdentityKey = ref("");
const messages = ref([]);
const input = ref("");
const msgBox = ref(null);
const unreadCount = ref(0);

let stomp = null;
let sub = null;

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

function isStaffHandoffMessage(message) {
  if (!message || message.senderType !== "BOT") return false;

  const content = normalizeText(message.content);

  return (
    content.includes("gap nhan vien") ||
    content.includes("gap cskh") ||
    content.includes("ket noi") ||
    content.includes("tu van vien") ||
    content.includes("nhan vien tu van") ||
    content.includes("vui long cho trong giay lat") ||
    content.includes("bo phan cskh") ||
    content.includes("se ket noi") ||
    content.includes("ho tro bo phan")
  );
}

function shouldShowProducts(message) {
  return (
    !!message &&
    Array.isArray(message.products) &&
    message.products.length > 0 &&
    !isStaffHandoffMessage(message)
  );
}

function getLastBotMessageId() {
  for (let i = messages.value.length - 1; i >= 0; i--) {
    const m = messages.value[i];
    if (m?.senderType === "BOT" && !m.localTyping) {
      return m.id ?? `${m.senderType}-${m.createdAt}-${i}`;
    }
  }
  return null;
}

function showBotSupportPanel(message) {
  if (!message || message.senderType !== "BOT" || message.localTyping) {
    return false;
  }

  if (isStaffHandoffMessage(message)) {
    return false;
  }

  const currentId =
    message.id ??
    `${message.senderType}-${message.createdAt}-${messages.value.indexOf(message)}`;

  return currentId === getLastBotMessageId();
}

function scrollBottom() {
  if (!msgBox.value) return;
  msgBox.value.scrollTop = msgBox.value.scrollHeight;
}

function removeLocalTyping() {
  messages.value = messages.value.filter((m) => !m.localTyping);
}

function pushLocalClientMessage(content) {
  const localMsg = buildLocalClientMessage(content);
  messages.value.push(localMsg);
  nextTick(scrollBottom);
}

function ensureLocalWelcome() {
  if (conversationId.value) return;
  if (messages.value.length > 0) return;

  messages.value = [buildWelcomeMessage()];
  nextTick(scrollBottom);
}

async function toggle() {
  open.value = !open.value;

  if (open.value) {
    unreadCount.value = 0;

    await syncConversation();

    if (!conversationId.value) {
      ensureLocalWelcome();
    } else {
      nextTick(scrollBottom);
    }
  }
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

function formatPrice(value) {
  const n = Number(value);
  if (Number.isNaN(n)) return "";
  return n.toLocaleString("vi-VN") + " đ";
}

function buildImageUrl(path) {
  if (!path) return "";
  if (path.startsWith("http://") || path.startsWith("https://")) return path;
  return `${API}${path}`;
}

function onImageError(e) {
  e.target.style.display = "none";
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
    const normalized = {
      ...msg,
      products: Array.isArray(msg?.products) ? msg.products : [],
    };

    const exists = unique.some((m) => isSameMessage(m, normalized));
    if (!exists) {
      unique.push(normalized);
    }
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
      senderId: getSenderId(),
      content: content.trim(),
    }),
  });
}

async function createConversationAndSendFirstMessage(content) {
  if (!content?.trim()) return;

  const userId = getLoggedInUserId();
  const identityKey = getIdentityKey();

  const params =
    userId != null
      ? { customerId: userId }
      : { guestName: getGuestName() };

  const cv = await axios.post(`${API}/api/chat/conversation`, null, { params });

  conversationId.value = cv.data.id;
  localStorage.setItem(convKey(identityKey), String(conversationId.value));

  if (stomp?.connected && conversationId.value) {
    subscribeRoom(conversationId.value);
  }

  messages.value = messages.value.filter(
    (m) => !m.localOnly || m.senderType !== "BOT"
  );

  stomp.publish({
    destination: "/app/chat.send",
    body: JSON.stringify({
      conversationId: conversationId.value,
      senderType: "CLIENT",
      senderId: getSenderId(),
      content: content.trim(),
    }),
  });
}

function goToProduct(p) {
  if (!p?.sanPhamChiTietId) return;
  router.push(`/product/${p.sanPhamChiTietId}`);
}

async function sendQuickOption(text) {
  const content = text?.trim();
  if (!content) return;

  try {
    pushLocalClientMessage(content);

    removeLocalTyping();
    messages.value.push(buildTypingMessage());
    nextTick(scrollBottom);

    if (!conversationId.value) {
      await createConversationAndSendFirstMessage(content);
    } else {
      publishMessage(content);
    }
  } catch (e) {
    console.error("Send quick option failed:", e);
    removeLocalTyping();
  }
}

async function loadConversationAndHistory(identityKey) {
  const cached = localStorage.getItem(convKey(identityKey));

  if (!cached) {
    conversationId.value = null;
    messages.value = [];
    ensureLocalWelcome();
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
  } catch {}

  sub = stomp.subscribe(`/topic/conversations/${convId}`, (frame) => {
    const msg = JSON.parse(frame.body);
    const normalized = {
      ...msg,
      products: Array.isArray(msg?.products) ? msg.products : [],
    };

    removeLocalTyping();

    if (normalized.senderType === "CLIENT") {
      const idx = messages.value.findIndex(
        (m) =>
          m.localOnly &&
          m.senderType === "CLIENT" &&
          m.content === normalized.content
      );

      if (idx !== -1) {
        messages.value.splice(idx, 1);
      }
    }

    const exists = messages.value.some((m) => isSameMessage(m, normalized));
    if (!exists) {
      messages.value = messages.value.filter(
        (m) => !(m.localOnly && m.senderType === normalized.senderType)
      );
      messages.value.push(normalized);

      const isIncoming =
        normalized.senderType === "BOT" || normalized.senderType === "ADMIN";

      if (!open.value && isIncoming) {
        unreadCount.value += 1;
      }

      nextTick(scrollBottom);
    }
  });
}

async function reInitForIdentity(identityKey) {
  try {
    sub?.unsubscribe();
  } catch {}
  sub = null;

  messages.value = [];
  conversationId.value = null;
  input.value = "";

  await loadConversationAndHistory(identityKey);

  if (stomp?.connected && conversationId.value) {
    subscribeRoom(conversationId.value);
  }
}

async function syncConversation() {
  const identityKey = getIdentityKey();

  if (identityKey === currentIdentityKey.value && conversationId.value) return;
  if (identityKey === currentIdentityKey.value && !conversationId.value) return;

  currentIdentityKey.value = identityKey;
  await reInitForIdentity(identityKey);
}

async function handleAuthChanged() {
  try {
    sub?.unsubscribe();
  } catch {}
  sub = null;

  messages.value = [];
  conversationId.value = null;
  input.value = "";
  open.value = false;
  unreadCount.value = 0;

  currentIdentityKey.value = "";
  await syncConversation();
}

async function send() {
  const content = input.value.trim();
  if (!content) return;

  input.value = "";

  try {
    pushLocalClientMessage(content);

    removeLocalTyping();
    messages.value.push(buildTypingMessage());
    nextTick(scrollBottom);

    if (!conversationId.value) {
      await createConversationAndSendFirstMessage(content);
    } else {
      publishMessage(content);
    }
  } catch (e) {
    console.error("Send message failed:", e);
    removeLocalTyping();
  }
}

async function handleFocusOrVisible() {
  if (document.visibilityState === "visible") {
    await syncConversation();
  }
}

onMounted(async () => {
  connectWsIfNeeded();
  await syncConversation();

  window.addEventListener("focus", handleFocusOrVisible);
  document.addEventListener("visibilitychange", handleFocusOrVisible);
  window.addEventListener("auth-changed", handleAuthChanged);
});

onBeforeUnmount(() => {
  try {
    sub?.unsubscribe();
  } catch {}
  try {
    stomp?.deactivate();
  } catch {}

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

.chat-badge {
  position: absolute;
  top: -4px;
  right: -2px;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 11px;
  font-weight: 800;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.18);
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
  max-width: 82%;
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

.text.typing {
  font-style: italic;
  opacity: 0.7;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 10px;
}

.product-card {
  display: flex;
  gap: 10px;
  padding: 8px;
  border-radius: 10px;
  background: #f8fbff;
  border: 1px solid rgba(10, 133, 237, 0.12);
}

.clickable {
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.product-image {
  width: 68px;
  height: 68px;
  object-fit: cover;
  border-radius: 8px;
  background: #eee;
  flex-shrink: 0;
}

.product-info {
  min-width: 0;
  flex: 1;
}

.product-name {
  font-size: 13px;
  font-weight: 700;
  line-height: 1.35;
  margin-bottom: 4px;
}

.product-meta {
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
  word-break: break-word;
}

.product-price {
  font-size: 13px;
  font-weight: 700;
  color: #0a85ed;
}

.bot-support-panel {
  margin-top: 10px;
}

.support-btn {
  width: 100%;
  min-height: 40px;
  border-radius: 10px;
  background: #fff;
  padding: 0 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  margin-top: 8px;
}

.auto-btn {
  border: 1px solid #98dbc3;
  color: #51b897;
  background: #fbfffd;
}

.staff-btn {
  border: 1px solid #e6c36a;
  color: #9a7421;
  background: #fffdfa;
}

.support-icon {
  font-size: 14px;
  line-height: 1;
  flex-shrink: 0;
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
  height: 40px;
  padding: 0 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  outline: none;
  font-size: 14px;
  background: #f8f9fb;
}

.chat-input input::placeholder {
  color: #a0a7b1;
}

.chat-input button {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 12px;
  background: #0a85ed;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  flex-shrink: 0;
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