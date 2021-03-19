package ehealth.cashregisterintegration.data.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Builder
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "sales")
@NoArgsConstructor
@AllArgsConstructor
public class Sale extends BaseEntity {

    @Column(nullable = false)
    private Integer itemNumber;

    @Column(nullable = false)
    private Integer itemQuantity;

    @Column(nullable = false)
    private Integer totalSum;

    @Column(nullable = false)
    private Boolean sync;

    @Column(nullable = false)
    private Date date;
}
