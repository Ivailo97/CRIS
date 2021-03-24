package ehealth.cashregisterintegration.service;

import ehealth.cashregisterintegration.data.device.response.DaisyResponse;
import ehealth.cashregisterintegration.data.dto.ApiResponseDTO;
import ehealth.cashregisterintegration.data.dto.DeviceConfigDTO;
import ehealth.cashregisterintegration.data.dto.FreeSaleDTO;

public interface DeviceService {

    void loadConfig();

    ApiResponseDTO setConfig(DeviceConfigDTO config);

    DaisyResponse sell(FreeSaleDTO sale);

    DaisyResponse zDailyReport();

    DaisyResponse paperFeed(Integer count);

}
