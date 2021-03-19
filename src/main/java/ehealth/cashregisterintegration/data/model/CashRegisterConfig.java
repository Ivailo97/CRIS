package ehealth.cashregisterintegration.data.model;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Builder
@Data
@Entity
@Table(name = "configurations")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CashRegisterConfig extends BaseEntity {

    @Column(nullable = false)
    private String dirToListen;

    @Column(nullable = false)
    private String deviceName;

    @Column(nullable = false)
    private String deviceLocation;

    @Column(nullable = false)
    private String astoreUrl;

    @Column(nullable = false)
    private String astoreUsername;

    @Column(nullable = false)
    private String astorePassword;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Department> departments;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cashRegister")
    private List<Item> items;

}
