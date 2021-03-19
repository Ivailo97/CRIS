package ehealth.cashregisterintegration.rest;

import ehealth.cashregisterintegration.data.dto.ItemDTO;
import ehealth.cashregisterintegration.data.rest.ApiResponse;
import ehealth.cashregisterintegration.data.dto.SaleDTO;
import ehealth.cashregisterintegration.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private static final String SALE_FAILED = "SALE FAILED";

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<Page<ItemDTO>> ls(@RequestParam("page") Integer page,
                                            @RequestParam("size") Integer size,
                                            @RequestParam("filter") String filter,
                                            @RequestParam("sort") String sortField,
                                            @RequestParam("dir") String direction) {

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortField);
        return ResponseEntity.ok(itemService.search(PageRequest.of(page, size, sort), filter));
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse> rm() {
        return ResponseEntity.ok(itemService.deleteAll());
    }

    @PostMapping("/sell")
    public ResponseEntity<ApiResponse> sell(@Valid @RequestBody SaleDTO sale,
                                            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .message(SALE_FAILED).build());
        }

        return ResponseEntity.ok(itemService.sell(sale));
    }
}
