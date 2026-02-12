<template>
  <div class="dashboard">
    <!-- Slideshow Vest (GRID 3 ảnh / 1 lần) -->
    <div class="card">
      <div class="panel-head">
        <div>
          <div class="panel-title">Dashboard</div>
        </div>

        <div class="dots" aria-label="slide indicators">
          <span
            v-for="p in pageCount"
            :key="p"
            class="dot"
            :class="{ active: pageIndex === p - 1 }"
          ></span>
        </div>
      </div>

      <div class="slider" @mouseenter="pause" @mouseleave="resume">
        <button class="nav left" type="button" @click="prev" aria-label="Prev">
          <i class="bi bi-chevron-left"></i>
        </button>

        <div class="frame">
          <div class="grid3">
            <div class="tile" v-for="s in visibleSlides" :key="s.key">
              <img class="slide-img" :src="s.src" :alt="s.alt" />
            </div>
          </div>
        </div>

        <button class="nav right" type="button" @click="next" aria-label="Next">
          <i class="bi bi-chevron-right"></i>
        </button>
      </div>
    </div>

    <!-- Trạng thái đơn hàng -->
    <div class="card">
      <div class="panel-head">
        <div>
          <div class="panel-title">Trạng thái đơn hàng</div>
        </div>
      </div>

      <div class="status-bar">
        <div class="status-chip wait">
          <span class="pill"><i class="bi bi-hourglass-split me-2"></i>Chờ xử lý</span>
          <div class="qty"><span class="num">12</span>  </div>
        </div>

        <div class="status-chip ship">
          <span class="pill"><i class="bi bi-truck me-2"></i>Đang giao</span>
          <div class="qty"><span class="num">8</span></div>
        </div>

        <div class="status-chip paid">
          <span class="pill"><i class="bi bi-check2-circle me-2"></i>Đã hoàn thành</span>
          <div class="qty"><span class="num">25</span></div>
        </div>

        <div class="status-chip cancel">
          <span class="pill"><i class="bi bi-x-circle me-2"></i>Đã hủy</span>
          <div class="qty"><span class="num">2</span></div>
        </div>
      </div>
    </div>

    <!-- Giới thiệu -->
    <div class="grid intro">
      <div class="card intro-card">
        <div class="intro-icon"><i class="bi bi-award"></i></div>
        <div class="intro-title">VestShop</div>
        <div class="intro-sub">
          Chuyên vest cưới/tiệc, đo may & chỉnh sửa theo form. Trải nghiệm mua hàng nhanh gọn cho nhân viên.
        </div>
      </div>

      <div class="card intro-card">
        <div class="intro-icon"><i class="bi bi-box-seam"></i></div>
        <div class="intro-title">Quy trình làm việc</div>
        <ul class="intro-list">
          <li>Tạo đơn → xác nhận thông tin</li>
          <li>Chuẩn bị hàng → bàn giao ship</li>
          <li>Hoàn thành → lưu lịch sử</li>
        </ul>
      </div>

      <div class="card intro-card">
        <div class="intro-icon"><i class="bi bi-shield-check"></i></div>
        <div class="intro-title">Chính sách</div>
        <ul class="intro-list">
          <li>Đổi size nhanh trong 7 ngày</li>
          <li>Hỗ trợ chỉnh sửa form</li>
          <li>CSKH & bảo hành đường may</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from "vue";

/** ✅ mỗi ảnh 1 biến khác nhau */
import vest1 from "@/images/ao-vest-den-1.jpg";
import vest2 from "@/images/ao-vest-den-2.jpg";
import vest3 from "@/images/ao-vest-den-3.jpg";
import vest4 from "@/images/ao-vest-den-4.jpg";
import vest5 from "@/images/ao-vest-den-5.jpg";
/** ⚠️ nếu bạn không có ao-vest-den-6.jpg thì đổi lại đúng tên file đang có */
import vest6 from "@/images/ao-vest-den-6.jpg";

const slides = [
  { src: vest1, alt: "Vest 1" },
  { src: vest2, alt: "Vest 2" },
  { src: vest3, alt: "Vest 3" },
  { src: vest4, alt: "Vest 4" },
  { src: vest5, alt: "Vest 5" },
  { src: vest6, alt: "Vest 6" },
];

const perPage = 3;
const pageIndex = ref(0);

const pageCount = computed(() => Math.max(1, Math.ceil(slides.length / perPage)));

