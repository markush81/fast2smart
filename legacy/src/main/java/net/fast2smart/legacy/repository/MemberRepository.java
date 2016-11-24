package net.fast2smart.legacy.repository;

import net.fast2smart.legacy.model.Member;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by markus on 22/10/2016.
 */
public interface MemberRepository extends CrudRepository<Member, Long> {

    Member findByCardnumber(Long cardnumber);
}
