package com.vestshop.Service.impl;

import com.vestshop.Entity.DiaChiKhachHang;
import com.vestshop.Entity.KhachHang;
import com.vestshop.Exception.ApiException;
import com.vestshop.Repository.DiaChiKhachHangRepository;
import com.vestshop.Repository.KhachHangRepository;
import com.vestshop.Service.KhachHangService;
import com.vestshop.dto.request.KhachHangRequest;
import com.vestshop.dto.response.DiaChiKhachHangResponse;
import com.vestshop.dto.response.KhachHangResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KhachHangServiceImpl implements KhachHangService {

    private final KhachHangRepository khachHangRepository;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;

    private static final String DEFAULT_AVATAR = "/uploads/defaults/user.jpg";
    private static final String MA_PREFIX_DEFAULT = "KH";

    private String normalizeAvatar(String v) {
        if (v == null) return DEFAULT_AVATAR;
        String s = v.trim();
        return s.isEmpty() ? DEFAULT_AVATAR : s;
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

    private void append(StringBuilder sb, String part) {
        String s = part == null ? "" : part.trim();
        if (s.isEmpty()) return;
        if (sb.length() > 0) sb.append(", ");
        sb.append(s);
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

    private KhachHangResponse mapToResponse(KhachHang kh) {
        DiaChiKhachHang dc = diaChiKhachHangRepository
                .findFirstByKhachHangIdAndLaMacDinhTrueOrderByIdDesc(kh.getId())
                .orElse(null);

        return KhachHangResponse.builder()
                .id(kh.getId())
                .maKhachHang(kh.getMaKhachHang())
                .tenKhachHang(kh.getTenKhachHang())
                .gioiTinh(kh.getGioiTinh())
                .email(kh.getEmail())
                .soDienThoai(kh.getSoDienThoai())
                .taiKhoan(kh.getTaiKhoan())
                .ngayTao(kh.getNgayTao())
                .ngayCapNhat(kh.getNgayCapNhat())
                .trangThai(kh.getTrangThai())
                .anhDaiDien(normalizeAvatar(kh.getAnhDaiDien()))
                .diaChi(joinAddress(dc))
                .diaChiMacDinh(mapDiaChi(dc))
                .build();
    }

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
        kh.setTaiKhoan(request.getTaiKhoan());
        kh.setMatKhau(request.getMatKhau());
        kh.setNgayTao(now);
        kh.setNgayCapNhat(now);
        kh.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : Boolean.TRUE);
        kh.setAnhDaiDien(normalizeAvatar(request.getAnhDaiDien()));

        kh = khachHangRepository.save(kh);

        // ✅ Lưu địa chỉ mặc định (nếu FE có gửi)
        upsertDefaultAddress(kh, request);

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

        // ảnh: null => giữ, "" => default
        if (request.getAnhDaiDien() != null) {
            kh.setAnhDaiDien(normalizeAvatar(request.getAnhDaiDien()));
        }

        // mật khẩu: chỉ đổi khi có gửi và không rỗng
        if (request.getMatKhau() != null && !request.getMatKhau().isBlank()) {
            kh.setMatKhau(request.getMatKhau());
        }

        kh.setNgayCapNhat(LocalDateTime.now());
        kh = khachHangRepository.save(kh);

        // ✅ update/insert địa chỉ mặc định
        upsertDefaultAddress(kh, request);

        return mapToResponse(kh);
    }

    // ----------- FIX: UPSERT ĐỊA CHỈ MẶC ĐỊNH -----------
    private void upsertDefaultAddress(KhachHang kh, KhachHangRequest request) {

        // trim rỗng => null để tránh overwrite bằng ""
        String tenNguoiNhan  = trimToNull(request.getTenNguoiNhan());
        String sdtNguoiNhan  = trimToNull(request.getSdtNguoiNhan());
        String tinhThanh     = trimToNull(request.getTinhThanh());
        String quanHuyen     = trimToNull(request.getQuanHuyen());
        String phuongXa      = trimToNull(request.getPhuongXa());
        String diaChiChiTiet = trimToNull(request.getDiaChiChiTiet());
        String quocGia       = trimToNull(request.getQuocGia()); // nếu FE không gửi thì null

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

            // đảm bảo chỉ có 1 default
            diaChiKhachHangRepository.clearDefaultByKhachHangId(kh.getId());

            dc.setLaMacDinh(Boolean.TRUE);
            dc.setTrangThai(Boolean.TRUE);
        }

        // ✅ CHỈ set khi có giá trị (tránh overwrite bằng null/"")
        if (tenNguoiNhan != null)  dc.setTenNguoiNhan(tenNguoiNhan);
        if (sdtNguoiNhan != null)  dc.setSoDienThoai(sdtNguoiNhan); // ✅ đúng field
        if (diaChiChiTiet != null) dc.setDiaChiChiTiet(diaChiChiTiet);
        if (phuongXa != null)      dc.setPhuongXa(phuongXa);
        if (quanHuyen != null)     dc.setQuanHuyen(quanHuyen);
        if (tinhThanh != null)     dc.setTinhThanh(tinhThanh);

        // quocGia: nếu FE có gửi thì set, không thì (tạo mới) set mặc định Việt Nam
        if (quocGia != null) {
            dc.setQuocGia(quocGia);
        } else if (isNew && (dc.getQuocGia() == null || dc.getQuocGia().isBlank())) {
            dc.setQuocGia("Việt Nam");
        }

        diaChiKhachHangRepository.save(dc);
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Override
    @Transactional
    public KhachHangResponse updateTrangThai(Long id, Boolean trangThai) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng ID: " + id));

        kh.setTrangThai(trangThai);
        kh.setNgayCapNhat(LocalDateTime.now());

        return mapToResponse(khachHangRepository.save(kh));
    }

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
}
