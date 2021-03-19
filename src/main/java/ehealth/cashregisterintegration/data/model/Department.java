package ehealth.cashregisterintegration.data.model;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Builder
@Table(name = "departments")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Department extends BaseEntity {

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String taxGroup;

    @Column(nullable = false)
    private Integer maxDigits;

    @OneToMany(mappedBy = "department",cascade = CascadeType.ALL)
    private List<Item> items;

}
