<template>
  <div class="contact-page">
    <section class="contact-hero">
      <div class="container">
        <div class="contact-hero__content">
          <div class="contact-hero__badge">THE BOY TEAM</div>
          <h1 class="contact-hero__title">LIÊN HỆ VỚI CHÚNG TÔI</h1>
          <p class="contact-hero__desc">
            VestShop luôn sẵn sàng hỗ trợ tư vấn sản phẩm, chọn size, theo dõi đơn hàng
            và tiếp nhận mọi góp ý từ bạn.
          </p>
        </div>
      </div>
    </section>

    <section class="container py-5">
      <div class="row g-4">
        <div class="col-12 col-lg-4">
          <div class="contact-info-card h-100">
            <div class="section-subtitle">Thông tin liên hệ</div>
            <h2 class="section-title">Kết nối với VestShop</h2>
            <p class="contact-info-text">
              Bạn có thể liên hệ trực tiếp qua hotline, email hoặc ghé cửa hàng để được tư vấn nhanh hơn.
            </p>

            <div class="contact-info-list">
              <div class="contact-info-item">
                <div class="contact-info-icon">
                  <i class="bi bi-telephone-fill"></i>
                </div>
                <div>
                  <div class="contact-info-label">Hotline</div>
                  <div class="contact-info-value">0123 456 789</div>
                </div>
              </div>

              <div class="contact-info-item">
                <div class="contact-info-icon">
                  <i class="bi bi-envelope-fill"></i>
                </div>
                <div>
                  <div class="contact-info-label">Email</div>
                  <div class="contact-info-value">support@vest.vn</div>
                </div>
              </div>

              <div class="contact-info-item">
                <div class="contact-info-icon">
                  <i class="bi bi-geo-alt-fill"></i>
                </div>
                <div>
                  <div class="contact-info-label">Địa chỉ</div>
                  <div class="contact-info-value">
                    22 Phúc Diễn, Bắc Từ Liêm, Hà Nội
                  </div>
                </div>
              </div>

              <div class="contact-info-item">
                <div class="contact-info-icon">
                  <i class="bi bi-clock-fill"></i>
                </div>
                <div>
                  <div class="contact-info-label">Giờ làm việc</div>
                  <div class="contact-info-value">
                    08:00 - 21:00, Thứ 2 - Chủ nhật
                  </div>
                </div>
              </div>
            </div>

            <div class="contact-social">
              <a href="#" class="social-btn" @click.prevent aria-label="Facebook">
                <i class="bi bi-facebook"></i>
              </a>
              <a href="#" class="social-btn" @click.prevent aria-label="Instagram">
                <i class="bi bi-instagram"></i>
              </a>
              <a href="#" class="social-btn" @click.prevent aria-label="TikTok">
                <i class="bi bi-tiktok"></i>
              </a>
              <a href="#" class="social-btn" @click.prevent aria-label="YouTube">
                <i class="bi bi-youtube"></i>
              </a>
            </div>
          </div>
        </div>

        <div class="col-12 col-lg-8">
          <div class="contact-form-card">
            <div class="section-subtitle">Gửi yêu cầu</div>
            <h2 class="section-title">Biểu mẫu liên hệ</h2>
            <p class="contact-form-text">
              Điền thông tin bên dưới, VestShop sẽ phản hồi sớm nhất có thể.
            </p>

            <div
              v-if="submitMessage"
              class="alert"
              :class="submitOk ? 'alert-success' : 'alert-danger'"
            >
              {{ submitMessage }}
            </div>

            <form class="row g-3" @submit.prevent="submitForm">
              <div class="col-12 col-md-6">
                <label class="form-label">Họ và tên</label>
                <input
                  v-model.trim="form.hoTen"
                  type="text"
                  class="form-control"
                  placeholder="Nhập họ và tên"
                />
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Số điện thoại</label>
                <input
                  v-model.trim="form.soDienThoai"
                  type="text"
                  class="form-control"
                  placeholder="Nhập số điện thoại"
                />
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Email</label>
                <input
                  v-model.trim="form.email"
                  type="email"
                  class="form-control"
                  placeholder="Nhập email"
                />
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Chủ đề</label>
                <select v-model="form.chuDe" class="form-select">
                  <option value="">-- Chọn chủ đề --</option>
                  <option value="Tư vấn sản phẩm">Tư vấn sản phẩm</option>
                  <option value="Hỗ trợ đơn hàng">Hỗ trợ đơn hàng</option>
                  <option value="Đổi trả / bảo hành">Đổi trả / bảo hành</option>
                  <option value="Phản hồi dịch vụ">Phản hồi dịch vụ</option>
                  <option value="Khác">Khác</option>
                </select>
              </div>

              <div class="col-12">
                <label class="form-label">Nội dung</label>
                <textarea
                  v-model.trim="form.noiDung"
                  class="form-control contact-textarea"
                  rows="6"
                  placeholder="Nhập nội dung bạn muốn gửi cho VestShop..."
                ></textarea>
              </div>

              <div class="col-12 d-flex flex-wrap gap-3 align-items-center">
                <button type="submit" class="contact-submit-btn">
                  Gửi liên hệ
                </button>

                <a class="contact-alt-btn" href="tel:0123456789">
                  Gọi ngay
                </a>
              </div>
            </form>
          </div>
        </div>
      </div>
    </section>

    <section class="container pb-5">
      <div class="row g-4">
        <div class="col-12 col-lg-7">
          <div class="contact-map-card">
            <div class="section-subtitle">Bản đồ</div>
            <h2 class="section-title">Địa chỉ cửa hàng</h2>

            <div class="contact-map-wrap">
              <iframe
                title="Bản đồ cửa hàng"
                src="https://www.google.com/maps?q=22%20Ph%C3%BAc%20Di%E1%BB%85n%2C%20B%E1%BA%AFc%20T%E1%BB%AB%20Li%C3%AAm%2C%20H%C3%A0%20N%E1%BB%99i&z=15&output=embed"
                width="100%"
                height="100%"
                style="border: 0"
                allowfullscreen=""
                loading="lazy"
                referrerpolicy="no-referrer-when-downgrade"
              ></iframe>
            </div>
          </div>
        </div>

        <div class="col-12 col-lg-5">
          <div class="contact-faq-card">
            <div class="section-subtitle">Hỗ trợ nhanh</div>
            <h2 class="section-title">Câu hỏi thường gặp</h2>

            <div class="faq-list">
              <div class="faq-item" v-for="item in faqs" :key="item.q">
                <div class="faq-q">{{ item.q }}</div>
                <div class="faq-a">{{ item.a }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';

const form = reactive({
  hoTen: '',
  soDienThoai: '',
  email: '',
  chuDe: '',
  noiDung: '',
});

const submitMessage = ref('');
const submitOk = ref(false);

const faqs = [
  {
    q: 'VestShop có hỗ trợ chọn size không?',
    a: 'Có. Bạn có thể để lại số đo hoặc liên hệ hotline để được tư vấn chọn size phù hợp.',
  },
  {
    q: 'Bao lâu thì shop phản hồi liên hệ?',
    a: 'Thông thường trong giờ làm việc, shop sẽ phản hồi trong vòng 15 - 60 phút.',
  },
  {
    q: 'Có thể liên hệ để kiểm tra đơn hàng không?',
    a: 'Có. Bạn chỉ cần cung cấp mã đơn hàng hoặc số điện thoại đặt hàng.',
  },
];

function validEmail(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(value || '').trim());
}

