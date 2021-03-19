package ehealth.cashregisterintegration.service;

import ehealth.cashregisterintegration.data.dto.SaleDTO;

public interface ReportService {

    void listenForTotalReports(String delimiter, Boolean quotes);

    void sendSellInfo(SaleDTO sale);

}
