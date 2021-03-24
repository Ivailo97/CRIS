package ehealth.cashregisterintegration.data.device.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonNaming(value = PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class StartFiscRcpData {
    private Integer Operator;
    private Integer Password;
    private String UnicSaleNum;
    private String Invoice;
    private String Refund;
}
