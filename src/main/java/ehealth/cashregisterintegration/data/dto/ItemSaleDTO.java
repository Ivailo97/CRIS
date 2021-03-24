package ehealth.cashregisterintegration.data.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
public class ItemSaleDTO {

    @NotNull
    @Pattern(regexp = "^([-|+][1-9][0-9]?.[0-9][0-9]?|0)$") //only from -99.99 to +99.99 or 0
    private String percent;

    @NotEmpty
    @NotNull
    private String text1;

    private String taxGroup;

    private String text2;

    @NotNull
    private BigDecimal price;

//    @NotNull
//    private BigDecimal amountIn;

    @NotNull
    private Integer quantity;
}
