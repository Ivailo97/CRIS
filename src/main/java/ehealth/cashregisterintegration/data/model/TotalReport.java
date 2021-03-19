package ehealth.cashregisterintegration.data.model;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "total_reports")
@EqualsAndHashCode(callSuper = true)
public class TotalReport extends BaseEntity {

    @Column(nullable = false)
    private String operator;

    @Column(nullable = false, unique = true)
    private String date;

    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    private List<ItemReport> itemReports;

    @Column(nullable = false)
    private boolean dailySync;

}
