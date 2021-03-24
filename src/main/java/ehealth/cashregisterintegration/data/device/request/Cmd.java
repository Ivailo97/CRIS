package ehealth.cashregisterintegration.data.device.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonNaming(value = PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class Cmd {
    private String ComPortName;
    private ReqCommandWrapper[] COMPortMsgList;
}
