package net.fast2smart.external.scheduler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import net.fast2smart.legacy.model.Member;
import net.fast2smart.legacy.model.Partner;
import net.fast2smart.legacy.model.Treatment;
import net.fast2smart.legacy.service.MemberService;
import net.fast2smart.legacy.service.TreatmentService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.RollbackException;
import java.time.LocalDateTime;

/**
 * Singleton for consuming treatments topic. Singleton because of not thread-safe KafkaConsumer
 * <p>
 * Created by markus on 15/11/2016.
 */
@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class TreatmentAssignmentConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TreatmentAssignmentConsumer.class);

    @Autowired
    private Consumer<Long, String> kafkaConsumer;
    @Autowired
    private TreatmentService treatmentService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private ObjectMapper objectMapper;

    @Scheduled(initialDelay = 0, fixedDelay = 500L)
    public void consumeTreatmentAssignments() {
        ConsumerRecords<Long, String> records = kafkaConsumer.poll(10);
        records.iterator().forEachRemaining(record -> {
            try {
                TreatmentAssignment assignment = objectMapper.readValue(record.value(), TreatmentAssignment.class);
                Member member = memberService.getById(assignment.memberId);
                if (member != null) {
                    treatmentService.assignTreatment(assignment.toTreatment(member));
                } //else report error
            } catch (RollbackException e) {
                //This is intended, since UniqueConstraint in treatment will we violated, which is expected. Of course catching all RollbackException is not the "real" solution.
                LOGGER.debug("Treatment not saved: {}", e.getMessage());
            } catch (Exception e) {
                LOGGER.error("Exception occured: {}", e.getMessage());
            }
        });
        kafkaConsumer.commitSync();
    }

    @PostConstruct
    public void init() {
        kafkaConsumer.subscribe(Lists.newArrayList("treatments"));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TreatmentAssignment {
        Long memberId;
        Partner partner;
        String headline;

        @JsonCreator
        public TreatmentAssignment(@JsonProperty(value = "member", required = true) Long memberId, @JsonProperty(value = "partner", required = true) Partner partner, @JsonProperty(value = "headline", required = true) String headline) {
            this.memberId = memberId;
            this.partner = partner;
            this.headline = headline;
        }

        Treatment toTreatment(Member member) {
            return new Treatment(member, partner, headline, LocalDateTime.now());
        }
    }
}
