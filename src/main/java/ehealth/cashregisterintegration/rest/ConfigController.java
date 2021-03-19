package ehealth.cashregisterintegration.rest;

import ehealth.cashregisterintegration.data.dto.DeviceConfigDTO;
import ehealth.cashregisterintegration.data.dto.DeviceDTO;
import ehealth.cashregisterintegration.data.rest.ApiResponse;
import ehealth.cashregisterintegration.service.CashRegisterConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin("*")
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigController {

    private static final String FAILED = "Configuration failed";

    private final CashRegisterConfigService service;

    @GetMapping("/device")
    public ResponseEntity<DeviceDTO> device() {
        return ResponseEntity.ok(service.getDevice());
    }

    @PostMapping("/device")
    public ResponseEntity<ApiResponse> configure(@Valid @RequestBody DeviceConfigDTO config,
                                                 BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .message(FAILED).build());
        }

        return ResponseEntity.ok(service.setConfig(config));
    }

}
