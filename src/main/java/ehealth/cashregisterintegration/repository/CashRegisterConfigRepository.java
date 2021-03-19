package ehealth.cashregisterintegration.repository;

import ehealth.cashregisterintegration.data.model.CashRegisterConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashRegisterConfigRepository extends JpaRepository<CashRegisterConfig, Long> {
}
