package ehealth.cashregisterintegration.data.dto;

import lombok.Data;

@Data
public class DeviceDTO {
    private String deviceName;
    private String deviceLocation;
    private String dirToListen;
    private String astoreUrl;
}
