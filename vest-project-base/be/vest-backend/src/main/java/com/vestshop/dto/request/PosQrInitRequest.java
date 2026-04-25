package com.vestshop.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PosQrInitRequest {
    private String source; // optional, ví dụ "POS"
}