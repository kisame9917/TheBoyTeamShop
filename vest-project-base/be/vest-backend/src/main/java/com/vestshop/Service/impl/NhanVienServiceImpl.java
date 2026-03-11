package com.vestshop.Service.impl;

import com.vestshop.Entity.NhanVien;
import com.vestshop.Entity.QuyenHan;
import com.vestshop.Exception.ApiException;
import com.vestshop.Repository.NhanVienRepository;
import com.vestshop.Repository.QuyenHanRepository;
import com.vestshop.Service.CloudinaryMediaStorageService;
import com.vestshop.Service.EmailService;
import com.vestshop.Service.NhanVienService;
import com.vestshop.Util.CredentialUtil;
import com.vestshop.dto.request.NhanVienRequest;
import com.vestshop.dto.response.NhanVienResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NhanVienServiceImpl implements NhanVienService {

    private final NhanVienRepository nhanVienRepository;
    private final QuyenHanRepository quyenHanRepository;
    private final EmailService emailService;
    private final CloudinaryMediaStorageService mediaStorageService;

    private static final String DEFAULT_AVATAR = "/uploads/defaults/user.jpg";
    private static final int USERNAME_MAX_LEN = 80;

    // Chống trùng nhẹ khi tạo liên tiếp (1 instance backend)
    private static final Object USERNAME_SEQ_LOCK = new Object();

    private String normalizeAvatar(String v) {
        if (v == null) return DEFAULT_AVATAR;
        String s = v.trim();
        return s.isEmpty() ? DEFAULT_AVATAR : s;
    }

    /**
     * Lấy số đuôi 3 chữ số lớn nhất trong toàn bộ bảng nhân viên, rồi +1.
     * Không phụ thuộc vào base tên.
     *
     * Ví dụ đã có: abc001, xyz002 => return 3
     *
     * NOTE: Cố tình làm bằng Java để tránh các vấn đề CHAR/NCHAR pad khoảng trắng trong SQL Server.
     */
    private int nextGlobalSuffix3Digits() {
        List<NhanVien> all = nhanVienRepository.findAll();

        int max = 0;
        for (NhanVien nv : all) {
            String tk = nv.getTaiKhoan();
            if (tk == null) continue;

            tk = tk.trim(); // quan trọng nếu DB lưu CHAR bị pad
            if (tk.length() < 3) continue;

            String last3 = tk.substring(tk.length() - 3);
            if (isAllDigits(last3)) {
                int val = Integer.parseInt(last3);
                if (val > max) max = val;
            }
        }
        return max + 1;
    }

    private boolean isAllDigits(String s) {
        if (s == null || s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NhanVienResponse> getAll() {
        return nhanVienRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NhanVienResponse getById(Long id) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy nhân viên ID: " + id));
        return mapToResponse(nv);
    }

    @Override
    @Transactional
    public NhanVienResponse create(NhanVienRequest request) {
        // Email bắt buộc để gửi tài khoản/mật khẩu
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email không được để trống để gửi thông tin tài khoản");
        }

        if (nhanVienRepository.existsByMaNhanVien(request.getMaNhanVien())) {
            throw new ApiException(HttpStatus.CONFLICT, "Mã nhân viên đã tồn tại");
        }
        if (nhanVienRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email đã tồn tại");
        }
//        if (request.getCccd() != null && nhanVienRepository.existsByCccd(request.getCccd())) {
//            throw new ApiException(HttpStatus.CONFLICT, "CCCD đã tồn tại");
//        }
        if (request.getSoDienThoai() != null && nhanVienRepository.existsBySoDienThoai(request.getSoDienThoai())) {
            throw new ApiException(HttpStatus.CONFLICT, "Số điện thoại đã tồn tại");
        }

        QuyenHan quyenHan = quyenHanRepository.findById(request.getQuyenHanId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                        "Không tìm thấy quyền hạn ID: " + request.getQuyenHanId()));

        final String generatedTaiKhoan;
        final String generatedMatKhau;

        // ✅ Sinh tài khoản: base theo tên + đuôi số global tăng dần
        synchronized (USERNAME_SEQ_LOCK) {
            String base = CredentialUtil.buildUsernameBase(request.getTenNhanVien());

            int next = nextGlobalSuffix3Digits();
            if (next > 999) {
                throw new ApiException(HttpStatus.CONFLICT, "Đã vượt quá giới hạn 999 tài khoản theo định dạng 3 số.");
            }

            String suffix = String.format("%03d", next);

            // đảm bảo tổng length <= 80
            int maxBaseLen = USERNAME_MAX_LEN - suffix.length(); // 80 - 3 = 77
            if (maxBaseLen < 1) maxBaseLen = 1;
            if (base.length() > maxBaseLen) base = base.substring(0, maxBaseLen);

            String tk = base + suffix;

            // an toàn: nếu trùng thì tăng tiếp
            while (nhanVienRepository.existsByTaiKhoan(tk)) {
                next++;
                if (next > 999) {
                    throw new ApiException(HttpStatus.CONFLICT, "Đã vượt quá giới hạn 999 tài khoản theo định dạng 3 số.");
                }
                suffix = String.format("%03d", next);
                tk = base + suffix;
            }

            generatedTaiKhoan = tk;
            generatedMatKhau = CredentialUtil.generateRandomPassword(10);
        }

        LocalDateTime now = LocalDateTime.now();

        NhanVien nv = NhanVien.builder()
                .quyenHan(quyenHan)
                .maNhanVien(request.getMaNhanVien())
                .tenNhanVien(request.getTenNhanVien())
                .soDienThoai(request.getSoDienThoai())
//                .cccd(request.getCccd())
                .email(request.getEmail())
                .taiKhoan(generatedTaiKhoan)
                .matKhau(generatedMatKhau)
                .ngaySinh(request.getNgaySinh())
                .gioiTinh(request.getGioiTinh())
                .diaChi(request.getDiaChi())
                .ngayTao(now)
                .ngayCapNhat(now)
                .trangThai(request.getTrangThai() != null ? request.getTrangThai() : Boolean.TRUE)
                .mediaAvatar(mediaStorageService.getOptional(request.getMediaAvatarId()))
                .anhDaiDien(normalizeAvatar(mediaStorageService.resolveUrl(mediaStorageService.getOptional(request.getMediaAvatarId()), request.getAnhDaiDien())))
                .build();

        NhanVien saved = nhanVienRepository.save(nv);

        // ✅ gửi email (fail thì throw -> rollback)
        emailService.sendNewNhanVienCredentials(
                request.getEmail().trim(),
                request.getTenNhanVien(),
                generatedTaiKhoan,
                generatedMatKhau
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public NhanVienResponse update(Long id, NhanVienRequest request) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy nhân viên ID: " + id));

        // ✅ KHÔNG cho sửa tài khoản / mật khẩu (đúng yêu cầu)
        if (request.getTaiKhoan() != null && !request.getTaiKhoan().isBlank()
                && !request.getTaiKhoan().trim().equals(String.valueOf(nv.getTaiKhoan()).trim())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Không được phép cập nhật tài khoản");
        }
        if (request.getMatKhau() != null && !request.getMatKhau().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Không được phép cập nhật mật khẩu");
        }

        if (request.getQuyenHanId() != null
                && (nv.getQuyenHan() == null || !request.getQuyenHanId().equals(nv.getQuyenHan().getId()))) {
            QuyenHan quyenHan = quyenHanRepository.findById(request.getQuyenHanId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                            "Không tìm thấy quyền hạn ID: " + request.getQuyenHanId()));
            nv.setQuyenHan(quyenHan);
        }

        if (request.getEmail() != null && !request.getEmail().equals(nv.getEmail())) {
            nhanVienRepository.findByEmail(request.getEmail())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> { throw new ApiException(HttpStatus.CONFLICT, "Email đã tồn tại"); });
            nv.setEmail(request.getEmail());
        }

//        if (request.getCccd() != null && !request.getCccd().equals(nv.getCccd())) {
//            nhanVienRepository.findByCccd(request.getCccd())
//                    .filter(other -> !other.getId().equals(id))
//                    .ifPresent(other -> { throw new ApiException(HttpStatus.CONFLICT, "CCCD đã tồn tại"); });
//            nv.setCccd(request.getCccd());
//        }

        if (request.getSoDienThoai() != null && !request.getSoDienThoai().equals(nv.getSoDienThoai())) {
            nhanVienRepository.findBySoDienThoai(request.getSoDienThoai())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> { throw new ApiException(HttpStatus.CONFLICT, "Số điện thoại đã tồn tại"); });
            nv.setSoDienThoai(request.getSoDienThoai());
        }

        if (request.getTenNhanVien() != null) nv.setTenNhanVien(request.getTenNhanVien());
        if (request.getNgaySinh() != null) nv.setNgaySinh(request.getNgaySinh());
        if (request.getGioiTinh() != null) nv.setGioiTinh(request.getGioiTinh());
        if (request.getDiaChi() != null) nv.setDiaChi(request.getDiaChi());
        if (request.getTrangThai() != null) nv.setTrangThai(request.getTrangThai());

        if (request.getAnhDaiDien() != null || request.getMediaAvatarId() != null) {
            var mediaAvatar = mediaStorageService.getOptional(request.getMediaAvatarId());
            nv.setMediaAvatar(mediaAvatar);
            nv.setAnhDaiDien(normalizeAvatar(mediaStorageService.resolveUrl(mediaAvatar, request.getAnhDaiDien())));
        }

        nv.setNgayCapNhat(LocalDateTime.now());
        return mapToResponse(nhanVienRepository.save(nv));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy nhân viên ID: " + id));
        nv.setTrangThai(Boolean.FALSE);
        nv.setNgayCapNhat(LocalDateTime.now());
        nhanVienRepository.save(nv);
    }

    private NhanVienResponse mapToResponse(NhanVien nv) {
        return NhanVienResponse.builder()
                .id(nv.getId())
                .quyenHanId(nv.getQuyenHan() != null ? nv.getQuyenHan().getId() : null)
                .tenQuyenHan(nv.getQuyenHan() != null ? nv.getQuyenHan().getTenQuyenHan() : null)
                .maNhanVien(nv.getMaNhanVien())
                .tenNhanVien(nv.getTenNhanVien())
                .soDienThoai(nv.getSoDienThoai())
//                .cccd(nv.getCccd())
                .email(nv.getEmail())
                .taiKhoan(nv.getTaiKhoan())
                .ngaySinh(nv.getNgaySinh())
                .gioiTinh(nv.getGioiTinh())
                .diaChi(nv.getDiaChi())
                .nguoiCapNhat(nv.getNguoiCapNhat())
                .ngayTao(nv.getNgayTao())
                .ngayCapNhat(nv.getNgayCapNhat())
                .trangThai(nv.getTrangThai())
                .anhDaiDien(normalizeAvatar(mediaStorageService.resolveUrl(nv.getMediaAvatar(), nv.getAnhDaiDien())))
                .avatarUrl(normalizeAvatar(mediaStorageService.resolveUrl(nv.getMediaAvatar(), nv.getAnhDaiDien())))
                .mediaAvatarId(nv.getMediaAvatar() != null ? nv.getMediaAvatar().getId() : null)
                .build();
    }

    @Override
    @Transactional
    public NhanVienResponse updateTrangThai(Long id, Boolean trangThai) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy nhân viên ID: " + id));

        nv.setTrangThai(trangThai);
        nv.setNgayCapNhat(LocalDateTime.now());
        return mapToResponse(nhanVienRepository.save(nv));
    }

    @Override
    public NhanVien findEntityByTaiKhoan(String taiKhoan) {
        return nhanVienRepository.findByTaiKhoan(taiKhoan)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

    }
}
