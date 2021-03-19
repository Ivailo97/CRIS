package ehealth.cashregisterintegration.config;

import ehealth.cashregisterintegration.data.dto.ItemDTO;
import ehealth.cashregisterintegration.data.model.Item;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

public class ItemConverter implements Converter<Item, ItemDTO> {
    @Override
    public ItemDTO convert(MappingContext<Item, ItemDTO> mappingContext) {
        Item source = mappingContext.getSource();
        return ItemDTO.builder()
                .department(source.getDepartment().getNumber())
                .name(source.getName())
                .price(source.getPrice())
                .quantity(source.getQuantity())
                .taxGroup(source.getTaxGroup())
                .number(source.getNumber())
                .barcode(source.getBarcode())
                .build();
    }
}
