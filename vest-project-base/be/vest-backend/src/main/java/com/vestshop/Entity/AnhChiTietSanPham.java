package com.vestshop.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name="anh_chi_tiet_san_pham")
public class AnhChiTietSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_san_pham_chi_tiet", nullable=false)
    private SanPhamChiTiet sanPhamChiTiet;

    @Column(name="ma", nullable=false, length=80)
    private String ma;

    @Column(name="ten", nullable=false, length=500)
    private String ten;

    @Column(name="trang_thai", nullable=false)
    private Integer trangThai;

}
