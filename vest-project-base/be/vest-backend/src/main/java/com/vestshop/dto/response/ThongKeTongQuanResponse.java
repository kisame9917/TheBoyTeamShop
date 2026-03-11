package com.vestshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThongKeTongQuanResponse {
    private ThongKeTongQuanCardResponse homNay;
    private ThongKeTongQuanCardResponse tuanNay;
    private ThongKeTongQuanCardResponse thangNay;
    private ThongKeTongQuanCardResponse namNay;
}