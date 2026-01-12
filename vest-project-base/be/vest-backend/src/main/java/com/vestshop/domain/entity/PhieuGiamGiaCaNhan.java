package com.vestshop.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name="Phieu_giam_gia_ca_nhan")
public class PhieuGiamGiaCaNhan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_khach_hang", nullable=false)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_phieu_giam_gia", nullable=false)
    private PhieuGiamGia phieuGiamGia;

    @Column(name="ma_phieu_giam_gia_ca_nhan", nullable=false, length=80)
    private String maPhieuGiamGiaCaNhan;

    @Column(name="ngay_nhan", nullable=false)
    private LocalDateTime ngayNhan;

    @Column(name="da_su_dung", nullable=false)
    private Boolean daSuDung;

    @Column(name="trang_thai", nullable=false)
    private Integer trangThai;

}
