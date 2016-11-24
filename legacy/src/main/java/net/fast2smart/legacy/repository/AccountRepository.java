package net.fast2smart.legacy.repository;

import net.fast2smart.legacy.model.Account;
import net.fast2smart.legacy.model.Member;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by markus on 22/10/2016.
 */
public interface AccountRepository extends CrudRepository<Account, Long> {

    Account findByMemberCardnumber(Long cardnumber);

    Account findByMember(Member member);
}
