package com.vestshop.Service;

import com.vestshop.Entity.*;
import com.vestshop.Repository.*;
import com.vestshop.dto.request.DongCaRequest;
import com.vestshop.dto.request.MoCaRequest;
import com.vestshop.dto.response.CheckInCaResponse;
import com.vestshop.dto.response.PhienCaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GiaoCaService {

    private final PhienCaRepository phienCaRepository;
    private final NhanVienRepository nhanVienRepository;
    private final LichLamViecRepository lichLamViecRepository;
    private final CaLamViecRepository caLamViecRepository;
    private final GiaoDichThanhToanRepository giaoDichThanhToanRepository;
    private final HoaDonRepository hoaDonRepository;

    private static final int HOA_DON_HOAN_THANH = 4;

    /**
     * Threshold gộp ca liên = 0 phút (end == start)
     */
    private static final Duration MERGE_THRESHOLD = Duration.ZERO;

    // =========================
    // STAFF
    // =========================

    @Transactional(readOnly = true)
    public CheckInCaResponse checkIn() {
        NhanVien nv = getCurrentNhanVien();
        LocalDateTime now = LocalDateTime.now();

        Optional<PhienCa> dangMoOpt = phienCaRepository
                .findFirstByNhanVien_IdAndTrangThaiOrderByThoiGianMoDesc(nv.getId(), PhienCa.TRANG_THAI_DANG_MO);

        // Load khung ca (block) để FE có thể cảnh báo sắp hết ca
        List<ShiftBlock> blocks = resolveBlocksForNow(nv.getId(), now);
        ShiftBlock currentBlock = findBlockContaining(blocks, now);
        ShiftBlock nextBlock = findNextBlock(blocks, now);

        BigDecimal expectedTienTaiKhoanDauCa = expectedTienTaiKhoanDauCa(nv.getId());

        if (dangMoOpt.isPresent()) {
            Long secondsToEnd = null;
            LocalDateTime blockStart = null;
            LocalDateTime blockEnd = null;
            if (currentBlock != null) {
                blockStart = currentBlock.start;
                blockEnd = currentBlock.end;
                secondsToEnd = Math.max(0L, Duration.between(now, currentBlock.end).getSeconds());
            }

            return CheckInCaResponse.builder()
                    .phienDangMo(toResponse(dangMoOpt.get()))
                    .idNhanVien(nv.getId())
                    .maNhanVien(nv.getMaNhanVien())
                    .tenNhanVien(nv.getTenNhanVien())
                    .anhDaiDienNhanVien(nv.getAnhDaiDien())
                    .coLichPhanCong(currentBlock != null || nextBlock != null)
                    .duocMoCa(true)
                    .duocMoCaTuDo(false)
                    .serverNow(now)
                    .secondsToStart(0L)
                    .secondsToEnd(secondsToEnd)
                    .blockStartTime(blockStart)
                    .blockEndTime(blockEnd)
                    .expectedTienTaiKhoanDauCa(expectedTienTaiKhoanDauCa)
                    .message(currentBlock != null ? "Bạn đang trong ca làm việc" : "Ca đã hết giờ theo lịch. Vui lòng bàn giao ca.")
                    .build();
        }

        // Chưa mở ca
        if (currentBlock != null) {
            ShiftSegment seg = findSegmentContaining(currentBlock, now);
            CaLamViec ca = seg != null ? seg.ca : null;

            return CheckInCaResponse.builder()
                    .idNhanVien(nv.getId())
                    .maNhanVien(nv.getMaNhanVien())
                    .tenNhanVien(nv.getTenNhanVien())
                    .anhDaiDienNhanVien(nv.getAnhDaiDien())
                    .coLichPhanCong(true)
                    .idCaLamViec(ca != null ? ca.getId() : null)
                    .tenCa(ca != null ? ca.getTenCa() : null)
                    .gioBatDau(ca != null ? ca.getGioBatDau() : null)
                    .gioKetThuc(ca != null ? ca.getGioKetThuc() : null)
                    .ngayLamViec(seg != null ? seg.ngayLamViec : LocalDate.now())
                    .duocMoCa(true)
                    .duocMoCaTuDo(false)
                    .serverNow(now)
                    .secondsToStart(0L)
                    .secondsToEnd(Math.max(0L, Duration.between(now, currentBlock.end).getSeconds()))
                    .blockStartTime(currentBlock.start)
                    .blockEndTime(currentBlock.end)
                    .expectedTienTaiKhoanDauCa(expectedTienTaiKhoanDauCa)
                    .message("Bạn đang đúng ca phân công")
                    .build();
        }

        if (nextBlock != null) {
            ShiftSegment seg = nextBlock.segments.isEmpty() ? null : nextBlock.segments.get(0);
            CaLamViec ca = seg != null ? seg.ca : null;
            long secToStart = Math.max(0L, Duration.between(now, nextBlock.start).getSeconds());

            return CheckInCaResponse.builder()
                    .idNhanVien(nv.getId())
                    .maNhanVien(nv.getMaNhanVien())
                    .tenNhanVien(nv.getTenNhanVien())
                    .anhDaiDienNhanVien(nv.getAnhDaiDien())
                    .coLichPhanCong(true)
                    .idCaLamViec(ca != null ? ca.getId() : null)
                    .tenCa(ca != null ? ca.getTenCa() : null)
                    .gioBatDau(ca != null ? ca.getGioBatDau() : null)
                    .gioKetThuc(ca != null ? ca.getGioKetThuc() : null)
                    .ngayLamViec(seg != null ? seg.ngayLamViec : LocalDate.now())
                    .duocMoCa(false)
                    .duocMoCaTuDo(false)
                    .serverNow(now)
                    .secondsToStart(secToStart)
                    .secondsToEnd(null)
                    .blockStartTime(nextBlock.start)
                    .blockEndTime(nextBlock.end)
                    .expectedTienTaiKhoanDauCa(expectedTienTaiKhoanDauCa)
                    .message("Chưa tới giờ ca làm việc")
                    .build();
        }

        // Không có lịch phân công: KHÔNG cho mở ca tự do
        return CheckInCaResponse.builder()
                .idNhanVien(nv.getId())
                .maNhanVien(nv.getMaNhanVien())
                .tenNhanVien(nv.getTenNhanVien())
                .anhDaiDienNhanVien(nv.getAnhDaiDien())
                .coLichPhanCong(false)
                .duocMoCa(false)
                .duocMoCaTuDo(false)
                .serverNow(now)
                .secondsToStart(null)
                .secondsToEnd(null)
                .blockStartTime(null)
                .blockEndTime(null)
                .expectedTienTaiKhoanDauCa(expectedTienTaiKhoanDauCa)
                .message("Không tìm thấy lịch phân công")
                .build();
    }

    @Transactional
    public PhienCaResponse moCa(MoCaRequest req) {
        NhanVien nv = getCurrentNhanVien();

        Optional<PhienCa> dangMoOpt = phienCaRepository
                .findFirstByNhanVien_IdAndTrangThaiOrderByThoiGianMoDesc(nv.getId(), PhienCa.TRANG_THAI_DANG_MO);
        if (dangMoOpt.isPresent()) {
            throw new RuntimeException("Bạn đang có ca đang mở. Vui lòng đóng ca trước.");
        }

        // Không hỗ trợ mở ca tự do
        if (req != null && Boolean.TRUE.equals(req.getMoTuDo())) {
            throw new RuntimeException("Không hỗ trợ mở ca tự do.");
        }

        LocalDateTime now = LocalDateTime.now();
        List<ShiftBlock> blocks = resolveBlocksForNow(nv.getId(), now);
        ShiftBlock currentBlock = findBlockContaining(blocks, now);

        if (currentBlock == null) {
            throw new RuntimeException("Bạn không có ca phân công hợp lệ tại thời điểm này.");
        }

        // Validate tiền tài khoản đầu ca phải khớp tiền tài khoản thực tế ca trước
        BigDecimal expected = expectedTienTaiKhoanDauCa(nv.getId());
        BigDecimal inputTienTaiKhoanDauCa = safeMoney(req != null ? req.getTienTaiKhoanDauCa() : null);
        if (inputTienTaiKhoanDauCa.compareTo(expected) != 0) {
            throw new RuntimeException("Vui lòng kiểm tra lại doanh thu ca trước.");
        }

        ShiftSegment seg = findSegmentContaining(currentBlock, now);
        CaLamViec ca = seg != null ? seg.ca : null;
        LocalDate ngayLamViec = seg != null ? seg.ngayLamViec : LocalDate.now();

        // Nếu FE truyền idCaLamViec thì có thể dùng để tham chiếu (optional)
        if (ca == null && req != null && req.getIdCaLamViec() != null) {
            ca = caLamViecRepository.findById(req.getIdCaLamViec()).orElse(null);
        }

        String ma = buildMaPhien(nv.getMaNhanVien(), now);
        PhienCa phien = PhienCa.builder()
                .maPhien(ma)
                .nhanVien(nv)
                .caLamViec(ca)
                .ngayLamViec(ngayLamViec)
                .thoiGianMo(now)
                .tienMatDauCa(safeMoney(req != null ? req.getTienMatDauCa() : null))
                .tienTaiKhoanDauCa(inputTienTaiKhoanDauCa)
                .ghiChu(req != null ? req.getGhiChu() : null)
                .trangThai(PhienCa.TRANG_THAI_DANG_MO)
                .build();

        PhienCa saved = phienCaRepository.save(phien);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PhienCaResponse phienHienTai() {
        NhanVien nv = getCurrentNhanVien();
        PhienCa phien = phienCaRepository
                .findFirstByNhanVien_IdAndTrangThaiOrderByThoiGianMoDesc(nv.getId(), PhienCa.TRANG_THAI_DANG_MO)
                .orElseThrow(() -> new RuntimeException("Bạn chưa mở ca"));
        return toResponse(phien);
    }

    @Transactional
    public PhienCaResponse dongCa(DongCaRequest req) {
        NhanVien nv = getCurrentNhanVien();
        PhienCa phien = phienCaRepository
                .findFirstByNhanVien_IdAndTrangThaiOrderByThoiGianMoDesc(nv.getId(), PhienCa.TRANG_THAI_DANG_MO)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca đang mở"));

        LocalDateTime dong = LocalDateTime.now();
        LocalDateTime from = phien.getThoiGianMo();

        BigDecimal doanhThuTienMat = giaoDichThanhToanRepository.sumTienMatByHoaDonCreatedRange(from, dong, HOA_DON_HOAN_THANH);
        BigDecimal doanhThuKhac = giaoDichThanhToanRepository.sumKhacTienMatByHoaDonCreatedRange(from, dong, HOA_DON_HOAN_THANH);

        BigDecimal tong = safeMoney(doanhThuTienMat).add(safeMoney(doanhThuKhac));

        BigDecimal tienMatLyThuyet = safeMoney(phien.getTienMatDauCa()).add(safeMoney(doanhThuTienMat));
        BigDecimal tienTaiKhoanLyThuyet = safeMoney(phien.getTienTaiKhoanDauCa()).add(safeMoney(doanhThuKhac));

        BigDecimal tienMatThucTe = safeMoney(req != null ? req.getTienMatThucTe() : null);
        BigDecimal tienTaiKhoanThucTe = safeMoney(req != null ? req.getTienTaiKhoanThucTe() : null);

        BigDecimal chenhTienMat = tienMatThucTe.subtract(tienMatLyThuyet);

        phien.setThoiGianDong(dong);
        phien.setDoanhThuTienMat(safeMoney(doanhThuTienMat));
        phien.setDoanhThuCkThe(safeMoney(doanhThuKhac));
        phien.setTongDoanhThu(tong);
        phien.setTienMatLyThuyet(tienMatLyThuyet);
        phien.setTienMatThucTe(tienMatThucTe);
        phien.setTienTaiKhoanThucTe(tienTaiKhoanThucTe);
        phien.setChenhLech(chenhTienMat);

        if (req != null && req.getGhiChu() != null) {
            phien.setGhiChu(req.getGhiChu());
        }
        phien.setTrangThai(PhienCa.TRANG_THAI_DA_DONG);

        PhienCa saved = phienCaRepository.save(phien);
        // toResponse() sẽ tính thêm tổng lý thuyết/thực tế/chênh lệch tổng
        return toResponse(saved);
    }

    // =========================
    // ADMIN
    // =========================

    @Transactional(readOnly = true)
    public Page<PhienCaResponse> adminSearch(String keyword,
                                            Long idCa,
                                            Long idNv,
                                            LocalDate fromDate,
                                            LocalDate toDate,
                                            Integer trangThai,
                                            Pageable pageable) {
        Page<PhienCa> page = phienCaRepository.searchAdmin(keyword, idCa, idNv, fromDate, toDate, trangThai, pageable);
        return page.map(this::toResponse);
    }

    // =========================
    // Helpers
    // =========================

    private NhanVien getCurrentNhanVien() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Không xác định được người dùng hiện tại");
        }
        String username = auth.getName();
        return nhanVienRepository.findByTaiKhoan(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên: " + username));
    }

    private static LocalDateTime combine(LocalDate date, LocalTime time) {
        return LocalDateTime.of(date, time);
    }

    private static BigDecimal safeMoney(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static String buildMaPhien(String maNhanVien, LocalDateTime now) {
        String ts = now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return "PC-" + (maNhanVien != null ? maNhanVien : "NV") + "-" + ts;
    }

    private BigDecimal expectedTienTaiKhoanDauCa(Long nhanVienId) {
        return phienCaRepository
                .findFirstByNhanVien_IdAndTrangThaiOrderByThoiGianDongDesc(nhanVienId, PhienCa.TRANG_THAI_DA_DONG)
                .map(p -> safeMoney(p.getTienTaiKhoanThucTe()))
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Load lịch cho today + yesterday để xử lý ca qua đêm.
     */
    private List<ShiftBlock> resolveBlocksForNow(Long nhanVienId, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalDate yesterday = today.minusDays(1);

        List<ShiftSegment> segments = new ArrayList<>();
        segments.addAll(loadSegments(nhanVienId, yesterday));
        segments.addAll(loadSegments(nhanVienId, today));

        return buildShiftBlocks(segments);
    }

    private List<ShiftSegment> loadSegments(Long nhanVienId, LocalDate date) {
        List<LichLamViec> lich = lichLamViecRepository.findByNhanVienIdAndNgayLamViec(nhanVienId, date);
        List<ShiftSegment> out = new ArrayList<>();

        for (LichLamViec l : lich) {
            CaLamViec ca = l.getCaLamViec();
            if (ca == null || ca.getGioBatDau() == null || ca.getGioKetThuc() == null) continue;

            LocalDateTime s = combine(date, ca.getGioBatDau());
            LocalDateTime e = combine(date, ca.getGioKetThuc());
            if (!e.isAfter(s)) {
                e = e.plusDays(1); // ca qua đêm
            }
            out.add(new ShiftSegment(date, ca, s, e));
        }

        out.sort(Comparator.comparing(seg -> seg.start));
        return out;
    }

    private List<ShiftBlock> buildShiftBlocks(List<ShiftSegment> segments) {
        List<ShiftBlock> blocks = new ArrayList<>();
        if (segments == null || segments.isEmpty()) return blocks;

        segments.sort(Comparator.comparing(seg -> seg.start));

        LocalDateTime curStart = null;
        LocalDateTime curEnd = null;
        List<ShiftSegment> curSegs = new ArrayList<>();

        for (ShiftSegment seg : segments) {
            if (curStart == null) {
                curStart = seg.start;
                curEnd = seg.end;
                curSegs.add(seg);
                continue;
            }

            boolean contiguous = seg.start.isEqual(curEnd.plus(MERGE_THRESHOLD))
                    || seg.start.isEqual(curEnd)
                    || seg.start.isBefore(curEnd); // phòng trường hợp data overlap

            if (contiguous) {
                curEnd = seg.end.isAfter(curEnd) ? seg.end : curEnd;
                curSegs.add(seg);
            } else {
                blocks.add(new ShiftBlock(curStart, curEnd, new ArrayList<>(curSegs)));
                curStart = seg.start;
                curEnd = seg.end;
                curSegs.clear();
                curSegs.add(seg);
            }
        }

        if (curStart != null) {
            blocks.add(new ShiftBlock(curStart, curEnd, new ArrayList<>(curSegs)));
        }
        return blocks;
    }

    private ShiftBlock findBlockContaining(List<ShiftBlock> blocks, LocalDateTime now) {
        if (blocks == null) return null;
        for (ShiftBlock b : blocks) {
            if ((now.isEqual(b.start) || now.isAfter(b.start)) && now.isBefore(b.end)) {
                return b;
            }
        }
        return null;
    }

    private ShiftBlock findNextBlock(List<ShiftBlock> blocks, LocalDateTime now) {
        if (blocks == null) return null;
        ShiftBlock best = null;
        for (ShiftBlock b : blocks) {
            if (b.start.isAfter(now)) {
                if (best == null || b.start.isBefore(best.start)) {
                    best = b;
                }
            }
        }
        return best;
    }

    private ShiftSegment findSegmentContaining(ShiftBlock block, LocalDateTime now) {
        if (block == null || block.segments == null || block.segments.isEmpty()) return null;
        for (ShiftSegment seg : block.segments) {
            if ((now.isEqual(seg.start) || now.isAfter(seg.start)) && now.isBefore(seg.end)) {
                return seg;
            }
        }
        return block.segments.get(0);
    }

    private PhienCaResponse toResponse(PhienCa p) {
        NhanVien nv = p.getNhanVien();
        CaLamViec ca = p.getCaLamViec();

        BigDecimal doanhThuTienMat = safeMoney(p.getDoanhThuTienMat());
        BigDecimal doanhThuKhac = safeMoney(p.getDoanhThuCkThe());

        BigDecimal tienMatLyThuyet = p.getTienMatLyThuyet() != null
                ? p.getTienMatLyThuyet()
                : safeMoney(p.getTienMatDauCa()).add(doanhThuTienMat);

        BigDecimal tienTaiKhoanLyThuyet = safeMoney(p.getTienTaiKhoanDauCa()).add(doanhThuKhac);

        BigDecimal tongLyThuyet = safeMoney(tienMatLyThuyet).add(safeMoney(tienTaiKhoanLyThuyet));
        BigDecimal tongThucTe = safeMoney(p.getTienMatThucTe()).add(safeMoney(p.getTienTaiKhoanThucTe()));
        BigDecimal chenhLechTong = tongThucTe.subtract(tongLyThuyet);

        return PhienCaResponse.builder()
                .id(p.getId())
                .maPhien(p.getMaPhien())
                .idNhanVien(nv != null ? nv.getId() : null)
                .maNhanVien(nv != null ? nv.getMaNhanVien() : null)
                .tenNhanVien(nv != null ? nv.getTenNhanVien() : null)
                .anhDaiDienNhanVien(nv != null ? nv.getAnhDaiDien() : null)
                .idCaLamViec(ca != null ? ca.getId() : null)
                .tenCa(ca != null ? ca.getTenCa() : null)
                .gioBatDau(ca != null ? ca.getGioBatDau() : null)
                .gioKetThuc(ca != null ? ca.getGioKetThuc() : null)
                .ngayLamViec(p.getNgayLamViec())
                .thoiGianMo(p.getThoiGianMo())
                .thoiGianDong(p.getThoiGianDong())
                .tienMatDauCa(p.getTienMatDauCa())
                .tienTaiKhoanDauCa(p.getTienTaiKhoanDauCa())
                .tienMatThucTe(p.getTienMatThucTe())
                .tienTaiKhoanThucTe(p.getTienTaiKhoanThucTe())
                .doanhThuTienMat(doanhThuTienMat)
                .doanhThuCkThe(doanhThuKhac)
                .tongDoanhThu(safeMoney(p.getTongDoanhThu()))
                .tienMatLyThuyet(tienMatLyThuyet)
                .tienTaiKhoanLyThuyet(tienTaiKhoanLyThuyet)
                .chenhLech(p.getChenhLech())
                .tongLyThuyet(tongLyThuyet)
                .tongThucTe(tongThucTe)
                .chenhLechTong(chenhLechTong)
                .ghiChu(p.getGhiChu())
                .trangThai(p.getTrangThai())
                .build();
    }

    private static class ShiftSegment {
        private final LocalDate ngayLamViec;
        private final CaLamViec ca;
        private final LocalDateTime start;
        private final LocalDateTime end;

        private ShiftSegment(LocalDate ngayLamViec, CaLamViec ca, LocalDateTime start, LocalDateTime end) {
            this.ngayLamViec = ngayLamViec;
            this.ca = ca;
            this.start = start;
            this.end = end;
        }
    }

    private static class ShiftBlock {
        private final LocalDateTime start;
        private final LocalDateTime end;
        private final List<ShiftSegment> segments;

        private ShiftBlock(LocalDateTime start, LocalDateTime end, List<ShiftSegment> segments) {
            this.start = start;
            this.end = end;
            this.segments = segments;
        }
    }
}
