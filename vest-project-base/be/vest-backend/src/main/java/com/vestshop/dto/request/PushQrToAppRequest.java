package com.vestshop.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushQrToAppRequest {
    private String qrCode;
    private String qrNote;
}
