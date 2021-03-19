package ehealth.cashregisterintegration.data.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class DeviceConfigDTO {

    @NotNull
    private String deviceName;

    @NotNull
    private String deviceLocation;

    @NotNull
    private String dirToListen;

    @NotNull
    private String astoreUrl;

    @NotNull
    private String astoreUsername;

    @NotNull
    private String astorePassword;

    @Valid
    private List<DepartmentDTO> departments;

    @Valid
    private List<ItemDTO> items;

}
