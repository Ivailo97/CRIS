package ehealth.cashregisterintegration.data.model;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Builder
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "sales")
@NoArgsConstructor
@AllArgsConstructor
public class Sale extends BaseEntity {

    @Column(nullable = false)
    private BigDecimal total;

    @Column(nullable = false)
    private Date date;

    @Column(nullable = false)
    private boolean sync;

    @OneToMany(cascade = CascadeType.ALL)
    private List<ItemSale> items;

}
