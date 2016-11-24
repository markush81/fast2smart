package net.fast2smart.legacy.service;

import net.fast2smart.legacy.model.Treatment;
import net.fast2smart.legacy.repository.TreatmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by markus on 10/11/2016.
 */
@Service
public class TreatmentService {

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void assignTreatment(Treatment treatment) {
        treatmentRepository.save(treatment);
    }

    public Iterable<Treatment> getByCardnumber(Long cardnumber) {
        return treatmentRepository.findByMemberCardnumber(cardnumber);
    }
}