function submitForm() {
  submitMessage.value = '';

  if (!form.hoTen) {
    submitOk.value = false;
    submitMessage.value = 'Vui lòng nhập họ và tên.';
    return;
  }

  if (!form.soDienThoai) {
    submitOk.value = false;
    submitMessage.value = 'Vui lòng nhập số điện thoại.';
    return;
  }

  const digits = form.soDienThoai.replace(/[^\d]/g, '');
  if (digits.length < 9 || digits.length > 15) {
    submitOk.value = false;
    submitMessage.value = 'Số điện thoại không hợp lệ.';
    return;
  }

  if (!form.email) {
    submitOk.value = false;
    submitMessage.value = 'Vui lòng nhập email.';
    return;
  }

  if (!validEmail(form.email)) {
    submitOk.value = false;
    submitMessage.value = 'Email không hợp lệ.';
    return;
  }

  if (!form.chuDe) {
    submitOk.value = false;
    submitMessage.value = 'Vui lòng chọn chủ đề liên hệ.';
    return;
  }

  if (!form.noiDung) {
    submitOk.value = false;
    submitMessage.value = 'Vui lòng nhập nội dung liên hệ.';
    return;
  }

  submitOk.value = true;
  submitMessage.value = 'VestShop đã nhận thông tin của bạn. Chúng tôi sẽ phản hồi sớm nhất.';

  form.hoTen = '';
  form.soDienThoai = '';
  form.email = '';
  form.chuDe = '';
  form.noiDung = '';
}
</script>

