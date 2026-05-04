<template>
  <header class="topbar" @click.stop>
    <div class="left">
      <div class="title">{{ resolvedTitle }}</div>
      <div class="subtitle">{{ resolvedSubtitle }}</div>
    </div>

    <div class="right">
      <button
        v-if="showViewBadge"
        class="view-mode-pill"
        type="button"
        title="Nhấn để mở lại màn hình vào ca"
        @click="openGate"
      >
        Chế độ xem
      </button>

      <div class="dd-wrap">
        <button
          class="icon-btn"
          type="button"
          @click.stop="toggleNoti"
          aria-label="Thông báo"
        >
          <svg viewBox="0 0 24 24" class="icon" aria-hidden="true">
            <path
              d="M12 22a2.5 2.5 0 0 0 2.45-2h-4.9A2.5 2.5 0 0 0 12 22Zm7-6V11a7 7 0 1 0-14 0v5l-2 2v1h18v-1l-2-2Z"
              fill="currentColor"
            />
          </svg>
          <span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span>
        </button>

        <div v-if="showNoti" class="dropdown">
          <div class="dropdown-head">
            <div class="dropdown-title">Thông báo</div>
            <button
              class="link-btn"
              type="button"
              @click="markAllRead"
              :disabled="notificationsLocal.length === 0"
            >
              Đã đọc tất cả
            </button>
          </div>

          <div v-if="notificationsLocal.length === 0" class="empty">
            Không có thông báo.
          </div>

          <ul v-else class="list">
            <li
              v-for="n in notificationsLocal"
              :key="n.id"
              class="item"
              :class="{ unread: !n.read }"
              @click="openNoti(n)"
            >
              <div class="item-main">
                <div class="item-title">{{ n.title }}</div>
                <div class="item-time">{{ n.time }}</div>
              </div>

              <button
                v-if="!n.read"
                class="mini"
                type="button"
                title="Đánh dấu đã đọc"
                @click.stop="markRead(n.id)"
              >
                ✓
              </button>
            </li>
          </ul>
        </div>
      </div>

      <div class="dd-wrap">
        <button class="user-btn" type="button" @click.stop="toggleUser">
          <span class="avatar">
            <img
              v-if="avatarUrl"
              :src="avatarUrl"
              class="avatar-img"
              alt="avatar"
              @error="onAvatarError"
            />
            <span v-else>{{ initials }}</span>
          </span>

          <span class="user-name">{{ admin.name }}</span>

          <svg viewBox="0 0 24 24" class="chev" aria-hidden="true">
            <path
              d="M7 10l5 5 5-5"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            />
          </svg>
        </button>

        <div v-if="showUser" class="dropdown user-dd">
          <div class="user-card">
            <div class="user-card-top">
              <span class="user-card-avatar">
                <img
                  v-if="avatarUrl"
                  :src="avatarUrl"
                  class="user-card-avatar-img"
                  alt="avatar"
                  @error="onAvatarError"
                />
                <span v-else>{{ initials }}</span>
              </span>

              <div>
                <div class="u-name">{{ admin.name }}</div>
                <div class="u-mail">{{ roleLabel }}</div>
              </div>
            </div>
          </div>

          <div class="divider"></div>

          <button class="menu danger" type="button" @click="logout">
            Đăng xuất
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { useShiftStore } from "@/stores/shift";
import { useNotificationStore } from "@/stores/notification";
import http from "@/services/http";
import { resolveMediaUrl } from "@/utils/media";

const props = defineProps({
  title: { type: String, default: "" },
  subtitle: { type: String, default: "" },
});

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const shift = useShiftStore();
const notificationStore = useNotificationStore();

const showViewBadge = computed(() => auth.isStaff && shift.isLocked);

function openGate() {
  shift.bootstrap(true);
}

const role = computed(() => {
  const storeRole = auth?.role;
  if (storeRole) return String(storeRole).toUpperCase();

  const lsRole =
    localStorage.getItem("vest_role") || localStorage.getItem("role") || "";
  return String(lsRole).toUpperCase();
});

const roleLabel = computed(() => {
  const r = String(role.value || "").toUpperCase();
  if (r === "ADMIN") return "Quyền: Admin";
  if (r === "STAFF") return "Quyền: Staff";
  if (!r) return "Quyền: -";
  return `Quyền: ${r}`;
});

function cleanName(name) {
  let s = String(name || "").trim();
  s = s.replace(/^(admin|staff|nhân viên|nhan vien|nv)\s*[:\-]?\s*/i, "");
  s = s.replace(/^(ADMIN|STAFF)\s+/i, "");
  return s.trim();
}

