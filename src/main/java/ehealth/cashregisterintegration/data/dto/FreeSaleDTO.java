package ehealth.cashregisterintegration.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class FreeSaleDTO {

    @NotNull
    @NotEmpty
    private String number;

    @NotNull
    @NotEmpty
    private String date;

    @Valid
    @Size(min = 1)
    private ItemSaleDTO[] items;

}


