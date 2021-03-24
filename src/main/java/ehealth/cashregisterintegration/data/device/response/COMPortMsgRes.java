package ehealth.cashregisterintegration.data.device.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import ehealth.cashregisterintegration.data.device.request.ReqCommand;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonNaming(value = PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class COMPortMsgRes {
    private String Res;
    private boolean HasErr;
    private ReqCommand ReqCommandRes;
    private ResCommand ResCommand;
}