function readUserFromLocal() {
  try {
    const raw = localStorage.getItem("vest_user");
    if (raw) {
      const u = JSON.parse(raw);
      const name = cleanName(
        u?.tenNhanVien ||
          u?.name ||
          u?.hoTen ||
          localStorage.getItem("vest_name") ||
          "",
      );

      return {
        id: u?.id ?? null,
        name: name || (role.value === "STAFF" ? "Staff" : "Admin"),
        email: u?.email || "",
        taiKhoan: u?.taiKhoan || "",
        avatar:
          u?.anhDaiDien ||
          u?.avatarUrl ||
          u?.avatar ||
          u?.mediaAvatarUrl ||
          localStorage.getItem("vest_avatar") ||
          "",
      };
    }
  } catch {}

  const name = cleanName(localStorage.getItem("vest_name") || "");
  return {
    id: null,
    name: name || (role.value === "STAFF" ? "Staff" : "Admin"),
    email: "",
    taiKhoan: "",
    avatar: localStorage.getItem("vest_avatar") || "",
  };
}

const admin = ref(readUserFromLocal());
const avatarError = ref(false);

watch(role, async () => {
  admin.value = readUserFromLocal();
  avatarError.value = false;
  await loadCurrentNhanVienAvatar();
});

function onStorage(e) {
  if (!e || !e.key) return;
  if (
    [
      "vest_user",
      "vest_name",
      "vest_avatar",
      "vest_role",
      "role",
      "token",
      "vest_token",
    ].includes(e.key)
  ) {
    admin.value = readUserFromLocal();
    avatarError.value = false;
    loadCurrentNhanVienAvatar();
  }
}

const resolvedTitle = computed(
  () => props.title || route.meta?.title || "Vest Shop",
);

const notificationsLocal = computed(() => notificationStore.items || []);
const unreadCount = computed(() => notificationStore.unreadCount || 0);

const showNoti = ref(false);
const showUser = ref(false);

const now = ref(new Date());
let timer;

onMounted(async () => {
  timer = setInterval(() => {
    now.value = new Date();
  }, 60_000);

  document.addEventListener("click", closeAll);
  window.addEventListener("storage", onStorage);

  admin.value = readUserFromLocal();
  await loadCurrentNhanVienAvatar();

  notificationStore.init();
});

onBeforeUnmount(() => {
  clearInterval(timer);
  document.removeEventListener("click", closeAll);
  window.removeEventListener("storage", onStorage);
});

const resolvedSubtitle = computed(() => {
  if (props.subtitle) return props.subtitle;
  if (route.meta?.subtitle) return route.meta.subtitle;

  const datePart = new Intl.DateTimeFormat("vi-VN", {
    weekday: "long",
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  }).format(now.value);

  const timePart = new Intl.DateTimeFormat("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  }).format(now.value);

  return `${datePart} • ${timePart}`;
});

const initials = computed(() => {
  const name = (admin.value.name || "").trim() || "AD";
  const parts = name.split(/\s+/).filter(Boolean);

  if (parts.length === 0) return "AD";
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();

  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
});

const avatarUrl = computed(() => {
  if (avatarError.value) return "";

  const raw = String(admin.value?.avatar || "").trim();
  if (!raw) return "";

  return resolveMediaUrl(raw);
});

function onAvatarError() {
  avatarError.value = true;
}

function unwrapObj(data) {
  if (!data) return null;
  if (data.result && typeof data.result === "object") return data.result;
  return data;
}

async function loadCurrentNhanVienAvatar() {
  const id = admin.value?.id;
  if (!id) return;

  try {
    const res = await http.get("/api/nhan-vien/" + id);
    const data = unwrapObj(res?.data);

    const avatar =
      data?.anhDaiDien ||
      data?.avatarUrl ||
      data?.avatar ||
      data?.mediaAvatarUrl ||
      "";

    const name = cleanName(data?.tenNhanVien || admin.value?.name || "");

    admin.value = {
      ...admin.value,
      name: name || admin.value?.name || "Admin",
      email: data?.email || admin.value?.email || "",
      taiKhoan: data?.taiKhoan || admin.value?.taiKhoan || "",
      avatar,
    };

    const raw = localStorage.getItem("vest_user");
    const oldUser = raw ? JSON.parse(raw) : {};

    localStorage.setItem(
      "vest_user",
      JSON.stringify({
        ...oldUser,
        id: data?.id ?? oldUser?.id ?? id,
        tenNhanVien: data?.tenNhanVien || oldUser?.tenNhanVien,
        email: data?.email || oldUser?.email,
        taiKhoan: data?.taiKhoan || oldUser?.taiKhoan,
        anhDaiDien: avatar,
        avatarUrl: avatar,
      }),
    );

    if (avatar) {
      localStorage.setItem("vest_avatar", avatar);
      avatarError.value = false;
    }
  } catch {}
}

function toggleNoti() {
  showNoti.value = !showNoti.value;
  if (showNoti.value) showUser.value = false;
}

function toggleUser() {
  showUser.value = !showUser.value;
  if (showUser.value) showNoti.value = false;
}

function closeAll() {
  showNoti.value = false;
  showUser.value = false;
}

function markRead(id) {
  notificationStore.markRead(id);
}

function markAllRead() {
  notificationStore.markAllRead();
}

function openNoti(n) {
  if (!n.read) notificationStore.markRead(n.id);
  closeAll();
  if (n.link) router.push(n.link);
}

function logout() {
  closeAll();
  notificationStore.disconnect?.();
  auth.logout?.();

  localStorage.removeItem("vest_user");
  localStorage.removeItem("vest_name");
  localStorage.removeItem("vest_avatar");
  localStorage.removeItem("vest_role");
  localStorage.removeItem("role");
  localStorage.removeItem("token");
  localStorage.removeItem("vest_token");

  router.replace("/login");
}
</script>

<style scoped>
.topbar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 64px;
  padding: 10px 24px;
  box-sizing: border-box;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
}

