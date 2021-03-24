package ehealth.cashregisterintegration.repository;

import ehealth.cashregisterintegration.data.model.listen.DailyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {

    Optional<DailyReport> findByDate(String date);

}
