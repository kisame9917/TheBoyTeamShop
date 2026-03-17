package com.vestshop.dto.request;

import java.math.BigDecimal;
import java.util.List;

public class OnlineCheckoutRequest {

    private String maHoaDon;
    private Boolean loaiDon;
    private BigDecimal phiVanChuyen;

    private Long idKhachHang;
    private String tenKhachHang;
    private String soDienThoai;
    private String emailKhachHang;
    private String diaChiKhachHang;

    private Long idPhieuGiamGia;
    private Integer giamThuCongPercent;
    private BigDecimal paid;
    private String ghiChu;

    private String paymentMethod;
    private String paymentGateway;
    private String maGiaoDich;
    private String ghiChuThanhToan;

    private String tenNguoiNhanHang;
    private String soDienThoaiNhanHang;
    private String tinhThanhNhanHang;
    private String phuongXaNhanHang;
    private String quanHuyenNhanHang;
    private String diaChiNhanHangChiTiet;

    private BigDecimal tongTien;
    private BigDecimal tongTienGiam;
    private BigDecimal tongTienSauGiam;

    private List<OnlineCheckoutItemRequest> items;

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public Boolean getLoaiDon() {
        return loaiDon;
    }

    public void setLoaiDon(Boolean loaiDon) {
        this.loaiDon = loaiDon;
    }

    public BigDecimal getPhiVanChuyen() {
        return phiVanChuyen;
    }

    public void setPhiVanChuyen(BigDecimal phiVanChuyen) {
        this.phiVanChuyen = phiVanChuyen;
    }

    public Long getIdKhachHang() {
        return idKhachHang;
    }

    public void setIdKhachHang(Long idKhachHang) {
        this.idKhachHang = idKhachHang;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmailKhachHang() {
        return emailKhachHang;
    }

    public void setEmailKhachHang(String emailKhachHang) {
        this.emailKhachHang = emailKhachHang;
    }

    public String getDiaChiKhachHang() {
        return diaChiKhachHang;
    }

    public void setDiaChiKhachHang(String diaChiKhachHang) {
        this.diaChiKhachHang = diaChiKhachHang;
    }

    public Long getIdPhieuGiamGia() {
        return idPhieuGiamGia;
    }

    public void setIdPhieuGiamGia(Long idPhieuGiamGia) {
        this.idPhieuGiamGia = idPhieuGiamGia;
    }

    public Integer getGiamThuCongPercent() {
        return giamThuCongPercent;
    }

    public void setGiamThuCongPercent(Integer giamThuCongPercent) {
        this.giamThuCongPercent = giamThuCongPercent;
    }

    public BigDecimal getPaid() {
        return paid;
    }

    public void setPaid(BigDecimal paid) {
        this.paid = paid;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
    public String getPaymentGateway() {
        return paymentGateway;
    }

    public void setPaymentGateway(String paymentGateway) {
        this.paymentGateway = paymentGateway;
    }
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getMaGiaoDich() {
        return maGiaoDich;
    }

    public void setMaGiaoDich(String maGiaoDich) {
        this.maGiaoDich = maGiaoDich;
    }

    public String getGhiChuThanhToan() {
        return ghiChuThanhToan;
    }

    public void setGhiChuThanhToan(String ghiChuThanhToan) {
        this.ghiChuThanhToan = ghiChuThanhToan;
    }

    public String getTenNguoiNhanHang() {
        return tenNguoiNhanHang;
    }

    public void setTenNguoiNhanHang(String tenNguoiNhanHang) {
        this.tenNguoiNhanHang = tenNguoiNhanHang;
    }

    public String getSoDienThoaiNhanHang() {
        return soDienThoaiNhanHang;
    }

    public void setSoDienThoaiNhanHang(String soDienThoaiNhanHang) {
        this.soDienThoaiNhanHang = soDienThoaiNhanHang;
    }

    public String getTinhThanhNhanHang() {
        return tinhThanhNhanHang;
    }

    public void setTinhThanhNhanHang(String tinhThanhNhanHang) {
        this.tinhThanhNhanHang = tinhThanhNhanHang;
    }

    public String getPhuongXaNhanHang() {
        return phuongXaNhanHang;
    }

    public void setPhuongXaNhanHang(String phuongXaNhanHang) {
        this.phuongXaNhanHang = phuongXaNhanHang;
    }

    public String getQuanHuyenNhanHang() {
        return quanHuyenNhanHang;
    }

    public void setQuanHuyenNhanHang(String quanHuyenNhanHang) {
        this.quanHuyenNhanHang = quanHuyenNhanHang;
    }

    public String getDiaChiNhanHangChiTiet() {
        return diaChiNhanHangChiTiet;
    }

    public void setDiaChiNhanHangChiTiet(String diaChiNhanHangChiTiet) {
        this.diaChiNhanHangChiTiet = diaChiNhanHangChiTiet;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public BigDecimal getTongTienGiam() {
        return tongTienGiam;
    }

    public void setTongTienGiam(BigDecimal tongTienGiam) {
        this.tongTienGiam = tongTienGiam;
    }

    public BigDecimal getTongTienSauGiam() {
        return tongTienSauGiam;
    }

    public void setTongTienSauGiam(BigDecimal tongTienSauGiam) {
        this.tongTienSauGiam = tongTienSauGiam;
    }

    public List<OnlineCheckoutItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OnlineCheckoutItemRequest> items) {
        this.items = items;
    }
}