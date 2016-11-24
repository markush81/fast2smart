package net.fast2smart.external.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import net.fast2smart.external.model.ExternalMember;
import net.fast2smart.legacy.model.Member;
import net.fast2smart.legacy.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping
class MemberResource {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private KafkaTemplate<Long, String> kafkaTemplate;
    @Autowired
    private MemberService memberService;

    @RequestMapping(path = "/members", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ExternalMember> create(@RequestBody ExternalMember externalMember) throws JsonProcessingException {
        Member member = memberService.enrolMember(externalMember.toMember());
        kafkaTemplate.send("enrolments", member.getId(), objectMapper.writeValueAsString(member));
        ExternalMember result = ExternalMember.fromMember(member);
        return ResponseEntity.ok(enrichExternalMember(result));
    }


    @RequestMapping(path = "/members/{cardnumber}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ExternalMember> get(@PathVariable Long cardnumber) {
        Member member = memberService.getByCardnumber(cardnumber);
        if (member == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(enrichExternalMember(ExternalMember.fromMember(member)));
    }

    @RequestMapping(path = "/members", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<ExternalMember>> get() {
        Iterable<Member> members = memberService.getAll();
        return ResponseEntity.ok(Lists.newArrayList(members).stream().map(ExternalMember::fromMember).map(this::enrichExternalMember).collect(Collectors.toList()));
    }

    private ExternalMember enrichExternalMember(ExternalMember result) {
        Long cardnumber = result.getCard().getNumber();
        result.add(linkTo(methodOn(MemberResource.class, cardnumber).get(cardnumber)).withSelfRel());
        result.add(linkTo(methodOn(AccountResource.class, cardnumber).get(cardnumber)).withRel("account"));
        result.add(linkTo(methodOn(TreatmentResource.class, cardnumber).get(cardnumber)).withRel("treatments"));
        result.add(linkTo(methodOn(PurchaseEventResource.class, cardnumber).getByCardNumber(cardnumber)).withRel("purchases"));
        return result;
    }
}
