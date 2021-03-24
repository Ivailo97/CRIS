package ehealth.cashregisterintegration.data.device.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@JsonNaming(value = PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class TotalSumData {
    private String Text1;
    private String Text2;
    private String Payment;
    private BigDecimal AmountIn;
}
