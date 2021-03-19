package ehealth.cashregisterintegration.repository;

import ehealth.cashregisterintegration.data.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends PagingAndSortingRepository<Item, Long> {

    Page<Item> findByNameContaining(String filter, Pageable pageable);

    Optional<Item> findByNumber(Integer number);

}
