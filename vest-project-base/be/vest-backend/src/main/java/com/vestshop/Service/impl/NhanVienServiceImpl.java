package com.vestshop.Service.impl;

import com.vestshop.Entity.NhanVien;
import com.vestshop.Entity.QuyenHan;
import com.vestshop.Exception.ApiException;
import com.vestshop.Repository.NhanVienRepository;
import com.vestshop.Repository.QuyenHanRepository;
import com.vestshop.Service.NhanVienService;
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

    // ✅ default ảnh
    private static final String DEFAULT_AVATAR = "/uploads/defaults/user.jpg";

    private String normalizeAvatar(String v) {
        if (v == null) return DEFAULT_AVATAR;
        String s = v.trim();
        return s.isEmpty() ? DEFAULT_AVATAR : s;
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
        // Validate required password
        if (request.getMatKhau() == null || request.getMatKhau().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Mật khẩu không được để trống");
        }

        // Uniqueness checks
        if (nhanVienRepository.existsByMaNhanVien(request.getMaNhanVien())) {
            throw new ApiException(HttpStatus.CONFLICT, "Mã nhân viên đã tồn tại");
        }
        if (nhanVienRepository.existsByTaiKhoan(request.getTaiKhoan())) {
            throw new ApiException(HttpStatus.CONFLICT, "Tài khoản đã tồn tại");
        }
        if (request.getEmail() != null && nhanVienRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email đã tồn tại");
        }
        if (request.getCccd() != null && nhanVienRepository.existsByCccd(request.getCccd())) {
            throw new ApiException(HttpStatus.CONFLICT, "CCCD đã tồn tại");
        }
        if (request.getSoDienThoai() != null && nhanVienRepository.existsBySoDienThoai(request.getSoDienThoai())) {
            throw new ApiException(HttpStatus.CONFLICT, "Số điện thoại đã tồn tại");
        }

        QuyenHan quyenHan = quyenHanRepository.findById(request.getQuyenHanId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Không tìm thấy quyền hạn ID: " + request.getQuyenHanId()));

        LocalDateTime now = LocalDateTime.now();

        NhanVien nv = NhanVien.builder()
                .quyenHan(quyenHan)
                .maNhanVien(request.getMaNhanVien())
                .tenNhanVien(request.getTenNhanVien())
                .soDienThoai(request.getSoDienThoai())
                .cccd(request.getCccd())
                .email(request.getEmail())
                .taiKhoan(request.getTaiKhoan())
                .matKhau(request.getMatKhau())
                .ngaySinh(request.getNgaySinh())
                .gioiTinh(request.getGioiTinh())
                .diaChi(request.getDiaChi())
                .ngayTao(now)
                .ngayCapNhat(now)
                .trangThai(request.getTrangThai() != null ? request.getTrangThai() : Boolean.TRUE)

                // ✅ set ảnh đại diện (nếu null/blank => default)
                .anhDaiDien(normalizeAvatar(request.getAnhDaiDien()))
                .build();

        return mapToResponse(nhanVienRepository.save(nv));
    }

    @Override
    @Transactional
    public NhanVienResponse update(Long id, NhanVienRequest request) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy nhân viên ID: " + id));

        // Update role if changed
        if (request.getQuyenHanId() != null
                && (nv.getQuyenHan() == null || !request.getQuyenHanId().equals(nv.getQuyenHan().getId()))) {
            QuyenHan quyenHan = quyenHanRepository.findById(request.getQuyenHanId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Không tìm thấy quyền hạn ID: " + request.getQuyenHanId()));
            nv.setQuyenHan(quyenHan);
        }

        // Unique fields: taiKhoan, email, cccd, soDienThoai
        if (request.getTaiKhoan() != null && !request.getTaiKhoan().equals(nv.getTaiKhoan())) {
            nhanVienRepository.findByTaiKhoan(request.getTaiKhoan())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> { throw new ApiException(HttpStatus.CONFLICT, "Tài khoản đã tồn tại"); });
            nv.setTaiKhoan(request.getTaiKhoan());
        }

        if (request.getEmail() != null && !request.getEmail().equals(nv.getEmail())) {
            nhanVienRepository.findByEmail(request.getEmail())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> { throw new ApiException(HttpStatus.CONFLICT, "Email đã tồn tại"); });
            nv.setEmail(request.getEmail());
        }

        if (request.getCccd() != null && !request.getCccd().equals(nv.getCccd())) {
            nhanVienRepository.findByCccd(request.getCccd())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> { throw new ApiException(HttpStatus.CONFLICT, "CCCD đã tồn tại"); });
            nv.setCccd(request.getCccd());
        }

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

        // ✅ Update ảnh: chỉ cập nhật khi client có gửi field anhDaiDien
        // - gửi "" => reset về default
        // - gửi null => giữ nguyên ảnh cũ
        if (request.getAnhDaiDien() != null) {
            nv.setAnhDaiDien(normalizeAvatar(request.getAnhDaiDien()));
        }

        // Only update password when provided
        if (request.getMatKhau() != null && !request.getMatKhau().isBlank()) {
            nv.setMatKhau(request.getMatKhau());
        }

        nv.setNgayCapNhat(LocalDateTime.now());
        return mapToResponse(nhanVienRepository.save(nv));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy nhân viên ID: " + id));

        // Soft delete
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
                .cccd(nv.getCccd())
                .email(nv.getEmail())
                .taiKhoan(nv.getTaiKhoan())
                .ngaySinh(nv.getNgaySinh())
                .gioiTinh(nv.getGioiTinh())
                .diaChi(nv.getDiaChi())
                .nguoiCapNhat(nv.getNguoiCapNhat())
                .ngayTao(nv.getNgayTao())
                .ngayCapNhat(nv.getNgayCapNhat())
                .trangThai(nv.getTrangThai())

                // ✅ trả ảnh ra FE
                .anhDaiDien(normalizeAvatar(nv.getAnhDaiDien()))
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
}
