package com.vestshop.Service.impl;

import com.vestshop.Entity.DiaChiKhachHang;
import com.vestshop.Entity.KhachHang;
import com.vestshop.Exception.ApiException;
import com.vestshop.Repository.DiaChiKhachHangRepository;
import com.vestshop.Repository.KhachHangRepository;
import com.vestshop.Service.CloudinaryMediaStorageService;
import com.vestshop.Service.KhachHangService;
import com.vestshop.dto.request.DiaChiKhachHangRequest;
import com.vestshop.dto.request.KhachHangRequest;
import com.vestshop.dto.response.DiaChiKhachHangResponse;
import com.vestshop.dto.response.KhachHangResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class KhachHangServiceImpl implements KhachHangService {

    private final KhachHangRepository khachHangRepository;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;
    private final CloudinaryMediaStorageService mediaStorageService;

    private static final String DEFAULT_AVATAR = "/uploads/defaults/user.jpg";
    private static final String MA_PREFIX_DEFAULT = "KH";
    private static final int MAX_ADDRESS = 5;

    private String normalizeAvatar(String v) {
        if (v == null) return DEFAULT_AVATAR;
        String s = v.trim();
        return s.isEmpty() ? DEFAULT_AVATAR : s;
    }

    private void append(StringBuilder sb, String part) {
        String s = part == null ? "" : part.trim();
        if (s.isEmpty()) return;
        if (sb.length() > 0) sb.append(", ");
        sb.append(s);
    }

    private String joinAddress(DiaChiKhachHang d) {
        if (d == null) return "";
        StringBuilder sb = new StringBuilder();
        append(sb, d.getDiaChiChiTiet());
        append(sb, d.getPhuongXa());
        append(sb, d.getQuanHuyen());
        append(sb, d.getTinhThanh());
        append(sb, d.getQuocGia());
        return sb.toString();
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private boolean isDigitsOnly(String s) {
        if (s == null) return false;
        String t = s.trim();
        return !t.isEmpty() && t.matches("^\\d+$");
    }

    private DiaChiKhachHangResponse mapDiaChi(DiaChiKhachHang d) {
        if (d == null) return null;
        return DiaChiKhachHangResponse.builder()
                .id(d.getId())
                .idKhachHang(d.getKhachHang() != null ? d.getKhachHang().getId() : null)
                .tenNguoiNhan(d.getTenNguoiNhan())
                .soDienThoai(d.getSoDienThoai())
                .diaChiChiTiet(d.getDiaChiChiTiet())
                .phuongXa(d.getPhuongXa())
                .quanHuyen(d.getQuanHuyen())
                .tinhThanh(d.getTinhThanh())
                .quocGia(d.getQuocGia())
                .laMacDinh(d.getLaMacDinh())
                .trangThai(d.getTrangThai())
                .build();
    }

    /**
     * ✅ FIX list chưa có địa chỉ:
     * - ưu tiên lấy default
     * - nếu chưa có default => lấy địa chỉ active mới nhất làm fallback
     */
    private DiaChiKhachHang pickDefaultOrLatest(Long khachHangId) {
        DiaChiKhachHang dc = diaChiKhachHangRepository
                .findFirstByKhachHangIdAndLaMacDinhTrueOrderByIdDesc(khachHangId)
                .orElse(null);

        if (dc != null) return dc;

        // fallback: lấy địa chỉ active mới nhất (để list không bị trống)
        List<DiaChiKhachHang> list = diaChiKhachHangRepository
                .findByKhachHang_IdAndTrangThaiTrueOrderByLaMacDinhDescIdDesc(khachHangId);

        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    private KhachHangResponse mapToResponse(KhachHang kh) {
        DiaChiKhachHang dc = pickDefaultOrLatest(kh.getId());

        return KhachHangResponse.builder()
                .id(kh.getId())
                .maKhachHang(kh.getMaKhachHang())
                .tenKhachHang(kh.getTenKhachHang())
                .gioiTinh(kh.getGioiTinh())
                .email(kh.getEmail())
                .soDienThoai(kh.getSoDienThoai())
                .ngaySinh(kh.getNgaySinh()) // ✅ ADD
                .taiKhoan(kh.getTaiKhoan())
                .ngayTao(kh.getNgayTao())
                .ngayCapNhat(kh.getNgayCapNhat())
                .trangThai(kh.getTrangThai())
                .anhDaiDien(normalizeAvatar(mediaStorageService.resolveUrl(kh.getMediaAvatar(), kh.getAnhDaiDien())))
                .avatarUrl(normalizeAvatar(mediaStorageService.resolveUrl(kh.getMediaAvatar(), kh.getAnhDaiDien())))
                .mediaAvatarId(kh.getMediaAvatar() != null ? kh.getMediaAvatar().getId() : null)
                .diaChi(joinAddress(dc))
                .diaChiMacDinh(mapDiaChi(dc))
                .build();
    }

    // ====================== LIST / DETAIL ======================

    @Override
    @Transactional(readOnly = true)
    public List<KhachHangResponse> getAll() {
        return khachHangRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public KhachHangResponse getById(Long id) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng ID: " + id));
        return mapToResponse(kh);
    }

    // ====================== CRUD ======================

    @Override
    @Transactional
    public KhachHangResponse create(KhachHangRequest request) {
        if (request.getMatKhau() == null || request.getMatKhau().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Mật khẩu không được để trống");
        }
        if (request.getTaiKhoan() == null || request.getTaiKhoan().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tài khoản không được để trống");
        }
        if (request.getTenKhachHang() == null || request.getTenKhachHang().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tên khách hàng không được để trống");
        }

        if (khachHangRepository.existsByTaiKhoan(request.getTaiKhoan())) {
            throw new ApiException(HttpStatus.CONFLICT, "Tài khoản đã tồn tại");
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && khachHangRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email đã tồn tại");
        }
        if (request.getSoDienThoai() != null && !request.getSoDienThoai().isBlank()
                && khachHangRepository.existsBySoDienThoai(request.getSoDienThoai())) {
            throw new ApiException(HttpStatus.CONFLICT, "Số điện thoại đã tồn tại");
        }

        String ma = getNextMaKhachHang(MA_PREFIX_DEFAULT);
        LocalDateTime now = LocalDateTime.now();

        KhachHang kh = new KhachHang();
        kh.setMaKhachHang(ma);
        kh.setTenKhachHang(request.getTenKhachHang());
        kh.setGioiTinh(request.getGioiTinh());
        kh.setEmail(request.getEmail());
        kh.setSoDienThoai(request.getSoDienThoai());
        kh.setNgaySinh(request.getNgaySinh()); // ✅ ADD
        kh.setTaiKhoan(request.getTaiKhoan());
        kh.setMatKhau(request.getMatKhau());
        kh.setNgayTao(now);
        kh.setNgayCapNhat(now);
        kh.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : Boolean.TRUE);
        var mediaAvatar = mediaStorageService.getOptional(request.getMediaAvatarId());
        kh.setMediaAvatar(mediaAvatar);
        kh.setAnhDaiDien(normalizeAvatar(mediaStorageService.resolveUrl(mediaAvatar, request.getAnhDaiDien())));

        kh = khachHangRepository.save(kh);

        // ✅ NEW: nếu FE gửi diaChiList / diaChiMacDinhId -> xử lý multi-address
        // ✅ OLD: nếu FE gửi kiểu cũ -> upsert default
        syncAddresses(kh, request);

        return mapToResponse(kh);
    }

    @Override
    @Transactional
    public KhachHangResponse update(Long id, KhachHangRequest request) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng ID: " + id));

        if (request.getTaiKhoan() != null && !request.getTaiKhoan().equals(kh.getTaiKhoan())) {
            khachHangRepository.findByTaiKhoan(request.getTaiKhoan())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> { throw new ApiException(HttpStatus.CONFLICT, "Tài khoản đã tồn tại"); });
            kh.setTaiKhoan(request.getTaiKhoan());
        }

        if (request.getEmail() != null && !request.getEmail().equals(kh.getEmail())) {
            khachHangRepository.findByEmail(request.getEmail())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> { throw new ApiException(HttpStatus.CONFLICT, "Email đã tồn tại"); });
            kh.setEmail(request.getEmail());
        }

        if (request.getSoDienThoai() != null && !request.getSoDienThoai().equals(kh.getSoDienThoai())) {
            khachHangRepository.findBySoDienThoai(request.getSoDienThoai())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> { throw new ApiException(HttpStatus.CONFLICT, "Số điện thoại đã tồn tại"); });
            kh.setSoDienThoai(request.getSoDienThoai());
        }

        if (request.getTenKhachHang() != null) kh.setTenKhachHang(request.getTenKhachHang());
        if (request.getGioiTinh() != null) kh.setGioiTinh(request.getGioiTinh());
        if (request.getTrangThai() != null) kh.setTrangThai(request.getTrangThai());

        // ✅ ADD: ngày sinh (chỉ update khi có gửi)
        if (request.getNgaySinh() != null) {
            kh.setNgaySinh(request.getNgaySinh());
        }

        // ảnh: null => giữ, "" => default
        if (request.getAnhDaiDien() != null || request.getMediaAvatarId() != null) {
            var mediaAvatar = mediaStorageService.getOptional(request.getMediaAvatarId());
            kh.setMediaAvatar(mediaAvatar);
            kh.setAnhDaiDien(normalizeAvatar(mediaStorageService.resolveUrl(mediaAvatar, request.getAnhDaiDien())));
        }

        // mật khẩu: chỉ đổi khi có gửi và không rỗng
        if (request.getMatKhau() != null && !request.getMatKhau().isBlank()) {
            kh.setMatKhau(request.getMatKhau());
        }

        kh.setNgayCapNhat(LocalDateTime.now());
        kh = khachHangRepository.save(kh);

        // ✅ Sync địa chỉ kiểu mới / kiểu cũ
        syncAddresses(kh, request);

        return mapToResponse(kh);
    }

    // ====================== SWITCH TRẠNG THÁI ======================

    @Override
    @Transactional
    public KhachHangResponse updateTrangThai(Long id, Boolean trangThai) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng ID: " + id));

        kh.setTrangThai(trangThai);
        kh.setNgayCapNhat(LocalDateTime.now());

        return mapToResponse(khachHangRepository.save(kh));
    }

    // ====================== NEXT CODE ======================

    @Override
    @Transactional(readOnly = true)
    public String getNextMaKhachHang(String prefix) {
        String p = (prefix == null || prefix.isBlank()) ? MA_PREFIX_DEFAULT : prefix.trim().toUpperCase();

        Optional<KhachHang> top = khachHangRepository.findTopByMaKhachHangStartingWithOrderByMaKhachHangDesc(p);
        int next = 1;

        if (top.isPresent()) {
            String last = top.get().getMaKhachHang(); // KH012
            String num = last.replaceAll("[^0-9]", "");
            if (!num.isBlank()) {
                try { next = Integer.parseInt(num) + 1; } catch (Exception ignored) {}
            }
        }
        return p + String.format("%03d", next);
    }

    // ====================== ADDRESS APIs (✅ implement interface) ======================

    @Override
    @Transactional(readOnly = true)
    public List<DiaChiKhachHangResponse> getDiaChiList(Long khachHangId) {
        // check KH tồn tại
        if (!khachHangRepository.existsById(khachHangId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng ID: " + khachHangId);
        }

        List<DiaChiKhachHang> list = diaChiKhachHangRepository
                .findByKhachHang_IdAndTrangThaiTrueOrderByLaMacDinhDescIdDesc(khachHangId);

        return (list == null ? List.<DiaChiKhachHang>of() : list).stream()
                .map(this::mapDiaChi)
                .toList();
    }

    @Override
    @Transactional
    public DiaChiKhachHangResponse addDiaChi(Long khachHangId, DiaChiKhachHangRequest request) {
        KhachHang kh = khachHangRepository.findById(khachHangId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng ID: " + khachHangId));

        long count = diaChiKhachHangRepository.countByKhachHang_IdAndTrangThaiTrue(khachHangId);
        if (count >= MAX_ADDRESS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tối đa " + MAX_ADDRESS + " địa chỉ");
        }

        String tenNguoiNhan = trimToNull(request.getTenNguoiNhan());
        String soDienThoai = trimToNull(request.getSoDienThoai());
        String tinhThanh = trimToNull(request.getTinhThanh());
        String quanHuyen = trimToNull(request.getQuanHuyen());
        String phuongXa = trimToNull(request.getPhuongXa());
        String diaChiChiTiet = trimToNull(request.getDiaChiChiTiet());
        String quocGia = trimToNull(request.getQuocGia());

        if (tenNguoiNhan == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Thiếu tên người nhận");
        if (soDienThoai == null || !isDigitsOnly(soDienThoai)) throw new ApiException(HttpStatus.BAD_REQUEST, "SĐT người nhận phải là số");
        if (tinhThanh == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Thiếu Tỉnh/Thành phố");
        if (quanHuyen == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Thiếu Quận/Huyện");
        if (phuongXa == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Thiếu Phường/Xã");
        if (diaChiChiTiet == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Thiếu địa chỉ chi tiết");
        if (quocGia == null) quocGia = "Việt Nam";

        DiaChiKhachHang dc = new DiaChiKhachHang();
        dc.setKhachHang(kh);
        dc.setTenNguoiNhan(tenNguoiNhan);
        dc.setSoDienThoai(soDienThoai);
        dc.setTinhThanh(tinhThanh);
        dc.setQuanHuyen(quanHuyen);
        dc.setPhuongXa(phuongXa);
        dc.setDiaChiChiTiet(diaChiChiTiet);
        dc.setQuocGia(quocGia);
        dc.setTrangThai(Boolean.TRUE);

        // nếu request.laMacDinh = true hoặc KH chưa có default => set default
        boolean wantDefault = Boolean.TRUE.equals(request.getLaMacDinh());
        boolean hasDefault = diaChiKhachHangRepository
                .findFirstByKhachHangIdAndLaMacDinhTrueOrderByIdDesc(khachHangId)
                .isPresent();

        if (wantDefault || !hasDefault) {
            diaChiKhachHangRepository.clearDefaultByKhachHangId(khachHangId);
            dc.setLaMacDinh(Boolean.TRUE);
        } else {
            dc.setLaMacDinh(Boolean.FALSE);
        }

        dc = diaChiKhachHangRepository.save(dc);
        return mapDiaChi(dc);
    }

    @Override
    @Transactional
    public DiaChiKhachHangResponse setDiaChiMacDinh(Long khachHangId, Long diaChiId) {
        if (!khachHangRepository.existsById(khachHangId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng ID: " + khachHangId);
        }

        DiaChiKhachHang dc = diaChiKhachHangRepository.findByIdAndKhachHang_Id(diaChiId, khachHangId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy địa chỉ ID: " + diaChiId));

        diaChiKhachHangRepository.clearDefaultByKhachHangId(khachHangId);
        dc.setLaMacDinh(Boolean.TRUE);
        dc.setTrangThai(Boolean.TRUE);

        dc = diaChiKhachHangRepository.save(dc);
        return mapDiaChi(dc);
    }

    // ====================== INTERNAL: SYNC ADDRESS (NEW + OLD) ======================

    /**
     * ✅ Nếu FE dùng kiểu mới: request.diaChiList / request.diaChiMacDinhId
     * ✅ Nếu FE dùng kiểu cũ: tenNguoiNhan/sdtNguoiNhan/... thì upsert default như logic cũ của bạn
     */
    private void syncAddresses(KhachHang kh, KhachHangRequest request) {

        // 1) nếu chỉ muốn set default bằng diaChiMacDinhId
        if (request.getDiaChiMacDinhId() != null) {
            setDiaChiMacDinh(kh.getId(), request.getDiaChiMacDinhId());
            return;
        }

        // 2) nếu có diaChiList (kiểu mới)
        if (request.getDiaChiList() != null && !request.getDiaChiList().isEmpty()) {
            if (request.getDiaChiList().size() > MAX_ADDRESS) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Tối đa " + MAX_ADDRESS + " địa chỉ");
            }

            // lấy địa chỉ active hiện có
            List<DiaChiKhachHang> existing = diaChiKhachHangRepository
                    .findByKhachHang_IdAndTrangThaiTrueOrderByLaMacDinhDescIdDesc(kh.getId());

            Map<Long, DiaChiKhachHang> byId = new HashMap<>();
            if (existing != null) {
                for (DiaChiKhachHang d : existing) {
                    if (d.getId() != null) byId.put(d.getId(), d);
                }
            }

            int activeCount = existing == null ? 0 : existing.size();
            Long chosenDefaultId = null;

            // nếu item nào laMacDinh=true thì ưu tiên
            for (DiaChiKhachHangRequest it : request.getDiaChiList()) {
                if (Boolean.TRUE.equals(it.getLaMacDinh()) && it.getId() != null) {
                    chosenDefaultId = it.getId();
                    break;
                }
            }

            // xử lý add/update từng địa chỉ
            List<Long> touchedIds = new ArrayList<>();

            for (DiaChiKhachHangRequest it : request.getDiaChiList()) {
                DiaChiKhachHang entity = null;

                if (it.getId() != null) entity = byId.get(it.getId());

                if (entity == null) {
                    // create new
                    if (activeCount >= MAX_ADDRESS) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Tối đa " + MAX_ADDRESS + " địa chỉ");
                    }
                    entity = new DiaChiKhachHang();
                    entity.setKhachHang(kh);
                    entity.setTrangThai(Boolean.TRUE);
                    entity.setLaMacDinh(Boolean.FALSE);
                    activeCount++;
                }

                // validate required
                String tenNguoiNhan = trimToNull(it.getTenNguoiNhan());
                String soDienThoai = trimToNull(it.getSoDienThoai());
                String tinhThanh = trimToNull(it.getTinhThanh());
                String quanHuyen = trimToNull(it.getQuanHuyen());
                String phuongXa = trimToNull(it.getPhuongXa());
                String diaChiChiTiet = trimToNull(it.getDiaChiChiTiet());
                String quocGia = trimToNull(it.getQuocGia());
                if (quocGia == null) quocGia = "Việt Nam";

                if (tenNguoiNhan == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Thiếu tên người nhận");
                if (soDienThoai == null || !isDigitsOnly(soDienThoai)) throw new ApiException(HttpStatus.BAD_REQUEST, "SĐT người nhận phải là số");
                if (tinhThanh == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Thiếu Tỉnh/Thành phố");
                if (quanHuyen == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Thiếu Quận/Huyện");
                if (phuongXa == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Thiếu Phường/Xã");
                if (diaChiChiTiet == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Thiếu địa chỉ chi tiết");

                entity.setTenNguoiNhan(tenNguoiNhan);
                entity.setSoDienThoai(soDienThoai);
                entity.setTinhThanh(tinhThanh);
                entity.setQuanHuyen(quanHuyen);
                entity.setPhuongXa(phuongXa);
                entity.setDiaChiChiTiet(diaChiChiTiet);
                entity.setQuocGia(quocGia);
                entity.setTrangThai(Boolean.TRUE);

                entity = diaChiKhachHangRepository.save(entity);
                touchedIds.add(entity.getId());

                // nếu item mới được tạo và có laMacDinh=true thì chọn làm default
                if (chosenDefaultId == null && Boolean.TRUE.equals(it.getLaMacDinh())) {
                    chosenDefaultId = entity.getId();
                }
            }

            // nếu vẫn chưa chọn default => giữ default cũ nếu có, không thì lấy item đầu tiên
            if (chosenDefaultId == null) {
                DiaChiKhachHang currentDefault = diaChiKhachHangRepository
                        .findFirstByKhachHangIdAndLaMacDinhTrueOrderByIdDesc(kh.getId())
                        .orElse(null);
                if (currentDefault != null) chosenDefaultId = currentDefault.getId();
            }
            if (chosenDefaultId == null && !touchedIds.isEmpty()) chosenDefaultId = touchedIds.get(0);

            if (chosenDefaultId != null) {
                setDiaChiMacDinh(kh.getId(), chosenDefaultId);
            }

            return;
        }

        // 3) nếu không có diaChiList / diaChiMacDinhId => dùng kiểu cũ (default address)
        upsertDefaultAddressOld(kh, request);
    }

    // ----------- OLD: UPSERT ĐỊA CHỈ MẶC ĐỊNH (giữ logic của bạn) -----------

    private void upsertDefaultAddressOld(KhachHang kh, KhachHangRequest request) {

        String tenNguoiNhan  = trimToNull(request.getTenNguoiNhan());
        String sdtNguoiNhan  = trimToNull(request.getSdtNguoiNhan());
        String tinhThanh     = trimToNull(request.getTinhThanh());
        String quanHuyen     = trimToNull(request.getQuanHuyen());
        String phuongXa      = trimToNull(request.getPhuongXa());
        String diaChiChiTiet = trimToNull(request.getDiaChiChiTiet());
        String quocGia       = trimToNull(request.getQuocGia());

        boolean hasAnyAddr =
                tenNguoiNhan != null ||
                        sdtNguoiNhan != null ||
                        tinhThanh != null ||
                        quanHuyen != null ||
                        phuongXa != null ||
                        diaChiChiTiet != null ||
                        quocGia != null;

        if (!hasAnyAddr) return;

        DiaChiKhachHang dc = diaChiKhachHangRepository
                .findFirstByKhachHangIdAndLaMacDinhTrueOrderByIdDesc(kh.getId())
                .orElse(null);

        boolean isNew = false;

        if (dc == null) {
            isNew = true;
            dc = new DiaChiKhachHang();
            dc.setKhachHang(kh);

            diaChiKhachHangRepository.clearDefaultByKhachHangId(kh.getId());

            dc.setLaMacDinh(Boolean.TRUE);
            dc.setTrangThai(Boolean.TRUE);
        }

        if (tenNguoiNhan != null)  dc.setTenNguoiNhan(tenNguoiNhan);

        if (sdtNguoiNhan != null) {
            if (!isDigitsOnly(sdtNguoiNhan)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "SĐT người nhận phải là số");
            }
            dc.setSoDienThoai(sdtNguoiNhan);
        }

        if (diaChiChiTiet != null) dc.setDiaChiChiTiet(diaChiChiTiet);
        if (phuongXa != null)      dc.setPhuongXa(phuongXa);
        if (quanHuyen != null)     dc.setQuanHuyen(quanHuyen);
        if (tinhThanh != null)     dc.setTinhThanh(tinhThanh);

        if (quocGia != null) {
            dc.setQuocGia(quocGia);
        } else if (isNew && (dc.getQuocGia() == null || dc.getQuocGia().isBlank())) {
            dc.setQuocGia("Việt Nam");
        }

        diaChiKhachHangRepository.save(dc);
    }
}
