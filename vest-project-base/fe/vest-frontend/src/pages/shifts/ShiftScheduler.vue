<template>
  <div class="container-fluid py-3">
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-calendar-check fs-4"></i>
        <h5 class="mb-0">Xếp Lịch Nhân Viên</h5>
      </div>
---ơp
      <div class="d-flex align-items-center gap-2">
        <!-- Toggle view -->
        <div class="btn-group btn-group-sm" role="group" aria-label="Chế độ hiển thị">
          <button
              type="button"
              class="btn"
              :class="viewMode === 'calendar' ? 'btn-primary text-white' : 'btn-outline-primary'"
              @click="setViewMode('calendar')"
          >
            <i class="bi bi-grid-3x3-gap me-1"></i> Lịch biểu
          </button>
          <button
              type="button"
              class="btn"
              :class="viewMode === 'list' ? 'btn-primary text-white' : 'btn-outline-primary'"
              @click="setViewMode('list')"
          >
            <i class="bi bi-list-ul me-1"></i> Danh sách
          </button>
        </div>

        <!-- Export chỉ ở dạng Danh sách -->
        <button
            v-if="viewMode === 'list' && !exportMode"
            class="btn btn-outline-primary btn-sm"
            type="button"
            @click="openExportMode"
        >
          <i class="bi bi-file-earmark-excel me-1"></i> Xuất Excel
        </button>

        <template v-else-if="viewMode === 'list' && exportMode">
          <button
              class="btn btn-success btn-sm"
              type="button"
              :disabled="selectedIds.length === 0 || exporting"
              @click="exportSelectedToExcel"
          >
            <i class="bi bi-download me-1"></i>
            {{ exporting ? "Đang xuất..." : `Tải xuống (${selectedIds.length})` }}
          </button>
          <button class="btn btn-outline-secondary btn-sm" @click="cancelExportMode">
            <i class="bi bi-x-lg me-1"></i> Hủy
          </button>
        </template>

        <!-- ✅ NEW: Bulk add -->
        <button class="btn btn-outline-primary btn-sm" @click="openBulkModal">
          <i class="bi bi-ui-checks-grid me-1"></i> Thêm hàng loạt
        </button>

        <button class="btn btn-outline-primary btn-sm" @click="openModal()">
          <i class="bi bi-plus-lg me-1"></i> Phân Ca Mới
        </button>
      </div>
    </div>

    <!-- ✅ BỘ LỌC: hiển thị cho CẢ Danh sách + Lịch biểu -->
    <div class="card shadow-sm mb-3 filter-card">
      <div
          class="filter-header d-flex align-items-center justify-content-between"
          data-bs-toggle="collapse"
          data-bs-target="#filterBody"
          role="button"
          aria-expanded="true"
          aria-controls="filterBody"
      >
        <div class="d-flex align-items-center gap-2">
          <span class="filter-icon">▼</span>
          <span class="filter-title">Bộ lọc tìm kiếm</span>
        </div>
        <small class="filter-hint">Nhấn để thu gọn/mở rộng</small>
      </div>

      <div id="filterBody" class="collapse show">
        <div class="card-body filter-body">
          <div class="row g-3">
            <div class="col-12 col-lg-3">
              <label class="form-label">Tìm nhân viên</label>
              <input
                  v-model.trim="filters.keyword"
                  type="text"
                  class="form-control"
                  placeholder="Tên hoặc Mã NV..."
                  @input="applyFilters"
              />
            </div>

            <div class="col-12 col-lg-3">
              <label class="form-label">Ca làm việc</label>

              <!-- ✅ CHANGED: searchable combo (filter shift) -->
              <Multiselect
                  v-model="shiftFilterSelected"
                  :options="shiftFilterOptions"
                  :searchable="true"
                  :allow-empty="false"
                  :show-labels="false"
                  :close-on-select="true"
                  :custom-label="shiftFilterLabel"
                  track-by="id"
                  placeholder="Tất cả ca"
              />
            </div>

            <!-- ✅ Chỉ dùng Từ/Đến ngày cho Danh sách (vì calendar theo tuần riêng) -->
            <div class="col-12 col-lg-3" v-if="viewMode === 'list'">
              <label class="form-label">Từ ngày</label>
              <div class="input-group date-picker-group">
                <input ref="fromPickerRef" type="text" class="form-control" placeholder="dd/mm/yyyy" />
                <button class="btn btn-outline-secondary" type="button" @click="openFromPicker" title="Chọn ngày">
                  <i class="bi bi-calendar3"></i>
                </button>
                <button class="btn btn-outline-secondary" type="button" @click="clearFromDate" title="Xóa">
                  <i class="bi bi-x-lg"></i>
                </button>
              </div>
            </div>

            <div class="col-12 col-lg-3" v-if="viewMode === 'list'">
              <label class="form-label">Đến ngày</label>
              <div class="input-group date-picker-group">
                <input ref="toPickerRef" type="text" class="form-control" placeholder="dd/mm/yyyy" />
                <button class="btn btn-outline-secondary" type="button" @click="openToPicker" title="Chọn ngày">
                  <i class="bi bi-calendar3"></i>
                </button>
                <button class="btn btn-outline-secondary" type="button" @click="clearToDate" title="Xóa">
                  <i class="bi bi-x-lg"></i>
                </button>
              </div>
            </div>

            <div class="col-12 d-flex justify-content-end">
              <button class="btn btn-light btn-sm" @click="resetFilters">
                <i class="bi bi-arrow-counterclockwise me-1"></i> Đặt lại
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Toolbar tuần (chỉ ở dạng Lịch biểu) -->
    <div v-if="viewMode === 'calendar'" class="card shadow-sm mb-3 calendar-toolbar">
      <div class="card-body py-2">
        <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
          <div class="d-flex align-items-center gap-2">
            <button class="btn btn-outline-secondary btn-sm" type="button" @click="prevWeek" title="Tuần trước">
              <i class="bi bi-chevron-left"></i>
            </button>

            <input
                ref="calendarPickerRef"
                type="text"
                class="form-control form-control-sm"
                style="width: 160px"
                placeholder="dd/mm/yyyy"
                readonly
            />

            <button class="btn btn-outline-secondary btn-sm" type="button" @click="nextWeek" title="Tuần sau">
              <i class="bi bi-chevron-right"></i>
            </button>

            <button class="btn btn-outline-primary btn-sm" type="button" @click="goToday">
              Hôm nay
            </button>
          </div>

          <div class="badge bg-light text-dark border week-range-badge">
            Tuần từ: <span class="fw-semibold">{{ formatDate(calendarWeekStart) }}</span> đến
            <span class="fw-semibold">{{ formatDate(calendarWeekEnd) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- DANH SÁCH -->
    <div v-if="viewMode === 'list'" class="card shadow-sm">
      <div class="card-body">
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-primary" role="status"></div>
          <div class="mt-2 text-muted">Đang tải dữ liệu...</div>
        </div>

        <div v-else class="table-wrap">
          <table class="custom-table">
            <colgroup>
              <col v-if="exportMode" style="width: 40px" />
              <col style="width: 50px" />
              <col style="width: 100px" />
              <col style="width: 80px" />
              <col style="width: 120px" />
              <col style="width: 200px" />
              <col style="width: 120px" />
              <col style="width: 130px" />
              <col style="width: 150px" />
              <col style="width: 100px" />
            </colgroup>
            <thead>
            <tr>
              <th v-if="exportMode" class="text-center">
                <input
                    type="checkbox"
                    :checked="allVisibleSelected"
                    @change="toggleSelectAllVisible($event.target.checked)"
                />
              </th>
              <th class="text-center">STT</th>
              <th>Ngày làm</th>
              <th>Thứ</th>
              <th>Mã NV</th>
              <th>Tên nhân viên</th>
              <th>Tên Ca</th>
              <th>Khung giờ</th>
              <th>Ghi chú</th>
              <th class="text-center pe-3">Hành động</th>
            </tr>
            </thead>

            <tbody>
            <tr v-if="pagedItems.length === 0">
              <td :colspan="exportMode ? 10 : 9" class="empty">Không có dữ liệu</td>
            </tr>

            <tr v-for="(item, index) in pagedItems" :key="item.id">
              <td v-if="exportMode" class="text-center">
                <input
                    type="checkbox"
                    :checked="isSelected(item.id)"
                    @change="toggleSelect(item, $event.target.checked)"
                />
              </td>

              <td class="text-center">{{ (page.page - 1) * page.size + index + 1 }}</td>

              <td>{{ formatDate(item.ngayLamViec) }}</td>
              <td>{{ getDayOfWeek(item.ngayLamViec) }}</td>

              <td><span class="badge badge-muted text-dark border">{{ item.maNhanVien }}</span></td>
              <td>{{ item.tenNhanVien }}</td>

              <td><span class="badge bg-primary border">{{ item.tenCa }}</span></td>

              <td>{{ formatTime(item.gioBatDau) }} - {{ formatTime(item.gioKetThuc) }}</td>

              <td><span class="ellipsis" :title="item.ghiChu">{{ item.ghiChu || "-" }}</span></td>

              <td class="text-end pe-3">
                <div class="d-flex justify-content-center gap-2">
                  <button class="btn btn-outline-warning btn-sm" @click="openModal(item)" title="Sửa">
                    <i class="bi bi-pencil-square"></i>
                  </button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <div class="d-flex align-items-center mt-3 flex-column flex-md-row gap-2" v-if="totalElements > 0">
          <div class="text-muted flex-grow-1">
            Hiển thị {{ pagedItems.length }} / tổng {{ totalElements }} bản ghi
          </div>

          <div class="d-flex align-items-center gap-2 justify-content-center flex-grow-1">
            <button class="btn btn-outline-secondary btn-sm" :disabled="page.page === 1" @click="setPage(page.page - 1)">
              <i class="bi bi-chevron-left"></i>
            </button>

            <div class="input-group input-group-sm" style="width: 110px">
              <span class="input-group-text">Trang</span>
              <input
                  type="number"
                  min="1"
                  :max="totalPages"
                  class="form-control"
                  v-model.number="pageInput"
                  @keyup.enter="jumpPage"
              />
            </div>

            <button class="btn btn-outline-secondary btn-sm" :disabled="page.page >= totalPages" @click="setPage(page.page + 1)">
              <i class="bi bi-chevron-right"></i>
            </button>
          </div>

          <div class="d-flex justify-content-md-end flex-grow-1">
            <select class="form-select form-select-sm" style="width: 180px" v-model.number="page.size">
              <option :value="10">10 bản ghi / trang</option>
              <option :value="20">20 bản ghi / trang</option>
              <option :value="50">50 bản ghi / trang</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- LỊCH BIỂU -->
    <div v-else class="card shadow-sm">
      <div class="card-body">
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-primary" role="status"></div>
          <div class="mt-2 text-muted">Đang tải dữ liệu...</div>
        </div>

        <div v-else class="calendar-wrap">
          <table class="calendar-table">
            <thead>
            <tr>
              <th class="shift-col">CA / NGÀY</th>
              <th
                  v-for="d in calendarDays"
                  :key="d.date"
                  class="day-head"
                  :class="{ today: d.isToday }"
              >
                <div class="fw-bold">{{ d.label }}</div>
                <div class="small opacity-75">{{ formatDate(d.date) }}</div>
              </th>
            </tr>
            </thead>

            <tbody>
            <tr v-if="sortedTemplates.length === 0">
              <td :colspan="8" class="empty">Chưa có ca làm việc (hoặc tất cả ca đang không hoạt động)</td>
            </tr>

            <tr v-for="ca in sortedTemplates" :key="ca.id">
              <td class="shift-col shift-info">
                <div class="fw-bold">{{ ca.tenCa }}</div>
                <div class="small text-muted">
                  {{ formatTime(ca.gioBatDau) }} - {{ formatTime(ca.gioKetThuc) }}
                </div>
              </td>

              <td
                  v-for="d in calendarDays"
                  :key="d.date"
                  class="calendar-cell"
                  :class="{
      today: d.isToday,
      'calendar-cell-locked': isShiftStartedForDate(ca.id, d.date),
      'calendar-cell-clickable': canAddToCell(ca.id, d.date)
    }"
                  @click="onCalendarCellClick(ca.id, d.date)"
              >
                <!-- Có nhân viên -->
                <div
                    v-if="getPrimaryAssignment(ca.id, d.date)"
                    class="emp-badge"
                    :class="{ 'emp-badge-disabled': isShiftStartedForDate(ca.id, d.date) }"
                    :title="isShiftStartedForDate(ca.id, d.date) ? 'Ca đã qua giờ bắt đầu' : 'Bấm để sửa'"
                    @click.stop="openAssignedFromCalendar(ca.id, d.date)"
                >
                  <img
                      v-if="resolveAvatarUrl(getPrimaryAssignment(ca.id, d.date))"
                      :src="resolveAvatarUrl(getPrimaryAssignment(ca.id, d.date))"
                      class="emp-badge-avatar"
                      alt="avatar"
                      @error="onEmpAvatarError($event, getPrimaryAssignment(ca.id, d.date))"
                  />
                  <div v-else class="emp-badge-initials">
                    {{ getInitials(getPrimaryAssignment(ca.id, d.date)?.tenNhanVien) }}
                  </div>

                  <div class="emp-badge-name">
                    {{ getPrimaryAssignment(ca.id, d.date)?.tenNhanVien }}
                  </div>
                  <div class="emp-badge-code">
                    {{ getPrimaryAssignment(ca.id, d.date)?.maNhanVien || "" }}
                  </div>
                </div>

                <!-- Ô trống còn hợp lệ -->
                <button
                    v-else-if="canAddToCell(ca.id, d.date)"
                    class="btn btn-outline-primary btn-sm add-btn add-btn-center"
                    type="button"
                    title="Thêm nhân viên vào ca"
                    @click.stop="openModalFromCalendar(ca.id, d.date)"
                >
                  <i class="bi bi-plus-lg"></i>
                </button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- CONFIRM -->
    <div v-if="showConfirm" class="modal-overlay confirm-overlay" @click.self="closeConfirm">
      <div class="modal-card">
        <h3 class="modal-title">Xác nhận</h3>
        <p class="modal-desc">{{ confirmText }}</p>
        <div class="modal-actions">
          <button class="btn btn-outline-secondary" :disabled="confirmLoading" @click="closeConfirm">Hủy</button>
          <button class="btn btn-danger" :disabled="confirmLoading" @click="confirmYes">
            {{ confirmLoading ? "Đang xử lý..." : "Đồng ý" }}
          </button>
        </div>
      </div>
    </div>

    <!-- MODAL -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card shadow-lg" style="width: 500px;">
        <h3 class="modal-title border-bottom pb-2 mb-3">
          {{ isEdit ? "Cập nhật Lịch Làm Việc" : "Phân Ca Làm Việc Mới" }}
        </h3>

        <form @submit.prevent="submitAssign">
          <div class="mb-3">
            <label class="form-label fw-bold small">Nhân viên <span class="text-danger">*</span></label>

            <!-- ✅ CHANGED: searchable combo (modal staff select) -->
            <Multiselect
                v-model="staffSelected"
                :options="selectableStaffList"
                :searchable="true"
                :allow-empty="true"
                :show-labels="false"
                :close-on-select="true"
                :custom-label="staffOptionLabel"
                track-by="id"
                placeholder="-- Chọn nhân viên --"
                :disabled="isEdit"
            />
            <div v-if="isEdit" class="form-text text-muted">Không thể thay đổi nhân viên khi chỉnh sửa.</div>
          </div>

          <div class="mb-3">
            <label class="form-label fw-bold small">Ca làm việc <span class="text-danger">*</span></label>
            <select
                v-model="form.idCaLamViec"
                class="form-select"
                required
                :disabled="lockShiftDate && !isEdit"
            >
              <option :value="null" disabled>-- Chọn ca mẫu --</option>
              <option
                  v-for="ca in activeTemplates"
                  :key="ca.id"
                  :value="ca.id"
                  :disabled="!isEdit && isShiftStartedForDate(ca, form.ngayLamViec)"
              >
                {{ ca.tenCa }} ({{ formatTime(ca.gioBatDau) }} - {{ formatTime(ca.gioKetThuc) }})
              </option>
            </select>
          </div>

          <div class="mb-3">
            <label class="form-label fw-bold small">Ngày làm việc <span class="text-danger">*</span></label>
            <input
                v-model="form.ngayLamViec"
                type="date"
                class="form-control"
                required
                :disabled="lockShiftDate && !isEdit"
                :min="!isEdit ? todayIso : null"
            />
          </div>

          <div class="mb-3">
            <label class="form-label fw-bold small">Ghi chú</label>
            <input v-model="form.ghiChu" type="text" class="form-control" placeholder="VD: Tăng ca, trực thay..." />
          </div>

          <div class="modal-actions mt-4">
            <button
                v-if="isEdit"
                type="button"
                class="btn btn-danger me-auto"
                @click="requestDeleteFromModal"
            >
              <i class="bi bi-trash me-1"></i> Xóa
            </button>

            <button type="button" class="btn btn-outline-secondary" @click="closeModal">Hủy</button>
            <button type="submit" class="btn btn-primary fw-bold px-4">
              {{ isEdit ? "Cập nhật" : "Lưu lịch" }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- ✅ BULK MODAL -->
    <div v-if="showBulkModal" class="modal-overlay" @click.self="closeBulkModal">
      <div class="modal-card shadow-lg bulk-card">
        <div class="bulk-header">
          <div>
            <div class="bulk-title">Phân ca hàng loạt</div>
            <div class="bulk-subtitle">Chọn nhân viên, ca làm việc rồi tick ngày để thêm nhanh.</div>
          </div>
          <button class="btn btn-sm btn-light" type="button" @click="closeBulkModal" title="Đóng">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>

        <form @submit.prevent="submitBulkAssign">
          <div class="bulk-body">
            <div class="row g-3">
              <!-- Left -->
              <div class="col-12 col-lg-4">
                <div class="bulk-box">
                  <label class="form-label fw-bold small mb-2">Chế độ</label>
                  <div class="btn-group btn-group-sm w-100" role="group">
                    <button
                        type="button"
                        class="btn"
                        :class="bulk.mode === 'week' ? 'btn-primary text-white' : 'btn-outline-primary'"
                        @click="setBulkMode('week')"
                    >
                      Theo tuần
                    </button>
                    <button
                        type="button"
                        class="btn"
                        :class="bulk.mode === 'month' ? 'btn-primary text-white' : 'btn-outline-primary'"
                        @click="setBulkMode('month')"
                    >
                      Theo tháng
                    </button>
                  </div>

                  <div class="mt-3" v-if="bulk.mode === 'week'">
                    <label class="form-label fw-bold small">Chọn ngày mốc (tự lấy T2–CN)</label>
                    <input v-model="bulk.anchorDate" type="date" class="form-control" :min="todayIso" />
                    <div class="form-text text-muted">Tuần từ {{ bulkWeekStart }} đến {{ bulkWeekEnd }}</div>
                  </div>

                  <div class="mt-3" v-else>
                    <label class="form-label fw-bold small">Chọn tháng</label>
                    <input v-model="bulk.month" type="month" class="form-control" :min="currentMonthIso" />
<!--                    <div class="form-text text-muted">Tháng {{ bulk.month }}</div>-->
                  </div>
                </div>

                <div class="bulk-box mt-3">
                  <label class="form-label fw-bold small">Nhân viên <span class="text-danger">*</span></label>
                  <Multiselect
                      v-model="bulkStaffSelected"
                      :options="selectableStaffList"
                      :searchable="true"
                      :allow-empty="true"
                      :show-labels="false"
                      :close-on-select="true"
                      :custom-label="staffOptionLabel"
                      track-by="id"
                      placeholder="-- Chọn nhân viên --"
                  />
                </div>

                <div class="bulk-box mt-3">
                  <label class="form-label fw-bold small">Ca làm việc <span class="text-danger">*</span></label>
                  <Multiselect
                      v-model="bulkShiftSelected"
                      :options="activeTemplates"
                      :searchable="true"
                      :allow-empty="true"
                      :show-labels="false"
                      :close-on-select="true"
                      :custom-label="shiftOptionLabel"
                      track-by="id"
                      placeholder="-- Chọn ca mẫu --"
                  />
                </div>

                <div class="bulk-box mt-3">
                  <label class="form-label fw-bold small">Ghi chú</label>
                  <input v-model="bulk.ghiChu" type="text" class="form-control" placeholder="VD: Tăng ca, trực thay..." />
<!--                  <div class="form-text text-muted">Ghi chú sẽ áp dụng cho tất cả ngày đã chọn.</div>-->
                </div>

                <div class="bulk-box mt-3">
                  <div class="d-flex align-items-center justify-content-between">
                    <div class="fw-bold small">Đang chọn</div>
                    <div class="badge bg-light text-dark border">{{ bulkSelectedDates.length }} ngày</div>
                  </div>

                  <div class="d-flex gap-2 mt-2 flex-wrap">
                    <button type="button" class="btn btn-outline-secondary btn-sm" @click="bulkSelectAll">
                      Chọn tất cả
                    </button>
                    <button type="button" class="btn btn-outline-secondary btn-sm" @click="bulkSelectWeekdays">
                      Chỉ T2–T6
                    </button>
                    <button type="button" class="btn btn-outline-secondary btn-sm" @click="bulkClearSelection">
                      Bỏ chọn
                    </button>
                  </div>

                  <div class="mt-2 small text-muted" v-if="bulkLoading">
                    <span class="spinner-border spinner-border-sm me-2"></span>Đang tải lịch để kiểm tra trùng...
                  </div>
                </div>
              </div>

              <!-- Right: Calendar -->
              <div class="col-12 col-lg-8">
                <div class="bulk-calendar">
                  <div class="bulk-cal-head">
                    <div class="fw-bold">
                      {{ bulk.mode === 'week' ? 'Chọn ngày theo tuần' : 'Chọn ngày theo tháng' }}
                    </div>
<!--                    <div class="text-muted small">-->
<!--                      Tick các ô ngày muốn thêm-->
<!--                    </div>-->
                  </div>

                  <!-- Week grid -->
                  <div v-if="bulk.mode === 'week'" class="bulk-week-grid">
                    <div
                        v-for="d in bulkWeekDays"
                        :key="d.date"
                        class="bulk-day"
                        :class="{ disabled: d.disabled }"
                    >
                      <label class="bulk-day-inner">
                        <input
                            type="checkbox"
                            class="form-check-input me-2"
                            :disabled="d.disabled"
                            :checked="isBulkSelected(d.date)"
                            @change="toggleBulkDate(d.date, $event.target.checked)"
                        />
                        <div>
                          <div class="fw-semibold">{{ d.label }}</div>
                          <div class="small text-muted">{{ formatDate(d.date) }}</div>

                        </div>
                      </label>
                    </div>
                  </div>

                  <!-- Month table -->
                  <div v-else class="bulk-month-wrap">
                    <table class="bulk-month-table">
                      <thead>
                      <tr>
                        <th v-for="h in bulkMonthHeaders" :key="h">{{ h }}</th>
                      </tr>
                      </thead>
                      <tbody>
                      <tr v-for="(w, wi) in bulkMonthMatrix" :key="wi">
                        <td
                            v-for="cell in w"
                            :key="cell.key"
                            class="bulk-month-cell"
                            :class="{ 'out-month': !cell.inMonth, disabled: cell.disabled }"
                        >
                          <div class="bulk-month-top">
                            <div class="bulk-daynum">{{ cell.dayNum }}</div>
                            <input
                                type="checkbox"
                                class="form-check-input bulk-month-checkbox"
                                :disabled="cell.disabled || !cell.inMonth"
                                :checked="cell.inMonth && isBulkSelected(cell.date)"
                                @change="toggleBulkDate(cell.date, $event.target.checked)"
                            />
                          </div>
<!--                          <div v-if="cell.reason" class="bulk-reason">{{ cell.reason }}</div>-->
                        </td>
                      </tr>
                      </tbody>
                    </table>
                  </div>

                  <!-- Report -->

                </div>
              </div>
            </div>
          </div>

          <!-- Footer -->
          <div class="bulk-footer">
            <button type="button" class="btn btn-outline-secondary" @click="closeBulkModal" :disabled="bulkSubmitting">
              Hủy
            </button>
            <button
                type="submit"
                class="btn btn-primary fw-bold"
                :disabled="bulkSubmitting || bulkSelectedDates.length === 0 || !bulk.idNhanVien || !bulk.idCaLamViec"
            >
              <span v-if="bulkSubmitting" class="spinner-border spinner-border-sm me-2"></span>
              Thêm mới ({{ bulkSelectedDates.length }})
            </button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch, nextTick } from "vue";import shiftApi from "@/services/shiftApi";
import * as nhanVienApi from "@/services/nhanVienApi";
import { useToast } from "@/composables/useToast";
import * as XLSX from "xlsx";
import flatpickr from "flatpickr";
import "flatpickr/dist/flatpickr.css";
import { Vietnamese } from "flatpickr/dist/l10n/vn.js";
import { resolveMediaUrl } from "@/utils/media";

/** ✅ Searchable combo lib already in your project */
import Multiselect from "vue-multiselect";
import "vue-multiselect/dist/vue-multiselect.css";

const toast = useToast();

// ====================== VIEW MODE ======================
const viewMode = ref("calendar"); // ✅ default calendar
function setViewMode(mode) {
  if (viewMode.value === mode) return;
  viewMode.value = mode;

  if (viewMode.value !== "list") {
    exportMode.value = false;
    selectedIds.value = [];
  }

  if (mode === "calendar") loadCalendar();
  else loadSchedule();
}

// ====================== STATE ======================
const loading = ref(false);
const rawSchedules = ref([]);
const staffList = ref([]);
const templates = ref([]);
const showModal = ref(false);
const isEdit = ref(false);
const editingId = ref(null);
const lockShiftDate = ref(false);

const selectableStaffList = computed(() => {
  return (staffList.value || []).filter((nv) => {
    const active = nv.trangThai === true || nv.trangThai === 1;
    const isAdmin = nv.quyenHanId === 1 || String(nv.tenQuyenHan || "").toLowerCase().includes("admin");
    return active && !isAdmin;
  });
});

// ====================== FILTERS (SHARED: list + calendar) ======================
const nowRef = ref(new Date());
let nowTimer = null;

const toDateStr = (d) =>
    new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().split("T")[0];

const today = computed(() => nowRef.value);
const todayIso = computed(() => toDateStr(nowRef.value));
const currentMonthIso = computed(() => todayIso.value.slice(0, 7));

const firstDay = computed(() => {
  const d = today.value;
  return new Date(d.getFullYear(), d.getMonth(), 1);
});
const lastDay = computed(() => {
  const d = today.value;
  return new Date(d.getFullYear(), d.getMonth() + 1, 0);
});

const filters = reactive({
  keyword: "",
  shiftId: null,
  from: toDateStr(firstDay.value), // list only
  to: toDateStr(lastDay.value), // list only
});

const fromPickerRef = ref(null);
const toPickerRef = ref(null);
let fromPickerInstance = null;
let toPickerInstance = null;

const isoToLocalDate = (iso) => {
  if (!iso) return null;
  const [y, m, d] = iso.split("-").map(Number);
  return new Date(y, m - 1, d);
};

function startOfDay(d) {
  const x = new Date(d);
  x.setHours(0, 0, 0, 0);
  return x;
}

function combineDateAndTime(dateIso, timeStr) {
  if (!dateIso || !timeStr) return null;

  const [y, m, d] = String(dateIso).split("-").map(Number);
  const [hh = 0, mm = 0, ss = 0] = String(timeStr).split(":").map(Number);

  return new Date(y, m - 1, d, hh, mm, ss, 0);
}

function findTemplateById(id) {
  return (templates.value || []).find((x) => x.id === id) || null;
}

function isPastDate(dateIso) {
  if (!dateIso) return false;
  const picked = startOfDay(isoToLocalDate(dateIso));
  const todayStart = startOfDay(nowRef.value);
  return picked < todayStart;
}

function isShiftStartedForDate(caOrId, dateIso) {
  if (!dateIso || !caOrId) return false;

  if (isPastDate(dateIso)) return true;
  if (dateIso !== todayIso.value) return false;

  const ca = typeof caOrId === "object" ? caOrId : findTemplateById(caOrId);
  if (!ca?.gioBatDau) return false;

  const shiftStart = combineDateAndTime(dateIso, ca.gioBatDau);
  if (!shiftStart) return false;

  return shiftStart < nowRef.value;
}

function openFromPicker() {
  fromPickerInstance?.open();
}
function openToPicker() {
  toPickerInstance?.open();
}

function clearFromDate() {
  filters.from = "";
  fromPickerInstance?.clear();
  page.page = 1;
  loadSchedule();
}
function clearToDate() {
  filters.to = "";
  toPickerInstance?.clear();
  page.page = 1;
  loadSchedule();
}
function applyFilters() {
  page.page = 1;
  // Không cần gọi API lại vì lọc client-side.
  // Calendar cũng tự cập nhật vì dùng filteredSchedules + primaryMap.
}

// ====================== ✅ SEARCHABLE COMBO (FILTER SHIFT) ======================
const shiftFilterOptions = computed(() => {
  return [
    { id: null, tenCa: "Tất cả ca" },
    ...activeTemplates.value
  ];
});
const shiftFilterLabel = (opt) => (opt?.id == null ? "Tất cả ca" : String(opt?.tenCa || ""));
const shiftFilterSelected = computed({
  get() {
    const found = shiftFilterOptions.value.find((x) => x.id === filters.shiftId);

    if (!found) {
      filters.shiftId = null;
      return shiftFilterOptions.value[0] || null;
    }

    return found;
  },
  set(opt) {
    filters.shiftId = opt?.id ?? null;
    applyFilters();
  },
});

// ====================== PAGINATION (LIST) ======================
const page = reactive({ page: 1, size: 10 });
const pageInput = ref(1);
function setPage(p) {
  if (p >= 1 && p <= totalPages.value) page.page = p;
}
function jumpPage() {
  const p = Number(pageInput.value || 1);
  setPage(Math.min(Math.max(p, 1), totalPages.value || 1));
}

// ====================== FORM (MODAL) ======================
const form = reactive({
  idNhanVien: null,
  idCaLamViec: null,
  ngayLamViec: toDateStr(new Date()),
  ghiChu: "",
});

// ====================== ✅ SEARCHABLE COMBO (MODAL STAFF) ======================
const staffOptionLabel = (nv) => (nv ? `${nv.tenNhanVien} (${nv.maNhanVien})` : "");
const staffSelected = computed({
  get() {
    return selectableStaffList.value.find((x) => x.id === form.idNhanVien) || null;
  },
  set(nv) {
    form.idNhanVien = nv?.id ?? null;
  },
});

// ====================== CONFIRM ======================
const showConfirm = ref(false);
const confirmText = ref("");
const confirmLoading = ref(false);
let pendingDeleteId = null;
const deleteFromModal = ref(false);

// ====================== EXPORT ======================
const exportMode = ref(false);
const exporting = ref(false);
const selectedIds = ref([]);

// ====================== CALENDAR (WEEK) ======================
const calendarAnchor = ref(toDateStr(new Date()));
const calendarPickerRef = ref(null);
let calendarPickerInstance = null;

function startOfWeekISO(iso) {
  const d = isoToLocalDate(iso);
  if (!d) return toDateStr(new Date());
  const day = d.getDay(); // 0..6 (CN..T7)
  const diff = day === 0 ? 6 : day - 1; // Monday start
  d.setDate(d.getDate() - diff);
  return toDateStr(d);
}
function addDaysISO(iso, days) {
  const d = isoToLocalDate(iso);
  d.setDate(d.getDate() + days);
  return toDateStr(d);
}

const calendarWeekStart = computed(() => startOfWeekISO(calendarAnchor.value));
const calendarWeekEnd = computed(() => addDaysISO(calendarWeekStart.value, 6));

const calendarDays = computed(() => {
  const labels = ["CN", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"];
  const start = calendarWeekStart.value;
  return Array.from({ length: 7 }, (_, i) => {
    const date = addDaysISO(start, i);
    const dayIdx = new Date(date).getDay();
    return {
      date,
      label: labels[dayIdx],
      isToday: date === todayIso.value,
    };
  });
});

function prevWeek() {
  calendarAnchor.value = addDaysISO(calendarWeekStart.value, -7);
}
function nextWeek() {
  calendarAnchor.value = addDaysISO(calendarWeekStart.value, 7);
}
function goToday() {
  calendarAnchor.value = todayIso.value;
}
function onCalendarAnchorChange() {}

watch(calendarAnchor, () => {
  calendarPickerInstance?.setDate(isoToLocalDate(calendarAnchor.value), false);

  if (viewMode.value === "calendar") {
    loadCalendar();
  }
});

// ====================== LOAD DATA ======================
function initListPickersIfNeeded() {
  if (!fromPickerInstance && fromPickerRef.value) {
    fromPickerInstance = flatpickr(fromPickerRef.value, {
      locale: Vietnamese,
      allowInput: true,
      dateFormat: "d/m/Y",
      defaultDate: isoToLocalDate(filters.from),
      appendTo: document.body,
      positionElement: fromPickerRef.value,
      position: "below left",
      onOpen: () => {
        setTimeout(() => {
          fromPickerInstance?._positionCalendar?.();
        }, 0);
      },
      onChange: (selectedDates) => {
        filters.from = selectedDates?.[0] ? toDateStr(selectedDates[0]) : "";
        page.page = 1;
        loadSchedule();
      },
    });
  }

  if (!toPickerInstance && toPickerRef.value) {
    toPickerInstance = flatpickr(toPickerRef.value, {
      locale: Vietnamese,
      allowInput: true,
      dateFormat: "d/m/Y",
      defaultDate: isoToLocalDate(filters.to),
      appendTo: document.body,
      positionElement: toPickerRef.value,
      position: "below left",
      onOpen: () => {
        setTimeout(() => {
          toPickerInstance?._positionCalendar?.();
        }, 0);
      },
      onChange: (selectedDates) => {
        filters.to = selectedDates?.[0] ? toDateStr(selectedDates[0]) : "";
        page.page = 1;
        loadSchedule();
      },
    });
  }
}

function initCalendarPickerIfNeeded() {
  if (!calendarPickerInstance && calendarPickerRef.value) {
    calendarPickerInstance = flatpickr(calendarPickerRef.value, {
      locale: Vietnamese,
      allowInput: false,
      dateFormat: "d/m/Y",
      defaultDate: isoToLocalDate(calendarAnchor.value),
      onChange: (selectedDates) => {
        if (selectedDates?.[0]) {
          calendarAnchor.value = toDateStr(selectedDates[0]);
        }
      },
    });
  }
}

watch(viewMode, async (m) => {
  await nextTick();

  if (m === "list") {
    initListPickersIfNeeded();
  }

  if (m === "calendar") {
    initCalendarPickerIfNeeded();
    calendarPickerInstance?.setDate(isoToLocalDate(calendarAnchor.value), false);
  }
});

onMounted(async () => {
  nowTimer = setInterval(() => {
    nowRef.value = new Date();
  }, 30000);

  // default calendar => do not init pickers now
  await nextTick();

  if (viewMode.value === "list") {
    initListPickersIfNeeded();
  }

  if (viewMode.value === "calendar") {
    initCalendarPickerIfNeeded();
    loadCalendar();
  } else {
    loadSchedule();
  }

  loadResources();
});

onBeforeUnmount(() => {
  if (nowTimer) clearInterval(nowTimer);
});

async function loadSchedule() {
  loading.value = true;
  try {
    const res = await shiftApi.getSchedules(filters.from, filters.to);
    const data = res.data;
    if (Array.isArray(data)) rawSchedules.value = data;
    else if (data && Array.isArray(data.result)) rawSchedules.value = data.result;
    else if (data && Array.isArray(data.data)) rawSchedules.value = data.data;
    else rawSchedules.value = [];
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

async function loadCalendar() {
  loading.value = true;
  try {
    const res = await shiftApi.getSchedules(calendarWeekStart.value, calendarWeekEnd.value);
    const data = res.data;
    if (Array.isArray(data)) rawSchedules.value = data;
    else if (data && Array.isArray(data.result)) rawSchedules.value = data.result;
    else if (data && Array.isArray(data.data)) rawSchedules.value = data.data;
    else rawSchedules.value = [];
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

function reloadCurrentView() {
  if (viewMode.value === "calendar") return loadCalendar();
  return loadSchedule();
}

async function loadResources() {
  try {
    const resNV = await nhanVienApi.listNhanVien(0, 100);
    let rawNV = [];
    if (resNV.content && Array.isArray(resNV.content)) rawNV = resNV.content;
    else if (resNV.data && Array.isArray(resNV.data)) rawNV = resNV.data;
    else if (Array.isArray(resNV)) rawNV = resNV;

    staffList.value = rawNV.filter((nv) => nv.trangThai === 1 || nv.trangThai === true);

    const resCa = await shiftApi.getTemplates();
    const dataCa = resCa.data || resCa;
    if (Array.isArray(dataCa)) templates.value = dataCa;
    else if (dataCa.data) templates.value = dataCa.data;
    else templates.value = [];
  } catch (e) {
    console.error("Resource error", e);
  }
}

// ====================== ✅ FILTERED SCHEDULES (SHARED) ======================
const filteredSchedules = computed(() => {
  let items = rawSchedules.value || [];

  if (filters.keyword) {
    const kw = filters.keyword.toLowerCase();
    items = items.filter(
        (i) => (i.tenNhanVien || "").toLowerCase().includes(kw) || (i.maNhanVien || "").toLowerCase().includes(kw)
    );
  }

  if (filters.shiftId) items = items.filter((i) => i.idCaLamViec === filters.shiftId);

  return items;
});

// ====================== COMPUTED (LIST) ======================
const totalElements = computed(() => filteredSchedules.value.length);
const totalPages = computed(() => Math.ceil(totalElements.value / page.size) || 1);
const pagedItems = computed(() =>
    filteredSchedules.value.slice((page.page - 1) * page.size, page.page * page.size)
);

// ====================== ✅ COMPUTED (CALENDAR) ======================
function isTemplateActive(ca) {
  const v = ca?.trangThai ?? ca?.hoatDong ?? ca?.isActive ?? ca?.active ?? ca?.status;
  return v === true || v === 1 || v === "1" || String(v || "").toLowerCase() === "active";
}

const sortedTemplates = computed(() => {
  const list = Array.isArray(templates.value) ? [...templates.value] : [];
  const activeOnly = list.filter(isTemplateActive);

  return activeOnly.sort((a, b) => {
    const ta = (a.gioBatDau || "").toString();
    const tb = (b.gioBatDau || "").toString();
    if (ta !== tb) return ta.localeCompare(tb);
    return (a.tenCa || "").toString().localeCompare((b.tenCa || "").toString(), "vi");
  });
});
const activeTemplates = computed(() => {
  const list = Array.isArray(templates.value) ? [...templates.value] : [];
  return list
      .filter(isTemplateActive)
      .sort((a, b) => {
        const ta = (a.gioBatDau || "").toString();
        const tb = (b.gioBatDau || "").toString();
        if (ta !== tb) return ta.localeCompare(tb);
        return (a.tenCa || "").toString().localeCompare((b.tenCa || "").toString(), "vi");
      });
});

const staffById = computed(() => {
  const map = {};
  (staffList.value || []).forEach((s) => (map[s.id] = s));
  return map;
});

const primaryMap = computed(() => {
  const map = {};
  (filteredSchedules.value || []).forEach((item) => {
    const key = `${item.idCaLamViec}_${item.ngayLamViec}`;
    if (!map[key]) map[key] = item;
  });
  return map;
});

function getPrimaryAssignment(caId, dateIso) {
  return primaryMap.value[`${caId}_${dateIso}`] || null;
}

function canAddToCell(caId, dateIso) {
  if (getPrimaryAssignment(caId, dateIso)) return false;
  if (isShiftStartedForDate(caId, dateIso)) return false;
  return true;
}

function openModalFromCalendar(caId, dateIso) {
  if (isShiftStartedForDate(caId, dateIso)) return;

  isEdit.value = false;
  editingId.value = null;
  lockShiftDate.value = true;

  form.idNhanVien = null;
  form.idCaLamViec = caId;
  form.ngayLamViec = dateIso;
  form.ghiChu = "";

  showModal.value = true;
}

function onCalendarCellClick(caId, dateIso) {
  if (!canAddToCell(caId, dateIso)) return;
  openModalFromCalendar(caId, dateIso);
}

function openAssignedFromCalendar(caId, dateIso) {
  const item = getPrimaryAssignment(caId, dateIso);
  if (!item) return;

  if (isShiftStartedForDate(caId, dateIso)) return;

  openModal(item);
}

// function openAssignedFromCalendar(caId, dateIso) {
//   const item = getPrimaryAssignment(caId, dateIso);
//   if (!item) return;
//
//   if (isShiftStartedForDate(caId, dateIso)) {
//     toast.error("Không thể chỉnh sửa ca đã qua giờ bắt đầu.");
//     return;
//   }
//
//   openModal(item);
// }

// ====================== ACTIONS ======================
function resetFilters() {
  filters.keyword = "";
  filters.shiftId = null;

  filters.from = toDateStr(firstDay.value);
  filters.to = toDateStr(lastDay.value);

  fromPickerInstance?.setDate(isoToLocalDate(filters.from), false);
  toPickerInstance?.setDate(isoToLocalDate(filters.to), false);

  page.page = 1;

  if (viewMode.value === "list") loadSchedule();
}

function openModal(item = null) {
  lockShiftDate.value = false;
  isEdit.value = !!item;

  if (item) {
    editingId.value = item.id;
    form.idNhanVien = item.idNhanVien;
    form.idCaLamViec = item.idCaLamViec;
    form.ngayLamViec = item.ngayLamViec;
    form.ghiChu = item.ghiChu;
  } else {
    editingId.value = null;
    form.idNhanVien = null;
    form.idCaLamViec = null;
    form.ngayLamViec = toDateStr(new Date());
    form.ghiChu = "";
  }

  showModal.value = true;
}

function closeModal() {
  showModal.value = false;
  lockShiftDate.value = false;
}

async function submitAssign() {
  try {
    if (!isEdit.value) {
      if (isPastDate(form.ngayLamViec)) {
        toast.error("Không thể chọn ngày trong quá khứ.");
        return;
      }

      if (isShiftStartedForDate(form.idCaLamViec, form.ngayLamViec)) {
        toast.error("Không thể tạo lịch cho ca đã qua giờ bắt đầu.");
        return;
      }
    }

    if (isEdit.value) {
      await shiftApi.updateSchedule(editingId.value, form);
      toast.success("Cập nhật lịch thành công!");
    } else {
      await shiftApi.assignSchedule(form);
      toast.success("Xếp lịch thành công!");
    }
    closeModal();
    await reloadCurrentView();
  } catch (e) {
    toast.error(e.response?.data?.message || "Lỗi xếp lịch");
  }
}

function requestDeleteFromModal() {
  if (!editingId.value) return;
  deleteFromModal.value = true;
  confirmDelete(editingId.value);
}

function confirmDelete(id) {
  confirmText.value = "Bạn có chắc chắn muốn xóa lịch này?";
  pendingDeleteId = id;
  showConfirm.value = true;
}

function closeConfirm() {
  showConfirm.value = false;
  pendingDeleteId = null;
  deleteFromModal.value = false;
}

async function confirmYes() {
  if (!pendingDeleteId) return;
  confirmLoading.value = true;
  try {
    await shiftApi.deleteSchedule(pendingDeleteId);
    toast.success("Đã xóa lịch");
    if (deleteFromModal.value) closeModal();
    await reloadCurrentView();
  } catch (e) {
    toast.error("Không thể xóa");
  } finally {
    confirmLoading.value = false;
    showConfirm.value = false;
    pendingDeleteId = null;
    deleteFromModal.value = false;
  }
}

// ====================== EXPORT ======================
function openExportMode() {
  exportMode.value = true;
}
function cancelExportMode() {
  exportMode.value = false;
  selectedIds.value = [];
}
function isSelected(id) {
  return selectedIds.value.includes(id);
}
function toggleSelect(item, checked) {
  if (checked && !selectedIds.value.includes(item.id)) selectedIds.value.push(item.id);
  else if (!checked) selectedIds.value = selectedIds.value.filter((x) => x !== item.id);
}
const allVisibleSelected = computed(() => pagedItems.value.length > 0 && pagedItems.value.every((i) => selectedIds.value.includes(i.id)));
function toggleSelectAllVisible(checked) {
  pagedItems.value.forEach((item) => toggleSelect(item, checked));
}

async function exportSelectedToExcel() {
  if (selectedIds.value.length === 0) return;
  exporting.value = true;
  try {
    const dataToExport = rawSchedules.value
        .filter((item) => selectedIds.value.includes(item.id))
        .map((item, index) => ({
          STT: index + 1,
          "Ngày làm": formatDate(item.ngayLamViec),
          Thứ: getDayOfWeek(item.ngayLamViec),
          "Mã NV": item.maNhanVien,
          "Tên NV": item.tenNhanVien,
          Ca: item.tenCa,
          Giờ: `${formatTime(item.gioBatDau)} - ${formatTime(item.gioKetThuc)}`,
          "Ghi chú": item.ghiChu || "",
        }));

    const ws = XLSX.utils.json_to_sheet(dataToExport);
    ws["!cols"] = [{ wch: 6 }, { wch: 12 }, { wch: 10 }, { wch: 10 }, { wch: 22 }, { wch: 14 }, { wch: 16 }, { wch: 22 }];
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "LichNhanVien");
    XLSX.writeFile(wb, `LichNhanVien_${Date.now()}.xlsx`);
    toast.success("Xuất Excel thành công");
    cancelExportMode();
  } catch (e) {
    toast.error("Lỗi xuất Excel");
  } finally {
    exporting.value = false;
  }
}

// ====================== HELPERS ======================
const formatTime = (t) => (t ? t.substring(0, 5) : "-");
const formatDate = (d) => {
  if (!d) return "";
  const [y, m, day] = d.split("-");
  return `${day}/${m}/${y}`;
};
const getDayOfWeek = (d) => ["Chủ nhật", "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy"][new Date(d).getDay()];

// Avatar helpers
function resolveAvatarUrl(item) {
  return resolveMediaUrl(item?.anhDaiDien || staffById.value?.[item?.idNhanVien]?.anhDaiDien || item?.avatarUrl || "");
}
function onEmpAvatarError(e, item) {
  if (item) item.anhDaiDien = "";
  if (e?.target) e.target.src = "";
}
function getInitials(name) {
  const s = String(name || "").trim();
  if (!s) return "NV";
  const parts = s.split(/\s+/).filter(Boolean);
  const a = parts[0]?.[0] || "N";
  const b = parts[parts.length - 1]?.[0] || "V";
  return (a + b).toUpperCase();
}

// ====================== ✅ BULK ADD (NO BE CHANGE) ======================
const showBulkModal = ref(false);
const bulkLoading = ref(false);
const bulkSubmitting = ref(false);
const bulkReport = ref(null);
const bulkSelectedDates = ref([]);
const bulkRangeSchedules = ref([]);

const bulk = reactive({
  mode: "week", // 'week' | 'month'
  anchorDate: toDateStr(new Date()),
  month: toDateStr(new Date()).slice(0, 7), // YYYY-MM
  idNhanVien: null,
  idCaLamViec: null,
  ghiChu: "",
});

function openBulkModal() {
  bulkReport.value = null;
  bulkSelectedDates.value = [];

  // default follow current calendar anchor for convenience
  bulk.mode = "week";
  bulk.anchorDate =
      calendarAnchor.value && !isPastDate(calendarAnchor.value)
          ? calendarAnchor.value
          : todayIso.value;

  bulk.month = (bulk.anchorDate || todayIso.value).slice(0, 7);

  bulk.idNhanVien = null;
  bulk.idCaLamViec = null;
  bulk.ghiChu = "";

  showBulkModal.value = true;
  loadBulkRangeSchedules();
}

function closeBulkModal() {
  showBulkModal.value = false;
  bulkLoading.value = false;
  bulkSubmitting.value = false;
  bulkReport.value = null;
  bulkSelectedDates.value = [];
}

function setBulkMode(m) {
  if (bulk.mode === m) return;
  bulk.mode = m;
  bulkSelectedDates.value = [];
  bulkReport.value = null;
  loadBulkRangeSchedules();
}

const bulkWeekStart = computed(() => startOfWeekISO(bulk.anchorDate));
const bulkWeekEnd = computed(() => addDaysISO(bulkWeekStart.value, 6));

function monthStartISO(ym) {
  if (!ym) return toDateStr(new Date()).slice(0, 7) + "-01";
  return `${ym}-01`;
}
function monthEndISO(ym) {
  const [y, m] = String(ym || "").split("-").map(Number);
  if (!y || !m) return toDateStr(new Date());
  const d = new Date(y, m, 0); // last day
  return toDateStr(d);
}

const bulkRangeStart = computed(() => (bulk.mode === "week" ? bulkWeekStart.value : monthStartISO(bulk.month)));
const bulkRangeEnd = computed(() => (bulk.mode === "week" ? bulkWeekEnd.value : monthEndISO(bulk.month)));

watch(
    () => [bulk.anchorDate, bulk.month],
    () => {
      if (!showBulkModal.value) return;
      bulkSelectedDates.value = [];
      bulkReport.value = null;
      loadBulkRangeSchedules();
    }
);

const dayLabels = ["CN", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"];
function getDowLabel(dateIso) {
  return dayLabels[new Date(dateIso).getDay()];
}

const bulkWeekDays = computed(() => {
  const start = bulkWeekStart.value;
  return Array.from({ length: 7 }, (_, i) => {
    const date = addDaysISO(start, i);
    const { disabled, reason } = getBulkCellState(date, true);
    return { date, label: getDowLabel(date), disabled, reason };
  });
});

const bulkMonthHeaders = ["Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "CN"];
const bulkMonthMatrix = computed(() => {
  const ym = bulk.month;
  const first = monthStartISO(ym);
  const gridStart = startOfWeekISO(first); // monday start
  const cells = Array.from({ length: 42 }, (_, i) => {
    const date = addDaysISO(gridStart, i);
    const inMonth = date.slice(0, 7) === ym;
    const dayNum = Number(date.split("-")[2]);
    const { disabled, reason } = getBulkCellState(date, inMonth);
    return {
      key: date,
      date,
      inMonth,
      dayNum,
      disabled,
      reason,
    };
  });

  // split into 6 weeks
  const weeks = [];
  for (let w = 0; w < 6; w++) {
    // reorder to Monday..Sunday columns: gridStart is Monday already
    const row = cells.slice(w * 7, w * 7 + 7);
    weeks.push(row);
  }
  return weeks;
});

const bulkShiftMap = computed(() => {
  const map = {};
  (bulkRangeSchedules.value || []).forEach((item) => {
    const active = item?.trangThai === 1 || item?.trangThai === true;
    if (!active) return;
    const key = `${item.idCaLamViec}_${item.ngayLamViec}`;
    if (!map[key]) map[key] = item;
  });
  return map;
});

function getBulkCellState(dateIso, inMonth) {
  if (!inMonth) return { disabled: true, reason: "" };

  if (isPastDate(dateIso)) {
    return { disabled: true, reason: "Ngày đã qua" };
  }

  if (bulk.idCaLamViec && isShiftStartedForDate(bulk.idCaLamViec, dateIso)) {
    return { disabled: true, reason: "Ca này đã qua giờ bắt đầu" };
  }

  if (!bulk.idCaLamViec) return { disabled: false, reason: "" };

  const hit = bulkShiftMap.value[`${bulk.idCaLamViec}_${dateIso}`];
  if (hit) {
    return {
      disabled: true,
      reason: `Đã có: ${hit.tenNhanVien || ""} ${hit.maNhanVien ? `(${hit.maNhanVien})` : ""}`.trim(),
    };
  }

  return { disabled: false, reason: "" };
}

async function loadBulkRangeSchedules() {
  if (!showBulkModal.value) return;
  bulkLoading.value = true;
  try {
    const res = await shiftApi.getSchedules(bulkRangeStart.value, bulkRangeEnd.value);
    const data = res.data;
    if (Array.isArray(data)) bulkRangeSchedules.value = data;
    else if (data && Array.isArray(data.result)) bulkRangeSchedules.value = data.result;
    else if (data && Array.isArray(data.data)) bulkRangeSchedules.value = data.data;
    else bulkRangeSchedules.value = [];
  } catch (e) {
    console.error(e);
    bulkRangeSchedules.value = [];
  } finally {
    bulkLoading.value = false;
  }
}

function isBulkSelected(dateIso) {
  return bulkSelectedDates.value.includes(dateIso);
}
function toggleBulkDate(dateIso, checked) {
  if (!dateIso) return;

  // if disabled -> ignore
  const st = getBulkCellState(dateIso, true);
  if (st.disabled) return;

  if (checked) {
    if (!bulkSelectedDates.value.includes(dateIso)) bulkSelectedDates.value.push(dateIso);
  } else {
    bulkSelectedDates.value = bulkSelectedDates.value.filter((x) => x !== dateIso);
  }
}

function bulkClearSelection() {
  bulkSelectedDates.value = [];
}

function bulkSelectAll() {
  const dates = [];

  if (bulk.mode === "week") {
    bulkWeekDays.value.forEach((d) => {
      if (!d.disabled) dates.push(d.date);
    });
  } else {
    bulkMonthMatrix.value.flat().forEach((c) => {
      if (c.inMonth && !c.disabled) dates.push(c.date);
    });
  }

  bulkSelectedDates.value = Array.from(new Set(dates));
}

function bulkSelectWeekdays() {
  const dates = [];
  const isWeekday = (iso) => {
    const dow = new Date(iso).getDay(); // 0 CN, 1 T2..5 T6
    return dow >= 1 && dow <= 5;
  };

  if (bulk.mode === "week") {
    bulkWeekDays.value.forEach((d) => {
      if (!d.disabled && isWeekday(d.date)) dates.push(d.date);
    });
  } else {
    bulkMonthMatrix.value.flat().forEach((c) => {
      if (c.inMonth && !c.disabled && isWeekday(c.date)) dates.push(c.date);
    });
  }

  bulkSelectedDates.value = Array.from(new Set(dates));
}

const shiftOptionLabel = (ca) => {
  if (!ca) return "";
  const t = `${formatTime(ca.gioBatDau)} - ${formatTime(ca.gioKetThuc)}`;
  return `${ca.tenCa} (${t})`;
};

const bulkStaffSelected = computed({
  get() {
    return selectableStaffList.value.find((x) => x.id === bulk.idNhanVien) || null;
  },
  set(nv) {
    bulk.idNhanVien = nv?.id ?? null;
  },
});
const bulkShiftSelected = computed({
  get() {
    return activeTemplates.value.find((x) => x.id === bulk.idCaLamViec) || null;
  },
  set(ca) {
    bulk.idCaLamViec = ca?.id ?? null;

    bulkSelectedDates.value = bulkSelectedDates.value.filter((d) => !getBulkCellState(d, true).disabled);
  },
});

async function submitBulkAssign() {
  bulkReport.value = null;

  if (!bulk.idNhanVien || !bulk.idCaLamViec || bulkSelectedDates.value.length === 0) {
    toast.error("Vui lòng chọn nhân viên, ca làm việc và ít nhất 1 ngày.");
    return;
  }

  bulkSubmitting.value = true;

  let created = 0;
  let skipped = 0;
  let failed = 0;
  const failedDetails = [];

  try {
    const dates = [...bulkSelectedDates.value].sort();

    for (const dateIso of dates) {
      const st = getBulkCellState(dateIso, true);
      if (st.disabled) {
        skipped++;
        continue;
      }

      try {
        await shiftApi.assignSchedule({
          idNhanVien: bulk.idNhanVien,
          idCaLamViec: bulk.idCaLamViec,
          ngayLamViec: dateIso,
          ghiChu: bulk.ghiChu,
        });
        created++;
      } catch (e) {
        failed++;
        failedDetails.push({
          date: dateIso,
          message: e?.response?.data?.message || "Không thể thêm (có thể trùng lịch/giờ)",
        });
      }
    }

    bulkReport.value = { created, skipped, failed, failedDetails };

    if (created > 0) {
      toast.success(`Đã thêm ${created} lịch.`);
    } else {
      toast.error("Không thêm được lịch nào.");
    }

    await reloadCurrentView();

    if (created > 0) {
      closeBulkModal();
      return;
    }

    await loadBulkRangeSchedules();
  } finally {
    bulkSubmitting.value = false;
  }
}

// ====================== END BULK ADD ======================
</script>

<style scoped>
.week-range-badge {
  font-size: 14px;
  padding: 8px 14px;
  border-radius: 10px;
  min-height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.week-range-badge .fw-semibold {
  font-size: 15px;
}
/* Reuse Filter & Table Styles from ShiftTemplateList */
.filter-card {
  border-radius: 14px;
  overflow: visible;
  border: 1px solid #e9ecef;
}
.filter-header {
  background: #1f2a44;
  color: #fff;
  padding: 12px 16px;
  cursor: pointer;
  user-select: none;
}
.filter-title {
  font-weight: 700;
}
.filter-body {
  background: #f8fafc;
}
.filter-card .form-control,
.filter-card .form-select {
  border-radius: 10px;
}
.filter-hint {
  opacity: 0.75;
}

.filter-icon {
  display: inline-flex;
  width: 26px;
  height: 26px;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 12px;
  transition: transform 0.2s ease;
}
.filter-header[aria-expanded="false"] .filter-icon {
  transform: rotate(-90deg);
}

.table-wrap {
  border: 1px solid #dee2e6;
  border-radius: 12px;
  overflow: auto;
  background: #fff;
  text-align: center;
}
.custom-table {
  width: 100%;
  min-width: 1000px;
  border-collapse: separate;
  border-spacing: 0;
}
.custom-table th,
.custom-table td {
  padding: 12px;
  border-bottom: 1px solid #e9ecef;
  vertical-align: middle;
  white-space: nowrap;
}
.custom-table thead th {
  background: #1f2a44;
  color: #fff;
  font-weight: 700;
}
.ellipsis {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}
.empty {
  text-align: center;
  padding: 20px;
  color: #6c757d;
}
.badge-muted {
  background: #f8f9fa;
  color: #212529;
  font-weight: 500;
}
.badge {
  font-weight: 500;
}

/* ===================== CALENDAR FIX ===================== */
.calendar-toolbar .form-control {
  border-radius: 10px;
}

.calendar-wrap {
  border: 1px solid #dee2e6;
  border-radius: 12px;
  overflow: auto;
  background: #fff;
}

.calendar-table {
  width: 100%;
  min-width: 1380px;
  table-layout: fixed;
  border-collapse: separate;
  border-spacing: 0;
}

.calendar-table th,
.calendar-table td {
  border-bottom: 1px solid #e9ecef;
  border-right: 1px solid #e9ecef;
  padding: 10px;
  vertical-align: top;
  overflow: hidden;
}

.calendar-table td {
  display: table-cell !important;
  text-align: left;
}

.calendar-table thead th {
  background: #1f2a44 !important;
  color: #fff !important;
  font-weight: 700;
  position: sticky;
  top: 0;
  z-index: 3;
  text-align: center;
}

.calendar-table thead th.shift-col {
  text-align: left;
}

.shift-col {
  position: sticky;
  left: 0;
  z-index: 2;
  background: #f8fafc;
  width: 260px;
  min-width: 260px;
  max-width: 260px;
}

.calendar-table thead th.shift-col {
  left: 0;
  z-index: 4;
}

.shift-info {
  border-right: 1px solid #e9ecef;
}

.calendar-table tbody tr {
  height: 140px;
}

.emp-badge-code {
  margin-top: 2px;
  font-size: 12px;
  font-weight: 600;
  color: #6c757d;
  width: 100%;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.calendar-cell {
  position: relative;
  min-height: 140px;
  background: #fff;
}

.calendar-cell.today {
  background: #f5f9ff;
}

.day-head.today {
  box-shadow: inset 0 -2px 0 rgba(255, 255, 255, 0.35);
}

/* ===== Ô trống: nút + ở GIỮA ô ===== */
.add-btn-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.calendar-cell:hover .add-btn-center {
  opacity: 1;
  transform: translate(-50%, -50%) scale(1.02);
}

/* ===== Badge nhân viên kiểu mẫu (card giữa ô) ===== */
.emp-badge {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);

  width: 190px;
  max-width: calc(100% - 16px);

  background: #fff;
  border: 1px solid #e9ecef;
  border-left: 5px solid #22c55e;
  border-radius: 12px;

  padding: 10px 10px 12px;
  cursor: pointer;

  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.emp-badge:hover {
  border-color: #cfe2ff;
  border-left-color: #22c55e;
}

.emp-badge-avatar {
  width: 44px;
  height: 44px;
  border-radius: 999px;
  object-fit: cover;
  border: 1px solid #dee2e6;
  margin-bottom: 6px;
}

.emp-badge-initials {
  width: 44px;
  height: 44px;
  border-radius: 999px;
  background: #1f2a44;
  color: #fff;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 6px;
}

.emp-badge-name {
  width: 100%;
  text-align: center;
  font-weight: 700;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ===================== MODAL ===================== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  min-width: 400px;
}
.modal-title {
  margin-bottom: 15px;
  font-size: 18px;
  font-weight: 700;
}
.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

/* Confirm phải nổi trên modal cập nhật */
.confirm-overlay {
  z-index: 10050 !important;
}

/* ===================== ✅ vue-multiselect theme tweaks ===================== */
:deep(.multiselect) {
  border-radius: 10px;
}
:deep(.multiselect__tags) {
  border-radius: 10px;
  min-height: 38px;
  padding: 6px 10px;
  border-color: #dee2e6;
}
:deep(.multiselect__select) {
  height: 38px;
}
:deep(.multiselect__single) {
  margin-bottom: 0;
}
.filter-card :deep(.multiselect__option--highlight) {
  background: #0d6efd !important;
  color: #fff !important;
}

.filter-card :deep(.multiselect__option--selected.multiselect__option--highlight) {
  background: #0d6efd !important;
  color: #fff !important;
}

.filter-card :deep(.multiselect__option) {
  background: #fff;
  color: #212529;
  font-weight: 400;
}

.filter-card :deep(.multiselect__option--selected) {
  background: #fff !important;
  color: #212529 !important;
  font-weight: 400 !important;
}

.filter-card :deep(.multiselect__option--highlight) {
  background: #0d6efd !important;
  color: #fff !important;
}

.filter-card :deep(.multiselect__option--selected.multiselect__option--highlight) {
  background: #0d6efd !important;
  color: #fff !important;
  font-weight: 400 !important;
}

.filter-card :deep(.multiselect__option--highlight::after),
.filter-card :deep(.multiselect__option--selected::after),
.filter-card :deep(.multiselect__option--selected.multiselect__option--highlight::after) {
  display: none !important;
}
/* ===================== BULK MODAL ===================== */
.bulk-card {
  width: min(1240px, 98vw);
  padding: 0;
  overflow: hidden;
}
.bulk-header {
  background: #1f2a44;
  color: #fff;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.bulk-title {
  font-weight: 700;
  font-size: 16px;
}
.bulk-subtitle {
  opacity: 0.85;
  font-size: 12px;
}
.bulk-body {
  padding: 16px;
  background: #f8fafc;
}
.bulk-box {
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 12px;
}
.bulk-calendar {
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 12px;
  min-height: 520px;
  overflow: hidden;
}
.bulk-cal-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  border-bottom: 1px solid #eef2f7;
  padding-bottom: 8px;
  margin-bottom: 10px;
}
.bulk-week-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}
@media (min-width: 992px) {
  .bulk-week-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
.bulk-day {
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 10px;
  background: #fff;
}
.bulk-day.disabled {
  opacity: 0.6;
  background: #f8f9fa;
}
.bulk-day-inner {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  cursor: pointer;
}

.bulk-month-wrap {
  width: 100%;
  overflow: hidden;
}

.bulk-month-table {
  width: 100%;
  min-width: 0;
  table-layout: fixed;
  border-collapse: separate;
  border-spacing: 0;
}
.bulk-month-table th {
  background: #1f2a44;
  color: #fff;
  padding: 10px;
  text-align: center;
  font-weight: 700;
  position: sticky;
  top: 0;
  z-index: 1;
}
.bulk-month-cell {
  border: 1px solid #eef2f7;
  vertical-align: top;
  padding: 8px;
  height: 82px;
  background: #fff;
  overflow: hidden;
  position: relative;
}

.bulk-month-cell.out-month {
  background: #fbfcfe;
  opacity: 0.55;
}
.bulk-month-cell.disabled {
  background: #f8f9fa;
  opacity: 0.7;
}
.bulk-month-top {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.bulk-month-checkbox {
  position: absolute;
  top: 2px;
  right: 2px;
  border-color: #adb5bd !important;
  box-shadow: none !important;
}

.bulk-month-checkbox:not(:checked) {
  background-color: #fff;
}
.bulk-daynum {
  font-weight: 700;
  font-size: 18px;
  color: #495057;
  text-align: center;
  line-height: 1;
}
.bulk-reason {
  margin-top: 6px;
  font-size: 11px;
  color: #dc3545;
  line-height: 1.2;
}
.bulk-footer {
  padding: 12px 16px;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  background: #fff;
}
.bulk-report {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px dashed #e9ecef;
}

.calendar-cell-locked {
  background: #f3f4f6;
}

.calendar-cell-clickable {
  cursor: pointer;
}

.emp-badge-disabled {
  cursor: not-allowed;
  opacity: 0.72;
  border-left-color: #94a3b8;
}

.emp-badge-disabled:hover {
  border-color: #e9ecef;
  border-left-color: #94a3b8;
}
.filter-card {
  overflow: visible;
  position: relative;
  z-index: 20;
}

.filter-body,
.filter-card .card-body {
  overflow: visible;
}

.filter-card :deep(.multiselect) {
  position: relative;
}

.filter-card :deep(.multiselect--active) {
  z-index: 1000;
}

.filter-card :deep(.multiselect__content-wrapper) {
  position: absolute !important;
  top: 100%;
  left: 0;
  right: 0;
  width: 100%;
  max-height: 220px !important;
  margin-top: 4px;
  border: 1px solid #dee2e6;
  //border-radius: 10px;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.12);
  overflow-y: auto;
  z-index: 9999;
  background: #fff;
}
.date-picker-group {
  position: relative;
}

:global(.flatpickr-calendar) {
  z-index: 20000 !important;
}
</style>
