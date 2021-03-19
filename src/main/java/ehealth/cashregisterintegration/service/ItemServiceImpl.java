package ehealth.cashregisterintegration.service;

import ehealth.cashregisterintegration.config.ItemConverter;
import ehealth.cashregisterintegration.data.dto.ItemDTO;
import ehealth.cashregisterintegration.data.model.Sale;
import ehealth.cashregisterintegration.data.rest.ApiResponse;
import ehealth.cashregisterintegration.data.dto.SaleDTO;
import ehealth.cashregisterintegration.repository.ItemRepository;
import ehealth.cashregisterintegration.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final ModelMapper mapper;

    private final ItemConverter itemConverter;

    private final ReportService reportService;

    private final SaleRepository saleRepository;

    @Override
    public Page<ItemDTO> search(Pageable pageable, String filter) {
        return itemRepository.findByNameContaining(filter, pageable).map(x -> {
            mapper.addConverter(itemConverter);
            return mapper.map(x, ItemDTO.class);
        });
    }

    @Override
    public ApiResponse deleteAll() {
        this.itemRepository.deleteAll();
        return ApiResponse.builder().message("Deleted successful").build();
    }

    @Override
    public ApiResponse sell(SaleDTO sale) {
        AtomicReference<String> message = new AtomicReference<>();

        this.itemRepository.findByNumber(sale.getItemNumber())
                .ifPresentOrElse(
                        item -> {
                            if (!item.getQuantity().equals(0) && item.getQuantity() - sale.getItemQuantity() >= 0) {
                                item.setQuantity(item.getQuantity() - sale.getItemQuantity());
                                itemRepository.save(item);

                                Sale entity = mapper.map(sale, Sale.class);
                                entity.setDate(Date.from(Instant.now()));
                                entity.setSync(false);
                                saleRepository.save(entity);


                                message.set("Sale successful");
                                reportService.sendSellInfo(sale);
                            } else {
                                message.set("Out of stock");
                            }
                        },
                        () -> message.set("Invalid item number"));

        return ApiResponse.builder().message(message.get()).build();
    }
}
