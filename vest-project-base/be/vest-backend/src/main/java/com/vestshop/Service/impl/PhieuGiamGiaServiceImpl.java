package com.vestshop.Service.impl;

import com.vestshop.Entity.KhachHang;
import com.vestshop.Entity.PhieuGiamGia;
import com.vestshop.Entity.PhieuGiamGiaCaNhan;
import com.vestshop.Repository.KhachHangRepository;
import com.vestshop.Repository.PhieuGiamGiaCaNhanRepository;
import com.vestshop.Repository.PhieuGiamGiaRepository;
import com.vestshop.Service.PhieuGiamGiaService;
import com.vestshop.dto.request.PhieuGiamGiaCreateRequest;
import com.vestshop.dto.request.PhieuGiamGiaUpdateRequest;
import com.vestshop.dto.request.UpdateKhachHangNhanPhieuRequest;
import com.vestshop.dto.response.PhieuGiamGiaCaNhanResponse;
import com.vestshop.dto.response.PhieuGiamGiaDetailResponse;
import com.vestshop.dto.response.PhieuGiamGiaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

        // DateTime: FE gửi 00:00:00 và 23:59:59 rồi
        pgg.setNgayBatDau(dto.getNgayBatDau());
        pgg.setNgayKetThuc(dto.getNgayKetThuc());

        pgg.setMoTa(dto.getMoTa());
        pgg.setNgayTao(LocalDateTime.now());
        pgg.setTrangThai(true);

        pgg.setDonHangToiThieu(dto.getDonHangToiThieu());
        pgg.setGiaTriGiamToiDa(dto.getGiaTriGiamToiDa());

        if (Boolean.TRUE.equals(dto.getLoaiGiam())) {
            // giảm %
            pgg.setGiaTriPhanTram(dto.getGiaTriPhanTram());
            pgg.setGiaTriGiamToiDa(dto.getGiaTriGiamToiDa());
            pgg.setGiaTriTienMat(null);
        } else {
            // giảm tiền
            pgg.setGiaTriTienMat(dto.getGiaTriTienMat());
            pgg.setGiaTriPhanTram(null);
            pgg.setGiaTriGiamToiDa(null);
        }

        // true = CA_NHAN, false = CONG_KHAI (theo FE bạn đang gửi)
        pgg.setLoaiPhieu(dto.getLoaiPhieu());

        // ✅ Lưu phiếu trước để có ID
        PhieuGiamGia saved = repo.save(pgg);

        // ✅ Nếu phiếu cá nhân thì lưu bảng join
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

                // ✅ tạo mã riêng cho dòng cá nhân
                ct.setMaPhieuGiamGiaCaNhan("PGGCN-" + saved.getId() + "-" + kh.getId());

                cnrepo.save(ct);
            }
        }

        return saved;
    }

    @Override
    public PhieuGiamGia update(Long id, PhieuGiamGiaUpdateRequest dto)  {
        Optional<PhieuGiamGia> pgg = repo.findById(id);
        if(!pgg.isPresent()){
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
//        updatepgg.setTrangThai(dto.getTrangThai());
        return repo.save(updatepgg);

    }

    @Override
    public void delete(Long id) {
        PhieuGiamGia pgg = repo.findById(id).orElseThrow(()-> new RuntimeException("Khong tim thay id:" +id));
        pgg.setTrangThai(false);
        pgg.setNgayCapNhat(LocalDateTime.now());
        repo.save(pgg);
    }
    @Override
    public void startpgg(Long id) throws Exception{
        PhieuGiamGia pgg = repo.findById(id)
                .orElseThrow(() -> new Exception("Phiếu giảm giá không tồn tại"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = pgg.getNgayBatDau();
        LocalDateTime end = pgg.getNgayKetThuc();

        // nếu đã kết thúc rồi thì không cho start
        if (end != null && now.isAfter(end)) {
            throw new Exception("Phiếu đã kết thúc");
        }

        // nếu đã đang áp dụng rồi thì thôi
        if (start != null && (now.isEqual(start) || now.isAfter(start)) && (end == null || now.isBefore(end) || now.isEqual(end))) {
            return;
        }

        // ✅ bắt đầu ngay
        pgg.setNgayBatDau(now);

        // (tuỳ bạn) nếu end < now (do dữ liệu bẩn) thì set end = now + 1 phút
        if (end != null && end.isBefore(now)) {
            pgg.setNgayKetThuc(now.plusMinutes(1));
        }

        repo.save(pgg);
    }


    @Override
        public void endpgg(Long id) throws Exception {
            PhieuGiamGia pgg = repo.findById(id)
                    .orElseThrow(() -> new Exception("Phiếu giảm giá không tồn tại"));

            // (tùy bạn) chặn nếu chưa đang áp dụng
            LocalDateTime today = LocalDateTime.now();
            LocalDateTime start = pgg.getNgayBatDau();
            LocalDateTime end = pgg.getNgayKetThuc();

            // UPCOMING => không cho kết thúc ngay (bạn có thể bỏ đoạn này nếu muốn)
            if (start != null && today.isBefore(start)) {
                throw new Exception("Phiếu đang sắp diễn ra, không thể kết thúc ngay");
            }
            // EXPIRED rồi => bỏ qua hoặc báo lỗi
            if (end != null && today.isAfter(end)) {
                throw new Exception("Phiếu đã kết thúc");
            }

            // ✅ Kết thúc ngay: set end = today
            pgg.setNgayKetThuc(today);

            repo.save(pgg);
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

        // Lấy tất cả dòng hiện có (kể cả trangThai=false để xử lý add lại)
        List<PhieuGiamGiaCaNhan> current = cnrepo.findByPhieuGiamGia_Id(pggId);

        // Map theo khId
        java.util.Map<Long, PhieuGiamGiaCaNhan> map = new java.util.HashMap<>();
        for (PhieuGiamGiaCaNhan row : current) {
            if (row.getKhachHang() != null && row.getKhachHang().getId() != null) {
                map.put(row.getKhachHang().getId(), row);
            }
        }

        java.util.Set<Long> newSet = new java.util.HashSet<>(ids);

        // 1) Xử lý REMOVE (những KH hiện đang active nhưng không còn nằm trong newSet)
        for (PhieuGiamGiaCaNhan row : current) {
            Long khId = row.getKhachHang() != null ? row.getKhachHang().getId() : null;
            if (khId == null) continue;

            boolean isActiveRow = Boolean.TRUE.equals(row.getTrangThai());
            boolean shouldKeep = newSet.contains(khId);

            if (isActiveRow && !shouldKeep) {
                if (Boolean.TRUE.equals(row.getDaSuDung())) {
                    throw new Exception("Không thể bỏ khách hàng id=" + khId + " vì đã sử dụng phiếu");
                }
                row.setTrangThai(false); // soft delete
                cnrepo.save(row);
            }
        }

        // 2) Xử lý ADD (những KH mới trong newSet nhưng chưa có active)
        for (Long khId : newSet) {
            PhieuGiamGiaCaNhan existed = map.get(khId);

            if (existed != null) {
                // Nếu đã tồn tại nhưng đang bị tắt -> bật lại
                if (!Boolean.TRUE.equals(existed.getTrangThai())) {
                    existed.setTrangThai(true);
                    cnrepo.save(existed);
                }
                continue;
            }

            KhachHang kh = khrepo.findById(khId)
                    .orElseThrow(() -> new Exception("Khách hàng không tồn tại: " + khId));

            PhieuGiamGiaCaNhan ct = new PhieuGiamGiaCaNhan();
            ct.setKhachHang(kh);
            ct.setPhieuGiamGia(pgg);
            ct.setNgayNhan(LocalDateTime.now());
            ct.setDaSuDung(false);
            ct.setTrangThai(true);
            ct.setMaPhieuGiamGiaCaNhan("PGGCN-" + pgg.getId() + "-" + kh.getId());

            cnrepo.save(ct);
        }
    }


    private String generateUniqueMaGiamGia() {
        // ví dụ: VCH-8K2P9A
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
