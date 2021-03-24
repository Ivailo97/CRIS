package ehealth.cashregisterintegration.utils;

import ehealth.cashregisterintegration.data.dto.FreeSaleDTO;
import ehealth.cashregisterintegration.data.dto.ItemSaleDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SaleUtils {

    private static final String DEFAULT_OPERATOR_INFO = "-OP01-";

    public BigDecimal calculateTotal(FreeSaleDTO saleDTO) {
        BigDecimal total = new BigDecimal(0);
        for (ItemSaleDTO item : saleDTO.getItems()) {
            BigDecimal price = calculatePrice(item);
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);
        }
        return total;
    }

    public String formSaleNumber(String deviceName, FreeSaleDTO sale) {
        return deviceName + DEFAULT_OPERATOR_INFO + sale.getNumber().substring(2);
    }

    private BigDecimal calculatePrice(ItemSaleDTO item) {
        String discount = item.getPercent();
        BigDecimal multiplier = BigDecimal.valueOf(1);
        if (discount.startsWith("+")) {
            multiplier = new BigDecimal("1." + discount.substring(1)
                    .replace(".", ""));
        } else if (discount.startsWith("-")) {
            BigDecimal discountDecimal = new BigDecimal("0." + discount.substring(1)
                    .replace(".", ""));
            multiplier = new BigDecimal(1).subtract(discountDecimal);
        }
        return item.getPrice().multiply(multiplier);
    }
}
