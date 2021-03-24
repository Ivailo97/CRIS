package ehealth.cashregisterintegration.data.model;

import lombok.*;

import javax.persistence.*;

@Builder
@Data
@Entity
@Table(name = "configurations")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CashRegisterConfig extends BaseEntity {

    @Column(nullable = false)
    private String deviceName;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String dirToListen;

    @Column(nullable = false)
    private String accountingServiceUrl;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private Credentials credentials;

}
