package ehealth.cashregisterintegration.utils;

import ehealth.cashregisterintegration.data.model.CashRegisterConfig;
import ehealth.cashregisterintegration.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InsertListener implements PostCommitInsertEventListener {

    private static final String DELIMITER = ", ";

    private final ReportService reportService;

    @Override
    public void onPostInsertCommitFailed(PostInsertEvent event) {
        log.error("Persisting failed for: " + event.getEntity());
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        if (event.getEntity() instanceof CashRegisterConfig) {
            System.out.println("registered a device");
//            reportService.listenForTotalReports(DELIMITER, true);
        }
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return true;
    }
}
