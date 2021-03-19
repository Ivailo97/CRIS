package ehealth.cashregisterintegration.service;

import ehealth.cashregisterintegration.data.dto.DeviceConfigDTO;
import ehealth.cashregisterintegration.data.dto.DeviceDTO;
import ehealth.cashregisterintegration.data.rest.ApiResponse;

public interface CashRegisterConfigService {

    ApiResponse setConfig(DeviceConfigDTO config);

    DeviceDTO getDevice();

}
