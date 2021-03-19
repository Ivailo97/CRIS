package ehealth.cashregisterintegration.data.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class SaleDTO {

    @NotNull
    private Integer itemNumber;

    @NotNull
    private Integer itemQuantity;

    @NotNull
    private BigDecimal totalSum;

    @NotNull
    private String date;
}
