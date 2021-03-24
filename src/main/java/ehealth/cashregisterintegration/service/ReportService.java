package ehealth.cashregisterintegration.service;

import ehealth.cashregisterintegration.data.dto.FreeSaleDTO;
import ehealth.cashregisterintegration.data.model.CashRegisterConfig;

public interface ReportService {

    void listenForTotalReports(String delimiter, Boolean quotes);

    void saveAndSendSaleInfo(CashRegisterConfig config, FreeSaleDTO sale);

}
