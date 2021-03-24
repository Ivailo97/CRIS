package ehealth.cashregisterintegration.data.device.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(value = PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class FreeSaleData {
    private String Text1;
    private String Text2;
    private String TaxGrp;
    private String Sign;
    private BigDecimal Price;
    private Integer Qty;
    private String Percent;  // percent can be in range -99.99 to +99.99
    private Integer Netto;
}
