package ehealth.cashregisterintegration.rest;

import ehealth.cashregisterintegration.data.device.response.DaisyResponse;
import ehealth.cashregisterintegration.data.dto.ApiResponseDTO;
import ehealth.cashregisterintegration.data.dto.DeviceConfigDTO;
import ehealth.cashregisterintegration.data.dto.FreeSaleDTO;
import ehealth.cashregisterintegration.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin("*")
@RestController
@RequestMapping("/daisy")
@RequiredArgsConstructor
public class DeviceController {

    private static final String CONFIG_FAIL = "Invalid configuration";
    private static final String SELL_FAIL = "Invalid sell";

    private final DeviceService deviceService;

    @PostMapping("/device")
    public ResponseEntity<ApiResponseDTO> configure(@Valid @RequestBody DeviceConfigDTO config,
                                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException(CONFIG_FAIL);
        }
        return ResponseEntity.ok(deviceService.setConfig(config));
    }

    @PostMapping("/line/{count}")
    public ResponseEntity<DaisyResponse> lineFeed(@PathVariable Integer count) {
        return ResponseEntity.ok(deviceService.paperFeed(count));
    }

    @PostMapping("/daily")
    public ResponseEntity<DaisyResponse> dailyReport() {
        return ResponseEntity.ok(deviceService.zDailyReport());
    }

    @PostMapping("/sell")
    public ResponseEntity<DaisyResponse> freeSale(@Valid @RequestBody FreeSaleDTO sale,
                                                  BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException(SELL_FAIL);
        }
        return ResponseEntity.ok(deviceService.sell(sale));
    }

}
