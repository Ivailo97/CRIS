package ehealth.cashregisterintegration.repository;

import ehealth.cashregisterintegration.data.model.TotalReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TotalReportRepository extends JpaRepository<TotalReport, Long> {

    Optional<TotalReport> findByDate(String date);

}
