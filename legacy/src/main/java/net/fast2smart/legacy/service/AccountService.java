package net.fast2smart.legacy.service;

import net.fast2smart.legacy.model.Account;
import net.fast2smart.legacy.model.Purchase;
import net.fast2smart.legacy.repository.AccountRepository;
import net.fast2smart.legacy.repository.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by markus on 10/11/2016.
 */
@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;

    @Transactional
    public Purchase bookPurchase(Purchase purchase) {
        Account account = accountRepository.findByMember(purchase.getMember());
        account.update(purchase.getBasePoints(), purchase.getStatusPoints(), purchase.getDate());
        accountRepository.save(account);
        return purchaseRepository.save(purchase);
    }

    public Iterable<Purchase> getAllPurchases() {
        return purchaseRepository.findAll();
    }

    public Iterable<Purchase> getPurchase(Long cardnumber) {
        return purchaseRepository.findByMemberCardnumber(cardnumber);
    }

    public Account getByCardnumber(Long cardnumber) {
        return accountRepository.findByMemberCardnumber(cardnumber);
    }
}
