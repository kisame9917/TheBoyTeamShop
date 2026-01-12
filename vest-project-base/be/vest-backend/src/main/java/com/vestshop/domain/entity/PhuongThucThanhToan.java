package com.vestshop.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name="phuong_thuc_thanh_toan")
public class PhuongThucThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false)
    private Long id;

    @Column(name="ma_phuong_thuc_thanh_toan", nullable=false, length=80)
    private String maPhuongThucThanhToan;

    @Column(name="ten_phuong_thuc_thanh_toan", nullable=false, length=255)
    private String tenPhuongThucThanhToan;

    @Column(name="nha_cung_cap", length=255)
    private String nhaCungCap;

    @Column(name="trang_thai", nullable=false)
    private Integer trangThai;

    @OneToMany(mappedBy = "phuongThucThanhToan", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<GiaoDichThanhToan> giaoDichThanhToans = new ArrayList<>();

}
