package com.vestshop.dto.request;

import lombok.Data;

@Data
public class PushQrToAppRequest {
    private String qrCode;
    private String message;
}
