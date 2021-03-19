package ehealth.cashregisterintegration.repository;

import ehealth.cashregisterintegration.data.model.ItemReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemReportRepository extends JpaRepository<ItemReport, Long> {
}
