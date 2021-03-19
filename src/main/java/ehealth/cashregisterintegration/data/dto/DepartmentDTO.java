package ehealth.cashregisterintegration.data.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class DepartmentDTO {

    @NotNull
    @Min(1)
    @Max(50)
    private Integer number;

    @NotNull
    @Length(max = 20)
    private String name;

    @Pattern(regexp = "^([АБВГДЕЖЗ])$")
    private String taxGroup;

    @NotNull
    @Min(0)
    @Max(9)
    private Integer maxDigits;
}
