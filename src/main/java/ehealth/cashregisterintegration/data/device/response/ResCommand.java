package ehealth.cashregisterintegration.data.device.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonNaming(value = PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class ResCommand {
    private String Cmd;
    private Object CmdData;
    private boolean IsValid;
    private boolean IsPackedMsg;
    private Integer ErrorCode;
    private FDState FDState;
    private Integer SeqNo;
}
