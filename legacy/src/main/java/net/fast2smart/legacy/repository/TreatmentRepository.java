package net.fast2smart.legacy.repository;

import net.fast2smart.legacy.model.Member;
import net.fast2smart.legacy.model.Treatment;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by markus on 22/10/2016.
 */
public interface TreatmentRepository extends CrudRepository<Treatment, Long> {

    Iterable<Treatment> findByMemberCardnumber(Long cardnumber);

    Iterable<Treatment> findByMember(Member member);

}