const visibleSlides = computed(() => {
  const start = pageIndex.value * perPage;
  const slice = slides.slice(start, start + perPage);

  // nếu page cuối thiếu ảnh, lặp từ đầu cho đủ 3
  const filled = [...slice];
  let i = 0;
  while (filled.length < perPage && slides.length > 0) {
    filled.push(slides[i % slides.length]);
    i++;
  }

  return filled.map((s, idx) => ({ ...s, key: `${start}-${idx}-${s.alt}` }));
});

function next() {
  pageIndex.value = (pageIndex.value + 1) % pageCount.value;
}
function prev() {
  pageIndex.value = (pageIndex.value - 1 + pageCount.value) % pageCount.value;
}

/** auto */
const intervalMs = 4000;
let timer = null;

function start() {
  stop();
  timer = setInterval(next, intervalMs);
}
function stop() {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
}
function pause() {
  stop();
}
function resume() {
  start();
}

onMounted(start);
onBeforeUnmount(stop);
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.dashboard {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 100%;
}

/* base card */
.card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.06);
  overflow: hidden;
}

/* panel head */
.panel-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}
.panel-title {
  font-weight: 800;
  color: #0f172a;
}

/* slideshow wrapper */
.slider {
  position: relative;
  width: 100%;
  overflow: hidden;
}

/* bỏ padding để full ngang */
.frame {
  width: 100%;
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  padding: 0;
}

/* grid 3 ảnh */
.grid3 {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  padding: 0;
}

/* chiều cao tile */
.tile {
  border-radius: 16px;
  overflow: hidden;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  height: 520px;
}

/* ảnh fill */
.slide-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  display: block;
}

/* nav overlay */
.nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  border-radius: 14px;
  border: 1px solid rgba(229, 231, 235, 0.9);
  background: rgba(255, 255, 255, 0.92);
  display: grid;
  place-items: center;
  z-index: 5;
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.18);
}
.nav.left {
  left: 12px;
}
.nav.right {
  right: 12px;
}
.nav:hover {
  background: #fff;
}

/* dots */
.dots {
  display: inline-flex;
  gap: 6px;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #e5e7eb;
}
.dot.active {
  background: #1d4ed8;
}

/* =========================
   ✅ TRẠNG THÁI: MÀU PHỦ CẢ Ô
   ========================= */
.status-bar {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.status-chip {
  border-radius: 18px;
  padding: 22px 22px; /* to hơn */
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  border: 1px solid transparent;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
}

/* ✅ pill chỉ là chữ/icon, không còn ô trắng */
.pill {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-weight: 900;
  font-size: 18px;
  padding: 0;
  border: 0;
  background: transparent;
  border-radius: 0;
  white-space: nowrap;
}
.pill i {
  font-size: 20px;
}

.qty {
  display: inline-flex;
  align-items: baseline;
  gap: 8px;
}
.num {
  font-weight: 950;
  font-size: 40px;
  letter-spacing: -0.6px;
  color: currentColor;
}
.txt {
  font-size: 15px;
  font-weight: 800;
  color: currentColor;
  opacity: 0.85;
}

/* màu phủ cả ô */
.status-chip.wait {
  background: #fff7ed;
  border-color: #fed7aa;
  color: #9a3412;
}
.status-chip.ship {
  background: #eff6ff;
  border-color: #bfdbfe;
  color: #1d4ed8;
}
.status-chip.paid {
  background: #ecfdf5;
  border-color: #bbf7d0;
  color: #047857;
}
.status-chip.cancel {
  background: #fef2f2;
  border-color: #fecaca;
  color: #b91c1c;
}

/* intro */
.grid.intro {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
.intro-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.intro-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  background: #f1f5f9;
  color: #0f172a;
  font-size: 18px;
}
.intro-title {
  font-weight: 900;
  color: #0f172a;
}
.intro-sub {
  color: #6b7280;
  font-size: 13px;
  line-height: 1.45;
}
.intro-list {
  margin: 0;
  padding-left: 18px;
  color: #475569;
  font-size: 13px;
  line-height: 1.5;
}
.intro-list li {
  margin: 4px 0;
}

/* responsive */
@media (max-width: 1100px) {
  .status-bar {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .grid.intro {
    grid-template-columns: 1fr;
  }
  .grid3 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .tile {
    height: 320px;
  }
}
@media (max-width: 720px) {
  .status-bar {
    grid-template-columns: 1fr;
  }
  .grid3 {
    grid-template-columns: 1fr;
  }
  .tile {
    height: 280px;
  }
}
</style>
