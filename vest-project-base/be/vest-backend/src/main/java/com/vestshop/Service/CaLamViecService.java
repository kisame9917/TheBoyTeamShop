package com.vestshop.Service;

import com.vestshop.Entity.CaLamViec;
import com.vestshop.Entity.LichLamViec;
import com.vestshop.Entity.NhanVien;
import com.vestshop.Repository.CaLamViecRepository;
import com.vestshop.Repository.LichLamViecRepository;
import com.vestshop.Repository.NhanVienRepository;
import com.vestshop.dto.request.CaLamViecRequest;
import com.vestshop.dto.request.LichLamViecRequest;
import com.vestshop.dto.response.LichLamViecResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CaLamViecService {

    @Autowired private CaLamViecRepository caRepo;
    @Autowired private LichLamViecRepository lichRepo;
    @Autowired private NhanVienRepository nhanVienRepo;

    // ================= QUẢN LÝ CA MẪU (TEMPLATES) =================

    public List<CaLamViec> getAllCaLamViec() {
        return caRepo.findAll();
    }

    public CaLamViec createCaLamViec(CaLamViecRequest req) {
        if(req.getGioBatDau().isAfter(req.getGioKetThuc())) {
            throw new RuntimeException("Giờ kết thúc phải sau giờ bắt đầu!");
        }
        CaLamViec ca = CaLamViec.builder()
                .tenCa(req.getTenCa())
                .gioBatDau(req.getGioBatDau())
                .gioKetThuc(req.getGioKetThuc())
                .moTa(req.getMoTa())
                .trangThai(1)
                .build();
        return caRepo.save(ca);
    }

    public CaLamViec updateCaLamViec(Long id, CaLamViecRequest req) {
        CaLamViec ca = caRepo.findById(id).orElseThrow(() -> new RuntimeException("Ca không tồn tại"));
        ca.setTenCa(req.getTenCa());
        ca.setGioBatDau(req.getGioBatDau());
        ca.setGioKetThuc(req.getGioKetThuc());
        ca.setMoTa(req.getMoTa());
        ca.setTrangThai(req.getTrangThai());
        return caRepo.save(ca);
    }

    // ================= QUẢN LÝ LỊCH LÀM VIỆC (SCHEDULE) =================

    // 1. Lấy lịch làm việc
    public List<LichLamViecResponse> getLichLamViec(LocalDate from, LocalDate to) {
        List<LichLamViec> list = lichRepo.findByDateRange(from, to);
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // 2. Xếp lịch cho nhân viên (Check trùng giờ)
    @Transactional
    public LichLamViecResponse taoLichLamViec(LichLamViecRequest req) {
        NhanVien nv = nhanVienRepo.findById(req.getIdNhanVien())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));
        CaLamViec caMoi = caRepo.findById(req.getIdCaLamViec())
                .orElseThrow(() -> new RuntimeException("Ca làm việc không tồn tại"));


        // Validate trùng lịch trong ngày
        List<LichLamViec> lichDaCo = lichRepo.findByNhanVienIdAndNgayLamViec(nv.getId(), req.getNgayLamViec());

        for (LichLamViec lichCu : lichDaCo) {
            CaLamViec caCu = lichCu.getCaLamViec();
            boolean isOverlap = caMoi.getGioBatDau().isBefore(caCu.getGioKetThuc())
                    && caMoi.getGioKetThuc().isAfter(caCu.getGioBatDau());

            if (isOverlap) {
                throw new RuntimeException("Nhân viên " + nv.getTenNhanVien() +
                        " bị trùng lịch với ca: " + caCu.getTenCa() +
                        " (" + caCu.getGioBatDau() + "-" + caCu.getGioKetThuc() + ")");
            }
        }

        LichLamViec lich = LichLamViec.builder()
                .nhanVien(nv)
                .caLamViec(caMoi)
                .ngayLamViec(req.getNgayLamViec())
                .trangThai(1)
                .ghiChu(req.getGhiChu())
                .build();

        LichLamViec saved = lichRepo.save(lich);
        return mapToResponse(saved);
    }

    // 3. Xóa lịch làm việc
    public void xoaLichLamViec(Long id) {
        lichRepo.deleteById(id);
    }

    // Mapper helper
    private LichLamViecResponse mapToResponse(LichLamViec l) {
        return LichLamViecResponse.builder()
                .id(l.getId())
                .idNhanVien(l.getNhanVien().getId())
                .tenNhanVien(l.getNhanVien().getTenNhanVien())
                .maNhanVien(l.getNhanVien().getMaNhanVien())
                .idCaLamViec(l.getCaLamViec().getId())
                .tenCa(l.getCaLamViec().getTenCa())
                .gioBatDau(l.getCaLamViec().getGioBatDau())
                .gioKetThuc(l.getCaLamViec().getGioKetThuc())
                .ngayLamViec(l.getNgayLamViec())
                .trangThai(l.getTrangThai())
                .ghiChu(l.getGhiChu())
                .build();
    }

    public List<LichLamViecResponse> getLichCaNhan(Long idNhanVien, LocalDate from, LocalDate to) {
        // Gọi hàm repository đã khai báo trước đó: findByNhanVienIdAndNgayLamViecBetween
        List<LichLamViec> list = lichRepo.findByNhanVienIdAndNgayLamViecBetween(idNhanVien, from, to);
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public LichLamViecResponse updateLichLamViec(Long id, LichLamViecRequest req) {
        LichLamViec lich = lichRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch làm việc không tồn tại"));

        NhanVien nv = nhanVienRepo.findById(req.getIdNhanVien())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));
        CaLamViec caMoi = caRepo.findById(req.getIdCaLamViec())
                .orElseThrow(() -> new RuntimeException("Ca làm việc không tồn tại"));

        // ✅ Rule mới: 1 ca / 1 ngày chỉ được 1 nhân viên (loại trừ lịch đang sửa)
        List<LichLamViec> lichTrungCa2 = lichRepo.findByCaLamViec_IdAndNgayLamViecAndTrangThai(
                caMoi.getId(), req.getNgayLamViec(), 1
        );
        for (LichLamViec x : lichTrungCa2) {
            if (!x.getId().equals(id)) {
                throw new RuntimeException("Ca " + caMoi.getTenCa() + " ngày " + req.getNgayLamViec() +
                        " đã được xếp cho nhân viên: " + x.getNhanVien().getTenNhanVien());
            }
        }


        // Validate trùng lịch (Loại trừ chính lịch đang sửa)
        List<LichLamViec> lichDaCo = lichRepo.findByNhanVienIdAndNgayLamViec(nv.getId(), req.getNgayLamViec());
        for (LichLamViec lichCu : lichDaCo) {
            if (lichCu.getId().equals(id)) continue;

            CaLamViec caCu = lichCu.getCaLamViec();
            boolean isOverlap = caMoi.getGioBatDau().isBefore(caCu.getGioKetThuc())
                    && caMoi.getGioKetThuc().isAfter(caCu.getGioBatDau());

            if (isOverlap) {
                throw new RuntimeException("Trùng lịch với ca: " + caCu.getTenCa() +
                        " (" + caCu.getGioBatDau() + "-" + caCu.getGioKetThuc() + ")");
            }
        }

        lich.setNhanVien(nv);
        lich.setCaLamViec(caMoi);
        lich.setNgayLamViec(req.getNgayLamViec());
        lich.setGhiChu(req.getGhiChu());

        LichLamViec saved = lichRepo.save(lich);
        return mapToResponse(saved);
    }
}