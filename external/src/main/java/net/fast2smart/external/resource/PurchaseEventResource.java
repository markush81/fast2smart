package net.fast2smart.external.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import net.fast2smart.external.model.PurchaseEvent;
import net.fast2smart.legacy.model.Member;
import net.fast2smart.legacy.model.Purchase;
import net.fast2smart.legacy.service.AccountService;
import net.fast2smart.legacy.service.MemberService;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping
class PurchaseEventResource {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Producer<Long, String> kafkaProducer;
    @Autowired
    private AccountService accountService;
    @Autowired
    private MemberService memberService;

    @RequestMapping(path = "/purchases", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<PurchaseEvent> create(@RequestBody PurchaseEvent purchaseEvent) throws JsonProcessingException {
        Member member = memberService.getByCardnumber(purchaseEvent.getCardnumber());
        if (member == null) {
            return ResponseEntity.badRequest().body(null);
        }
        Purchase purchase = accountService.bookPurchase(purchaseEvent.toPurchase(member));
        //TODO: if no Kafka is running this will block for metadata.fetch.timeout.ms amount of time. Solution: do it totally async. Bascially a KafkaProducer is async but only after metadata has been fetched.
        kafkaProducer.send(new ProducerRecord<>("purchases", purchase.getId(), objectMapper.writeValueAsString(purchase)));
        PurchaseEvent result = PurchaseEvent.fromPurchase(purchase);
        return ResponseEntity.ok(enrichtPurchaseEvent(result));
    }


    @RequestMapping(path = "/purchases", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<PurchaseEvent>> get() {
        Iterable<Purchase> purchases = accountService.getAllPurchases();
        return ResponseEntity.ok(transformResultList(purchases));
    }

    @RequestMapping(path = "/members/{cardnumber}/purchases", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<PurchaseEvent>> getByCardNumber(@PathVariable Long cardnumber) {
        Iterable<Purchase> purchases = accountService.getPurchase(cardnumber);
        return ResponseEntity.ok(transformResultList(purchases));
    }

    private List<PurchaseEvent> transformResultList(Iterable<Purchase> purchases) {
        return Lists.newArrayList(purchases).stream().map(PurchaseEvent::fromPurchase).map(this::enrichtPurchaseEvent).collect(Collectors.toList());
    }

    private PurchaseEvent enrichtPurchaseEvent(PurchaseEvent result) {
        Long cardnumber = result.getCardnumber();
        result.add(linkTo(methodOn(MemberResource.class, cardnumber).get(cardnumber)).withRel("member"));
        result.add(linkTo(methodOn(AccountResource.class, cardnumber).get(cardnumber)).withRel("account"));
        return result;
    }
}
