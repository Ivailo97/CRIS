package ehealth.cashregisterintegration.data.device.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonNaming(value = PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class FDState {
    private Integer NoExtDisplay;
    private Integer PrintAllowed;
    private Integer TaxRatesActive;
    private Integer SerialActive;
}
