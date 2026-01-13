package com.vestshop.Entity;

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
@Table(name="loai_san_pham")
public class LoaiSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false)
    private Long id;

    @Column(name="ma", nullable=false, length=80)
    private String ma;

    @Column(name="ten", nullable=false, length=255)
    private String ten;

    @Column(name="mo_ta", length=500)
    private String moTa;

    @Column(name="trang_thai", nullable=false)
    private Boolean trangThai;

    @OneToMany(mappedBy = "loaiSanPham", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SanPham> sanPhams = new ArrayList<>();

}
