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
@Table(name="quyen_han")
public class QuyenHan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false)
    private Long id;

    @Column(name="ma_quyen_han", nullable=false, length=80)
    private String maQuyenHan;

    @Column(name="ten_quyen_han", nullable=false, length=255)
    private String tenQuyenHan;

    @Column(name="trang_thai", nullable=false)
    private Integer trangThai;

    @OneToMany(mappedBy = "quyenHan", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<NhanVien> nhanViens = new ArrayList<>();

}
