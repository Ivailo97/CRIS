package ehealth.cashregisterintegration.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "item_sales")
public class ItemSale extends BaseEntity {

    @Column(nullable = false)
    private String percent;

    @Column(nullable = false)
    private String text1;

    @Column(nullable = false)
    private String taxGroup;

    private String text2;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

}