.left {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
  line-height: 1.25;
  margin-left: 12px;
}

.title {
  font-weight: 800;
  font-size: 16px;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.subtitle {
  margin-top: 4px;
  font-size: 12.5px;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.view-mode-pill {
  height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: #0ea5e9;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  border: none;
  cursor: pointer;
}

.view-mode-pill:hover {
  filter: brightness(0.95);
}

.dd-wrap {
  position: relative;
}

.icon-btn {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-btn:hover {
  background: #f9fafb;
}

.icon {
  width: 20px;
  height: 20px;
  color: #111827;
}

.badge {
  position: absolute;
  top: -6px;
  right: -6px;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #fff;
}

.user-btn {
  height: 40px;
  padding: 0 10px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-btn:hover {
  background: #f9fafb;
}

.avatar {
  width: 26px;
  height: 26px;
  border-radius: 999px;
  background: #111827;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 12px;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.user-name {
  font-weight: 700;
  font-size: 14px;
  color: #111827;
}

.chev {
  width: 18px;
  height: 18px;
  color: #6b7280;
}

.dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 10px);
  width: 320px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.dropdown-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-bottom: 1px solid #f3f4f6;
}

.dropdown-title {
  font-weight: 800;
  color: #111827;
}

.link-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  color: #2563eb;
  font-size: 13px;
}

.link-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.empty {
  padding: 12px;
  color: #6b7280;
  font-size: 13px;
}

.list {
  list-style: none;
  margin: 0;
  padding: 6px;
  max-height: 320px;
  overflow: auto;
}

.item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  padding: 10px;
  border-radius: 12px;
  cursor: pointer;
}

.item:hover {
  background: #f9fafb;
}

.item.unread {
  background: #eff6ff;
}

.item-title {
  font-weight: 700;
  color: #111827;
  font-size: 13px;
}

.item-time {
  margin-top: 2px;
  font-size: 12px;
  color: #6b7280;
}

.mini {
  width: 28px;
  height: 28px;
  border-radius: 10px;
  border: 1px solid #d1d5db;
  background: #fff;
  cursor: pointer;
}

.mini:hover {
  background: #f9fafb;
}

.user-dd {
  width: 260px;
}

.user-card {
  padding: 12px;
}

.user-card-top {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-card-avatar {
  width: 38px;
  height: 38px;
  border-radius: 999px;
  background: #111827;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 13px;
  overflow: hidden;
  flex-shrink: 0;
}

.user-card-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.u-name {
  font-weight: 900;
  color: #111827;
}

.u-mail {
  margin-top: 2px;
  font-size: 12.5px;
  color: #6b7280;
}

.divider {
  height: 1px;
  background: #f3f4f6;
}

.menu {
  display: block;
  padding: 10px 12px;
  text-decoration: none;
  color: #111827;
  background: #fff;
  border: none;
  width: 100%;
  text-align: left;
  cursor: pointer;
  font-weight: 600;
}

.menu:hover {
  background: #f9fafb;
}

.menu.danger {
  color: #dc2626;
}
</style>