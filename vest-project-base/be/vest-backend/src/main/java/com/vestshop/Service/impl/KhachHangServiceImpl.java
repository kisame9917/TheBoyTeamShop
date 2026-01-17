package com.vestshop.Service.impl;

import com.vestshop.Entity.KhachHang;
import com.vestshop.Exception.ApiException;
import com.vestshop.Repository.KhachHangRepository;
import com.vestshop.Service.KhachHangService;
import com.vestshop.dto.request.KhachHangRequest;
import com.vestshop.dto.response.KhachHangResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KhachHangServiceImpl implements KhachHangService {

    private final KhachHangRepository khachHangRepository;

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
        // Uniqueness checks
        if (khachHangRepository.existsByMaKhachHang(request.getMaKhachHang())) {
            throw new ApiException(HttpStatus.CONFLICT, "Mã khách hàng đã tồn tại");
        }
        if (khachHangRepository.existsByTaiKhoan(request.getTaiKhoan())) {
            throw new ApiException(HttpStatus.CONFLICT, "Tài khoản đã tồn tại");
        }
        if (request.getEmail() != null && khachHangRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email đã tồn tại");
        }
        if (request.getSoDienThoai() != null && khachHangRepository.existsBySoDienThoai(request.getSoDienThoai())) {
            throw new ApiException(HttpStatus.CONFLICT, "Số điện thoại đã tồn tại");
        }
        if (request.getMatKhau() == null || request.getMatKhau().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Mật khẩu không được để trống");
        }

        LocalDateTime now = LocalDateTime.now();

        KhachHang kh = KhachHang.builder()
                .maKhachHang(request.getMaKhachHang())
                .tenKhachHang(request.getTenKhachHang())
                .soDienThoai(request.getSoDienThoai())
                .taiKhoan(request.getTaiKhoan())
                .matKhau(request.getMatKhau())
                .email(request.getEmail())
                .ngayTao(now)
                .ngayCapNhat(now)
                .trangThai(request.getTrangThai() != null ? request.getTrangThai() : Boolean.TRUE)
                .build();

        return mapToResponse(khachHangRepository.save(kh));
    }

    @Override
    @Transactional
    public KhachHangResponse update(Long id, KhachHangRequest request) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng ID: " + id));

        // Unique fields: taiKhoan, email, soDienThoai
        if (request.getTaiKhoan() != null && !request.getTaiKhoan().equals(kh.getTaiKhoan())) {
            khachHangRepository.findByTaiKhoan(request.getTaiKhoan())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> {
                        throw new ApiException(HttpStatus.CONFLICT, "Tài khoản đã tồn tại");
                    });
            kh.setTaiKhoan(request.getTaiKhoan());
        }

        if (request.getEmail() != null && !request.getEmail().equals(kh.getEmail())) {
            khachHangRepository.findByEmail(request.getEmail())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> {
                        throw new ApiException(HttpStatus.CONFLICT, "Email đã tồn tại");
                    });
            kh.setEmail(request.getEmail());
        }

        if (request.getSoDienThoai() != null && !request.getSoDienThoai().equals(kh.getSoDienThoai())) {
            khachHangRepository.findBySoDienThoai(request.getSoDienThoai())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> {
                        throw new ApiException(HttpStatus.CONFLICT, "Số điện thoại đã tồn tại");
                    });
            kh.setSoDienThoai(request.getSoDienThoai());
        }

        if (request.getTenKhachHang() != null) {
            kh.setTenKhachHang(request.getTenKhachHang());
        }

        // Only update password when provided
        if (request.getMatKhau() != null && !request.getMatKhau().isBlank()) {
            kh.setMatKhau(request.getMatKhau());
        }

        if (request.getTrangThai() != null) {
            kh.setTrangThai(request.getTrangThai());
        }

        kh.setNgayCapNhat(LocalDateTime.now());
        return mapToResponse(khachHangRepository.save(kh));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng ID: " + id));

        // Soft delete to avoid FK constraint issues with hóa đơn, địa chỉ...
        kh.setTrangThai(Boolean.FALSE);
        kh.setNgayCapNhat(LocalDateTime.now());
        khachHangRepository.save(kh);
    }

    private KhachHangResponse mapToResponse(KhachHang kh) {
        return KhachHangResponse.builder()
                .id(kh.getId())
                .maKhachHang(kh.getMaKhachHang())
                .tenKhachHang(kh.getTenKhachHang())
                .soDienThoai(kh.getSoDienThoai())
                .taiKhoan(kh.getTaiKhoan())
                .email(kh.getEmail())
                .ngayTao(kh.getNgayTao())
                .ngayCapNhat(kh.getNgayCapNhat())
                .trangThai(kh.getTrangThai())
                .build();
    }
}
