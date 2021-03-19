package ehealth.cashregisterintegration.data.dto;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

@Builder
@Data
public class ItemDTO {

    @NotNull
    @Min(1)
    @Max(30000)
    private Integer number;

    @NotNull
    @Length(max = 20)
    private String name;

    @NotNull
    @Max(999999999)
    @Min(0)
    private BigDecimal price;

    @NotNull
    @Max(999999)
    @Min(0)
    private Integer quantity;

    //if 0 the item is not for sale
    @NotNull
    @Min(0)
    @Max(50)
    private Integer department;

    @Pattern(regexp = "^([АБВГ])$")
    private String taxGroup;

//    @Pattern(regexp = "^$|^(EAN8|EAN13|PC‐A|UPC‐B|тегловен)$")
    private String barcode;

}
