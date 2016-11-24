package net.fast2smart.legacy.repository;

import net.fast2smart.legacy.model.Purchase;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by markus on 22/10/2016.
 */
public interface PurchaseRepository extends CrudRepository<Purchase, Long> {

    List<Purchase> findByMemberCardnumber(Long cardnumber);
}
