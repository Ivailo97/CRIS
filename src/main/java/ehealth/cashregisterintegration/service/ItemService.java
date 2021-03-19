package ehealth.cashregisterintegration.service;

import ehealth.cashregisterintegration.data.dto.ItemDTO;
import ehealth.cashregisterintegration.data.rest.ApiResponse;
import ehealth.cashregisterintegration.data.dto.SaleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemService {

    Page<ItemDTO> search(Pageable pageable, String filter);

    ApiResponse deleteAll();

    ApiResponse sell(SaleDTO sale);

}
