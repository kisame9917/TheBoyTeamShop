package com.vestshop.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.*;
import lombok.*;
@Entity
@Table(name="phuong_thuc_thanh_toan")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PhuongThucThanhToan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="ma_phuong_thuc_thanh_toan", nullable=false, length=80)
    private String maPhuongThucThanhToan;

    @Column(name="ten_phuong_thuc_thanh_toan", nullable=false, length=255)
    private String tenPhuongThucThanhToan;

    @Column(name="nha_cung_cap", length=255)
    private String nhaCungCap;

    // DB: hinh_thuc int NOT NULL
    @Column(name="hinh_thuc", nullable=false)
    private Integer hinhThuc;

    @Column(name="trang_thai", nullable=false)
    private Boolean trangThai;

    @OneToMany(mappedBy = "phuongThucThanhToan", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.List<GiaoDichThanhToan> giaoDichThanhToans = new java.util.ArrayList<>();

    // (Nếu bạn muốn map lịch sử thanh toán từ pttt -> lstt thì thêm cái này, KHÔNG tạo cột mới)
    // @OneToMany(mappedBy = "phuongThucThanhToan", fetch = FetchType.LAZY)
    // @JsonIgnore
    // private List<LichSuThanhToan> lichSuThanhToans = new ArrayList<>();
}