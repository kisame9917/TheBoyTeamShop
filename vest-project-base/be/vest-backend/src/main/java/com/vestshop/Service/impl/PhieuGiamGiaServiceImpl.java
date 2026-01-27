package com.vestshop.Service.impl;

import com.vestshop.Entity.KhachHang;
import com.vestshop.Entity.PhieuGiamGia;
import com.vestshop.Entity.PhieuGiamGiaCaNhan;
import com.vestshop.Repository.KhachHangRepository;
import com.vestshop.Repository.PhieuGiamGiaCaNhanRepository;
import com.vestshop.Repository.PhieuGiamGiaRepository;
import com.vestshop.Service.EmailService;
import com.vestshop.Service.PhieuGiamGiaService;
import com.vestshop.dto.request.PhieuGiamGiaCreateRequest;
import com.vestshop.dto.request.PhieuGiamGiaUpdateRequest;
import com.vestshop.dto.request.UpdateKhachHangNhanPhieuRequest;
import com.vestshop.dto.response.PhieuGiamGiaCaNhanResponse;
import com.vestshop.dto.response.PhieuGiamGiaDetailResponse;
import com.vestshop.dto.response.PhieuGiamGiaResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PhieuGiamGiaServiceImpl implements PhieuGiamGiaService {

    @Autowired
    PhieuGiamGiaRepository repo;

    @Autowired
    KhachHangRepository khrepo;

    @Autowired
    PhieuGiamGiaCaNhanRepository cnrepo;

    @Autowired
    EmailService emailService;

    @Override
    public List<PhieuGiamGiaResponse> getAll() {
        return repo.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getTrangThai()))
                .map(p -> new PhieuGiamGiaResponse(
                        p.getId(),
                        p.getMaGiamGia(),
                        p.getTenGiamGia(),
                        p.getSoLuong(),
                        p.getNgayBatDau(),
                        p.getNgayKetThuc(),
                        p.getTrangThai(),
                        p.getLoaiGiam(),
                        p.getGiaTriPhanTram(),
                        p.getGiaTriTienMat(),
                        Boolean.TRUE.equals(p.getLoaiPhieu()) ? "CA_NHAN" : "CONG_KHAI"
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PhieuGiamGia> findbyId(Long id) {
        return repo.findById(id);
    }

    @Override
    public PhieuGiamGiaDetailResponse detail(Long id) {
        PhieuGiamGia p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy PGG id=" + id));
        return new PhieuGiamGiaDetailResponse(
                p.getId(),
                p.getMaGiamGia(),
                p.getTenGiamGia(),
                p.getSoLuong(),
                p.getLoaiGiam(),
                p.getGiaTriPhanTram(),
                p.getGiaTriTienMat(),
                p.getMoTa(),
                p.getNgayBatDau(),
                p.getNgayKetThuc(),
                p.getNgayTao(),
                p.getNgayCapNhat(),
                p.getTrangThai(),
                p.getDonHangToiThieu(),
                p.getGiaTriGiamToiDa(),
                p.getLoaiPhieu()
        );
    }

    @Override
    public PhieuGiamGia create(PhieuGiamGiaCreateRequest dto) throws Exception {

        PhieuGiamGia pgg = new PhieuGiamGia();
        pgg.setMaGiamGia(generateUniqueMaGiamGia());
        pgg.setTenGiamGia(dto.getTenGiamGia());
        pgg.setSoLuong(dto.getSoLuong());
        pgg.setLoaiGiam(dto.getLoaiGiam());

        pgg.setNgayBatDau(dto.getNgayBatDau());
        pgg.setNgayKetThuc(dto.getNgayKetThuc());

        pgg.setMoTa(dto.getMoTa());
        pgg.setNgayTao(LocalDateTime.now());
        pgg.setTrangThai(true);

        pgg.setDonHangToiThieu(dto.getDonHangToiThieu());
        pgg.setGiaTriGiamToiDa(dto.getGiaTriGiamToiDa());

        if (Boolean.TRUE.equals(dto.getLoaiGiam())) {
            pgg.setGiaTriPhanTram(dto.getGiaTriPhanTram());
            pgg.setGiaTriGiamToiDa(dto.getGiaTriGiamToiDa());
            pgg.setGiaTriTienMat(null);
        } else {
            pgg.setGiaTriTienMat(dto.getGiaTriTienMat());
            pgg.setGiaTriPhanTram(null);
            pgg.setGiaTriGiamToiDa(null);
        }

        pgg.setLoaiPhieu(dto.getLoaiPhieu());

        PhieuGiamGia saved = repo.save(pgg);

        // ✅ Nếu phiếu cá nhân thì lưu bảng join + gửi mail "bạn vừa nhận phiếu"
        if (Boolean.TRUE.equals(saved.getLoaiPhieu())) {

            var ids = dto.getKhachHangIds();
            if (ids == null || ids.isEmpty()) {
                throw new Exception("Phiếu cá nhân phải chọn ít nhất 1 khách hàng");
            }

            for (Long khId : ids) {
                KhachHang kh = khrepo.findById(khId)
                        .orElseThrow(() -> new Exception("Khách hàng không tồn tại: " + khId));

                PhieuGiamGiaCaNhan ct = new PhieuGiamGiaCaNhan();
                ct.setKhachHang(kh);
                ct.setPhieuGiamGia(saved);
                ct.setNgayNhan(LocalDateTime.now());
                ct.setDaSuDung(false);
                ct.setTrangThai(true);
                ct.setMaPhieuGiamGiaCaNhan("PGGCN-" + saved.getId() + "-" + kh.getId());

                PhieuGiamGiaCaNhan savedCt = cnrepo.save(ct);

                // ✅ Mail nhận phiếu
                try {
                    emailService.sendPersonalVoucherAssignedEmail(kh, saved, savedCt.getMaPhieuGiamGiaCaNhan());
                } catch (Exception ex) {
                    // tuỳ bạn: log thôi để không làm fail create
                    System.out.println("Send mail failed (create): " + ex.getMessage());
                }
            }
        }

        return saved;
    }

    @Override
    public PhieuGiamGia update(Long id, PhieuGiamGiaUpdateRequest dto) {
        Optional<PhieuGiamGia> pgg = repo.findById(id);
        if (!pgg.isPresent()) {
            throw new RuntimeException("Khong tim thay id: " + id);
        }
        PhieuGiamGia updatepgg = pgg.get();
        updatepgg.setTenGiamGia(dto.getTenGiamGia());
        updatepgg.setSoLuong(dto.getSoLuong());
        updatepgg.setLoaiGiam(dto.getLoaiGiam());
        updatepgg.setNgayBatDau(dto.getNgayBatDau());
        updatepgg.setNgayKetThuc(dto.getNgayKetThuc());
        updatepgg.setMoTa(dto.getMoTa());
        updatepgg.setNgayCapNhat(LocalDateTime.now());
        updatepgg.setGiaTriPhanTram(dto.getGiaTriPhanTram());
        updatepgg.setGiaTriTienMat(dto.getGiaTriTienMat());
        updatepgg.setLoaiPhieu(dto.getLoaiPhieu());
        updatepgg.setGiaTriGiamToiDa(dto.getGiaTriGiamToiDa());
        updatepgg.setDonHangToiThieu(dto.getDonHangToiThieu());
        return repo.save(updatepgg);
    }

    @Override
    public void delete(Long id) {
        PhieuGiamGia pgg = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay id:" + id));
        pgg.setTrangThai(false);
        pgg.setNgayCapNhat(LocalDateTime.now());
        repo.save(pgg);
    }
    @Transactional
    @Override
    public void startpgg(Long id) throws Exception {
        PhieuGiamGia pgg = repo.findById(id)
                .orElseThrow(() -> new Exception("Phiếu giảm giá không tồn tại"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = pgg.getNgayBatDau();
        LocalDateTime end = pgg.getNgayKetThuc();

        if (end != null && !now.isBefore(end)) throw new Exception("Phiếu đã kết thúc");

        // đang active rồi => return (không gửi mail)
        if (start != null && (now.isEqual(start) || now.isAfter(start))
                && (end == null || now.isBefore(end) || now.isEqual(end))) {
            return;
        }

        pgg.setNgayBatDau(now);
        if (end != null && end.isBefore(now)) pgg.setNgayKetThuc(now.plusMinutes(1));
        repo.save(pgg);

        // ✅ gửi mail chỉ khi là phiếu cá nhân
        if (Boolean.TRUE.equals(pgg.getLoaiPhieu())) {
            // dùng method fetch KH
            List<PhieuGiamGiaCaNhan> rows = cnrepo.findByPhieuGiamGia_IdAndTrangThaiTrue(pgg.getId());
            for (PhieuGiamGiaCaNhan row : rows) {
                KhachHang kh = row.getKhachHang(); // giờ chắc chắn có data
                emailService.sendPersonalVoucherStartedEmail(kh, pgg, row.getMaPhieuGiamGiaCaNhan());
            }
        }
    }
    @Transactional
    @Override
    public void endpgg(Long id) throws Exception {
        PhieuGiamGia pgg = repo.findById(id)
                .orElseThrow(() -> new Exception("Phiếu giảm giá không tồn tại"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = pgg.getNgayBatDau();
        LocalDateTime end = pgg.getNgayKetThuc();

        if (start != null && now.isBefore(start)) throw new Exception("Phiếu đang sắp diễn ra, không thể kết thúc ngay");
        if (end != null && !now.isBefore(end)) throw new Exception("Phiếu đã kết thúc");

        pgg.setNgayKetThuc(now);
        repo.save(pgg);

        if (Boolean.TRUE.equals(pgg.getLoaiPhieu())) {
            List<PhieuGiamGiaCaNhan> rows = cnrepo.findByPhieuGiamGia_IdAndTrangThaiTrue(pgg.getId());
            for (PhieuGiamGiaCaNhan row : rows) {
                emailService.sendPersonalVoucherEndedEmail(row.getKhachHang(), pgg, row.getMaPhieuGiamGiaCaNhan());
            }
        }
    }

    @Override
    public List<PhieuGiamGiaCaNhanResponse> getKhachHangNhanPhieu(Long pggId) {
        return cnrepo.findDsKhachHangNhanPhieu(pggId);
    }

    @Override
    public void updateKhachHangNhanPhieu(Long pggId, UpdateKhachHangNhanPhieuRequest req) throws Exception {
        PhieuGiamGia pgg = repo.findById(pggId)
                .orElseThrow(() -> new Exception("Phiếu giảm giá không tồn tại"));

        if (!Boolean.TRUE.equals(pgg.getLoaiPhieu())) {
            throw new Exception("Phiếu này không phải phiếu cá nhân");
        }

        var ids = req.getKhachHangIds();
        if (ids == null) ids = List.of();

        List<PhieuGiamGiaCaNhan> current = cnrepo.findByPhieuGiamGia_Id(pggId);

        java.util.Map<Long, PhieuGiamGiaCaNhan> map = new java.util.HashMap<>();
        for (PhieuGiamGiaCaNhan row : current) {
            if (row.getKhachHang() != null && row.getKhachHang().getId() != null) {
                map.put(row.getKhachHang().getId(), row);
            }
        }

        java.util.Set<Long> newSet = new java.util.HashSet<>(ids);

        // 1) REMOVE
        for (PhieuGiamGiaCaNhan row : current) {
            Long khId = row.getKhachHang() != null ? row.getKhachHang().getId() : null;
            if (khId == null) continue;

            boolean isActiveRow = Boolean.TRUE.equals(row.getTrangThai());
            boolean shouldKeep = newSet.contains(khId);

            if (isActiveRow && !shouldKeep) {
                if (Boolean.TRUE.equals(row.getDaSuDung())) {
                    throw new Exception("Không thể bỏ khách hàng id=" + khId + " vì đã sử dụng phiếu");
                }
                row.setTrangThai(false);
                cnrepo.save(row);
            }
        }

        // 2) ADD
        for (Long khId : newSet) {
            PhieuGiamGiaCaNhan existed = map.get(khId);

            if (existed != null) {
                // ✅ KH đã tồn tại sẵn => KHÔNG gửi mail
                if (!Boolean.TRUE.equals(existed.getTrangThai())) {
                    existed.setTrangThai(true);
                    cnrepo.save(existed);
                }
                continue;
            }

            // ✅ mới hoàn toàn => tạo record + gửi mail
            KhachHang kh = khrepo.findById(khId)
                    .orElseThrow(() -> new Exception("Khách hàng không tồn tại: " + khId));

            PhieuGiamGiaCaNhan ct = new PhieuGiamGiaCaNhan();
            ct.setKhachHang(kh);
            ct.setPhieuGiamGia(pgg);
            ct.setNgayNhan(LocalDateTime.now());
            ct.setDaSuDung(false);
            ct.setTrangThai(true);
            ct.setMaPhieuGiamGiaCaNhan("PGGCN-" + pgg.getId() + "-" + kh.getId());

            PhieuGiamGiaCaNhan savedCt = cnrepo.save(ct);

            try {
                emailService.sendPersonalVoucherAssignedEmail(kh, pgg, savedCt.getMaPhieuGiamGiaCaNhan());
            } catch (Exception ex) {
                System.out.println("Send mail failed (update add KH): " + ex.getMessage());
            }
        }
    }

    private String generateUniqueMaGiamGia() {
        for (int i = 0; i < 50; i++) {
            String code = "VC-" + randomAlphaNum(6);
            if (!repo.existsByMaGiamGia(code)) return code;
        }
        throw new RuntimeException("Không thể tạo mã giảm giá");
    }

    private String randomAlphaNum(int len) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(len);
        java.util.concurrent.ThreadLocalRandom r = java.util.concurrent.ThreadLocalRandom.current();
        for (int i = 0; i < len; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }
}
