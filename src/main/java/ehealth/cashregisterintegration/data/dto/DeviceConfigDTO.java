package ehealth.cashregisterintegration.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceConfigDTO {

    @NotNull
    @NotEmpty
    private String deviceName;

    @NotNull
    @NotEmpty
    private String location;

    @NotNull
    @NotEmpty
    private String dirToListen;

    @NotNull
    @NotEmpty
    private String accountingServiceUrl;

    @Valid
    private CredentialsDTO credentials;

}