<style scoped>
.contact-page {
  background: #f8fafc;
}

.contact-hero {
  background: linear-gradient(135deg, #000f51 0%, #102c8b 100%);
  color: #fff;
  padding: 72px 0;
}

.contact-hero__content {
  max-width: 720px;
}

.contact-hero__badge,
.section-subtitle {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  color: #dbe7ff;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.4px;
  margin-bottom: 14px;
}

.section-subtitle {
  background: #eef4ff;
  color: #0b2c86;
}

.contact-hero__title,
.section-title {
  font-size: 34px;
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 14px;
}

.section-title {
  color: #111827;
  font-size: 28px;
}

.contact-hero__desc,
.contact-form-text,
.contact-info-text {
  font-size: 15px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.9);
}

.contact-form-text,
.contact-info-text {
  color: #6b7280;
}

.contact-info-card,
.contact-form-card,
.contact-map-card,
.contact-faq-card {
  background: #fff;
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
  height: 100%;
}

.contact-info-list {
  display: grid;
  gap: 16px;
  margin-top: 24px;
}

.contact-info-item {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  padding: 16px;
  border-radius: 18px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
}

.contact-info-icon {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: #000f51;
  color: #fff;
  font-size: 18px;
  flex-shrink: 0;
}

.contact-info-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 4px;
}

.contact-info-value {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  line-height: 1.6;
}

.contact-social {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.social-btn {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  text-decoration: none;
  background: #eef4ff;
  color: #0b2c86;
  font-size: 18px;
  transition: all 0.2s ease;
}

.social-btn:hover {
  background: #0b2c86;
  color: #fff;
}

.form-label {
  font-weight: 600;
  color: #111827;
  margin-bottom: 8px;
}

.form-control,
.form-select {
  border-radius: 14px;
  border: 1px solid #dbe2ea;
  min-height: 48px;
  padding-left: 14px;
  padding-right: 14px;
  box-shadow: none;
}

.form-control:focus,
.form-select:focus {
  border-color: #0b2c86;
  box-shadow: 0 0 0 0.2rem rgba(11, 44, 134, 0.12);
}

.contact-textarea {
  min-height: 140px;
  padding-top: 12px;
}

.contact-submit-btn,
.contact-alt-btn {
  min-width: 160px;
  border: none;
  border-radius: 14px;
  padding: 12px 20px;
  font-weight: 700;
  text-decoration: none;
  text-align: center;
  transition: all 0.2s ease;
}

.contact-submit-btn {
  background: #000f51;
  color: #fff;
}

.contact-submit-btn:hover {
  background: #102c8b;
}

.contact-alt-btn {
  background: #eef4ff;
  color: #0b2c86;
}

.contact-alt-btn:hover {
  background: #dfeaff;
}

.contact-map-wrap {
  overflow: hidden;
  border-radius: 20px;
  height: 360px;
  border: 1px solid #e5e7eb;
  margin-top: 18px;
}

.faq-list {
  display: grid;
  gap: 14px;
  margin-top: 18px;
}

.faq-item {
  padding: 16px 18px;
  border-radius: 18px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
}

.faq-q {
  font-weight: 700;
  color: #111827;
  margin-bottom: 8px;
  line-height: 1.5;
}

.faq-a {
  color: #6b7280;
  line-height: 1.7;
  font-size: 14px;
}

@media (max-width: 991.98px) {
  .contact-hero {
    padding: 56px 0;
  }

  .contact-hero__title,
  .section-title {
    font-size: 28px;
  }
}

@media (max-width: 575.98px) {
  .contact-info-card,
  .contact-form-card,
  .contact-map-card,
  .contact-faq-card {
    padding: 20px;
    border-radius: 20px;
  }

  .contact-hero__title,
  .section-title {
    font-size: 24px;
  }
}
</style>