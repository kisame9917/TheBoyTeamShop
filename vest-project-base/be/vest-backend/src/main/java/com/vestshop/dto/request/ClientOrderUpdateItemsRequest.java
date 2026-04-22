package com.vestshop.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClientOrderUpdateItemsRequest {

    private List<Item> items;

    @Getter
    @Setter
    public static class Item {
        private Long idSanPhamChiTiet;
        private Integer soLuong;
    }
}
