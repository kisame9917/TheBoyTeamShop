package com.vestshop.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientOrderCancelRequest {
    private String lyDo;
    private String ghiChu;
}