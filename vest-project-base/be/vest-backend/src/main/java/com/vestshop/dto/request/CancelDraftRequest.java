package com.vestshop.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelDraftRequest {
    private String reason;
    private List<Item> items;

    @Data
    public static class Item {
        private Long idSanPhamChiTiet;
        private Integer soLuong;
    }
}
