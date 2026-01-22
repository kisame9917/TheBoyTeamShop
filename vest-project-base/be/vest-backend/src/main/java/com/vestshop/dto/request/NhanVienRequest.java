package com.vestshop.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhanVienRequest {

    @NotNull
    private Long quyenHanId;

    @NotBlank
    @Size(max = 80)
    private String maNhanVien;

    @NotBlank
    @Size(max = 255)
    private String tenNhanVien;

    @Size(max = 20)
    private String soDienThoai;

    @Size(max = 20)
    private String cccd;

    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(max = 80)
    private String taiKhoan;

    @Size(max = 255)
    private String matKhau;

    private LocalDate ngaySinh;
    private Boolean gioiTinh;

    @Size(max = 255)
    private String diaChi;

    private Boolean trangThai;

    @Column(name="anh_dai_dien", length=500)
    private String anhDaiDien;
}

